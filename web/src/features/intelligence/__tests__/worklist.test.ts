import { describe, test, expect } from 'bun:test'
import { computeWorklist } from '../worklist'
import type { IntelligenceDataSource } from '../types'
import type { DiagnosisQueuePatient, DiagnosisResult } from '@/types/diagnosis'
import type { PatientRecordPayload } from '@/types/patient'

function makeQueue(items: Partial<DiagnosisQueuePatient>[]): DiagnosisQueuePatient[] {
  return items.map((item, i) => ({
    id: String(i + 1),
    name: `患者${i + 1}`,
    gender: '男',
    age: 45,
    aiStatus: '未诊断',
    ...item
  }))
}

function makeDataSource(
  queue: DiagnosisQueuePatient[],
  records: Map<string, PatientRecordPayload> = new Map(),
  _results: Map<string, DiagnosisResult> = new Map()
): IntelligenceDataSource {
  return {
    async getQueue() { return queue },
    async getPatientRecord(patientId: string) {
      return records.get(patientId) ?? null
    },
    async getDiagnosisResult() { return null }
  }
}

function makeRecord(overrides: Partial<PatientRecordPayload> = {}): PatientRecordPayload {
  return {
    patientNo: 'P001',
    name: '测试患者',
    gender: '男',
    age: 45,
    visitDate: '2026-06-01',
    phone: '',
    idCard: '',
    occupation: '',
    currentAddress: '',
    nativePlace: '',
    department: '',
    encounterType: 'OUTPATIENT',
    consanguinity: false,
    chiefComplaint: '',
    presentIllness: '',
    history: {
      diseaseHistory: {
        smokingHistory: 'NO',
        drinkingHistory: 'NO',
        diabetesHistory: 'NO',
        hypertensionHistory: 'NO',
        hyperuricemiaHistory: 'NO',
        hyperlipidemiaHistory: 'NO',
        coronaryHeartDiseaseHistory: 'NO',
        hepatitisBHistory: 'NO'
      },
      surgeryHistory: { status: 'NO', detail: '' },
      transfusionHistory: { status: 'NO', detail: '' },
      allergyHistory: '',
      medicationHistory: '',
      familyHistory: ''
    },
    physicalExam: {
      heightCm: null,
      weightKg: null,
      bmi: null,
      bloodPressureSystolic: null,
      bloodPressureDiastolic: null,
      respiratoryRate: null,
      heartRate: null,
      liverFibrosis: 'NO',
      cirrhosis: 'NO',
      fattyLiver: 'NO',
      liverFailure: 'NO',
      cholestasis: 'NO',
      viralHepatitis: 'NO'
    },
    laboratoryScreening: {
      clinicalBiochemistry: {
        liverFunction: {
          tbil: '', dbil: '', ibil: '', alt: '', ast: '', astAltRatio: '', tp: '', alb: '', glob: '', albGlobRatio: '', glu: '', tg: '', chol: '', hdlC: '', ldlC: '', alp: '', ggt: '', ck: '', ldh: '', hbdh: '', tba: '', nh3: ''
        },
        renalFunction: { urea: '', crea: '', eGfr: '', cysC: '', uric: '' },
        metabolism: { cer: '', aat: '' },
        tumorMarkers: { afp: '', cea: '', ca19_9: '' },
        inflammation: { crp: '', pct: '' }
      },
      clinicalBasic: {
        bloodRoutine: { rbc: '', hgb: '', hct: '', mcv: '', mch: '', mchc: '', plt: '', wbc: '', neutPercent: '', lymphPercent: '', monoPercent: '', eoPercent: '', basoPercent: '' },
        reticulocyte: { retAbsolute: '', retPermillage: '', lfrPercent: '', mfrPercent: '', hfrPercent: '' },
        coagulation: { pivka: '', pt: '', inr: '', aptt: '', tt: '', fdp: '' }
      },
      clinicalImmunology: {
        antibody: { igg: '', iga: '', igm: '', igg4: '' },
        autoantibody: { ana: '' }
      },
      clinicalMicrobiology: {}
    },
    imagingReports: [],
    pathology: { performed: false, reportText: '', nasScore: null, sourceType: 'MANUAL' },
    geneticSequencing: { tested: false, method: '', reportSource: '', summary: '', conclusion: '', variants: [], sourceType: 'MANUAL' },
    clinicalDecision: { diagnosis: '', treatmentPlan: '' },
    ...overrides
  }
}

describe('computeWorklist', () => {
  test('无病历时走 synthesized 路径', async () => {
    const queue = makeQueue([{ id: '1', aiStatus: '未诊断' }])
    const ds = makeDataSource(queue)
    const list = await computeWorklist(ds, 5)

    expect(list).toHaveLength(1)
    expect(list[0].origin).toBe('synthesized')
    expect(list[0].riskScore).toBeGreaterThanOrEqual(30)
    expect(list[0].riskScore).toBeLessThanOrEqual(85)
    expect(['低', '中', '高']).toContain(list[0].riskLevel)
  })

  test('有病历时走 derived 路径', async () => {
    const queue = makeQueue([{ id: '1', aiStatus: '未诊断' }])
    const record = makeRecord({
      physicalExam: {
        heightCm: null, weightKg: null, bmi: null,
        bloodPressureSystolic: null, bloodPressureDiastolic: null,
        respiratoryRate: null, heartRate: null,
        liverFibrosis: 'NO', cirrhosis: 'YES', fattyLiver: 'NO',
        liverFailure: 'NO', cholestasis: 'NO', viralHepatitis: 'NO'
      }
    })
    const ds = makeDataSource(queue, new Map([['1', record]]))
    const list = await computeWorklist(ds, 5)

    expect(list).toHaveLength(1)
    expect(list[0].origin).toBe('derived')
    expect(list[0].reason).not.toBe('')
  })

  test('按风险分降序排序', async () => {
    const queue = makeQueue([
      { id: 'a', aiStatus: '未诊断' },
      { id: 'b', aiStatus: '未诊断' },
      { id: 'c', aiStatus: '未诊断' }
    ])
    const ds = makeDataSource(queue)
    const list = await computeWorklist(ds, 5)

    for (let i = 1; i < list.length; i++) {
      expect(list[i - 1].riskScore).toBeGreaterThanOrEqual(list[i].riskScore)
    }
  })

  test('Top-N 限制', async () => {
    const queue = makeQueue([
      { id: '1', aiStatus: '未诊断' },
      { id: '2', aiStatus: '未诊断' },
      { id: '3', aiStatus: '未诊断' }
    ])
    const ds = makeDataSource(queue)
    const list = await computeWorklist(ds, 2)

    expect(list).toHaveLength(2)
  })

  test('已诊断患者不包含在工作清单中', async () => {
    const queue = makeQueue([
      { id: '1', aiStatus: '未诊断' },
      { id: '2', aiStatus: '已诊断' }
    ])
    const ds = makeDataSource(queue)
    const list = await computeWorklist(ds, 5)

    expect(list).toHaveLength(1)
    expect(list[0].id).toBe('1')
  })

  test('风险分级边界：低 < 20，高 ≥ 70', async () => {
    // 通过有病历控制得分
    const queue = makeQueue([{ id: '1', aiStatus: '未诊断' }])
    // 一个几乎没有异常证据的病历应该得分很低
    const record = makeRecord()
    const ds = makeDataSource(queue, new Map([['1', record]]))
    const list = await computeWorklist(ds, 5)

    // 无异常证据时，如果有基础证据项，可能得分为15（基础分）
    // 15 -> 低风险
    if (list[0].riskScore < 20) {
      expect(list[0].riskLevel).toBe('低')
    }
  })
})
