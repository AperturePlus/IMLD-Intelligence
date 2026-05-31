import modelMetadataUrl from '@/assets/models/imld/imld_model_meta.json?url'
import modelUrl from '@/assets/models/imld/imld_xgboost_model.onnx?url'

type ProgressStatus = '' | 'success' | 'warning' | 'exception'

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

interface DiagnosisPayload {
  diseaseName: string
  probability: number
  indicators: Array<{
    name: string
    value: number
    unit: string
    normal: string
    percentage: number
    status: ProgressStatus
  }>
  genes: string[]
  diet: string
  sequencing: string
}

interface OrtTensor {
  data: ArrayLike<number | bigint>
}

interface OrtSession {
  inputNames: string[]
  run: (feeds: Record<string, unknown>) => Promise<Record<string, OrtTensor>>
}

interface OrtRuntime {
  env: {
    wasm: {
      wasmPaths?: string
    }
  }
  InferenceSession: {
    create: (path: string, options?: Record<string, unknown>) => Promise<OrtSession>
  }
  Tensor: new (type: string, data: Float32Array, dims: number[]) => unknown
}

declare global {
  interface Window {
    ort?: OrtRuntime
  }
}

let metadataPromise: Promise<ModelMetadata> | null = null
let runtimePromise: Promise<OrtRuntime> | null = null
let sessionPromise: Promise<OrtSession> | null = null

const DEFAULT_RUNTIME_SCRIPT_URL = 'https://cdn.jsdelivr.net/npm/onnxruntime-web/dist/ort.min.js'
const DEFAULT_WASM_BASE_URL = 'https://cdn.jsdelivr.net/npm/onnxruntime-web/dist/'

const isDemoInferenceEnabled = (): boolean => {
  return import.meta.env.VITE_USE_MOCK === 'true'
    && import.meta.env.VITE_IMLD_DEMO_INFERENCE_ENABLED === 'true'
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

const loadScript = (src: string): Promise<void> => {
  const existed = document.querySelector<HTMLScriptElement>(`script[data-imld-onnx-runtime="${src}"]`)
  if (existed) {
    return Promise.resolve()
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = src
    script.async = true
    script.dataset.imldOnnxRuntime = src
    script.onload = () => resolve()
    script.onerror = () => reject(new Error(`failed to load ONNX Runtime Web script: ${src}`))
    document.head.appendChild(script)
  })
}

const loadOrtRuntime = (): Promise<OrtRuntime> => {
  if (!runtimePromise) {
    runtimePromise = (async () => {
      if (typeof window === 'undefined' || typeof document === 'undefined') {
        throw new Error('browser runtime is not available')
      }
      if (!window.ort) {
        const runtimeScriptUrl = import.meta.env.VITE_IMLD_ONNX_RUNTIME_SCRIPT_URL || DEFAULT_RUNTIME_SCRIPT_URL
        await loadScript(runtimeScriptUrl)
      }
      if (!window.ort) {
        throw new Error('ONNX Runtime Web did not expose window.ort')
      }
      window.ort.env.wasm.wasmPaths = import.meta.env.VITE_IMLD_ONNX_WASM_BASE_URL || DEFAULT_WASM_BASE_URL
      return window.ort
    })()
  }
  return runtimePromise
}

const loadSession = async (): Promise<OrtSession> => {
  if (!sessionPromise) {
    sessionPromise = (async () => {
      const ort = await loadOrtRuntime()
      return ort.InferenceSession.create(modelUrl, {
        executionProviders: ['wasm']
      })
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

const diseaseGenes = (diseaseName: string): string[] => {
  if (diseaseName.includes('血色')) {
    return ['HFE (C282Y)', 'HFE (H63D)']
  }
  if (diseaseName.includes('抗胰蛋白酶')) {
    return ['SERPINA1 (Pi*ZZ)']
  }
  if (diseaseName.includes('脂肪') || diseaseName.includes('代谢')) {
    return []
  }
  return ['ATP7B (c.2333G>T)', 'ATP7B (c.2975C>T)']
}

const dietAdvice = (diseaseName: string): string => {
  if (diseaseName.includes('血色')) {
    return '建议限制红肉和动物内脏，避免随餐补充维生素C，餐后可饮茶抑制铁吸收。'
  }
  if (diseaseName.includes('抗胰蛋白酶')) {
    return '建议高蛋白、低脂饮食，减少酒精摄入，配合呼吸系统评估。'
  }
  if (diseaseName.includes('脂肪') || diseaseName.includes('代谢')) {
    return '建议控制总热量和精制碳水摄入，配合体重管理与规律运动。'
  }
  return '建议低铜饮食，禁食坚果、巧克力和动物内脏。'
}

const sequencingAdvice = (diseaseName: string): string => {
  if (diseaseName.includes('血色')) {
    return '建议进行 HFE 基因检测，并对一级亲属开展家系筛查。'
  }
  if (diseaseName.includes('抗胰蛋白酶')) {
    return '建议进行 SERPINA1 基因分型，并评估肝肺联合受累风险。'
  }
  if (diseaseName.includes('脂肪') || diseaseName.includes('代谢')) {
    return '建议优先完善代谢危险因素评估，必要时结合遗传易感位点检测。'
  }
  return '建议 ATP7B 靶向测序，并开展一级亲属筛查。'
}

export const predictImldDemoDiagnosis = async (patient: DemoPatient): Promise<DiagnosisPayload | null> => {
  if (!isDemoInferenceEnabled()) {
    return null
  }

  try {
    const [metadata, session, ort] = await Promise.all([loadMetadata(), loadSession(), loadOrtRuntime()])
    const features = buildFeatureVector(metadata, patient)
    const inputName = session.inputNames[0] || 'features'
    const outputs = await session.run({
      [inputName]: new ort.Tensor('float32', features, [1, features.length])
    })
    const riskProbability = readRiskProbability(outputs)
    const probability = Math.round(riskProbability * 100)
    const overrides = patientOverrides(patient)
    const diseaseName = patient.disease || '遗传代谢性肝病风险提示'

    return {
      diseaseName,
      probability,
      indicators: [
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
      ],
      genes: diseaseGenes(diseaseName),
      diet: dietAdvice(diseaseName),
      sequencing: sequencingAdvice(diseaseName)
    }
  } catch (error) {
    console.warn('IMLD browser demo inference unavailable, fallback to mock payload.', error)
    return null
  }
}

export {}
