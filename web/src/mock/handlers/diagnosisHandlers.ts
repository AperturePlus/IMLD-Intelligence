// @ts-nocheck

import {
  buildDiagnosisPayload,
  findRecordPayloadByPatientNo,
  loadPatients,
  loadReports,
  saveReports,
  toAiFinding
} from '../core/mockState'
import { wait } from '../core/mockUtils'
import {
  DEFAULT_MODEL_FEATURE_COUNT,
  buildEvidenceItemsFromDiagnosis,
  buildEvidenceSummary,
  buildKeySignsFromEvidenceItems
} from '@/features/diagnosis/services/diagnosisEvidence'
import { resolveDiagnosisConfidence } from '@/features/diagnosis/services/confidenceConfig'
import { predictImldDemoDiagnosis } from '@/features/diagnosis/services/imldDemoInference'
import { toChineseRiskLabel } from '@/features/diagnosis/services/riskLevel'

const SUCCESS_CODE = 200
const DEFAULT_PAGE_SIZE = 20
const MAX_PAGE_SIZE = 200
const DEFAULT_DOCTOR_ID = 1
const DEFAULT_MODEL_REGISTRY_ID = 1
const AI_DIAGNOSIS_EXTRA_DELAY_MS = Number(import.meta.env.VITE_MOCK_AI_DIAGNOSIS_DELAY_MS ?? 1800)

const buildMockDiagnosisPayload = async (patient) => {
  const record = findRecordPayloadByPatientNo(patient.id)
  const inferredPayload = await predictImldDemoDiagnosis(patient, record)
  return inferredPayload || buildDiagnosisPayload(patient)
}

const successEnvelope = (data, message = 'success') => ({
  code: SUCCESS_CODE,
  message,
  data
})

const failEnvelope = (status, message) => ({
  status,
  statusText: 'Error',
  data: {
    code: status,
    message,
    data: null
  }
})

