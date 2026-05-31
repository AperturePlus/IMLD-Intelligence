import type { RouteRecordRaw } from 'vue-router'

type RouteComponent = NonNullable<RouteRecordRaw['component']>

interface AuthRouteDefinition {
  name: string
  path: string
  title: string
  sectionTitle: string
  component: RouteComponent
}

interface CenterRouteDefinition {
  name: string
  path: string
  fullPath: string
  title: string
  sectionTitle: string
  component: RouteComponent
}

interface NavigationItem {
  path: string
  title: string
}

export interface NavigationGroup {
  index: string
  title: string
  icon: string
  items: NavigationItem[]
}

export const authRouteDefinition: AuthRouteDefinition = {
  name: 'index',
  path: '/',
  title: '登录',
  sectionTitle: '身份认证',
  component: () => import('@/pages/auth/LoginPage.vue')
}

export const centerRouteDefinitions = {
  welcome: {
    name: 'welcome',
    path: 'welcome',
    fullPath: '/center/welcome',
    title: '首页',
    sectionTitle: '临床工作台',
    component: () => import('@/pages/dashboard/WelcomePage.vue')
  },
  patientList: {
    name: 'patient-list',
    path: 'patient-list',
    fullPath: '/center/patient-list',
    title: '患者列表',
    sectionTitle: '患者档案',
    component: () => import('@/pages/patients/PatientListPage.vue')
  },
  patientRecord: {
    name: 'patient-record',
    path: 'patient-record',
    fullPath: '/center/patient-record',
    title: '病历录入',
    sectionTitle: '患者档案',
    component: () => import('@/pages/patients/PatientRecordPage.vue')
  },
  aiDiagnosis: {
    name: 'ai-diagnosis',
    path: 'ai-diagnosis',
    fullPath: '/center/ai-diagnosis',
    title: '智能诊断',
    sectionTitle: '辅助诊断',
    component: () => import('@/pages/diagnosis/AiDiagnosisPage.vue')
  },
  expertDiagnosis: {
    name: 'expert-diagnosis',
    path: 'expert-diagnosis',
    fullPath: '/center/expert-diagnosis',
    title: '专家报告',
    sectionTitle: '辅助诊断',
    component: () => import('@/pages/diagnosis/ExpertDiagnosisPage.vue')
  },
  diet: {
    name: 'diet',
    path: 'diet',
    fullPath: '/center/diet',
    title: '膳食干预',
    sectionTitle: '慢病管理',
    component: () => import('@/pages/management/DietPage.vue')
  },
  dataScreening: {
    name: 'data-screening',
    path: 'data-screening',
    fullPath: '/center/data-screening',
    title: '筛查数据',
    sectionTitle: '慢病管理',
    component: () => import('@/pages/management/DataScreeningPage.vue')
  },
  licenseActivation: {
    name: 'license-activation',
    path: 'license-activation',
    fullPath: '/center/license-activation',
    title: '许可证激活',
    sectionTitle: '系统设置',
    component: () => import('@/pages/system/LicenseActivationPage.vue')
  },
  accountManage: {
    name: 'account-manage',
    path: 'account-manage',
    fullPath: '/center/account-manage',
    title: '账号权限',
    sectionTitle: '系统设置',
    component: () => import('@/pages/system/AccountManagePage.vue')
  },
  accountSettings: {
    name: 'account-settings',
    path: 'account-settings',
    fullPath: '/center/account-settings',
    title: '账户设置',
    sectionTitle: '系统设置',
    component: () => import('@/pages/system/AccountSettingsPage.vue')
  }
} satisfies Record<string, CenterRouteDefinition>

export const navigationGroups: NavigationGroup[] = [
  {
    index: '2',
    title: '患者档案',
    icon: 'UserFilled',
    items: [
      {
        path: centerRouteDefinitions.patientList.fullPath,
        title: centerRouteDefinitions.patientList.title
      },
      {
        path: centerRouteDefinitions.patientRecord.fullPath,
        title: centerRouteDefinitions.patientRecord.title
      }
    ]
  },
  {
    index: '3',
    title: '辅助诊断',
    icon: 'Cpu',
    items: [
      {
        path: centerRouteDefinitions.aiDiagnosis.fullPath,
        title: centerRouteDefinitions.aiDiagnosis.title
      },
      {
        path: centerRouteDefinitions.expertDiagnosis.fullPath,
        title: centerRouteDefinitions.expertDiagnosis.title
      }
    ]
  },
  {
    index: '4',
    title: '慢病管理',
    icon: 'FirstAidKit',
    items: [
      {
        path: centerRouteDefinitions.diet.fullPath,
        title: centerRouteDefinitions.diet.title
      },
      {
        path: centerRouteDefinitions.dataScreening.fullPath,
        title: centerRouteDefinitions.dataScreening.title
      }
    ]
  },
  {
    index: '5',
    title: '系统设置',
    icon: 'Setting',
    items: [
      {
        path: centerRouteDefinitions.licenseActivation.fullPath,
        title: centerRouteDefinitions.licenseActivation.title
      },
      {
        path: centerRouteDefinitions.accountManage.fullPath,
        title: centerRouteDefinitions.accountManage.title
      }
    ]
  }
]

