import { getToken } from '@/utils/auth'

const loginPage = '/pages/login'

const whiteList: string[] = [
  '/pages/login',
  '/pages/register',
  '/pages/common/webview/index',
  '/pages/appeal',
  '/pages/forget_password',
  '/pages/reset_password',
  '/pages/login_admin'
]

function checkWhite(url: string): boolean {
  const path = (url || '').split('?')[0]
  return whiteList.includes(path)
}

const interceptorList = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab'] as const

interceptorList.forEach((item) => {
  uni.addInterceptor(item, {
    invoke(to: { url: string }) {
      if (getToken()) {
        if (to.url === loginPage) {
          uni.reLaunch({
            url: '/'
          })
        }
        return true
      }

      if (checkWhite(to.url)) {
        return true
      }

      uni.reLaunch({
        url: loginPage
      })
      return false
    },
    fail(err: unknown) {
      console.log(err)
    }
  })
})
