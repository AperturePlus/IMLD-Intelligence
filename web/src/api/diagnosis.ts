import type { AxiosError, AxiosResponse } from 'axios'
import service from './base'
import type {
  DiagnosisQueueResponse,
  DiagnosisResult,
  ExpertReport,
  ExpertReportListResponse,
  SignExpertReportPayload,
  SignExpertReportResponse
} from '@/types/diagnosis'
import type { ApiEnvelope, PagedResult } from '@/types/common'
import {
  buildDiagnosisResultFromExpertReport,
  buildDiagnosisResultFromSession,
  extractInferencePayload,
  mapInferenceIndicators,
  normalizeDiagnosisResultPayload,
  type DiagnosisSessionApi
} from '@/features/diagnosis/services/diagnosisResultMapper'

interface IdentityPatientApi {
  id: number
  patientName: string
  gender?: string
  birthDate?: string | null
}

const SUCCESS_CODE = 200
const MAX_PAGE_SIZE = 200

const parsePositiveInteger = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
    return Math.floor(value)
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number.parseInt(value, 10)
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null
  }
  return null
}

const resolveTenantId = (): number => {
  const fromStorage = parsePositiveInteger(localStorage.getItem('tenantId'))
  if (fromStorage) {
    return fromStorage
  }
  const fromEnv = parsePositiveInteger(import.meta.env.VITE_TENANT_ID)
  if (fromEnv) {
    return fromEnv
  }
  return 1
}

const resolveDoctorId = (): number => {
  const fromStorage = parsePositiveInteger(localStorage.getItem('userId'))
  if (fromStorage) {
    return fromStorage
  }
  const fromEnv = parsePositiveInteger(import.meta.env.VITE_DOCTOR_ID)
  if (fromEnv) {
    return fromEnv
  }
  return 1
}

const tenantHeaders = () => ({
  'X-Tenant-Id': resolveTenantId()
})

const unwrapApiEnvelope = <T>(envelope: ApiEnvelope<T>): T => {
  if (!envelope || envelope.code !== SUCCESS_CODE || envelope.data === undefined) {
    throw new Error(envelope?.message || '诊断接口返回异常')
  }
  return envelope.data
}

const mapAxiosResponse = <T>(base: AxiosResponse, data: T): AxiosResponse<T> => ({
  ...base,
  data
})

const asRecord = (value: unknown): Record<string, unknown> | null => {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) {
    return null
  }
  return value as Record<string, unknown>
}

const normalizeGender = (value: string | undefined): string => {
  if (!value) {
    return '--'
  }
  const normalized = value.toLowerCase()
  if (normalized === 'female' || normalized === 'f' || normalized === '女') {
    return '女'
  }
  if (normalized === 'male' || normalized === 'm' || normalized === '男') {
    return '男'
  }
  return value
}

const calculateAge = (birthDate?: string | null): number => {
  if (!birthDate) {
    return 0
  }
  const birthday = new Date(birthDate)
  if (Number.isNaN(birthday.getTime())) {
    return 0
  }
  const now = new Date()
  let age = now.getFullYear() - birthday.getFullYear()
  const monthDiff = now.getMonth() - birthday.getMonth()
  const needAdjust = monthDiff < 0 || (monthDiff === 0 && now.getDate() < birthday.getDate())
  if (needAdjust) {
    age -= 1
  }
  return Math.max(age, 0)
}

const toDateOnly = (value?: string): string => {
  if (!value) {
    return new Date().toISOString().slice(0, 10)
  }
  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) {
    return value.slice(0, 10)
  }
  return parsed.toISOString().slice(0, 10)
}

const sessionTimeWeight = (session: DiagnosisSessionApi): number => {
  const value = session.completedAt || session.startedAt
  if (!value) {
    return 0
  }
  const parsed = new Date(value).getTime()
  return Number.isNaN(parsed) ? 0 : parsed
}

