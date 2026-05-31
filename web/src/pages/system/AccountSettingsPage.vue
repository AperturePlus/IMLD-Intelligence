<template>
  <div class="account-settings-page">
    <div class="page-heading">
      <div>
        <h1>账户设置</h1>
        <p>{{ profileSummary }}</p>
      </div>
      <el-button :icon="Refresh" :loading="isLoading" @click="loadProfile">刷新</el-button>
    </div>

    <el-alert
      v-if="loadErrorMessage"
      :title="loadErrorMessage"
      type="warning"
      :closable="false"
      class="status-alert"
    />

    <div class="settings-layout">
      <section class="settings-panel">
        <div class="panel-header">
          <div>
            <h2>基础资料</h2>
            <span>{{ profileForm.username || '-' }}</span>
          </div>
          <el-tag type="info">{{ roleLabel }}</el-tag>
        </div>

        <el-form
          class="settings-form"
          :model="profileForm"
          label-width="96px"
          @submit.prevent
        >
          <div class="readonly-grid">
            <div class="readonly-item">
              <span>账号</span>
              <strong>{{ profileForm.username || '-' }}</strong>
            </div>
            <div class="readonly-item">
              <span>工号</span>
              <strong>{{ profileForm.userNo || '-' }}</strong>
            </div>
            <div class="readonly-item">
              <span>用户类型</span>
              <strong>{{ userTypeLabel }}</strong>
            </div>
            <div class="readonly-item">
              <span>最近登录</span>
              <strong>{{ formatDateTime(profileForm.lastLoginAt) }}</strong>
            </div>
          </div>

          <el-form-item label="姓名" required>
            <el-input
              v-model.trim="profileForm.displayName"
              maxlength="100"
              show-word-limit
              placeholder="请输入姓名"
            />
          </el-form-item>

          <el-form-item label="科室">
            <el-input
              v-model.trim="profileForm.deptName"
              maxlength="100"
              show-word-limit
              placeholder="请输入科室"
            />
          </el-form-item>

          <el-form-item label="邮箱" required>
            <el-input
              v-model.trim="profileForm.email"
              maxlength="128"
              placeholder="请输入邮箱"
            />
          </el-form-item>

          <el-form-item label="手机号">
            <div class="mobile-field">
              <el-input
                v-model.trim="profileForm.mobilePlaintext"
                maxlength="32"
                placeholder="输入新手机号"
              />
              <el-text type="info">当前：{{ profileForm.mobileMasked || '未绑定' }}</el-text>
            </div>
          </el-form-item>

          <el-form-item v-if="isContactChangePending" label="当前密码" required>
            <el-input
              v-model="profileForm.currentPassword"
              type="password"
              maxlength="255"
              show-password
              placeholder="请输入当前密码"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button
              type="primary"
              :icon="Check"
              :loading="isSavingProfile"
              @click="handleSaveProfile"
            >
              保存资料
            </el-button>
          </div>
        </el-form>
      </section>

      <section class="settings-panel security-panel">
        <div class="panel-header">
          <div>
            <h2>安全设置</h2>
            <span>{{ profileForm.username || '-' }}</span>
          </div>
          <el-tag type="warning">密码</el-tag>
        </div>

        <el-form class="settings-form" :model="passwordForm" label-width="96px" @submit.prevent>
          <el-form-item label="当前密码" required>
            <el-input
              v-model="passwordForm.currentPassword"
              type="password"
              maxlength="255"
              show-password
              placeholder="请输入当前密码"
            />
          </el-form-item>

          <el-form-item label="新密码" required>
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              maxlength="255"
              show-password
              placeholder="请输入新密码"
            />
          </el-form-item>

          <el-form-item label="确认密码" required>
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              maxlength="255"
              show-password
              placeholder="请再次输入新密码"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button
              type="danger"
              :icon="Lock"
              :loading="isChangingPassword"
              @click="handleChangePassword"
            >
              修改密码
            </el-button>
          </div>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AxiosError } from 'axios'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Lock, Refresh } from '@element-plus/icons-vue'
import accountApi from '@/api/account'
import type { AccountProfileResponse, UpdateAccountProfileRequest } from '@/types/account'
import type { ApiEnvelope } from '@/types/common'

type ProfileFormModel = {
  userId: number | null
  tenantId: number | null
  userNo: string
  username: string
  displayName: string
  userType: string
  deptName: string
  email: string
  mobileMasked: string
  mobilePlaintext: string
  currentPassword: string
  roleCodes: string[]
  lastLoginAt: string | null
}

