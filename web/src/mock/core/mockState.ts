// @ts-nocheck

import { SEED_PATIENT_RECORDS } from './patientRecordsSeed'
import { buildDiseaseDisplayFields } from '../../features/diagnosis/services/diseaseDisplay'

const MOCK_USERS_KEY = '__imld_mock_users__'
const MOCK_TOKENS_KEY = '__imld_mock_tokens__'
const MOCK_PATIENTS_KEY = '__imld_mock_patients__'
const MOCK_RECORDS_KEY = '__imld_mock_records__'
const MOCK_REPORTS_KEY = '__imld_mock_reports__'
const MOCK_DIET_OVERRIDES_KEY = '__imld_mock_diet_overrides__'
const MOCK_EMAIL_CODES_KEY = '__imld_mock_email_codes__'
const MOCK_SEED_VERSION = '2026-06-03-young-rare-imld'

const DEFAULT_MOCK_USERS = [
  {
    username: 'doctor',
    email: 'doctor@imld.local',
    password: '123456',
    role: 'chief-physician'
  },
  {
    username: 'admin',
    email: 'admin@imld.local',
    password: '123456',
    role: 'admin'
  }
]

export const SEED_PATIENTS = [
  { id: 'P001', name: '方亦辰', gender: '男', age: 14, riskLevel: '高', disease: '肝豆状核变性 (Wilson病)', compliance: '良好', aiStatus: '未诊断', avatar: '' },
  { id: 'P002', name: '梁知夏', gender: '女', age: 8, riskLevel: '高', disease: 'Citrin缺乏症', compliance: '极佳', aiStatus: '已诊断', avatar: '' },
  { id: 'P003', name: '何星澜', gender: '男', age: 6, riskLevel: '高', disease: 'PFIC2/ABCB11', compliance: '一般', aiStatus: '未诊断', avatar: '' },
  { id: 'P004', name: '苏念青', gender: '女', age: 11, riskLevel: '高', disease: 'PFIC3/ABCB4', compliance: '良好', aiStatus: '未诊断', avatar: '' },
  { id: 'P005', name: '江屿川', gender: '男', age: 17, riskLevel: '低', disease: 'Gilbert综合征', compliance: '良好', aiStatus: '未诊断', avatar: '' },
  { id: 'P006', name: '许安琪', gender: '女', age: 19, riskLevel: '中', disease: 'Dubin-Johnson综合征', compliance: '良好', aiStatus: '未诊断', avatar: '' },
  { id: 'P007', name: '林沐阳', gender: '男', age: 9, riskLevel: '高', disease: 'Alagille综合征', compliance: '一般', aiStatus: '未诊断', avatar: '' },
  { id: 'P008', name: '程小禾', gender: '女', age: 5, riskLevel: '中', disease: 'NTCP缺乏症/SLC10A1', compliance: '极佳', aiStatus: '未诊断', avatar: '' },
  { id: 'P009', name: '唐景曜', gender: '男', age: 16, riskLevel: '高', disease: '溶酶体酸性脂肪酶缺乏症/LIPA', compliance: '一般', aiStatus: '未诊断', avatar: '' },
  { id: 'P010', name: '韩可一', gender: '女', age: 7, riskLevel: '高', disease: '糖原累积病I型/G6PC', compliance: '良好', aiStatus: '已诊断', avatar: '' },
  { id: 'P011', name: '沈予安', gender: '男', age: 23, riskLevel: '中', disease: 'α1-抗胰蛋白酶缺乏症', compliance: '良好', aiStatus: '未诊断', avatar: '' },
  { id: 'P012', name: '叶清源', gender: '男', age: 29, riskLevel: '高', disease: '青年型遗传性血色病', compliance: '一般', aiStatus: '未诊断', avatar: '' }
]

const SEED_PATIENT_IDS = new Set(SEED_PATIENTS.map((item) => item.id))
const FIXED_REPORT_IDS = new Set(['REP-202311-001', 'REP-202311-002', 'REP-202311-003'])

const MOCK_REFERENCE_NOTE = 'mock参考区间，实际以检验机构为准'

const formatIndicatorStatus = (status = '') => {
  if (status === 'exception') {
    return '显著偏离'
  }
  if (status === 'warning') {
    return '偏离'
  }
  if (status === 'success') {
    return '正常'
  }
  return '已纳入'
}

const formatIndicatorSummary = (indicators = [], withMockNote = true) => {
  if (!Array.isArray(indicators) || indicators.length === 0) {
    return '见诊断会话明细'
  }

  const summary = indicators
    .map((item) => {
      const unit = item.unit ? `${item.unit === '%' ? '' : ' '}${item.unit}` : ''
      const reference = item.normal ? `参考 ${item.normal}` : '参考 --'
      return `${item.name} ${item.value}${unit} (${reference} / ${formatIndicatorStatus(item.status)})`
    })
    .join('，')

  return withMockNote ? `${summary}（${MOCK_REFERENCE_NOTE}）` : summary
}

