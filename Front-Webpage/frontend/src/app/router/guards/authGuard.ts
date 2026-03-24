import type { NavigationGuardWithThis } from 'vue-router'

const WHITE_LIST = new Set(['/'])

export const authGuard: NavigationGuardWithThis<undefined> = (to, _from, next) => {
  const token = localStorage.getItem('token')

  if (token) {
    if (to.path === '/') {
      next({ path: '/center' })
      return
    }

    next()
    return
  }

  if (WHITE_LIST.has(to.path)) {
    next()
    return
  }

  next({ path: '/' })
}
