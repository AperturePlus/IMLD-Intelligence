import { computed, onUnmounted, reactive, ref } from 'vue'
import type { AxiosError } from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import userApi from '@/api/user'
import type { LoginResponse } from '@/types/auth'
import type { ApiEnvelope } from '@/types/common'

type RegisterFormModel = {
  username: string
  email: string
  emailCode: string
  password1: string
  password2: string
}

type LoginFormModel = {
  username: string
  password: string
}

type ResetPasswordFormModel = {
  username: string
  email: string
  emailCode: string
  newPassword: string
  confirmPassword: string
}

type ErrorPayload = ApiEnvelope<unknown> & {
  detail?: string
  non_field_errors?: string[]
  [key: string]: unknown
}

const SUCCESS_CODE = 200
const DEFAULT_RESEND_SECONDS = 60

const hasText = (value: string | null | undefined): value is string =>
  typeof value === 'string' && value.trim().length > 0

type LoginUserWithAvatar = NonNullable<LoginResponse['user']> & {
  avatar?: string | null
  avatarUrl?: string | null
  doctorAvatar?: string | null
}

const resolveUserAvatar = (user: LoginResponse['user'] | null | undefined): string => {
  if (!user) {
    return ''
  }

  const withAvatar = user as LoginUserWithAvatar
  const candidate = withAvatar.avatar ?? withAvatar.avatarUrl ?? withAvatar.doctorAvatar ?? ''
  return hasText(candidate) ? candidate.trim() : ''
}

const resolveTenantCode = (): string | undefined => {
  const fromStorage = localStorage.getItem('tenantCode')
  if (hasText(fromStorage)) {
    return fromStorage.trim()
  }
  const fromEnv = String(import.meta.env.VITE_TENANT_CODE ?? '').trim()
  return hasText(fromEnv) ? fromEnv : undefined
}

const unwrapEnvelope = <T>(envelope: ApiEnvelope<T> | undefined, fallback: string): T => {
  if (!envelope || envelope.code !== SUCCESS_CODE || envelope.data === undefined || envelope.data === null) {
    throw new Error(envelope?.message || fallback)
  }
  return envelope.data
}

const assertEnvelopeSuccess = (envelope: ApiEnvelope<unknown> | undefined, fallback: string): void => {
  if (!envelope || envelope.code !== SUCCESS_CODE) {
    throw new Error(envelope?.message || fallback)
  }
}

const extractErrorMessage = (error: unknown, fallback: string): string => {
  const axiosError = error as AxiosError<ErrorPayload>
  const payload = axiosError.response?.data
  if (!payload || typeof payload !== 'object') {
    return fallback
  }

  if (hasText(payload.message)) {
    return payload.message.trim()
  }
  if (hasText(payload.detail)) {
    return payload.detail.trim()
  }
  if (Array.isArray(payload.non_field_errors) && payload.non_field_errors.length > 0) {
    const first = payload.non_field_errors.find((item): item is string => typeof item === 'string' && item.trim().length > 0)
    if (first) {
      return first
    }
  }

  const messages: string[] = []
  Object.values(payload).forEach((value) => {
    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (typeof item === 'string' && item.trim()) {
          messages.push(item.trim())
        }
      })
      return
    }
    if (typeof value === 'string' && value.trim()) {
      messages.push(value.trim())
    }
  })
  return messages.length > 0 ? messages.join('；') : fallback
}

const startCountdown = (
  seconds: number,
  countdownRef: { value: number },
  timerRef: { value: number | null }
): void => {
  if (timerRef.value !== null) {
    window.clearInterval(timerRef.value)
  }
  countdownRef.value = Math.max(1, seconds)
  timerRef.value = window.setInterval(() => {
    if (countdownRef.value <= 1) {
      countdownRef.value = 0
      if (timerRef.value !== null) {
        window.clearInterval(timerRef.value)
        timerRef.value = null
      }
      return
    }
    countdownRef.value -= 1
  }, 1000)
}

