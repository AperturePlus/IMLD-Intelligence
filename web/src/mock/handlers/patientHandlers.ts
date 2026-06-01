// @ts-nocheck

import {
  findRecordPayloadByPatientNo,
  loadPatients,
  loadRecords,
  nextPatientId,
  resolveDiseaseByDiagnosis,
  resolveRiskByDiagnosis,
  savePatients,
  saveRecords
} from '../core/mockState'
import { normalizeLaboratoryScreening } from '@/features/patient-record/constants/laboratoryScreening'

const REQUIRED_RECORD_FIELDS = [
  'patientNo',
  'name',
  'gender',
  'age',
  'visitDate',
  'occupation',
  'currentAddress',
  'nativePlace',
  'department',
  'encounterType',
  'chiefComplaint',
  'presentIllness',
  'history.diseaseHistory.smokingHistory',
  'history.diseaseHistory.drinkingHistory',
  'history.diseaseHistory.diabetesHistory',
  'history.diseaseHistory.hypertensionHistory',
  'history.diseaseHistory.hyperuricemiaHistory',
  'history.diseaseHistory.hyperlipidemiaHistory',
  'history.diseaseHistory.coronaryHeartDiseaseHistory',
  'history.diseaseHistory.hepatitisBHistory',
  'history.surgeryHistory.status',
  'history.transfusionHistory.status',
  'history.allergyHistory',
  'history.medicationHistory',
  'history.familyHistory',
  'physicalExam.heightCm',
  'physicalExam.weightKg',
  'physicalExam.bmi',
  'physicalExam.bloodPressureSystolic',
  'physicalExam.bloodPressureDiastolic',
  'physicalExam.respiratoryRate',
  'physicalExam.heartRate',
  'physicalExam.liverFibrosis',
  'physicalExam.cirrhosis',
  'physicalExam.fattyLiver',
  'physicalExam.liverFailure',
  'physicalExam.cholestasis',
  'physicalExam.viralHepatitis',
  'clinicalDecision.diagnosis'
]

const CONDITIONAL_REQUIRED_FIELDS = [
  {
    statusPath: 'history.surgeryHistory.status',
    detailPath: 'history.surgeryHistory.detail',
    label: '手术史'
  },
  {
    statusPath: 'history.transfusionHistory.status',
    detailPath: 'history.transfusionHistory.detail',
    label: '输血史'
  }
]

const DEFAULT_DISEASE_HISTORY = {
  smokingHistory: 'UNKNOWN',
  drinkingHistory: 'UNKNOWN',
  diabetesHistory: 'UNKNOWN',
  hypertensionHistory: 'UNKNOWN',
  hyperuricemiaHistory: 'UNKNOWN',
  hyperlipidemiaHistory: 'UNKNOWN',
  coronaryHeartDiseaseHistory: 'UNKNOWN',
  hepatitisBHistory: 'UNKNOWN'
}

const DEFAULT_CONDITIONAL_HISTORY = {
  status: 'UNKNOWN',
  detail: ''
}

const DEFAULT_PHYSICAL_EXAM = {
  heightCm: null,
  weightKg: null,
  bmi: null,
  bloodPressureSystolic: null,
  bloodPressureDiastolic: null,
  respiratoryRate: null,
  heartRate: null,
  liverFibrosis: 'UNKNOWN',
  cirrhosis: 'UNKNOWN',
  fattyLiver: 'UNKNOWN',
  liverFailure: 'UNKNOWN',
  cholestasis: 'UNKNOWN',
  viralHepatitis: 'UNKNOWN'
}

const DEFAULT_PATHOLOGY = {
  performed: false,
  reportText: '',
  nasScore: null,
  reportedAt: '',
  fileId: null,
  sourceType: 'MANUAL'
}

const DEFAULT_GENETIC_SEQUENCING = {
  tested: false,
  method: '',
  reportSource: '',
  reportDate: '',
  summary: '',
  conclusion: '',
  fileId: null,
  sourceType: 'MANUAL',
  variants: []
}

