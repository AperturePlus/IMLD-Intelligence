import type { AxiosResponse } from 'axios'
import service from './base'
import type {
  CreatePatientRecordResponse,
  PatientListQuery,
  PatientListResponse,
  PatientRecordPayload
} from './types'

const patientApi = {
  list(params: PatientListQuery = {}): Promise<AxiosResponse<PatientListResponse>> {
    return service({
      url: '/api/v1/patients/',
      method: 'get',
      params
    })
  },

  createRecord(data: PatientRecordPayload): Promise<AxiosResponse<CreatePatientRecordResponse>> {
    return service({
      url: '/api/v1/patient-records/',
      method: 'post',
      data
    })
  }
}

export default patientApi
