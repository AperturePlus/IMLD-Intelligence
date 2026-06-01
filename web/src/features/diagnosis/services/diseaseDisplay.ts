export interface DiagnosisRecommendationLike {
  recType?: string
  content?: string
  reason?: string
}

export interface ClinicalAbnormalityLike {
  feature?: string
  value?: number
  unit?: string
  normal_range?: number[]
  normalRange?: number[]
  normal_range_label?: string
  normalRangeLabel?: string
  direction?: string
  severity?: string
}

export interface GeneAbnormalityLike {
  gene?: string
  c_change?: string
  cChange?: string
  p_change?: string
  pChange?: string
}

export interface EvidenceItemLike {
  category?: string
  label?: string
  value?: string
  source?: string
  severity?: string
}

export interface InferenceDisplayLike {
  suggestions?: string[]
  clinical_abnormalities?: ClinicalAbnormalityLike[]
  clinicalAbnormalities?: ClinicalAbnormalityLike[]
  gene_abnormalities?: GeneAbnormalityLike[]
  geneAbnormalities?: GeneAbnormalityLike[]
  evidence_items?: EvidenceItemLike[]
  evidenceItems?: EvidenceItemLike[]
  differentials?: string[]
  differential_diagnoses?: string[]
  differentialDiagnoses?: string[]
  key_signs?: string[]
  keySigns?: string[]
  diet_tags?: string[]
  dietTags?: string[]
  gene_recommendation_title?: string
  geneRecommendationTitle?: string
}

export interface DiseaseDisplayInput {
  diseaseName?: string
  probability?: number
  inference?: InferenceDisplayLike
  recommendations?: DiagnosisRecommendationLike[]
  suggestions?: string[]
  genes?: string[]
  diet?: string
  sequencing?: string
}

export interface DiseaseDisplayFields {
  differentials: string[]
  keySigns: string[]
  dietTags: string[]
  genes: string[]
  diet: string
  sequencing: string
  geneRecommendationTitle: string
  dataConfidenceLabel: string
}

export interface DiseaseProfile {
  match: (name: string) => boolean
  differentials: string[]
  keySigns: string[]
  dietTags: string[]
  genes: string[]
  diet: string
  sequencing: string
  geneRecommendationTitle: string
}

const DEFAULT_DISEASE_NAME = '遗传代谢性肝病风险提示'

