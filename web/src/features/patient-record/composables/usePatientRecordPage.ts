import { nextTick, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormItemRule, FormRules } from 'element-plus'
import patientApi from '@/api/patient'
import type {
  EncounterType,
  ImportSourceType,
  PatientImportPreview,
  PatientRecordClinicalDecision,
  PatientRecordGeneticMethod,
  PatientRecordGeneticSequencing,
  PatientRecordGeneticSourceType,
  PatientRecordGeneticVariantItem,
  PatientRecordImagingModality,
  PatientRecordImagingReportItem,
  PatientRecordImagingSourceType,
  PatientRecordImportMeta,
  PatientRecordLaboratoryScreening,
  PatientRecordPathology,
  PatientRecordPathologySourceType,
  PatientRecordPayload,
  TernaryFlag
} from '@/types/patient'
import {
  createInitialLaboratoryScreening,
  normalizeLaboratoryScreening
} from '@/features/patient-record/constants/laboratoryScreening'
import {
  PATIENT_RECORD_DRAFT_LIMIT,
  deletePatientRecordDraft,
  getPatientRecordDraft,
  listPatientRecordDraftSummaries,
  migrateLegacyPatientRecordDraft,
  savePatientRecordDraft,
  type PatientRecordDraftSummary
} from '@/features/patient-record/services/patientRecordDraftStorage'

const TRI_STATE_VALUES = ['YES', 'NO', 'UNKNOWN'] as const
const IMAGING_SOURCE_TYPES = ['MANUAL', 'IMAGE_OCR', 'PDF_OCR', 'PACS_IMPORT'] as const
const PATHOLOGY_SOURCE_TYPES = ['MANUAL', 'IMAGE_OCR', 'PDF_OCR', 'PACS_IMPORT'] as const
const GENETIC_SOURCE_TYPES = ['MANUAL', 'IMAGE_OCR', 'PDF_OCR', 'HIS_LIS'] as const
const GENETIC_METHODS = ['PANEL', 'WES', 'WGS', 'OTHER', ''] as const
const IMAGING_MODALITIES = ['CT', 'ULTRASOUND', 'MRI', 'OTHER'] as const

type TriStateFormValue = TernaryFlag | ''
type PatientRecordTabName = 'basic' | 'clinical' | 'laboratory' | 'imaging' | 'pathology' | 'genetic' | 'clinicalDecision'

interface RequiredFieldDefinition {
  field: string
  label: string
  tab: PatientRecordTabName
  isActive?: (formData: PatientRecordFormModel) => boolean
}

export interface PatientRecordValidationField {
  field: string
  label: string
  tab: PatientRecordTabName
}

export interface PatientRecordValidationSummary {
  count: number
  firstField: string
  firstLabel: string
  fields: PatientRecordValidationField[]
}

interface PatientRecordDiseaseHistoryFormModel {
  smokingHistory: TriStateFormValue
  drinkingHistory: TriStateFormValue
  diabetesHistory: TriStateFormValue
  hypertensionHistory: TriStateFormValue
  hyperuricemiaHistory: TriStateFormValue
  hyperlipidemiaHistory: TriStateFormValue
  coronaryHeartDiseaseHistory: TriStateFormValue
  hepatitisBHistory: TriStateFormValue
}

interface PatientRecordConditionalHistoryFormModel {
  status: TriStateFormValue
  detail: string
}

interface PatientRecordHistoryFormModel {
  diseaseHistory: PatientRecordDiseaseHistoryFormModel
  surgeryHistory: PatientRecordConditionalHistoryFormModel
  transfusionHistory: PatientRecordConditionalHistoryFormModel
  allergyHistory: string
  medicationHistory: string
  familyHistory: string
}

interface PatientRecordPhysicalExamFormModel {
  heightCm: number | null
  weightKg: number | null
  bmi: number | null
  bloodPressureSystolic: number | null
  bloodPressureDiastolic: number | null
  respiratoryRate: number | null
  heartRate: number | null
  liverFibrosis: TriStateFormValue
  cirrhosis: TriStateFormValue
  fattyLiver: TriStateFormValue
  liverFailure: TriStateFormValue
  cholestasis: TriStateFormValue
  viralHepatitis: TriStateFormValue
}

export interface PatientRecordImagingReportFormModel
  extends Omit<PatientRecordImagingReportItem, 'examinedAt'> {
  localId: string
  examinedAt: Date | string | null
}

export interface PatientRecordGeneticVariantFormModel extends PatientRecordGeneticVariantItem {
  localId: string
}

export interface PatientRecordPathologyFormModel extends Omit<PatientRecordPathology, 'reportedAt'> {
  reportedAt: Date | string | null
}

export interface PatientRecordGeneticSequencingFormModel
  extends Omit<PatientRecordGeneticSequencing, 'reportDate' | 'variants'> {
  reportDate: Date | string | null
  variants: PatientRecordGeneticVariantFormModel[]
}

export interface PatientRecordFormModel {
  patientNo: string
  name: string
  gender: string
  age: number | null
  visitDate: Date | string | null
  phone: string
  idCard: string
  occupation: string
  currentAddress: string
  nativePlace: string
  department: string
  encounterType: EncounterType | ''
  consanguinity: boolean
  chiefComplaint: string
  presentIllness: string
  history: PatientRecordHistoryFormModel
  physicalExam: PatientRecordPhysicalExamFormModel
  laboratoryScreening: PatientRecordLaboratoryScreening
  imagingReports: PatientRecordImagingReportFormModel[]
  pathology: PatientRecordPathologyFormModel
  geneticSequencing: PatientRecordGeneticSequencingFormModel
  clinicalDecision: PatientRecordClinicalDecision
}

const createLocalId = (prefix: string): string =>
  `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`

const createVisitId = (): string => `VISIT-${Date.now().toString().slice(-8)}`

const createInitialDiseaseHistory = (): PatientRecordDiseaseHistoryFormModel => ({
  smokingHistory: '',
  drinkingHistory: '',
  diabetesHistory: '',
  hypertensionHistory: '',
  hyperuricemiaHistory: '',
  hyperlipidemiaHistory: '',
  coronaryHeartDiseaseHistory: '',
  hepatitisBHistory: ''
})

const createInitialConditionalHistory = (): PatientRecordConditionalHistoryFormModel => ({
  status: '',
  detail: ''
})

const createInitialHistory = (): PatientRecordHistoryFormModel => ({
  diseaseHistory: createInitialDiseaseHistory(),
  surgeryHistory: createInitialConditionalHistory(),
  transfusionHistory: createInitialConditionalHistory(),
  allergyHistory: '',
  medicationHistory: '',
  familyHistory: ''
})

const createInitialPhysicalExam = (): PatientRecordPhysicalExamFormModel => ({
  heightCm: null,
  weightKg: null,
  bmi: null,
  bloodPressureSystolic: null,
  bloodPressureDiastolic: null,
  respiratoryRate: null,
  heartRate: null,
  liverFibrosis: '',
  cirrhosis: '',
  fattyLiver: '',
  liverFailure: '',
  cholestasis: '',
  viralHepatitis: ''
})

