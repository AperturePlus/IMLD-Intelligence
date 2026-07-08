import type { ChatMessageProps } from '@/components/molecules/ChatMessage.types'

export type CopilotMessage = ChatMessageProps & { __id?: number }

export interface AiCopilotDockProps {
  messages: CopilotMessage[]
  privacyNote?: string
  suggestions?: string[]
}

export interface AiCopilotDockEmits {
  (e: 'send', text: string): void
  (e: 'close'): void
}
