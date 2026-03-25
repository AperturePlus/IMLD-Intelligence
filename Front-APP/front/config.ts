export interface Agreement {
  title: string
  url: string
}

export interface AppInfo {
  name: string
  version: string
  logo: string
  site_url: string
  agreements: Agreement[]
}

export interface AppConfig {
  baseUrl: string
  appInfo: AppInfo
}

const config: AppConfig = {
  baseUrl: 'http://localhost:9090',
  appInfo: {
    name: 'ruoyi-app',
    version: '1.1.0',
    logo: '/static/logo.png',
    site_url: 'http://ruoyi.vip',
    agreements: [
      {
        title: '隐私政策',
        url: 'https://ruoyi.vip/protocol.html'
      },
      {
        title: '用户服务协议',
        url: 'https://ruoyi.vip/protocol.html'
      }
    ]
  }
}

export default config