const DEFAULT_CLINICAL_DECISION = {
  diagnosis: '',
  treatmentPlan: ''
}

const getValueByPath = (input, path) =>
  String(path)
    .split('.')
    .reduce((current, segment) => (current == null ? undefined : current[segment]), input)

const normalizeText = (value) => (typeof value === 'string' ? value.trim() : '')

const normalizeNumber = (value) => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number.parseFloat(value)
    return Number.isFinite(parsed) ? parsed : null
  }

  return null
}

const normalizeTriState = (value, fallback = 'UNKNOWN') => {
  if (value === 'YES' || value === 'NO' || value === 'UNKNOWN') {
    return value
  }

  if (value === true) {
    return 'YES'
  }

  if (value === false) {
    return 'NO'
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (trimmed === '有' || trimmed === '是' || trimmed === '阳性') {
      return 'YES'
    }
    if (trimmed === '无' || trimmed === '否' || trimmed === '阴性' || trimmed === '未见') {
      return 'NO'
    }
    if (trimmed === '未查') {
      return 'UNKNOWN'
    }
  }

  return fallback
}

const normalizeNullableText = (value) => {
  const text = normalizeText(value)
  return text || null
}

const normalizeNasScore = (value) => {
  const parsed = normalizeNumber(value)
  if (parsed == null) {
    return null
  }
  return Math.max(0, Math.min(8, Math.round(parsed)))
}

const normalizeDateOnly = (value) => {
  const text = normalizeText(value)
  if (!text) {
    return ''
  }

  const parsed = new Date(text)
  if (Number.isNaN(parsed.getTime())) {
    return text
  }

  return parsed.toISOString().slice(0, 10)
}

const normalizeImagingModality = (value) => {
  const normalized = normalizeText(value).toUpperCase()
  if (normalized === 'US' || normalized === 'ULTRASOUND' || normalized === '超声') {
    return 'ULTRASOUND'
  }
  if (normalized === 'CT') {
    return 'CT'
  }
  if (normalized === 'MRI') {
    return 'MRI'
  }
  return normalized === 'OTHER' ? 'OTHER' : 'OTHER'
}

const normalizeImagingSourceType = (value) => {
  const normalized = normalizeText(value).toUpperCase()
  return ['MANUAL', 'IMAGE_OCR', 'PDF_OCR', 'PACS_IMPORT'].includes(normalized) ? normalized : 'MANUAL'
}

const normalizePathologySourceType = (value) => {
  const normalized = normalizeText(value).toUpperCase()
  return ['MANUAL', 'IMAGE_OCR', 'PDF_OCR', 'PACS_IMPORT'].includes(normalized) ? normalized : 'MANUAL'
}

const normalizeGeneticSourceType = (value) => {
  const normalized = normalizeText(value).toUpperCase()
  return ['MANUAL', 'IMAGE_OCR', 'PDF_OCR', 'HIS_LIS'].includes(normalized) ? normalized : 'MANUAL'
}

const normalizeGeneticMethod = (value) => {
  const normalized = normalizeText(value).toUpperCase()
  return ['PANEL', 'WES', 'WGS', 'OTHER'].includes(normalized) ? normalized : ''
}

const normalizeImagingReports = (value, legacyImagingResult) => {
  if (Array.isArray(value)) {
    return value.map((item) => ({
      modality: normalizeImagingModality(item?.modality),
      reportText: normalizeText(item?.reportText),
      examinedAt: normalizeDateOnly(item?.examinedAt),
      fileId: normalizeNullableText(item?.fileId),
      sourceType: normalizeImagingSourceType(item?.sourceType)
    }))
  }

  const legacyText = normalizeText(legacyImagingResult)
  if (!legacyText) {
    return []
  }

  return [
    {
      modality: 'OTHER',
      reportText: legacyText,
      examinedAt: '',
      fileId: null,
      sourceType: 'MANUAL'
    }
  ]
}