const DIAGNOSIS_PAYLOADS = {
  '肝豆状核变性 (Wilson病)': {
    diseaseName: '肝豆状核变性 (Wilson病)',
    riskLevel: '高',
    probability: 94,
    indicators: [
      { name: '血清铜蓝蛋白', value: 0.055, unit: 'g/L', normal: '0.20-0.60', percentage: 92, status: 'exception' },
      { name: 'ALT', value: 126, unit: 'U/L', normal: '9-50', percentage: 82, status: 'exception' },
      { name: 'AST', value: 98, unit: 'U/L', normal: '15-40', percentage: 76, status: 'exception' }
    ],
    genes: ['ATP7B (c.2333G>T)', 'ATP7B (c.2975C>T)'],
    diet: '建议低铜饮食，避免坚果、巧克力、贝类和动物内脏，并结合驱铜治疗计划随访。',
    sequencing: '建议 ATP7B 靶向测序或肝病遗传 panel，并对一级亲属开展家系筛查。'
  },
  Citrin缺乏症: {
    diseaseName: 'Citrin缺乏症',
    riskLevel: '高',
    probability: 91,
    indicators: [
      { name: 'DBIL', value: 20, unit: 'μmol/L', normal: '0-6.8', percentage: 78, status: 'exception' },
      { name: 'GLU', value: 3.2, unit: 'mmol/L', normal: '3.9-6.1', percentage: 72, status: 'warning' },
      { name: 'NH3', value: 92, unit: 'μmol/L', normal: '18-72', percentage: 68, status: 'warning' }
    ],
    genes: ['SLC25A13 (c.852_855del)', 'SLC25A13 (c.1638_1660dup)'],
    diet: '建议避免高碳水负荷，采用少量多餐、相对高蛋白高脂饮食，并补充脂溶性维生素。',
    sequencing: '建议进行 SLC25A13 基因检测，并对父母和同胞开展携带者筛查。'
  },
  'PFIC2/ABCB11': {
    diseaseName: 'PFIC2/ABCB11',
    riskLevel: '高',
    probability: 93,
    indicators: [
      { name: 'DBIL', value: 68, unit: 'μmol/L', normal: '0-6.8', percentage: 94, status: 'exception' },
      { name: 'TBA', value: 180, unit: 'μmol/L', normal: '0-10', percentage: 95, status: 'exception' },
      { name: 'GGT', value: 28, unit: 'U/L', normal: '0-60', percentage: 20, status: 'success' }
    ],
    genes: ['ABCB11 (c.890A>G)', 'ABCB11 (p.Gly982Arg)'],
    diet: '建议补充脂溶性维生素和中链脂肪，瘙痒明显时评估胆汁酸转运相关治疗。',
    sequencing: '建议 ABCB11 靶向测序或胆汁淤积 panel，并评估家系复发风险。'
  },
  'PFIC3/ABCB4': {
    diseaseName: 'PFIC3/ABCB4',
    riskLevel: '高',
    probability: 88,
    indicators: [
      { name: 'DBIL', value: 48, unit: 'μmol/L', normal: '0-6.8', percentage: 88, status: 'exception' },
      { name: 'GGT', value: 210, unit: 'U/L', normal: '0-60', percentage: 94, status: 'exception' },
      { name: 'TBA', value: 130, unit: 'μmol/L', normal: '0-10', percentage: 92, status: 'exception' }
    ],
    genes: ['ABCB4 (c.959C>T)'],
    diet: '建议脂溶性维生素补充，保持足量能量摄入，并在医生指导下评估熊去氧胆酸反应。',
    sequencing: '建议 ABCB4 基因检测，必要时纳入 PFIC/胆汁淤积 panel 进行复核。'
  },
  Gilbert综合征: {
    diseaseName: 'Gilbert综合征',
    riskLevel: '低',
    probability: 48,
    indicators: [
      { name: 'IBIL', value: 31, unit: 'μmol/L', normal: '1.7-13.7', percentage: 74, status: 'warning' },
      { name: 'TBIL', value: 36, unit: 'μmol/L', normal: '3.4-17.1', percentage: 68, status: 'warning' },
      { name: 'ALT', value: 28, unit: 'U/L', normal: '9-50', percentage: 30, status: 'success' }
    ],
    genes: ['UGT1A1'],
    diet: '通常无需特殊忌口，建议规律作息、避免饥饿、熬夜和过度疲劳。',
    sequencing: '可结合 UGT1A1 基因多态性检测；通常为良性病程，定期随访即可。'
  },
  'Dubin-Johnson综合征': {
    diseaseName: 'Dubin-Johnson综合征',
    riskLevel: '中',
    probability: 66,
    indicators: [
      { name: 'DBIL', value: 42, unit: 'μmol/L', normal: '0-6.8', percentage: 86, status: 'exception' },
      { name: 'TBIL', value: 58, unit: 'μmol/L', normal: '3.4-17.1', percentage: 82, status: 'exception' },
      { name: 'ALT', value: 32, unit: 'U/L', normal: '9-50', percentage: 32, status: 'success' }
    ],
    genes: ['ABCC2'],
    diet: '多数病例无需特殊饮食限制，建议避免不必要用药刺激并定期复查胆红素分型。',
    sequencing: '建议 ABCC2 基因检测，结合直接胆红素升高和家族史完成分型。'
  },
  Alagille综合征: {
    diseaseName: 'Alagille综合征',
    riskLevel: '高',
    probability: 90,
    indicators: [
      { name: 'DBIL', value: 62, unit: 'μmol/L', normal: '0-6.8', percentage: 92, status: 'exception' },
      { name: 'GGT', value: 260, unit: 'U/L', normal: '0-60', percentage: 95, status: 'exception' },
      { name: 'ALP', value: 520, unit: 'U/L', normal: '45-150', percentage: 90, status: 'exception' }
    ],
    genes: ['JAG1', 'NOTCH2'],
    diet: '建议高能量饮食和脂溶性维生素补充，瘙痒明显时按胆汁淤积路径管理。',
    sequencing: '建议 JAG1/NOTCH2 基因检测，并联合心血管、眼科和骨骼系统评估。'
  },
  'NTCP缺乏症/SLC10A1': {
    diseaseName: 'NTCP缺乏症/SLC10A1',
    riskLevel: '中',
    probability: 63,
    indicators: [
      { name: 'TBA', value: 185, unit: 'μmol/L', normal: '0-10', percentage: 95, status: 'exception' },
      { name: 'ALT', value: 26, unit: 'U/L', normal: '9-50', percentage: 26, status: 'success' },
      { name: 'TBIL', value: 18, unit: 'μmol/L', normal: '3.4-17.1', percentage: 42, status: 'warning' }
    ],
    genes: ['SLC10A1 (c.800C>T)'],
    diet: '多以观察随访为主，建议均衡饮食，避免因单项胆汁酸升高过度限制营养。',
    sequencing: '建议 SLC10A1 基因检测，结合胆汁酸显著升高且肝酶正常的表型确认。'
  },
  '溶酶体酸性脂肪酶缺乏症/LIPA': {
    diseaseName: '溶酶体酸性脂肪酶缺乏症/LIPA',
    riskLevel: '高',
    probability: 87,
    indicators: [
      { name: 'LDL-C', value: 5, unit: 'mmol/L', normal: '<3.4', percentage: 82, status: 'exception' },
      { name: 'TG', value: 3.4, unit: 'mmol/L', normal: '<1.7', percentage: 88, status: 'exception' },
      { name: 'ALT', value: 88, unit: 'U/L', normal: '9-50', percentage: 64, status: 'warning' }
    ],
    genes: ['LIPA (c.894G>A)'],
    diet: '建议低胆固醇饮食并避免过度高脂摄入，评估酶替代治疗适应证。',
    sequencing: '建议 LIPA 基因检测，必要时补充溶酶体酸性脂肪酶活性检测。'
  },
  '糖原累积病I型/G6PC': {
    diseaseName: '糖原累积病I型/G6PC',
    riskLevel: '高',
    probability: 89,
    indicators: [
      { name: 'GLU', value: 2.9, unit: 'mmol/L', normal: '3.9-6.1', percentage: 82, status: 'exception' },
      { name: 'TG', value: 4.8, unit: 'mmol/L', normal: '<1.7', percentage: 94, status: 'exception' },
      { name: 'URIC', value: 520, unit: 'μmol/L', normal: '<420', percentage: 72, status: 'warning' }
    ],
    genes: ['G6PC (c.648G>T)'],
    diet: '建议避免长时间空腹，评估夜间生玉米淀粉方案，并控制高尿酸和高甘油三酯。',
    sequencing: '建议 G6PC/SLC37A4 基因检测，并结合低血糖、高乳酸或高尿酸表型分型。'
  },
  'α1-抗胰蛋白酶缺乏症': {
    diseaseName: 'α1-抗胰蛋白酶缺乏症',
    riskLevel: '中',
    probability: 82,
    indicators: [
      { name: 'AAT', value: 0.43, unit: 'g/L', normal: '0.90-2.00', percentage: 18, status: 'exception' },
      { name: 'ALT', value: 78, unit: 'U/L', normal: '9-50', percentage: 58, status: 'warning' },
      { name: 'AST', value: 64, unit: 'U/L', normal: '15-40', percentage: 52, status: 'warning' }
    ],
    genes: ['SERPINA1 (Pi*ZZ)'],
    diet: '建议高蛋白、低脂饮食，避免烟草暴露和酒精摄入，配合呼吸系统评估。',
    sequencing: '建议进行 SERPINA1 基因分型，并评估肝肺联合受累风险。'
  },
  青年型遗传性血色病: {
    diseaseName: '青年型遗传性血色病',
    riskLevel: '高',
    probability: 86,
    indicators: [
      { name: '血清铁蛋白', value: 1180, unit: 'ng/mL', normal: '30-300', percentage: 95, status: 'exception' },
      { name: '转铁蛋白饱和度', value: 72, unit: '%', normal: '20-45', percentage: 90, status: 'exception' },
      { name: 'ALT', value: 105, unit: 'U/L', normal: '9-50', percentage: 72, status: 'exception' }
    ],
    genes: ['HJV (HFE2)', 'HAMP'],
    diet: '建议限制红肉和动物内脏，避免随餐补充维生素 C，治疗前不自行使用铁剂。',
    sequencing: '建议 HJV/HAMP/HFE/TFR2 铁代谢 panel，并对一级亲属开展家系筛查。'
  },
  遗传性血色病: {
    diseaseName: '遗传性血色病',
    riskLevel: '高',
    probability: 84,
    indicators: [
      { name: '血清铁蛋白', value: 980, unit: 'ng/mL', normal: '30-300', percentage: 92, status: 'exception' },
      { name: '转铁蛋白饱和度', value: 68, unit: '%', normal: '20-45', percentage: 88, status: 'exception' },
      { name: 'ALT', value: 92, unit: 'U/L', normal: '9-50', percentage: 68, status: 'warning' }
    ],
    genes: ['HFE (C282Y)', 'HFE (H63D)'],
    diet: '建议限制红肉和动物内脏，避免随餐补充维生素 C，餐后可饮茶抑制铁吸收。',
    sequencing: '建议进行 HFE 基因检测，并对一级亲属开展家系筛查。'
  }
}

