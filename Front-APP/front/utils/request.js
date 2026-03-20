import store from '@/store'
import config from '@/config'
import {
	getToken
} from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import {
	toast,
	showConfirm,
	tansParams
} from '@/utils/common'


let timeout = 10000
const baseUrl = config.baseUrl

// 修改后的 request 方法（建议）
const request = config => {
	const isToken = (config.headers || {}).isToken === false
	config.header = config.header || {}

	if (getToken() && !isToken) {
		config.header['Authorization'] = 'Bearer ' + getToken()
	}

	// ✅ 不再手动拼接 URL 参数了！

	console.log(config)

	return new Promise((resolve, reject) => {
		uni.request({
			method: config.method || 'get',
			timeout: config.timeout || timeout,
			url: config.baseUrl || baseUrl + config.url,
			// ✅ 统一使用 data 传递参数（GET、POST 都支持）
			data: config.params || config.data || {},
			header: config.header,
			dataType: 'json'
		}).then(response => {
			let [error, res] = response
			if (error) {
				toast('后端接口连接异常')
				reject('后端接口连接异常')
				return
			}
			const code = res.data.code || 200
			const msg = errorCode[code] || res.data.msg || errorCode['default']
			if (code === 401) {
				showConfirm('登录状态已过期，您可以继续留在该页面，或者重新登录?').then(res => {
					if (res.confirm) {
						store.dispatch('LogOut').then(() => {
							uni.reLaunch({
								url: '/pages/login'
							})
						})
					}
				})
				reject('无效的会话，或者会话已过期，请重新登录。')
			} else if (code === 500) {
				toast(msg)
				reject('500')
			} else if (code !== 200) {
				toast(msg)
				reject(code)
			}
			resolve(res.data)
		}).catch(error => {
			let { message } = error
			if (message === 'Network Error') {
				message = '后端接口连接异常'
			} else if (message.includes('timeout')) {
				message = '系统接口请求超时'
			} else if (message.includes('Request failed with status code')) {
				message = '系统接口' + message.substr(message.length - 3) + '异常'
			}
			toast(message)
			reject(error)
		})
	})
}

export default request


