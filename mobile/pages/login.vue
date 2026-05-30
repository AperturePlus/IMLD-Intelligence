<template>
  <view class="normal-login-container">
    <image
      class="bg-image"
      src="/static/images/login.png"
      mode="aspectFill"
    ></image>

    <view class="logo-content align-center justify-center flex-direction flex">
      <text class="title" style="font-weight: bold; font-size: 24px"
        >数智肝循</text
      >
      <text class="subtitle" style="font-size: 16px; margin-top: 5px"
        >IMLD 患者管理平台</text
      >
    </view>

    <view class="login-form-content">
      <view v-if="!isMockMode" class="mode-tabs">
        <view
          class="mode-tab"
          :class="mode === 'wechat' ? 'active' : ''"
          @tap="mode = 'wechat'"
          >微信登录</view
        >
        <view
          class="mode-tab"
          :class="mode === 'phone' ? 'active' : ''"
          @tap="mode = 'phone'"
          >手机号登录</view
        >
      </view>

      <view v-if="!isMockMode && mode === 'wechat'">
        <button
          @click="handleWechatLogin"
          class="login-btn cu-btn block bg-blue lg round"
        >
          微信一键登录
        </button>
        <view class="tips text-center">
          <text class="text-white">登录即代表同意</text>
          <text @click="handlePrivacy" class="text-blue underline-text"
            >隐私政策</text
          >
          <text class="text-white">与</text>
          <text @click="handleUserAgrement" class="text-blue underline-text"
            >用户协议</text
          >
        </view>
      </view>

      <view v-else-if="!isMockMode">
        <view class="input-item flex align-center">
          <view class="iconfont icon-phone icon"></view>
          <input
            v-model="phoneForm.mobile"
            class="input"
            type="number"
            placeholder="请输入手机号"
            maxlength="20"
          />
        </view>

        <view class="input-item flex align-center code-row">
          <view class="iconfont icon-code icon"></view>
          <input
            v-model="phoneForm.code"
            class="input"
            type="number"
            placeholder="请输入验证码"
            maxlength="6"
          />
          <button
            class="code-btn"
            :disabled="cooldownSeconds > 0 || sendingCode"
            @click="handleSendCode"
          >
            {{ cooldownSeconds > 0 ? cooldownSeconds + "s" : "发送验证码" }}
          </button>
        </view>

        <view class="action-btn">
          <button
            @click="handlePhoneLogin"
            class="login-btn cu-btn block bg-blue lg round"
          >
            登录
          </button>
        </view>
      </view>

      <view v-if="isMockMode" class="dev-login-wrap">
        <view class="dev-divider">
          <text class="dev-divider-text">Mock 模式</text>
        </view>
        <button
          @click="handleMockLogin"
          class="login-btn cu-btn block bg-orange lg round"
        >
          Mock 模式一键登录
        </button>
      </view>

      <view v-if="isDevMode" class="dev-login-wrap">
        <view class="dev-divider">
          <text class="dev-divider-text">开发环境</text>
        </view>
        <button
          @click="handleDevLogin"
          class="login-btn cu-btn block bg-cyan lg round"
        >
          开发模式一键登录（李晓华）
        </button>
      </view>
    </view>

    <view class="bottom-footer flex flex-direction align-center">
      <text @click="handleAdminLogin" class="admin-link">管理员入口</text>
      <text class="copyright">© 2026 四川大学华西临床医学院 IMLD 课题组</text>
      <text class="copyright">All rights reserved.</text>
    </view>
  </view>
</template>

<script>
import { phoneLogin, sendPhoneLoginCode, wechatLogin } from "@/api/tocAuth";
import { devLogin } from "@/api/devAuth";
import config from "@/config";
import {
  setRefreshToken,
  setTenantId,
  setTocNickname,
  setTocUserId,
  setToken,
} from "@/utils/auth";