const parsePositiveInt = (value, fallback = 0) => {
  const parsed = Number.parseInt(String(value ?? ''), 10)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

const clampPageSize = (value) => {
  const parsed = parsePositiveInt(value, DEFAULT_PAGE_SIZE)
  return Math.min(Math.max(parsed, 1), MAX_PAGE_SIZE)
}

const toPatientNumericId = (patientId = '') => {
  const digits = String(patientId).replace(/\D/g, '')
  return parsePositiveInt(digits)
}

const toEncounterId = (visitId = '') => {
  const digits = String(visitId).replace(/\D/g, '')
  return parsePositiveInt(digits)
}

const toBirthDateByAge = (age = 0) => {
  const years = Number.isFinite(Number(age)) ? Math.max(0, Number(age)) : 0
  return `${new Date().getFullYear() - years}-01-01`
}

const toIsoDayStart = (dateText = '') => {
  if (typeof dateText === 'string' && dateText.trim()) {
    return `${dateText.slice(0, 10)}T08:00:00.000Z`
  }
  return new Date().toISOString()
}

const toIsoDayEnd = (dateText = '') => {
  if (typeof dateText === 'string' && dateText.trim()) {
    return `${dateText.slice(0, 10)}T08:30:00.000Z`
  }
  return new Date().toISOString()
}

const parseProbability = (value) => {
  const parsed = Number.parseFloat(String(value ?? '0'))
  if (!Number.isFinite(parsed)) {
    return 0.5
  }
  const normalized = parsed > 1 ? parsed / 100 : parsed
  return Math.max(0, Math.min(1, normalized))
}

const buildSessionId = (report) => {
  const source = String(report.id || `${report.patientId}_${report.visitId}`)
  let hash = 0
  for (let index = 0; index < source.length; index += 1) {
    hash = (hash * 31 + source.charCodeAt(index)) % 900000
  }
  return 100000 + hash
}

const buildResultId = (sessionId) => sessionId * 10 + 1

const inferDiseaseCode = (diseaseName = '') => {
  if (diseaseName.includes('Wilson') || diseaseName.includes('肝豆')) {
    return 'WILSON_DISEASE'
  }
  if (diseaseName.includes('Citrin')) {
    return 'CITRIN_DEFICIENCY'
  }
  if (diseaseName.includes('PFIC2') || diseaseName.includes('ABCB11')) {
    return 'PFIC2_ABCB11'
  }
  if (diseaseName.includes('PFIC3') || diseaseName.includes('ABCB4')) {
    return 'PFIC3_ABCB4'
  }
  if (diseaseName.includes('Dubin')) {
    return 'DUBIN_JOHNSON'
  }
  if (diseaseName.includes('Alagille')) {
    return 'ALAGILLE_SYNDROME'
  }
  if (diseaseName.includes('NTCP') || diseaseName.includes('SLC10A1')) {
    return 'NTCP_DEFICIENCY'
  }
  if (diseaseName.includes('脂肪酶') || diseaseName.includes('LIPA')) {
    return 'LIPA_DEFICIENCY'
  }
  if (diseaseName.includes('糖原') || diseaseName.includes('G6PC')) {
    return 'GLYCOGEN_STORAGE_DISEASE_I'
  }
  if (diseaseName.includes('血色')) {
    return diseaseName.includes('青年型') ? 'JUVENILE_HEMOCHROMATOSIS' : 'HEMOCHROMATOSIS'
  }
  if (diseaseName.includes('抗胰蛋白酶')) {
    return 'AAT_DEFICIENCY'
  }
  if (diseaseName.includes('Gilbert') || diseaseName.includes('吉尔伯特')) {
    return 'GILBERT_SYNDROME'
  }
  return 'METABOLIC_LIVER_DISEASE'
}

const inferRiskLevel = (probability) => {
  return toChineseRiskLabel(undefined, probability)
}

const inferGeneByDisease = (diseaseName = '') => {
  if (diseaseName.includes('Wilson') || diseaseName.includes('肝豆')) {
    return 'ATP7B'
  }
  if (diseaseName.includes('Citrin')) {
    return 'SLC25A13'
  }
  if (diseaseName.includes('PFIC2') || diseaseName.includes('ABCB11')) {
    return 'ABCB11'
  }
  if (diseaseName.includes('PFIC3') || diseaseName.includes('ABCB4')) {
    return 'ABCB4'
  }
  if (diseaseName.includes('Dubin')) {
    return 'ABCC2'
  }
  if (diseaseName.includes('Alagille')) {
    return 'JAG1'
  }
  if (diseaseName.includes('NTCP') || diseaseName.includes('SLC10A1')) {
    return 'SLC10A1'
  }
  if (diseaseName.includes('脂肪酶') || diseaseName.includes('LIPA')) {
    return 'LIPA'
  }
  if (diseaseName.includes('糖原') || diseaseName.includes('G6PC')) {
    return 'G6PC'
  }
  if (diseaseName.includes('血色')) {
    return diseaseName.includes('青年型') ? 'HJV' : 'HFE'
  }
  if (diseaseName.includes('抗胰蛋白酶')) {
    return 'SERPINA1'
  }
  if (diseaseName.includes('Gilbert') || diseaseName.includes('吉尔伯特')) {
    return 'UGT1A1'
  }
  return 'ATP7B'
}

const severityFromIndicator = (indicator) => {
  if (indicator.status === 'exception') {
    return '高'
  }
  if (indicator.status === 'warning') {
    return '中'
  }
  return ''
}

const directionFromIndicator = (indicator) => {
  const name = String(indicator.name || '')
  if (name === 'GLU' && Number(indicator.value) < 3.9) {
    return 'low'
  }
  return /铜蓝蛋白|AAT|抗胰蛋白酶/.test(name) ? 'low' : 'high'
}

const buildClinicalAbnormalities = (diagnosisPayload) => {
  return (diagnosisPayload?.indicators || []).map((indicator) => ({
    feature: indicator.name,
    value: indicator.value,
    unit: indicator.unit,
    normal_range_label: indicator.normal,
    direction: indicator.status ? directionFromIndicator(indicator) : '',
    severity: severityFromIndicator(indicator)
  }))
}

const buildGeneAbnormalities = (diseaseName, record) => {
  const variants = record?.geneticSequencing?.variants || []
  if (variants.length > 0) {
    return variants.map((variant) => ({
      gene: variant.gene,
      c_change: variant.hgvsC,
      p_change: variant.hgvsP
    }))
  }
  return [{ gene: inferGeneByDisease(diseaseName) }]
}

const buildInference = (report, record) => {
  const probability = parseProbability(report.aiFindings?.probability)
  const diseaseName = String(report.aiFindings?.disease || '')
  const diagnosisPayload = report.diagnosisPayload || null
  const indicators = diagnosisPayload?.indicators || []
  const confidence = diagnosisPayload?.confidence || resolveDiagnosisConfidence(probability)
  const evidenceItems = Array.isArray(diagnosisPayload?.evidenceItems) && diagnosisPayload.evidenceItems.length > 0
    ? diagnosisPayload.evidenceItems
    : buildEvidenceItemsFromDiagnosis({
        diseaseName,
        indicators,
        record,
        genes: diagnosisPayload?.genes || []
      })
  const evidenceSummary = diagnosisPayload?.evidenceSummary ||
    buildEvidenceSummary(evidenceItems, confidence, DEFAULT_MODEL_FEATURE_COUNT)

  return {
    risk_probability: probability,
    model_feature_count: evidenceSummary.modelFeatureCount,
    abnormal_evidence_count: evidenceSummary.abnormalEvidenceCount,
    evidence_items: evidenceItems,
    key_signs: buildKeySignsFromEvidenceItems(evidenceItems),
    suggestions: [
      report.expertConclusion || '建议结合病史、查体和关键化验指标综合判断。',
      report.treatmentPlan || '建议建立长期随访并动态评估病情变化。'
    ],
    clinical_abnormalities: buildClinicalAbnormalities(diagnosisPayload),
    gene_abnormalities: buildGeneAbnormalities(diseaseName, record)
  }
}

const buildRecommendations = (report) => [
  {
    recType: 'DIET',
    content: report.treatmentPlan || '建议低负担、均衡饮食，并避免风险食材。'
  },
  {
    recType: 'GENETIC',
    content: `建议结合 ${report.aiFindings?.disease || '临床线索'} 进行针对性基因检测。`
  }
]

const buildFeedbacks = (report, doctorId) => {
  if (report.feedbackAction && report.feedbackCreatedAt) {
    return [
      {
        doctorId,
        action: report.feedbackAction,
        modifiedValue: report.feedbackModifiedValue || {
          expertConclusion: report.expertConclusion || '',
          treatmentPlan: report.treatmentPlan || ''
        },
        createdAt: report.feedbackCreatedAt
      }
    ]
  }

  if (report.status !== '已签发') {
    return []
  }

  return [
    {
      doctorId,
      action: 'MODIFY',
      modifiedValue: {
        expertConclusion: report.expertConclusion || '',
        treatmentPlan: report.treatmentPlan || ''
      },
      createdAt: report.signedAt || toIsoDayEnd(report.date)
    }
  ]
}

const buildSessionFromReport = (report, patient, doctorIdOverride) => {
  const sessionId = buildSessionId(report)
  const probability = parseProbability(report.aiFindings?.probability)
  const diseaseName = String(report.aiFindings?.disease || '遗传代谢性肝病风险提示')
  const doctorId = parsePositiveInt(doctorIdOverride, parsePositiveInt(report.doctorId, DEFAULT_DOCTOR_ID))
  const feedbacks = buildFeedbacks(report, doctorId)
  const record = findRecordPayloadByPatientNo(report.patientId || patient?.id)

  return {
    id: sessionId,
    patientId: toPatientNumericId(report.patientId) || toPatientNumericId(patient?.id),
    encounterId: toEncounterId(report.visitId),
    doctorId,
    modelRegistryId: parsePositiveInt(report.modelRegistryId, DEFAULT_MODEL_REGISTRY_ID),
    status: feedbacks.length > 0 ? 'REVIEWED' : 'COMPLETED',
    startedAt: toIsoDayStart(report.date),
    completedAt: report.signedAt || toIsoDayEnd(report.date),
    results: [
      {
        id: buildResultId(sessionId),
        diseaseCode: inferDiseaseCode(diseaseName),
        diseaseName,
        confidence: probability,
        rankNo: 1,
        riskLevel: inferRiskLevel(probability),
        evidenceJson: {
          inference: buildInference(report, record)
        }
      }
    ],
    recommendations: buildRecommendations(report),
    feedbacks
  }
}

const buildSessions = () => {
  const patients = loadPatients()
  const patientMap = new Map(patients.map((item) => [item.id, item]))

  return loadReports().map((report) => {
    const patient = patientMap.get(report.patientId)
    return buildSessionFromReport(report, patient)
  })
}

const sortSessionsByTime = (sessions) => {
  return [...sessions].sort((left, right) => {
    const leftTime = new Date(left.completedAt || left.startedAt || 0).getTime()
    const rightTime = new Date(right.completedAt || right.startedAt || 0).getTime()
    return rightTime - leftTime
  })
}

const upsertDiagnosisReport = (patient, diagnosisPayload, doctorId = DEFAULT_DOCTOR_ID) => {
  const reports = loadReports()
  const existed = reports.find((item) => item.patientId === patient.id && item.status === '待签发')

  if (existed) {
    existed.patientName = patient.name
    existed.gender = patient.gender
    existed.age = patient.age
    existed.date = new Date().toISOString().slice(0, 10)
    existed.aiFindings = toAiFinding(diagnosisPayload)
    existed.diagnosisPayload = diagnosisPayload
    existed.doctorId = doctorId
    saveReports(reports)
    return existed
  }

  const reportId = `REP-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(Math.floor(Math.random() * 900) + 100)}`
  const created = {
    id: reportId,
    patientId: patient.id,
    visitId: `MZ${Math.floor(Math.random() * 9000000 + 1000000)}`,
    patientName: patient.name,
    gender: patient.gender,
    age: patient.age,
    date: new Date().toISOString().slice(0, 10),
    status: '待签发',
    aiFindings: toAiFinding(diagnosisPayload),
    diagnosisPayload,
    expertConclusion: '',
    treatmentPlan: '',
    doctorId
  }
  reports.unshift(created)
  saveReports(reports)
  return created
}

export const diagnosisExactHandlers = {
  'GET /api/v1/web/identity/patients': async ({ query }) => {
    const page = Math.max(parsePositiveInt(query.page), 0)
    const size = clampPageSize(query.size)
    const start = page * size

    const all = loadPatients().map((item) => ({
      id: toPatientNumericId(item.id),
      patientName: item.name,
      gender: item.gender,
      birthDate: toBirthDateByAge(item.age)
    }))

    return {
      status: 200,
      data: successEnvelope({
        page,
        size,
        total: all.length,
        items: all.slice(start, start + size)
      })
    }
  },

  'GET /api/v1/web/diagnoses/sessions': async ({ query }) => {
    const page = Math.max(parsePositiveInt(query.page), 0)
    const size = clampPageSize(query.size)
    const start = page * size
    const sessions = sortSessionsByTime(buildSessions())

    return {
      status: 200,
      data: successEnvelope({
        page,
        size,
        total: sessions.length,
        items: sessions.slice(start, start + size)
      })
    }
  },

  'POST /api/v1/web/diagnoses/sessions': async ({ data }) => {
    const numericPatientId = parsePositiveInt(data.patientId)
    if (!numericPatientId) {
      return failEnvelope(400, 'patientId 非法')
    }

    const patients = loadPatients()
    const patient = patients.find((item) => toPatientNumericId(item.id) === numericPatientId)
    if (!patient) {
      return failEnvelope(404, '患者不存在')
    }

    await wait(AI_DIAGNOSIS_EXTRA_DELAY_MS)

    const diagnosisPayload = await buildMockDiagnosisPayload(patient)

    const doctorId = parsePositiveInt(data.doctorId, DEFAULT_DOCTOR_ID)
    const report = upsertDiagnosisReport(patient, diagnosisPayload, doctorId)
    const session = buildSessionFromReport(report, patient, doctorId)

    return {
      status: 200,
      data: successEnvelope(session)
    }
  },

  'POST /api/v1/web/diagnoses/feedbacks': async ({ data }) => {
    const sessionId = parsePositiveInt(data.sessionId)
    if (!sessionId) {
      return failEnvelope(400, 'sessionId 非法')
    }

    const reports = loadReports()
    const report = reports.find((item) => buildSessionId(item) === sessionId)
    if (!report) {
      return failEnvelope(404, '诊断会话不存在')
    }

    const doctorId = parsePositiveInt(data.doctorId, parsePositiveInt(report.doctorId, DEFAULT_DOCTOR_ID))
    const action = String(data.action || 'MODIFY').toUpperCase()
    const modifiedValue = data.modifiedValue && typeof data.modifiedValue === 'object'
      ? data.modifiedValue
      : {}

    if (typeof modifiedValue.expertConclusion === 'string') {
      report.expertConclusion = modifiedValue.expertConclusion
    }
    if (typeof modifiedValue.treatmentPlan === 'string') {
      report.treatmentPlan = modifiedValue.treatmentPlan
    }

    if (action === 'MODIFY' || action === 'ACCEPT') {
      report.status = '已签发'
      report.signedAt = new Date().toISOString()
    }

    report.doctorId = doctorId
    report.feedbackAction = action
    report.feedbackModifiedValue = modifiedValue
    report.feedbackCreatedAt = new Date().toISOString()
    saveReports(reports)

    const patient = loadPatients().find((item) => item.id === report.patientId)
    const session = buildSessionFromReport(report, patient, doctorId)

    return {
      status: 200,
      data: successEnvelope(session)
    }
  },

  'GET /api/v1/web/diagnosis/ai-queue/': async () => {
    const reportedPatientIds = new Set(loadReports().map((report) => String(report.patientId || '')))
    const items = loadPatients().map((item) => ({
      id: item.id,
      name: item.name,
      gender: item.gender,
      age: item.age,
      avatar: '',
      aiStatus: reportedPatientIds.has(item.id) ? '已诊断' : '未诊断'
    }))

    return { status: 200, data: { items } }
  },

  'POST /api/v1/web/diagnosis/ai-reports/': async ({ data }) => {
    const patientId = String(data.patientId || '')
    const numericPatientId = parsePositiveInt(data.patientId)
    const patients = loadPatients()
    const patient = patients.find(
      (item) => item.id === patientId || toPatientNumericId(item.id) === numericPatientId
    )

    if (!patient) {
      return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
    }

    await wait(AI_DIAGNOSIS_EXTRA_DELAY_MS)

    const diagnosisPayload = await buildMockDiagnosisPayload(patient)

    upsertDiagnosisReport(patient, diagnosisPayload)

    return { status: 200, data: diagnosisPayload }
  },

  'GET /api/v1/web/diagnosis/expert-reports/': async () => {
    return { status: 200, data: { items: loadReports() } }
  }
}

export const diagnosisDynamicHandlers = [
  {
    method: 'GET',
    pattern: /^\/api\/v1\/web\/diagnoses\/sessions\/([^/]+)$/,
    buildParams: (match) => ({ sessionId: match[1] }),
    handler: async ({ params }) => {
      const sessionId = parsePositiveInt(params.sessionId)
      if (!sessionId) {
        return failEnvelope(400, 'sessionId 非法')
      }

      const session = buildSessions().find((item) => item.id === sessionId)
      if (!session) {
        return failEnvelope(404, '诊断会话不存在')
      }

      return {
        status: 200,
        data: successEnvelope(session)
      }
    }
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/web\/diagnosis\/expert-reports\/([^/]+)\/sign\/$/,
    buildParams: (match) => ({ reportId: match[1] }),
    handler: async ({ params, data }) => {
      const reports = loadReports()
      const report = reports.find((item) => item.id === params.reportId)
      if (!report) {
        return { status: 404, statusText: 'Not Found', data: { detail: '报告不存在' } }
      }

      if (!data.expertConclusion || !data.treatmentPlan) {
        return { status: 400, statusText: 'Bad Request', data: { detail: '签发前请完善专家结论和干预计划' } }
      }

      report.status = '已签发'
      report.expertConclusion = data.expertConclusion
      report.treatmentPlan = data.treatmentPlan
      report.signedAt = new Date().toISOString()
      saveReports(reports)

      return { status: 200, data: { report } }
    }
  }
]

export const diagnosisRouteDocs = [
  {
    module: 'diagnosis',
    method: 'GET',
    path: '/api/v1/web/identity/patients',
    kind: 'exact',
    description: '诊断工作台患者列表（新版契约）。'
  },
  {
    module: 'diagnosis',
    method: 'GET',
    path: '/api/v1/web/diagnoses/sessions',
    kind: 'exact',
    description: '诊断会话分页列表（新版契约）。'
  },
  {
    module: 'diagnosis',
    method: 'GET',
    path: '/api/v1/web/diagnoses/sessions/:sessionId',
    kind: 'dynamic',
    description: '诊断会话详情（新版契约）。'
  },
  {
    module: 'diagnosis',
    method: 'POST',
    path: '/api/v1/web/diagnoses/sessions',
    kind: 'exact',
    description: '启动诊断会话并落库（新版契约）。'
  },
  {
    module: 'diagnosis',
    method: 'POST',
    path: '/api/v1/web/diagnoses/feedbacks',
    kind: 'exact',
    description: '医生反馈/签发（新版契约）。'
  },
  {
    module: 'diagnosis',
    method: 'GET',
    path: '/api/v1/web/diagnosis/ai-queue/',
    kind: 'exact',
    description: 'AI 诊断队列。'
  },
  {
    module: 'diagnosis',
    method: 'POST',
    path: '/api/v1/web/diagnosis/ai-reports/',
    kind: 'exact',
    description: '触发 AI 诊断并返回诊断详情。'
  },
  {
    module: 'diagnosis',
    method: 'GET',
    path: '/api/v1/web/diagnosis/expert-reports/',
    kind: 'exact',
    description: '专家报告列表。'
  },
  {
    module: 'diagnosis',
    method: 'POST',
    path: '/api/v1/web/diagnosis/expert-reports/:reportId/sign/',
    kind: 'dynamic',
    description: '签发专家报告。'
  }
]
