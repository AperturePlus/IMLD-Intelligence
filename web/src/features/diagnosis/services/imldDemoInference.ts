import * as ort from 'onnxruntime-web'
import ortWasmMjsUrl from '../../../../node_modules/onnxruntime-web/dist/ort-wasm-simd-threaded.mjs?url'
import ortWasmUrl from '../../../../node_modules/onnxruntime-web/dist/ort-wasm-simd-threaded.wasm?url'
import modelMetadataUrl from '@/assets/models/imld/imld_model_meta.json?url'
import modelUrl from '@/assets/models/imld/imld_xgboost_model.onnx?url'
import type { DiagnosisResult } from '@/types/diagnosis'
import type { PatientRecordPayload } from '@/types/patient'
import { resolveDiagnosisConfidence } from './confidenceConfig'
import { buildDiseaseDisplayFields } from './diseaseDisplay'
import { buildEvidenceItemsFromDiagnosis, buildEvidenceSummary } from './diagnosisEvidence'
import { buildFeatureVectorFromRecord, buildIndicatorsFromRecord } from './emrFeatureMapping'
import { isBrowserOnnxInferenceMode } from './inferenceMode'
import { riskLevelFromProbability } from './riskLevel'

interface DemoPatient {
  id?: string
  name?: string
  gender?: string
  age?: number
  riskLevel?: string
  disease?: string
}

interface ModelMetadata {
  feature_columns: string[]
  feature_medians?: Record<string, number>
  metrics?: Record<string, number>
  version?: string
}

type DiagnosisPayload = DiagnosisResult

interface OrtTensor {
  data: ArrayLike<number | bigint>
}

interface DemoOrtSession {
  readonly inputNames: readonly string[]
  run: (feeds: Record<string, unknown>) => Promise<Record<string, OrtTensor>>
}

let metadataPromise: Promise<ModelMetadata> | null = null
let sessionPromise: Promise<DemoOrtSession> | null = null

const isDemoInferenceEnabled = (): boolean => {
  return import.meta.env.VITE_USE_MOCK === 'true' && isBrowserOnnxInferenceMode()
}

const loadMetadata = (): Promise<ModelMetadata> => {
  if (!metadataPromise) {
    metadataPromise = fetch(modelMetadataUrl).then((response) => {
      if (!response.ok) {
        throw new Error(`failed to load IMLD model metadata: ${response.status}`)
      }
      return response.json() as Promise<ModelMetadata>
    })
  }
  return metadataPromise
}

const toAbsoluteAssetUrl = (assetUrl: string): string => {
  if (typeof window === 'undefined') {
    return assetUrl
  }
  return new URL(assetUrl, window.location.href).href
}

const configureOrtRuntime = () => {
  const explicitBasePath = import.meta.env.VITE_IMLD_ONNX_WASM_BASE_URL
  const wasmEnv = ort.env.wasm as typeof ort.env.wasm & {
    wasmPaths?: string | { mjs: string; wasm: string }
    numThreads?: number
  }
  wasmEnv.numThreads = 1
  wasmEnv.wasmPaths = explicitBasePath || {
    mjs: toAbsoluteAssetUrl(ortWasmMjsUrl),
    wasm: toAbsoluteAssetUrl(ortWasmUrl)
  }
}

const loadSession = async (): Promise<DemoOrtSession> => {
  if (!sessionPromise) {
    sessionPromise = (async () => {
      configureOrtRuntime()
      const session = await ort.InferenceSession.create(modelUrl, {
        executionProviders: ['wasm']
      })
      return session as unknown as DemoOrtSession
    })()
  }
  return sessionPromise
}

const genderFlag = (gender?: string): number => {
  const normalized = String(gender || '').trim().toLowerCase()
  return normalized === '女' || normalized === 'female' || normalized === 'f' || normalized === '0' ? 0 : 1
}

const clamp = (value: number, min: number, max: number): number => Math.max(min, Math.min(max, value))

