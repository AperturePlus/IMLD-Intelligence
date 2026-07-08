import type {
  DiagnosisQueuePatient,
  DiagnosisResult
} from '@/types/diagnosis'
import type { PatientRecordPayload } from '@/types/patient'
import diagnosisApi from '@/api/diagnosis'

export interface IntelligenceDataSource {
  getQueue(): Promise<DiagnosisQueuePatient[]>
  getPatientRecord(patientId: string): Promise<PatientRecordPayload | null>
  getDiagnosisResult(patientId: string): Promise<DiagnosisResult | null>
}

export class DefaultIntelligenceDataSource implements IntelligenceDataSource {
  async getQueue(): Promise<DiagnosisQueuePatient[]> {
    const res = await diagnosisApi.getAiQueue()
    return res.data.items || []
  }

  async getPatientRecord(_patientId: string): Promise<PatientRecordPayload | null> {
    // 当前无统一病历查询接口，返回 null 以触发 synthesized 降级路径
    return null
  }

  async getDiagnosisResult(patientId: string): Promise<DiagnosisResult | null> {
    const res = await diagnosisApi.getLatestDiagnosisResultByPatient(patientId)
    return res.data
  }
}

export const defaultIntelligenceDataSource = new DefaultIntelligenceDataSource()