type PasswordFormModel = {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

type ErrorPayload = ApiEnvelope<unknown> & {
  detail?: string
  [key: string]: unknown
}

const router = useRouter()
const isLoading = ref(false)
const isSavingProfile = ref(false)
const isChangingPassword = ref(false)
const loadErrorMessage = ref('')
const originalProfile = ref<AccountProfileResponse | null>(null)

const profileForm = reactive<ProfileFormModel>({
  userId: null,
  tenantId: null,
  userNo: '',
  username: '',
  displayName: '',
  userType: '',
  deptName: '',
  email: '',
  mobileMasked: '',
  mobilePlaintext: '',
  currentPassword: '',
  roleCodes: [],
  lastLoginAt: null
})

const passwordForm = reactive<PasswordFormModel>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleLabelMap: Record<string, string> = {
  SYSTEM_ADMIN: '系统管理员',
  COMPLIANCE_AUDITOR: '合规审计员',
  DOCTOR: '医生',
  NURSE: '护士',
  ADMIN: '管理员',
  PATIENT: '患者'
}

const userTypeLabel = computed(() => {
  const normalized = profileForm.userType.trim().toUpperCase()
  return roleLabelMap[normalized] || normalized || '-'
})

const roleLabel = computed(() => {
  const role = profileForm.roleCodes.find((item) => roleLabelMap[item])
  return role ? roleLabelMap[role] : userTypeLabel.value
})

const profileSummary = computed(() => {
  const dept = profileForm.deptName || '未配置科室'
  return `${profileForm.displayName || profileForm.username || '当前账号'} · ${dept}`
})

const isContactChangePending = computed(() => {
  const original = originalProfile.value
  if (!original) {
    return false
  }

  const emailChanged = profileForm.email.trim().toLowerCase() !== (original.email || '').trim().toLowerCase()
  const mobileChanged = profileForm.mobilePlaintext.trim().length > 0
  return emailChanged || mobileChanged
})

const unwrapEnvelope = <T>(envelope: ApiEnvelope<T> | undefined, fallback: string): T => {
  if (!envelope || envelope.code !== 200 || envelope.data === undefined || envelope.data === null) {
    throw new Error(envelope?.message || fallback)
  }
  return envelope.data
}

const assertEnvelopeSuccess = (envelope: ApiEnvelope<unknown> | undefined, fallback: string): void => {
  if (!envelope || envelope.code !== 200) {
    throw new Error(envelope?.message || fallback)
  }
}

const extractErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof Error && error.message && !('response' in error)) {
    return error.message
  }

  const axiosError = error as AxiosError<ErrorPayload>
  const payload = axiosError.response?.data
  if (payload?.message && typeof payload.message === 'string') {
    return payload.message
  }
  if (payload?.detail && typeof payload.detail === 'string') {
    return payload.detail
  }
  if (axiosError.message) {
    return axiosError.message
  }
  return fallback
}

const applyProfile = (profile: AccountProfileResponse): void => {
  originalProfile.value = profile
  profileForm.userId = profile.userId
  profileForm.tenantId = profile.tenantId
  profileForm.userNo = profile.userNo || ''
  profileForm.username = profile.username || ''
  profileForm.displayName = profile.displayName || ''
  profileForm.userType = profile.userType || ''
  profileForm.deptName = profile.deptName || ''
  profileForm.email = profile.email || ''
  profileForm.mobileMasked = profile.mobileMasked || ''
  profileForm.mobilePlaintext = ''
  profileForm.currentPassword = ''
  profileForm.roleCodes = profile.roleCodes || []
  profileForm.lastLoginAt = profile.lastLoginAt

  localStorage.setItem('username', profile.username || '')
  localStorage.setItem('userDisplayName', profile.displayName || profile.username || '')
  localStorage.setItem('userType', profile.userType || '')
  localStorage.setItem('roleCodes', JSON.stringify(profile.roleCodes || []))
  localStorage.setItem('userId', String(profile.userId))
  localStorage.setItem('tenantId', String(profile.tenantId))
  window.dispatchEvent(new Event('imld:user-profile-updated'))
}

const formatDateTime = (value: string | null): string => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}