const FIXED_REPORT_DISEASE_BY_ID = {
  'REP-202311-001': '肝豆状核变性 (Wilson病)',
  'REP-202311-002': 'Citrin缺乏症',
  'REP-202311-003': 'PFIC2/ABCB11'
}

const copyDiagnosisPayload = (payload) => ({
  ...payload,
  indicators: (payload.indicators || []).map((item) => ({ ...item })),
  genes: [...(payload.genes || [])]
})

const resolveDiagnosisPayloadTemplate = (diseaseName = '') => {
  if (DIAGNOSIS_PAYLOADS[diseaseName]) {
    return DIAGNOSIS_PAYLOADS[diseaseName]
  }
  if (diseaseName.includes('Wilson') || diseaseName.includes('肝豆')) {
    return DIAGNOSIS_PAYLOADS['肝豆状核变性 (Wilson病)']
  }
  if (diseaseName.includes('Citrin')) {
    return DIAGNOSIS_PAYLOADS.Citrin缺乏症
  }
  if (diseaseName.includes('PFIC2') || diseaseName.includes('ABCB11')) {
    return DIAGNOSIS_PAYLOADS['PFIC2/ABCB11']
  }
  if (diseaseName.includes('PFIC3') || diseaseName.includes('ABCB4')) {
    return DIAGNOSIS_PAYLOADS['PFIC3/ABCB4']
  }
  if (diseaseName.includes('Dubin')) {
    return DIAGNOSIS_PAYLOADS['Dubin-Johnson综合征']
  }
  if (diseaseName.includes('Alagille')) {
    return DIAGNOSIS_PAYLOADS.Alagille综合征
  }
  if (diseaseName.includes('NTCP') || diseaseName.includes('SLC10A1')) {
    return DIAGNOSIS_PAYLOADS['NTCP缺乏症/SLC10A1']
  }
  if (diseaseName.includes('脂肪酶') || diseaseName.includes('LIPA')) {
    return DIAGNOSIS_PAYLOADS['溶酶体酸性脂肪酶缺乏症/LIPA']
  }
  if (diseaseName.includes('糖原') || diseaseName.includes('G6PC')) {
    return DIAGNOSIS_PAYLOADS['糖原累积病I型/G6PC']
  }
  if (diseaseName.includes('抗胰蛋白酶')) {
    return DIAGNOSIS_PAYLOADS['α1-抗胰蛋白酶缺乏症']
  }
  if (diseaseName.includes('血色')) {
    return diseaseName.includes('青年型')
      ? DIAGNOSIS_PAYLOADS.青年型遗传性血色病
      : DIAGNOSIS_PAYLOADS['遗传性血色病']
  }
  if (diseaseName.includes('Gilbert') || diseaseName.includes('吉尔伯特')) {
    return DIAGNOSIS_PAYLOADS.Gilbert综合征
  }
  return DIAGNOSIS_PAYLOADS['肝豆状核变性 (Wilson病)']
}

