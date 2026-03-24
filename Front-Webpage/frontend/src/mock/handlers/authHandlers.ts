// @ts-nocheck

import {
  createToken,
  loadTokens,
  loadUsers,
  readAuthorizationToken,
  saveTokens,
  saveUsers
} from '../core/mockState'

export const authExactHandlers = {
  'POST /dj-rest-auth/login/': async ({ data }) => {
    const { username = '', password = '' } = data
    if (!username || !password) {
      return { status: 400, statusText: 'Bad Request', data: { non_field_errors: ['请输入账号和密码'] } }
    }

    const users = loadUsers()
    const currentUser = users.find((item) => item.username === username)
    if (!currentUser || currentUser.password !== password) {
      return { status: 400, statusText: 'Bad Request', data: { non_field_errors: ['账号或密码错误'] } }
    }

    const token = createToken(username)
    const tokens = loadTokens()
    tokens[token] = username
    saveTokens(tokens)

    return {
      status: 200,
      data: {
        key: token,
        token,
        access: token,
        username
      }
    }
  },

  'POST /dj-rest-auth/registration/': async ({ data }) => {
    const { username = '', email = '', password1 = '', password2 = '' } = data
    const errors = {}

    if (!username) errors.username = ['请输入账号']
    if (!email) errors.email = ['请输入邮箱']
    if (!password1) errors.password1 = ['请输入密码']
    if (password1 && password1.length < 6) errors.password1 = ['密码至少为 6 位']
    if (password1 !== password2) errors.password2 = ['两次密码不一致']

    const users = loadUsers()
    if (users.some((item) => item.username === username)) {
      errors.username = ['该账号已存在']
    }

    if (Object.keys(errors).length > 0) {
      return { status: 400, statusText: 'Bad Request', data: errors }
    }

    users.push({ username, email, password: password1, role: 'doctor' })
    saveUsers(users)
    return { status: 201, statusText: 'Created', data: { detail: '注册成功' } }
  },

  'GET /dj-rest-auth/user/': async ({ headers }) => {
    const token = readAuthorizationToken(headers)
    if (!token) return { status: 401, statusText: 'Unauthorized', data: { detail: '未认证' } }

    const tokenMap = loadTokens()
    const username = tokenMap[token]
    if (!username) return { status: 401, statusText: 'Unauthorized', data: { detail: '凭证无效或已过期' } }

    const user = loadUsers().find((item) => item.username === username)
    if (!user) return { status: 404, statusText: 'Not Found', data: { detail: '用户不存在' } }

    return {
      status: 200,
      data: {
        username: user.username,
        email: user.email,
        role: user.role
      }
    }
  }
}

export const authDynamicHandlers = []

export const authRouteDocs = [
  {
    module: 'auth',
    method: 'POST',
    path: '/dj-rest-auth/login/',
    kind: 'exact',
    description: '账号密码登录并返回 token。'
  },
  {
    module: 'auth',
    method: 'POST',
    path: '/dj-rest-auth/registration/',
    kind: 'exact',
    description: '用户注册。'
  },
  {
    module: 'auth',
    method: 'GET',
    path: '/dj-rest-auth/user/',
    kind: 'exact',
    description: '查询当前登录用户信息。'
  }
]
