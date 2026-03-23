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
import { reactive, ref } from 'vue'
import userApi from '../api/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isLoading = ref(false)
const isRightPanelActive = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password1: '',
  password2: ''
})

const loginForm = reactive({
  username: '',
  password: ''
})

const togglePanel = (isRightActive: boolean) => {
  isRightPanelActive.value = isRightActive
}

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password1) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (registerForm.password1 !== registerForm.password2) {
    ElMessage.error('两次密码不一致')
    return
  }

  isLoading.value = true

  try {
    await userApi.register({
      username: registerForm.username,
      email: registerForm.email,
      password1: registerForm.password1,
      password2: registerForm.password2
    })

    ElMessage.success('注册成功，请登录')
    registerForm.username = ''
    registerForm.email = ''
    registerForm.password1 = ''
    registerForm.password2 = ''
    togglePanel(false)
  } catch (error: any) {
    let errorMsg = '注册失败，请检查网络或账号信息'

    if (error.response && error.response.data) {
      const data = error.response.data
      const messages: string[] = []

      Object.keys(data).forEach((key) => {
        const value = data[key]
        if (Array.isArray(value)) {
          messages.push(...value)
        } else if (typeof value === 'string') {
          messages.push(value)
        }
      })

      if (messages.length > 0) {
        errorMsg = messages.join('<br>')
      }
    }

    ElMessage.error({
      message: errorMsg,
      dangerouslyUseHTMLString: true,
      duration: 5000
    })
  } finally {
    isLoading.value = false
  }
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  isLoading.value = true

  try {
    const res = await userApi.login({
      username: loginForm.username,
      password: loginForm.password
    })

    const token =
      res?.data?.key ||
      res?.data?.token ||
      res?.data?.access ||
      res?.data?.access_token

    if (token) {
      localStorage.setItem('token', token)
      localStorage.setItem('username', loginForm.username)
      ElMessage.success('登录成功')

      setTimeout(() => {
        router.replace('/center')
      }, 400)
    } else {
      ElMessage.error('登录失败：未获取到 Token')
    }
  } catch (error: any) {
    const errorMsg = error.response?.data?.non_field_errors?.[0] || '登录失败，请检查账号密码'
    ElMessage.error(errorMsg)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 26px 20px 78px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(9, 40, 52, 0.88), rgba(7, 63, 78, 0.86)),
    url('../assets/login.webp') center / cover no-repeat;
}

.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  filter: blur(2px);
  pointer-events: none;
}

.login-container::before {
  width: 260px;
  height: 260px;
  top: 8%;
  left: -68px;
  background: rgba(52, 211, 184, 0.2);
  animation: drift 8s ease-in-out infinite;
}

.login-container::after {
  width: 320px;
  height: 320px;
  right: -98px;
  bottom: 10%;
  background: rgba(56, 189, 248, 0.22);
  animation: drift 9s ease-in-out infinite reverse;
}

.page-title {
  z-index: 2;
  text-align: center;
  color: #f8fcff;
  margin-bottom: 24px;
}

.page-title h1 {
  margin: 0;
  font-size: clamp(2.2rem, 5.4vw, 3.6rem);
  font-weight: 700;
  letter-spacing: 0.06em;
}

.page-title h2 {
  margin: 12px 0 0;
  font-size: clamp(1.15rem, 2.4vw, 1.6rem);
  font-weight: 600;
}

.page-title p {
  margin: 12px 0 0;
  font-size: 0.96rem;
  color: rgba(240, 248, 255, 0.92);
  letter-spacing: 0.04em;
}

.mobile-switch {
  display: none;
}

.container {
  z-index: 2;
  position: relative;
  overflow: hidden;
  width: min(960px, 100%);
  min-height: 520px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.36);
  box-shadow:
    0 26px 58px rgba(10, 33, 46, 0.38),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(10px);
}

.container__form {
  position: absolute;
  top: 0;
  height: 100%;
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.65s cubic-bezier(0.2, 0.6, 0.2, 1), opacity 0.45s ease;
}

.container--signin {
  left: 0;
  z-index: 2;
  opacity: 1;
  pointer-events: auto;
}

.container--signup {
  left: 0;
  z-index: 1;
  opacity: 0;
  pointer-events: none;
}

.container.right-panel-active .container--signin {
  transform: translateX(100%);
  opacity: 0;
  pointer-events: none;
}

.container.right-panel-active .container--signup {
  transform: translateX(100%);
  opacity: 1;
  z-index: 3;
  pointer-events: auto;
}

.container__overlay {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  overflow: hidden;
  z-index: 9;
  transition: transform 0.65s cubic-bezier(0.2, 0.6, 0.2, 1);
}

