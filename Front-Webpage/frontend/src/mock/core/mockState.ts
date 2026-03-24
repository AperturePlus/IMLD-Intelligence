// @ts-nocheck

const MOCK_USERS_KEY = '__imld_mock_users__'
const MOCK_TOKENS_KEY = '__imld_mock_tokens__'
const MOCK_PATIENTS_KEY = '__imld_mock_patients__'
const MOCK_RECORDS_KEY = '__imld_mock_records__'
const MOCK_REPORTS_KEY = '__imld_mock_reports__'
const MOCK_DIET_OVERRIDES_KEY = '__imld_mock_diet_overrides__'

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

const SEED_PATIENTS = [
  { id: 'P001', name: '林建国', gender: '男', age: 58, riskLevel: '高', disease: '遗传性血色病', compliance: '一般', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/men/32.jpg' },
  { id: 'P002', name: '陈婉婷', gender: '女', age: 32, riskLevel: '低', disease: '肝豆状核变性 (Wilson病)', compliance: '极佳', aiStatus: '已诊断', avatar: 'https://randomuser.me/api/portraits/women/44.jpg' },
  { id: 'P003', name: '张明远', gender: '男', age: 45, riskLevel: '中', disease: 'α1-抗胰蛋白酶缺乏症', compliance: '良好', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/men/67.jpg' },
  { id: 'P004', name: '王淑芬', gender: '女', age: 62, riskLevel: '高', disease: '代谢相关脂肪性肝病', compliance: '良好', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/women/68.jpg' },
  { id: 'P005', name: '李浩宇', gender: '男', age: 28, riskLevel: '低', disease: 'Gilbert综合征', compliance: '良好', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/men/22.jpg' },
  { id: 'P006', name: '赵雪梅', gender: '女', age: 51, riskLevel: '中', disease: '遗传性血色病', compliance: '一般', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/women/33.jpg' },
  { id: 'P007', name: '刘振华', gender: '男', age: 66, riskLevel: '高', disease: '肝豆状核变性 (Wilson病)', compliance: '差', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/men/45.jpg' },
  { id: 'P008', name: '周小雅', gender: '女', age: 24, riskLevel: '低', disease: '肝豆状核变性 (Wilson病)', compliance: '良好', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/women/12.jpg' },
  { id: 'P009', name: '吴建强', gender: '男', age: 53, riskLevel: '高', disease: '遗传性血色病', compliance: '一般', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/men/51.jpg' },
  { id: 'P010', name: '郑丽丽', gender: '女', age: 38, riskLevel: '中', disease: '肝豆状核变性 (Wilson病)', compliance: '良好', aiStatus: '已诊断', avatar: 'https://randomuser.me/api/portraits/women/25.jpg' },
  { id: 'P011', name: '孙立军', gender: '男', age: 41, riskLevel: '低', disease: '遗传性血色病', compliance: '一般', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/men/18.jpg' },
  { id: 'P012', name: '马桂英', gender: '女', age: 71, riskLevel: '高', disease: '代谢相关脂肪性肝病', compliance: '一般', aiStatus: '未诊断', avatar: 'https://randomuser.me/api/portraits/women/71.jpg' }
]

const SEED_REPORTS = [
  {
    id: 'REP-202311-001',
    patientId: 'P001',
    visitId: 'MZ8849201',
    patientName: '林建国',
    gender: '男',
    age: 58,
    date: '2023-11-20',
    status: '待签发',
    aiFindings: {
      biochemical: '血清铁蛋白 850 ng/mL (显著升高), 转铁蛋白饱和度 65% (异常)。',
      clinical: '皮肤色素沉着伴轻度肝肿大，无角膜 K-F 环。',
      probability: '89',
      disease: '遗传性血色病'
    },
    expertConclusion: '同意 AI 辅助诊断意见。患者铁代谢指标显著异常，结合临床表型，考虑遗传性血色病可能性大。',
    treatmentPlan: '建议：完善 HFE 基因检测，评估后考虑启动静脉放血治疗，并严格限制高铁饮食摄入。'
  },
  {
    id: 'REP-202311-002',
    patientId: 'P002',
    visitId: 'MZ8849205',
    patientName: '陈婉婷',
    gender: '女',
    age: 32,
    date: '2023-11-21',
    status: '已签发',
    aiFindings: {
      biochemical: '铜蓝蛋白 0.08 g/L (极低), 24h尿铜 215 μg (升高), ALT 125 U/L。',
      clinical: '双眼角膜 K-F 环 (+)，伴有轻微非对称性手部震颤。',
      probability: '96',
      disease: '肝豆状核变性 (Wilson病)'
    },
    expertConclusion: '根据生化指标及裂隙灯检查结果（K-F环阳性），Wilson病诊断明确。',
    treatmentPlan: '立即启动青霉胺驱铜治疗，严格低铜饮食，并建议一级亲属开展 ATP7B 基因筛查。'
  },
  {
    id: 'REP-202311-003',
    patientId: 'P003',
    visitId: 'MZ8849212',
    patientName: '张明远',
    gender: '男',
    age: 45,
    date: '2023-11-22',
    status: '待签发',
    aiFindings: {
      biochemical: '血清 α1-抗胰蛋白酶水平 < 0.5 g/L (显著降低)。',
      clinical: '早期肺气肿改变，伴有不明原因肝硬化。',
      probability: '85',
      disease: 'α1-抗胰蛋白酶缺乏症'
    },
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
  }
}

const hasStorage = () => typeof window !== 'undefined' && !!window.localStorage
const clone = (input) => JSON.parse(JSON.stringify(input))

const safeRead = (key, fallback) => {
  if (!hasStorage()) return fallback
  try {
    const raw = window.localStorage.getItem(key)
    if (!raw) return fallback
    return JSON.parse(raw)
  } catch {
    return fallback
  }
}

const safeWrite = (key, value) => {
  if (!hasStorage()) return
  try {
    window.localStorage.setItem(key, JSON.stringify(value))
  } catch {
    // ignore
  }
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
  if (Array.isArray(items) && items.length > 0) return items

  const seeded = clone(SEED_PATIENTS)
  safeWrite(MOCK_PATIENTS_KEY, seeded)
  return seeded
}

export const savePatients = (items) => safeWrite(MOCK_PATIENTS_KEY, items)

export const loadRecords = () => safeRead(MOCK_RECORDS_KEY, [])
export const saveRecords = (items) => safeWrite(MOCK_RECORDS_KEY, items)

export const loadReports = () => {
  const items = safeRead(MOCK_REPORTS_KEY, [])
  if (Array.isArray(items) && items.length > 0) return items

  const seeded = clone(SEED_REPORTS)
  safeWrite(MOCK_REPORTS_KEY, seeded)
  return seeded
}

export const saveReports = (items) => safeWrite(MOCK_REPORTS_KEY, items)

export const loadDietOverrides = () => safeRead(MOCK_DIET_OVERRIDES_KEY, {})
export const saveDietOverrides = (items) => safeWrite(MOCK_DIET_OVERRIDES_KEY, items)

export const createToken = (username) => `mock_${username}_${Math.random().toString(36).slice(2, 11)}`

export const readAuthorizationToken = (headers = {}) => {
  const value =
    (typeof headers.get === 'function' ? headers.get('Authorization') : '') ||
    headers.Authorization ||
    headers.authorization ||
    ''

  if (typeof value !== 'string') return ''
  if (value.startsWith('Token ')) return value.slice(6)
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
  if (diagnosis.includes('血色')) {
    return '遗传性血色病'
  }
  return '肝豆状核变性 (Wilson病)'
}

export const resolveRiskByDiagnosis = (diagnosis = '') => {
  if (diagnosis.includes('高危') || diagnosis.includes('Wilson') || diagnosis.includes('血色')) {
    return '高'
  }
  return '中'
}

export const buildDiagnosisPayload = (patient) => {
  if (patient.disease === '遗传性血色病') {
    return {
      diseaseName: '遗传性血色病',
      probability: 89,
      indicators: [
        { name: '血清铁蛋白', value: 850, unit: 'ng/mL', normal: '30-300', percentage: 92, status: 'exception' },
        { name: '转铁蛋白饱和度', value: 65, unit: '%', normal: '20-45', percentage: 86, status: 'exception' },
        { name: 'ALT', value: 92, unit: 'U/L', normal: '9-50', percentage: 58, status: 'warning' }
      ],
      genes: ['HFE (C282Y)', 'HFE (H63D)'],
      diet: '建议严格限制红肉和动物内脏，避免随餐维生素C补充，餐后可饮茶抑制铁吸收。',
      sequencing: '建议进行 HFE 基因检测，并对一级亲属开展家系筛查。'
    }
  }

  if (patient.disease === 'α1-抗胰蛋白酶缺乏症') {
    return {
      diseaseName: 'α1-抗胰蛋白酶缺乏症',
      probability: 85,
      indicators: [
        { name: 'AAT', value: 0.45, unit: 'g/L', normal: '0.90-2.00', percentage: 15, status: 'exception' },
        { name: 'ALT', value: 78, unit: 'U/L', normal: '9-50', percentage: 52, status: 'warning' },
        { name: 'AST', value: 64, unit: 'U/L', normal: '15-40', percentage: 48, status: 'warning' }
      ],
      genes: ['SERPINA1 (Pi*ZZ)'],
      diet: '建议高蛋白、低脂饮食，减少酒精摄入，配合呼吸系统评估。',
      sequencing: '建议进行 SERPINA1 基因分型，并评估肝肺联合受累风险。'
    }
  }

  return {
    diseaseName: '肝豆状核变性 (Wilson病)',
    probability: 92,
    indicators: [
      { name: '血清铜蓝蛋白', value: 0.08, unit: 'g/L', normal: '0.20-0.60', percentage: 15, status: 'exception' },
      { name: '24h 尿铜', value: 215, unit: 'μg/24h', normal: '< 100', percentage: 85, status: 'exception' },
      { name: 'ALT', value: 125, unit: 'U/L', normal: '9-50', percentage: 60, status: 'warning' }
    ],
    genes: ['ATP7B (c.2333G>T)', 'ATP7B (c.2975C>T)'],
    diet: '建议立即启动低铜饮食，禁食坚果、巧克力和动物内脏。',
    sequencing: '建议 ATP7B 靶向测序，并开展一级亲属筛查。'
  }
}

export const toAiFinding = (diagnosisPayload) => ({
  biochemical: diagnosisPayload.indicators.map((item) => `${item.name} ${item.value}${item.unit}`).join('，'),
  clinical: diagnosisPayload.diet,
  probability: String(diagnosisPayload.probability),
  disease: diagnosisPayload.diseaseName
})
