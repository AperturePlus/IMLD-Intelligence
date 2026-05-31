import type { AxiosResponse } from 'axios'
import service from './base'
import type {
  CreatePatientRecordResponse,
  HisLisPatientImportRequest,
  OcrPatientImportRequest,
  PatientImportPreview,
  PatientListQuery,
  PatientListResponse,
  PatientRecordPayload
} from '@/types/patient'
import type { ApiEnvelope } from '@/types/common'

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

const SUCCESS_CODE = 200

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
      throw new Error(envelope.message || '患者接口返回异常')
    }
    return {
      ...response,
      data: envelope.data
    }
  }

  return response as AxiosResponse<T>
}

const patientApi = {
  list(params: PatientListQuery = {}): Promise<AxiosResponse<PatientListResponse>> {
    return service<PatientListResponse | ApiEnvelope<PatientListResponse>>({
      url: '/api/v1/web/patients/',
      method: 'get',
      params,
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  getRecord(patientNo: string): Promise<AxiosResponse<PatientRecordPayload>> {
    return service<PatientRecordPayload | ApiEnvelope<PatientRecordPayload>>({
      url: `/api/v1/web/patient-records/${encodeURIComponent(patientNo)}`,
      method: 'get',
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  createRecord(data: PatientRecordPayload): Promise<AxiosResponse<CreatePatientRecordResponse>> {
    return service<CreatePatientRecordResponse | ApiEnvelope<CreatePatientRecordResponse>>({
      url: '/api/v1/web/patient-records/',
      method: 'post',
      data,
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  importPatientFromHisLis(
    data: HisLisPatientImportRequest
  ): Promise<AxiosResponse<PatientImportPreview>> {
    return service<PatientImportPreview | ApiEnvelope<PatientImportPreview>>({
      url: '/api/v1/web/integration/imports/patient/his-lis',
      method: 'post',
      data,
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  importPatientFromImage(
    data: OcrPatientImportRequest
  ): Promise<AxiosResponse<PatientImportPreview>> {
    return service<PatientImportPreview | ApiEnvelope<PatientImportPreview>>({
      url: '/api/v1/web/integration/imports/patient/image',
      method: 'post',
      data,
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  },

  importPatientFromPdf(
    data: OcrPatientImportRequest
  ): Promise<AxiosResponse<PatientImportPreview>> {
    return service<PatientImportPreview | ApiEnvelope<PatientImportPreview>>({
      url: '/api/v1/web/integration/imports/patient/pdf',
      method: 'post',
      data,
      headers: tenantHeaders()
    }).then(unwrapIfEnvelope)
  }
}

export default patientApi
