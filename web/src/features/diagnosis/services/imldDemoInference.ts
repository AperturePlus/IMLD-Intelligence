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

  if (disease.includes('血色')) {
    return {
      ...common,
      '高脂血症病史': age >= 50 ? 1 : 0,
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
  patient: DemoPatient,
  probability: number
): DiagnosisPayload['indicators'] => {
  const overrides = patientOverrides(patient)
  return [
    {
      name: 'TBIL',
      value: Number(overrides['TBIL(μmol/L)'] ?? 0),
      unit: 'μmol/L',
      normal: '3.4-17.1',
      percentage: clamp(Math.round(Number(overrides['TBIL(μmol/L)'] ?? 0) / 0.6), 20, 95),
      status: probability >= 70 ? 'exception' : 'warning'
    },
    {
      name: 'ALT',
      value: Number(overrides['ALT(U/L)'] ?? 0),
      unit: 'U/L',
      normal: '0-40',
      percentage: clamp(Math.round(Number(overrides['ALT(U/L)'] ?? 0) / 1.4), 20, 95),
      status: probability >= 70 ? 'exception' : 'warning'
    },
    {
      name: 'ceruloplasmin',
      value: Number(overrides.ceruloplasmin ?? 0),
      unit: 'mg/L',
      normal: '200-600',
      percentage: clamp(Math.round(Number(overrides.ceruloplasmin ?? 0) / 6), 15, 95),
      status: Number(overrides.ceruloplasmin ?? 0) < 200 ? 'exception' : ''
    }
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
      : buildIndicatorsFromOverrides(patient, probability)

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