export default {
  data() {
    return {
      mode: "wechat",
      globalConfig: getApp().globalData ? getApp().globalData.config : {},
      phoneForm: { mobile: "", code: "" },
      sendingCode: false,
      cooldownSeconds: 0,
      cooldownTimer: null,
    };
  },
  computed: {
    isMockMode() {
      return config.deploymentMode === "mock";
    },
    isDevMode() {
      return config.deploymentMode === "dev";
    },
  },
  onUnload() {
    this.stopCooldown();
  },
  methods: {
    handleAdminLogin() {
      this.$tab.navigateTo("/pages/login-admin");
    },
    handlePrivacy() {
      const site = this.globalConfig.appInfo.agreements[0];
      this.$tab.navigateTo(
        `/pages/common/webview/index?title=${site.title}&url=${site.url}`
      );
    },
    handleUserAgrement() {
      const site = this.globalConfig.appInfo.agreements[1];
      this.$tab.navigateTo(
        `/pages/common/webview/index?title=${site.title}&url=${site.url}`
      );
    },
    applySession(response) {
      const data = (response && response.data) || {};
      if (!data.accessToken || !data.tenantId || !data.tocUserId) {
        this.$modal.msgError("登录失败，请稍后重试");
        return false;
      }
      setToken(data.accessToken);
      setRefreshToken(data.refreshToken || "");
      setTenantId(data.tenantId);
      setTocUserId(data.tocUserId);
      setTocNickname(data.nickname || "");
      return true;
    },
    handleMockLogin() {
      this.$modal.loading("Mock 模式登录中...");
      setToken("mock_token_xxx");
      setRefreshToken("mock_refresh_token");
      setTenantId(1);
      setTocUserId(999);
      setTocNickname("Mock用户");
      this.$modal.closeLoading();
      this.$tab.reLaunch("/pages/index");
    },
    handleDevLogin() {
      this.$modal.loading("开发模式登录中...");
      devLogin()
        .then((res) => {
          this.$modal.closeLoading();
          if (this.applySession(res)) {
            this.$tab.reLaunch("/pages/index");
          }
        })
        .catch(() => {
          this.$modal.closeLoading();
          this.$modal.msgError("开发模式登录失败，请检查后端服务");
        });
    },
    handleWechatLogin() {
      this.$modal.loading("登录中，请稍候...");
      uni.login({
        provider: "weixin",
        success: (loginRes) => {
          const jsCode = (loginRes && loginRes.code) || "";
          if (!jsCode) {
            this.$modal.closeLoading();
            this.$modal.msgError("获取微信登录凭证失败");
            return;
          }
          wechatLogin({ jsCode })
            .then((res) => {
              this.$modal.closeLoading();
              if (this.applySession(res)) {
                this.$tab.reLaunch("/pages/index");
              }
            })
            .catch(() => {
              this.$modal.closeLoading();
              this.$modal.msgError("微信登录失败，请稍后重试");
            });
        },
        fail: () => {
          this.$modal.closeLoading();
          this.$modal.msgError("当前环境不支持微信登录");
        },
      });
    },
    validateMobile(mobile) {
      const value = String(mobile || "").trim();
      if (!value) {
        return "";
      }
      if (!/^1\\d{10}$/.test(value)) {
        return "";
      }
      return value;
    },
    startCooldown(seconds) {
      this.stopCooldown();
      this.cooldownSeconds = Math.max(0, Number(seconds || 0));
      if (this.cooldownSeconds <= 0) {
        return;
      }
      this.cooldownTimer = setInterval(() => {
        if (this.cooldownSeconds <= 1) {
          this.stopCooldown();
          return;
        }
        this.cooldownSeconds -= 1;
      }, 1000);
    },
    stopCooldown() {
      if (this.cooldownTimer) {
        clearInterval(this.cooldownTimer);
        this.cooldownTimer = null;
      }
      this.cooldownSeconds = 0;
    },
    handleSendCode() {
      if (this.sendingCode || this.cooldownSeconds > 0) {
        return;
      }
      const rawMobile = String(this.phoneForm.mobile || "").trim();
      const mobile = this.isMockMode
        ? rawMobile
        : this.validateMobile(rawMobile);
      if (!this.isMockMode && !mobile) {
        this.$modal.msgError("请输入正确的手机号");
        return;
      }
      this.sendingCode = true;
      sendPhoneLoginCode(mobile)
        .then((res) => {
          const data = (res && res.data) || {};
          const seconds =
            data.resendAfterSeconds === undefined
              ? 60
              : data.resendAfterSeconds;
          this.startCooldown(seconds);
          uni.showToast({ title: "验证码已发送", icon: "success" });
        })
        .catch(() => {
          this.$modal.msgError("验证码发送失败，请稍后重试");
        })
        .finally(() => {
          this.sendingCode = false;
        });
    },
    handlePhoneLogin() {
      const rawMobile = String(this.phoneForm.mobile || "").trim();
      const rawCode = String(this.phoneForm.code || "").trim();
      const mobile = this.isMockMode
        ? rawMobile
        : this.validateMobile(rawMobile);
      if (!this.isMockMode && !mobile) {
        this.$modal.msgError("请输入正确的手机号");
        return;
      }
      if (!this.isMockMode && !rawCode) {
        this.$modal.msgError("请输入验证码");
        return;
      }
      this.$modal.loading("登录中，请稍候...");
      phoneLogin({
        mobile: mobile || "mock-mobile",
        code: rawCode || "mock-code",
      })
        .then((res) => {
          this.$modal.closeLoading();
          if (this.applySession(res)) {
            this.$tab.reLaunch("/pages/index");
          }
        })
        .catch(() => {
          this.$modal.closeLoading();
          this.$modal.msgError(
            this.isMockMode
              ? "Mock 登录失败，请检查 mock 配置"
              : "登录失败，请检查验证码"
          );
        });
    },
  },
};
</script>

