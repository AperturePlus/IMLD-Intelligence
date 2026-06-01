export interface PatientListQuery {
  keyword?: string
}

export interface PatientSummary {
  id: string
  name: string
  gender: string
  age: number
  riskLevel?: string | null
  aiStatus: string
  avatar?: string | null
}

export interface PatientListResponse {
  items: PatientSummary[]
}

export type EncounterType = 'OUTPATIENT' | 'EMERGENCY' | 'INPATIENT'

export type ImportSourceType = 'HIS_LIS' | 'IMAGE_OCR' | 'PDF_OCR'

export type TernaryFlag = 'YES' | 'NO' | 'UNKNOWN'

export interface PatientRecordDiseaseHistory {
  smokingHistory: TernaryFlag
  drinkingHistory: TernaryFlag
  diabetesHistory: TernaryFlag
  hypertensionHistory: TernaryFlag
  hyperuricemiaHistory: TernaryFlag
  hyperlipidemiaHistory: TernaryFlag
  coronaryHeartDiseaseHistory: TernaryFlag
  hepatitisBHistory: TernaryFlag
}

export interface PatientRecordConditionalHistory {
  status: TernaryFlag
  detail: string
}

export interface PatientRecordHistory {
  diseaseHistory: PatientRecordDiseaseHistory
  surgeryHistory: PatientRecordConditionalHistory
  transfusionHistory: PatientRecordConditionalHistory
  allergyHistory: string
  medicationHistory: string
  familyHistory: string
}

export interface PatientRecordPhysicalExam {
  heightCm: number | null
  weightKg: number | null
  bmi: number | null
  bloodPressureSystolic: number | null
  bloodPressureDiastolic: number | null
  respiratoryRate: number | null
  heartRate: number | null
  liverFibrosis: TernaryFlag
  cirrhosis: TernaryFlag
  fattyLiver: TernaryFlag
  liverFailure: TernaryFlag
  cholestasis: TernaryFlag
  viralHepatitis: TernaryFlag
}

export interface PatientRecordImportMeta {
  sourceType: ImportSourceType | ''
  traceId: string
  confidence: number | null
  importedAt: string
}

export interface PatientRecordLiverFunction {
  tbil: string
  dbil: string
  ibil: string
  alt: string
  ast: string
  astAltRatio: string
  tp: string
  alb: string
  glob: string
  albGlobRatio: string
  glu: string
  tg: string
  chol: string
  hdlC: string
  ldlC: string
  alp: string
  ggt: string
  ck: string
  ldh: string
  hbdh: string
  tba: string
  nh3: string
}

export interface PatientRecordRenalFunction {
  urea: string
  crea: string
  eGfr: string
  cysC: string
  uric: string
}

export interface PatientRecordMetabolism {
  cer: string
  aat: string
}

export interface PatientRecordTumorMarkers {
  afp: string
  cea: string
  ca19_9: string
}

export interface PatientRecordInflammation {
  crp: string
  pct: string
}

export interface PatientRecordClinicalBiochemistry {
  liverFunction: PatientRecordLiverFunction
  renalFunction: PatientRecordRenalFunction
  metabolism: PatientRecordMetabolism
  tumorMarkers: PatientRecordTumorMarkers
  inflammation: PatientRecordInflammation
}

export interface PatientRecordBloodRoutine {
  rbc: string
  hgb: string
  hct: string
  mcv: string
  mch: string
  mchc: string
  plt: string
  wbc: string
  neutPercent: string
  lymphPercent: string
  monoPercent: string
  eoPercent: string
  basoPercent: string
}

export interface PatientRecordReticulocyte {
  retAbsolute: string
  retPermillage: string
  lfrPercent: string
  mfrPercent: string
  hfrPercent: string
}

export interface PatientRecordCoagulation {
  pivka: string
  pt: string
  inr: string
  aptt: string
  tt: string
  fdp: string
}