const createInitialImagingReport = (
  modality: PatientRecordImagingModality = 'CT',
  overrides: Partial<PatientRecordImagingReportFormModel> = {}
): PatientRecordImagingReportFormModel => ({
  localId: createLocalId('img'),
  modality,
  reportText: '',
  examinedAt: null,
  fileId: null,
  sourceType: 'MANUAL',
  ...overrides
})

const createInitialPathology = (): PatientRecordPathologyFormModel => ({
  performed: false,
  reportText: '',
  nasScore: null,
  reportedAt: null,
  fileId: null,
  sourceType: 'MANUAL'
})

const createInitialGeneticVariant = (
  overrides: Partial<PatientRecordGeneticVariantFormModel> = {}
): PatientRecordGeneticVariantFormModel => ({
  localId: createLocalId('variant'),
  gene: '',
  hgvsC: '',
  hgvsP: '',
  variantType: '',
  zygosity: '',
  classification: '',
  evidence: '',
  ...overrides
})

const createInitialGeneticSequencing = (): PatientRecordGeneticSequencingFormModel => ({
  tested: false,
  method: '',
  reportSource: '',
  reportDate: null,
  summary: '',
  conclusion: '',
  fileId: null,
  sourceType: 'MANUAL',
  variants: []
})

const createInitialClinicalDecision = (): PatientRecordClinicalDecision => ({
  diagnosis: '',
  treatmentPlan: ''
})

const createInitialFormData = (): PatientRecordFormModel => ({
  patientNo: '',
  name: '',
  gender: '',
  age: null,
  visitDate: new Date(),
  phone: '',
  idCard: '',
  occupation: '',
  currentAddress: '',
  nativePlace: '',
  department: '',
  encounterType: '',
  consanguinity: false,
  chiefComplaint: '',
  presentIllness: '',
  history: createInitialHistory(),
  physicalExam: createInitialPhysicalExam(),
  laboratoryScreening: createInitialLaboratoryScreening(),
  imagingReports: [],
  pathology: createInitialPathology(),
  geneticSequencing: createInitialGeneticSequencing(),
  clinicalDecision: createInitialClinicalDecision()
})

const createInitialImportMeta = (): PatientRecordImportMeta => ({
  sourceType: '',
  traceId: '',
  confidence: null,
  importedAt: ''
})

const REQUIRED_FIELD_DEFINITIONS: RequiredFieldDefinition[] = [
  { field: 'patientNo', label: '病人ID号', tab: 'basic' },
  { field: 'name', label: '患者姓名', tab: 'basic' },
  { field: 'gender', label: '性别', tab: 'basic' },
  { field: 'age', label: '年龄', tab: 'basic' },
  { field: 'visitDate', label: '就诊日期', tab: 'basic' },
  { field: 'encounterType', label: '就诊方式', tab: 'basic' },
  { field: 'department', label: '科室', tab: 'basic' },
  { field: 'occupation', label: '职业', tab: 'basic' },
  { field: 'currentAddress', label: '现住址', tab: 'basic' },
  { field: 'nativePlace', label: '籍贯', tab: 'basic' },
  { field: 'chiefComplaint', label: '主诉', tab: 'clinical' },
  { field: 'presentIllness', label: '现病史', tab: 'clinical' },
  { field: 'history.diseaseHistory.smokingHistory', label: '吸烟史', tab: 'clinical' },
  { field: 'history.diseaseHistory.drinkingHistory', label: '饮酒史', tab: 'clinical' },
  { field: 'history.diseaseHistory.diabetesHistory', label: '糖尿病史', tab: 'clinical' },
  { field: 'history.diseaseHistory.hypertensionHistory', label: '高血压史', tab: 'clinical' },
  { field: 'history.diseaseHistory.hyperuricemiaHistory', label: '高尿酸血症史', tab: 'clinical' },
  { field: 'history.diseaseHistory.hyperlipidemiaHistory', label: '高脂血症史', tab: 'clinical' },
  { field: 'history.diseaseHistory.coronaryHeartDiseaseHistory', label: '冠心病史', tab: 'clinical' },
  { field: 'history.diseaseHistory.hepatitisBHistory', label: '乙肝病史', tab: 'clinical' },
  { field: 'history.surgeryHistory.status', label: '手术史', tab: 'clinical' },
  {
    field: 'history.surgeryHistory.detail',
    label: '手术史明细',
    tab: 'clinical',
    isActive: (formData) => formData.history.surgeryHistory.status === 'YES'
  },
  { field: 'history.transfusionHistory.status', label: '输血史', tab: 'clinical' },
  {
    field: 'history.transfusionHistory.detail',
    label: '输血史明细',
    tab: 'clinical',
    isActive: (formData) => formData.history.transfusionHistory.status === 'YES'
  },
  { field: 'history.allergyHistory', label: '过敏史', tab: 'clinical' },
  { field: 'history.medicationHistory', label: '用药史', tab: 'clinical' },
  { field: 'history.familyHistory', label: '家族史', tab: 'clinical' },
  { field: 'physicalExam.heightCm', label: '身高', tab: 'clinical' },
  { field: 'physicalExam.weightKg', label: '体重', tab: 'clinical' },
  { field: 'physicalExam.bloodPressureSystolic', label: '收缩压', tab: 'clinical' },
  { field: 'physicalExam.bloodPressureDiastolic', label: '舒张压', tab: 'clinical' },
  { field: 'physicalExam.respiratoryRate', label: '呼吸频率', tab: 'clinical' },
  { field: 'physicalExam.heartRate', label: '心率', tab: 'clinical' },
  { field: 'physicalExam.liverFibrosis', label: '肝纤维化', tab: 'clinical' },
  { field: 'physicalExam.cirrhosis', label: '肝硬化', tab: 'clinical' },
  { field: 'physicalExam.fattyLiver', label: '脂肪肝', tab: 'clinical' },
  { field: 'physicalExam.liverFailure', label: '肝衰竭', tab: 'clinical' },
  { field: 'physicalExam.cholestasis', label: '胆汁淤积', tab: 'clinical' },
  { field: 'physicalExam.viralHepatitis', label: '病毒性肝炎', tab: 'clinical' },
  {
    field: 'pathology.reportText',
    label: '肝穿刺活检结果',
    tab: 'pathology',
    isActive: (formData) => formData.pathology.performed
  },
  {
    field: 'geneticSequencing.method',
    label: '检测方法',
    tab: 'genetic',
    isActive: (formData) => formData.geneticSequencing.tested
  },
  { field: 'clinicalDecision.diagnosis', label: '初步诊断', tab: 'clinicalDecision' }
]

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value)

const isTernaryFlag = (value: unknown): value is TernaryFlag =>
  typeof value === 'string' && TRI_STATE_VALUES.includes(value as TernaryFlag)

