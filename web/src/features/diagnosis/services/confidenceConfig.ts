import { resolveImldInferenceMode, type ImldInferenceMode } from './inferenceMode'

export interface DiagnosisConfidence {
  visible: boolean
  rawValue: number
  displayValue: number
  reviewRequired: boolean
  adjusted: boolean
  label: string
}

export interface DiagnosisConfidenceOptions {
  visible?: boolean
  minThreshold?: number
  boostEnabled?: boolean
  mode?: ImldInferenceMode
}

const DEFAULT_MIN_THRESHOLD = 0.7

const clamp = (value: number, min: number, max: number): number => Math.max(min, Math.min(max, value))

const normalizeProbability = (value: number): number => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) {
    return 0
  }
  return clamp(parsed > 1 ? parsed / 100 : parsed, 0, 1)
}

const parseBoolean = (value: unknown, fallback: boolean): boolean => {
  if (typeof value !== 'string') {
    return fallback
  }
  const normalized = value.trim().toLowerCase()
  if (['true', '1', 'yes', 'on'].includes(normalized)) {
    return true
  }
  if (['false', '0', 'no', 'off'].includes(normalized)) {
    return false
  }
  return fallback
}

const parseThreshold = (value: unknown, fallback = DEFAULT_MIN_THRESHOLD): number => {
  const parsed = typeof value === 'number' ? value : Number.parseFloat(String(value ?? ''))
  if (!Number.isFinite(parsed)) {
    return fallback
  }
  return normalizeProbability(parsed)
}

const confidenceText = (value: number): string => value.toFixed(2)

const canBoostConfidence = (mode: ImldInferenceMode, enabled: boolean): boolean => {
  return enabled && (mode === 'browser-onnx' || mode === 'mock')
}

export const resolveDiagnosisConfidence = (
  probability: number,
  options: DiagnosisConfidenceOptions = {}
): DiagnosisConfidence => {
  const mode = options.mode ?? resolveImldInferenceMode()
  const rawValue = normalizeProbability(probability)
  const visible = options.visible ?? parseBoolean(import.meta.env.VITE_IMLD_CONFIDENCE_VISIBLE, true)
  const minThreshold = options.minThreshold ?? parseThreshold(import.meta.env.VITE_IMLD_CONFIDENCE_MIN_THRESHOLD)
  const boostEnabled =
    options.boostEnabled ?? parseBoolean(import.meta.env.VITE_IMLD_CONFIDENCE_BOOST_ENABLED, false)
  const displayValue = canBoostConfidence(mode, boostEnabled)
    ? Math.max(rawValue, minThreshold)
    : rawValue
  const reviewRequired = rawValue < minThreshold
  const adjusted = displayValue !== rawValue

  let label = '不展示'
  if (visible) {
    if (adjusted) {
      label = `演示校准 (${confidenceText(displayValue)}，原始 ${confidenceText(rawValue)})`
    } else if (reviewRequired) {
      label = `需复核 (${confidenceText(rawValue)})`
    } else {
      const level = rawValue >= 0.8 ? '高' : '可用'
      label = `${level} (${confidenceText(displayValue)})`
    }
  }

  return {
    visible,
    rawValue,
    displayValue,
    reviewRequired,
    adjusted,
    label
  }
}
