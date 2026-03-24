// @ts-nocheck

export const MOCK_DELAY_MS = Number(import.meta.env.VITE_MOCK_DELAY_MS ?? 280)

export const parseUrlParts = (input = '') => {
  const url = new URL(input, 'http://mock.local')
  const path = url.pathname.endsWith('/') ? url.pathname : `${url.pathname}/`
  return { path, query: url.searchParams }
}

export const parseBody = (data) => {
  if (data == null) return {}
  if (typeof data === 'string') {
    try {
      return JSON.parse(data)
    } catch {
      return {}
    }
  }
  if (typeof data === 'object') return data
  return {}
}

export const parseQueryObject = (searchParams, extraParams) => {
  const query = {}
  searchParams.forEach((value, key) => {
    query[key] = value
  })

  if (extraParams && typeof extraParams === 'object') {
    Object.entries(extraParams).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        query[key] = String(value)
      }
    })
  }

  return query
}

export const buildResponse = (config, status, data, statusText = 'OK') => ({
  data,
  status,
  statusText,
  headers: {},
  config,
  request: { mocked: true }
})

export const wait = (delay) => new Promise((resolve) => setTimeout(resolve, delay))

export const resolveHandler = (method, path, exactHandlers, dynamicHandlers) => {
  const routeKey = `${method} ${path}`
  if (exactHandlers[routeKey]) {
    return { handler: exactHandlers[routeKey], params: {} }
  }

  for (const route of dynamicHandlers) {
    if (route.method !== method) continue

    const matched = path.match(route.pattern)
    if (matched) {
      return {
        handler: route.handler,
        params: route.buildParams ? route.buildParams(matched) : {}
      }
    }
  }

  return null
}
