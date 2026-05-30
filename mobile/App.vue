<script>
import config from "./config";
import store from "@/store";
import { devLogin } from "@/api/devAuth";
import {
  getToken,
  setToken,
  setRefreshToken,
  setTenantId,
  setTocUserId,
  setTocNickname,
} from "@/utils/auth";
import Vue from "vue";
//onload 用于页面或资源加载完成时，而 onlaunch 通常用于应用程序或小程序的初始化阶段。
export default {
  onLaunch: function () {
    uni.getSystemInfo({
      success: function (e) {
        // #ifndef MP
        Vue.prototype.StatusBar = e.statusBarHeight;
        if (e.platform == "android") {
          Vue.prototype.CustomBar = e.statusBarHeight + 50;
        } else {
          Vue.prototype.CustomBar = e.statusBarHeight + 45;
        }
        // #endif
        //如果是微信小程序平台，设置状态栏高度和自定义导航栏高度。
        // #ifdef MP-WEIXIN
        Vue.prototype.StatusBar = e.statusBarHeight;
        let custom = wx.getMenuButtonBoundingClientRect();
        Vue.prototype.Custom = custom;
        Vue.prototype.CustomBar =
          custom.bottom + custom.top - e.statusBarHeight;
        // #endif
        // #ifdef MP-ALIPAY
        Vue.prototype.StatusBar = e.statusBarHeight;
        Vue.prototype.CustomBar = e.statusBarHeight + e.titleBarHeight;
        // #endif
      },
    });
    this.initApp();
  },
  methods: {
    // 初始化应用
    initApp() {
      // 初始化应用配置
      this.initConfig();
      // 检查用户登录状态
      //#ifdef H5
      this.checkLogin();
      //#endif
    },
    initConfig() {
      this.globalData.config = config;
      //globalData 是 UniApp 提供的一个全局数据对象，它不需要在页面上显式声明
    },
    checkLogin() {
      if (getToken()) {
        return;
      }
      if (config.deploymentMode === "dev") {
        devLogin()
          .then((res) => {
            const data = (res && res.data) || {};
            if (data.accessToken && data.tenantId && data.tocUserId) {
              setToken(data.accessToken);
              setRefreshToken(data.refreshToken || "");
              setTenantId(data.tenantId);
              setTocUserId(data.tocUserId);
              setTocNickname(data.nickname || "");
              return;
            }
            this.$tab.reLaunch("/pages/login");
          })
          .catch(() => {
            this.$tab.reLaunch("/pages/login");
          });
        return;
      }
      if (config.deploymentMode === "mock") {
        setToken("mock_token_xxx");
        setRefreshToken("mock_refresh_token");
        setTenantId(1);
        setTocUserId(999);
        setTocNickname("Mock用户");
        return;
      }
      this.$tab.reLaunch("/pages/login");
    },
  },
};
</script>

<style lang="scss">
@import "colorui/main.css";
@import "colorui/icon.css";
@import "@/static/scss/index.scss";
@import "@/static/scss/global.scss";
@import "@/static/scss/animations.scss";
</style>
