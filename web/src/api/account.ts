import type { AxiosResponse } from 'axios'
import service from './base'
import { resolveTenantId } from './tenant'
import type { ApiEnvelope } from '@/types/common'
import type {
  AccountProfileResponse,
  ChangePasswordRequest,
  UpdateAccountProfileRequest
} from '@/types/account'

const tenantHeaders = () => ({
  'X-Tenant-Id': resolveTenantId()
})

const accountApi = {
  getCurrentProfile(): Promise<AxiosResponse<ApiEnvelope<AccountProfileResponse>>> {
    return service<ApiEnvelope<AccountProfileResponse>>({
      url: '/api/v1/web/identity/account/me',
      method: 'get',
      headers: tenantHeaders()
    })
  },

  updateCurrentProfile(
    data: UpdateAccountProfileRequest
  ): Promise<AxiosResponse<ApiEnvelope<AccountProfileResponse>>> {
    return service<ApiEnvelope<AccountProfileResponse>>({
      url: '/api/v1/web/identity/account/me',
      method: 'patch',
      data,
      headers: tenantHeaders()
    })
  },

  changePassword(data: ChangePasswordRequest): Promise<AxiosResponse<ApiEnvelope<void>>> {
    return service<ApiEnvelope<void>>({
      url: '/api/v1/web/identity/account/password',
      method: 'post',
      data,
      headers: tenantHeaders()
    })
  }
}

export default accountApi
