import type { PatientRecordPayload } from '../../types/patient'
import { createInitialLaboratoryScreening } from '../../features/patient-record/constants/laboratoryScreening'

interface SeedBasics {
  patientNo: string
  name: string
  gender: '男' | '女'
  age: number
  visitDate: string
  occupation?: string
  department?: string
  chiefComplaint: string
  presentIllness: string
}

const estimateBody = (basics: SeedBasics) => {
  const age = basics.age
  const male = basics.gender === '男'
  if (age <= 5) return { heightCm: 110, weightKg: 19 }
  if (age <= 6) return { heightCm: 118, weightKg: 21 }
  if (age <= 7) return { heightCm: 124, weightKg: 23 }
  if (age <= 8) return { heightCm: 130, weightKg: 26 }
  if (age <= 9) return { heightCm: 135, weightKg: 30 }
  if (age <= 11) return { heightCm: 145, weightKg: 38 }
  if (age <= 14) return { heightCm: male ? 165 : 158, weightKg: male ? 52 : 48 }
  if (age <= 17) return { heightCm: male ? 170 : 160, weightKg: male ? 60 : 50 }
  if (age <= 23) return { heightCm: male ? 174 : 162, weightKg: male ? 68 : 52 }
  return { heightCm: male ? 176 : 163, weightKg: male ? 72 : 54 }
}

const calculateBmi = (heightCm: number, weightKg: number) => {
  const meters = heightCm / 100
  return Number((weightKg / (meters * meters)).toFixed(1))
}

const estimateVitals = (age: number) => {
  if (age <= 8) return { systolic: 98, diastolic: 62, heartRate: 88 }
  if (age <= 14) return { systolic: 108, diastolic: 68, heartRate: 82 }
  if (age <= 17) return { systolic: 114, diastolic: 72, heartRate: 78 }
  return { systolic: 120, diastolic: 76, heartRate: 76 }
}

