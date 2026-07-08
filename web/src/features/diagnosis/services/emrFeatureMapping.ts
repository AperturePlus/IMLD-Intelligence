import type { PatientRecordPayload, TernaryFlag } from '@/types/patient'

type ProgressStatus = '' | 'success' | 'warning' | 'exception'

export interface ModelMetadata {
  feature_columns: string[]
  feature_medians?: Record<string, number>
}

export interface DiagnosisIndicator {
  name: string
  value: number
  unit: string
  normal: string
  percentage: number
  status: ProgressStatus
}

const clamp = (value: number, min: number, max: number): number => Math.max(min, Math.min(max, value))

const toNumber = (value: string | number | null | undefined): number | null => {
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : null
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number.parseFloat(value)
    return Number.isFinite(parsed) ? parsed : null
  }
  return null
}

// YES->1, NO->0, UNKNOWN/缺失->null（由调用方回退中位数）
const flagToNumber = (value: TernaryFlag | undefined): number | null => {
  if (value === 'YES') return 1
  if (value === 'NO') return 0
  return null
}

type FeatureAccessor = (record: PatientRecordPayload) => number | null

const liver = (r: PatientRecordPayload) => r.laboratoryScreening.clinicalBiochemistry.liverFunction
const biochem = (r: PatientRecordPayload) => r.laboratoryScreening.clinicalBiochemistry
const basic = (r: PatientRecordPayload) => r.laboratoryScreening.clinicalBasic
const immuno = (r: PatientRecordPayload) => r.laboratoryScreening.clinicalImmunology

// 键必须与 imld_model_meta.json feature_columns 逐字一致（注意全角括号）。
const FEATURE_ACCESSORS: Record<string, FeatureAccessor> = {
  gender: (r) => (r.gender === '女' ? 0 : 1),
  age: (r) => toNumber(r.age),
  Smoking: (r) => flagToNumber(r.history.diseaseHistory.smokingHistory),
  Drinking: (r) => flagToNumber(r.history.diseaseHistory.drinkingHistory),
  '糖尿病病史': (r) => flagToNumber(r.history.diseaseHistory.diabetesHistory),
  '高血压病史': (r) => flagToNumber(r.history.diseaseHistory.hypertensionHistory),
  '高尿酸血症病史': (r) => flagToNumber(r.history.diseaseHistory.hyperuricemiaHistory),
  '高脂血症病史': (r) => flagToNumber(r.history.diseaseHistory.hyperlipidemiaHistory),
  '乙肝病史': (r) => flagToNumber(r.history.diseaseHistory.hepatitisBHistory),
  NAS: (r) => (r.pathology.performed ? toNumber(r.pathology.nasScore) : null),
  'TBIL(μmol/L)': (r) => toNumber(liver(r).tbil),
  'DBIL(μmol/L)': (r) => toNumber(liver(r).dbil),
  'IBIL(μmol/L)': (r) => toNumber(liver(r).ibil),
  'ALT(U/L)': (r) => toNumber(liver(r).alt),
  'AST(U/L)': (r) => toNumber(liver(r).ast),
  'TP(g/L)': (r) => toNumber(liver(r).tp),
  'ALB(g/L)': (r) => toNumber(liver(r).alb),
  'GLB(g/L)': (r) => toNumber(liver(r).glob),
  'GLU(mmol/L)': (r) => toNumber(liver(r).glu),
  'URIC(μmol/L)': (r) => toNumber(biochem(r).renalFunction.uric),
  'TG(mmol/L)': (r) => toNumber(liver(r).tg),
  'CHOL(mmol/L)': (r) => toNumber(liver(r).chol),
  'HDL-C(mmol/L)': (r) => toNumber(liver(r).hdlC),
  'LDL-C(mmol/L)': (r) => toNumber(liver(r).ldlC),
  'ALP(U/L)': (r) => toNumber(liver(r).alp),
  'GGT(U/L)': (r) => toNumber(liver(r).ggt),
  'TBA(μmol/L)': (r) => toNumber(liver(r).tba),
  'NH3(μmol/L)': (r) => toNumber(liver(r).nh3),
  'PLT(10^9/L)': (r) => toNumber(basic(r).bloodRoutine.plt),
  'WBC(10^9/L)': (r) => toNumber(basic(r).bloodRoutine.wbc),
  ceruloplasmin: (r) => toNumber(biochem(r).metabolism.cer),
  'PIVKA（mAU/mL）': (r) => toNumber(basic(r).coagulation.pivka),
  'PT(s)': (r) => toNumber(basic(r).coagulation.pt),
  INR: (r) => toNumber(basic(r).coagulation.inr),
  'CRP(mg/L)': (r) => toNumber(biochem(r).inflammation.crp),
  'IgG(g/L)': (r) => toNumber(immuno(r).antibody.igg),
  'IgA(g/L)': (r) => toNumber(immuno(r).antibody.iga),
  'IgM(g/L)': (r) => toNumber(immuno(r).antibody.igm)
  // STE（Kpa）, USSS: EMR 无对应字段 -> 中位数回退
}

