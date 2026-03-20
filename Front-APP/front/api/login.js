import request from '@/utils/request'

// 登录方法
export function login(username, password, code, uuid, role) {
  const data = {
    username,
    password,
    code,
    uuid,
	role
  }
  return request({
    'url': '/user/login',
    headers: {
      isToken: false
    },
    'method': 'get',
    'data': data
  })
}

// 注册方法
export function register(username,password,code,role) {
	const data = {
	  username,
	  password,
	  code,
	  role
	}
  return request({
    url: '/user/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取用户详细信息
export function getInfo(data) {
  return request({
    'url': '/user',
    'method': 'get',
    data
  })
}


// 退出方法
export function logout() {
  return request({
    'url': '/logout',
    'method': 'post'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    'url': '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}