const buildFixedReportDiagnosisPayload = (reportId) => {
  const diseaseName = FIXED_REPORT_DISEASE_BY_ID[reportId]
  const template = diseaseName ? resolveDiagnosisPayloadTemplate(diseaseName) : null
  return template ? withDiagnosisDisplayFields(copyDiagnosisPayload(template)) : null
}

const SEED_REPORTS = [
  {
    id: 'REP-202311-001',
    patientId: 'P001',
    visitId: 'MZ8849201',
    patientName: '方亦辰',
    gender: '男',
    age: 14,
    date: '2026-05-02',
    status: '待签发',
    aiFindings: {
      biochemical: formatIndicatorSummary(buildFixedReportDiagnosisPayload('REP-202311-001')?.indicators),
      clinical: '手抖和注意力下降，裂隙灯提示可疑 K-F 环，铜蓝蛋白显著降低。',
      probability: '94',
      disease: '肝豆状核变性 (Wilson病)'
    },
    diagnosisPayload: buildFixedReportDiagnosisPayload('REP-202311-001'),
    expertConclusion: '同意 AI 辅助诊断意见。患者铜蓝蛋白显著降低，结合神经系统表现和 ATP7B 复合杂合变异，Wilson 病可能性高。',
    treatmentPlan: '建议：完善 24h 尿铜和眼科复核，评估驱铜治疗，低铜饮食，并开展一级亲属筛查。'
  },
  {
    id: 'REP-202311-002',
    patientId: 'P002',
    visitId: 'MZ8849205',
    patientName: '梁知夏',
    gender: '女',
    age: 8,
    date: '2026-05-03',
    status: '已签发',
    aiFindings: {
      biochemical: formatIndicatorSummary(buildFixedReportDiagnosisPayload('REP-202311-002')?.indicators),
      clinical: '反复黄疸、厌油和低血糖样发作，伴 SLC25A13 复合杂合变异。',
      probability: '91',
      disease: 'Citrin缺乏症'
    },
    diagnosisPayload: buildFixedReportDiagnosisPayload('REP-202311-002'),
    expertConclusion: '根据胆红素分型、低血糖线索和 SLC25A13 复合杂合变异，符合 Citrin 缺乏症表现。',
    treatmentPlan: '建议少量多餐，避免高碳水负荷，补充中链脂肪和脂溶性维生素，并进行家系遗传咨询。'
  },
  {
    id: 'REP-202311-003',
    patientId: 'P003',
    visitId: 'MZ8849212',
    patientName: '何星澜',
    gender: '男',
    age: 6,
    date: '2026-05-04',
    status: '待签发',
    aiFindings: {
      biochemical: formatIndicatorSummary(buildFixedReportDiagnosisPayload('REP-202311-003')?.indicators),
      clinical: '儿童期胆汁淤积、瘙痒和低/正常 GGT 表型，伴 ABCB11 双等位变异。',
      probability: '93',
      disease: 'PFIC2/ABCB11'
    },
    diagnosisPayload: buildFixedReportDiagnosisPayload('REP-202311-003'),
    expertConclusion: '',
    treatmentPlan: ''
  }
]

