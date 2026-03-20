import axios from "axios"
import router from '../router' // 导入你的 router

const service = axios.create({
    baseURL: "http://115.190.101.111:10001/",
    // baseURL: "http://127.0.0.1:8000/",
    withCredentials: true,
})

// 1. 请求拦截器 (你已经写好的)
service.interceptors.request.use(
    config => {
        if (localStorage.getItem('token')) {
            // 注意：确保你的后端需要 'Token ' 这个前缀
            // 有些后端需要 'Bearer '
            config.headers['Authorization'] = 'Token ' + localStorage.getItem('token');
        }
        return config
    },

    error => {
        console.log(error) // for debug
        return Promise.reject(error)
    }
)

// 2. 响应拦截器 (处理 token 过期等错误)
service.interceptors.response.use(
    response => {
        // 任何 2xx 范围内的状态码都会触发这里
        // 直接返回响应数据
        return response
    },
    error => {
        // 任何非 2xx 范围的状态码都会触发这里
        if (error.response && error.response.status === 401) {
            // 401 错误 (未授权)，大概率是 token 过期了
            console.warn('Token expired or unauthorized, redirecting to login.')

            // a. 清除本地的过期 token
            localStorage.removeItem('token')

            // b. 提示用户
            // (你也可以使用 Element Plus 的 ElMessage.error)
            alert('登录已过期，请重新登录。')

            // c. 跳转回登录页 (首页 '/')
            // 注意：确保 router 实例被正确导入
            router.push('/')
        }
        // 必须返回一个 rejected Promise，否则请求的 .catch() 不会触发
        return Promise.reject(error)
    }
)

export default service
