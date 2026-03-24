// @ts-nocheck

import {
  buildDiagnosisPayload,
  loadPatients,
  loadReports,
  savePatients,
  saveReports,
  toAiFinding
} from '../core/mockState'

export const diagnosisExactHandlers = {
  'GET /api/v1/diagnosis/ai-queue/': async () => {
    const items = loadPatients().map((item) => ({
      id: item.id,
      name: item.name,
      gender: item.gender,
      age: item.age,
      avatar: item.avatar,
      aiStatus: item.aiStatus || '未诊断'
    }))

    return { status: 200, data: { items } }
  },

  'POST /api/v1/diagnosis/ai-reports/': async ({ data }) => {
    const patientId = data.patientId
    const patients = loadPatients()
    const patient = patients.find((item) => item.id === patientId)

    if (!patient) {
      return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
    }

    const diagnosisPayload = buildDiagnosisPayload(patient)
    patient.aiStatus = '已诊断'
    savePatients(patients)

    const reports = loadReports()
    const existed = reports.find((item) => item.patientId === patient.id && item.status === '待签发')
    if (!existed) {
      const reportId = `REP-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(Math.floor(Math.random() * 900) + 100)}`
      reports.unshift({
        id: reportId,
        patientId: patient.id,
        visitId: `MZ${Math.floor(Math.random() * 9000000 + 1000000)}`,
        patientName: patient.name,
        gender: patient.gender,
        age: patient.age,
        date: new Date().toISOString().slice(0, 10),
        status: '待签发',
        aiFindings: toAiFinding(diagnosisPayload),
        expertConclusion: '',
        treatmentPlan: ''
      })
      saveReports(reports)
    }

    return { status: 200, data: diagnosisPayload }
  },

  'GET /api/v1/diagnosis/expert-reports/': async () => {
    return { status: 200, data: { items: loadReports() } }
  }
}

export const diagnosisDynamicHandlers = [
  {
    method: 'POST',
    pattern: /^\/api\/v1\/diagnosis\/expert-reports\/([^/]+)\/sign\/$/,
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
    path: '/api/v1/diagnosis/ai-queue/',
    kind: 'exact',
    description: 'AI 诊断队列。'
  },
  {
    module: 'diagnosis',
    method: 'POST',
    path: '/api/v1/diagnosis/ai-reports/',
    kind: 'exact',
    description: '触发 AI 诊断并返回诊断详情。'
  },
  {
    module: 'diagnosis',
    method: 'GET',
    path: '/api/v1/diagnosis/expert-reports/',
    kind: 'exact',
    description: '专家报告列表。'
  },
  {
    module: 'diagnosis',
    method: 'POST',
    path: '/api/v1/diagnosis/expert-reports/:reportId/sign/',
    kind: 'dynamic',
    description: '签发专家报告。'
  }
]
