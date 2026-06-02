import { resolveTenantId } from '../../../api/tenant'
import type { PatientRecordImportMeta } from '../../../types/patient'

export const LEGACY_PATIENT_RECORD_DRAFT_KEY = 'imld_patient_record_draft'
export const PATIENT_RECORD_DRAFT_LIMIT = 20

const DRAFTS_STORAGE_KEY_PREFIX = 'imld_patient_record_drafts_v1'

export interface PatientRecordDraftSnapshot extends Record<string, unknown> {
  visitId?: unknown
  importMeta?: unknown
  patientNo?: unknown
  name?: unknown
}

export interface PatientRecordDraftItem {
  id: string
  createdAt: string
  updatedAt: string
  visitId: string
  patientNo: string
  name: string
  importMeta: PatientRecordImportMeta | null
  snapshot: PatientRecordDraftSnapshot
}

export interface PatientRecordDraftSummary {
  id: string
  createdAt: string
  updatedAt: string
  visitId: string
  patientNo: string
  name: string
  importMeta: PatientRecordImportMeta | null
}

export interface SavePatientRecordDraftResult {
  draft: PatientRecordDraftItem
  isNew: boolean
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value)

const normalizeText = (value: unknown): string => (typeof value === 'string' ? value.trim() : '')

const normalizeImportMeta = (value: unknown): PatientRecordImportMeta | null => {
  if (!isRecord(value)) {
    return null
  }

  const sourceType =
    value.sourceType === 'HIS_LIS' || value.sourceType === 'IMAGE_OCR' || value.sourceType === 'PDF_OCR'
      ? value.sourceType
      : ''

  const confidence = typeof value.confidence === 'number' && Number.isFinite(value.confidence)
    ? value.confidence
    : null

  return {
    sourceType,
    traceId: normalizeText(value.traceId),
    confidence,
    importedAt: normalizeText(value.importedAt)
  }
}

const nowIso = (): string => new Date().toISOString()

const createDraftId = (): string =>
  `draft-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`

const getDraftStorageKey = (): string => `${DRAFTS_STORAGE_KEY_PREFIX}:${resolveTenantId()}`

const toDraftSummary = (draft: PatientRecordDraftItem): PatientRecordDraftSummary => ({
  id: draft.id,
  createdAt: draft.createdAt,
  updatedAt: draft.updatedAt,
  visitId: draft.visitId,
  patientNo: draft.patientNo,
  name: draft.name,
  importMeta: draft.importMeta
})

const normalizeDraftItem = (value: unknown): PatientRecordDraftItem | null => {
  if (!isRecord(value) || !isRecord(value.snapshot)) {
    return null
  }

  const snapshot = value.snapshot as PatientRecordDraftSnapshot
  const id = normalizeText(value.id) || createDraftId()
  const createdAt = normalizeText(value.createdAt) || nowIso()
  const updatedAt = normalizeText(value.updatedAt) || createdAt
  const visitId = normalizeText(value.visitId) || normalizeText(snapshot.visitId)
  const patientNo = normalizeText(value.patientNo) || normalizeText(snapshot.patientNo)
  const name = normalizeText(value.name) || normalizeText(snapshot.name)

  return {
    id,
    createdAt,
    updatedAt,
    visitId,
    patientNo,
    name,
    importMeta: normalizeImportMeta(value.importMeta) ?? normalizeImportMeta(snapshot.importMeta),
    snapshot
  }
}

const sortDrafts = (drafts: PatientRecordDraftItem[]): PatientRecordDraftItem[] =>
  [...drafts].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))

const writeDrafts = (drafts: PatientRecordDraftItem[]): void => {
  localStorage.setItem(getDraftStorageKey(), JSON.stringify(sortDrafts(drafts)))
}

export const loadPatientRecordDrafts = (): PatientRecordDraftItem[] => {
  const raw = localStorage.getItem(getDraftStorageKey())
  if (!raw) {
    return []
  }

  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      localStorage.removeItem(getDraftStorageKey())
      return []
    }
    return sortDrafts(parsed.map((item) => normalizeDraftItem(item)).filter((item): item is PatientRecordDraftItem => Boolean(item)))
  } catch {
    localStorage.removeItem(getDraftStorageKey())
    return []
  }
}

export const listPatientRecordDraftSummaries = (): PatientRecordDraftSummary[] =>
  loadPatientRecordDrafts().map(toDraftSummary)

export const getPatientRecordDraft = (draftId: string): PatientRecordDraftItem | null =>
  loadPatientRecordDrafts().find((draft) => draft.id === draftId) ?? null

export const savePatientRecordDraft = (
  snapshot: PatientRecordDraftSnapshot,
  draftId?: string | null
): SavePatientRecordDraftResult => {
  const drafts = loadPatientRecordDrafts()
  const existingIndex = draftId ? drafts.findIndex((draft) => draft.id === draftId) : -1
  const existing = existingIndex >= 0 ? drafts[existingIndex] : null

  if (!existing && drafts.length >= PATIENT_RECORD_DRAFT_LIMIT) {
    throw new Error('PATIENT_RECORD_DRAFT_LIMIT_REACHED')
  }

  const timestamp = nowIso()
  const draft: PatientRecordDraftItem = {
    id: existing?.id ?? createDraftId(),
    createdAt: existing?.createdAt ?? timestamp,
    updatedAt: timestamp,
    visitId: normalizeText(snapshot.visitId),
    patientNo: normalizeText(snapshot.patientNo),
    name: normalizeText(snapshot.name),
    importMeta: normalizeImportMeta(snapshot.importMeta),
    snapshot
  }

  const nextDrafts = existing
    ? drafts.map((item) => (item.id === existing.id ? draft : item))
    : [draft, ...drafts]

  writeDrafts(nextDrafts)
  return { draft, isNew: !existing }
}

export const deletePatientRecordDraft = (draftId: string): void => {
  writeDrafts(loadPatientRecordDrafts().filter((draft) => draft.id !== draftId))
}

export const migrateLegacyPatientRecordDraft = (): PatientRecordDraftItem | null => {
  const legacyRaw = localStorage.getItem(LEGACY_PATIENT_RECORD_DRAFT_KEY)
  if (!legacyRaw) {
    return null
  }

  try {
    const parsed = JSON.parse(legacyRaw)
    if (!isRecord(parsed)) {
      localStorage.removeItem(LEGACY_PATIENT_RECORD_DRAFT_KEY)
      return null
    }

    try {
      const result = savePatientRecordDraft(parsed as PatientRecordDraftSnapshot)
      localStorage.removeItem(LEGACY_PATIENT_RECORD_DRAFT_KEY)
      return result.draft
    } catch (error) {
      if (error instanceof Error && error.message === 'PATIENT_RECORD_DRAFT_LIMIT_REACHED') {
        return null
      }
      localStorage.removeItem(LEGACY_PATIENT_RECORD_DRAFT_KEY)
      return null
    }
  } catch {
    localStorage.removeItem(LEGACY_PATIENT_RECORD_DRAFT_KEY)
    return null
  }
}
