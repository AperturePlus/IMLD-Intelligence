import type { AxiosResponse } from 'axios'
import service from './base'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  UserDetailResponse
} from './types'

const userApi = {
  login(data: LoginRequest): Promise<AxiosResponse<LoginResponse>> {
    return service({
      url: '/dj-rest-auth/login/',
      method: 'post',
      data
    })
  },

  getUserDetail(): Promise<AxiosResponse<UserDetailResponse>> {
    return service({
      url: '/dj-rest-auth/user/',
      method: 'get'
    })
  },

  register(data: RegisterRequest): Promise<AxiosResponse<RegisterResponse>> {
    return service({
      url: '/dj-rest-auth/registration/',
      method: 'post',
      data
    })
  }
}

export default userApi
