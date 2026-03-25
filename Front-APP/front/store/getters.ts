interface UserState {
  token: string
  avatar: string
  name: string
  roles: string[]
  permissions: string[]
}

interface RootState {
  user: UserState
}

const getters = {
  token: (state: RootState) => state.user.token,
  avatar: (state: RootState) => state.user.avatar,
  name: (state: RootState) => state.user.name,
  roles: (state: RootState) => state.user.roles,
  permissions: (state: RootState) => state.user.permissions
}

export default getters
