export interface AppTopbarProps {
  /** AI 引擎是否运行中（由智能引擎状态驱动） */
  engineRunning?: boolean
  modelName?: string
  version?: string
}

export interface AppTopbarEmits {
  (e: 'openCopilot'): void
}
