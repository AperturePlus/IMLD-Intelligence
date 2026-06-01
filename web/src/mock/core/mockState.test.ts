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
})
