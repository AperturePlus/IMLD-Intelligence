import {createRouter, createWebHistory} from 'vue-router'

const routes=[
    {
        path:"/",
        name:"index",
        component:()=>import('../views/Index.vue')
    },
     {
        path:"/center",
        name:"center",
        component:()=>import('../views/Center.vue'),
        redirect: '/center/welcome', // 默认重定向到欢迎页面
        children: [
        

        {
        path: 'welcome',
        name: 'welcome',
        component: () => import('../views/Welcome.vue') // 引入 Welcome 组件
        },

        {
        path: 'patient-list',
        name: 'patient-list',
        component: () => import('../views/Patients/patient-list.vue') // 引入 PatientList 组件
        },

        {
        path: 'patient-record',
        name: 'patient-record',
        component: () => import('../views/Patients/patient-record.vue') // 引入 PatientRecord 组件
        },
     


         {
        path: 'ai-diagnosis',
        name: 'ai-diagnosis',
        component: () => import('../views/Diagnosis/AI-Diagnosis.vue') // 引入 AIDiagnosis 组件
        },

         {
        path: 'expert-diagnosis',
        name: 'expert-diagnosis',
        component: () => import('../views/Diagnosis/Expert-Diagnosis.vue') // 引入 ExpertDiagnosis 组件
        },

        {
        path: 'diet',
        name: 'diet',
        component: () => import('../views/Management/Diet.vue') // 引入 Diet 组件
        },

        {
        path: 'data-screening',
        name: 'data-screening',
        component: () => import('../views/Management/Data-Screening.vue') // 引入 DataScreening 组件
        },


    ]
    }
]

const router=createRouter({
    routes,
    history:createWebHistory()
})



const whiteList = ['/']
// 2. 添加全局前置导航守卫
//    (注意这里的 _from)

router.beforeEach((to, _from, next) => {
  // 尝试从 localStorage 获取 token
  const token = localStorage.getItem('token')

  if (token) {
    // --- 1. 用户已登录 ---
    if (to.path === '/') {
      // 如果已登录，还想访问登录页(首页'/')
      // 则重定向到个人中心
      next({ path: '/center' })
    } else {
      // 访问其他页面 (如 /center)，直接放行
      next()
    }
  } else {
    // --- 2. 用户未登录 ---
    if (whiteList.includes(to.path)) {
      // 如果访问的是白名单页面 ('/'), 直接放行
      next()
    } else {
      // 如果访问的是其他任何受保护页面 (如 /center/my-crafts)
      // 则重定向回登录页 ('/')
      console.log('访问被拦截，重定向到登录页')
      next({ path: '/' })
    }
  }
})


export default router