export const DISEASE_CONFIGS = {
  '肝豆状核变性 (Wilson病)': {
    targets: [
      { label: '每日铜摄入量', value: '< 1.0', unit: 'mg/日', color: '#f56c6c', desc: '绝对核心指标，超量将加重肝脑损伤' },
      { label: '每日蛋白质摄入', value: '1.5-2.0', unit: 'g/kg', color: '#409EFF', desc: '促进铜排泄与肝细胞修复' },
      { label: '每日饮水量', value: '> 2000', unit: 'ml', color: '#67c23a', desc: '建议饮用纯净水' }
    ],
    foods: {
      red: ['猪肝', '牛羊内脏', '巧克力', '花生', '核桃', '牡蛎'],
      yellow: ['牛肉', '羊肉', '燕麦', '黄豆'],
      green: ['精白米面', '鸡蛋清', '瘦猪肉', '牛奶', '白菜', '苹果']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '牛奶 250ml，白面馒头 1个，鸡蛋白 2个', nutrition: '含铜量约 0.12mg' },
      { type: 'warning', time: '午餐', menu: '白米饭，清蒸鱼肉，蒜蓉白菜', nutrition: '含铜量约 0.25mg' },
      { type: 'info', time: '晚餐', menu: '白米粥，青椒肉丝，凉拌黄瓜', nutrition: '含铜量约 0.18mg' }
    ]
  },
  Citrin缺乏症: {
    targets: [
      { label: '碳水负荷', value: '分散', unit: '少量', color: '#f56c6c', desc: '避免高碳水一次性摄入诱发代谢失衡' },
      { label: '蛋白脂肪', value: '充足', unit: '供能', color: '#409EFF', desc: '偏好高蛋白、高脂的能量结构' },
      { label: '低血糖监测', value: '每日', unit: '记录', color: '#67c23a', desc: '关注晨起和餐前低血糖线索' }
    ],
    foods: {
      red: ['含糖饮料', '大份甜点', '单次大量米粥', '高糖零食'],
      yellow: ['白米饭', '面条', '土豆', '红薯'],
      green: ['鸡蛋', '鱼肉', '瘦肉', '豆腐', '牛奶', '橄榄油']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '鸡蛋羹，牛奶 200ml，小份全麦面包', nutrition: '蛋白脂肪供能，避免空腹' },
      { type: 'warning', time: '午餐', menu: '米饭半碗，清蒸鱼，豆腐青菜', nutrition: '碳水分散摄入' },
      { type: 'info', time: '晚餐', menu: '瘦肉蔬菜汤，少量杂粮饭，酸奶', nutrition: '睡前评估低血糖风险' }
    ]
  },
  'PFIC2/ABCB11': {
    targets: [
      { label: '脂溶维生素', value: '补充', unit: 'A/D/E/K', color: '#409EFF', desc: '胆汁淤积时易缺乏脂溶性维生素' },
      { label: '皮肤瘙痒', value: '每日', unit: '评分', color: '#f56c6c', desc: '与胆汁酸负荷和生活质量相关' },
      { label: '总胆汁酸', value: '动态', unit: '复查', color: '#67c23a', desc: '评估胆汁淤积活动度' }
    ],
    foods: {
      red: ['油炸食品', '高脂奶油', '暴饮暴食'],
      yellow: ['坚果', '全脂奶', '肥肉'],
      green: ['中链脂肪配方', '鱼肉', '鸡蛋清', '米面主食', '深色蔬菜']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '低脂牛奶，鸡蛋清，软面包', nutrition: '兼顾能量与低脂负担' },
      { type: 'warning', time: '午餐', menu: '米饭，清蒸鱼，焯青菜', nutrition: '补充优质蛋白' },
      { type: 'info', time: '晚餐', menu: '面片汤，豆腐，少量中链脂肪配方', nutrition: '配合脂溶性维生素方案' }
    ]
  },
  'PFIC3/ABCB4': {
    targets: [
      { label: '胆汁淤积', value: '复查', unit: 'GGT/TBA', color: '#f56c6c', desc: 'PFIC3 常伴 GGT 升高' },
      { label: '脂溶维生素', value: '补充', unit: 'A/D/E/K', color: '#409EFF', desc: '降低长期胆汁淤积相关营养风险' },
      { label: '能量摄入', value: '足量', unit: '成长', color: '#67c23a', desc: '避免因忌口影响儿童生长' }
    ],
    foods: {
      red: ['油炸食品', '动物油', '高糖饮料'],
      yellow: ['肥肉', '奶酪', '蛋黄'],
      green: ['鱼肉', '鸡胸肉', '豆腐', '米饭', '绿叶菜', '中链脂肪配方']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '燕麦粥，鸡蛋白，低脂牛奶', nutrition: '温和供能' },
      { type: 'warning', time: '午餐', menu: '米饭，鸡胸肉，胡萝卜青菜', nutrition: '低脂高蛋白' },
      { type: 'info', time: '晚餐', menu: '豆腐汤，少量面食，蒸南瓜', nutrition: '减少油脂负担' }
    ]
  },
  Gilbert综合征: {
    targets: [
      { label: '空腹时间', value: '< 10', unit: '小时', color: '#f56c6c', desc: '饥饿可诱发胆红素波动' },
      { label: '作息', value: '规律', unit: '优先', color: '#409EFF', desc: '熬夜和疲劳可加重巩膜黄染' },
      { label: '饮水', value: '充足', unit: '每日', color: '#67c23a', desc: '维持基础代谢稳定' }
    ],
    foods: {
      red: ['长时间禁食', '极端节食', '大量酒精'],
      yellow: ['高强度运动后不进食', '过量咖啡'],
      green: ['规律三餐', '鸡蛋', '牛奶', '米面主食', '水果', '蔬菜']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '牛奶，鸡蛋，全麦面包', nutrition: '避免晨起空腹过久' },
      { type: 'warning', time: '午餐', menu: '米饭，瘦肉，时蔬', nutrition: '保持规律供能' },
      { type: 'info', time: '晚餐', menu: '面条，豆腐，水果', nutrition: '避免节食和过劳' }
    ]
  },
  'Dubin-Johnson综合征': {
    targets: [
      { label: '直接胆红素', value: '随访', unit: '趋势', color: '#409EFF', desc: '以趋势观察为主' },
      { label: '药物负担', value: '避免', unit: '不必要', color: '#f56c6c', desc: '减少误用药和重复治疗' },
      { label: '生活节律', value: '稳定', unit: '每日', color: '#67c23a', desc: '帮助判断诱发因素' }
    ],
    foods: {
      red: ['大量酒精', '不明保健品', '长期熬夜'],
      yellow: ['高油夜宵', '浓茶咖啡过量'],
      green: ['均衡主食', '瘦肉', '鱼肉', '蔬菜', '水果', '低脂奶']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '小米粥，鸡蛋，苹果', nutrition: '清淡均衡' },
      { type: 'warning', time: '午餐', menu: '米饭，鱼肉，绿叶菜', nutrition: '稳定蛋白摄入' },
      { type: 'info', time: '晚餐', menu: '豆腐蔬菜汤，面食', nutrition: '避免高油夜宵' }
    ]
  },
  Alagille综合征: {
    targets: [
      { label: '能量摄入', value: '高能', unit: '成长', color: '#409EFF', desc: '儿童胆汁淤积需保障生长发育' },
      { label: '脂溶维生素', value: '补充', unit: 'A/D/E/K', color: '#f56c6c', desc: '长期胆汁淤积高风险缺乏' },
      { label: '瘙痒评分', value: '每日', unit: '记录', color: '#67c23a', desc: '辅助评估干预效果' }
    ],
    foods: {
      red: ['油炸食品', '高糖饮料', '随意停用补充剂'],
      yellow: ['肥肉', '全脂奶酪', '坚果过量'],
      green: ['中链脂肪配方', '鱼肉', '鸡蛋清', '米饭', '蔬菜泥', '酸奶']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '酸奶，鸡蛋清，软面包', nutrition: '高能量且低油脂负担' },
      { type: 'warning', time: '午餐', menu: '米饭，清蒸鱼，胡萝卜泥', nutrition: '配合维生素补充' },
      { type: 'info', time: '晚餐', menu: '蔬菜豆腐汤，小份面食', nutrition: '关注瘙痒和消化耐受' }
    ]
  },
  'NTCP缺乏症/SLC10A1': {
    targets: [
      { label: '总胆汁酸', value: '随访', unit: '趋势', color: '#409EFF', desc: '单项升高时重点观察趋势' },
      { label: '肝酶', value: '稳定', unit: '复查', color: '#67c23a', desc: '多数以正常或轻度异常为主' },
      { label: '营养限制', value: '避免', unit: '过度', color: '#f56c6c', desc: '避免因误解造成儿童营养不足' }
    ],
    foods: {
      red: ['极端低脂饮食', '不明保健品', '长期禁食'],
      yellow: ['油炸食品', '甜饮料'],
      green: ['均衡三餐', '牛奶', '鸡蛋', '鱼肉', '米面主食', '水果']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '牛奶，鸡蛋，馒头', nutrition: '不做过度忌口' },
      { type: 'warning', time: '午餐', menu: '米饭，瘦肉，青菜', nutrition: '保持均衡供能' },
      { type: 'info', time: '晚餐', menu: '鱼肉面片汤，水果', nutrition: '随访胆汁酸趋势' }
    ]
  },
  '溶酶体酸性脂肪酶缺乏症/LIPA': {
    targets: [
      { label: 'LDL-C', value: '控制', unit: '目标', color: '#f56c6c', desc: 'LAL-D 常见 LDL-C 升高' },
      { label: '甘油三酯', value: '下降', unit: '趋势', color: '#e6a23c', desc: '配合血脂和肝酶随访' },
      { label: '肝纤维化', value: '评估', unit: '周期', color: '#409EFF', desc: '关注长期进展风险' }
    ],
    foods: {
      red: ['动物内脏', '肥肉', '奶油点心', '油炸食品'],
      yellow: ['蛋黄', '奶酪', '坚果过量'],
      green: ['鱼肉', '鸡胸肉', '燕麦', '豆制品', '绿叶菜', '低脂奶']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '燕麦，低脂牛奶，苹果', nutrition: '低胆固醇高纤维' },
      { type: 'warning', time: '午餐', menu: '米饭，鸡胸肉，绿叶菜', nutrition: '控制饱和脂肪' },
      { type: 'info', time: '晚餐', menu: '清蒸鱼，豆腐汤，杂粮饭', nutrition: '兼顾血脂和肝酶' }
    ]
  },
  '糖原累积病I型/G6PC': {
    targets: [
      { label: '空腹时间', value: '< 4', unit: '小时', color: '#f56c6c', desc: '避免长时间空腹诱发低血糖' },
      { label: '夜间加餐', value: '计划', unit: '执行', color: '#409EFF', desc: '配合生玉米淀粉或加餐方案' },
      { label: '尿酸/血脂', value: '监测', unit: '复查', color: '#67c23a', desc: '关注代谢并发症' }
    ],
    foods: {
      red: ['长时间禁食', '高果糖饮料', '大量蔗糖甜点'],
      yellow: ['水果汁', '蜂蜜', '甜奶茶'],
      green: ['生玉米淀粉方案', '米面主食', '瘦肉', '鸡蛋', '蔬菜', '无糖酸奶']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '米粥，鸡蛋，无糖酸奶', nutrition: '晨起及时供能' },
      { type: 'warning', time: '午餐', menu: '米饭，瘦肉，青菜', nutrition: '避免高糖饮料' },
      { type: 'info', time: '晚餐', menu: '面食，豆腐，睡前按医嘱加餐', nutrition: '降低夜间低血糖风险' }
    ]
  },
  'α1-抗胰蛋白酶缺乏症': {
    targets: [
      { label: '蛋白摄入', value: '充足', unit: '每日', color: '#409EFF', desc: '支持肝细胞修复和生长发育' },
      { label: '烟草暴露', value: '避免', unit: '全部', color: '#f56c6c', desc: '降低肺部受累风险' },
      { label: '肝肺随访', value: '联合', unit: '评估', color: '#67c23a', desc: '同步观察肝酶和肺功能' }
    ],
    foods: {
      red: ['酒精', '二手烟环境', '高油夜宵'],
      yellow: ['肥肉', '油炸食品', '含糖饮料'],
      green: ['鱼肉', '鸡蛋', '低脂奶', '豆腐', '米饭', '蔬菜']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '低脂牛奶，鸡蛋，全麦面包', nutrition: '高蛋白低脂' },
      { type: 'warning', time: '午餐', menu: '米饭，鱼肉，青菜', nutrition: '减少油脂负担' },
      { type: 'info', time: '晚餐', menu: '豆腐汤，鸡胸肉，杂粮饭', nutrition: '配合肝肺随访' }
    ]
  },
  '遗传性血色病': {
    targets: [
      { label: '每日铁摄入量', value: '极低', unit: '控制', color: '#f56c6c', desc: '严格控制富含血红素铁的食物' },
      { label: '维生素C摄入', value: '避免', unit: '随餐', color: '#e6a23c', desc: '维C会显著增加铁吸收率' },
      { label: '每日饮茶量', value: '推荐', unit: '随餐', color: '#67c23a', desc: '茶多酚可抑制铁吸收' }
    ],
    foods: {
      red: ['猪血', '鸭血', '动物内脏', '牛排', '铁强化谷物', '维生素C补剂'],
      yellow: ['鸡鸭肉', '深绿色蔬菜', '柑橘类水果'],
      green: ['精制谷物', '鸡蛋', '奶制品', '根茎类蔬菜', '红茶']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '白米粥，水煮鸡蛋，热红茶', nutrition: '随餐茶饮抑制铁吸收' },
      { type: 'warning', time: '午餐', menu: '素炒西葫芦，清炖豆腐，白米饭', nutrition: '极低血红素铁' },
      { type: 'info', time: '晚餐', menu: '鸡胸肉沙拉，全麦面包，脱脂牛奶', nutrition: '避免维C同餐' }
    ]
  },
  青年型遗传性血色病: {
    targets: [
      { label: '血红素铁', value: '限制', unit: '严格', color: '#f56c6c', desc: '青年型铁过载需控制高铁来源' },
      { label: '维生素C', value: '避免', unit: '随餐', color: '#e6a23c', desc: '避免促进铁吸收' },
      { label: '家系筛查', value: '建议', unit: '一级亲属', color: '#409EFF', desc: '青年起病提示遗传风险更高' }
    ],
    foods: {
      red: ['动物内脏', '牛羊肉大量摄入', '猪血鸭血', '铁剂', '维生素C补剂'],
      yellow: ['深绿色蔬菜', '柑橘类水果', '鸡鸭肉'],
      green: ['精制谷物', '鸡蛋', '奶制品', '豆腐', '根茎类蔬菜', '红茶']
    },
    mealPlan: [
      { type: 'success', time: '早餐', menu: '白米粥，鸡蛋，热红茶', nutrition: '随餐茶饮抑制铁吸收' },
      { type: 'warning', time: '午餐', menu: '豆腐青菜，白米饭，少量鸡胸肉', nutrition: '限制血红素铁' },
      { type: 'info', time: '晚餐', menu: '蔬菜面，低脂牛奶，苹果', nutrition: '避免维C补剂同餐' }
    ]
  }
}

