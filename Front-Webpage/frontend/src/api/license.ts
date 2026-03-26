import type { AxiosResponse } from 'axios'
import service from './base'
import type {
  ActivateLicenseRequest,
  ApiEnvelope,
  FingerprintResponse,
  LicenseStatusResponse,
  LicenseValidationResponse
} from './types'

const licenseApi = {
  getLicenseStatus(): Promise<AxiosResponse<ApiEnvelope<LicenseStatusResponse>>> {
    return service({
      url: '/api/v1/license/status',
      method: 'get'
    })
  },

  getFingerprint(): Promise<AxiosResponse<ApiEnvelope<FingerprintResponse>>> {
    return service({
      url: '/api/v1/license/fingerprint',
      method: 'get'
    })
  },

  activateLicense(
    data: ActivateLicenseRequest
  ): Promise<AxiosResponse<ApiEnvelope<LicenseValidationResponse>>> {
    return service({
      url: '/api/v1/license/activate',
      method: 'post',
      data
    })
  }
}

export default licenseApi