const patientOverrides = (patient: DemoPatient): Record<string, number> => {
  const age = Number.isFinite(Number(patient.age)) ? Number(patient.age) : 40
  const gender = genderFlag(patient.gender)
  const disease = patient.disease || ''
  const riskLevel = patient.riskLevel || ''
  const highRisk = riskLevel.includes('高')

  const common = {
    age,
    gender,
    Smoking: gender === 1 && age >= 45 ? 1 : 0,
    Drinking: gender === 1 && age >= 45 ? 1 : 0
  }

  if (disease.includes('Citrin')) {
    return {
      ...common,
      'TBIL(μmol/L)': 42,
      'DBIL(μmol/L)': 20,
      'IBIL(μmol/L)': 22,
      'ALT(U/L)': 92,
      'AST(U/L)': 75,
      'GLU(mmol/L)': 3.2,
      'TG(mmol/L)': 2.8,
      'NH3(μmol/L)': 92,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('PFIC2') || disease.includes('ABCB11')) {
    return {
      ...common,
      'TBIL(μmol/L)': 96,
      'DBIL(μmol/L)': 68,
      'ALT(U/L)': 180,
      'AST(U/L)': 150,
      'GGT(U/L)': 28,
      'ALP(U/L)': 420,
      'TBA(μmol/L)': 180,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('PFIC3') || disease.includes('ABCB4')) {
    return {
      ...common,
      'TBIL(μmol/L)': 72,
      'DBIL(μmol/L)': 48,
      'ALT(U/L)': 132,
      'AST(U/L)': 104,
      'GGT(U/L)': 210,
      'ALP(U/L)': 460,
      'TBA(μmol/L)': 130,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('Gilbert') || disease.includes('吉尔伯特')) {
    return {
      ...common,
      'TBIL(μmol/L)': 36,
      'DBIL(μmol/L)': 5,
      'IBIL(μmol/L)': 31,
      'ALT(U/L)': 28,
      'AST(U/L)': 25,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('Dubin')) {
    return {
      ...common,
      'TBIL(μmol/L)': 58,
      'DBIL(μmol/L)': 42,
      'IBIL(μmol/L)': 16,
      'ALT(U/L)': 32,
      'AST(U/L)': 29,
      'GGT(U/L)': 34,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('Alagille')) {
    return {
      ...common,
      'TBIL(μmol/L)': 84,
      'DBIL(μmol/L)': 62,
      'ALT(U/L)': 96,
      'AST(U/L)': 82,
      'GGT(U/L)': 260,
      'ALP(U/L)': 520,
      'TBA(μmol/L)': 150,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('NTCP') || disease.includes('SLC10A1')) {
    return {
      ...common,
      'TBIL(μmol/L)': 18,
      'ALT(U/L)': 26,
      'AST(U/L)': 28,
      'GGT(U/L)': 24,
      'TBA(μmol/L)': 185,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('脂肪酶') || disease.includes('LIPA')) {
    return {
      ...common,
      '高脂血症病史': 1,
      'ALT(U/L)': 88,
      'AST(U/L)': 72,
      'GGT(U/L)': 75,
      'TG(mmol/L)': 3.4,
      'CHOL(mmol/L)': 7.2,
      'LDL-C(mmol/L)': 5,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('糖原') || disease.includes('G6PC')) {
    return {
      ...common,
      'ALT(U/L)': 66,
      'AST(U/L)': 58,
      'GLU(mmol/L)': 2.9,
      'TG(mmol/L)': 4.8,
      'CHOL(mmol/L)': 5.9,
      'URIC(μmol/L)': 520,
      ceruloplasmin: 280
    }
  }

  if (disease.includes('血色')) {
    return {
      ...common,
      '高脂血症病史': disease.includes('青年型') ? 0 : age >= 50 ? 1 : 0,
      'TBIL(μmol/L)': highRisk ? 42 : 31,
      'ALT(U/L)': highRisk ? 118 : 88,
      'AST(U/L)': highRisk ? 96 : 65,
      'GGT(U/L)': 78,
      'ceruloplasmin': 305
    }
  }

  if (disease.includes('抗胰蛋白酶')) {
    return {
      ...common,
      NAS: 4,
      'TBIL(μmol/L)': 24,
      'ALT(U/L)': 78,
      'AST(U/L)': 64,
      'ALB(g/L)': 40,
      'ceruloplasmin': 250
    }
  }

  if (disease.includes('脂肪') || disease.includes('代谢')) {
    return {
      ...common,
      '糖尿病病史': age >= 55 ? 1 : 0,
      '高脂血症病史': 1,
      NAS: 5,
      'TBIL(μmol/L)': 33,
      'ALT(U/L)': 86,
      'AST(U/L)': 58,
      'TG(mmol/L)': 2.1,
      'CHOL(mmol/L)': 5.6,
      'ceruloplasmin': 288
    }
  }

  return {
    ...common,
    NAS: highRisk ? 5 : 2,
    'TBIL(μmol/L)': highRisk ? 36 : 22,
    'ALT(U/L)': highRisk ? 125 : 64,
    'AST(U/L)': highRisk ? 88 : 45,
    'ceruloplasmin': highRisk ? 110 : 185
  }
}

const buildFeatureVector = (metadata: ModelMetadata, patient: DemoPatient): Float32Array => {
  const medians = metadata.feature_medians || {}
  const overrides = patientOverrides(patient)
  return new Float32Array(
    metadata.feature_columns.map((feature) => {
      const value = overrides[feature] ?? medians[feature] ?? 0
      return Number.isFinite(value) ? value : 0
    })
  )
}

const readRiskProbability = (outputs: Record<string, OrtTensor>): number => {
  const probabilities = outputs.probabilities || Object.values(outputs).find((value) => value?.data && value.data.length >= 2)
  const data = probabilities?.data
  if (!data || data.length === 0) {
    return 0.5
  }
  const positiveProbability = Number(data.length >= 2 ? data[data.length - 1] : data[0])
  return clamp(Number.isFinite(positiveProbability) ? positiveProbability : 0.5, 0, 1)
}

const buildIndicatorsFromOverrides = (
  patient: DemoPatient
): DiagnosisPayload['indicators'] => {
  const overrides = patientOverrides(patient)
  const disease = patient.disease || ''
  const highStatus = (value: number, high: number) => value >= high * 1.8 ? 'exception' : value > high ? 'warning' : ''
  const lowStatus = (value: number, low: number) => value <= low * 0.75 ? 'exception' : value < low ? 'warning' : ''
  const highPct = (value: number, high: number) => clamp(Math.round((value / (high * 2)) * 100), 20, 95)
  const lowPct = (value: number, low: number) => clamp(Math.round(((value > 0 ? low / value : 2) / 2) * 95), 20, 95)
  const item = (
    name: string,
    value: number,
    unit: string,
    normal: string,
    status: DiagnosisPayload['indicators'][number]['status'],
    percentage: number
  ) => ({ name, value, unit, normal, status, percentage })

  if (disease.includes('Citrin')) {
    return [
      item('DBIL', Number(overrides['DBIL(μmol/L)'] ?? 0), 'μmol/L', '0-6.8', highStatus(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8), highPct(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8)),
      item('GLU', Number(overrides['GLU(mmol/L)'] ?? 0), 'mmol/L', '3.9-6.1', lowStatus(Number(overrides['GLU(mmol/L)'] ?? 0), 3.9), lowPct(Number(overrides['GLU(mmol/L)'] ?? 0), 3.9)),
      item('NH3', Number(overrides['NH3(μmol/L)'] ?? 0), 'μmol/L', '18-72', highStatus(Number(overrides['NH3(μmol/L)'] ?? 0), 72), highPct(Number(overrides['NH3(μmol/L)'] ?? 0), 72))
    ]
  }

  if (disease.includes('PFIC2') || disease.includes('ABCB11')) {
    return [
      item('DBIL', Number(overrides['DBIL(μmol/L)'] ?? 0), 'μmol/L', '0-6.8', highStatus(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8), highPct(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8)),
      item('TBA', Number(overrides['TBA(μmol/L)'] ?? 0), 'μmol/L', '0-10', highStatus(Number(overrides['TBA(μmol/L)'] ?? 0), 10), highPct(Number(overrides['TBA(μmol/L)'] ?? 0), 10)),
      item('GGT', Number(overrides['GGT(U/L)'] ?? 0), 'U/L', '0-60', '', 25)
    ]
  }

  if (disease.includes('PFIC3') || disease.includes('ABCB4') || disease.includes('Alagille')) {
    return [
      item('DBIL', Number(overrides['DBIL(μmol/L)'] ?? 0), 'μmol/L', '0-6.8', highStatus(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8), highPct(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8)),
      item('GGT', Number(overrides['GGT(U/L)'] ?? 0), 'U/L', '0-60', highStatus(Number(overrides['GGT(U/L)'] ?? 0), 60), highPct(Number(overrides['GGT(U/L)'] ?? 0), 60)),
      item('TBA', Number(overrides['TBA(μmol/L)'] ?? 0), 'μmol/L', '0-10', highStatus(Number(overrides['TBA(μmol/L)'] ?? 0), 10), highPct(Number(overrides['TBA(μmol/L)'] ?? 0), 10))
    ]
  }

  if (disease.includes('Gilbert') || disease.includes('吉尔伯特')) {
    return [
      item('IBIL', Number(overrides['IBIL(μmol/L)'] ?? 0), 'μmol/L', '1.7-13.7', highStatus(Number(overrides['IBIL(μmol/L)'] ?? 0), 13.7), highPct(Number(overrides['IBIL(μmol/L)'] ?? 0), 13.7)),
      item('TBIL', Number(overrides['TBIL(μmol/L)'] ?? 0), 'μmol/L', '3.4-17.1', highStatus(Number(overrides['TBIL(μmol/L)'] ?? 0), 17.1), highPct(Number(overrides['TBIL(μmol/L)'] ?? 0), 17.1)),
      item('ALT', Number(overrides['ALT(U/L)'] ?? 0), 'U/L', '0-40', '', 30)
    ]
  }

  if (disease.includes('Dubin')) {
    return [
      item('DBIL', Number(overrides['DBIL(μmol/L)'] ?? 0), 'μmol/L', '0-6.8', highStatus(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8), highPct(Number(overrides['DBIL(μmol/L)'] ?? 0), 6.8)),
      item('TBIL', Number(overrides['TBIL(μmol/L)'] ?? 0), 'μmol/L', '3.4-17.1', highStatus(Number(overrides['TBIL(μmol/L)'] ?? 0), 17.1), highPct(Number(overrides['TBIL(μmol/L)'] ?? 0), 17.1)),
      item('ALT', Number(overrides['ALT(U/L)'] ?? 0), 'U/L', '0-40', '', 30)
    ]
  }

  if (disease.includes('NTCP') || disease.includes('SLC10A1')) {
    return [
      item('TBA', Number(overrides['TBA(μmol/L)'] ?? 0), 'μmol/L', '0-10', highStatus(Number(overrides['TBA(μmol/L)'] ?? 0), 10), highPct(Number(overrides['TBA(μmol/L)'] ?? 0), 10)),
      item('ALT', Number(overrides['ALT(U/L)'] ?? 0), 'U/L', '0-40', '', 30),
      item('TBIL', Number(overrides['TBIL(μmol/L)'] ?? 0), 'μmol/L', '3.4-17.1', highStatus(Number(overrides['TBIL(μmol/L)'] ?? 0), 17.1), highPct(Number(overrides['TBIL(μmol/L)'] ?? 0), 17.1))
    ]
  }

  if (disease.includes('脂肪酶') || disease.includes('LIPA')) {
    return [
      item('LDL-C', Number(overrides['LDL-C(mmol/L)'] ?? 0), 'mmol/L', '<3.4', highStatus(Number(overrides['LDL-C(mmol/L)'] ?? 0), 3.4), highPct(Number(overrides['LDL-C(mmol/L)'] ?? 0), 3.4)),
      item('TG', Number(overrides['TG(mmol/L)'] ?? 0), 'mmol/L', '<1.7', highStatus(Number(overrides['TG(mmol/L)'] ?? 0), 1.7), highPct(Number(overrides['TG(mmol/L)'] ?? 0), 1.7)),
      item('ALT', Number(overrides['ALT(U/L)'] ?? 0), 'U/L', '0-40', highStatus(Number(overrides['ALT(U/L)'] ?? 0), 40), highPct(Number(overrides['ALT(U/L)'] ?? 0), 40))
    ]
  }

  if (disease.includes('糖原') || disease.includes('G6PC')) {
    return [
      item('GLU', Number(overrides['GLU(mmol/L)'] ?? 0), 'mmol/L', '3.9-6.1', lowStatus(Number(overrides['GLU(mmol/L)'] ?? 0), 3.9), lowPct(Number(overrides['GLU(mmol/L)'] ?? 0), 3.9)),
      item('TG', Number(overrides['TG(mmol/L)'] ?? 0), 'mmol/L', '<1.7', highStatus(Number(overrides['TG(mmol/L)'] ?? 0), 1.7), highPct(Number(overrides['TG(mmol/L)'] ?? 0), 1.7)),
      item('URIC', Number(overrides['URIC(μmol/L)'] ?? 0), 'μmol/L', '<420', highStatus(Number(overrides['URIC(μmol/L)'] ?? 0), 420), highPct(Number(overrides['URIC(μmol/L)'] ?? 0), 420))
    ]
  }

  const ceruloplasminValue = Number(overrides.ceruloplasmin ?? 0)
  const tbilValue = Number(overrides['TBIL(μmol/L)'] ?? 0)
  const altValue = Number(overrides['ALT(U/L)'] ?? 0)
  return [
    item('TBIL', tbilValue, 'μmol/L', '3.4-17.1', highStatus(tbilValue, 17.1), highPct(tbilValue, 17.1)),
    item('ALT', altValue, 'U/L', '0-40', highStatus(altValue, 40), highPct(altValue, 40)),
    item('ceruloplasmin', ceruloplasminValue, 'mg/L', '200-600', ceruloplasminValue < 200 ? 'exception' : '', clamp(Math.round(ceruloplasminValue / 6), 15, 95))
  ]
}

export const predictImldDemoDiagnosis = async (
  patient: DemoPatient,
  record?: PatientRecordPayload | null
): Promise<DiagnosisPayload | null> => {
  if (!isDemoInferenceEnabled()) {
    return null
  }

  try {
    const [metadata, session] = await Promise.all([loadMetadata(), loadSession()])
    const features = record
      ? buildFeatureVectorFromRecord(metadata, record)
      : buildFeatureVector(metadata, patient)
    const inputName = session.inputNames[0] || 'features'
    const outputs = await session.run({
      [inputName]: new ort.Tensor('float32', features, [1, features.length])
    })
    const riskProbability = readRiskProbability(outputs)
    const probability = Math.round(riskProbability * 100)
    const diseaseName = record?.clinicalDecision?.diagnosis || patient.disease || '遗传代谢性肝病风险提示'

    const indicators = record
      ? buildIndicatorsFromRecord(record, diseaseName)
      : buildIndicatorsFromOverrides(patient)

    const displayFields = buildDiseaseDisplayFields({
      diseaseName,
      probability
    })
    const confidence = resolveDiagnosisConfidence(probability)
    const evidenceItems = buildEvidenceItemsFromDiagnosis({
      diseaseName,
      indicators,
      record,
      genes: displayFields.genes
    })

    return {
      diseaseName,
      riskLevel: riskLevelFromProbability(probability),
      probability,
      indicators,
      confidence,
      evidenceItems,
      evidenceSummary: buildEvidenceSummary(evidenceItems, confidence),
      ...displayFields
    }
  } catch (error) {
    console.warn('IMLD browser demo inference unavailable, fallback to mock payload.', error)
    return null
  }
}

export {}