const sortSessionsDesc = (sessions: DiagnosisSessionApi[]): DiagnosisSessionApi[] => {
  return [...sessions].sort((a, b) => {
    const timeDelta = sessionTimeWeight(b) - sessionTimeWeight(a)
    if (timeDelta !== 0) {
      return timeDelta
    }
    return b.id - a.id
  })
}

const isDiagnosedStatus = (status: string | undefined): boolean => {
  if (!status) {
    return false
  }
  const upper = status.toUpperCase()
  return upper === 'COMPLETED' || upper === 'REVIEWED'
}

const fetchSessions = async (): Promise<{ response: AxiosResponse<ApiEnvelope<PagedResult<DiagnosisSessionApi>>>; items: DiagnosisSessionApi[] }> => {
  const response = await service<ApiEnvelope<PagedResult<DiagnosisSessionApi>>>({
    url: '/api/v1/web/diagnoses/sessions',
    method: 'get',
    params: {
      page: 0,
      size: MAX_PAGE_SIZE
    },
    headers: tenantHeaders()
  })
  const page = unwrapApiEnvelope(response.data)
  return {
    response,
    items: page.items || []
  }
}

const fetchPatients = async (): Promise<IdentityPatientApi[]> => {
  const response = await service<ApiEnvelope<PagedResult<IdentityPatientApi>>>({
    url: '/api/v1/web/identity/patients',
    method: 'get',
    params: {
      page: 0,
      size: MAX_PAGE_SIZE
    },
    headers: tenantHeaders()
  })
  return unwrapApiEnvelope(response.data).items || []
}

const buildExpertReport = (
  session: DiagnosisSessionApi,
  patientMap: Map<number, IdentityPatientApi>
): ExpertReport | null => {
  const sortedResults = [...(session.results || [])].sort((a, b) => (a.rankNo || 0) - (b.rankNo || 0))
  const primaryResult = sortedResults[0]
  if (!primaryResult) {
    return null
  }
  const patient = patientMap.get(session.patientId)
  const inference = extractInferencePayload(session)
  const indicators = mapInferenceIndicators(inference)
  const latestFeedback = [...(session.feedbacks || [])]
    .sort((a, b) => {
      const t1 = new Date(b.createdAt || '').getTime()
      const t2 = new Date(a.createdAt || '').getTime()
      return (Number.isNaN(t1) ? 0 : t1) - (Number.isNaN(t2) ? 0 : t2)
    })[0]
  const feedbackBody = asRecord(latestFeedback?.modifiedValue)
  const signed = latestFeedback && ['ACCEPT', 'MODIFY'].includes((latestFeedback.action || '').toUpperCase())
  const rawProbability = typeof inference.risk_probability === 'number' ? inference.risk_probability : primaryResult.confidence || 0
  const probability = Math.round(Math.max(0, Math.min(1, rawProbability)) * 100)
  return {
    id: String(session.id),
    patientId: String(session.patientId),
    visitId: session.encounterId ? String(session.encounterId) : '--',
    patientName: patient?.patientName || `患者${session.patientId}`,
    gender: normalizeGender(patient?.gender),
    age: calculateAge(patient?.birthDate),
    date: toDateOnly(session.completedAt || session.startedAt),
    status: signed ? '已签发' : '待签发',
    aiFindings: {
      biochemical: indicators.map((item) => `${item.name} ${item.value}`).join('，') || '见诊断会话明细',
      clinical: (inference.suggestions || [])[0] || '请结合病史与临床体征综合判断',
      probability: String(probability),
      disease: primaryResult.diseaseName || '遗传代谢性肝病风险提示'
    },
    expertConclusion: typeof feedbackBody?.expertConclusion === 'string' ? feedbackBody.expertConclusion : '',
    treatmentPlan: typeof feedbackBody?.treatmentPlan === 'string' ? feedbackBody.treatmentPlan : '',
    signedAt: latestFeedback?.createdAt,
    sessionId: session.id,
    resultId: primaryResult.id,
    doctorId: session.doctorId
  }
}

