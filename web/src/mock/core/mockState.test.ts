// @ts-nocheck

import { afterEach, beforeEach, describe, expect, test } from 'bun:test'
import { loadReports, saveReports } from './mockState'

const MOCK_REPORTS_KEY = '__imld_mock_reports__'
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

    expect(first.diagnosisPayload.indicators).toHaveLength(3)
    expect(first.diagnosisPayload.indicators[0]).toMatchObject({ name: '血清铁蛋白', normal: '30-300' })
    expect(first.diagnosisPayload.indicators[2]).toMatchObject({ name: 'ALT', value: 110, normal: '9-50' })
    expect(second.diagnosisPayload.indicators[1]).toMatchObject({ name: '24h尿铜', unit: 'μg/24h', normal: '<100' })
    expect(third.diagnosisPayload.indicators[0]).toMatchObject({ name: 'AAT', value: 0.45, normal: '0.90-2.00' })
    expect(first.aiFindings.biochemical).toContain('参考 30-300 / 显著偏离')
    expect(first.aiFindings.biochemical).toContain('mock参考区间')

    const stored = JSON.parse(sessionStorage.getItem(MOCK_REPORTS_KEY))
    expect(stored[0].diagnosisPayload.indicators[0].normal).toBe('30-300')
    expect(stored[1].aiFindings.biochemical).toContain('参考 <100 / 显著偏离')
  })
})