const normalizePathology = (value, legacyBiopsyResult) => {
  if (value && typeof value === 'object') {
    const reportText = normalizeText(value.reportText) || normalizeText(legacyBiopsyResult)
    return {
      performed: typeof value.performed === 'boolean' ? value.performed : Boolean(reportText),
      reportText,
      nasScore: normalizeNasScore(value.nasScore),
      reportedAt: normalizeDateOnly(value.reportedAt),
      fileId: normalizeNullableText(value.fileId),
      sourceType: normalizePathologySourceType(value.sourceType)
    }
  }

  const legacyText = normalizeText(legacyBiopsyResult)
  return {
    ...DEFAULT_PATHOLOGY,
    performed: Boolean(legacyText),
    reportText: legacyText
  }
}

const normalizeGeneticVariants = (value) => {
  if (!Array.isArray(value)) {
    return []
  }

  return value.map((item) => ({
    gene: normalizeText(item?.gene),
    hgvsC: normalizeText(item?.hgvsC),
    hgvsP: normalizeText(item?.hgvsP),
    variantType: normalizeText(item?.variantType),
    zygosity: normalizeText(item?.zygosity),
    classification: normalizeText(item?.classification),
    evidence: normalizeText(item?.evidence)
  }))
}

const normalizeGeneticSequencing = (value, legacyGeneticTested, legacyMutatedGene) => {
  if (value && typeof value === 'object') {
    const variants = normalizeGeneticVariants(value.variants)
    const legacyGene = normalizeText(legacyMutatedGene)
    return {
      tested:
        typeof value.tested === 'boolean'
          ? value.tested
          : typeof legacyGeneticTested === 'boolean'
            ? legacyGeneticTested
            : variants.length > 0,
      method: normalizeGeneticMethod(value.method),
      reportSource: normalizeText(value.reportSource),
      reportDate: normalizeDateOnly(value.reportDate),
      summary: normalizeText(value.summary),
      conclusion: normalizeText(value.conclusion) || legacyGene,
      fileId: normalizeNullableText(value.fileId),
      sourceType: normalizeGeneticSourceType(value.sourceType),
      variants: variants.length > 0 ? variants : legacyGene ? [{ gene: legacyGene }] : []
    }
  }

  const legacyGene = normalizeText(legacyMutatedGene)
  return {
    ...DEFAULT_GENETIC_SEQUENCING,
    tested: typeof legacyGeneticTested === 'boolean' ? legacyGeneticTested : false,
    conclusion: legacyGene,
    variants: legacyGene ? [{ gene: legacyGene }] : []
  }
}

const normalizeClinicalDecision = (value, legacyDiagnosis, legacyTreatmentPlan) => {
  if (value && typeof value === 'object') {
    return {
      diagnosis: normalizeText(value.diagnosis) || normalizeText(legacyDiagnosis),
      treatmentPlan: normalizeText(value.treatmentPlan) || normalizeText(legacyTreatmentPlan)
    }
  }

  return {
    diagnosis: normalizeText(legacyDiagnosis),
    treatmentPlan: normalizeText(legacyTreatmentPlan)
  }
}

const isBlankValue = (value) => {
  if (value == null) {
    return true
  }

  if (typeof value === 'string') {
    return value.trim() === ''
  }

  if (typeof value === 'number') {
    return Number.isNaN(value)
  }

  return false
}

const normalizeStoredConditionalHistory = (value) => ({
  status: normalizeTriState(value?.status),
  detail: normalizeTriState(value?.status) === 'YES' ? normalizeText(value?.detail) : ''
})

