const parseTenantId = (value: unknown): number | null => {
  if (typeof value !== 'string' || !value.trim()) {
    return null
  }

  const parsed = Number.parseInt(value, 10)
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return null
  }

  return parsed
}

export const resolveTenantId = (): number => {
  const fromStorage = parseTenantId(localStorage.getItem('tenantId'))
  if (fromStorage) {
    return fromStorage
  }

  const fromEnv = parseTenantId(import.meta.env.VITE_TENANT_ID)
  if (fromEnv) {
    return fromEnv
  }

  return 1
}
