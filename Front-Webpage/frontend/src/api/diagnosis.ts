import type { AxiosResponse } from 'axios'
import service from './base'
import type {
  DiagnosisQueueResponse,
  DiagnosisResult,
  ExpertReportListResponse,
  SignExpertReportPayload,
  SignExpertReportResponse
} from './types'

const diagnosisApi = {
  getAiQueue(): Promise<AxiosResponse<DiagnosisQueueResponse>> {
    return service({
      url: '/api/v1/diagnosis/ai-queue/',
      method: 'get'
    })
  },

  runAiDiagnosis(patientId: string): Promise<AxiosResponse<DiagnosisResult>> {
    return service({
      url: '/api/v1/diagnosis/ai-reports/',
      method: 'post',
      data: { patientId }
    })
  },

  getExpertReports(): Promise<AxiosResponse<ExpertReportListResponse>> {
    return service({
      url: '/api/v1/diagnosis/expert-reports/',
      method: 'get'
    })
  },

  signExpertReport(
    reportId: string,
    payload: SignExpertReportPayload
  ): Promise<AxiosResponse<SignExpertReportResponse>> {
    return service({
      url: `/api/v1/diagnosis/expert-reports/${reportId}/sign/`,
      method: 'post',
      data: payload
    })
  }
}

export default diagnosisApi
