// @ts-nocheck

import {
  createToken,
  loadEmailCodes,
  loadTokens,
  loadUsers,
  readAuthorizationToken,
  saveEmailCodes,
  saveTokens,
  saveUsers
} from '../core/mockState'
import defaultDoctorAvatar from '@/assets/default-doctor.svg'

const SUCCESS_CODE = 200
const EMAIL_CODE_EXPIRES_MS = 10 * 60 * 1000
const EMAIL_CODE_RESEND_MS = 60 * 1000

const normalizeText = (value = '') => String(value).trim()
const normalizeEmail = (value = '') => normalizeText(value).toLowerCase()

const envelope = (code, message, data) => {
  if (data === undefined) {
    return { code, message }
  }
  return { code, message, data }
}

const success = (data) => ({
  status: 200,
  data: envelope(SUCCESS_CODE, 'success', data)
})

const fail = (status, message, data = undefined) => ({
  status,
  statusText: 'Error',
  data: envelope(status, message, data)
})

const resolveRoleCodes = (role = '') => {
  const normalized = normalizeText(role).toLowerCase()
  if (normalized === 'admin') {
    return ['SYSTEM_ADMIN']
  }
  return ['DOCTOR']
}

const resolveUserType = (role = '') => {
  const normalized = normalizeText(role).toLowerCase()
  if (normalized === 'admin') {
    return 'ADMIN'
  }
  return 'DOCTOR'
}

const maskMobile = (value = '') => {
  const mobile = normalizeText(value)
  if (!mobile) {
    return null
  }
  if (mobile.length <= 7) {
    return `${mobile.slice(0, 2)}****${mobile.slice(-1)}`
  }
  return `${mobile.slice(0, 3)}****${mobile.slice(-4)}`
}

const buildAccountProfile = (user, userIndex) => ({
  userId: userIndex + 1,
  tenantId: 1,
  userNo: user.userNo || `USR-${String(userIndex + 1).padStart(3, '0')}`,
  username: user.username,
  displayName: user.displayName || user.username,
  userType: user.userType || resolveUserType(user.role),
  deptName: user.deptName || (resolveUserType(user.role) === 'ADMIN' ? '系统管理部' : '肝病医学科'),
  email: user.email || null,
  mobileMasked: maskMobile(user.mobile),
  roleCodes: resolveRoleCodes(user.role),
  lastLoginAt: user.lastLoginAt || new Date().toISOString()
})

const buildAuthSession = (user, token, userIndex) => ({
  accessToken: token,
  refreshToken: `refresh_${token}`,
  expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
  user: {
    ...buildAccountProfile(user, userIndex),
    avatar: defaultDoctorAvatar
  }
})

const generateEmailCode = () => String(Math.floor(Math.random() * 900000) + 100000)

const buildCodeKey = (purpose, username, email) => `${purpose}|${username}|${email}`

const getCodeRecord = (purpose, username, email) => {
  const key = buildCodeKey(purpose, username, email)
  const codes = loadEmailCodes()
  return { key, codes, record: codes[key] }
}

const issueCode = (purpose, username, email) => {
  const now = Date.now()
  const { key, codes, record } = getCodeRecord(purpose, username, email)

  if (record && now - (record.createdAt || 0) < EMAIL_CODE_RESEND_MS) {
    const retryAfter = Math.ceil((EMAIL_CODE_RESEND_MS - (now - record.createdAt)) / 1000)
    return fail(429, `请在 ${retryAfter} 秒后重试`)
  }

  const code = generateEmailCode()
  const expiresAt = new Date(now + EMAIL_CODE_EXPIRES_MS).toISOString()
  codes[key] = {
    code,
    purpose,
    username,
    email,
    createdAt: now,
    expiresAt
  }
  saveEmailCodes(codes)

  return success({
    email,
    purpose,
    expiresAt,
    resendAfterSeconds: Math.floor(EMAIL_CODE_RESEND_MS / 1000)
  })
}

const verifyCode = (purpose, username, email, inputCode) => {
  const now = Date.now()
  const { key, codes, record } = getCodeRecord(purpose, username, email)

  if (!record) {
    return { ok: false, error: fail(400, '验证码不存在或已失效') }
  }
  if (new Date(record.expiresAt).getTime() <= now) {
    delete codes[key]
    saveEmailCodes(codes)
    return { ok: false, error: fail(400, '验证码已过期') }
  }
  if (String(record.code) !== String(inputCode)) {
    return { ok: false, error: fail(400, '验证码错误') }
  }

  delete codes[key]
  saveEmailCodes(codes)
  return { ok: true }
}

const loginWithUsernamePassword = (usernameInput, passwordInput) => {
  const username = normalizeText(usernameInput)
  const password = String(passwordInput || '')

  if (!username || !password) {
    return fail(400, '请输入账号和密码')
  }

  const users = loadUsers()
  const userIndex = users.findIndex((item) => item.username === username)
  if (userIndex < 0 || users[userIndex].password !== password) {
    return fail(401, '账号或密码错误')
  }

  const token = createToken(username)
  const tokens = loadTokens()
  tokens[token] = username
  saveTokens(tokens)

  return success(buildAuthSession(users[userIndex], token, userIndex))
}

