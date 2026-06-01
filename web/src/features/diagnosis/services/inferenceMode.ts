export type ImldInferenceMode = 'browser-onnx' | 'backend' | 'mock'

const MODES = new Set<ImldInferenceMode>(['browser-onnx', 'backend', 'mock'])

export const resolveImldInferenceMode = (): ImldInferenceMode => {
  const configured = import.meta.env.VITE_IMLD_INFERENCE_MODE
  if (MODES.has(configured as ImldInferenceMode)) {
    return configured as ImldInferenceMode
  }
  return import.meta.env.VITE_IMLD_DEMO_INFERENCE_ENABLED === 'true' ? 'browser-onnx' : 'mock'
}

export const isBrowserOnnxInferenceMode = (): boolean => resolveImldInferenceMode() === 'browser-onnx'

export const isBackendInferenceMode = (): boolean => resolveImldInferenceMode() === 'backend'