const memoryStorage = new Map()
const clone = (input) => JSON.parse(JSON.stringify(input))

const getBrowserStorage = (storageType = 'local') => {
  if (typeof window === 'undefined') return null
  try {
    const storage = storageType === 'session' ? window.sessionStorage : window.localStorage
    return storage || null
  } catch {
    return null
  }
}

const safeRead = (key, fallback, storageType = 'local') => {
  const storage = getBrowserStorage(storageType)
  if (!storage) return fallback
  try {
    const raw = storage.getItem(key)
    if (!raw) return fallback
    return JSON.parse(raw)
  } catch {
    return fallback
  }
}

const safeWrite = (key, value, storageType = 'local') => {
  const storage = getBrowserStorage(storageType)
  if (!storage) return
  try {
    storage.setItem(key, JSON.stringify(value))
  } catch {
    // ignore
  }
}

const safeReadMemory = (key, fallback) => {
  if (!memoryStorage.has(key)) return fallback
  try {
    return JSON.parse(memoryStorage.get(key))
  } catch {
    return fallback
  }
}

const safeWriteMemory = (key, value) => {
  memoryStorage.set(key, JSON.stringify(value))
}

const seedVersionKey = (storageKey) => `${storageKey}:seed-version`

const makeSeedRecords = () => SEED_PATIENT_RECORDS.map((payload) => ({
  id: `REC-${payload.patientNo}`,
  visitId: payload.visitId || `VISIT-${payload.patientNo}`,
  submittedAt: `${payload.visitDate || '2026-05-01'}T08:30:00.000Z`,
  payload
}))

