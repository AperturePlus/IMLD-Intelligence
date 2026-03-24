// @ts-nocheck

const toRouteId = (route) => `${route.method} ${route.path}`

export const createMockRouteCatalog = (routeGroups) => {
  return routeGroups
    .flat()
    .map((route) => ({
      ...route,
      id: toRouteId(route)
    }))
    .sort((a, b) => {
      if (a.module !== b.module) {
        return a.module.localeCompare(b.module)
      }
      if (a.path !== b.path) {
        return a.path.localeCompare(b.path)
      }
      return a.method.localeCompare(b.method)
    })
}

export const toMockRouteMarkdown = (routes) => {
  const header = [
    '# Mock Route Catalog',
    '',
    '| Module | Method | Path | Type | Description |',
    '| --- | --- | --- | --- | --- |'
  ]

  const rows = routes.map((route) => {
    return `| ${route.module} | ${route.method} | ${route.path} | ${route.kind} | ${route.description} |`
  })

  return [...header, ...rows].join('\n')
}