const normalizeStoredRecordPayload = (payload = {}) => {
  const {
    alt,
    ast,
    tbil,
    ceruloplasmin,
    urineCopper,
    ferritin,
    transferrinSat,
    laboratoryScreening,
    imagingReports,
    pathology,
    geneticSequencing,
    clinicalDecision,
    ...restPayload
  } = payload
  const history = payload.history || {}
  const diseaseHistory = history.diseaseHistory || {}
  const physicalExam = payload.physicalExam || {}

  return {
    ...restPayload,
    presentIllness: normalizeText(payload.presentIllness),
    history: {
      diseaseHistory: {
        ...DEFAULT_DISEASE_HISTORY,
        smokingHistory: normalizeTriState(diseaseHistory.smokingHistory),
        drinkingHistory: normalizeTriState(diseaseHistory.drinkingHistory),
        diabetesHistory: normalizeTriState(diseaseHistory.diabetesHistory),
        hypertensionHistory: normalizeTriState(diseaseHistory.hypertensionHistory),
        hyperuricemiaHistory: normalizeTriState(diseaseHistory.hyperuricemiaHistory),
        hyperlipidemiaHistory: normalizeTriState(diseaseHistory.hyperlipidemiaHistory),
        coronaryHeartDiseaseHistory: normalizeTriState(diseaseHistory.coronaryHeartDiseaseHistory),
        hepatitisBHistory: normalizeTriState(diseaseHistory.hepatitisBHistory)
      },
      surgeryHistory: normalizeStoredConditionalHistory(history.surgeryHistory),
      transfusionHistory: normalizeStoredConditionalHistory(history.transfusionHistory),
      allergyHistory: normalizeText(history.allergyHistory),
      medicationHistory: normalizeText(history.medicationHistory),
      familyHistory: normalizeText(history.familyHistory) || normalizeText(payload.familyHistoryDetail)
    },
    physicalExam: {
      ...DEFAULT_PHYSICAL_EXAM,
      heightCm: normalizeNumber(physicalExam.heightCm),
      weightKg: normalizeNumber(physicalExam.weightKg),
      bmi: normalizeNumber(physicalExam.bmi),
      bloodPressureSystolic: normalizeNumber(physicalExam.bloodPressureSystolic),
      bloodPressureDiastolic: normalizeNumber(physicalExam.bloodPressureDiastolic),
      respiratoryRate: normalizeNumber(physicalExam.respiratoryRate),
      heartRate: normalizeNumber(physicalExam.heartRate),
      liverFibrosis: normalizeTriState(physicalExam.liverFibrosis),
      cirrhosis: normalizeTriState(physicalExam.cirrhosis),
      fattyLiver: normalizeTriState(physicalExam.fattyLiver),
      liverFailure: normalizeTriState(physicalExam.liverFailure),
      cholestasis: normalizeTriState(physicalExam.cholestasis),
      viralHepatitis: normalizeTriState(physicalExam.viralHepatitis)
    },
    imagingReports: normalizeImagingReports(imagingReports, payload.imagingResult),
    pathology: normalizePathology(pathology, payload.biopsyResult),
    geneticSequencing: normalizeGeneticSequencing(
      geneticSequencing,
      payload.geneticTested,
      payload.mutatedGene
    ),
    clinicalDecision: normalizeClinicalDecision(
      clinicalDecision,
      payload.diagnosis,
      payload.treatmentPlan
    ),
    laboratoryScreening: normalizeLaboratoryScreening(laboratoryScreening, {
      alt,
      ast,
      tbil,
      ceruloplasmin,
      urineCopper,
      ferritin,
      transferrinSat
    })
  }
}

