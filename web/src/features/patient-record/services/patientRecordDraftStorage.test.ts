// @ts-nocheck

import { afterEach, beforeEach, describe, expect, test } from 'bun:test'
import {
  LEGACY_PATIENT_RECORD_DRAFT_KEY,
  PATIENT_RECORD_DRAFT_LIMIT,
  deletePatientRecordDraft,
  listPatientRecordDraftSummaries,
  loadPatientRecordDrafts,
  migrateLegacyPatientRecordDraft,
  savePatientRecordDraft
} from './patientRecordDraftStorage'

const originalLocalStorage = globalThis.localStorage
const originalWindow = globalThis.window
const originalTenantId = import.meta.env.VITE_TENANT_ID

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
  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    writable: true,
    value: localStorage
  })
  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    writable: true,
    value: { localStorage }
  })
  return { localStorage }
}

beforeEach(() => {
  installBrowserStorage()
  import.meta.env.VITE_TENANT_ID = '1'
})

afterEach(() => {
  if (originalTenantId === undefined) {
    delete import.meta.env.VITE_TENANT_ID
  } else {
    import.meta.env.VITE_TENANT_ID = originalTenantId
  }

  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    writable: true,
    value: originalLocalStorage
  })
  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    writable: true,
    value: originalWindow
  })
})

const makeSnapshot = (patientNo, name) => ({
  visitId: `VISIT-${patientNo}`,
  patientNo,
  name,
  importMeta: {
    sourceType: '',
    traceId: '',
    confidence: null,
    importedAt: ''
  },
  clinicalDecision: {
    diagnosis: ''
  }
})

describe('patient record draft storage', () => {
  test('migrates the legacy single draft into tenant-scoped draft storage', () => {
    localStorage.setItem(LEGACY_PATIENT_RECORD_DRAFT_KEY, JSON.stringify(makeSnapshot('P001', '张三')))

    const migrated = migrateLegacyPatientRecordDraft()

    expect(migrated?.patientNo).toBe('P001')
    expect(localStorage.getItem(LEGACY_PATIENT_RECORD_DRAFT_KEY)).toBeNull()
    expect(loadPatientRecordDrafts()).toHaveLength(1)
    expect(listPatientRecordDraftSummaries()[0].name).toBe('张三')
  })

  test('keeps drafts isolated by tenant id', () => {
    localStorage.setItem('tenantId', '10')
    savePatientRecordDraft(makeSnapshot('P010', '租户十'))

    localStorage.setItem('tenantId', '20')
    savePatientRecordDraft(makeSnapshot('P020', '租户二十'))

    expect(loadPatientRecordDrafts()[0].patientNo).toBe('P020')

    localStorage.setItem('tenantId', '10')
    expect(loadPatientRecordDrafts()[0].patientNo).toBe('P010')
  })

  test('updates an existing draft instead of creating duplicates', () => {
    const first = savePatientRecordDraft(makeSnapshot('P002', '李四'))
    const second = savePatientRecordDraft(makeSnapshot('P002', '李四复诊'), first.draft.id)

    const drafts = loadPatientRecordDrafts()

    expect(first.isNew).toBe(true)
    expect(second.isNew).toBe(false)
    expect(drafts).toHaveLength(1)
    expect(drafts[0].name).toBe('李四复诊')
  })

  test('deletes a draft by id', () => {
    const first = savePatientRecordDraft(makeSnapshot('P003', '王五'))
    savePatientRecordDraft(makeSnapshot('P004', '赵六'))

    deletePatientRecordDraft(first.draft.id)

    const drafts = loadPatientRecordDrafts()
    expect(drafts).toHaveLength(1)
    expect(drafts[0].patientNo).toBe('P004')
  })

  test('rejects new drafts when the tenant draft limit is reached', () => {
    for (let i = 0; i < PATIENT_RECORD_DRAFT_LIMIT; i += 1) {
      savePatientRecordDraft(makeSnapshot(`P${i}`, `患者${i}`))
    }

    expect(() => savePatientRecordDraft(makeSnapshot('P999', '超限患者'))).toThrow('PATIENT_RECORD_DRAFT_LIMIT_REACHED')
    expect(loadPatientRecordDrafts()).toHaveLength(PATIENT_RECORD_DRAFT_LIMIT)
  })
})
