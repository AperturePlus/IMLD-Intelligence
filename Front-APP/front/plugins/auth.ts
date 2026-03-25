import store from '@/store'

function authPermission(permission: string): boolean {
  const allPermission = '*:*:*'
  const permissions = (store.getters && store.getters.permissions) || []

  if (!permission || permission.length === 0) {
    return false
  }

  return permissions.some((value: string) => {
    return allPermission === value || value === permission
  })
}

function authRole(role: string): boolean {
  const superAdmin = 'admin'
  const roles = (store.getters && store.getters.roles) || []

  if (!role || role.length === 0) {
    return false
  }

  return roles.some((value: string) => {
    return superAdmin === value || value === role
  })
}

const auth = {
  hasPermi(permission: string): boolean {
    return authPermission(permission)
  },
  hasPermiOr(permissions: string[]): boolean {
    return permissions.some((item) => authPermission(item))
  },
  hasPermiAnd(permissions: string[]): boolean {
    return permissions.every((item) => authPermission(item))
  },
  hasRole(role: string): boolean {
    return authRole(role)
  },
  hasRoleOr(roles: string[]): boolean {
    return roles.some((item) => authRole(item))
  },
  hasRoleAnd(roles: string[]): boolean {
    return roles.every((item) => authRole(item))
  }
}

export default auth
