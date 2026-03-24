<template>
  <div class="record-container">
    <div class="page-header">
      <el-row justify="space-between" align="middle">
        <el-col :span="12">
          <el-text tag="b" class="header-title">
            <el-icon class="mr-2"><DocumentAdd /></el-icon>
            专病电子病历录入 - 遗传代谢性肝病
          </el-text>
        </el-col>
        <el-col :span="12" style="text-align: right;">
          <el-text type="info" size="small">就诊单号：{{ visitId }}</el-text>
        </el-col>
      </el-row>
    </div>

    <div class="form-wrapper">
      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules" 
        label-position="right" 
        label-width="140px"
      >
        <el-tabs v-model="activeTab" type="border-card" class="custom-tabs">
          
          <el-tab-pane label="基础档案" name="basic">
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="患者姓名" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="formData.gender">
                    <el-radio label="男">男</el-radio>
                    <el-radio label="女">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="年龄" prop="age">
                  <el-input-number v-model="formData.age" :min="0" :max="120" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="就诊日期" prop="visitDate">
                  <el-date-picker v-model="formData.visitDate" type="date" placeholder="选择日期" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="联系电话">
                  <el-input v-model="formData.phone" placeholder="请输入手机号" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="身份证号">
                  <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">遗传病史特征</el-divider>
            
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="父母是否近亲婚配">
                  <el-switch v-model="formData.consanguinity" active-text="是" inactive-text="否" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="肝病家族史">
                  <el-switch v-model="formData.familyHistory" active-text="有" inactive-text="无" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="家族病史详情" v-if="formData.familyHistory">
                  <el-input v-model="formData.familyHistoryDetail" placeholder="如：父亲患有肝豆状核变性" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="临床特征" name="clinical">
            <el-form-item label="主诉" prop="chiefComplaint">
              <el-input 
                v-model="formData.chiefComplaint" 
                type="textarea" 
                :rows="2" 
                placeholder="请简明扼要记录患者就诊主要症状及持续时间..." 
              />
            </el-form-item>
            
            <el-form-item label="现病史" prop="presentIllness">
              <el-input 
                v-model="formData.presentIllness" 
                type="textarea" 
                :rows="4" 
                placeholder="详细记录疾病发生、发展、诊疗经过..." 
              />
            </el-form-item>

            <el-divider content-position="left">体格检查 (阳性体征筛查)</el-divider>
            
            <el-row :gutter="24">
              <el-col :span="6">
                <el-form-item label="皮肤/巩膜黄染">
                  <el-select v-model="formData.jaundice" placeholder="请选择">
                    <el-option label="无" value="无" />
                    <el-option label="轻度" value="轻度" />
                    <el-option label="中重度" value="中重度" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="肝脏肿大">
                  <el-switch v-model="formData.hepatomegaly" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="脾脏肿大">
                  <el-switch v-model="formData.splenomegaly" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="角膜 K-F 环" title="肝豆状核变性特征">
                  <el-select v-model="formData.kfRing" placeholder="裂隙灯检查结果">
                    <el-option label="未见" value="未见" />
                    <el-option label="阳性 (+)" value="阳性" />
                    <el-option label="未查" value="未查" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
               <el-col :span="24">
                  <el-form-item label="神经系统症状">
                    <el-checkbox-group v-model="formData.neuroSymptoms">
                      <el-checkbox label="震颤" />
                      <el-checkbox label="肌张力障碍" />
                      <el-checkbox label="步态异常" />
                      <el-checkbox label="发音困难" />
                      <el-checkbox label="精神行为异常" />
                      <el-checkbox label="无" />
                    </el-checkbox-group>
                  </el-form-item>
               </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="生化与代谢筛查" name="metabolic">
            <div class="section-title">常规肝功能</div>
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="ALT (谷丙转氨酶)">
                  <el-input v-model="formData.alt" placeholder="0.00"><template #append>U/L</template></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="AST (谷草转氨酶)">
                  <el-input v-model="formData.ast" placeholder="0.00"><template #append>U/L</template></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="TBIL (总胆红素)">
                  <el-input v-model="formData.tbil" placeholder="0.00"><template #append>μmol/L</template></el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="section-title">铜代谢 (Wilson病筛查)</div>
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="血清铜蓝蛋白">
                  <el-input v-model="formData.ceruloplasmin" placeholder="< 0.2 提示异常"><template #append>g/L</template></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="24h 尿铜">
                  <el-input v-model="formData.urineCopper" placeholder="> 100 提示异常"><template #append>μg/24h</template></el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="section-title">铁代谢 (血色病筛查)</div>
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="血清铁蛋白">
                  <el-input v-model="formData.ferritin" placeholder="0.00"><template #append>ng/mL</template></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="转铁蛋白饱和度">
                  <el-input v-model="formData.transferrinSat" placeholder="0.00"><template #append>%</template></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="影像/基因与诊断" name="diagnosis">
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="影像学表现">
                  <el-input 
                    v-model="formData.imagingResult" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="录入超声/CT/MRI肝脏及脾脏影像学描述..." 
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="肝穿刺活检">
                  <el-input 
                    v-model="formData.biopsyResult" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="如：肝细胞内大量铜颗粒沉积 / 铁沉积..." 
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">遗传学检测</el-divider>
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="是否进行基因检测">
                  <el-radio-group v-model="formData.geneticTested">
                    <el-radio :label="true">已测</el-radio>
                    <el-radio :label="false">未测</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="8" v-if="formData.geneticTested">
                <el-form-item label="致病基因突变">
                  <el-select v-model="formData.mutatedGene" placeholder="选择已知突变基因" filterable allow-create>
                    <el-option label="ATP7B (肝豆状核变性)" value="ATP7B" />
                    <el-option label="HFE (遗传性血色病)" value="HFE" />
                    <el-option label="SERPINA1 (α1-抗胰蛋白酶缺乏)" value="SERPINA1" />
                    <el-option label="G6PC / SLC37A4 (糖原累积病)" value="G6PC" />
                    <el-option label="阴性/未发现" value="阴性" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">临床诊断与干预</el-divider>
            <el-form-item label="初步诊断" prop="diagnosis">
              <el-input v-model="formData.diagnosis" placeholder="如：肝豆状核变性（Wilson病）、肝硬化代偿期..." />
            </el-form-item>
            <el-form-item label="治疗干预方案">
              <el-input 
                v-model="formData.treatmentPlan" 
                type="textarea" 
                :rows="4" 
                placeholder="如：1. 低铜饮食； 2. 青霉胺 0.25g tid 驱铜治疗； 3. 保肝对症处理..." 
              />
            </el-form-item>
          </el-tab-pane>

        </el-tabs>
      </el-form>
    </div>

    <div class="action-footer">
      <el-button @click="resetForm" size="large">清空重置</el-button>
      <el-button type="warning" plain size="large" @click="saveDraft">保存草稿</el-button>
      <el-button type="primary" size="large" @click="submitForm" :icon="Select" :loading="submitting">提交归档</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { DocumentAdd, Select } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import patientApi from '../../api/patient'
