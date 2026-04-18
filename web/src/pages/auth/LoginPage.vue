<template>
  <div class="login-container">
    <FloatingParticles />
    <Liver3DModel />
    <div class="page-title">
      <h1>{{ BRANDING.shortName }}</h1>
      <h2>{{ BRANDING.systemName }}</h2>
    </div>

    <div class="mobile-switch">
      <button :class="{ active: !isRightPanelActive }" @click="togglePanel(false)">
        登录
      </button>
      <button :class="{ active: isRightPanelActive }" @click="togglePanel(true)">
        注册
      </button>
    </div>

    <div class="container" :class="{ 'right-panel-active': isRightPanelActive }" id="container">
      <div class="container__form container--signup">
        <el-form class="form" :model="registerForm" label-width="84px" label-position="left">
          <h2 class="form__title">注册账号</h2>

          <el-form-item prop="username" label="账号">
            <el-input v-model="registerForm.username" placeholder="请输入账号" clearable />
          </el-form-item>

          <el-form-item prop="email" label="邮箱">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>

          <el-form-item prop="emailCode" label="验证码">
            <div class="code-row">
              <el-input v-model="registerForm.emailCode" placeholder="请输入邮箱验证码" clearable />
              <el-button
                class="code-button"
                :disabled="isRegisterCodeSending || registerCodeCountdown > 0"
                @click="handleSendRegisterCode"
              >
                {{ registerCodeButtonText }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="password1" label="密码">
            <el-input v-model="registerForm.password1" placeholder="请输入密码" show-password />
          </el-form-item>

          <el-form-item prop="password2" label="确认密码">
            <el-input v-model="registerForm.password2" placeholder="请再次输入密码" show-password />
          </el-form-item>

          <el-form-item label-width="0" class="btn-item">
            <el-button type="primary" class="btn" :loading="isLoading" @click="handleRegister">
              注册
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="container__form container--signin">
        <el-form class="form" :model="loginForm" label-width="64px" label-position="left">
          <h2 class="form__title">账号登录</h2>

          <el-form-item prop="username" label="账号">
            <el-input v-model="loginForm.username" placeholder="请输入账号" clearable />
          </el-form-item>

          <el-form-item prop="password" label="密码">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item label-width="0" class="no-gap-item">
            <a href="#" class="link" @click.prevent="openResetPasswordDialog">忘记密码?</a>
          </el-form-item>

          <el-form-item label-width="0" class="btn-item">
            <el-button type="primary" class="btn" :loading="isLoading" @click="handleLogin">
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="container__overlay">
        <div class="overlay">
          <div class="overlay__panel overlay--left">
            <el-button class="btn ghost" @click="togglePanel(false)">已有账号? 去登录</el-button>
          </div>
          <div class="overlay__panel overlay--right">
            <el-button class="btn ghost" @click="togglePanel(true)">没有账号? 去注册</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="isResetDialogVisible"
      title="重置密码"
      width="min(520px, 92vw)"
      class="reset-password-dialog"
      destroy-on-close
    >
      <el-form :model="resetPasswordForm" label-width="86px" label-position="left">
        <el-form-item label="账号">
          <el-input v-model="resetPasswordForm.username" placeholder="请输入账号" clearable />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="resetPasswordForm.email" placeholder="请输入注册邮箱" clearable />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="resetPasswordForm.emailCode" placeholder="请输入邮箱验证码" clearable />
            <el-button
              class="code-button"
              :disabled="isResetCodeSending || resetCodeCountdown > 0"
              @click="handleSendResetCode"
            >
              {{ resetCodeButtonText }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码">
          <el-input v-model="resetPasswordForm.newPassword" placeholder="请输入新密码" show-password />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="resetPasswordForm.confirmPassword" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="reset-dialog-footer">
          <el-button @click="isResetDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="isLoading" @click="handleResetPassword">确认重置</el-button>
        </div>
      </template>
    </el-dialog>

    <div class="page-footer">
      © 四川大学华西临床医学院 2026 IMLD 项目组 All Rights Reserved.
    </div>
  </div>
</template>

<script setup lang="ts">
import FloatingParticles from '@/features/auth/components/FloatingParticles.vue'
import Liver3DModel from '@/features/auth/components/Liver3DModel.vue'
import { useAuthPage } from '@/features/auth/composables/useAuthPage'
import { BRANDING } from '@/constants/branding'

const {
  isLoading,
  isRightPanelActive,
  isResetDialogVisible,
  isRegisterCodeSending,
  isResetCodeSending,
  registerCodeCountdown,
  resetCodeCountdown,
  registerCodeButtonText,
  resetCodeButtonText,
  registerForm,
  loginForm,
  resetPasswordForm,
  togglePanel,
  openResetPasswordDialog,
  handleSendRegisterCode,
  handleRegister,
  handleLogin,
  handleSendResetCode,
  handleResetPassword
} = useAuthPage()
</script>

<style scoped src="@/features/auth/styles/login-page.css"></style>