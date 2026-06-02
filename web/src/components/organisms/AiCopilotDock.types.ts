import type { ChatMessageProps } from '@/components/molecules/ChatMessage.types'

export interface AiCopilotDockProps {
  messages: ChatMessageProps[]
  privacyNote?: string
  suggestions?: string[]
}

export interface AiCopilotDockEmits {
  (e: 'send', text: string): void
}