import type { PatientRecordPayload } from '../../api/types'

const visitId = ref(`VISIT-${Date.now().toString().slice(-8)}`)
const activeTab = ref('basic')
const formRef = ref<FormInstance>()
const submitting = ref(false)

interface PatientRecordForm {
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

const formData = reactive<PatientRecordForm>({
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

const rules: FormRules<PatientRecordForm> = {
  name: [{ required: true, message: '患者姓名不能为空', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
  chiefComplaint: [{ required: true, message: '主诉不能为空', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入初步诊断', trigger: 'blur' }]
}

const normalizePayload = (): PatientRecordPayload => ({
  ...formData,
  visitId: visitId.value,
  visitDate: formData.visitDate ? new Date(formData.visitDate).toISOString().slice(0, 10) : '',
  neuroSymptoms: [...formData.neuroSymptoms]
})

const saveDraft = () => {
  localStorage.setItem('imld_patient_record_draft', JSON.stringify(normalizePayload()))
  ElMessage({
    message: '草稿已保存至本地缓存',
    type: 'success'
  })
}

const submitForm = () => {
  if (!formRef.value || submitting.value) return

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

const resetForm = () => {
  ElMessageBox.confirm('清空后当前录入的所有数据将丢失，是否继续？', '警告', {
    confirmButtonText: '确认清空',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    if (formRef.value) formRef.value.resetFields()
    ElMessage.info('表单已重置')
  }).catch(() => {})
}
</script>

<style scoped>
.record-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  position: relative;
  padding-bottom: 80px; /* 给底部悬浮操作栏留出空间 */
}

/* 顶部标题区 */
.page-header {
  margin-bottom: 20px;
  background: #ffffff;
  padding: 16px 24px;
  border-radius: 8px;
  border-left: 5px solid #409EFF;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.header-title {
  font-size: 18px;
  display: flex;
  align-items: center;
  color: #303133;
}

.mr-2 {
  margin-right: 8px;
}

/* 表单主体 */
.form-wrapper {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 自定义 Tabs 样式，使其看起来更大气 */
.custom-tabs {
  border: none;
  box-shadow: none;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  height: 50px;
  line-height: 50px;
}

:deep(.el-tab-pane) {
  padding: 24px;
}

/* 内部区块标题 */
.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #606266;
  margin: 20px 0 16px 0;
  padding-left: 10px;
  border-left: 3px solid #909399;
}

/* 底部悬浮操作栏 */
.action-footer {
  position: fixed;
  bottom: 0;
  right: 0;
  width: calc(100% - 220px); /* 减去侧边栏宽度，如果是折叠状态这里可以动态计算，简化处理先写死 */
  background: #ffffff;
  padding: 16px 40px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  z-index: 100;
  border-top: 1px solid #ebeef5;
}

/* 适配侧边栏折叠时的底部操作栏宽度 */
@media screen and (max-width: 1200px) {
  .action-footer {
    width: calc(100% - 64px);
  }
}
</style>

