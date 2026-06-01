import type {
  DiagnosisConfidence,
  DiagnosisEvidenceItem,
  DiagnosisEvidenceSeverity,
  DiagnosisEvidenceSummary,
  DiagnosisIndicator,
  ProgressStatus
} from '@/types/diagnosis'
import type { PatientRecordPayload, TernaryFlag } from '@/types/patient'

export const DEFAULT_MODEL_FEATURE_COUNT = 40

interface BuildEvidenceInput {
  diseaseName: string
  indicators?: DiagnosisIndicator[]
  record?: PatientRecordPayload | null
  genes?: string[]
}

const compact = (value: string | null | undefined): string => String(value || '').trim()

const uniqueByLabel = (items: DiagnosisEvidenceItem[]): DiagnosisEvidenceItem[] => {
  const seen = new Set<string>()
  return items.filter((item) => {
    const key = `${item.category}:${item.label}:${item.value || ''}`
    if (seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
}

const truncate = (value: string, max = 56): string => {
  const text = compact(value)
  return text.length > max ? `${text.slice(0, max)}...` : text
}

const flagYes = (value: TernaryFlag | undefined): boolean => value === 'YES'

const severityFromStatus = (status: ProgressStatus): DiagnosisEvidenceSeverity => {
  if (status === 'exception') {
    return 'exception'
  }
  if (status === 'warning') {
    return 'warning'
  }
  if (status === 'success') {
    return 'success'
  }
  return 'info'
}

const statusLabel = (status: ProgressStatus): string => {
  if (status === 'exception') {
    return '显著异常'
  }
  if (status === 'warning') {
    return '异常'
  }
  if (status === 'success') {
    return '正常'
  }
  return '已纳入'
}

const indicatorEvidence = (indicator: DiagnosisIndicator): DiagnosisEvidenceItem => ({
  category: '生化',
  label: `${indicator.name} ${indicator.value}${indicator.unit}`,
  value: `参考 ${indicator.normal}，${statusLabel(indicator.status)}`,
  source: 'EMR/LIS',
  severity: severityFromStatus(indicator.status)
})

const geneEvidence = (record: PatientRecordPayload): DiagnosisEvidenceItem[] => {
  const sequencing = record.geneticSequencing
  if (!sequencing?.tested) {
    return []
  }

  const variantItems = (sequencing.variants || []).slice(0, 3).map((variant) => {
    const change = compact(variant.hgvsC || variant.hgvsP)
    return {
      category: '基因',
      label: change ? `${variant.gene} ${change}` : variant.gene,
      value: compact(variant.classification || sequencing.conclusion || sequencing.summary) || undefined,
      source: sequencing.method || sequencing.reportSource || '基因检测',
      severity: 'exception' as DiagnosisEvidenceSeverity
    }
  })

  if (variantItems.length > 0) {
    return variantItems
  }

  const summary = compact(sequencing.conclusion || sequencing.summary)
  return summary
    ? [
        {
          category: '基因',
          label: truncate(summary),
          source: sequencing.method || sequencing.reportSource || '基因检测',
          severity: 'warning'
        }
      ]
    : []
}

const historyEvidence = (record: PatientRecordPayload, diseaseName: string): DiagnosisEvidenceItem[] => {
  const history = record.history.diseaseHistory
  const items: DiagnosisEvidenceItem[] = []
  if (flagYes(history.diabetesHistory)) {
    items.push({
      category: '病史',
      label: '糖尿病史/糖代谢异常',
      source: '病史',
      severity: diseaseName.includes('血色') || diseaseName.includes('脂肪') || diseaseName.includes('代谢') ? 'warning' : 'info'
    })
  }
  if (flagYes(history.hyperlipidemiaHistory)) {
    items.push({
      category: '病史',
      label: '高脂血症病史',
      source: '病史',
      severity: diseaseName.includes('脂肪') || diseaseName.includes('代谢') ? 'warning' : 'info'
    })
  }
  if (flagYes(history.drinkingHistory)) {
    items.push({
      category: '病史',
      label: '饮酒史阳性，需与酒精相关肝病鉴别',
      source: '病史',
      severity: 'warning'
    })
  }
  if (flagYes(history.smokingHistory) && diseaseName.includes('抗胰蛋白酶')) {
    items.push({
      category: '病史',
      label: '吸烟史阳性，需联合评估肺部受累',
      source: '病史',
      severity: 'warning'
    })
  }
  return items
}

const physicalEvidence = (record: PatientRecordPayload): DiagnosisEvidenceItem[] => {
  const exam = record.physicalExam
  const items: DiagnosisEvidenceItem[] = []
  if (flagYes(exam.liverFibrosis)) {
    items.push({ category: '体征', label: '肝纤维化线索阳性', source: '查体/病程', severity: 'warning' })
  }
  if (flagYes(exam.cirrhosis)) {
    items.push({ category: '体征', label: '肝硬化线索阳性', source: '查体/病程', severity: 'exception' })
  }
  if (flagYes(exam.fattyLiver)) {
    items.push({ category: '体征', label: '脂肪肝线索阳性', source: '查体/影像摘要', severity: 'warning' })
  }
  return items
}

const narrativeEvidence = (record: PatientRecordPayload, diseaseName: string): DiagnosisEvidenceItem[] => {
  const text = `${record.chiefComplaint || ''} ${record.presentIllness || ''}`
  const items: DiagnosisEvidenceItem[] = []
  if (diseaseName.includes('血色') && /色素|皮肤|铁过载|血糖|关节/.test(text)) {
    items.push({
      category: '临床表型',
      label: truncate(record.chiefComplaint || record.presentIllness),
      source: '主诉/现病史',
      severity: 'warning'
    })
  } else if ((diseaseName.includes('Wilson') || diseaseName.includes('肝豆')) && /震颤|构音|K-F|铜/.test(text)) {
    items.push({
      category: '临床表型',
      label: truncate(record.chiefComplaint || record.presentIllness),
      source: '主诉/现病史',
      severity: 'warning'
    })
  } else if (diseaseName.includes('抗胰蛋白酶') && /肺气肿|咳喘|气促/.test(text)) {
    items.push({
      category: '临床表型',
      label: truncate(record.chiefComplaint || record.presentIllness),
      source: '主诉/现病史',
      severity: 'warning'
    })
  } else if ((diseaseName.includes('脂肪') || diseaseName.includes('代谢')) && /肥胖|血糖|血脂|脂肪肝/.test(text)) {
    items.push({
      category: '临床表型',
      label: truncate(record.chiefComplaint || record.presentIllness),
      source: '主诉/现病史',
      severity: 'warning'
    })
  }
  return items
}

const imagingEvidence = (record: PatientRecordPayload): DiagnosisEvidenceItem[] => {
  return (record.imagingReports || []).slice(0, 2).map((report) => ({
    category: '影像',
    label: truncate(report.reportText),
    source: report.modality,
    severity: 'warning'
  }))
}

const pathologyEvidence = (record: PatientRecordPayload): DiagnosisEvidenceItem[] => {
  if (!record.pathology?.performed) {
    return []
  }
  const nas = record.pathology.nasScore == null ? '' : `NAS ${record.pathology.nasScore}；`
  return [
    {
      category: '病理',
      label: truncate(`${nas}${record.pathology.reportText || '已完成病理评估'}`),
      source: '病理',
      severity: (record.pathology.nasScore || 0) >= 5 ? 'exception' : 'warning'
    }
  ]
}

export const buildEvidenceItemsFromDiagnosis = ({
  diseaseName,
  indicators = [],
  record,
  genes = []
}: BuildEvidenceInput): DiagnosisEvidenceItem[] => {
  const items: DiagnosisEvidenceItem[] = []
  items.push(...indicators.filter((item) => item.status).map(indicatorEvidence))

  if (record) {
    items.push(...geneEvidence(record))
    items.push(...narrativeEvidence(record, diseaseName))
    items.push(...historyEvidence(record, diseaseName))
    items.push(...physicalEvidence(record))
    items.push(...imagingEvidence(record))
    items.push(...pathologyEvidence(record))
  }

  for (const gene of genes) {
    if (!gene) {
      continue
    }
    items.push({
      category: '基因',
      label: gene,
      source: '疾病谱默认建议',
      severity: 'info'
    })
  }

  return uniqueByLabel(items).slice(0, 12)
}

export const buildEvidenceSummary = (
  evidenceItems: DiagnosisEvidenceItem[],
  confidence: Pick<DiagnosisConfidence, 'reviewRequired'>,
  modelFeatureCount = DEFAULT_MODEL_FEATURE_COUNT
): DiagnosisEvidenceSummary => ({
  modelFeatureCount,
  abnormalEvidenceCount: evidenceItems.filter((item) => ['warning', 'exception'].includes(item.severity)).length,
  reviewRequired: confidence.reviewRequired
})

export const buildKeySignsFromEvidenceItems = (items: DiagnosisEvidenceItem[], limit = 6): string[] => {
  return items
    .filter((item) => ['warning', 'exception'].includes(item.severity))
    .slice(0, limit)
    .map((item) => (item.value ? `${item.label}（${item.value}）` : item.label))
}
