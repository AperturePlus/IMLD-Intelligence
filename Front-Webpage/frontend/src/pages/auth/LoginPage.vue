<template>
  <div class="login-container">
    <div class="page-title">
      <h1>数智肝循</h1>
      <h2>遗传代谢性肝病全周期管理平台</h2>
      <p>面向基层机构与大型医院的一体化临床工作台</p>
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
        <el-form
          class="form"
          :model="registerForm"
          label-width="84px"
          label-position="left"
        >
          <h2 class="form__title">注册账号</h2>

          <el-form-item prop="username" label="账号">
            <el-input v-model="registerForm.username" placeholder="请输入账号" clearable />
          </el-form-item>

          <el-form-item prop="email" label="邮箱">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>

          <el-form-item prop="password1" label="密码">
            <el-input v-model="registerForm.password1" placeholder="请输入密码" show-password />
          </el-form-item>

          <el-form-item prop="password2" label="确认密码">
            <el-input v-model="registerForm.password2" placeholder="请再次输入密码" show-password />
          </el-form-item>

          <el-form-item label-width="0" class="btn-item">
            <el-button
              type="primary"
              class="btn"
              :loading="isLoading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="container__form container--signin">
        <el-form
          class="form"
          :model="loginForm"
          label-width="64px"
          label-position="left"
        >
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
            <a href="#" class="link" @click.prevent>忘记密码?</a>
          </el-form-item>

          <el-form-item label-width="0" class="btn-item">
            <el-button
              type="primary"
              class="btn"
              :loading="isLoading"
              @click="handleLogin"
            >
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

    <div class="page-footer">
      © 四川大学华西临床医学院 2026 IMLD 项目组 All Rights Reserved.
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthPage } from '@/features/auth/composables/useAuthPage'

const {
  isLoading,
  isRightPanelActive,
  registerForm,
  loginForm,
  togglePanel,
  handleRegister,
  handleLogin
} = useAuthPage()
</script>

<style scoped src="@/features/auth/styles/login-page.css"></style>