const isEndpointMissing = (error: unknown): boolean => {
  const axiosError = error as AxiosError | undefined
  const status = axiosError?.response?.status
  return status === 404 || status === 405 || status === 501
}

const diagnosisApi = {
  getAiQueue(): Promise<AxiosResponse<DiagnosisQueueResponse>> {
    return (async () => {
      try {
        const { response, items: sessions } = await fetchSessions()
        let patients: IdentityPatientApi[] = []
        try {
          patients = await fetchPatients()
        } catch {
          patients = []
        }
        const sortedSessions = sortSessionsDesc(sessions)
        const latestSessionMap = new Map<number, DiagnosisSessionApi>()
        sortedSessions.forEach((session) => {
          if (!latestSessionMap.has(session.patientId)) {
            latestSessionMap.set(session.patientId, session)
          }
        })

        const queueItems = patients.length > 0
          ? patients.map((patient) => {
              const latest = latestSessionMap.get(patient.id)
              return {
                id: String(patient.id),
                name: patient.patientName,
                gender: normalizeGender(patient.gender),
                age: calculateAge(patient.birthDate),
                avatar: '',
                aiStatus: latest && isDiagnosedStatus(latest.status) ? '已诊断' : '未诊断',
                encounterId: latest?.encounterId,
                doctorId: latest?.doctorId,
                modelRegistryId: latest?.modelRegistryId
              }
            })
          : Array.from(latestSessionMap.values()).map((session) => ({
              id: String(session.patientId),
              name: `患者${session.patientId}`,
              gender: '--',
              age: 0,
              avatar: '',
              aiStatus: isDiagnosedStatus(session.status) ? '已诊断' : '未诊断',
              encounterId: session.encounterId,
              doctorId: session.doctorId,
              modelRegistryId: session.modelRegistryId
            }))

        return mapAxiosResponse(response, { items: queueItems })
      } catch (error) {
        if (!isEndpointMissing(error)) {
          throw error
        }
        return service({
          url: '/api/v1/web/diagnosis/ai-queue/',
          method: 'get'
        }) as Promise<AxiosResponse<DiagnosisQueueResponse>>
      }
    })()
  },

  getLatestDiagnosisResultByPatient(patientId: string): Promise<AxiosResponse<DiagnosisResult | null>> {
    return (async () => {
      const numericPatientId = parsePositiveInteger(patientId)
      if (!numericPatientId) {
        throw new Error('invalid patientId')
      }

      try {
        const { response, items: sessions } = await fetchSessions()
        const latestSession = sortSessionsDesc(sessions).find((session) => {
          return session.patientId === numericPatientId && isDiagnosedStatus(session.status)
        })
        return mapAxiosResponse(response, latestSession ? buildDiagnosisResultFromSession(latestSession) : null)
      } catch (error) {
        if (!isEndpointMissing(error)) {
          throw error
        }

        const fallbackResponse = await service<ExpertReportListResponse>({
          url: '/api/v1/web/diagnosis/expert-reports/',
          method: 'get'
        })
        const latestReport = [...(fallbackResponse.data.items || [])]
          .filter((item) => parsePositiveInteger(item.patientId) === numericPatientId)
          .sort((left, right) => {
            const leftTime = new Date(left.signedAt || left.date || '').getTime()
            const rightTime = new Date(right.signedAt || right.date || '').getTime()
            return (Number.isNaN(rightTime) ? 0 : rightTime) - (Number.isNaN(leftTime) ? 0 : leftTime)
          })[0]

        return mapAxiosResponse(
          fallbackResponse,
          latestReport ? buildDiagnosisResultFromExpertReport(latestReport) : null
        )
      }
    })()
  },

  runAiDiagnosis(patientId: string): Promise<AxiosResponse<DiagnosisResult>> {
    return (async () => {
      try {
        const numericPatientId = parsePositiveInteger(patientId)
        if (!numericPatientId) {
          throw new Error('invalid patientId')
        }
        const response = await service<ApiEnvelope<DiagnosisSessionApi>>({
          url: '/api/v1/web/diagnoses/sessions',
          method: 'post',
          headers: tenantHeaders(),
          data: {
            patientId: numericPatientId,
            doctorId: resolveDoctorId(),
            triggeredBy: 'MANUAL'
          }
        })
        const session = unwrapApiEnvelope(response.data)
        return mapAxiosResponse(response, buildDiagnosisResultFromSession(session))
      } catch (error) {
        if (!isEndpointMissing(error)) {
          throw error
        }
        const fallbackResponse = await service<DiagnosisResult>({
          url: '/api/v1/web/diagnosis/ai-reports/',
          method: 'post',
          data: { patientId }
        })
        return mapAxiosResponse(fallbackResponse, normalizeDiagnosisResultPayload(fallbackResponse.data))
      }
    })()
  },

  getExpertReports(): Promise<AxiosResponse<ExpertReportListResponse>> {
    return (async () => {
      try {
        const { response, items: sessions } = await fetchSessions()
        let patients: IdentityPatientApi[] = []
        try {
          patients = await fetchPatients()
        } catch {
          patients = []
        }
        const patientMap = new Map<number, IdentityPatientApi>(patients.map((item) => [item.id, item]))
        const reports = sortSessionsDesc(sessions)
          .map((session) => buildExpertReport(session, patientMap))
          .filter((item): item is ExpertReport => item !== null)
        return mapAxiosResponse(response, { items: reports })
      } catch (error) {
        if (!isEndpointMissing(error)) {
          throw error
        }
        return service({
          url: '/api/v1/web/diagnosis/expert-reports/',
          method: 'get'
        }) as Promise<AxiosResponse<ExpertReportListResponse>>
      }
    })()
  },

  signExpertReport(
    reportId: string,
    payload: SignExpertReportPayload
  ): Promise<AxiosResponse<SignExpertReportResponse>> {
    return (async () => {
      try {
        const sessionId = parsePositiveInteger(reportId)
        if (!sessionId) {
          throw new Error('invalid report id')
        }
        const sessionResponse = await service<ApiEnvelope<DiagnosisSessionApi>>({
          url: `/api/v1/web/diagnoses/sessions/${sessionId}`,
          method: 'get',
          headers: tenantHeaders()
        })
        const session = unwrapApiEnvelope(sessionResponse.data)
        const primaryResult = [...(session.results || [])].sort((a, b) => (a.rankNo || 0) - (b.rankNo || 0))[0]
        if (!primaryResult) {
          throw new Error('session has no diagnosis result')
        }
        const feedbackResponse = await service<ApiEnvelope<DiagnosisSessionApi>>({
          url: '/api/v1/web/diagnoses/feedbacks',
          method: 'post',
          headers: tenantHeaders(),
          data: {
            sessionId: session.id,
            resultId: primaryResult.id,
            doctorId: session.doctorId || resolveDoctorId(),
            action: 'MODIFY',
            modifiedValue: {
              expertConclusion: payload.expertConclusion,
              treatmentPlan: payload.treatmentPlan
            }
          }
        })
        const updatedSession = unwrapApiEnvelope(feedbackResponse.data)
        let patients: IdentityPatientApi[] = []
        try {
          patients = await fetchPatients()
        } catch {
          patients = []
        }
        const patientMap = new Map<number, IdentityPatientApi>(patients.map((item) => [item.id, item]))
        const report = buildExpertReport(updatedSession, patientMap)
        if (!report) {
          throw new Error('failed to rebuild signed report')
        }
        return mapAxiosResponse(feedbackResponse, { report })
      } catch (error) {
        if (!isEndpointMissing(error)) {
          throw error
        }
        return service({
          url: `/api/v1/web/diagnosis/expert-reports/${reportId}/sign/`,
          method: 'post',
          data: payload
        }) as Promise<AxiosResponse<SignExpertReportResponse>>
      }
    })()
  }
}

export default diagnosisApi