export interface PatientRecordClinicalBasic {
  bloodRoutine: PatientRecordBloodRoutine
  reticulocyte: PatientRecordReticulocyte
  coagulation: PatientRecordCoagulation
}

export interface PatientRecordAntibody {
  igg: string
  iga: string
  igm: string
  igg4: string
}

export interface PatientRecordAutoantibody {
  ana: string
}

export interface PatientRecordClinicalImmunology {
  antibody: PatientRecordAntibody
  autoantibody: PatientRecordAutoantibody
}

export type PatientRecordClinicalMicrobiology = Record<string, never>

export interface PatientRecordLaboratoryScreening {
  clinicalBiochemistry: PatientRecordClinicalBiochemistry
  clinicalBasic: PatientRecordClinicalBasic
  clinicalImmunology: PatientRecordClinicalImmunology
  clinicalMicrobiology: PatientRecordClinicalMicrobiology
}

export type PatientRecordImagingModality = 'CT' | 'ULTRASOUND' | 'MRI' | 'OTHER'

export type PatientRecordImagingSourceType = 'MANUAL' | 'IMAGE_OCR' | 'PDF_OCR' | 'PACS_IMPORT'

export interface PatientRecordImagingReportItem {
  modality: PatientRecordImagingModality
  reportText: string
  examinedAt?: string
  fileId?: string | null
  sourceType: PatientRecordImagingSourceType
}

export type PatientRecordPathologySourceType = 'MANUAL' | 'IMAGE_OCR' | 'PDF_OCR' | 'PACS_IMPORT'

export interface PatientRecordPathology {
  performed: boolean
  reportText: string
  nasScore: number | null
  reportedAt?: string
  fileId?: string | null
  sourceType: PatientRecordPathologySourceType
}

export type PatientRecordGeneticSourceType = 'MANUAL' | 'IMAGE_OCR' | 'PDF_OCR' | 'HIS_LIS'

export type PatientRecordGeneticMethod = 'PANEL' | 'WES' | 'WGS' | 'OTHER' | ''

export interface PatientRecordGeneticVariantItem {
  gene: string
  hgvsC?: string
  hgvsP?: string
  variantType?: string
  zygosity?: string
  classification?: string
  evidence?: string
}

export interface PatientRecordGeneticSequencing {
  tested: boolean
  method: PatientRecordGeneticMethod
  reportSource: string
  reportDate?: string
  summary: string
  conclusion: string
  fileId?: string | null
  sourceType: PatientRecordGeneticSourceType
  variants: PatientRecordGeneticVariantItem[]
}

export interface PatientRecordClinicalDecision {
  diagnosis: string
  treatmentPlan: string
}

export interface PatientRecordPayload {
  patientNo: string
  name: string
  gender: string
  age: number | null
  visitDate: string
  phone: string
  idCard: string
  occupation: string
  currentAddress: string
  nativePlace: string
  department: string
  encounterType: EncounterType
  consanguinity: boolean
  chiefComplaint: string
  presentIllness: string
  history: PatientRecordHistory
  physicalExam: PatientRecordPhysicalExam
  laboratoryScreening: PatientRecordLaboratoryScreening
  imagingReports: PatientRecordImagingReportItem[]
  pathology: PatientRecordPathology
  geneticSequencing: PatientRecordGeneticSequencing
  clinicalDecision: PatientRecordClinicalDecision
  visitId?: string
  importMeta?: PatientRecordImportMeta
}

export interface HisLisPatientImportRequest {
  patientNo?: string
  visitNo?: string
}

export interface OcrPatientImportRequest {
  fileName: string
  fileContentBase64: string
}

export interface PatientImportPreview {
  sourceType: ImportSourceType
  traceId: string
  confidence: number
  patientNo: string
  name: string
  gender: string
  age: number | null
  visitDate: string
  phone: string
  idCard: string
  occupation: string
  currentAddress: string
  nativePlace: string
  department: string
  encounterType: EncounterType
}

export interface CreatePatientRecordResponse {
  recordId: string
  visitId: string
  message: string
}
