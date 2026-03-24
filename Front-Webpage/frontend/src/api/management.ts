import type { AxiosResponse } from 'axios'
import service from './base'
import type {
  DietPatientsResponse,
  DietPlanResponse,
  PushDietPlanResponse,
  RegenerateDietPlanResponse,
  ScreeningOverviewQuery,
  ScreeningOverviewResponse
} from './types'

const managementApi = {
  getScreeningOverview(
    params: ScreeningOverviewQuery = {}
  ): Promise<AxiosResponse<ScreeningOverviewResponse>> {
    return service({
      url: '/api/v1/screening/overview/',
      method: 'get',
      params
    })
  },

  getDietPatients(params: { keyword?: string } = {}): Promise<AxiosResponse<DietPatientsResponse>> {
    return service({
      url: '/api/v1/diet/patients/',
      method: 'get',
      params
    })
  },

  getDietPlan(patientId: string): Promise<AxiosResponse<DietPlanResponse>> {
    return service({
      url: `/api/v1/diet/patients/${patientId}/plan/`,
      method: 'get'
    })
  },

  regenerateDietPlan(patientId: string): Promise<AxiosResponse<RegenerateDietPlanResponse>> {
    return service({
      url: `/api/v1/diet/patients/${patientId}/regenerate/`,
      method: 'post'
    })
  },

  pushDietPlan(patientId: string): Promise<AxiosResponse<PushDietPlanResponse>> {
    return service({
      url: `/api/v1/diet/patients/${patientId}/push/`,
      method: 'post'
    })
  }
}

export default managementApi
