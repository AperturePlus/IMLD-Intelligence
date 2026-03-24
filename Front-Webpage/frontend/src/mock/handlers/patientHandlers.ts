// @ts-nocheck

import {
  loadPatients,
  loadRecords,
  nextPatientId,
  resolveDiseaseByDiagnosis,
  resolveRiskByDiagnosis,
  savePatients,
  saveRecords
} from '../core/mockState'

export const patientExactHandlers = {
  'GET /api/v1/patients/': async ({ query }) => {
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

  'POST /api/v1/patient-records/': async ({ data }) => {
    const requiredFields = ['name', 'gender', 'age', 'visitDate', 'chiefComplaint', 'diagnosis']
    const errors = {}

    requiredFields.forEach((field) => {
      if (!data[field]) {
        errors[field] = [`${field}不能为空`]
      }
    })

    if (Object.keys(errors).length > 0) {
      return { status: 400, statusText: 'Bad Request', data: errors }
    }

    const records = loadRecords()
    const patients = loadPatients()
    const now = Date.now()
    const recordId = `REC-${now}`
    const visitId = data.visitId || `VISIT-${String(now).slice(-8)}`

    records.push({
      id: recordId,
      visitId,
      submittedAt: new Date().toISOString(),
      payload: data
    })
    saveRecords(records)

    const existing = patients.find((item) => item.name === data.name)
    if (!existing) {
      const disease = resolveDiseaseByDiagnosis(data.diagnosis)
      patients.unshift({
        id: nextPatientId(patients),
        name: data.name,
        gender: data.gender,
        age: Number(data.age) || 0,
        riskLevel: resolveRiskByDiagnosis(data.diagnosis),
        disease,
        compliance: '一般',
        aiStatus: '未诊断',
        avatar: `https://randomuser.me/api/portraits/${data.gender === '女' ? 'women' : 'men'}/${Math.floor(Math.random() * 80) + 10}.jpg`
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
  }
}

export const patientDynamicHandlers = []

export const patientRouteDocs = [
  {
    module: 'patient',
    method: 'GET',
    path: '/api/v1/patients/',
    kind: 'exact',
    description: '患者列表查询（支持 keyword）。'
  },
  {
    module: 'patient',
    method: 'POST',
    path: '/api/v1/patient-records/',
    kind: 'exact',
    description: '创建病历并在必要时写入新患者。'
  }
]
