import request from '@/utils/request'

export interface TocAuthSession {
  accessToken: string
  refreshToken: string
  expiresAt: string
  tenantId: number
  tocUserId: number
  nickname?: string
}

export interface PhoneCodeSendResult {
  purpose: string
  expiresAt: string
  resendAfterSeconds: number
}

export const wechatLogin = (payload: { jsCode: string; nickname?: string }): Promise<any> =>
  request({
    url: '/api/v1/app/toc/auth/wechat/login',
    method: 'post',
    headers: {
      isToken: false
    },
    data: payload
  })

export const sendPhoneLoginCode = (mobile: string): Promise<any> =>
  request({
    url: '/api/v1/app/toc/auth/phone/send-code',
    method: 'post',
    headers: {
      isToken: false
    },
    data: { mobile }
  })

export const phoneLogin = (payload: { mobile: string; code: string }): Promise<any> =>
  request({
    url: '/api/v1/app/toc/auth/phone/login',
    method: 'post',
    headers: {
      isToken: false
    },
    data: payload
  })

export const refreshTocSession = (refreshToken: string): Promise<any> =>
  request({
    url: '/api/v1/app/toc/auth/refresh',
    method: 'post',
    headers: {
      isToken: false
    },
    data: { refreshToken }
  })

export const logoutTocSession = (refreshToken: string): Promise<any> =>
  request({
    url: '/api/v1/app/toc/auth/logout',
    method: 'post',
    headers: {
      isToken: false
    },
    data: { refreshToken }
  })

