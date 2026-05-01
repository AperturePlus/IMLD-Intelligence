const tokenKey = 'App-Token'
const tenantIdKey = 'App-Tenant-Id'
const tocUserIdKey = 'App-Toc-User-Id'
const refreshTokenKey = 'App-Refresh-Token'
const tocNicknameKey = 'App-Toc-Nickname'

export function getToken(): string {
  return uni.getStorageSync(tokenKey) || ''
}

export function setToken(token: string): void {
  uni.setStorageSync(tokenKey, token)
}

export function removeToken(): void {
  uni.removeStorageSync(tokenKey)
}

export function getTenantId(): string {
  return uni.getStorageSync(tenantIdKey) || ''
}

export function setTenantId(tenantId: string | number): void {
  if (tenantId === null || tenantId === undefined || tenantId === '') {
    return
  }
  uni.setStorageSync(tenantIdKey, String(tenantId))
}

export function removeTenantId(): void {
  uni.removeStorageSync(tenantIdKey)
}

export function getTocUserId(): string {
  return uni.getStorageSync(tocUserIdKey) || ''
}

export function setTocUserId(tocUserId: string | number): void {
  if (tocUserId === null || tocUserId === undefined || tocUserId === '') {
    return
  }
  uni.setStorageSync(tocUserIdKey, String(tocUserId))
}

export function removeTocUserId(): void {
  uni.removeStorageSync(tocUserIdKey)
}

export function getRefreshToken(): string {
  return uni.getStorageSync(refreshTokenKey) || ''
}

export function setRefreshToken(refreshToken: string): void {
  if (!refreshToken) {
    return
  }
  uni.setStorageSync(refreshTokenKey, refreshToken)
}

export function removeRefreshToken(): void {
  uni.removeStorageSync(refreshTokenKey)
}

export function getTocNickname(): string {
  return uni.getStorageSync(tocNicknameKey) || ''
}

export function setTocNickname(nickname: string): void {
  const value = String(nickname || '').trim()
  if (!value) {
    return
  }
  uni.setStorageSync(tocNicknameKey, value)
}

export function removeTocNickname(): void {
  uni.removeStorageSync(tocNicknameKey)
}

export function clearTocSession(): void {
  removeToken()
  removeRefreshToken()
  removeTenantId()
  removeTocUserId()
  removeTocNickname()
}
