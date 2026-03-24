import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'index',
    component: () => import('@/pages/auth/LoginPage.vue')
  },
  {
    path: '/center',
    name: 'center',
    component: () => import('@/layouts/CenterLayout.vue'),
    redirect: '/center/welcome',
    children: [
      {
        path: 'welcome',
        name: 'welcome',
        component: () => import('@/pages/dashboard/WelcomePage.vue')
      },
      {
        path: 'patient-list',
        name: 'patient-list',
        component: () => import('@/pages/patients/PatientListPage.vue')
      },
      {
        path: 'patient-record',
        name: 'patient-record',
        component: () => import('@/pages/patients/PatientRecordPage.vue')
      },
      {
        path: 'ai-diagnosis',
        name: 'ai-diagnosis',
        component: () => import('@/pages/diagnosis/AiDiagnosisPage.vue')
      },
      {
        path: 'expert-diagnosis',
        name: 'expert-diagnosis',
        component: () => import('@/pages/diagnosis/ExpertDiagnosisPage.vue')
      },
      {
        path: 'diet',
        name: 'diet',
        component: () => import('@/pages/management/DietPage.vue')
      },
      {
        path: 'data-screening',
        name: 'data-screening',
        component: () => import('@/pages/management/DataScreeningPage.vue')
      }
    ]
  }
]