const DISEASE_PROFILES: DiseaseProfile[] = [
  {
    match: (name) => name.includes('Wilson') || name.includes('肝豆'),
    differentials: ['遗传性血色病（低可能）', '自身免疫性肝炎（需鉴别）', '胆汁淤积性肝病（需排除）'],
    keySigns: ['铜蓝蛋白降低', '24h 尿铜或 K-F 环需复核', '神经系统体征需同步评估'],
    dietTags: ['低铜饮食', '避免坚果/巧克力', '避免动物内脏'],
    genes: ['ATP7B (c.2333G>T)', 'ATP7B (c.2975C>T)'],
    diet: '建议低铜饮食，避免坚果、巧克力、贝类和动物内脏，并结合驱铜治疗计划随访。',
    sequencing: '建议 ATP7B 靶向测序或肝病遗传 panel，并对一级亲属开展家系筛查。',
    geneRecommendationTitle: 'ATP7B 基因检测建议'
  },
  {
    match: (name) => name.includes('血色') || name.toLowerCase().includes('hemochromatosis'),
    differentials: ['肝豆状核变性（低可能）', '代谢相关脂肪性肝病（需鉴别）', '酒精相关肝病（需结合病史）'],
    keySigns: ['铁蛋白或转铁蛋白饱和度升高', '皮肤色素沉着/关节症状需核实', '糖代谢异常需同步评估'],
    dietTags: ['限制红肉', '避免维生素C同餐', '避免动物内脏'],
    genes: ['HFE (C282Y)', 'HFE (H63D)'],
    diet: '建议限制红肉和动物内脏，避免随餐补充维生素 C，餐后可饮茶抑制铁吸收。',
    sequencing: '建议进行 HFE 基因检测，并对一级亲属开展家系筛查。',
    geneRecommendationTitle: 'HFE 基因检测建议'
  },
  {
    match: (name) => name.includes('抗胰蛋白酶') || name.toLowerCase().includes('aat'),
    differentials: ['代谢相关脂肪性肝病（需鉴别）', '不明原因肝硬化（需追踪）', '慢性阻塞性肺疾病相关风险（需评估）'],
    keySigns: ['α1-抗胰蛋白酶水平降低', '肺气肿或反复咳喘史需核实', '不明原因肝硬化需排查'],
    dietTags: ['高蛋白低脂', '戒烟限酒', '肝肺联合随访'],
    genes: ['SERPINA1 (Pi*ZZ)'],
    diet: '建议高蛋白、低脂饮食，减少酒精摄入并严格戒烟，配合呼吸系统评估。',
    sequencing: '建议进行 SERPINA1 基因分型，并评估肝肺联合受累风险。',
    geneRecommendationTitle: 'SERPINA1 基因检测建议'
  },
  {
    match: (name) => name.includes('脂肪') || name.includes('代谢'),
    differentials: ['遗传代谢性肝病（需排除）', '酒精相关肝病（需结合饮酒史）', '药物性肝损伤（需结合用药史）'],
    keySigns: ['BMI/腰围与代谢危险因素', 'TG、CHOL 或血糖升高', '脂肪肝影像或 NAS 评分异常'],
    dietTags: ['控制总热量', '减少精制碳水', '规律运动'],
    genes: [],
    diet: '建议控制总热量和精制碳水摄入，配合体重管理、规律运动和血糖血脂管理。',
    sequencing: '建议优先完善代谢危险因素评估；若存在家族聚集或早发重症，可结合遗传易感位点检测。',
    geneRecommendationTitle: '遗传易感评估建议'
  },
  {
    match: (name) => name.includes('Gilbert') || name.includes('吉尔伯特'),
    differentials: ['溶血性黄疸（需排除）', '胆汁淤积性肝病（低可能）', '药物相关胆红素升高（需结合用药史）'],
    keySigns: ['间接胆红素升高', '肝酶多为正常或轻度异常', '劳累、饥饿后黄疸波动'],
    dietTags: ['规律作息', '避免饥饿', '避免过度疲劳'],
    genes: ['UGT1A1'],
    diet: '良性高胆红素血症通常无需特殊忌口，建议规律作息、避免饥饿与过度疲劳。',
    sequencing: '可结合 UGT1A1 基因多态性检测；通常为良性病程，定期随访即可。',
    geneRecommendationTitle: 'UGT1A1 多态性检测建议'
  }
]

const DEFAULT_PROFILE: DiseaseProfile = {
  match: () => true,
  differentials: ['遗传代谢性肝病谱系（需进一步分型）', '常见慢性肝病（需鉴别）', '药物或感染相关肝损伤（需结合病史）'],
  keySigns: ['肝功能异常', '遗传或家族史线索', '影像、病理或代谢指标异常'],
  dietTags: ['均衡饮食', '避免酒精', '动态随访'],
  genes: [],
  diet: '建议清淡均衡饮食，避免酒精和高脂饮食，并根据最终诊断调整专病干预方案。',
  sequencing: '建议结合家系史、临床特征与实验室结果评估是否进行基因检测。',
  geneRecommendationTitle: '基因检测建议'
}

const compactStrings = (items: Array<string | null | undefined>): string[] => {
  return items.map((item) => item?.trim()).filter((item): item is string => Boolean(item))
}

const uniqueStrings = (items: string[]): string[] => [...new Set(compactStrings(items))]

export const resolveDiseaseProfile = (diseaseName?: string): DiseaseProfile => {
  const normalized = (diseaseName || DEFAULT_DISEASE_NAME).trim()
  return DISEASE_PROFILES.find((profile) => profile.match(normalized)) || DEFAULT_PROFILE
}

const firstArray = (...values: Array<string[] | undefined>): string[] => {
  for (const value of values) {
    const normalized = uniqueStrings(value || [])
    if (normalized.length > 0) {
      return normalized
    }
  }
  return []
}

const firstText = (...values: Array<string | undefined>): string | undefined => {
  return compactStrings(values)[0]
}

const findRecommendation = (recommendations: DiagnosisRecommendationLike[], type: string): string | undefined => {
  return recommendations.find((item) => item.recType?.toUpperCase() === type)?.content
}