const replaceSeedItems = (items, seeded, getId) => {
  const seedIds = new Set(seeded.map(getId))
  const customItems = Array.isArray(items)
    ? items.filter((item) => !seedIds.has(getId(item)))
    : []
  return [...clone(seeded), ...customItems]
}

const migrateSeededItems = (storageKey, items, seeded, getId) => {
  const versionKey = seedVersionKey(storageKey)
  const version = safeRead(versionKey, '')
  if (version === MOCK_SEED_VERSION) {
    return items
  }

  const migrated = replaceSeedItems(items, seeded, getId)
  safeWrite(storageKey, migrated)
  safeWrite(versionKey, MOCK_SEED_VERSION)
  return migrated
}

const stripSeedDietOverrides = (items) => {
  const next = {}
  Object.entries(items || {}).forEach(([patientId, value]) => {
    if (!SEED_PATIENT_IDS.has(patientId)) {
      next[patientId] = value
    }
  })
  return next
}

const shouldPersistDiagnosisReports = () => {
  return String(import.meta.env.VITE_MOCK_DIAGNOSIS_PERSIST ?? '').toLowerCase() === 'true'
}

const diagnosisReportStorageType = () => shouldPersistDiagnosisReports() ? 'local' : 'session'

const readDiagnosisReports = (fallback) => {
  const storageType = diagnosisReportStorageType()
  if (getBrowserStorage(storageType)) {
    return safeRead(MOCK_REPORTS_KEY, fallback, storageType)
  }
  return safeReadMemory(MOCK_REPORTS_KEY, fallback)
}

const writeDiagnosisReports = (value) => {
  const storageType = diagnosisReportStorageType()
  if (getBrowserStorage(storageType)) {
    safeWrite(MOCK_REPORTS_KEY, value, storageType)
    return
  }
  safeWriteMemory(MOCK_REPORTS_KEY, value)
}

const readDiagnosisReportSeedVersion = () => {
  const storageType = diagnosisReportStorageType()
  const key = seedVersionKey(MOCK_REPORTS_KEY)
  if (getBrowserStorage(storageType)) {
    return safeRead(key, '', storageType)
  }
  return safeReadMemory(key, '')
}

const writeDiagnosisReportSeedVersion = () => {
  const storageType = diagnosisReportStorageType()
  const key = seedVersionKey(MOCK_REPORTS_KEY)
  if (getBrowserStorage(storageType)) {
    safeWrite(key, MOCK_SEED_VERSION, storageType)
    return
  }
  safeWriteMemory(key, MOCK_SEED_VERSION)
}

const migrateDiagnosisReports = (reports = []) => {
  if (readDiagnosisReportSeedVersion() === MOCK_SEED_VERSION) {
    return reports
  }

  const customReports = reports.filter((item) => !FIXED_REPORT_IDS.has(item?.id))
  const migrated = [...clone(SEED_REPORTS), ...customReports]
  writeDiagnosisReports(migrated)
  writeDiagnosisReportSeedVersion()
  return migrated
}

export const loadUsers = () => {
  const users = safeRead(MOCK_USERS_KEY, [])
  if (Array.isArray(users) && users.length > 0) {
    const merged = [...users]
    let changed = false

    DEFAULT_MOCK_USERS.forEach((seedUser) => {
      if (!merged.some((item) => item.username === seedUser.username)) {
        merged.push(seedUser)
        changed = true
      }
    })

    if (changed) {
      safeWrite(MOCK_USERS_KEY, merged)
    }

    return merged
  }

  const seeded = clone(DEFAULT_MOCK_USERS)
  safeWrite(MOCK_USERS_KEY, seeded)
  return seeded
}

export const saveUsers = (users) => safeWrite(MOCK_USERS_KEY, users)

export const loadTokens = () => safeRead(MOCK_TOKENS_KEY, {})
export const saveTokens = (tokens) => safeWrite(MOCK_TOKENS_KEY, tokens)

export const loadPatients = () => {
  const items = safeRead(MOCK_PATIENTS_KEY, [])
  if (Array.isArray(items) && items.length > 0) {
    return migrateSeededItems(MOCK_PATIENTS_KEY, items, SEED_PATIENTS, (item) => item.id)
  }

  const seeded = clone(SEED_PATIENTS)
  safeWrite(MOCK_PATIENTS_KEY, seeded)
  safeWrite(seedVersionKey(MOCK_PATIENTS_KEY), MOCK_SEED_VERSION)
  return seeded
}

export const savePatients = (items) => safeWrite(MOCK_PATIENTS_KEY, items)

export const loadRecords = () => {
  const items = safeRead(MOCK_RECORDS_KEY, [])
  if (Array.isArray(items) && items.length > 0) {
    return migrateSeededItems(
      MOCK_RECORDS_KEY,
      items,
      makeSeedRecords(),
      (item) => item.payload?.patientNo || String(item.id || '').replace(/^REC-/, '')
    )
  }

  const seeded = makeSeedRecords()
  safeWrite(MOCK_RECORDS_KEY, seeded)
  safeWrite(seedVersionKey(MOCK_RECORDS_KEY), MOCK_SEED_VERSION)
  return seeded
}

export const saveRecords = (items) => safeWrite(MOCK_RECORDS_KEY, items)

export const findRecordPayloadByPatientNo = (patientNo) => {
  const target = String(patientNo || '').toLowerCase()
  if (!target) return null
  const found = loadRecords().find(
    (item) => String(item.payload?.patientNo || '').toLowerCase() === target
  )
  return found ? found.payload : null
}