const resolveAuthorizedUser = (headers) => {
  const token = readAuthorizationToken(headers)
  if (!token) {
    return { ok: false, error: fail(401, '未认证') }
  }

  const tokenMap = loadTokens()
  const username = tokenMap[token]
  if (!username) {
    return { ok: false, error: fail(401, '凭证无效或已过期') }
  }

  const users = loadUsers()
  const userIndex = users.findIndex((item) => item.username === username)
  if (userIndex < 0) {
    return { ok: false, error: fail(404, '用户不存在') }
  }

  return { ok: true, token, tokenMap, users, user: users[userIndex], userIndex }
}

export const authExactHandlers = {
  'POST /api/v1/web/identity/auth/login': async ({ data }) =>
    loginWithUsernamePassword(data.username, data.password),

  'POST /api/v1/web/identity/auth/register/email-code': async ({ data }) => {
    const username = normalizeText(data.username)
    const email = normalizeEmail(data.email)

    if (!username || !email) {
      return fail(400, '请输入账号和邮箱')
    }

    const users = loadUsers()
    if (users.some((item) => item.username === username)) {
      return fail(409, '用户名已存在')
    }
    if (users.some((item) => normalizeEmail(item.email) === email)) {
      return fail(409, '邮箱已存在')
    }

    return issueCode('REGISTER', username, email)
  },

  'POST /api/v1/web/identity/auth/register': async ({ data }) => {
    const username = normalizeText(data.username)
    const email = normalizeEmail(data.email)
    const password = String(data.password || '')
    const emailCode = normalizeText(data.emailCode)

    if (!username || !email || !password || !emailCode) {
      return fail(400, '请填写完整注册信息')
    }

    const users = loadUsers()
    if (users.some((item) => item.username === username)) {
      return fail(409, '用户名已存在')
    }
    if (users.some((item) => normalizeEmail(item.email) === email)) {
      return fail(409, '邮箱已存在')
    }

    const verifyResult = verifyCode('REGISTER', username, email, emailCode)
    if (!verifyResult.ok) {
      return verifyResult.error
    }

    users.push({ username, email, password, role: 'doctor' })
    saveUsers(users)

    const token = createToken(username)
    const tokens = loadTokens()
    tokens[token] = username
    saveTokens(tokens)

    return success(buildAuthSession(users[users.length - 1], token, users.length - 1))
  },

  'POST /api/v1/web/identity/auth/password/forgot': async ({ data }) => {
    const username = normalizeText(data.username)
    const email = normalizeEmail(data.email)

    if (!username || !email) {
      return fail(400, '请输入账号和邮箱')
    }

    const users = loadUsers()
    const user = users.find((item) => item.username === username)
    if (!user || normalizeEmail(user.email) !== email) {
      return fail(400, '账号与邮箱不匹配')
    }

    return issueCode('PASSWORD_RESET', username, email)
  },

  'POST /api/v1/web/identity/auth/password/reset': async ({ data }) => {
    const username = normalizeText(data.username)
    const email = normalizeEmail(data.email)
    const emailCode = normalizeText(data.emailCode)
    const newPassword = String(data.newPassword || '')

    if (!username || !email || !emailCode || !newPassword) {
      return fail(400, '请填写完整重置信息')
    }

    const users = loadUsers()
    const userIndex = users.findIndex((item) => item.username === username)
    if (userIndex < 0 || normalizeEmail(users[userIndex].email) !== email) {
      return fail(400, '账号与邮箱不匹配')
    }

    const verifyResult = verifyCode('PASSWORD_RESET', username, email, emailCode)
    if (!verifyResult.ok) {
      return verifyResult.error
    }

    users[userIndex].password = newPassword
    saveUsers(users)

    return {
      status: 200,
      data: envelope(SUCCESS_CODE, 'success')
    }
  },

  'GET /api/v1/web/identity/account/me': async ({ headers }) => {
    const resolved = resolveAuthorizedUser(headers)
    if (!resolved.ok) {
      return resolved.error
    }

    return success(buildAccountProfile(resolved.user, resolved.userIndex))
  },

  'PATCH /api/v1/web/identity/account/me': async ({ headers, data }) => {
    const resolved = resolveAuthorizedUser(headers)
    if (!resolved.ok) {
      return resolved.error
    }

    const users = resolved.users
    const user = { ...resolved.user }
    const nextEmail = data.email == null ? user.email : normalizeEmail(data.email)
    const nextMobile = normalizeText(data.mobilePlaintext)
    const emailChanged = normalizeEmail(nextEmail || '') !== normalizeEmail(user.email || '')
    const mobileChanged = Boolean(nextMobile)

    if ((emailChanged || mobileChanged) && String(data.currentPassword || '') !== user.password) {
      return fail(403, '当前密码错误')
    }
    if (emailChanged && users.some((item, index) => index !== resolved.userIndex && normalizeEmail(item.email) === nextEmail)) {
      return fail(409, '邮箱已存在')
    }

    if (data.displayName !== undefined) {
      const displayName = normalizeText(data.displayName)
      if (!displayName) {
        return fail(400, '姓名不能为空')
      }
      user.displayName = displayName
    }
    if (data.deptName !== undefined) {
      user.deptName = normalizeText(data.deptName)
    }
    if (data.email !== undefined) {
      if (!nextEmail) {
        return fail(400, '邮箱不能为空')
      }
      user.email = nextEmail
    }
    if (nextMobile) {
      user.mobile = nextMobile
    }

    users[resolved.userIndex] = user
    saveUsers(users)
    return success(buildAccountProfile(user, resolved.userIndex))
  },

  'POST /api/v1/web/identity/account/password': async ({ headers, data }) => {
    const resolved = resolveAuthorizedUser(headers)
    if (!resolved.ok) {
      return resolved.error
    }

    const currentPassword = String(data.currentPassword || '')
    const newPassword = String(data.newPassword || '')
    const confirmPassword = String(data.confirmPassword || '')
    if (!currentPassword || !newPassword || !confirmPassword) {
      return fail(400, '请填写完整密码信息')
    }
    if (currentPassword !== resolved.user.password) {
      return fail(403, '当前密码错误')
    }
    if (newPassword !== confirmPassword) {
      return fail(400, '两次新密码不一致')
    }

    const users = resolved.users
    users[resolved.userIndex] = {
      ...resolved.user,
      password: newPassword
    }
    saveUsers(users)

    delete resolved.tokenMap[resolved.token]
    saveTokens(resolved.tokenMap)

    return {
      status: 200,
      data: envelope(SUCCESS_CODE, 'success')
    }
  },

  // Backward compatibility for old auth endpoints in mock mode.
  'POST /dj-rest-auth/login/': async ({ data }) =>
    loginWithUsernamePassword(data.username, data.password),

  'POST /dj-rest-auth/registration/': async ({ data }) => {
    const registerData = {
      username: data.username,
      email: data.email,
      password: data.password1,
      emailCode: '000000'
    }

    const username = normalizeText(registerData.username)
    const email = normalizeEmail(registerData.email)
    if (!username || !email || !registerData.password) {
      return fail(400, '请填写完整注册信息')
    }

    const users = loadUsers()
    if (users.some((item) => item.username === username)) {
      return fail(409, '用户名已存在')
    }

    users.push({ username, email, password: registerData.password, role: 'doctor' })
    saveUsers(users)
    return success({ detail: '注册成功' })
  },

  'GET /dj-rest-auth/user/': async ({ headers }) => {
    const token = readAuthorizationToken(headers)
    if (!token) {
      return fail(401, '未认证')
    }

    const tokenMap = loadTokens()
    const username = tokenMap[token]
    if (!username) {
      return fail(401, '凭证无效或已过期')
    }

    const user = loadUsers().find((item) => item.username === username)
    if (!user) {
      return fail(404, '用户不存在')
    }

    return success({
      username: user.username,
      email: user.email,
      role: user.role
    })
  }
}

