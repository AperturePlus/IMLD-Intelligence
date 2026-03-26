<template>
  <div class="license-page">
    <el-card class="license-card">
      <template #header>
        <div class="header-row">
          <span>私有化授权激活</span>
          <el-button :loading="isStatusLoading" @click="refreshAll">刷新状态</el-button>
        </div>
      </template>

      <el-alert
        v-if="status?.deploymentMode && status.deploymentMode !== 'private'"
        title="当前不是 private 部署模式，激活接口将不会生效。"
        type="warning"
        :closable="false"
        class="mb-16"
      />

      <el-descriptions :column="2" border class="mb-16">
        <el-descriptions-item label="部署模式">
          {{ status?.deploymentMode || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="激活状态">
          <el-tag :type="status?.activated ? 'success' : 'warning'">
            {{ status?.activated ? '已激活' : '未激活' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="许可证 ID">
          {{ status?.licenseId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="医院标识">
          {{ status?.hospitalId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="支持期起始">
          {{ status?.supportStartDate || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="支持期结束">
          {{ status?.supportEndDate || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-form :model="activationForm" label-width="132px" class="activate-form">
        <el-form-item label="本机指纹摘要">
          <el-input v-model="activationForm.machineFingerprintHash" readonly />
        </el-form-item>
        <el-form-item label="激活码">
          <el-input
            v-model="activationForm.activationCode"
            type="password"
            show-password
            placeholder="请输入激活码"
            maxlength="128"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="isActivating" @click="activate">
            提交激活
          </el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="validationResult"
        :title="validationResult.reason || (validationResult.valid ? '激活成功' : '激活失败')"
        :type="validationResult.valid ? 'success' : 'error'"
        :closable="false"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import licenseApi from '@/api/license'
import type {
  ActivateLicenseRequest,
  ApiEnvelope,
  LicenseStatusResponse,
  LicenseValidationResponse
} from '@/api/types'

const status = ref<LicenseStatusResponse | null>(null)
const validationResult = ref<LicenseValidationResponse | null>(null)
const isStatusLoading = ref(false)
const isActivating = ref(false)

const activationForm = reactive<ActivateLicenseRequest>({
  activationCode: '',
  machineFingerprintHash: ''
})

const unwrapApiEnvelope = <T>(envelope: ApiEnvelope<T>): T => {
  if (!envelope || envelope.code !== 200 || envelope.data === null || envelope.data === undefined) {
    throw new Error(envelope?.message || 'Request failed')
  }
  return envelope.data
}

const loadStatus = async (): Promise<void> => {
  const response = await licenseApi.getLicenseStatus()
  status.value = unwrapApiEnvelope(response.data)
}

const loadFingerprint = async (): Promise<void> => {
  const response = await licenseApi.getFingerprint()
  const data = unwrapApiEnvelope(response.data)
  activationForm.machineFingerprintHash = data.machineFingerprintHash
}

const refreshAll = async (): Promise<void> => {
  isStatusLoading.value = true
  try {
    await Promise.all([loadStatus(), loadFingerprint()])
  } catch (error) {
    const err = error as Error
    ElMessage.error(err.message || '读取激活状态失败')
  } finally {
    isStatusLoading.value = false
  }
}

const activate = async (): Promise<void> => {
  if (!activationForm.activationCode.trim()) {
    ElMessage.warning('请输入激活码')
    return
  }
  if (!activationForm.machineFingerprintHash.trim()) {
    ElMessage.warning('未获取到本机指纹摘要，请先刷新状态')
    return
  }

  isActivating.value = true
  validationResult.value = null
  try {
    const response = await licenseApi.activateLicense({
      activationCode: activationForm.activationCode.trim(),
      machineFingerprintHash: activationForm.machineFingerprintHash.trim()
    })
    const result = unwrapApiEnvelope(response.data)
    validationResult.value = result
    if (result.valid) {
      ElMessage.success('激活成功')
      activationForm.activationCode = ''
      await loadStatus()
    } else {
      ElMessage.error(result.reason || '激活失败')
    }
  } catch (error) {
    const err = error as Error
    ElMessage.error(err.message || '激活请求失败')
  } finally {
    isActivating.value = false
  }
}

onMounted(() => {
  refreshAll()
})
</script>

<style scoped>
.license-page {
  padding: 20px;
}

.license-card {
  width: 100%;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mb-16 {
  margin-bottom: 16px;
}

.activate-form {
  margin-top: 8px;
}
</style>