const findSuggestion = (suggestions: string[], keywords: string[]): string | undefined => {
  return suggestions.find((item) => {
    const normalized = item.toLowerCase()
    return keywords.some((keyword) => normalized.includes(keyword.toLowerCase()))
  })
}

const formatClinicalAbnormality = (item: ClinicalAbnormalityLike): string | null => {
  const feature = item.feature?.trim()
  if (!feature) {
    return null
  }
  const binarySummary = item.value === 1 && item.direction === 'high' && feature.length > 24
  if (binarySummary) {
    const severity = item.severity ? `（${item.severity}）` : ''
    return `${feature}${severity}`
  }
  const directionMap: Record<string, string> = {
    high: '升高',
    low: '降低',
    positive: '阳性'
  }
  const direction = item.direction ? directionMap[item.direction] || item.direction : ''
  const value = typeof item.value === 'number' && Number.isFinite(item.value)
    ? ` ${item.value}${item.unit || ''}`
    : ''
  const severity = item.severity ? `（${item.severity}）` : ''
  return `${feature}${value}${direction ? ` ${direction}` : ''}${severity}`
}

const formatGeneAbnormality = (item: GeneAbnormalityLike): string | null => {
  const gene = item.gene?.trim()
  if (!gene) {
    return null
  }
  const change = firstText(item.c_change, item.cChange, item.p_change, item.pChange)
  return change ? `${gene} (${change})` : gene
}

const formatEvidenceItem = (item: EvidenceItemLike): string | null => {
  const label = item.label?.trim()
  if (!label) {
    return null
  }
  return item.value?.trim() ? `${label}（${item.value.trim()}）` : label
}

export const buildDataConfidenceLabel = (probability = 0): string => {
  const normalized = Math.max(0, Math.min(100, probability))
  const label = normalized >= 80 ? '高' : normalized >= 50 ? '中' : '低'
  return `${label} (${(normalized / 100).toFixed(2)})`
}

export const buildDiseaseDisplayFields = (input: DiseaseDisplayInput): DiseaseDisplayFields => {
  const diseaseName = input.diseaseName || DEFAULT_DISEASE_NAME
  const profile = resolveDiseaseProfile(diseaseName)
  const inference = input.inference
  const recommendations = input.recommendations || []
  const suggestions = uniqueStrings([...(input.suggestions || []), ...(inference?.suggestions || [])])
  const clinicalSigns = uniqueStrings(
    (inference?.clinical_abnormalities || inference?.clinicalAbnormalities || [])
      .map(formatClinicalAbnormality)
      .filter((item): item is string => Boolean(item))
  )
  const inferenceGenes = uniqueStrings(
    (inference?.gene_abnormalities || inference?.geneAbnormalities || [])
      .map(formatGeneAbnormality)
      .filter((item): item is string => Boolean(item))
  )
  const evidenceSigns = uniqueStrings(
    (inference?.evidence_items || inference?.evidenceItems || [])
      .filter((item) => ['warning', 'exception'].includes(item.severity || ''))
      .map(formatEvidenceItem)
      .filter((item): item is string => Boolean(item))
  )
  const genes = firstArray(input.genes, inferenceGenes, profile.genes)

  return {
    differentials: firstArray(
      inference?.differentials,
      inference?.differential_diagnoses,
      inference?.differentialDiagnoses,
      profile.differentials
    ),
    keySigns: firstArray(inference?.key_signs, inference?.keySigns, evidenceSigns, clinicalSigns, profile.keySigns),
    dietTags: firstArray(inference?.diet_tags, inference?.dietTags, profile.dietTags),
    genes,
    diet: firstText(
      input.diet,
      findRecommendation(recommendations, 'DIET'),
      findSuggestion(suggestions, ['饮食', 'diet', '营养']),
      profile.diet
    ) || profile.diet,
    sequencing: firstText(
      input.sequencing,
      findRecommendation(recommendations, 'GENETIC'),
      findSuggestion(suggestions, ['基因', 'gene', '测序', 'panel', 'wes']),
      profile.sequencing
    ) || profile.sequencing,
    geneRecommendationTitle: firstText(
      inference?.gene_recommendation_title,
      inference?.geneRecommendationTitle,
      profile.geneRecommendationTitle
    ) || profile.geneRecommendationTitle,
    dataConfidenceLabel: buildDataConfidenceLabel(input.probability)
  }
}
