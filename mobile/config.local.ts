import type { AppConfigOverride } from "./config.base";

const localConfig: AppConfigOverride = {
  mockMode: "full",
  // 如需启用 mock 部署模式（无需后端），将下行改为 'mock'
  deploymentMode: "dev",
};

export default localConfig;