const toDateOnly = (date = new Date()) => {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

const createTraceId = (prefix) => `${prefix}-${Date.now()}`

const buildImportPreview = ({ sourceType, patientNo, name, gender, age, confidence }) => ({
  sourceType,
  traceId: createTraceId('IMP'),
  confidence,
  patientNo,
  name,
  gender,
  age,
  visitDate: toDateOnly(),
  phone: '13800138000',
  idCard: '510101198601012233',
  occupation: '教师',
  currentAddress: '四川省成都市武侯区人民南路三段',
  nativePlace: '四川成都',
  department: '肝病医学科',
  encounterType: 'OUTPATIENT'
})

const pickSeedByPatientNoOrVisitNo = (data) => {
  const candidates = loadPatients()
  const byPatientNo = data.patientNo
    ? candidates.find((item) => item.id.toLowerCase() === String(data.patientNo).toLowerCase())
    : null
  if (byPatientNo) {
    return byPatientNo
  }

  if (data.visitNo) {
    const hash = String(data.visitNo)
      .split('')
      .reduce((sum, ch) => sum + ch.charCodeAt(0), 0)
    return candidates[hash % Math.max(candidates.length, 1)]
  }

  return candidates[0]
}

export const patientExactHandlers = {
  'GET /api/v1/web/patients/': async ({ query }) => {
    const keyword = (query.keyword || '').trim()
    const items = loadPatients()
      .filter((item) => !keyword || item.name.includes(keyword) || item.id.includes(keyword))
      .map((item) => ({
        id: item.id,
        name: item.name,
        gender: item.gender,
        age: item.age,
        riskLevel: item.riskLevel,
        avatar: item.avatar
      }))

    return { status: 200, data: { items } }
  },

  'POST /api/v1/web/patient-records/': async ({ data }) => {
    const errors = {}

    REQUIRED_RECORD_FIELDS.forEach((field) => {
      if (isBlankValue(getValueByPath(data, field))) {
        errors[field] = [`${field}不能为空`]
      }
    })

    CONDITIONAL_REQUIRED_FIELDS.forEach(({ statusPath, detailPath, label }) => {
      if (getValueByPath(data, statusPath) === 'YES' && isBlankValue(getValueByPath(data, detailPath))) {
        errors[detailPath] = [`${label}为“有”时明细不能为空`]
      }
    })

    if (data?.pathology?.performed && isBlankValue(data?.pathology?.reportText)) {
      errors['pathology.reportText'] = ['已执行肝穿刺活检时必须填写病理结果']
    }

    if (data?.geneticSequencing?.tested && isBlankValue(data?.geneticSequencing?.method)) {
      errors['geneticSequencing.method'] = ['已进行基因检测时必须选择检测方法']
    }

    if (Object.keys(errors).length > 0) {
      return { status: 400, statusText: 'Bad Request', data: errors }
    }

    const normalizedPayload = normalizeStoredRecordPayload(data)
    const records = loadRecords().map((item) => ({
      ...item,
      payload: normalizeStoredRecordPayload(item.payload || {})
    }))
    const patients = loadPatients()
    const now = Date.now()
    const recordId = `REC-${now}`
    const visitId = normalizedPayload.visitId || `VISIT-${String(now).slice(-8)}`

    records.push({
      id: recordId,
      visitId,
      submittedAt: new Date().toISOString(),
      payload: normalizedPayload
    })
    saveRecords(records)

    const existing = patients.find(
      (item) =>
        item.id.toLowerCase() === String(normalizedPayload.patientNo).toLowerCase() || item.name === normalizedPayload.name
    )
    if (!existing) {
      const disease = resolveDiseaseByDiagnosis(normalizedPayload.clinicalDecision?.diagnosis)
      patients.unshift({
        id: String(normalizedPayload.patientNo || nextPatientId(patients)),
        name: normalizedPayload.name,
        gender: normalizedPayload.gender,
        age: Number(normalizedPayload.age) || 0,
        riskLevel: resolveRiskByDiagnosis(normalizedPayload.clinicalDecision?.diagnosis),
        disease,
        compliance: '一般',
        aiStatus: '未诊断',
        avatar: `https://randomuser.me/api/portraits/${normalizedPayload.gender === '女' ? 'women' : 'men'}/${Math.floor(Math.random() * 80) + 10}.jpg`
      })
      savePatients(patients)
    }

    return {
      status: 201,
      statusText: 'Created',
      data: {
        recordId,
        visitId,
        message: '病历归档成功'
      }
    }
  },

  'POST /api/v1/web/integration/imports/patient/his-lis': async ({ data }) => {
    if (!data.patientNo && !data.visitNo) {
      return {
        status: 400,
        statusText: 'Bad Request',
        data: {
          message: 'patientNo 或 visitNo 至少提供一个'
        }
      }
    }

    const seed = pickSeedByPatientNoOrVisitNo(data)
    return {
      status: 200,
      data: buildImportPreview({
        sourceType: 'HIS_LIS',
        patientNo: data.patientNo || seed?.id || `P${Date.now().toString().slice(-5)}`,
        name: seed?.name || '未知患者',
        gender: seed?.gender || '男',
        age: seed?.age ?? 35,
        confidence: 0.96
      })
    }
  },

  'POST /api/v1/web/integration/imports/patient/image': async ({ data }) => {
    if (!data.fileName || !data.fileContentBase64) {
      return {
        status: 400,
        statusText: 'Bad Request',
        data: {
          message: '图片文件内容不能为空'
        }
      }
    }

    return {
      status: 200,
      data: buildImportPreview({
        sourceType: 'IMAGE_OCR',
        patientNo: `IMG-${Date.now().toString().slice(-6)}`,
        name: '图片识别患者',
        gender: '女',
        age: 29,
        confidence: 0.83
      })
    }
  },

  'POST /api/v1/web/integration/imports/patient/pdf': async ({ data }) => {
    if (!data.fileName || !data.fileContentBase64) {
      return {
        status: 400,
        statusText: 'Bad Request',
        data: {
          message: 'PDF 文件内容不能为空'
        }
      }
    }

    return {
      status: 200,
      data: buildImportPreview({
        sourceType: 'PDF_OCR',
        patientNo: `PDF-${Date.now().toString().slice(-6)}`,
        name: 'PDF识别患者',
        gender: '男',
        age: 47,
        confidence: 0.88
      })
    }
  }
}

export const patientDynamicHandlers = [
  {
    method: 'GET',
    pattern: /^\/api\/v1\/web\/patient-records\/([^/]+)$/,
    buildParams: (match) => ({ patientNo: match[1] }),
    handler: async ({ params }) => {
      const payload = findRecordPayloadByPatientNo(decodeURIComponent(params.patientNo))
      if (!payload) {
        return { status: 404, statusText: 'Not Found', data: { detail: '该患者暂无电子病历' } }
      }
      return { status: 200, data: payload }
    }
  }
]

export const patientRouteDocs = [
  {
    module: 'patient',
    method: 'GET',
    path: '/api/v1/web/patients/',
    kind: 'exact',
    description: '患者列表查询（支持 keyword）。'
  },
  {
    module: 'patient',
    method: 'POST',
    path: '/api/v1/web/patient-records/',
    kind: 'exact',
    description: '创建病历并在必要时写入新患者。'
  },
  {
    module: 'patient',
    method: 'GET',
    path: '/api/v1/web/patient-records/:patientNo',
    kind: 'dynamic',
    description: '按病号查询患者电子病历（只读）。'
  },
  {
    module: 'patient',
    method: 'POST',
    path: '/api/v1/web/integration/imports/patient/his-lis',
    kind: 'exact',
    description: '按病人ID号/就诊号从 HIS/LIS 预填病历基础信息。'
  },
  {
    module: 'patient',
    method: 'POST',
    path: '/api/v1/web/integration/imports/patient/image',
    kind: 'exact',
    description: '图片 OCR 识别并预填病历基础信息。'
  },
  {
    module: 'patient',
    method: 'POST',
    path: '/api/v1/web/integration/imports/patient/pdf',
    kind: 'exact',
    description: 'PDF OCR 识别并预填病历基础信息。'
  }
]
