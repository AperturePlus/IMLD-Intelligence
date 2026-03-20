<template>
	<view class="normal-login-container">
		<view class="logo-content align-center justify-center flex">
			<view class="back-button" @click="goBack">
			    <text class="back-icon">| </text>
			    <text class="back-text">返回</text>
			</view>
			<text class="title" style="margin-left: auto; margin-right: auto;">管理员登录</text>
		</view>

		<view class="login-form-content">
			<view class="input-item flex align-center">
				<view class="iconfont icon-user icon"></view>
				<input v-model="loginForm.username" class="input" type="text" placeholder="请输入账号" maxlength="30" />
			</view>
			<view class="input-item flex align-center">
				<view class="iconfont icon-password icon"></view>
				<input v-model="loginForm.password" type="password" class="input" placeholder="请输入密码" maxlength="20" />
			</view>
			<view class="input-item flex align-center" style="width: 60%;margin: 0px;" v-if="captchaEnabled">
				<view class="iconfont icon-code icon"></view>
				<input v-model="loginForm.code" type="number" class="input" placeholder="请输入验证码" maxlength="4" />
				<view class="login-code">
					<image :src="codeUrl" @click="getCode" class="login-code-img"></image>
				</view>
			</view>
			<view class="action-btn">
				<button @click="handleLogin" class="login-btn cu-btn block bg-blue lg round">登录</button>
			</view>
			
		</view>

		<!-- ✅ 新增管理员登录按钮 -->
	</view>
</template>

<script>
	import {
		getCodeImg
	} from '@/api/login'

	export default {
		data() {
			return {
				codeUrl: "",
				captchaEnabled: true,
				register: true,
				globalConfig: getApp().globalData.config,
				loginForm: {
					username:"",
					password:"",
					code: "",
					uuid: '',
					role:"1"
				}
			}
		},
		created() {
			this.getCode()
		},
		methods: {
			getCode() {
				getCodeImg().then(res => {
					this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
					if (this.captchaEnabled) {
						this.codeUrl = 'data:image/png;base64,' + res.data
						this.loginForm.uuid = res.uuid
					}
				})
			},
			async handleLogin() {
				if (this.loginForm.username === "") {
					this.$modal.msgError("请输入您的账号")
				} else if (this.loginForm.password === "") {
					this.$modal.msgError("请输入您的密码")
				} else if (this.loginForm.code === "" && this.captchaEnabled) {
					this.$modal.msgError("请输入验证码")
				} else {
					this.$modal.loading("登录中，请耐心等待...")
					this.pwdLogin()
				}
			},
			async pwdLogin() {
				this.$store.dispatch('Login', this.loginForm).then(() => {
					this.$modal.closeLoading()
					this.loginSuccess()
				}).catch(() => {
					if (this.captchaEnabled) {
						this.getCode()
					}
				})
			},
			loginSuccess() {
				this.$store.dispatch('GetInfo').then(res => {
					console.log(res)
					this.$tab.reLaunch('/pages/admin/index')
				})
			},
			
			goBack() {
			    uni.navigateBack({
			        delta: 1
			    });
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
		.back-button {
		    display: flex;
		    align-items: center;
		    cursor: pointer;
		
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

			.login-btn {
				margin-top: 40px;
				height: 45px;
			}

			.reg {
				margin-top: 15px;
			}

			.appeal {
				margin-top: 15px;
			}

			.forget {
				margin-top: 15px;
			}

			.text-center {
				text-align: center;
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

	// ✅ 管理员登录按钮样式
	.admin-login-btn {
		position: fixed;
		bottom: 30rpx;
		right: 30rpx;
		width: 150rpx;

		.cu-btn {
			height: 40px;
			font-size: 14px;
		}
	}
</style>