// 正常基线：填充展示/推理关心的化验为正常值，其余留空（视图显示「—」，推理回退中位数）。
const buildBaseline = (basics: SeedBasics): PatientRecordPayload => {
  const labs = createInitialLaboratoryScreening()
  const body = estimateBody(basics)
  const vitals = estimateVitals(basics.age)
  Object.assign(labs.clinicalBiochemistry.liverFunction, {
    tbil: '12', dbil: '4', ibil: '8', alt: '25', ast: '24', astAltRatio: '1.0',
    tp: '72', alb: '45', glob: '27', albGlobRatio: '1.6', glu: '5.0',
    tg: '1.2', chol: '4.2', hdlC: '1.3', ldlC: '2.4', alp: '80', ggt: '30',
    ck: '90', ldh: '180', hbdh: '130', tba: '6', nh3: '35'
  })
  Object.assign(labs.clinicalBiochemistry.renalFunction, {
    urea: '5.0', crea: '70', eGfr: '100', cysC: '0.9', uric: '320'
  })
  Object.assign(labs.clinicalBiochemistry.metabolism, { cer: '280', aat: '1300' })
  Object.assign(labs.clinicalBiochemistry.inflammation, { crp: '2.0', pct: '0.05' })
  Object.assign(labs.clinicalBasic.bloodRoutine, {
    rbc: '4.6', hgb: '140', hct: '0.42', mcv: '90', mch: '30', mchc: '330',
    plt: '220', wbc: '6.2', neutPercent: '58', lymphPercent: '32', monoPercent: '7', eoPercent: '2.5', basoPercent: '0.5'
  })
  Object.assign(labs.clinicalBasic.coagulation, { pivka: '20', pt: '11.5', inr: '1.0', aptt: '32', tt: '17', fdp: '2.5' })
  Object.assign(labs.clinicalImmunology.antibody, { igg: '12', iga: '2.3', igm: '1.3', igg4: '0.6' })
  Object.assign(labs.clinicalImmunology.autoantibody, { ana: '阴性' })

  return {
    patientNo: basics.patientNo,
    name: basics.name,
    gender: basics.gender,
    age: basics.age,
    visitDate: basics.visitDate,
    phone: '13800000000',
    idCard: '',
    occupation: basics.occupation || '职员',
    currentAddress: '四川省成都市武侯区',
    nativePlace: '四川成都',
    department: basics.department || '肝病医学科',
    encounterType: 'OUTPATIENT',
    consanguinity: false,
    chiefComplaint: basics.chiefComplaint,
    presentIllness: basics.presentIllness,
    history: {
      diseaseHistory: {
        smokingHistory: 'NO', drinkingHistory: 'NO', diabetesHistory: 'NO',
        hypertensionHistory: 'NO', hyperuricemiaHistory: 'NO', hyperlipidemiaHistory: 'NO',
        coronaryHeartDiseaseHistory: 'NO', hepatitisBHistory: 'NO'
      },
      surgeryHistory: { status: 'NO', detail: '' },
      transfusionHistory: { status: 'NO', detail: '' },
      allergyHistory: '无',
      medicationHistory: '无',
      familyHistory: '无特殊'
    },
    physicalExam: {
      heightCm: body.heightCm,
      weightKg: body.weightKg,
      bmi: calculateBmi(body.heightCm, body.weightKg),
      bloodPressureSystolic: vitals.systolic, bloodPressureDiastolic: vitals.diastolic,
      respiratoryRate: 18, heartRate: vitals.heartRate,
      liverFibrosis: 'NO', cirrhosis: 'NO', fattyLiver: 'NO',
      liverFailure: 'NO', cholestasis: 'NO', viralHepatitis: 'NO'
    },
    laboratoryScreening: labs,
    imagingReports: [],
    pathology: { performed: false, reportText: '', nasScore: null, fileId: null, sourceType: 'MANUAL' },
    geneticSequencing: { tested: false, method: '', reportSource: '', summary: '', conclusion: '', fileId: null, sourceType: 'MANUAL', variants: [] },
    clinicalDecision: { diagnosis: '', treatmentPlan: '' },
    visitId: `VISIT-${basics.patientNo}`
  }
}

const withOverrides = (
  basics: SeedBasics,
  mutate: (payload: PatientRecordPayload) => void
): PatientRecordPayload => {
  const payload = buildBaseline(basics)
  mutate(payload)
  return payload
}

