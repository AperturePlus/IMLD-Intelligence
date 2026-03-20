<template>
  <view class="forget-password-container">
    <view class="logo-content align-center justify-center flex">
      <text class="title">找回密码</text>
    </view>

    <view class="back-button" @click="goBack">
      <text class="back-icon">| </text>
      <text class="back-text">返回</text>
    </view>

    <view class="form-content">
      <view class="input-item flex align-center">
        <view class="iconfont icon-user icon"></view>
        <input v-model="forgetForm.phone" class="input" type="text" placeholder="请输入手机号码" maxlength="11" />
      </view>

      <!-- 验证码输入和发送按钮同一行 -->
      <view class="input-item flex align-center code-row">
        <view class="iconfont icon-code icon"></view>
        <input v-model="forgetForm.code" type="number" class="input code-input" placeholder="请输入验证码" maxlength="4" />
        <button @click="sendCode" class="code-btn" color="white">发送验证码</button>
      </view>

      <view class="action-btn">
        <button @click="handleForgetPassword" class="login-btn cu-btn block bg-blue lg round">找回密码</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      forgetForm: {
        phone: '',
        code: ''
      }
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({
        delta: 1
      });
    },
    sendCode() {
      if (this.forgetForm.phone === '') {
        this.$modal.msgError('请输入手机号码')
        return
      }
      this.$modal.loading('发送验证码中...')
      setTimeout(() => {
        this.$modal.msgSuccess('验证码已发送')
        this.$modal.closeLoading()
      }, 1000)
    },
    handleForgetPassword() {
      if (this.forgetForm.phone === '') {
        this.$modal.msgError('请输入手机号码')
        return
      }
      if (this.forgetForm.code === '') {
        this.$modal.msgError('请输入验证码')
        return
      }
      this.$tab.navigateTo('/pages/reset_password')
    }
  }
}
</script>

<style lang="scss">
page {
  background-image: linear-gradient(to top, #f3cbee 0%, #ace0f9 100%);
}

.forget-password-container {
  width: 100%;

  .back-button {
    display: flex;
    align-items: center;
    padding-left: 15px;
    margin-top: 10px;

    .back-icon {
      font-size: 24px;
      margin-right: 6rpx;
      color: #333;
    }

    .back-text {
      font-size: 16px;
      color: #333;
    }
  }

  .logo-content {
    width: 100%;
    font-size: 21px;
    text-align: center;
    padding-top: 15%;
  }

  .form-content {
    text-align: center;
    margin: 20px auto;
    margin-top: 15%;
    width: 80%;

    .input-item {
      margin: 20px auto;
      background-color: #f5f6f7;
      height: 45px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      position: relative;

      .icon {
        font-size: 38rpx;
        margin-left: 10px;
        color: #999;
      }

      .input {
        font-size: 14px;
        line-height: 20px;
        text-align: left;
        padding-left: 15px;
        flex: 1;
      }
    }

    .code-row {
      .code-input {
        width: 65%;
      }

      .code-btn {
        margin-right: 10px;
        font-size: 12px;
		font: bold;
		color: #f5f6f7;
        background-color: #1882f4;
        border: none;
        padding: 5px 10px;
        border-radius: 15px;
        height: 30px;
        line-height: 30px;
      }
    }

    .login-btn {
      margin-top: 40px;
      height: 45px;
    }
  }
}
</style>
