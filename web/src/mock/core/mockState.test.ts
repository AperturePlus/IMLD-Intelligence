// @ts-nocheck

import { afterEach, beforeEach, describe, expect, test } from 'bun:test'
import { loadDietOverrides, loadPatients, loadRecords, loadReports, saveReports } from './mockState'

const MOCK_REPORTS_KEY = '__imld_mock_reports__'
const MOCK_PATIENTS_KEY = '__imld_mock_patients__'
const MOCK_RECORDS_KEY = '__imld_mock_records__'
const MOCK_DIET_OVERRIDES_KEY = '__imld_mock_diet_overrides__'
const originalPersistFlag = import.meta.env.VITE_MOCK_DIAGNOSIS_PERSIST
const originalWindow = globalThis.window

class MemoryStorage {
  values = new Map()

  get length() {
    return this.values.size
  }

  clear() {
    this.values.clear()
  }

  getItem(key) {
    return this.values.has(key) ? this.values.get(key) : null
  }

  key(index) {
    return Array.from(this.values.keys())[index] ?? null
  }

  removeItem(key) {
    this.values.delete(key)
  }

  setItem(key, value) {
    this.values.set(key, String(value))
  }
}

const installBrowserStorage = () => {
  const localStorage = new MemoryStorage()
  const sessionStorage = new MemoryStorage()
  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    writable: true,
    value: {
      localStorage,
      sessionStorage
    }
  })
  return { localStorage, sessionStorage }
}

beforeEach(() => {
  import.meta.env.VITE_MOCK_DIAGNOSIS_PERSIST = 'false'
})

afterEach(() => {
  if (originalPersistFlag === undefined) {
    delete import.meta.env.VITE_MOCK_DIAGNOSIS_PERSIST
  } else {
    import.meta.env.VITE_MOCK_DIAGNOSIS_PERSIST = originalPersistFlag
  }

  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    writable: true,
    value: originalWindow
  })
})

describe('mock diagnosis report storage', () => {
  test('defaults diagnosis reports to session storage', () => {
    const { localStorage, sessionStorage } = installBrowserStorage()

    const reports = loadReports()
    expect(reports.length).toBeGreaterThan(0)
    expect(sessionStorage.getItem(MOCK_REPORTS_KEY)).not.toBeNull()
    expect(localStorage.getItem(MOCK_REPORTS_KEY)).toBeNull()

    saveReports([{ ...reports[0], id: 'REP-SESSION-TEST' }])

    expect(loadReports()[0].id).toBe('REP-SESSION-TEST')
    expect(JSON.parse(sessionStorage.getItem(MOCK_REPORTS_KEY))[0].id).toBe('REP-SESSION-TEST')
    expect(localStorage.getItem(MOCK_REPORTS_KEY)).toBeNull()
  })

  test('uses local storage when diagnosis persistence is enabled', () => {
    import.meta.env.VITE_MOCK_DIAGNOSIS_PERSIST = 'true'
    const { localStorage, sessionStorage } = installBrowserStorage()

    const reports = loadReports()
    expect(reports.length).toBeGreaterThan(0)
    expect(localStorage.getItem(MOCK_REPORTS_KEY)).not.toBeNull()
    expect(sessionStorage.getItem(MOCK_REPORTS_KEY)).toBeNull()

    saveReports([{ ...reports[0], id: 'REP-LOCAL-TEST' }])

    expect(loadReports()[0].id).toBe('REP-LOCAL-TEST')
    expect(JSON.parse(localStorage.getItem(MOCK_REPORTS_KEY))[0].id).toBe('REP-LOCAL-TEST')
    expect(sessionStorage.getItem(MOCK_REPORTS_KEY)).toBeNull()
  })

  test('falls back to seeded reports when stored diagnosis data is corrupted', () => {
    const { sessionStorage } = installBrowserStorage()
    sessionStorage.setItem(MOCK_REPORTS_KEY, '{broken-json')

    const reports = loadReports()

    expect(reports.length).toBeGreaterThan(0)
    expect(reports[0].id).toBe('REP-202311-001')
    expect(JSON.parse(sessionStorage.getItem(MOCK_REPORTS_KEY))[0].id).toBe('REP-202311-001')
  })

  test('hydrates fixed seed reports with structured biochemical indicators', () => {
    const { sessionStorage } = installBrowserStorage()
    sessionStorage.setItem(MOCK_REPORTS_KEY, JSON.stringify([
      {
        id: 'REP-202311-001',
        patientId: 'P001',
        aiFindings: {
          biochemical: '旧版铁代谢摘要',
          probability: '89',
          disease: '遗传性血色病'
        }
      },
      {
        id: 'REP-202311-002',
        patientId: 'P002',
        aiFindings: {
          biochemical: '旧版铜代谢摘要',
          probability: '96',
          disease: '肝豆状核变性 (Wilson病)'
        }
      },
      {
        id: 'REP-202311-003',
        patientId: 'P003',
        aiFindings: {
          biochemical: '旧版 AAT 摘要',
          probability: '85',
          disease: 'α1-抗胰蛋白酶缺乏症'
        }
      }
    ]))

    const reports = loadReports()
    const first = reports.find((item) => item.id === 'REP-202311-001')
    const second = reports.find((item) => item.id === 'REP-202311-002')
    const third = reports.find((item) => item.id === 'REP-202311-003')

    expect(first.patientName).toBe('方亦辰')
    expect(first.age).toBe(14)
    expect(first.aiFindings.disease).toBe('肝豆状核变性 (Wilson病)')
    expect(first.diagnosisPayload.indicators).toHaveLength(3)
    expect(first.diagnosisPayload.indicators[0]).toMatchObject({ name: '血清铜蓝蛋白', normal: '0.20-0.60' })
    expect(first.diagnosisPayload.indicators[1]).toMatchObject({ name: 'ALT', value: 126, normal: '9-50' })
    expect(second.patientName).toBe('梁知夏')
    expect(second.age).toBe(8)
    expect(second.aiFindings.disease).toBe('Citrin缺乏症')
    expect(second.diagnosisPayload.indicators[0]).toMatchObject({ name: 'DBIL', unit: 'μmol/L', normal: '0-6.8' })
    expect(second.diagnosisPayload.indicators[1]).toMatchObject({ name: 'GLU', value: 3.2, normal: '3.9-6.1' })
    expect(third.patientName).toBe('何星澜')
    expect(third.age).toBe(6)
    expect(third.aiFindings.disease).toBe('PFIC2/ABCB11')
    expect(third.diagnosisPayload.indicators[1]).toMatchObject({ name: 'TBA', value: 180, normal: '0-10' })
    expect(first.aiFindings.biochemical).toContain('参考 0.20-0.60 / 显著偏离')
    expect(first.aiFindings.biochemical).toContain('mock参考区间')

    const stored = JSON.parse(sessionStorage.getItem(MOCK_REPORTS_KEY))
    expect(stored[0].diagnosisPayload.indicators[0].normal).toBe('0.20-0.60')
    expect(stored[1].aiFindings.biochemical).toContain('参考 3.9-6.1 / 偏离')
  })
})

