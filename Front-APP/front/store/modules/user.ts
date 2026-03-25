import config from '@/config'
import storage from '@/utils/storage'
import constant from '@/utils/constant'
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'

interface UserState {
  token: string
  name: string
  avatar: string
  roles: string[]
  permissions: string[]
  sessionId: string
}

const baseUrl = config.baseUrl

const user = {
  state: {
    token: getToken(),
    name: storage.get(constant.name) || '',
    avatar: storage.get(constant.avatar) || '',
    roles: storage.get(constant.roles) || [],
    permissions: storage.get(constant.permissions) || [],
    sessionId: storage.get(constant.sessionId) || ''
  } as UserState,

  mutations: {
    SET_TOKEN(state: UserState, token: string) {
      state.token = token
    },
    SET_NAME(state: UserState, name: string) {
      state.name = name
      storage.set(constant.name, name)
    },
    SET_AVATAR(state: UserState, avatar: string) {
      state.avatar = avatar
      storage.set(constant.avatar, avatar)
    },
    SET_ROLES(state: UserState, roles: string[]) {
      state.roles = roles
      storage.set(constant.roles, roles)
    },
    SET_PERMISSIONS(state: UserState, permissions: string[]) {
      state.permissions = permissions
      storage.set(constant.permissions, permissions)
    },
    SET_SESSION_ID(state: UserState, sessionId: string) {
      state.sessionId = sessionId
      storage.set(constant.sessionId, sessionId)
    }
  },

  actions: {
    Login({ commit }: any, userInfo: any): Promise<void> {
      const username = (userInfo.username || '').trim()
      const password = userInfo.password
      const code = userInfo.code
      const uuid = userInfo.uuid
      const role = userInfo.role

      return new Promise((resolve, reject) => {
        login(username, password, code, uuid, role)
          .then((res: any) => {
            setToken(res.msg)
            commit('SET_TOKEN', res.msg)
            commit('SET_SESSION_ID', res.msg)
            resolve()
          })
          .catch((error: any) => {
            reject(error)
          })
      })
    },

    GetInfo({ commit }: any): Promise<any> {
      return new Promise((resolve, reject) => {
        getInfo()
          .then((res: any) => {
            const currentUser = res.user
            const avatar =
              currentUser == null || currentUser.avatar == null || currentUser.avatar === ''
                ? require('@/static/images/profile.jpg')
                : `${baseUrl}${currentUser.avatar}`

            const username =
              currentUser == null || currentUser.userName == null || currentUser.userName === ''
                ? ''
                : currentUser.userName

            if (res.roles && res.roles.length > 0) {
              commit('SET_ROLES', res.roles)
              commit('SET_PERMISSIONS', res.permissions)
            } else {
              commit('SET_ROLES', ['ROLE_DEFAULT'])
            }

            commit('SET_NAME', username)
            commit('SET_AVATAR', avatar)
            resolve(res)
          })
          .catch((error: any) => {
            reject(error)
          })
      })
    },

    LogOut({ commit, state }: any): Promise<void> {
      return new Promise((resolve, reject) => {
        logout(state.token)
          .then(() => {
            commit('SET_TOKEN', '')
            commit('SET_ROLES', [])
            commit('SET_PERMISSIONS', [])
            removeToken()
            storage.clean()
            resolve()
          })
          .catch((error: any) => {
            reject(error)
          })
      })
    }
  }
}

export default user