export const buildFeatureVectorFromRecord = (
  metadata: ModelMetadata,
  record: PatientRecordPayload
): Float32Array => {
  const medians = metadata.feature_medians || {}
  return new Float32Array(
    metadata.feature_columns.map((feature) => {
      const accessor = FEATURE_ACCESSORS[feature]
      const fromRecord = accessor ? accessor(record) : null
      const resolved = fromRecord == null ? medians[feature] ?? 0 : fromRecord
      return Number.isFinite(resolved) ? resolved : 0
    })
  )
}

interface IndicatorDef {
  name: string
  unit: string
  low?: number
  high?: number
  normalLabel: string
  abnormal: 'high' | 'low'
  read: FeatureAccessor
}

const INDICATOR_DEFS: Record<string, IndicatorDef> = {
  TBIL: { name: 'TBIL', unit: 'μmol/L', low: 3.4, high: 17.1, normalLabel: '3.4-17.1', abnormal: 'high', read: (r) => toNumber(liver(r).tbil) },
  DBIL: { name: 'DBIL', unit: 'μmol/L', high: 6.8, normalLabel: '0-6.8', abnormal: 'high', read: (r) => toNumber(liver(r).dbil) },
  IBIL: { name: 'IBIL', unit: 'μmol/L', high: 13.7, normalLabel: '1.7-13.7', abnormal: 'high', read: (r) => toNumber(liver(r).ibil) },
  ALT: { name: 'ALT', unit: 'U/L', high: 40, normalLabel: '0-40', abnormal: 'high', read: (r) => toNumber(liver(r).alt) },
  AST: { name: 'AST', unit: 'U/L', high: 40, normalLabel: '15-40', abnormal: 'high', read: (r) => toNumber(liver(r).ast) },
  GGT: { name: 'GGT', unit: 'U/L', high: 60, normalLabel: '0-60', abnormal: 'high', read: (r) => toNumber(liver(r).ggt) },
  ALP: { name: 'ALP', unit: 'U/L', high: 150, normalLabel: '45-150', abnormal: 'high', read: (r) => toNumber(liver(r).alp) },
  TBA: { name: 'TBA', unit: 'μmol/L', high: 10, normalLabel: '0-10', abnormal: 'high', read: (r) => toNumber(liver(r).tba) },
  ceruloplasmin: { name: '铜蓝蛋白', unit: 'mg/L', low: 200, high: 600, normalLabel: '200-600', abnormal: 'low', read: (r) => toNumber(biochem(r).metabolism.cer) },
  AAT: { name: 'α1-抗胰蛋白酶', unit: 'mg/L', low: 900, high: 2000, normalLabel: '900-2000', abnormal: 'low', read: (r) => toNumber(biochem(r).metabolism.aat) },
  GLU_LOW: { name: 'GLU', unit: 'mmol/L', low: 3.9, high: 6.1, normalLabel: '3.9-6.1', abnormal: 'low', read: (r) => toNumber(liver(r).glu) },
  TG: { name: 'TG', unit: 'mmol/L', high: 1.7, normalLabel: '<1.7', abnormal: 'high', read: (r) => toNumber(liver(r).tg) },
  CHOL: { name: 'CHOL', unit: 'mmol/L', high: 5.2, normalLabel: '<5.2', abnormal: 'high', read: (r) => toNumber(liver(r).chol) },
  LDL: { name: 'LDL-C', unit: 'mmol/L', high: 3.4, normalLabel: '<3.4', abnormal: 'high', read: (r) => toNumber(liver(r).ldlC) },
  GLU: { name: 'GLU', unit: 'mmol/L', low: 3.9, high: 6.1, normalLabel: '3.9-6.1', abnormal: 'high', read: (r) => toNumber(liver(r).glu) },
  URIC: { name: 'URIC', unit: 'μmol/L', high: 420, normalLabel: '<420', abnormal: 'high', read: (r) => toNumber(biochem(r).renalFunction.uric) },
  NH3: { name: 'NH3', unit: 'μmol/L', high: 72, normalLabel: '18-72', abnormal: 'high', read: (r) => toNumber(liver(r).nh3) }
}

