import type { AxiosResponse } from 'axios'
import service from './base'
import { resolveTenantId } from './tenant'
import type { ApiEnvelope, PagedResult } from '@/types/common'
import type { UserAccountPageQuery, UserAccountResponse } from '@/types/identity'

const identityApi = {
  listUsers(
    params: UserAccountPageQuery
  ): Promise<AxiosResponse<ApiEnvelope<PagedResult<UserAccountResponse>>>> {
    return service({
      url: '/api/v1/web/identity/users',
      method: 'get',
      params,
      headers: {
        'X-Tenant-Id': resolveTenantId()
      }
    })
  }
}

export default identityApi