export const authDynamicHandlers = []

export const authRouteDocs = [
  {
    module: 'auth',
    method: 'POST',
    path: '/api/v1/web/identity/auth/login',
    kind: 'exact',
    description: '账号密码登录，返回 accessToken/refreshToken。'
  },
  {
    module: 'auth',
    method: 'POST',
    path: '/api/v1/web/identity/auth/register/email-code',
    kind: 'exact',
    description: '发送注册邮箱验证码。'
  },
  {
    module: 'auth',
    method: 'POST',
    path: '/api/v1/web/identity/auth/register',
    kind: 'exact',
    description: '邮箱验证码注册并直接登录。'
  },
  {
    module: 'auth',
    method: 'POST',
    path: '/api/v1/web/identity/auth/password/forgot',
    kind: 'exact',
    description: '发送找回密码邮箱验证码。'
  },
  {
    module: 'auth',
    method: 'POST',
    path: '/api/v1/web/identity/auth/password/reset',
    kind: 'exact',
    description: '使用邮箱验证码重置密码。'
  },
  {
    module: 'identity',
    method: 'GET',
    path: '/api/v1/web/identity/account/me',
    kind: 'exact',
    description: '查询当前登录账号资料。'
  },
  {
    module: 'identity',
    method: 'PATCH',
    path: '/api/v1/web/identity/account/me',
    kind: 'exact',
    description: '更新当前登录账号资料。'
  },
  {
    module: 'identity',
    method: 'POST',
    path: '/api/v1/web/identity/account/password',
    kind: 'exact',
    description: '修改当前登录账号密码。'
  }
]