const normalizeText = (value: unknown): string => (typeof value === 'string' ? value.trim() : '')

const getValueByPath = (source: unknown, path: string): unknown =>
  path.split('.').reduce<unknown>((current, segment) => {
    if (!isRecord(current)) {
      return undefined
    }
    return current[segment]
  }, source)

const isMissingRequiredValue = (value: unknown): boolean => {
  if (value === null || value === undefined) {
    return true
  }

  if (typeof value === 'string') {
    return !value.trim()
  }

  if (typeof value === 'number') {
    return !Number.isFinite(value) || value <= 0
  }

  return false
}

const normalizeNullableText = (value: unknown): string | null => {
  const text = normalizeText(value)
  return text ? text : null
}

const normalizeNumber = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number.parseFloat(value)
    return Number.isFinite(parsed) ? parsed : null
  }

  return null
}

const normalizeNasScore = (value: unknown): number | null => {
  const parsed = normalizeNumber(value)
  if (parsed === null) {
    return null
  }
  return Math.max(0, Math.min(8, Math.round(parsed)))
}

const normalizeTriStateFormValue = (value: unknown): TriStateFormValue => {
  if (isTernaryFlag(value)) {
    return value
  }

  if (value === true) {
    return 'YES'
  }

  if (value === false) {
    return 'NO'
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (trimmed === '有' || trimmed === '是' || trimmed === '阳性') {
      return 'YES'
    }
    if (trimmed === '无' || trimmed === '否' || trimmed === '阴性' || trimmed === '未见') {
      return 'NO'
    }
    if (trimmed === '未查') {
      return 'UNKNOWN'
    }
  }

  return ''
}

const normalizeEncounterType = (value: unknown): EncounterType | '' => {
  if (value === 'OUTPATIENT' || value === 'EMERGENCY' || value === 'INPATIENT') {
    return value
  }
  return ''
}

const normalizeVisitDate = (value: Date | string | null): string => {
  if (!value) {
    return ''
  }

  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) {
    return ''
  }

  return parsed.toISOString().slice(0, 10)
}

const normalizeOptionalDate = (value: Date | string | null | undefined): string | undefined => {
  const normalized = normalizeVisitDate(value ?? null)
  return normalized || undefined
}

const toFormVisitDate = (value: unknown): Date | string | null => {
  if (typeof value !== 'string' || !value.trim()) {
    return null
  }

  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? value : parsed
}

const computeBmi = (heightCm: number | null, weightKg: number | null): number | null => {
  if (!heightCm || !weightKg || heightCm <= 0 || weightKg <= 0) {
    return null
  }

  const heightMeter = heightCm / 100
  const bmi = weightKg / (heightMeter * heightMeter)
  return Number.isFinite(bmi) ? Number(bmi.toFixed(1)) : null
}

const normalizeDiseaseHistoryDraft = (value: unknown): PatientRecordDiseaseHistoryFormModel => {
  const defaults = createInitialDiseaseHistory()
  if (!isRecord(value)) {
    return defaults
  }

  return {
    smokingHistory: normalizeTriStateFormValue(value.smokingHistory),
    drinkingHistory: normalizeTriStateFormValue(value.drinkingHistory),
    diabetesHistory: normalizeTriStateFormValue(value.diabetesHistory),
    hypertensionHistory: normalizeTriStateFormValue(value.hypertensionHistory),
    hyperuricemiaHistory: normalizeTriStateFormValue(value.hyperuricemiaHistory),
    hyperlipidemiaHistory: normalizeTriStateFormValue(value.hyperlipidemiaHistory),
    coronaryHeartDiseaseHistory: normalizeTriStateFormValue(value.coronaryHeartDiseaseHistory),
    hepatitisBHistory: normalizeTriStateFormValue(value.hepatitisBHistory)
  }
}

const normalizeConditionalHistoryDraft = (value: unknown): PatientRecordConditionalHistoryFormModel => {
  const defaults = createInitialConditionalHistory()
  if (!isRecord(value)) {
    return defaults
  }

  return {
    status: normalizeTriStateFormValue(value.status),
    detail: normalizeText(value.detail)
  }
}

const normalizeHistoryDraft = (value: unknown, legacyFamilyHistoryDetail?: unknown): PatientRecordHistoryFormModel => {
  const defaults = createInitialHistory()
  const source = isRecord(value) ? value : {}

  return {
    diseaseHistory: normalizeDiseaseHistoryDraft(source.diseaseHistory),
    surgeryHistory: normalizeConditionalHistoryDraft(source.surgeryHistory),
    transfusionHistory: normalizeConditionalHistoryDraft(source.transfusionHistory),
    allergyHistory: normalizeText(source.allergyHistory),
    medicationHistory: normalizeText(source.medicationHistory),
    familyHistory: normalizeText(source.familyHistory) || normalizeText(legacyFamilyHistoryDetail) || defaults.familyHistory
  }
}

const normalizePhysicalExamDraft = (value: unknown): PatientRecordPhysicalExamFormModel => {
  const defaults = createInitialPhysicalExam()
  if (!isRecord(value)) {
    return defaults
  }

  return {
    heightCm: normalizeNumber(value.heightCm),
    weightKg: normalizeNumber(value.weightKg),
    bmi: normalizeNumber(value.bmi),
    bloodPressureSystolic: normalizeNumber(value.bloodPressureSystolic),
    bloodPressureDiastolic: normalizeNumber(value.bloodPressureDiastolic),
    respiratoryRate: normalizeNumber(value.respiratoryRate),
    heartRate: normalizeNumber(value.heartRate),
    liverFibrosis: normalizeTriStateFormValue(value.liverFibrosis),
    cirrhosis: normalizeTriStateFormValue(value.cirrhosis),
    fattyLiver: normalizeTriStateFormValue(value.fattyLiver),
    liverFailure: normalizeTriStateFormValue(value.liverFailure),
    cholestasis: normalizeTriStateFormValue(value.cholestasis),
    viralHepatitis: normalizeTriStateFormValue(value.viralHepatitis)
  }
}

const normalizeImportMetaDraft = (value: unknown): PatientRecordImportMeta => {
  const defaults = createInitialImportMeta()
  if (!isRecord(value)) {
    return defaults
  }

  return {
    sourceType:
      value.sourceType === 'HIS_LIS' || value.sourceType === 'IMAGE_OCR' || value.sourceType === 'PDF_OCR'
        ? value.sourceType
        : defaults.sourceType,
    traceId: normalizeText(value.traceId),
    confidence: normalizeNumber(value.confidence),
    importedAt: normalizeText(value.importedAt)
  }
}

const normalizeConditionalHistoryPayload = (
  value: PatientRecordConditionalHistoryFormModel
): { status: TernaryFlag; detail: string } => ({
  status: value.status as TernaryFlag,
  detail: value.status === 'YES' ? normalizeText(value.detail) : ''
})