const validateProfileForm = (): boolean => {
  if (!profileForm.displayName.trim()) {
    ElMessage.warning('请输入姓名')
    return false
  }
  if (!profileForm.email.trim()) {
    ElMessage.warning('请输入邮箱')
    return false
  }
  if (!profileForm.email.includes('@')) {
    ElMessage.warning('邮箱格式不正确')
    return false
  }
  if (isContactChangePending.value && !profileForm.currentPassword) {
    ElMessage.warning('请输入当前密码')
    return false
  }
  return true
}

const validatePasswordForm = (): boolean => {
  if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return false
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 个字符')
    return false
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次新密码不一致')
    return false
  }
  return true
}

const clearSessionStorage = (): void => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('username')
  localStorage.removeItem('userDisplayName')
  localStorage.removeItem('userType')
  localStorage.removeItem('roleCodes')
  localStorage.removeItem('userId')
  localStorage.removeItem('tenantId')
  localStorage.removeItem('userAvatar')
  window.dispatchEvent(new Event('imld:user-profile-updated'))
}

const loadProfile = async (): Promise<void> => {
  isLoading.value = true
  loadErrorMessage.value = ''
  try {
    const response = await accountApi.getCurrentProfile()
    applyProfile(unwrapEnvelope(response.data, '加载账户资料失败'))
  } catch (error) {
    loadErrorMessage.value = extractErrorMessage(error, '加载账户资料失败')
    ElMessage.error(loadErrorMessage.value)
  } finally {
    isLoading.value = false
  }
}

const handleSaveProfile = async (): Promise<void> => {
  if (!validateProfileForm()) {
    return
  }

  const payload: UpdateAccountProfileRequest = {
    displayName: profileForm.displayName.trim(),
    deptName: profileForm.deptName.trim(),
    email: profileForm.email.trim()
  }

  if (profileForm.mobilePlaintext.trim()) {
    payload.mobilePlaintext = profileForm.mobilePlaintext.trim()
  }
  if (isContactChangePending.value) {
    payload.currentPassword = profileForm.currentPassword
  }

  isSavingProfile.value = true
  try {
    const response = await accountApi.updateCurrentProfile(payload)
    applyProfile(unwrapEnvelope(response.data, '保存账户资料失败'))
    ElMessage.success('账户资料已保存')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '保存账户资料失败'))
  } finally {
    isSavingProfile.value = false
  }
}

const handleChangePassword = async (): Promise<void> => {
  if (!validatePasswordForm()) {
    return
  }

  const refreshToken = localStorage.getItem('refreshToken') || ''
  if (!refreshToken) {
    ElMessage.error('登录状态缺少刷新令牌')
    return
  }

  isChangingPassword.value = true
  try {
    const response = await accountApi.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
      refreshToken
    })
    assertEnvelopeSuccess(response.data, '修改密码失败')
    ElMessage.success('密码已修改，请重新登录')
    clearSessionStorage()
    setTimeout(() => {
      router.replace('/')
    }, 500)
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '修改密码失败'))
  } finally {
    isChangingPassword.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.account-settings-page {
  min-height: 100%;
  padding: 24px;
  background: #f5f7fb;
  box-sizing: border-box;
}

.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-heading h1 {
  margin: 0;
  font-size: 22px;
  line-height: 30px;
  color: #1f2a37;
}

.page-heading p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.status-alert {
  margin-bottom: 16px;
}

.settings-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 16px;
  align-items: start;
}

.settings-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.04);
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.panel-header h2 {
  margin: 0;
  font-size: 16px;
  line-height: 24px;
  color: #111827;
}

.panel-header span {
  display: block;
  margin-top: 3px;
  font-size: 12px;
  color: #6b7280;
}

.settings-form {
  max-width: 720px;
}

.readonly-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}

.readonly-item {
  min-height: 58px;
  padding: 10px 12px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #f9fafb;
  box-sizing: border-box;
}

.readonly-item span {
  display: block;
  margin-bottom: 5px;
  color: #6b7280;
  font-size: 12px;
}

.readonly-item strong {
  display: block;
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.mobile-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.security-panel {
  position: sticky;
  top: 20px;
}

@media (max-width: 1100px) {
  .settings-layout {
    grid-template-columns: 1fr;
  }

  .security-panel {
    position: static;
  }
}

@media (max-width: 640px) {
  .account-settings-page {
    padding: 16px;
  }

  .page-heading {
    align-items: stretch;
    flex-direction: column;
  }

  .readonly-grid {
    grid-template-columns: 1fr;
  }
}
</style>
