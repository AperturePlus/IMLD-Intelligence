// @ts-nocheck

import {
  DISEASE_CONFIGS,
  loadDietOverrides,
  loadPatients,
  saveDietOverrides
} from '../core/mockState'

export const dietExactHandlers = {
  'GET /api/v1/diet/patients/': async ({ query }) => {
    const keyword = (query.keyword || '').trim()
    const items = loadPatients()
      .filter((item) => !keyword || item.name.includes(keyword) || item.id.includes(keyword))
      .map((item) => ({
        id: item.id,
        name: item.name,
        gender: item.gender,
        age: item.age,
        avatar: item.avatar,
        disease: item.disease,
        compliance: item.compliance || '一般'
      }))

    return { status: 200, data: { items } }
  }
}

export const dietDynamicHandlers = [
  {
    method: 'GET',
    pattern: /^\/api\/v1\/diet\/patients\/([^/]+)\/plan\/$/,
    buildParams: (match) => ({ patientId: match[1] }),
    handler: async ({ params }) => {
      const patient = loadPatients().find((item) => item.id === params.patientId)
      if (!patient) {
        return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
      }

      const overrides = loadDietOverrides()
      const config = DISEASE_CONFIGS[patient.disease] || DISEASE_CONFIGS['肝豆状核变性 (Wilson病)']

      return {
        status: 200,
        data: {
          targets: config.targets,
          foods: config.foods,
          mealPlan: overrides[patient.id] || config.mealPlan
        }
      }
    }
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/diet\/patients\/([^/]+)\/regenerate\/$/,
    buildParams: (match) => ({ patientId: match[1] }),
    handler: async ({ params }) => {
      const patient = loadPatients().find((item) => item.id === params.patientId)
      if (!patient) {
        return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
      }

      const config = DISEASE_CONFIGS[patient.disease] || DISEASE_CONFIGS['肝豆状核变性 (Wilson病)']
      const overrides = loadDietOverrides()
      const basePlan = overrides[patient.id] || config.mealPlan
      const nextPlan = [...basePlan.slice(1), basePlan[0]].map((item, index) => ({
        ...item,
        nutrition: `${item.nutrition} · 方案版本 ${index + 1}`
      }))

      overrides[patient.id] = nextPlan
      saveDietOverrides(overrides)

      return {
        status: 200,
        data: {
          mealPlan: nextPlan,
          regeneratedAt: new Date().toISOString()
        }
      }
    }
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/diet\/patients\/([^/]+)\/push\/$/,
    buildParams: (match) => ({ patientId: match[1] }),
    handler: async ({ params }) => {
      const patient = loadPatients().find((item) => item.id === params.patientId)
      if (!patient) {
        return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
      }

      return {
        status: 200,
        data: {
          delivered: true,
          patientId: patient.id,
          deliveredAt: new Date().toISOString()
        }
      }
    }
  }
]

export const dietRouteDocs = [
  {
    module: 'diet',
    method: 'GET',
    path: '/api/v1/diet/patients/',
    kind: 'exact',
    description: '饮食干预患者列表。'
  },
  {
    module: 'diet',
    method: 'GET',
    path: '/api/v1/diet/patients/:patientId/plan/',
    kind: 'dynamic',
    description: '获取患者饮食方案。'
  },
  {
    module: 'diet',
    method: 'POST',
    path: '/api/v1/diet/patients/:patientId/regenerate/',
    kind: 'dynamic',
    description: '重生成饮食方案。'
  },
  {
    module: 'diet',
    method: 'POST',
    path: '/api/v1/diet/patients/:patientId/push/',
    kind: 'dynamic',
    description: '推送饮食方案给患者。'
  }
]