export const useAuthPage = () => {
  const router = useRouter()
  const isLoading = ref(false)
  const isRightPanelActive = ref(false)
  const isResetDialogVisible = ref(false)

  const isRegisterCodeSending = ref(false)
  const isResetCodeSending = ref(false)
  const registerCodeCountdown = ref(0)
  const resetCodeCountdown = ref(0)
  const registerCodeTimer = ref<number | null>(null)
  const resetCodeTimer = ref<number | null>(null)

  const registerForm = reactive<RegisterFormModel>({
    username: '',
    email: '',
    emailCode: '',
    password1: '',
    password2: ''
  })

  const loginForm = reactive<LoginFormModel>({
    username: '',
    password: ''
  })

  const resetPasswordForm = reactive<ResetPasswordFormModel>({
    username: '',
    email: '',
    emailCode: '',
    newPassword: '',
    confirmPassword: ''
  })

  const registerCodeButtonText = computed(() =>
    registerCodeCountdown.value > 0 ? `${registerCodeCountdown.value}s后重发` : '发送验证码'
  )
  const resetCodeButtonText = computed(() =>
    resetCodeCountdown.value > 0 ? `${resetCodeCountdown.value}s后重发` : '发送验证码'
  )

  const togglePanel = (isRightActive: boolean): void => {
    isRightPanelActive.value = isRightActive
  }

  const resetRegisterForm = (): void => {
    registerForm.username = ''
    registerForm.email = ''
    registerForm.emailCode = ''
    registerForm.password1 = ''
    registerForm.password2 = ''
  }

  const resetResetPasswordForm = (): void => {
    resetPasswordForm.username = ''
    resetPasswordForm.email = ''
    resetPasswordForm.emailCode = ''
    resetPasswordForm.newPassword = ''
    resetPasswordForm.confirmPassword = ''
  }

  const persistSession = (session: LoginResponse, fallbackUsername: string): void => {
    if (!hasText(session.accessToken)) {
      throw new Error('未获取到登录令牌')
    }
    localStorage.setItem('token', session.accessToken)
    localStorage.setItem('refreshToken', session.refreshToken ?? '')
    localStorage.setItem('username', session.user?.username || fallbackUsername)
    localStorage.setItem('userDisplayName', session.user?.displayName || session.user?.username || fallbackUsername)
    localStorage.setItem('userType', session.user?.userType || '')
    localStorage.setItem('roleCodes', JSON.stringify(session.user?.roleCodes || []))
    const avatar = resolveUserAvatar(session.user)
    if (avatar) {
      localStorage.setItem('userAvatar', avatar)
    } else {
      localStorage.removeItem('userAvatar')
    }
    if (session.user?.tenantId) {
      localStorage.setItem('tenantId', String(session.user.tenantId))
    }
    if (session.user?.userId) {
      localStorage.setItem('userId', String(session.user.userId))
    }
  }

  const handleSendRegisterCode = async (): Promise<void> => {
    if (!hasText(registerForm.username) || !hasText(registerForm.email)) {
      ElMessage.warning('请先输入注册账号和邮箱')
      return
    }
    if (registerCodeCountdown.value > 0 || isRegisterCodeSending.value) {
      return
    }

    isRegisterCodeSending.value = true
    try {
      const response = await userApi.sendRegistrationEmailCode({
        username: registerForm.username.trim(),
        email: registerForm.email.trim(),
        tenantCode: resolveTenantCode()
      })
      const payload = unwrapEnvelope(response.data, '发送注册验证码失败')
      startCountdown(
        payload.resendAfterSeconds || DEFAULT_RESEND_SECONDS,
        registerCodeCountdown,
        registerCodeTimer
      )
      ElMessage.success(`验证码已发送到 ${payload.email}`)
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '发送注册验证码失败'))
    } finally {
      isRegisterCodeSending.value = false
    }
  }

  const handleRegister = async (): Promise<void> => {
    if (!hasText(registerForm.username) || !hasText(registerForm.email) || !hasText(registerForm.emailCode)) {
      ElMessage.warning('请填写完整注册信息')
      return
    }
    if (!hasText(registerForm.password1) || !hasText(registerForm.password2)) {
      ElMessage.warning('请输入并确认密码')
      return
    }
    if (registerForm.password1 !== registerForm.password2) {
      ElMessage.error('两次密码不一致')
      return
    }

    isLoading.value = true
    try {
      const response = await userApi.register({
        username: registerForm.username.trim(),
        email: registerForm.email.trim(),
        emailCode: registerForm.emailCode.trim(),
        password: registerForm.password1,
        tenantCode: resolveTenantCode()
      })
      const session = unwrapEnvelope(response.data, '注册失败')
      persistSession(session, registerForm.username.trim())
      ElMessage.success('注册成功，已自动登录')
      resetRegisterForm()
      togglePanel(false)
      setTimeout(() => {
        router.replace('/center')
      }, 300)
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '注册失败，请检查输入信息'))
    } finally {
      isLoading.value = false
    }
  }

  const handleLogin = async (): Promise<void> => {
    if (!hasText(loginForm.username) || !hasText(loginForm.password)) {
      ElMessage.warning('请输入账号和密码')
      return
    }

    isLoading.value = true
    try {
      const response = await userApi.login({
        username: loginForm.username.trim(),
        password: loginForm.password,
        tenantCode: resolveTenantCode()
      })
      const session = unwrapEnvelope(response.data, '登录失败')
      persistSession(session, loginForm.username.trim())
      ElMessage.success('登录成功')
      setTimeout(() => {
        router.replace('/center')
      }, 300)
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '登录失败，请检查账号密码'))
    } finally {
      isLoading.value = false
    }
  }

  const openResetPasswordDialog = (): void => {
    isResetDialogVisible.value = true
    if (hasText(loginForm.username)) {
      resetPasswordForm.username = loginForm.username.trim()
    }
  }

  const handleSendResetCode = async (): Promise<void> => {
    if (!hasText(resetPasswordForm.username) || !hasText(resetPasswordForm.email)) {
      ElMessage.warning('请先输入账号和邮箱')
      return
    }
    if (resetCodeCountdown.value > 0 || isResetCodeSending.value) {
      return
    }

    isResetCodeSending.value = true
    try {
      const response = await userApi.sendPasswordResetEmailCode({
        username: resetPasswordForm.username.trim(),
        email: resetPasswordForm.email.trim(),
        tenantCode: resolveTenantCode()
      })
      const payload = unwrapEnvelope(response.data, '发送重置验证码失败')
      startCountdown(
        payload.resendAfterSeconds || DEFAULT_RESEND_SECONDS,
        resetCodeCountdown,
        resetCodeTimer
      )
      ElMessage.success(`验证码已发送到 ${payload.email}`)
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '发送重置验证码失败'))
    } finally {
      isResetCodeSending.value = false
    }
  }

  const handleResetPassword = async (): Promise<void> => {
    if (!hasText(resetPasswordForm.username) || !hasText(resetPasswordForm.email)) {
      ElMessage.warning('请输入账号和邮箱')
      return
    }
    if (!hasText(resetPasswordForm.emailCode) || !hasText(resetPasswordForm.newPassword)) {
      ElMessage.warning('请输入验证码和新密码')
      return
    }
    if (resetPasswordForm.newPassword !== resetPasswordForm.confirmPassword) {
      ElMessage.error('两次新密码不一致')
      return
    }

    isLoading.value = true
    try {
      const response = await userApi.resetPassword({
        username: resetPasswordForm.username.trim(),
        email: resetPasswordForm.email.trim(),
        emailCode: resetPasswordForm.emailCode.trim(),
        newPassword: resetPasswordForm.newPassword,
        tenantCode: resolveTenantCode()
      })
      assertEnvelopeSuccess(response.data, '重置密码失败')
      ElMessage.success('密码已重置，请使用新密码登录')
      loginForm.username = resetPasswordForm.username.trim()
      loginForm.password = ''
      isResetDialogVisible.value = false
      resetResetPasswordForm()
    } catch (error) {
      ElMessage.error(extractErrorMessage(error, '重置密码失败'))
    } finally {
      isLoading.value = false
    }
  }

  onUnmounted(() => {
    if (registerCodeTimer.value !== null) {
      window.clearInterval(registerCodeTimer.value)
      registerCodeTimer.value = null
    }
    if (resetCodeTimer.value !== null) {
      window.clearInterval(resetCodeTimer.value)
      resetCodeTimer.value = null
    }
  })

  return {
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
  }
}
