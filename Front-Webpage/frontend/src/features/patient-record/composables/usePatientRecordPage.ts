import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import patientApi from '@/api/patient'
import type { PatientRecordPayload } from '@/api/types'

export interface PatientRecordFormModel {
  name: string
  gender: string
  age: number | null
  visitDate: Date | string | null
  phone: string
  idCard: string
  consanguinity: boolean
  familyHistory: boolean
  familyHistoryDetail: string
  chiefComplaint: string
  presentIllness: string
  jaundice: string
  hepatomegaly: boolean
  splenomegaly: boolean
  kfRing: string
  neuroSymptoms: string[]
  alt: string
  ast: string
  tbil: string
  ceruloplasmin: string
  urineCopper: string
  ferritin: string
  transferrinSat: string
  imagingResult: string
  biopsyResult: string
  geneticTested: boolean
  mutatedGene: string
  diagnosis: string
  treatmentPlan: string
}

const createInitialFormData = (): PatientRecordFormModel => ({
  name: '',
  gender: '',
  age: null,
  visitDate: new Date(),
  phone: '',
  idCard: '',
  consanguinity: false,
  familyHistory: false,
  familyHistoryDetail: '',
  chiefComplaint: '',
  presentIllness: '',
  jaundice: '无',
  hepatomegaly: false,
  splenomegaly: false,
  kfRing: '未查',
  neuroSymptoms: [],
  alt: '',
  ast: '',
  tbil: '',
  ceruloplasmin: '',
  urineCopper: '',
  ferritin: '',
  transferrinSat: '',
  imagingResult: '',
  biopsyResult: '',
  geneticTested: false,
  mutatedGene: '',
  diagnosis: '',
  treatmentPlan: ''
})

const rules: FormRules<PatientRecordFormModel> = {
  name: [{ required: true, message: '患者姓名不能为空', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
  chiefComplaint: [{ required: true, message: '主诉不能为空', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入初步诊断', trigger: 'blur' }]
}

export const usePatientRecordPage = () => {
  const visitId = ref(`VISIT-${Date.now().toString().slice(-8)}`)
  const activeTab = ref('basic')
  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = reactive<PatientRecordFormModel>(createInitialFormData())

  const normalizePayload = (): PatientRecordPayload => ({
    ...formData,
    visitId: visitId.value,
    visitDate: formData.visitDate ? new Date(formData.visitDate).toISOString().slice(0, 10) : '',
    neuroSymptoms: [...formData.neuroSymptoms]
  })

  const saveDraft = (): void => {
    localStorage.setItem('imld_patient_record_draft', JSON.stringify(normalizePayload()))
    ElMessage({
      message: '草稿已保存至本地缓存',
      type: 'success'
    })
  }

  const submitForm = (): void => {
    if (!formRef.value || submitting.value) {
      return
    }

    formRef.value.validate(async (valid: boolean) => {
      if (!valid) {
        ElMessage.error('基础信息或必填项未完善，请检查红框字段')
        activeTab.value = 'basic'
        return
      }

      try {
        await ElMessageBox.confirm('确认核对无误并归档该患者病历吗？', '系统提示', {
          confirmButtonText: '确认提交',
          cancelButtonText: '返回修改',
          type: 'warning'
        })
      } catch {
        return
      }

      submitting.value = true
      try {
        const res = await patientApi.createRecord(normalizePayload())
        visitId.value = res.data.visitId || visitId.value
        ElMessage({
          type: 'success',
          message: '病历归档成功，已同步至 AI 辅助诊断中台',
          duration: 2800
        })
        localStorage.removeItem('imld_patient_record_draft')
      } catch {
        ElMessage.error('病历归档失败，请稍后重试')
      } finally {
        submitting.value = false
      }
    })
  }

  const resetForm = (): void => {
    ElMessageBox.confirm('清空后当前录入的所有数据将丢失，是否继续？', '警告', {
      confirmButtonText: '确认清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        formRef.value?.resetFields()
        ElMessage.info('表单已重置')
      })
      .catch(() => {
        // Ignore cancel action.
      })
  }

  return {
    visitId,
    activeTab,
    formRef,
    submitting,
    formData,
    rules,
    saveDraft,
    submitForm,
    resetForm
  }
}
