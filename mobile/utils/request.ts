import config from "@/config";
import { clearTocSession, getTenantId, getToken } from "@/utils/auth";
import errorCode from "@/utils/errorCode";
import { toast, showConfirm } from "@/utils/common";
import { handleMockRequest } from "@/mock";
import { getMockResponse } from "@/api/mockData";
import { ApiResponse, RequestConfig } from "@/types/api";

const timeout = 10000;
const baseUrl = config.baseUrl;

const handleResponseCode = (responseData: Record<string, any>) => {
  const code = Number(responseData.code || 200);
  const msg =
    errorCode[String(code)] ||
    responseData.message ||
    responseData.msg ||
    errorCode.default;
  return { code, msg };
};

const request = (options: RequestConfig): Promise<any> => {
  const isToken = (options.headers || {}).isToken === false;
  const requestHeader: Record<string, unknown> = options.header ?? {};
  const requestConfig: RequestConfig = {
    ...options,
    header: requestHeader,
  };

  if (getToken() && !isToken) {
    requestHeader.Authorization = `Bearer ${getToken()}`;
  }

  const tenantId = getTenantId();
  if (tenantId && !requestHeader["X-Tenant-Id"]) {
    requestHeader["X-Tenant-Id"] = tenantId;
  }

  return new Promise((resolve, reject) => {
    const requestMethod = (requestConfig.method || "get").toUpperCase();
    const requestData = requestConfig.params || requestConfig.data || {};
    const requestUrl =
      requestConfig.baseUrl || `${baseUrl}${requestConfig.url}`;

    if ((config as any).deploymentMode === "mock") {
      const mockData = getMockResponse(requestConfig.url || "", requestMethod);
      const { code, msg } = handleResponseCode(mockData);
      if (code !== 200) {
        toast(msg);
        reject(code);
        return;
      }
      resolve(mockData);
      return;
    }

    if (config.mockMode === "full") {
      handleMockRequest({
        method: requestMethod,
        url: requestUrl,
        data: requestData,
        headers: requestHeader,
      })
        .then((responseData: ApiResponse<any>) => {
          const { code, msg } = handleResponseCode(responseData as any);
          if (code !== 200) {
            toast(msg);
            reject(code);
            return;
          }
          resolve(responseData);
        })
        .catch((error: any) => {
          toast(error?.message || "Mock 请求异常");
          reject(error);
        });
      return;
    }

    uni
      .request({
        method: requestConfig.method || "get",
        timeout: requestConfig.timeout || timeout,
        url: requestUrl,
        data: requestData,
        header: requestHeader,
        dataType: "json",
      })
      .then((response: [any, any]) => {
        const [error, res] = response;

        if (error) {
          toast("后端接口连接异常");
          reject("后端接口连接异常");
          return;
        }

        const responseData = (res && res.data) || {};
        const { code, msg } = handleResponseCode(responseData);

        if (code === 401) {
          showConfirm(
            "登录状态已过期，您可以继续留在该页面，或者重新登录?"
          ).then((modalRes: any) => {
            if (modalRes.confirm) {
              clearTocSession();
              uni.reLaunch({
                url: "/pages/login",
              });
            }
          });
          reject("无效的会话，或者会话已过期，请重新登录。");
          return;
        }

        if (code === 500) {
          toast(msg);
          reject("500");
          return;
        }

        if (code !== 200) {
          toast(msg);
          reject(code);
          return;
        }

        resolve(responseData);
      })
      .catch((error: any) => {
        let message = error?.message || "";

        if (message === "Network Error") {
          message = "后端接口连接异常";
        } else if (message.includes("timeout")) {
          message = "系统接口请求超时";
        } else if (message.includes("Request failed with status code")) {
          message = `系统接口${message.substr(message.length - 3)}异常`;
        }

        toast(message);
        reject(error);
      });
  });
};

export default request;
