import baseConfig, { AppConfig } from './config.base'
import localConfig from './config.local'

const resolveMockMode = (
  value: unknown,
  fallback: AppConfig['mockMode']
): AppConfig['mockMode'] => {
  if (value === 'full' || value === 'off') {
    return value
  }
  return fallback
}

const localAppInfo = localConfig.appInfo || {}

const config: AppConfig = {
  ...baseConfig,
  ...localConfig,
  mockMode: resolveMockMode(localConfig.mockMode, baseConfig.mockMode),
  appInfo: {
    ...baseConfig.appInfo,
    ...localAppInfo,
    agreements: localAppInfo.agreements || baseConfig.appInfo.agreements
  }
}

export type { Agreement, AppInfo, AppConfig, AppConfigOverride } from './config.base'
export default config