const normalizeImagingSourceType = (value: unknown): PatientRecordImagingSourceType =>
  IMAGING_SOURCE_TYPES.includes(value as PatientRecordImagingSourceType)
    ? (value as PatientRecordImagingSourceType)
    : 'MANUAL'

const normalizePathologySourceType = (value: unknown): PatientRecordPathologySourceType =>
  PATHOLOGY_SOURCE_TYPES.includes(value as PatientRecordPathologySourceType)
    ? (value as PatientRecordPathologySourceType)
    : 'MANUAL'

const normalizeGeneticSourceType = (value: unknown): PatientRecordGeneticSourceType =>
  GENETIC_SOURCE_TYPES.includes(value as PatientRecordGeneticSourceType)
    ? (value as PatientRecordGeneticSourceType)
    : 'MANUAL'

const normalizeGeneticMethod = (value: unknown): PatientRecordGeneticMethod => {
  if (typeof value !== 'string') {
    return ''
  }
  const normalized = value.trim().toUpperCase()
  return GENETIC_METHODS.includes(normalized as PatientRecordGeneticMethod)
    ? (normalized as PatientRecordGeneticMethod)
    : ''
}

const normalizeImagingModality = (value: unknown): PatientRecordImagingModality => {
  if (typeof value !== 'string') {
    return 'OTHER'
  }
  const normalized = value.trim().toUpperCase()
  if (normalized === 'US' || normalized === 'ULTRASOUND' || normalized === '超声') {
    return 'ULTRASOUND'
  }
  if (normalized === 'CT') {
    return 'CT'
  }
  if (normalized === 'MRI') {
    return 'MRI'
  }
  return IMAGING_MODALITIES.includes(normalized as PatientRecordImagingModality)
    ? (normalized as PatientRecordImagingModality)
    : 'OTHER'
}

const normalizeImagingReportDraft = (value: unknown): PatientRecordImagingReportFormModel => {
  if (!isRecord(value)) {
    return createInitialImagingReport('CT')
  }

  return createInitialImagingReport(normalizeImagingModality(value.modality), {
    reportText: normalizeText(value.reportText),
    examinedAt: toFormVisitDate(value.examinedAt),
    fileId: normalizeNullableText(value.fileId),
    sourceType: normalizeImagingSourceType(value.sourceType)
  })
}

const normalizeImagingReportsDraft = (
  value: unknown,
  legacyImagingResult?: unknown
): PatientRecordImagingReportFormModel[] => {
  const reports = Array.isArray(value)
    ? value.map((item) => normalizeImagingReportDraft(item))
    : []

  if (reports.length > 0) {
    return reports
  }

  const legacyText = normalizeText(legacyImagingResult)
  if (!legacyText) {
    return []
  }

  return [
    createInitialImagingReport('OTHER', {
      reportText: legacyText
    })
  ]
}

const normalizePathologyDraft = (
  value: unknown,
  legacyBiopsyResult?: unknown
): PatientRecordPathologyFormModel => {
  const defaults = createInitialPathology()
  if (!isRecord(value)) {
    const legacyText = normalizeText(legacyBiopsyResult)
    return {
      ...defaults,
      performed: Boolean(legacyText),
      reportText: legacyText
    }
  }

  const reportText = normalizeText(value.reportText) || normalizeText(legacyBiopsyResult)
  const performed =
    typeof value.performed === 'boolean'
      ? value.performed
      : Boolean(reportText)

  return {
    performed,
    reportText,
    nasScore: normalizeNasScore(value.nasScore),
    reportedAt: toFormVisitDate(value.reportedAt),
    fileId: normalizeNullableText(value.fileId),
    sourceType: normalizePathologySourceType(value.sourceType)
  }
}

const normalizeGeneticVariantDraft = (value: unknown): PatientRecordGeneticVariantFormModel => {
  if (!isRecord(value)) {
    return createInitialGeneticVariant()
  }

  return createInitialGeneticVariant({
    gene: normalizeText(value.gene),
    hgvsC: normalizeText(value.hgvsC),
    hgvsP: normalizeText(value.hgvsP),
    variantType: normalizeText(value.variantType),
    zygosity: normalizeText(value.zygosity),
    classification: normalizeText(value.classification),
    evidence: normalizeText(value.evidence)
  })
}

const normalizeGeneticSequencingDraft = (
  value: unknown,
  legacyGeneticTested?: unknown,
  legacyMutatedGene?: unknown
): PatientRecordGeneticSequencingFormModel => {
  const defaults = createInitialGeneticSequencing()
  if (!isRecord(value)) {
    const tested = typeof legacyGeneticTested === 'boolean' ? legacyGeneticTested : false
    const mutatedGene = normalizeText(legacyMutatedGene)
    return {
      ...defaults,
      tested,
      conclusion: mutatedGene,
      variants: mutatedGene ? [createInitialGeneticVariant({ gene: mutatedGene })] : []
    }
  }

  const variants = Array.isArray(value.variants)
    ? value.variants.map((item) => normalizeGeneticVariantDraft(item))
    : []
  const tested =
    typeof value.tested === 'boolean'
      ? value.tested
      : typeof legacyGeneticTested === 'boolean'
        ? legacyGeneticTested
        : variants.length > 0

  const legacyGene = normalizeText(legacyMutatedGene)
  return {
    tested,
    method: normalizeGeneticMethod(value.method),
    reportSource: normalizeText(value.reportSource),
    reportDate: toFormVisitDate(value.reportDate),
    summary: normalizeText(value.summary),
    conclusion: normalizeText(value.conclusion) || legacyGene,
    fileId: normalizeNullableText(value.fileId),
    sourceType: normalizeGeneticSourceType(value.sourceType),
    variants: variants.length > 0 ? variants : legacyGene ? [createInitialGeneticVariant({ gene: legacyGene })] : []
  }
}

const normalizeClinicalDecisionDraft = (
  value: unknown,
  legacyDiagnosis?: unknown,
  legacyTreatmentPlan?: unknown
): PatientRecordClinicalDecision => {
  if (!isRecord(value)) {
    return {
      diagnosis: normalizeText(legacyDiagnosis),
      treatmentPlan: normalizeText(legacyTreatmentPlan)
    }
  }

  return {
    diagnosis: normalizeText(value.diagnosis) || normalizeText(legacyDiagnosis),
    treatmentPlan: normalizeText(value.treatmentPlan) || normalizeText(legacyTreatmentPlan)
  }
}

const requiredTextRule = (message: string): FormItemRule[] => [
  {
    required: true,
    trigger: 'blur',
    validator: (_rule, value, callback) => {
      if (!normalizeText(value)) {
        callback(new Error(message))
        return
      }
      callback()
    }
  }
]

const requiredSelectRule = (message: string): FormItemRule[] => [{ required: true, message, trigger: 'change' }]

const createPositiveNumberRule = (message: string): FormItemRule[] => [
  {
    required: true,
    trigger: 'change',
    validator: (_rule, value, callback) => {
      if (typeof value !== 'number' || Number.isNaN(value) || value <= 0) {
        callback(new Error(message))
        return
      }
      callback()
    }
  }
]