const DISEASE_INDICATOR_KEYS: Array<{ match: (name: string) => boolean; keys: string[] }> = [
  { match: (n) => n.includes('Wilson') || n.includes('肝豆'), keys: ['ceruloplasmin', 'ALT', 'AST', 'TBIL'] },
  { match: (n) => n.includes('Citrin') || n.includes('SLC25A13'), keys: ['DBIL', 'GLU_LOW', 'TG', 'NH3'] },
  { match: (n) => n.includes('PFIC2') || n.includes('ABCB11'), keys: ['DBIL', 'TBA', 'ALT', 'GGT'] },
  { match: (n) => n.includes('PFIC3') || n.includes('ABCB4'), keys: ['DBIL', 'GGT', 'TBA', 'ALP'] },
  { match: (n) => n.includes('Gilbert') || n.includes('吉尔伯特'), keys: ['IBIL', 'TBIL', 'ALT'] },
  { match: (n) => n.includes('Dubin'), keys: ['DBIL', 'TBIL', 'ALT'] },
  { match: (n) => n.includes('Alagille') || n.includes('JAG1') || n.includes('NOTCH2'), keys: ['DBIL', 'GGT', 'ALP', 'TBA'] },
  { match: (n) => n.includes('NTCP') || n.includes('SLC10A1'), keys: ['TBA', 'ALT', 'TBIL'] },
  { match: (n) => n.includes('脂肪酶') || n.includes('LIPA'), keys: ['LDL', 'TG', 'CHOL', 'ALT'] },
  { match: (n) => n.includes('糖原') || n.includes('G6PC'), keys: ['GLU_LOW', 'TG', 'URIC', 'ALT'] },
  { match: (n) => n.includes('血色'), keys: ['ALT', 'AST', 'GGT', 'TBIL', 'GLU'] },
  { match: (n) => n.includes('抗胰蛋白酶'), keys: ['AAT', 'ALT', 'AST', 'TBIL'] },
  { match: (n) => n.includes('脂肪') || n.includes('代谢'), keys: ['TG', 'CHOL', 'GLU', 'ALT'] }
]
const DEFAULT_INDICATOR_KEYS = ['ALT', 'AST', 'ceruloplasmin', 'TBIL']

const statusFor = (value: number, def: IndicatorDef): ProgressStatus => {
  if (def.abnormal === 'high' && def.high != null) {
    if (value >= def.high * 1.8) return 'exception'
    if (value > def.high) return 'warning'
    return ''
  }
  if (def.abnormal === 'low' && def.low != null) {
    if (value <= def.low * 0.5) return 'exception'
    if (value < def.low) return 'warning'
    return ''
  }
  return ''
}

const percentageFor = (value: number, def: IndicatorDef): number => {
  if (def.abnormal === 'high' && def.high != null) {
    return clamp(Math.round((value / (def.high * 2)) * 100), 10, 95)
  }
  if (def.abnormal === 'low' && def.low != null) {
    const severity = value > 0 ? def.low / value : 2
    return clamp(Math.round((severity / 2) * 95), 10, 95)
  }
  return 50
}

export const buildIndicatorsFromRecord = (
  record: PatientRecordPayload,
  diseaseName: string
): DiagnosisIndicator[] => {
  const group = DISEASE_INDICATOR_KEYS.find((item) => item.match(diseaseName))
  const keys = group ? group.keys : DEFAULT_INDICATOR_KEYS
  const indicators: DiagnosisIndicator[] = []
  for (const key of keys) {
    const def = INDICATOR_DEFS[key]
    if (!def) continue
    const value = def.read(record)
    if (value == null) continue
    indicators.push({
      name: def.name,
      value,
      unit: def.unit,
      normal: def.normalLabel,
      percentage: percentageFor(value, def),
      status: statusFor(value, def)
    })
  }
  return indicators
}