.container.right-panel-active .container__overlay {
  transform: translateX(-100%);
}

.overlay {
  position: relative;
  left: -100%;
  width: 200%;
  height: 100%;
  color: #f8fbff;
  background:
    linear-gradient(145deg, rgba(15, 109, 141, 0.9), rgba(34, 163, 159, 0.88)),
    url('../assets/login.webp') center / cover no-repeat;
  transform: translateX(0);
  transition: transform 0.65s cubic-bezier(0.2, 0.6, 0.2, 1);
}

.overlay::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 28% 28%, rgba(255, 255, 255, 0.22), transparent 40%),
    radial-gradient(circle at 78% 72%, rgba(13, 25, 44, 0.24), transparent 45%);
}

.container.right-panel-active .overlay {
  transform: translateX(50%);
}

.overlay__panel {
  position: absolute;
  top: 0;
  width: 50%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 46px;
  box-sizing: border-box;
  z-index: 2;
}

.overlay--left {
  transform: translateX(-20%);
}

.container.right-panel-active .overlay--left {
  transform: translateX(0);
}

.overlay--right {
  right: 0;
  transform: translateX(0);
}

.container.right-panel-active .overlay--right {
  transform: translateX(20%);
}

.form {
  width: 100%;
  height: 100%;
  padding: 0 54px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: left;
}

.form__title {
  align-self: flex-start;
  margin: 0 0 20px;
  font-size: 1.65rem;
  font-weight: 700;
  color: #123041;
  letter-spacing: 0.05em;
}

.btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 12px;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  background: linear-gradient(110deg, #0f6d8d 0%, #22a39f 96%);
  box-shadow: 0 12px 24px rgba(15, 109, 141, 0.26);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 24px rgba(15, 109, 141, 0.32);
}

.btn.ghost {
  width: auto;
  height: 48px;
  padding: 0 30px;
  border: 1px solid rgba(255, 255, 255, 0.84);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  box-shadow: none;
}

.btn.ghost:hover {
  background: rgba(255, 255, 255, 0.22);
}

.link {
  display: block;
  width: 100%;
  text-align: right;
  margin: 6px 0;
  color: #4c667a;
  font-size: 0.85rem;
}

.page-footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 18px;
  z-index: 2;
  text-align: center;
  color: rgba(237, 247, 255, 0.88);
  font-size: 0.8rem;
  letter-spacing: 0.03em;
}

:deep(.el-form-item) {
  width: 100%;
  margin-bottom: 18px !important;
}

:deep(.el-form-item.no-gap-item) {
  margin-bottom: 6px !important;
}

:deep(.el-form-item.btn-item) {
  margin-top: 6px;
}

:deep(.el-form-item.btn-item .el-form-item__content) {
  margin-left: 0 !important;
}

:deep(.el-form-item__label) {
  color: #395366;
  font-weight: 600;
}

:deep(.el-form-item__content) {
  margin-left: 0 !important;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: none;
  background: #eef5f8;
  border: 1px solid transparent;
  padding: 0 12px;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #22a39f;
  background: #ffffff;
}

@keyframes drift {
  0%,
  100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-18px) scale(1.04);
  }
}

@media (max-width: 980px) {
  .login-container {
    justify-content: flex-start;
    padding-top: 30px;
  }

  .page-title {
    margin-bottom: 18px;
  }

  .container {
    min-height: 600px;
    border-radius: 20px;
  }

  .container__overlay {
    display: none;
  }

  .container__form {
    width: 100%;
  }

  .container--signup {
    opacity: 0;
    pointer-events: none;
    transform: translateX(0);
  }

  .container.right-panel-active .container--signin {
    transform: translateX(100%);
    opacity: 0;
    pointer-events: none;
  }

  .container.right-panel-active .container--signup {
    transform: translateX(0);
    opacity: 1;
    pointer-events: auto;
  }

  .form {
    padding: 0 26px;
  }

  .mobile-switch {
    z-index: 2;
    display: inline-flex;
    border-radius: 999px;
    padding: 4px;
    margin-bottom: 16px;
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.35);
    backdrop-filter: blur(8px);
  }

  .mobile-switch button {
    border: none;
    color: #eaf6ff;
    font-size: 0.92rem;
    background: transparent;
    border-radius: 999px;
    width: 96px;
    height: 36px;
    transition: all 0.2s ease;
  }

  .mobile-switch button.active {
    background: #ffffff;
    color: #0f4e68;
    font-weight: 600;
  }

  .page-footer {
    position: static;
    margin-top: 16px;
  }
}
</style>
