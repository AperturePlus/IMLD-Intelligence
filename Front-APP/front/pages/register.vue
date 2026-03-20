<template>
  <view class="normal-login-container">
    <view class="logo-content align-center justify-center flex">
      
      <text class="title">注册</text>
    </view>
    <view class="login-form-content">
      <view class="input-item flex align-center">
        <view class="iconfont icon-user icon"></view>
        <input v-model="registerForm.username" class="input" type="text" placeholder="请输入账号" maxlength="30" />
      </view>
      <view class="input-item flex align-center">
        <view class="iconfont icon-password icon"></view>
        <input v-model="registerForm.password" type="password" class="input" placeholder="请输入密码" maxlength="20" />
      </view>
      <view class="input-item flex align-center">
        <view class="iconfont icon-password icon"></view>
        <input v-model="confirmPassword" type="password" class="input" placeholder="请输入重复密码" maxlength="20" />
      </view>
      <view class="input-item flex align-center" style="width: 60%;margin: 0px;" v-if="captchaEnabled">
        <view class="iconfont icon-code icon"></view>
        <input v-model="registerForm.code" type="number" class="input" placeholder="请输入验证码" maxlength="4" />
        <view class="login-code"> 
          <image :src="codeUrl" @click="getCode" class="login-code-img"></image>
        </view>
      </view>
      <view class="action-btn">
        <button @click="handleRegister()" class="register-btn cu-btn block bg-blue lg round">注册</button>
      </view>
    </view>
    <view class="xieyi text-center">
      <text @click="handleUserLogin" class="text-blue">使用已有账号登录</text>
    </view>
  </view>
</template>


<script>
  import { getCodeImg,register } from '@/api/system/user'
  export default {
    data() {
      return {
        codeUrl: "",
        captchaEnabled: true,
		confirmPassword: "",
		uuid: '',
        globalConfig: getApp().globalData.config,
        registerForm: {
          username: "",
          password: "",
          code: "" ,
		  role: "0"
        }
      }
    },
    created() {
      this.getCode()
    },
    methods: {
      // 用户登录
      handleUserLogin() {
        this.$tab.navigateTo(`/pages/login`)
      },
      // 获取图形验证码
      getCode() {
      	getCodeImg().then(res => {
			//promise对象的写法，如果getCodeImg()成功执行，则执行then（）括号中的内容
			//res=>{}是函数的简写方法，没有函数名，只有一个res的参数，函数体的实现在{}中
      		this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
      		if (this.captchaEnabled) {
      			this.codeUrl = 'data:image/gif;base64,' + res.data
      			this.uuid = res.uuid
      		}
      	})
      },
      // 注册方法
      async handleRegister() {
        if (this.registerForm.username === "") {
          this.$modal.msgError("请输入您的账号")
        } else if (this.registerForm.password === "") {
          this.$modal.msgError("请输入您的密码")
        } else if (this.confirmPassword === "") {
          this.$modal.msgError("请再次输入您的密码")
        } else if (this.registerForm.password !== this.confirmPassword) {
          this.$modal.msgError("两次输入的密码不一致")
        } else if (this.registerForm.code === "" && this.captchaEnabled) {
          this.$modal.msgError("请输入验证码")
        } else {
          this.$modal.loading("注册中，请耐心等待...")
          this.registerUser()
        }
      },
      // 调用注册接口
      async registerUser() {
        try {
		  console.log("Register Form Data:", this.registerForm); // 打印注册表单数据
          const response = await register(this.registerForm);
		  console.log("Register Response:", response);
          this.$modal.closeLoading();
          this.$modal.msgSuccess("注册成功");
          // 注册成功后跳转到登录页面
           uni.redirectTo({
                url: '/pages/login' // 跳转到登录页面
              });
        } catch (error) {
			console.error(error); // 输出错误信息
          this.$modal.closeLoading();
          this.$modal.msgError("注册失败，请重试");
        }
      }
    }
  }
</script>

<style lang="scss">
  page {
    background-image: linear-gradient(to top, #f3cbee 0%, #ace0f9 100%);
  }

  .normal-login-container {
    width: 100%;

    .logo-content {
      width: 100%;
      font-size: 21px;
      text-align: center;
      padding-top: 15%;

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

      .input-item {
        margin: 20px auto;
        background-color: #f5f6f7;
        height: 45px;
        border-radius: 20px;

        .icon {
          font-size: 38rpx;
          margin-left: 10px;
          color: #999;
        }

        .input {
          width: 100%;
          font-size: 14px;
          line-height: 20px;
          text-align: left;
          padding-left: 15px;
        }

      }

      .register-btn {
        margin-top: 40px;
        height: 45px;
      }

      .xieyi {
        color: #333;
        margin-top: 20px;
      }
      
      .login-code {
        height: 38px;
        float: right;
      
        .login-code-img {
          height: 38px;
          position: absolute;
          margin-left: 10px;
          width: 200rpx;
        }
      }
    }
  }

</style>
