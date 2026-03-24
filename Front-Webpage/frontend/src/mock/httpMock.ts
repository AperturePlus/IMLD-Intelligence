// @ts-nocheck

import { createMockRouteCatalog, toMockRouteMarkdown } from './core/mockRouteCatalog'
import { authDynamicHandlers, authExactHandlers, authRouteDocs } from './handlers/authHandlers'
import { diagnosisDynamicHandlers, diagnosisExactHandlers, diagnosisRouteDocs } from './handlers/diagnosisHandlers'
import { dietDynamicHandlers, dietExactHandlers, dietRouteDocs } from './handlers/dietHandlers'
import { patientDynamicHandlers, patientExactHandlers, patientRouteDocs } from './handlers/patientHandlers'
import { screeningDynamicHandlers, screeningExactHandlers, screeningRouteDocs } from './handlers/screeningHandlers'
import {
  MOCK_DELAY_MS,
  buildResponse,
  parseBody,
  parseQueryObject,
  parseUrlParts,
  resolveHandler,
  wait
} from './core/mockUtils'

const domainExactHandlers = {
  ...authExactHandlers,
  ...patientExactHandlers,
  ...diagnosisExactHandlers,
  ...screeningExactHandlers,
  ...dietExactHandlers
}

const dynamicHandlers = [
  ...authDynamicHandlers,
  ...patientDynamicHandlers,
  ...diagnosisDynamicHandlers,
  ...screeningDynamicHandlers,
  ...dietDynamicHandlers
]

export const mockRouteCatalog = createMockRouteCatalog([
  authRouteDocs,
  patientRouteDocs,
  diagnosisRouteDocs,
  screeningRouteDocs,
  dietRouteDocs,
  [
    {
      module: 'system',
      method: 'GET',
      path: '/api/v1/mock/routes/',
      kind: 'exact',
      description: '返回 mock 路由注册表（JSON）。'
    },
    {
      module: 'system',
      method: 'GET',
      path: '/api/v1/mock/routes/markdown/',
      kind: 'exact',
      description: '返回 mock 路由文档（Markdown 文本）。'
    }
  ]
])

export const getMockRouteCatalog = () => mockRouteCatalog
export const getMockRouteMarkdown = () => toMockRouteMarkdown(mockRouteCatalog)

const exactHandlers = {
  ...domainExactHandlers,
  'GET /api/v1/mock/routes/': async () => ({
    status: 200,
    data: {
      total: mockRouteCatalog.length,
      items: mockRouteCatalog
    }
  }),
  'GET /api/v1/mock/routes/markdown/': async () => ({
    status: 200,
    data: {
      markdown: getMockRouteMarkdown()
    }
  })
}

export const isMockEnabled = () => import.meta.env.DEV && import.meta.env.VITE_USE_MOCK === 'true'

export const createMockAdapter = (config) => {
  const method = (config.method || 'get').toUpperCase()
  const { path } = parseUrlParts(config.url || '')
  const resolved = resolveHandler(method, path, exactHandlers, dynamicHandlers)

  if (!resolved) {
    return null
  }

  return async (adapterConfig) => {
    await wait(MOCK_DELAY_MS)

    const { query: rawQuery } = parseUrlParts(adapterConfig.url || config.url || '')
    const query = parseQueryObject(rawQuery, adapterConfig.params)

    const result = await resolved.handler({
      data: parseBody(adapterConfig.data),
      headers: adapterConfig.headers || {},
      query,
      params: resolved.params,
      config: adapterConfig
    })

    return buildResponse(
      adapterConfig,
      result.status,
      result.data,
      result.statusText || 'OK'
    )
  }
}