export const SEED_PATIENT_RECORDS: PatientRecordPayload[] = [
  withOverrides(
    { patientNo: 'P001', name: '方亦辰', gender: '男', age: 14, visitDate: '2026-05-02', occupation: '学生', chiefComplaint: '手抖、学习注意力下降伴肝功能异常2月', presentIllness: '近2月出现细小震颤和注意力下降，体检发现转氨酶升高，裂隙灯提示可疑 K-F 环。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '34', dbil: '12', ibil: '22', alt: '126', ast: '98', ggt: '48', alb: '40' })
      p.laboratoryScreening.clinicalBiochemistry.metabolism.cer = '55'
      p.physicalExam.cholestasis = 'YES'
      p.imagingReports = [{ modality: 'MRI', reportText: '脑 MRI 基底节区 T2 信号轻度异常，需结合铜代谢评估。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'ATP7B 复合杂合变异', conclusion: '支持肝豆状核变性诊断', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ATP7B', hgvsC: 'c.2333G>T' }, { gene: 'ATP7B', hgvsC: 'c.2975C>T' }] }
      p.clinicalDecision = { diagnosis: '肝豆状核变性 (Wilson病)', treatmentPlan: '启动驱铜治疗评估，低铜饮食，建议一级亲属 ATP7B 筛查。' }
    }
  ),
  withOverrides(
    { patientNo: 'P002', name: '梁知夏', gender: '女', age: 8, visitDate: '2026-05-03', occupation: '学生', chiefComplaint: '反复黄疸、厌油伴低血糖发作半年', presentIllness: '半年来反复巩膜黄染和厌油，晨起偶有低血糖样出汗乏力，家属诉高蛋白饮食后症状加重。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '42', dbil: '20', ibil: '22', alt: '92', ast: '75', glu: '3.2', tg: '2.8', nh3: '92' })
      p.physicalExam.cholestasis = 'YES'
      p.geneticSequencing = { tested: true, method: 'WES', reportSource: '院内', summary: 'SLC25A13 复合杂合变异', conclusion: '支持 Citrin 缺乏症', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'SLC25A13', hgvsC: 'c.852_855del' }, { gene: 'SLC25A13', hgvsC: 'c.1638_1660dup' }] }
      p.clinicalDecision = { diagnosis: 'Citrin缺乏症', treatmentPlan: '少量多餐，限制高碳水负荷，补充中链脂肪与脂溶性维生素，遗传咨询。' }
    }
  ),
  withOverrides(
    { patientNo: 'P003', name: '何星澜', gender: '男', age: 6, visitDate: '2026-05-04', occupation: '学龄前', chiefComplaint: '皮肤瘙痒、黄疸伴生长迟缓1年', presentIllness: '1年来反复胆汁淤积性黄疸和皮肤瘙痒，近期出现生长曲线下降，GGT 不高。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '96', dbil: '68', ibil: '28', alt: '180', ast: '150', ggt: '28', alp: '420', tba: '180', alb: '36' })
      Object.assign(p.physicalExam, { cholestasis: 'YES', liverFibrosis: 'YES' })
      p.imagingReports = [{ modality: 'ULTRASOUND', reportText: '肝实质回声增粗，胆道未见明确扩张。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'ABCB11 双等位变异', conclusion: '支持 PFIC2', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ABCB11', hgvsC: 'c.890A>G' }, { gene: 'ABCB11', hgvsP: 'p.Gly982Arg' }] }
      p.clinicalDecision = { diagnosis: 'PFIC2/ABCB11', treatmentPlan: '胆汁淤积专病随访，补充脂溶性维生素，评估 IBAT 抑制剂或肝移植指征。' }
    }
  ),
  withOverrides(
    { patientNo: 'P004', name: '苏念青', gender: '女', age: 11, visitDate: '2026-05-05', occupation: '学生', chiefComplaint: '反复黄疸、皮肤瘙痒伴 GGT 升高8月', presentIllness: '8月来胆汁淤积反复，瘙痒明显，外院提示 GGT 与胆汁酸持续升高。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '72', dbil: '48', ibil: '24', alt: '132', ast: '104', ggt: '210', alp: '460', tba: '130' })
      Object.assign(p.physicalExam, { cholestasis: 'YES', liverFibrosis: 'YES' })
      p.pathology = { performed: true, reportText: '胆管损伤伴门管区纤维化，符合慢性胆汁淤积性改变。', nasScore: 4, fileId: null, sourceType: 'MANUAL' }
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'ABCB4 致病变异', conclusion: '支持 PFIC3', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ABCB4', hgvsC: 'c.959C>T' }] }
      p.clinicalDecision = { diagnosis: 'PFIC3/ABCB4', treatmentPlan: '熊去氧胆酸试用，脂溶性维生素补充，动态评估纤维化进展。' }
    }
  ),
  withOverrides(
    { patientNo: 'P005', name: '江屿川', gender: '男', age: 17, visitDate: '2026-05-06', occupation: '学生', chiefComplaint: '运动后间断巩膜黄染2年', presentIllness: '考试熬夜或运动后出现轻度巩膜黄染，肝酶基本正常，溶血指标阴性。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '36', ibil: '31', dbil: '5', alt: '28', ast: '25' })
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'UGT1A1 启动子多态性', conclusion: '支持 Gilbert 综合征', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'UGT1A1', hgvsC: 'c.-41_-40dup' }] }
      p.clinicalDecision = { diagnosis: 'Gilbert综合征', treatmentPlan: '良性病程，规律作息、避免饥饿与过劳，必要时复查胆红素分型。' }
    }
  ),
  withOverrides(
    { patientNo: 'P006', name: '许安琪', gender: '女', age: 19, visitDate: '2026-05-07', occupation: '学生', chiefComplaint: '直接胆红素升高伴尿色加深1年', presentIllness: '1年来间断尿色加深，体检多次提示直接胆红素升高，肝酶与凝血基本正常。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '58', dbil: '42', ibil: '16', alt: '32', ast: '29', ggt: '34', alp: '92' })
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'ABCC2 双等位变异', conclusion: '支持 Dubin-Johnson 综合征', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ABCC2', hgvsC: 'c.2302C>T' }] }
      p.clinicalDecision = { diagnosis: 'Dubin-Johnson综合征', treatmentPlan: '多为良性经过，避免不必要抗感染治疗，随访胆红素分型与肝功。' }
    }
  ),
  withOverrides(
    { patientNo: 'P007', name: '林沐阳', gender: '男', age: 9, visitDate: '2026-05-08', occupation: '学生', chiefComplaint: '黄疸、瘙痒伴心脏杂音随访', presentIllness: '幼年起有胆汁淤积和皮肤瘙痒，伴外周肺动脉狭窄病史，近期胆红素升高。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '84', dbil: '62', ibil: '22', alt: '96', ast: '82', ggt: '260', alp: '520', tba: '150' })
      Object.assign(p.physicalExam, { cholestasis: 'YES', liverFibrosis: 'YES' })
      p.imagingReports = [{ modality: 'ULTRASOUND', reportText: '肝内胆管稀少可能，建议结合病理与遗传结果。', sourceType: 'MANUAL', fileId: null }]
      p.pathology = { performed: true, reportText: '门管区胆管减少，符合胆管稀少综合征谱系。', nasScore: 3, fileId: null, sourceType: 'MANUAL' }
      p.geneticSequencing = { tested: true, method: 'WES', reportSource: '院内', summary: 'JAG1 致病变异', conclusion: '支持 Alagille 综合征', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'JAG1', hgvsC: 'c.703C>T' }] }
      p.clinicalDecision = { diagnosis: 'Alagille综合征', treatmentPlan: '胆汁淤积和瘙痒分层管理，补充脂溶性维生素，心肝联合随访。' }
    }
  ),
  withOverrides(
    { patientNo: 'P008', name: '程小禾', gender: '女', age: 5, visitDate: '2026-05-09', occupation: '学龄前', chiefComplaint: '体检发现总胆汁酸显著升高', presentIllness: '幼儿园体检发现总胆汁酸持续升高，无明显黄疸和瘙痒，肝酶基本正常。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '18', dbil: '6', ibil: '12', alt: '26', ast: '28', ggt: '24', tba: '185' })
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'SLC10A1 变异', conclusion: '支持 NTCP 缺乏症', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'SLC10A1', hgvsC: 'c.800C>T' }] }
      p.clinicalDecision = { diagnosis: 'NTCP缺乏症/SLC10A1', treatmentPlan: '以随访为主，避免过度治疗，动态观察胆汁酸、胆红素和生长发育。' }
    }
  ),
  withOverrides(
    { patientNo: 'P009', name: '唐景曜', gender: '男', age: 16, visitDate: '2026-05-10', occupation: '学生', chiefComplaint: '肝脾大、血脂异常伴转氨酶升高半年', presentIllness: '半年内多次体检提示 LDL-C 与甘油三酯升高，超声提示肝脾大，否认肥胖和饮酒。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '88', ast: '72', ggt: '75', tg: '3.4', chol: '7.2', ldlC: '5.0' })
      p.history.diseaseHistory.hyperlipidemiaHistory = 'YES'
      Object.assign(p.physicalExam, { liverFibrosis: 'YES', fattyLiver: 'YES' })
      p.imagingReports = [{ modality: 'ULTRASOUND', reportText: '肝脾轻度增大，肝实质回声增强，未见胆道扩张。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'WES', reportSource: '院内', summary: 'LIPA 致病变异', conclusion: '支持溶酶体酸性脂肪酶缺乏症', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'LIPA', hgvsC: 'c.894G>A' }] }
      p.clinicalDecision = { diagnosis: '溶酶体酸性脂肪酶缺乏症/LIPA', treatmentPlan: '低胆固醇饮食，评估酶替代治疗适应证，监测肝纤维化与血脂。' }
    }
  ),
  withOverrides(
    { patientNo: 'P010', name: '韩可一', gender: '女', age: 7, visitDate: '2026-05-11', occupation: '学生', chiefComplaint: '晨起低血糖、腹胀伴肝大1年', presentIllness: '1年来晨起易出汗乏力，进食后缓解，伴腹胀和生长迟缓，外院提示甘油三酯与尿酸升高。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '66', ast: '58', glu: '2.9', tg: '4.8', chol: '5.9' })
      p.laboratoryScreening.clinicalBiochemistry.renalFunction.uric = '520'
      Object.assign(p.physicalExam, { liverFibrosis: 'YES', heightCm: 113, weightKg: 20, bmi: 15.7 })
      p.imagingReports = [{ modality: 'ULTRASOUND', reportText: '肝脏弥漫性增大，未见胆道梗阻征象。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'WES', reportSource: '院内', summary: 'G6PC 双等位变异', conclusion: '支持糖原累积病 I 型', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'G6PC', hgvsC: 'c.648G>T' }] }
      p.clinicalDecision = { diagnosis: '糖原累积病I型/G6PC', treatmentPlan: '夜间生玉米淀粉方案评估，避免长时间空腹，管理高尿酸和高甘油三酯。' }
    }
  ),
  withOverrides(
    { patientNo: 'P011', name: '沈予安', gender: '男', age: 23, visitDate: '2026-05-12', occupation: '研究生', chiefComplaint: '反复咳喘伴不明原因肝酶升高1年', presentIllness: '近1年反复咳喘，肺功能提示早期阻塞性改变，同时多次出现 ALT/AST 升高。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '78', ast: '64', ggt: '62', tbil: '20' })
      p.laboratoryScreening.clinicalBiochemistry.metabolism.aat = '430'
      p.imagingReports = [{ modality: 'CT', reportText: '双肺下叶轻度肺气肿样改变。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'SERPINA1 Pi*ZZ', conclusion: '支持 α1-抗胰蛋白酶缺乏症', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'SERPINA1', hgvsP: 'p.Glu342Lys' }] }
      p.clinicalDecision = { diagnosis: 'α1-抗胰蛋白酶缺乏症', treatmentPlan: '戒烟避烟，高蛋白低脂饮食，肝肺联合随访。' }
    }
  ),
  withOverrides(
    { patientNo: 'P012', name: '叶清源', gender: '男', age: 29, visitDate: '2026-05-13', occupation: '设计师', chiefComplaint: '乏力、皮肤色素加深伴肝酶升高半年', presentIllness: '半年内出现乏力和皮肤色素加深，外院提示铁蛋白和转铁蛋白饱和度升高，无输血史。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '28', alt: '105', ast: '88', ggt: '90', glu: '6.4' })
      p.physicalExam.liverFibrosis = 'YES'
      p.imagingReports = [{ modality: 'MRI', reportText: '肝脏 T2* 信号减低，符合肝铁沉积表现。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'HJV 变异，提示青年型铁过载', conclusion: '支持青年型遗传性血色病', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'HJV', hgvsC: 'c.320T>G' }] }
      p.clinicalDecision = { diagnosis: '青年型遗传性血色病', treatmentPlan: '完善铁代谢定量，评估静脉放血或铁螯合治疗，并开展家系筛查。' }
    }
  )
]