describe('young rare IMLD mock seed data', () => {
  test('seeds young patients with a broad rare inherited metabolic liver disease spectrum', () => {
    installBrowserStorage()

    const patients = loadPatients()
    const diseases = new Set(patients.map((item) => item.disease))

    expect(patients).toHaveLength(12)
    expect(patients.every((item) => item.age >= 5 && item.age < 30)).toBe(true)
    expect(diseases.size).toBeGreaterThanOrEqual(10)
    for (const disease of [
      '肝豆状核变性 (Wilson病)',
      'Citrin缺乏症',
      'PFIC2/ABCB11',
      'PFIC3/ABCB4',
      'Gilbert综合征',
      'Dubin-Johnson综合征',
      'Alagille综合征',
      'NTCP缺乏症/SLC10A1',
      '溶酶体酸性脂肪酶缺乏症/LIPA',
      '糖原累积病I型/G6PC',
      'α1-抗胰蛋白酶缺乏症',
      '青年型遗传性血色病'
    ]) {
      expect(diseases.has(disease)).toBe(true)
    }
  })

  test('migrates old seeded patient, record and diet caches while preserving custom data', () => {
    const { localStorage } = installBrowserStorage()
    localStorage.setItem(MOCK_PATIENTS_KEY, JSON.stringify([
      { id: 'P001', name: '旧患者', gender: '男', age: 58, disease: '遗传性血色病', aiStatus: '未诊断' },
      { id: 'P099', name: '自定义患者', gender: '女', age: 22, disease: '自定义罕见病', aiStatus: '未诊断' }
    ]))
    localStorage.setItem(MOCK_RECORDS_KEY, JSON.stringify([
      { id: 'REC-P001', payload: { patientNo: 'P001', name: '旧病历', age: 58 } },
      { id: 'REC-P099', payload: { patientNo: 'P099', name: '自定义病历', age: 22 } }
    ]))
    localStorage.setItem(MOCK_DIET_OVERRIDES_KEY, JSON.stringify({
      P001: [{ time: '早餐', menu: '旧种子覆盖' }],
      P099: [{ time: '早餐', menu: '自定义覆盖' }]
    }))

    const patients = loadPatients()
    const records = loadRecords()
    const dietOverrides = loadDietOverrides()

    expect(patients.find((item) => item.id === 'P001')).toMatchObject({ name: '方亦辰', age: 14 })
    expect(patients.find((item) => item.id === 'P099')).toMatchObject({ name: '自定义患者', age: 22 })
    expect(records.find((item) => item.payload.patientNo === 'P001').payload).toMatchObject({ name: '方亦辰', age: 14 })
    expect(records.find((item) => item.payload.patientNo === 'P099').payload).toMatchObject({ name: '自定义病历', age: 22 })
    expect(dietOverrides.P001).toBeUndefined()
    expect(dietOverrides.P099).toEqual([{ time: '早餐', menu: '自定义覆盖' }])
  })
})