const hasGeneticVariantContent = (item: PatientRecordGeneticVariantFormModel): boolean =>
  Boolean(
    normalizeText(item.gene) ||
      normalizeText(item.hgvsC) ||
      normalizeText(item.hgvsP) ||
      normalizeText(item.variantType) ||
      normalizeText(item.zygosity) ||
      normalizeText(item.classification) ||
      normalizeText(item.evidence)
  )

const hasImagingContent = (item: PatientRecordImagingReportFormModel): boolean =>
  Boolean(normalizeText(item.reportText) || normalizeOptionalDate(item.examinedAt) || item.fileId)

export interface PatientRecordImportMetaState {
  sourceType: ImportSourceType | ''
  traceId: string
  confidence: number | null
  importedAt: string
}

export const usePatientRecordPage = () => {
  const visitId = ref(createVisitId())
  const activeTab = ref('basic')
  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = reactive<PatientRecordFormModel>(createInitialFormData())
  const importMeta = reactive<PatientRecordImportMetaState>(createInitialImportMeta())
  const validationSummary = ref<PatientRecordValidationSummary | null>(null)
  const drafts = ref<PatientRecordDraftSummary[]>([])
  const activeDraftId = ref<string | null>(null)

  const refreshDrafts = (): void => {
    drafts.value = listPatientRecordDraftSummaries()
  }

  const clearValidationSummary = (): void => {
    validationSummary.value = null
  }

  const getActiveRequiredFields = (): RequiredFieldDefinition[] =>
    REQUIRED_FIELD_DEFINITIONS.filter((item) => !item.isActive || item.isActive(formData))

  const getMissingRequiredFields = (): PatientRecordValidationField[] =>
    getActiveRequiredFields()
      .filter((item) => isMissingRequiredValue(getValueByPath(formData, item.field)))
      .map(({ field, label, tab }) => ({ field, label, tab }))

  const setValidationSummary = (fields: PatientRecordValidationField[]): void => {
    if (fields.length === 0) {
      validationSummary.value = null
      return
    }

    validationSummary.value = {
      count: fields.length,
      firstField: fields[0].field,
      firstLabel: fields[0].label,
      fields
    }
  }

  const rules: FormRules<PatientRecordFormModel> = {
    patientNo: requiredTextRule('病人ID号（院内病历号/患者号）不能为空'),
    name: requiredTextRule('患者姓名不能为空'),
    gender: requiredSelectRule('请选择性别'),
    age: createPositiveNumberRule('请输入年龄'),
    visitDate: requiredSelectRule('请选择就诊日期'),
    occupation: requiredTextRule('职业不能为空'),
    currentAddress: requiredTextRule('现住址不能为空'),
    nativePlace: requiredTextRule('籍贯不能为空'),
    department: requiredTextRule('科室不能为空'),
    encounterType: requiredSelectRule('请选择就诊方式'),
    chiefComplaint: requiredTextRule('主诉不能为空'),
    presentIllness: requiredTextRule('现病史不能为空'),
    'history.diseaseHistory.smokingHistory': requiredSelectRule('请选择吸烟史'),
    'history.diseaseHistory.drinkingHistory': requiredSelectRule('请选择饮酒史'),
    'history.diseaseHistory.diabetesHistory': requiredSelectRule('请选择糖尿病史'),
    'history.diseaseHistory.hypertensionHistory': requiredSelectRule('请选择高血压史'),
    'history.diseaseHistory.hyperuricemiaHistory': requiredSelectRule('请选择高尿酸血症史'),
    'history.diseaseHistory.hyperlipidemiaHistory': requiredSelectRule('请选择高脂血症史'),
    'history.diseaseHistory.coronaryHeartDiseaseHistory': requiredSelectRule('请选择冠心病史'),
    'history.diseaseHistory.hepatitisBHistory': requiredSelectRule('请选择乙肝病史'),
    'history.surgeryHistory.status': requiredSelectRule('请选择手术史'),
    'history.surgeryHistory.detail': [
      {
        trigger: 'blur',
        validator: (_rule, value, callback) => {
          if (formData.history.surgeryHistory.status === 'YES' && !normalizeText(value)) {
            callback(new Error('手术史为“有”时必须填写明细'))
            return
          }
          callback()
        }
      }
    ],
    'history.transfusionHistory.status': requiredSelectRule('请选择输血史'),
    'history.transfusionHistory.detail': [
      {
        trigger: 'blur',
        validator: (_rule, value, callback) => {
          if (formData.history.transfusionHistory.status === 'YES' && !normalizeText(value)) {
            callback(new Error('输血史为“有”时必须填写明细'))
            return
          }
          callback()
        }
      }
    ],
    'history.allergyHistory': requiredTextRule('过敏史不能为空'),
    'history.medicationHistory': requiredTextRule('用药史不能为空'),
    'history.familyHistory': requiredTextRule('家族史不能为空'),
    'physicalExam.heightCm': createPositiveNumberRule('请输入身高'),
    'physicalExam.weightKg': createPositiveNumberRule('请输入体重'),
    'physicalExam.bmi': [
      {
        trigger: 'change',
        validator: (_rule, value, callback) => {
          if (typeof value !== 'number' || Number.isNaN(value) || value <= 0) {
            callback(new Error('BMI 无法计算，请检查身高和体重'))
            return
          }
          callback()
        }
      }
    ],
    'physicalExam.bloodPressureSystolic': createPositiveNumberRule('请输入收缩压'),
    'physicalExam.bloodPressureDiastolic': createPositiveNumberRule('请输入舒张压'),
    'physicalExam.respiratoryRate': createPositiveNumberRule('请输入呼吸频率'),
    'physicalExam.heartRate': createPositiveNumberRule('请输入心率'),
    'physicalExam.liverFibrosis': requiredSelectRule('请选择肝纤维化情况'),
    'physicalExam.cirrhosis': requiredSelectRule('请选择肝硬化情况'),
    'physicalExam.fattyLiver': requiredSelectRule('请选择脂肪肝情况'),
    'physicalExam.liverFailure': requiredSelectRule('请选择肝衰竭情况'),
    'physicalExam.cholestasis': requiredSelectRule('请选择胆汁淤积情况'),
    'physicalExam.viralHepatitis': requiredSelectRule('请选择病毒性肝炎情况'),
    'pathology.reportText': [
      {
        trigger: 'blur',
        validator: (_rule, value, callback) => {
          if (formData.pathology.performed && !normalizeText(value)) {
            callback(new Error('已执行肝穿刺活检时必须填写病理结果'))
            return
          }
          callback()
        }
      }
    ],
    'geneticSequencing.method': [
      {
        trigger: 'change',
        validator: (_rule, value, callback) => {
          if (formData.geneticSequencing.tested && !normalizeGeneticMethod(value)) {
            callback(new Error('已进行基因检测时必须选择检测方法'))
            return
          }
          callback()
        }
      }
    ],
    'clinicalDecision.diagnosis': requiredTextRule('请输入初步诊断')
  }

  const applyFormData = (nextFormData: PatientRecordFormModel): void => {
    Object.assign(formData, nextFormData)
  }

  const serializeDraft = (): Record<string, unknown> => ({
    ...formData,
    visitDate: normalizeVisitDate(formData.visitDate),
    imagingReports: formData.imagingReports.map((item) => ({
      ...item,
      examinedAt: normalizeOptionalDate(item.examinedAt)
    })),
    pathology: {
      ...formData.pathology,
      reportedAt: normalizeOptionalDate(formData.pathology.reportedAt)
    },
    geneticSequencing: {
      ...formData.geneticSequencing,
      reportDate: normalizeOptionalDate(formData.geneticSequencing.reportDate)
    },
    visitId: visitId.value,
    importMeta: {
      ...importMeta
    }
  })

  const normalizePayload = (): PatientRecordPayload => {
    const imagingReports = formData.imagingReports
      .filter((item) => hasImagingContent(item))
      .map<PatientRecordImagingReportItem>((item) => ({
        modality: item.modality,
        reportText: normalizeText(item.reportText),
        examinedAt: normalizeOptionalDate(item.examinedAt),
        fileId: item.fileId,
        sourceType: item.sourceType
      }))

    const pathology: PatientRecordPathology = {
      performed: formData.pathology.performed,
      reportText: formData.pathology.performed ? normalizeText(formData.pathology.reportText) : '',
      nasScore: formData.pathology.performed ? normalizeNasScore(formData.pathology.nasScore) : null,
      reportedAt: formData.pathology.performed ? normalizeOptionalDate(formData.pathology.reportedAt) : undefined,
      fileId: formData.pathology.performed ? formData.pathology.fileId : null,
      sourceType: formData.pathology.sourceType
    }

    const geneticVariants = formData.geneticSequencing.variants
      .filter((item) => hasGeneticVariantContent(item))
      .map<PatientRecordGeneticVariantItem>((item) => ({
        gene: normalizeText(item.gene),
        hgvsC: normalizeText(item.hgvsC) || undefined,
        hgvsP: normalizeText(item.hgvsP) || undefined,
        variantType: normalizeText(item.variantType) || undefined,
        zygosity: normalizeText(item.zygosity) || undefined,
        classification: normalizeText(item.classification) || undefined,
        evidence: normalizeText(item.evidence) || undefined
      }))

    const geneticSequencing: PatientRecordGeneticSequencing = formData.geneticSequencing.tested
      ? {
          tested: true,
          method: normalizeGeneticMethod(formData.geneticSequencing.method),
          reportSource: normalizeText(formData.geneticSequencing.reportSource),
          reportDate: normalizeOptionalDate(formData.geneticSequencing.reportDate),
          summary: normalizeText(formData.geneticSequencing.summary),
          conclusion: normalizeText(formData.geneticSequencing.conclusion),
          fileId: formData.geneticSequencing.fileId,
          sourceType: formData.geneticSequencing.sourceType,
          variants: geneticVariants
        }
      : {
          tested: false,
          method: '',
          reportSource: '',
          summary: '',
          conclusion: '',
          fileId: null,
          sourceType: 'MANUAL',
          variants: []
        }

    return {
      patientNo: normalizeText(formData.patientNo),
      name: normalizeText(formData.name),
      gender: normalizeText(formData.gender),
      age: formData.age,
      visitDate: normalizeVisitDate(formData.visitDate),
      phone: normalizeText(formData.phone),
      idCard: normalizeText(formData.idCard),
      occupation: normalizeText(formData.occupation),
      currentAddress: normalizeText(formData.currentAddress),
      nativePlace: normalizeText(formData.nativePlace),
      department: normalizeText(formData.department),
      encounterType: formData.encounterType as EncounterType,
      consanguinity: formData.consanguinity,
      chiefComplaint: normalizeText(formData.chiefComplaint),
      presentIllness: normalizeText(formData.presentIllness),
      history: {
        diseaseHistory: {
          smokingHistory: formData.history.diseaseHistory.smokingHistory as TernaryFlag,
          drinkingHistory: formData.history.diseaseHistory.drinkingHistory as TernaryFlag,
          diabetesHistory: formData.history.diseaseHistory.diabetesHistory as TernaryFlag,
          hypertensionHistory: formData.history.diseaseHistory.hypertensionHistory as TernaryFlag,
          hyperuricemiaHistory: formData.history.diseaseHistory.hyperuricemiaHistory as TernaryFlag,
          hyperlipidemiaHistory: formData.history.diseaseHistory.hyperlipidemiaHistory as TernaryFlag,
          coronaryHeartDiseaseHistory: formData.history.diseaseHistory.coronaryHeartDiseaseHistory as TernaryFlag,
          hepatitisBHistory: formData.history.diseaseHistory.hepatitisBHistory as TernaryFlag
        },
        surgeryHistory: normalizeConditionalHistoryPayload(formData.history.surgeryHistory),
        transfusionHistory: normalizeConditionalHistoryPayload(formData.history.transfusionHistory),
        allergyHistory: normalizeText(formData.history.allergyHistory),
        medicationHistory: normalizeText(formData.history.medicationHistory),
        familyHistory: normalizeText(formData.history.familyHistory)
      },
      physicalExam: {
        heightCm: formData.physicalExam.heightCm,
        weightKg: formData.physicalExam.weightKg,
        bmi: computeBmi(formData.physicalExam.heightCm, formData.physicalExam.weightKg),
        bloodPressureSystolic: formData.physicalExam.bloodPressureSystolic,
        bloodPressureDiastolic: formData.physicalExam.bloodPressureDiastolic,
        respiratoryRate: formData.physicalExam.respiratoryRate,
        heartRate: formData.physicalExam.heartRate,
        liverFibrosis: formData.physicalExam.liverFibrosis as TernaryFlag,
        cirrhosis: formData.physicalExam.cirrhosis as TernaryFlag,
        fattyLiver: formData.physicalExam.fattyLiver as TernaryFlag,
        liverFailure: formData.physicalExam.liverFailure as TernaryFlag,
        cholestasis: formData.physicalExam.cholestasis as TernaryFlag,
        viralHepatitis: formData.physicalExam.viralHepatitis as TernaryFlag
      },
      laboratoryScreening: normalizeLaboratoryScreening(formData.laboratoryScreening),
      imagingReports,
      pathology,
      geneticSequencing,
      clinicalDecision: {
        diagnosis: normalizeText(formData.clinicalDecision.diagnosis),
        treatmentPlan: normalizeText(formData.clinicalDecision.treatmentPlan)
      },
      visitId: visitId.value,
      importMeta: {
        ...importMeta
      }
    }
  }

  const applyDraftSnapshot = (parsed: Record<string, unknown>): void => {
    const nextFormData = createInitialFormData()

    nextFormData.patientNo = normalizeText(parsed.patientNo)
    nextFormData.name = normalizeText(parsed.name)
    nextFormData.gender = normalizeText(parsed.gender)
    nextFormData.age = normalizeNumber(parsed.age)
    nextFormData.visitDate = toFormVisitDate(parsed.visitDate) ?? nextFormData.visitDate
    nextFormData.phone = normalizeText(parsed.phone)
    nextFormData.idCard = normalizeText(parsed.idCard)
    nextFormData.occupation = normalizeText(parsed.occupation)
    nextFormData.currentAddress = normalizeText(parsed.currentAddress)
    nextFormData.nativePlace = normalizeText(parsed.nativePlace)
    nextFormData.department = normalizeText(parsed.department)
    nextFormData.encounterType = normalizeEncounterType(parsed.encounterType)
    nextFormData.consanguinity =
      typeof parsed.consanguinity === 'boolean' ? parsed.consanguinity : nextFormData.consanguinity
    nextFormData.chiefComplaint = normalizeText(parsed.chiefComplaint)
    nextFormData.presentIllness = normalizeText(parsed.presentIllness)
    nextFormData.history = normalizeHistoryDraft(parsed.history, parsed.familyHistoryDetail)
    nextFormData.physicalExam = normalizePhysicalExamDraft(parsed.physicalExam)
    nextFormData.laboratoryScreening = normalizeLaboratoryScreening(parsed.laboratoryScreening, parsed)
    nextFormData.imagingReports = normalizeImagingReportsDraft(parsed.imagingReports, parsed.imagingResult)
    nextFormData.pathology = normalizePathologyDraft(parsed.pathology, parsed.biopsyResult)
    nextFormData.geneticSequencing = normalizeGeneticSequencingDraft(
      parsed.geneticSequencing,
      parsed.geneticTested,
      parsed.mutatedGene
    )
    nextFormData.clinicalDecision = normalizeClinicalDecisionDraft(
      parsed.clinicalDecision,
      parsed.diagnosis,
      parsed.treatmentPlan
    )

    applyFormData(nextFormData)

    if (typeof parsed.visitId === 'string' && parsed.visitId.trim()) {
      visitId.value = parsed.visitId.trim()
    }

    Object.assign(importMeta, normalizeImportMetaDraft(parsed.importMeta))
    clearValidationSummary()
    formRef.value?.clearValidate()
  }

  const restoreDraftIfNeeded = (): void => {
    migrateLegacyPatientRecordDraft()
    refreshDrafts()

    const latestDraftId = drafts.value[0]?.id
    if (!latestDraftId) {
      return
    }

    const latestDraft = getPatientRecordDraft(latestDraftId)
    if (!latestDraft) {
      return
    }

    applyDraftSnapshot(latestDraft.snapshot)
    activeDraftId.value = latestDraft.id
  }

  restoreDraftIfNeeded()

  watch(
    () => [formData.physicalExam.heightCm, formData.physicalExam.weightKg],
    ([heightCm, weightKg]) => {
      formData.physicalExam.bmi = computeBmi(heightCm, weightKg)
    },
    { immediate: true }
  )

  const applyImportPreview = (preview: PatientImportPreview): void => {
    formData.patientNo = preview.patientNo || formData.patientNo
    formData.name = preview.name || formData.name
    formData.gender = preview.gender || formData.gender
    formData.age = typeof preview.age === 'number' ? preview.age : formData.age
    formData.visitDate = toFormVisitDate(preview.visitDate) ?? formData.visitDate
    formData.phone = preview.phone || formData.phone
    formData.idCard = preview.idCard || formData.idCard
    formData.occupation = preview.occupation || formData.occupation
    formData.currentAddress = preview.currentAddress || formData.currentAddress
    formData.nativePlace = preview.nativePlace || formData.nativePlace
    formData.department = preview.department || formData.department
    formData.encounterType = (preview.encounterType as EncounterType) || formData.encounterType

    importMeta.sourceType = preview.sourceType
    importMeta.traceId = preview.traceId
    importMeta.confidence = preview.confidence
    importMeta.importedAt = new Date().toISOString()
    clearValidationSummary()
  }

  const clearImportMeta = (): void => {
    Object.assign(importMeta, createInitialImportMeta())
  }

  const addImagingReport = (modality: PatientRecordImagingModality = 'CT'): void => {
    formData.imagingReports.push(createInitialImagingReport(modality))
  }

  const removeImagingReport = (localId: string): void => {
    formData.imagingReports = formData.imagingReports.filter((item) => item.localId !== localId)
  }

  const addGeneticVariant = (): void => {
    formData.geneticSequencing.variants.push(createInitialGeneticVariant())
  }

  const removeGeneticVariant = (localId: string): void => {
    formData.geneticSequencing.variants = formData.geneticSequencing.variants.filter((item) => item.localId !== localId)
  }

  const hasCurrentFormContent = (): boolean =>
    Boolean(
      normalizeText(formData.patientNo) ||
        normalizeText(formData.name) ||
        normalizeText(formData.gender) ||
        formData.age !== null ||
        normalizeText(formData.phone) ||
        normalizeText(formData.idCard) ||
        normalizeText(formData.occupation) ||
        normalizeText(formData.currentAddress) ||
        normalizeText(formData.nativePlace) ||
        normalizeText(formData.department) ||
        normalizeText(formData.encounterType) ||
        normalizeText(formData.chiefComplaint) ||
        normalizeText(formData.presentIllness) ||
        Object.values(formData.history.diseaseHistory).some((value) => Boolean(value)) ||
        Boolean(formData.history.surgeryHistory.status) ||
        normalizeText(formData.history.surgeryHistory.detail) ||
        Boolean(formData.history.transfusionHistory.status) ||
        normalizeText(formData.history.transfusionHistory.detail) ||
        normalizeText(formData.history.allergyHistory) ||
        normalizeText(formData.history.medicationHistory) ||
        normalizeText(formData.history.familyHistory) ||
        formData.physicalExam.heightCm !== null ||
        formData.physicalExam.weightKg !== null ||
        formData.physicalExam.bloodPressureSystolic !== null ||
        formData.physicalExam.bloodPressureDiastolic !== null ||
        formData.physicalExam.respiratoryRate !== null ||
        formData.physicalExam.heartRate !== null ||
        Boolean(formData.physicalExam.liverFibrosis) ||
        Boolean(formData.physicalExam.cirrhosis) ||
        Boolean(formData.physicalExam.fattyLiver) ||
        Boolean(formData.physicalExam.liverFailure) ||
        Boolean(formData.physicalExam.cholestasis) ||
        Boolean(formData.physicalExam.viralHepatitis) ||
        formData.imagingReports.some((item) => hasImagingContent(item)) ||
        formData.pathology.performed ||
        normalizeText(formData.pathology.reportText) ||
        formData.geneticSequencing.tested ||
        normalizeText(formData.geneticSequencing.conclusion) ||
        formData.geneticSequencing.variants.some((item) => hasGeneticVariantContent(item)) ||
        normalizeText(formData.clinicalDecision.diagnosis) ||
        normalizeText(formData.clinicalDecision.treatmentPlan) ||
        Boolean(importMeta.sourceType)
    )

  const saveDraft = (): void => {
    try {
      const result = savePatientRecordDraft(serializeDraft(), activeDraftId.value)
      activeDraftId.value = result.draft.id
      refreshDrafts()
      clearValidationSummary()
      ElMessage({
        message: result.isNew ? '草稿已保存至草稿箱' : '草稿已更新',
        type: 'success'
      })
    } catch (error) {
      if (error instanceof Error && error.message === 'PATIENT_RECORD_DRAFT_LIMIT_REACHED') {
        ElMessage.warning(`草稿箱最多保留 ${PATIENT_RECORD_DRAFT_LIMIT} 条，请删除旧草稿后再保存`)
        return
      }
      ElMessage.error('草稿保存失败，请稍后重试')
    }
  }

  const loadDraft = async (draftId: string): Promise<boolean> => {
    if (activeDraftId.value !== draftId && hasCurrentFormContent()) {
      try {
        await ElMessageBox.confirm('载入草稿会覆盖当前页面已填写内容，是否继续？', '载入草稿', {
          confirmButtonText: '继续载入',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch {
        return false
      }
    }

    const draft = getPatientRecordDraft(draftId)
    if (!draft) {
      refreshDrafts()
      ElMessage.warning('该草稿不存在或已被删除')
      return false
    }

    applyDraftSnapshot(draft.snapshot)
    activeDraftId.value = draft.id
    activeTab.value = 'basic'
    refreshDrafts()
    ElMessage.success('已载入草稿')
    return true
  }

  const removeDraft = async (draftId: string): Promise<boolean> => {
    try {
      await ElMessageBox.confirm('删除草稿后不可恢复，是否继续？', '删除草稿', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return false
    }

    deletePatientRecordDraft(draftId)
    if (activeDraftId.value === draftId) {
      activeDraftId.value = null
    }
    refreshDrafts()
    ElMessage.success('草稿已删除')
    return true
  }

  const startNewRecord = async (): Promise<boolean> => {
    if (hasCurrentFormContent()) {
      try {
        await ElMessageBox.confirm('当前页面内容将被清空，已保存草稿仍会保留。是否新建病历？', '新建病历', {
          confirmButtonText: '确认新建',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch {
        return false
      }
    }

    applyFormData(createInitialFormData())
    visitId.value = createVisitId()
    activeTab.value = 'basic'
    activeDraftId.value = null
    formRef.value?.clearValidate()
    clearImportMeta()
    clearValidationSummary()
    refreshDrafts()
    ElMessage.success('已新建病历，可开始录入')
    return true
  }

  const resolveTabByInvalidField = (field: string): PatientRecordTabName => {
    const definition = REQUIRED_FIELD_DEFINITIONS.find((item) => item.field === field)
    if (definition) {
      return definition.tab
    }

    if (
      field === 'chiefComplaint' ||
      field === 'presentIllness' ||
      field.startsWith('history.') ||
      field.startsWith('physicalExam.')
    ) {
      return 'clinical'
    }

    if (field.startsWith('laboratoryScreening.')) {
      return 'laboratory'
    }

    if (field.startsWith('imagingReports.')) {
      return 'imaging'
    }

    if (field.startsWith('pathology.')) {
      return 'pathology'
    }

    if (field.startsWith('geneticSequencing.')) {
      return 'genetic'
    }

    if (field.startsWith('clinicalDecision.')) {
      return 'clinicalDecision'
    }

    return 'basic'
  }

  const goToValidationField = async (field = validationSummary.value?.firstField): Promise<void> => {
    if (!field) {
      return
    }

    activeTab.value = resolveTabByInvalidField(field)
    await nextTick()
    formRef.value?.scrollToField(field)
  }

  const submitForm = async (): Promise<void> => {
    if (!formRef.value || submitting.value) {
      return
    }

    try {
      await formRef.value.validate()
    } catch (invalidFields) {
      const invalidFieldNames = Object.keys((invalidFields as Record<string, unknown>) || {})
      const missingFields = getMissingRequiredFields()
      const fallbackFields = invalidFieldNames.map((field) => {
        const definition = REQUIRED_FIELD_DEFINITIONS.find((item) => item.field === field)
        return {
          field,
          label: definition?.label ?? field,
          tab: definition?.tab ?? resolveTabByInvalidField(field)
        }
      })
      const fields = missingFields.length > 0 ? missingFields : fallbackFields
      setValidationSummary(fields)
      const firstInvalidField = fields[0]?.field || invalidFieldNames[0] || 'patientNo'
      await goToValidationField(firstInvalidField)
      ElMessage.error(validationSummary.value
        ? `请先完善必填项：${validationSummary.value.firstLabel}`
        : '基础信息或必填项未完善，请检查红框字段')
      return
    }

    clearValidationSummary()

    try {
      await ElMessageBox.confirm('确认核对无误并归档该患者病历吗？', '系统提示', {
        confirmButtonText: '确认提交',
        cancelButtonText: '返回修改',
        type: 'warning'
      })
    } catch {
      return
    }

    submitting.value = true
    try {
      const res = await patientApi.createRecord(normalizePayload())
      visitId.value = res.data.visitId || visitId.value
      ElMessage({
        type: 'success',
        message: '病历归档成功，已同步至 AI 辅助诊断中台',
        duration: 2800
      })
      if (activeDraftId.value) {
        deletePatientRecordDraft(activeDraftId.value)
        activeDraftId.value = null
        refreshDrafts()
      }
    } catch {
      ElMessage.error('病历归档失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  }

  const resetForm = (): void => {
    ElMessageBox.confirm('清空后当前录入的所有数据将丢失，是否继续？', '警告', {
      confirmButtonText: '确认清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        applyFormData(createInitialFormData())
        activeTab.value = 'basic'
        formRef.value?.clearValidate()
        clearImportMeta()
        clearValidationSummary()
        if (activeDraftId.value) {
          deletePatientRecordDraft(activeDraftId.value)
          activeDraftId.value = null
          refreshDrafts()
        }
        ElMessage.info('表单已重置')
      })
      .catch(() => {
        // Ignore cancel action.
      })
  }

  return {
    visitId,
    activeTab,
    formRef,
    submitting,
    formData,
    importMeta,
    validationSummary,
    drafts,
    activeDraftId,
    rules,
    clearValidationSummary,
    goToValidationField,
    saveDraft,
    loadDraft,
    removeDraft,
    startNewRecord,
    submitForm,
    resetForm,
    applyImportPreview,
    clearImportMeta,
    addImagingReport,
    removeImagingReport,
    addGeneticVariant,
    removeGeneticVariant
  }
}
