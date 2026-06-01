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

// 正常基线：填充展示/推理关心的化验为正常值，其余留空（视图显示「—」，推理回退中位数）。
const buildBaseline = (basics: SeedBasics): PatientRecordPayload => {
  const labs = createInitialLaboratoryScreening()
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
      heightCm: basics.gender === '男' ? 172 : 160,
      weightKg: basics.gender === '男' ? 70 : 56,
      bmi: basics.gender === '男' ? 23.7 : 21.9,
      bloodPressureSystolic: 122, bloodPressureDiastolic: 78,
      respiratoryRate: 18, heartRate: 76,
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
    { patientNo: 'P001', name: '林建国', gender: '男', age: 58, visitDate: '2026-05-02', chiefComplaint: '皮肤色素沉着伴乏力、关节疼痛半年', presentIllness: '近半年皮肤渐进性色素沉着，乏力、多关节隐痛，否认输血史。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '30', alt: '110', ast: '92', ggt: '88', alb: '42' })
      p.history.diseaseHistory.drinkingHistory = 'YES'
      p.history.diseaseHistory.hyperlipidemiaHistory = 'YES'
      p.physicalExam.liverFibrosis = 'YES'
      p.imagingReports = [{ modality: 'MRI', reportText: '肝脏 T2* 信号减低，提示铁过载。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'HFE 基因 C282Y / H63D 变异', conclusion: '支持遗传性血色病', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'HFE', hgvsC: 'c.845G>A' }, { gene: 'HFE', hgvsC: 'c.187C>G' }] }
      p.clinicalDecision = { diagnosis: '遗传性血色病', treatmentPlan: '限制高铁饮食，评估后行静脉放血治疗，HFE 家系筛查。' }
    }
  ),
  withOverrides(
    { patientNo: 'P002', name: '陈婉婷', gender: '女', age: 32, visitDate: '2026-05-03', chiefComplaint: '体检发现转氨酶轻度升高1周', presentIllness: '体检发现 ALT 轻度升高，无明显不适，否认饮酒。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '55', ast: '48' })
      p.laboratoryScreening.clinicalBiochemistry.metabolism.cer = '180'
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'ATP7B 杂合携带', conclusion: '携带者，暂不支持临床诊断', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ATP7B', hgvsC: 'c.2333G>T' }] }
      p.clinicalDecision = { diagnosis: '肝豆状核变性 (Wilson病)', treatmentPlan: '低铜饮食宣教，3-6 月复查铜代谢与肝功。' }
    }
  ),
  withOverrides(
    { patientNo: 'P003', name: '张明远', gender: '男', age: 45, visitDate: '2026-05-04', occupation: '教师', chiefComplaint: '反复咳喘伴肝功能异常1年', presentIllness: '反复咳嗽气促1年，肺功能提示早期肺气肿，伴肝酶升高。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '78', ast: '64', ggt: '70' })
      p.laboratoryScreening.clinicalBiochemistry.metabolism.aat = '420'
      p.history.diseaseHistory.smokingHistory = 'YES'
      p.imagingReports = [{ modality: 'CT', reportText: '双肺下叶肺气肿样改变。', sourceType: 'MANUAL', fileId: null }]
      p.geneticSequencing = { tested: true, method: 'WES', reportSource: '院内', summary: 'SERPINA1 Pi*ZZ', conclusion: '支持 α1-抗胰蛋白酶缺乏症', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'SERPINA1', hgvsP: 'p.Glu342Lys' }] }
      p.clinicalDecision = { diagnosis: 'α1-抗胰蛋白酶缺乏症', treatmentPlan: '戒烟，高蛋白低脂饮食，肝肺联合随访。' }
    }
  ),
  withOverrides(
    { patientNo: 'P004', name: '王淑芬', gender: '女', age: 62, visitDate: '2026-05-05', chiefComplaint: '体检发现脂肪肝伴血糖升高2年', presentIllness: '超声示中重度脂肪肝，空腹血糖升高，体型肥胖。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '96', ast: '70', ggt: '85', tg: '3.2', chol: '6.4', glu: '8.1', ldlC: '3.6' })
      Object.assign(p.history.diseaseHistory, { diabetesHistory: 'YES', hypertensionHistory: 'YES', hyperlipidemiaHistory: 'YES' })
      p.physicalExam.fattyLiver = 'YES'
      Object.assign(p.physicalExam, { weightKg: 76, bmi: 29.7 })
      p.pathology = { performed: true, reportText: '肝细胞脂肪变性伴小叶炎症，NAS 评分中等。', nasScore: 5, fileId: null, sourceType: 'MANUAL' }
      p.clinicalDecision = { diagnosis: '代谢相关脂肪性肝病', treatmentPlan: '控制热量与精制碳水，减重与运动，管理血糖血脂。' }
    }
  ),
  withOverrides(
    { patientNo: 'P005', name: '李浩宇', gender: '男', age: 28, visitDate: '2026-05-06', occupation: '工程师', chiefComplaint: '间断巩膜黄染、疲劳时明显2年', presentIllness: '劳累或饥饿后间断巩膜黄染，肝酶正常，溶血指标阴性。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '32', ibil: '27', dbil: '5' })
      p.clinicalDecision = { diagnosis: 'Gilbert综合征', treatmentPlan: '良性病程，规律作息、避免饥饿与过劳，定期随访。' }
    }
  ),
  withOverrides(
    { patientNo: 'P006', name: '赵雪梅', gender: '女', age: 51, visitDate: '2026-05-07', chiefComplaint: '乏力伴肝功能异常3月', presentIllness: '近3月乏力，肝酶轻中度升高，无黄疸。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '72', ast: '60', ggt: '65', tbil: '22' })
      p.history.diseaseHistory.hyperlipidemiaHistory = 'YES'
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'HFE H63D 杂合', conclusion: '提示血色病易感', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'HFE', hgvsC: 'c.187C>G' }] }
      p.clinicalDecision = { diagnosis: '遗传性血色病', treatmentPlan: '限铁饮食，监测铁蛋白与转铁蛋白饱和度。' }
    }
  ),
  withOverrides(
    { patientNo: 'P007', name: '刘振华', gender: '男', age: 66, visitDate: '2026-05-06', chiefComplaint: '乏力、双手震颤伴言语含糊3月', presentIllness: '渐进性乏力，双上肢静止性震颤伴构音障碍，否认肝炎史。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { tbil: '38', dbil: '14', ibil: '24', alt: '168', ast: '142', ggt: '95', alb: '38' })
      p.laboratoryScreening.clinicalBiochemistry.metabolism.cer = '42'
      Object.assign(p.physicalExam, { liverFibrosis: 'YES', cirrhosis: 'YES' })
      p.imagingReports = [{ modality: 'ULTRASOUND', reportText: '肝实质回声增粗，符合肝硬化改变。', sourceType: 'MANUAL', fileId: null }]
      p.pathology = { performed: true, reportText: '肝细胞铜染色阳性，活动性炎症。', nasScore: 4, fileId: null, sourceType: 'MANUAL' }
      p.geneticSequencing = { tested: true, method: 'WES', reportSource: '院内', summary: 'ATP7B 复合杂合突变', conclusion: '支持肝豆状核变性诊断', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ATP7B', hgvsC: 'c.2333G>T' }, { gene: 'ATP7B', hgvsC: 'c.2975C>T' }] }
      p.clinicalDecision = { diagnosis: '肝豆状核变性 (Wilson病)', treatmentPlan: '青霉胺驱铜，严格低铜饮食，一级亲属 ATP7B 筛查。' }
    }
  ),
  withOverrides(
    { patientNo: 'P008', name: '周小雅', gender: '女', age: 24, visitDate: '2026-05-08', occupation: '学生', chiefComplaint: '家族筛查发现铜代谢异常', presentIllness: '兄长确诊 Wilson 病，本人筛查发现铜蓝蛋白偏低，无症状。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '40', ast: '36' })
      p.laboratoryScreening.clinicalBiochemistry.metabolism.cer = '170'
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'ATP7B 杂合', conclusion: '携带者，需随访', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ATP7B', hgvsC: 'c.2975C>T' }] }
      p.clinicalDecision = { diagnosis: '肝豆状核变性 (Wilson病)', treatmentPlan: '低铜饮食宣教，定期复查铜代谢。' }
    }
  ),
  withOverrides(
    { patientNo: 'P009', name: '吴建强', gender: '男', age: 53, visitDate: '2026-05-09', chiefComplaint: '乏力、皮肤变黑伴血糖升高1年', presentIllness: '乏力伴皮肤色素沉着，空腹血糖升高，肝酶明显升高。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '135', ast: '105', ggt: '110', tbil: '34', glu: '7.2' })
      Object.assign(p.history.diseaseHistory, { drinkingHistory: 'YES', diabetesHistory: 'YES' })
      Object.assign(p.physicalExam, { liverFibrosis: 'YES', cirrhosis: 'YES' })
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'HFE C282Y 纯合', conclusion: '支持遗传性血色病', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'HFE', hgvsC: 'c.845G>A' }] }
      p.clinicalDecision = { diagnosis: '遗传性血色病', treatmentPlan: '静脉放血治疗，限铁饮食，管理糖代谢。' }
    }
  ),
  withOverrides(
    { patientNo: 'P010', name: '郑丽丽', gender: '女', age: 38, visitDate: '2026-05-04', chiefComplaint: '肢体震颤伴肝功能异常半年', presentIllness: '半年来肢体震颤，肝酶升高，铜蓝蛋白偏低。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '88', ast: '72', tbil: '26' })
      p.laboratoryScreening.clinicalBiochemistry.metabolism.cer = '95'
      p.geneticSequencing = { tested: true, method: 'WES', reportSource: '院内', summary: 'ATP7B 杂合突变', conclusion: '结合临床支持 Wilson 病', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'ATP7B', hgvsC: 'c.2333G>T' }] }
      p.clinicalDecision = { diagnosis: '肝豆状核变性 (Wilson病)', treatmentPlan: '驱铜治疗，低铜饮食，神经科随访。' }
    }
  ),
  withOverrides(
    { patientNo: 'P011', name: '孙立军', gender: '男', age: 41, visitDate: '2026-05-10', chiefComplaint: '体检发现肝酶轻度升高', presentIllness: '体检发现 ALT/AST 轻度升高，无症状，少量饮酒。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '52', ast: '44', ggt: '48', tbil: '18' })
      p.geneticSequencing = { tested: true, method: 'PANEL', reportSource: '院内', summary: 'HFE H63D 杂合', conclusion: '易感携带', fileId: null, sourceType: 'HIS_LIS', variants: [{ gene: 'HFE', hgvsC: 'c.187C>G' }] }
      p.clinicalDecision = { diagnosis: '遗传性血色病', treatmentPlan: '监测铁代谢，限铁饮食宣教。' }
    }
  ),
  withOverrides(
    { patientNo: 'P012', name: '马桂英', gender: '女', age: 71, visitDate: '2026-05-11', chiefComplaint: '肥胖、血糖血脂升高伴肝功能异常3年', presentIllness: '长期肥胖伴糖尿病、高脂血症，超声示重度脂肪肝。' },
    (p) => {
      Object.assign(p.laboratoryScreening.clinicalBiochemistry.liverFunction, { alt: '102', ast: '82', ggt: '95', tg: '3.6', chol: '6.8', glu: '9.2', ldlC: '4.0' })
      Object.assign(p.history.diseaseHistory, { diabetesHistory: 'YES', hypertensionHistory: 'YES', hyperlipidemiaHistory: 'YES', hyperuricemiaHistory: 'YES' })
      Object.assign(p.physicalExam, { fattyLiver: 'YES', cirrhosis: 'YES', weightKg: 78, bmi: 30.5 })
      p.pathology = { performed: true, reportText: '脂肪性肝炎伴桥接纤维化，NAS 评分高。', nasScore: 6, fileId: null, sourceType: 'MANUAL' }
      p.clinicalDecision = { diagnosis: '代谢相关脂肪性肝病', treatmentPlan: '强化生活方式干预，控糖控脂，评估肝纤维化进展。' }
    }
  )
]
