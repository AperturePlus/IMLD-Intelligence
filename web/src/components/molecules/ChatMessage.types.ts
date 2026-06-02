export type ChatRole = 'user' | 'ai'

export interface ChatMessageProps {
  role: ChatRole
  text: string
  source?: string | null
  streaming?: boolean
}
