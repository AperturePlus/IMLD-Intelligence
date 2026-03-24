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

const MOCK_DELAY_MS = Number(import.meta.env.VITE_MOCK_DELAY_MS ?? 280)

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

const DISEASE_CONFIGS = {
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

const parseUrlParts = (input = '') => {
  const url = new URL(input, 'http://mock.local')
  const path = url.pathname.endsWith('/') ? url.pathname : `${url.pathname}/`
  return { path, query: url.searchParams }
}

const parseBody = (data) => {
  if (data == null) return {}
  if (typeof data === 'string') {
    try {
      return JSON.parse(data)
    } catch {
      return {}
    }
  }
  if (typeof data === 'object') return data
  return {}
}

const parseQueryObject = (searchParams, extraParams) => {
  const query = {}
  searchParams.forEach((value, key) => {
    query[key] = value
  })
  if (extraParams && typeof extraParams === 'object') {
    Object.entries(extraParams).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        query[key] = String(value)
      }
    })
  }
  return query
}

const buildResponse = (config, status, data, statusText = 'OK') => ({
  data,
  status,
  statusText,
  headers: {},
  config,
  request: { mocked: true }
})

const wait = (delay) => new Promise((resolve) => setTimeout(resolve, delay))

const loadUsers = () => {
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

const saveUsers = (users) => safeWrite(MOCK_USERS_KEY, users)

const loadTokens = () => safeRead(MOCK_TOKENS_KEY, {})
const saveTokens = (tokens) => safeWrite(MOCK_TOKENS_KEY, tokens)

const loadPatients = () => {
  const items = safeRead(MOCK_PATIENTS_KEY, [])
  if (Array.isArray(items) && items.length > 0) return items
  const seeded = clone(SEED_PATIENTS)
  safeWrite(MOCK_PATIENTS_KEY, seeded)
  return seeded
}

const savePatients = (items) => safeWrite(MOCK_PATIENTS_KEY, items)

const loadRecords = () => safeRead(MOCK_RECORDS_KEY, [])
const saveRecords = (items) => safeWrite(MOCK_RECORDS_KEY, items)

const loadReports = () => {
  const items = safeRead(MOCK_REPORTS_KEY, [])
  if (Array.isArray(items) && items.length > 0) return items
  const seeded = clone(SEED_REPORTS)
  safeWrite(MOCK_REPORTS_KEY, seeded)
  return seeded
}

const saveReports = (items) => safeWrite(MOCK_REPORTS_KEY, items)

const loadDietOverrides = () => safeRead(MOCK_DIET_OVERRIDES_KEY, {})
const saveDietOverrides = (items) => safeWrite(MOCK_DIET_OVERRIDES_KEY, items)

const createToken = (username) => `mock_${username}_${Math.random().toString(36).slice(2, 11)}`

const readAuthorizationToken = (headers = {}) => {
  const value =
    (typeof headers.get === 'function' ? headers.get('Authorization') : '') ||
    headers.Authorization ||
    headers.authorization ||
    ''

  if (typeof value !== 'string') return ''
  if (value.startsWith('Token ')) return value.slice(6)
  return value
}

const nextPatientId = (patients) => {
  const maxId = patients.reduce((acc, item) => Math.max(acc, Number(item.id.replace(/^P/, '')) || 0), 0)
  return `P${String(maxId + 1).padStart(3, '0')}`
}

const resolveDiseaseByDiagnosis = (diagnosis = '') => {
  if (diagnosis.includes('Wilson') || diagnosis.includes('肝豆')) {
    return '肝豆状核变性 (Wilson病)'
  }
  if (diagnosis.includes('血色')) {
    return '遗传性血色病'
  }
  return '肝豆状核变性 (Wilson病)'
}

const resolveRiskByDiagnosis = (diagnosis = '') => {
  if (diagnosis.includes('高危') || diagnosis.includes('Wilson') || diagnosis.includes('血色')) {
    return '高'
  }
  return '中'
}

const buildDiagnosisPayload = (patient) => {
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

const toAiFinding = (diagnosisPayload) => ({
  biochemical: diagnosisPayload.indicators.map((item) => `${item.name} ${item.value}${item.unit}`).join('，'),
  clinical: diagnosisPayload.diet,
  probability: String(diagnosisPayload.probability),
  disease: diagnosisPayload.diseaseName
})

const exactHandlers = {
  'POST /dj-rest-auth/login/': async ({ data }) => {
    const { username = '', password = '' } = data
    if (!username || !password) {
      return { status: 400, statusText: 'Bad Request', data: { non_field_errors: ['请输入账号和密码'] } }
    }

    const users = loadUsers()
    const currentUser = users.find((item) => item.username === username)
    if (!currentUser || currentUser.password !== password) {
      return { status: 400, statusText: 'Bad Request', data: { non_field_errors: ['账号或密码错误'] } }
    }

    const token = createToken(username)
    const tokens = loadTokens()
    tokens[token] = username
    saveTokens(tokens)

    return {
      status: 200,
      data: {
        key: token,
        token,
        access: token,
        username
      }
    }
  },

  'POST /dj-rest-auth/registration/': async ({ data }) => {
    const { username = '', email = '', password1 = '', password2 = '' } = data
    const errors = {}

    if (!username) errors.username = ['请输入账号']
    if (!email) errors.email = ['请输入邮箱']
    if (!password1) errors.password1 = ['请输入密码']
    if (password1 && password1.length < 6) errors.password1 = ['密码至少为 6 位']
    if (password1 !== password2) errors.password2 = ['两次密码不一致']

    const users = loadUsers()
    if (users.some((item) => item.username === username)) {
      errors.username = ['该账号已存在']
    }

    if (Object.keys(errors).length > 0) {
      return { status: 400, statusText: 'Bad Request', data: errors }
    }

    users.push({ username, email, password: password1, role: 'doctor' })
    saveUsers(users)
    return { status: 201, statusText: 'Created', data: { detail: '注册成功' } }
  },

  'GET /dj-rest-auth/user/': async ({ headers }) => {
    const token = readAuthorizationToken(headers)
    if (!token) return { status: 401, statusText: 'Unauthorized', data: { detail: '未认证' } }

    const tokenMap = loadTokens()
    const username = tokenMap[token]
    if (!username) return { status: 401, statusText: 'Unauthorized', data: { detail: '凭证无效或已过期' } }

    const user = loadUsers().find((item) => item.username === username)
    if (!user) return { status: 404, statusText: 'Not Found', data: { detail: '用户不存在' } }

    return {
      status: 200,
      data: {
        username: user.username,
        email: user.email,
        role: user.role
      }
    }
  },

  'GET /api/v1/patients/': async ({ query }) => {
    const keyword = (query.keyword || '').trim()
    const items = loadPatients()
      .filter((item) => !keyword || item.name.includes(keyword) || item.id.includes(keyword))
      .map((item) => ({
        id: item.id,
        name: item.name,
        gender: item.gender,
        age: item.age,
        riskLevel: item.riskLevel,
        avatar: item.avatar
      }))

    return { status: 200, data: { items } }
  },

  'POST /api/v1/patient-records/': async ({ data }) => {
    const requiredFields = ['name', 'gender', 'age', 'visitDate', 'chiefComplaint', 'diagnosis']
    const errors = {}

    requiredFields.forEach((field) => {
      if (!data[field]) {
        errors[field] = [`${field}不能为空`]
      }
    })

    if (Object.keys(errors).length > 0) {
      return { status: 400, statusText: 'Bad Request', data: errors }
    }

    const records = loadRecords()
    const patients = loadPatients()
    const now = Date.now()
    const recordId = `REC-${now}`
    const visitId = data.visitId || `VISIT-${String(now).slice(-8)}`

    records.push({
      id: recordId,
      visitId,
      submittedAt: new Date().toISOString(),
      payload: data
    })
    saveRecords(records)

    const existing = patients.find((item) => item.name === data.name)
    if (!existing) {
      const disease = resolveDiseaseByDiagnosis(data.diagnosis)
      patients.unshift({
        id: nextPatientId(patients),
        name: data.name,
        gender: data.gender,
        age: Number(data.age) || 0,
        riskLevel: resolveRiskByDiagnosis(data.diagnosis),
        disease,
        compliance: '一般',
        aiStatus: '未诊断',
        avatar: `https://randomuser.me/api/portraits/${data.gender === '女' ? 'women' : 'men'}/${Math.floor(Math.random() * 80) + 10}.jpg`
      })
      savePatients(patients)
    }

    return {
      status: 201,
      statusText: 'Created',
      data: {
        recordId,
        visitId,
        message: '病历归档成功'
      }
    }
  },

  'GET /api/v1/diagnosis/ai-queue/': async () => {
    const items = loadPatients().map((item) => ({
      id: item.id,
      name: item.name,
      gender: item.gender,
      age: item.age,
      avatar: item.avatar,
      aiStatus: item.aiStatus || '未诊断'
    }))

    return { status: 200, data: { items } }
  },

  'POST /api/v1/diagnosis/ai-reports/': async ({ data }) => {
    const patientId = data.patientId
    const patients = loadPatients()
    const patient = patients.find((item) => item.id === patientId)

    if (!patient) {
      return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
    }

    const diagnosisPayload = buildDiagnosisPayload(patient)
    patient.aiStatus = '已诊断'
    savePatients(patients)

    const reports = loadReports()
    const existed = reports.find((item) => item.patientId === patient.id && item.status === '待签发')
    if (!existed) {
      const reportId = `REP-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(Math.floor(Math.random() * 900) + 100)}`
      reports.unshift({
        id: reportId,
        patientId: patient.id,
        visitId: `MZ${Math.floor(Math.random() * 9000000 + 1000000)}`,
        patientName: patient.name,
        gender: patient.gender,
        age: patient.age,
        date: new Date().toISOString().slice(0, 10),
        status: '待签发',
        aiFindings: toAiFinding(diagnosisPayload),
        expertConclusion: '',
        treatmentPlan: ''
      })
      saveReports(reports)
    }

    return { status: 200, data: diagnosisPayload }
  },

  'GET /api/v1/diagnosis/expert-reports/': async () => {
    return { status: 200, data: { items: loadReports() } }
  },

  'GET /api/v1/screening/overview/': async () => {
    return {
      status: 200,
      data: {
        updatedAt: new Date().toLocaleString(),
        statCards: [
          { title: '累计筛查总人数', value: 12450, color: '#409EFF', icon: 'User', trend: 5.2 },
          { title: '检出高危/阳性', value: 342, color: '#f56c6c', icon: 'WarnTriangleFilled', trend: 1.5, suffix: '例' },
          { title: '基因突变携带率', value: 2.8, color: '#e6a23c', icon: 'TrendCharts', trend: -0.3, suffix: '%' },
          { title: 'AI 干预采纳数', value: 890, color: '#67c23a', icon: 'MagicStick', trend: 12.4, suffix: '次' }
        ],
        riskDistribution: [
          { level: '极高', count: 85, percentage: 15, color: '#f56c6c' },
          { level: '高', count: 257, percentage: 35, color: '#e6a23c' },
          { level: '中', count: 420, percentage: 55, color: '#e6a23c' },
          { level: '低', count: 11688, percentage: 100, color: '#67c23a' }
        ],
        topGenes: [
          { name: 'ATP7B', desc: '肝豆状核变性', percentage: 82 },
          { name: 'HFE', desc: '遗传性血色病', percentage: 65 },
          { name: 'SERPINA1', desc: 'α1-抗胰蛋白酶缺乏', percentage: 48 },
          { name: 'SLC37A4', desc: '糖原累积病 Ib型', percentage: 30 },
          { name: 'UGT1A1', desc: 'Gilbert综合征', percentage: 22 },
          { name: 'G6PC', desc: '糖原累积病 Ia型', percentage: 15 },
          { name: 'NPC1', desc: '尼曼-匹克病', percentage: 8 }
        ],
        aiEfficiency: {
          diagnosisMatchRate: 94.6,
          missRate: '0.2%',
          avgDuration: '1.2s'
        },
        highRiskPatients: [
          { date: '2023-11-24', name: '李*豪', age: 14, clue: '铜蓝蛋白极低 (0.05 g/L)，AST/ALT 比例失调', aiSuggest: 'Wilson病 (极高危)' },
          { date: '2023-11-23', name: '赵*刚', age: 52, clue: '铁蛋白 > 1000 ng/mL，转铁蛋白饱和度 75%', aiSuggest: '遗传性血色病' },
          { date: '2023-11-22', name: '陈*明', age: 28, clue: '持续性非结合胆红素升高', aiSuggest: 'Gilbert综合征' },
          { date: '2023-11-21', name: '王*宇', age: 6, clue: '空腹低血糖伴乳酸酸中毒', aiSuggest: '糖原累积病' }
        ]
      }
    }
  },

  'GET /api/v1/diet/patients/': async ({ query }) => {
    const keyword = (query.keyword || '').trim()
    const items = loadPatients()
      .filter((item) => !keyword || item.name.includes(keyword) || item.id.includes(keyword))
      .map((item) => ({
        id: item.id,
        name: item.name,
        gender: item.gender,
        age: item.age,
        avatar: item.avatar,
        disease: item.disease,
        compliance: item.compliance || '一般'
      }))

    return { status: 200, data: { items } }
  }
}

const dynamicHandlers = [
  {
    method: 'POST',
    pattern: /^\/api\/v1\/diagnosis\/expert-reports\/([^/]+)\/sign\/$/,
    buildParams: (match) => ({ reportId: match[1] }),
    handler: async ({ params, data }) => {
      const reports = loadReports()
      const report = reports.find((item) => item.id === params.reportId)
      if (!report) {
        return { status: 404, statusText: 'Not Found', data: { detail: '报告不存在' } }
      }

      if (!data.expertConclusion || !data.treatmentPlan) {
        return { status: 400, statusText: 'Bad Request', data: { detail: '签发前请完善专家结论和干预计划' } }
      }

      report.status = '已签发'
      report.expertConclusion = data.expertConclusion
      report.treatmentPlan = data.treatmentPlan
      report.signedAt = new Date().toISOString()
      saveReports(reports)

      return { status: 200, data: { report } }
    }
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/diet\/patients\/([^/]+)\/plan\/$/,
    buildParams: (match) => ({ patientId: match[1] }),
    handler: async ({ params }) => {
      const patient = loadPatients().find((item) => item.id === params.patientId)
      if (!patient) {
        return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
      }

      const overrides = loadDietOverrides()
      const config = DISEASE_CONFIGS[patient.disease] || DISEASE_CONFIGS['肝豆状核变性 (Wilson病)']

      return {
        status: 200,
        data: {
          targets: config.targets,
          foods: config.foods,
          mealPlan: overrides[patient.id] || config.mealPlan
        }
      }
    }
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/diet\/patients\/([^/]+)\/regenerate\/$/,
    buildParams: (match) => ({ patientId: match[1] }),
    handler: async ({ params }) => {
      const patient = loadPatients().find((item) => item.id === params.patientId)
      if (!patient) {
        return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
      }

      const config = DISEASE_CONFIGS[patient.disease] || DISEASE_CONFIGS['肝豆状核变性 (Wilson病)']
      const overrides = loadDietOverrides()
      const basePlan = overrides[patient.id] || config.mealPlan
      const nextPlan = [...basePlan.slice(1), basePlan[0]].map((item, index) => ({
        ...item,
        nutrition: `${item.nutrition} · 方案版本 ${index + 1}`
      }))

      overrides[patient.id] = nextPlan
      saveDietOverrides(overrides)

      return {
        status: 200,
        data: {
          mealPlan: nextPlan,
          regeneratedAt: new Date().toISOString()
        }
      }
    }
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/diet\/patients\/([^/]+)\/push\/$/,
    buildParams: (match) => ({ patientId: match[1] }),
    handler: async ({ params }) => {
      const patient = loadPatients().find((item) => item.id === params.patientId)
      if (!patient) {
        return { status: 404, statusText: 'Not Found', data: { detail: '患者不存在' } }
      }

      return {
        status: 200,
        data: {
          delivered: true,
          patientId: patient.id,
          deliveredAt: new Date().toISOString()
        }
      }
    }
  }
]

const resolveHandler = (method, path) => {
  const routeKey = `${method} ${path}`
  if (exactHandlers[routeKey]) {
    return { handler: exactHandlers[routeKey], params: {} }
  }

  for (const route of dynamicHandlers) {
    if (route.method !== method) continue
    const matched = path.match(route.pattern)
    if (matched) {
      return {
        handler: route.handler,
        params: route.buildParams ? route.buildParams(matched) : {}
      }
    }
  }

  return null
}

export const isMockEnabled = () => import.meta.env.DEV && import.meta.env.VITE_USE_MOCK === 'true'

export const createMockAdapter = (config) => {
  const method = (config.method || 'get').toUpperCase()
  const { path } = parseUrlParts(config.url || '')
  const resolved = resolveHandler(method, path)

  if (!resolved) {
    return null
  }

  return async (adapterConfig) => {
    await wait(MOCK_DELAY_MS)

    const { query: rawQuery } = parseUrlParts(adapterConfig.url || config.url || '')
    const query = parseQueryObject(rawQuery, adapterConfig.params)

    const result = await resolved.handler({
      data: parseBody(adapterConfig.data),
      headers: adapterConfig.headers || {},
      query,
      params: resolved.params,
      config: adapterConfig
    })

    return buildResponse(
      adapterConfig,
      result.status,
      result.data,
      result.statusText || 'OK'
    )
  }
}
