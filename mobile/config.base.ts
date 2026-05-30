export interface Agreement {
  title: string;
  url: string;
}

export interface AppInfo {
  name: string;
  version: string;
  logo: string;
  site_url: string;
  agreements: Agreement[];
}

export interface AppConfig {
  baseUrl: string;
  mockMode: "full" | "off";
  deploymentMode: "dev" | "prod" | "mock";
  appInfo: AppInfo;
}

export type AppConfigOverride = Partial<Omit<AppConfig, "appInfo">> & {
  appInfo?: Partial<AppInfo>;
};

const baseConfig: AppConfig = {
  baseUrl: "http://localhost:8080",
  mockMode: "off",
  deploymentMode: "prod",
  appInfo: {
    name: "ruoyi-app",
    version: "1.1.0",
    logo: "/static/logo.png",
    site_url: "http://ruoyi.vip",
    agreements: [
      {
        title: "隐私政策",
        url: "https://ruoyi.vip/protocol.html",
      },
      {
        title: "用户服务协议",
        url: "https://ruoyi.vip/protocol.html",
      },
    ],
  },
};

export default baseConfig;
