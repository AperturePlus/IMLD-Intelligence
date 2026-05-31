import request from '@/utils/request'

export const devLogin = (): Promise<any> =>
  request({
    url: '/api/v1/app/toc/auth/dev/login',
    method: 'post',
    headers: {
      isToken: false
    }
  })
