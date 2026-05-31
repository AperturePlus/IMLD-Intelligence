import type { AxiosResponse } from 'axios'
import service from './base'
import type {
  DietPatientsResponse,
  DietPlanResponse,
  PushDietPlanResponse,
  RegenerateDietPlanResponse,
  ScreeningOverviewQuery,
  ScreeningOverviewResponse
} from '@/types/management'
import type { ApiEnvelope } from '@/types/common'

const SUCCESS_CODE = 200

const parseTenantId = (value: unknown): number | null => {
  if (typeof value !== 'string' || !value.trim()) {
    return null
  }

  const parsed = Number.parseInt(value, 10)
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return null
  }

  return parsed
}

const resolveTenantId = (): number => {
  const fromStorage = parseTenantId(localStorage.getItem('tenantId'))
  if (fromStorage) {
    return fromStorage
  }

  const fromEnv = parseTenantId(import.meta.env.VITE_TENANT_ID)
  if (fromEnv) {
    return fromEnv
  }

  return 1
}

const tenantHeaders = () => ({
  'X-Tenant-Id': resolveTenantId()
})

const normalizeScreeningOverviewParams = (
  params: ScreeningOverviewQuery
): ScreeningOverviewQuery => {
  const normalized: ScreeningOverviewQuery = {}
  if (params.from?.trim()) {
    normalized.from = params.from
  }
  if (params.to?.trim()) {
    normalized.to = params.to
  }
  return normalized
}

const unwrapIfEnvelope = <T>(response: AxiosResponse<T | ApiEnvelope<T>>): AxiosResponse<T> => {
  const payload = response.data as ApiEnvelope<T> | T
  if (
    payload &&
    typeof payload === 'object' &&
    'code' in payload &&
    'message' in payload &&
    'data' in payload
  ) {
    const envelope = payload as ApiEnvelope<T>
    if (envelope.code !== SUCCESS_CODE || envelope.data === undefined) {
      throw new Error(envelope.message || '管理接口返回异常')
    }
    return {
      ...response,
      data: envelope.data
    }
  }

  return response as AxiosResponse<T>
}

const managementApi = {
  getScreeningOverview(
    params: ScreeningOverviewQuery = {}
  ): Promise<AxiosResponse<ScreeningOverviewResponse>> {
    return service<ScreeningOverviewResponse | ApiEnvelope<ScreeningOverviewResponse>>({
      url: '/api/v1/web/screening/overview/',
      method: 'get',
      params: normalizeScreeningOverviewParams(params),
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  getDietPatients(params: { keyword?: string } = {}): Promise<AxiosResponse<DietPatientsResponse>> {
    return service<DietPatientsResponse | ApiEnvelope<DietPatientsResponse>>({
      url: '/api/v1/web/diet/patients/',
      method: 'get',
      params,
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  getDietPlan(patientId: string): Promise<AxiosResponse<DietPlanResponse>> {
    return service<DietPlanResponse | ApiEnvelope<DietPlanResponse>>({
      url: `/api/v1/web/diet/patients/${patientId}/plan/`,
      method: 'get',
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  regenerateDietPlan(patientId: string): Promise<AxiosResponse<RegenerateDietPlanResponse>> {
    return service<RegenerateDietPlanResponse | ApiEnvelope<RegenerateDietPlanResponse>>({
      url: `/api/v1/web/diet/patients/${patientId}/regenerate/`,
      method: 'post',
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  pushDietPlan(patientId: string): Promise<AxiosResponse<PushDietPlanResponse>> {
    return service<PushDietPlanResponse | ApiEnvelope<PushDietPlanResponse>>({
      url: `/api/v1/web/diet/patients/${patientId}/push/`,
      method: 'post',
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  }
}

export default managementApi