<style lang="scss">
page {
  height: 100%;
  background-color: #f5f6f7;
}

.normal-login-container {
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
  padding-bottom: 120px;

  .bg-image {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: -1;
  }

  .logo-content {
    width: 100%;
    font-size: 21px;
    text-align: center;
    padding-top: 15%;
    color: #ffffff;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);

    image {
      border-radius: 4px;
    }

    .title {
      margin-left: 10px;
    }
  }

  .login-form-content {
    text-align: center;
    margin: 20px auto;
    margin-top: 15%;
    width: 80%;

    .mode-tabs {
      display: flex;
      justify-content: center;
      gap: 10px;
      margin-bottom: 20px;

      .mode-tab {
        padding: 8px 14px;
        border-radius: 20px;
        background-color: rgba(255, 255, 255, 0.25);
        color: #ffffff;
        font-size: 14px;
      }

      .mode-tab.active {
        background-color: rgba(255, 255, 255, 0.85);
        color: #2b85e4;
        font-weight: bold;
      }
    }

    .input-item {
      margin: 20px auto;
      background-color: rgba(255, 255, 255, 0.8);
      height: 45px;
      border-radius: 20px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

      .icon {
        font-size: 38rpx;
        margin-left: 10px;
        color: #fafafa;
      }

      .input {
        width: 100%;
        font-size: 14px;
        line-height: 20px;
        text-align: left;
        padding-left: 15px;
      }
    }

    .code-row {
      position: relative;
      padding-right: 110px;

      .code-btn {
        position: absolute;
        right: 10px;
        top: 6px;
        height: 33px;
        line-height: 33px;
        border-radius: 16px;
        font-size: 12px;
        padding: 0 10px;
        background-color: rgba(43, 133, 228, 0.12);
        color: #2b85e4;
      }
    }

    .login-btn {
      margin-top: 40px;
      height: 45px;
      box-shadow: 0 4px 12px rgba(32, 214, 255, 0.3);
    }

    .tips {
      margin-top: 15px;

      .underline-text {
        text-decoration: underline;
      }
    }

    .text-center {
      text-align: center;
    }

    .dev-login-wrap {
      margin-top: 30rpx;

      .dev-divider {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;

        &::before,
        &::after {
          content: "";
          flex: 1;
          height: 1px;
          background: rgba(255, 255, 255, 0.3);
        }

        .dev-divider-text {
          padding: 0 20rpx;
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.7);
        }
      }
    }
  }

  .bottom-footer {
    position: absolute;
    bottom: 40rpx;
    width: 100%;
    text-align: center;

    .admin-link {
      color: #ffffff;
      font-size: 14px;
      text-decoration: underline;
      margin-bottom: 20rpx;
      opacity: 0.9;
    }

    .copyright {
      color: #ffffff;
      font-size: 12px;
      line-height: 1.6;
      opacity: 0.6;
    }
  }
}
</style>