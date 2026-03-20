
const TokenKey = 'App-Token'
//这是前端从后端返回会话获得ID储存在前端本地的一些行为函数，以后每次向后端发送请求的时候都会带有这个token以便后端区分不同的会话

export function getToken() {
  return uni.getStorageSync(TokenKey)
}

export function setToken(token) {
  return uni.setStorageSync(TokenKey, token)
}

export function removeToken() {
  return uni.removeStorageSync(TokenKey)
}