const hydrateFixedDiagnosisReport = (report) => {
  const payload = buildFixedReportDiagnosisPayload(report?.id)
  if (!payload) {
    return { report, changed: false }
  }

  const biochemical = formatIndicatorSummary(payload.indicators)
  const existingIndicators = JSON.stringify(report.diagnosisPayload?.indicators || [])
  const expectedIndicators = JSON.stringify(payload.indicators)
  const changed = existingIndicators !== expectedIndicators || report.aiFindings?.biochemical !== biochemical

  if (!changed) {
    return { report, changed: false }
  }

  return {
    changed: true,
    report: {
      ...report,
      aiFindings: {
        ...report.aiFindings,
        biochemical,
        probability: report.aiFindings?.probability || String(payload.probability),
        disease: report.aiFindings?.disease || payload.diseaseName
      },
      diagnosisPayload: {
        ...(report.diagnosisPayload || {}),
        ...payload
      }
    }
  }
}

const hydrateDiagnosisReports = (reports = []) => {
  let changed = false
  const hydrated = reports.map((report) => {
    const result = hydrateFixedDiagnosisReport(report)
    if (result.changed) {
      changed = true
    }
    return result.report
  })
  return { reports: hydrated, changed }
}

export const loadReports = () => {
  const items = readDiagnosisReports([])
  if (Array.isArray(items) && items.length > 0) {
    const migrated = migrateDiagnosisReports(items)
    const hydrated = hydrateDiagnosisReports(migrated)
    if (hydrated.changed) {
      writeDiagnosisReports(hydrated.reports)
    }
    return hydrated.reports
  }

  const seeded = hydrateDiagnosisReports(clone(SEED_REPORTS)).reports
  writeDiagnosisReports(seeded)
  writeDiagnosisReportSeedVersion()
  return seeded
}

export const saveReports = (items) => writeDiagnosisReports(items)

export const loadDietOverrides = () => {
  const items = safeRead(MOCK_DIET_OVERRIDES_KEY, {})
  const versionKey = seedVersionKey(MOCK_DIET_OVERRIDES_KEY)
  if (safeRead(versionKey, '') === MOCK_SEED_VERSION) {
    return items
  }

  const migrated = stripSeedDietOverrides(items)
  safeWrite(MOCK_DIET_OVERRIDES_KEY, migrated)
  safeWrite(versionKey, MOCK_SEED_VERSION)
  return migrated
}
export const saveDietOverrides = (items) => safeWrite(MOCK_DIET_OVERRIDES_KEY, items)

export const loadEmailCodes = () => safeRead(MOCK_EMAIL_CODES_KEY, {})
export const saveEmailCodes = (items) => safeWrite(MOCK_EMAIL_CODES_KEY, items)

export const createToken = (username) => `mock_${username}_${Math.random().toString(36).slice(2, 11)}`

export const readAuthorizationToken = (headers = {}) => {
  const value =
    (typeof headers.get === 'function' ? headers.get('Authorization') : '') ||
    headers.Authorization ||
    headers.authorization ||
    ''

  if (typeof value !== 'string') return ''
  if (value.startsWith('Token ')) return value.slice(6)
  if (value.startsWith('Bearer ')) return value.slice(7)
  return value
}

export const nextPatientId = (patients) => {
  const maxId = patients.reduce((acc, item) => Math.max(acc, Number(item.id.replace(/^P/, '')) || 0), 0)
  return `P${String(maxId + 1).padStart(3, '0')}`
}

export const resolveDiseaseByDiagnosis = (diagnosis = '') => {
  if (diagnosis.includes('Wilson') || diagnosis.includes('肝豆')) {
    return '肝豆状核变性 (Wilson病)'
  }
  if (diagnosis.includes('Citrin')) {
    return 'Citrin缺乏症'
  }
  if (diagnosis.includes('PFIC2') || diagnosis.includes('ABCB11')) {
    return 'PFIC2/ABCB11'
  }
  if (diagnosis.includes('PFIC3') || diagnosis.includes('ABCB4')) {
    return 'PFIC3/ABCB4'
  }
  if (diagnosis.includes('Gilbert') || diagnosis.includes('吉尔伯特')) {
    return 'Gilbert综合征'
  }
  if (diagnosis.includes('Dubin')) {
    return 'Dubin-Johnson综合征'
  }
  if (diagnosis.includes('Alagille')) {
    return 'Alagille综合征'
  }
  if (diagnosis.includes('NTCP') || diagnosis.includes('SLC10A1')) {
    return 'NTCP缺乏症/SLC10A1'
  }
  if (diagnosis.includes('脂肪酶') || diagnosis.includes('LIPA')) {
    return '溶酶体酸性脂肪酶缺乏症/LIPA'
  }
  if (diagnosis.includes('糖原') || diagnosis.includes('G6PC')) {
    return '糖原累积病I型/G6PC'
  }
  if (diagnosis.includes('抗胰蛋白酶')) {
    return 'α1-抗胰蛋白酶缺乏症'
  }
  if (diagnosis.includes('血色')) {
    return diagnosis.includes('青年型') ? '青年型遗传性血色病' : '遗传性血色病'
  }
  return '肝豆状核变性 (Wilson病)'
}

export const resolveRiskByDiagnosis = (diagnosis = '') => {
  if (diagnosis.includes('低危') || diagnosis.includes('Gilbert')) {
    return '低'
  }
  if (diagnosis.includes('高危') || diagnosis.includes('Wilson') || diagnosis.includes('PFIC') || diagnosis.includes('Citrin') || diagnosis.includes('Alagille') || diagnosis.includes('糖原') || diagnosis.includes('LIPA') || diagnosis.includes('血色')) {
    return '高'
  }
  return '中'
}

function withDiagnosisDisplayFields(payload) {
  return {
    ...payload,
    ...buildDiseaseDisplayFields({
      diseaseName: payload.diseaseName,
      probability: payload.probability,
      genes: payload.genes,
      diet: payload.diet,
      sequencing: payload.sequencing
    })
  }
}

export const buildDiagnosisPayload = (patient) => {
  return withDiagnosisDisplayFields(copyDiagnosisPayload(resolveDiagnosisPayloadTemplate(patient.disease)))
}

export const toAiFinding = (diagnosisPayload) => ({
  biochemical: formatIndicatorSummary(diagnosisPayload.indicators),
  clinical: diagnosisPayload.diet,
  probability: String(diagnosisPayload.probability),
  disease: diagnosisPayload.diseaseName
})
