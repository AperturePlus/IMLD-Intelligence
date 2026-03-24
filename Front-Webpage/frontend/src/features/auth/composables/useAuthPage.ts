import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import userApi from '@/api/user'

type RegisterFormModel = {
  username: string
  email: string
  password1: string
  password2: string
}

type LoginFormModel = {
  username: string
  password: string
}

type ErrorLike = {
  response?: {
    data?: Record<string, unknown> & {
      non_field_errors?: string[]
    }
  }
}

const parseRegisterErrorMessage = (error: ErrorLike): string => {
  const defaultMessage = '注册失败，请检查网络或账号信息'
  const payload = error.response?.data

  if (!payload || typeof payload !== 'object') {
    return defaultMessage
  }

  const messages: string[] = []
  for (const value of Object.values(payload)) {
    if (Array.isArray(value)) {
      messages.push(...value.filter((item): item is string => typeof item === 'string'))
      continue
    }

    if (typeof value === 'string') {
      messages.push(value)
    }
  }

  return messages.length > 0 ? messages.join('<br>') : defaultMessage
}

export const useAuthPage = () => {
  const router = useRouter()
  const isLoading = ref(false)
  const isRightPanelActive = ref(false)

  const registerForm = reactive<RegisterFormModel>({
    username: '',
    email: '',
    password1: '',
    password2: ''
  })

  const loginForm = reactive<LoginFormModel>({
    username: '',
    password: ''
  })

  const togglePanel = (isRightActive: boolean): void => {
    isRightPanelActive.value = isRightActive
  }

  const resetRegisterForm = (): void => {
    registerForm.username = ''
    registerForm.email = ''
    registerForm.password1 = ''
    registerForm.password2 = ''
  }

  const handleRegister = async (): Promise<void> => {
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
      resetRegisterForm()
      togglePanel(false)
    } catch (error) {
      ElMessage.error({
        message: parseRegisterErrorMessage(error as ErrorLike),
        dangerouslyUseHTMLString: true,
        duration: 5000
      })
    } finally {
      isLoading.value = false
    }
  }

  const handleLogin = async (): Promise<void> => {
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

      if (!token) {
        ElMessage.error('登录失败：未获取到 Token')
        return
      }

      localStorage.setItem('token', token)
      localStorage.setItem('username', loginForm.username)
      ElMessage.success('登录成功')

      setTimeout(() => {
        router.replace('/center')
      }, 400)
    } catch (error) {
      const err = error as ErrorLike
      const errorMsg =
        err.response?.data?.non_field_errors?.[0] ?? '登录失败，请检查账号密码'
      ElMessage.error(errorMsg)
    } finally {
      isLoading.value = false
    }
  }

  return {
    isLoading,
    isRightPanelActive,
    registerForm,
    loginForm,
    togglePanel,
    handleRegister,
    handleLogin
  }
}
