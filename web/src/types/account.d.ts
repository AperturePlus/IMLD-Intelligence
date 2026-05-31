export interface AccountProfileResponse {
  userId: number
  tenantId: number
  userNo: string
  username: string
  displayName: string
  userType: string
  deptName: string | null
  email: string | null
  mobileMasked: string | null
  roleCodes: string[]
  lastLoginAt: string | null
}

export interface UpdateAccountProfileRequest {
  displayName?: string
  deptName?: string | null
  email?: string
  mobilePlaintext?: string
  currentPassword?: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
  refreshToken: string
}
