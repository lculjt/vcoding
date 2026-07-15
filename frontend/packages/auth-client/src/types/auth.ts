export interface CurrentUser {
  userId: number
  username: string
  phone: string
  adminFlag: boolean
}

export interface AuthSession {
  authenticated: boolean
  user: CurrentUser | null
}

export const anonymousSession: AuthSession = {
  authenticated: false,
  user: null
}

export interface LoginByPasswordRequest {
  account: string
  passwordCiphertext: string
  passwordKeyId: string
}

export interface LoginBySmsCodeRequest {
  phone: string
  smsCode: string
}

export interface LoginResponse {
  user: CurrentUser
  expiresInSeconds: number
}

export interface RegisterRequest {
  username: string
  phone: string
  passwordCiphertext: string
  passwordKeyId: string
  smsCode: string
}

export type RegisterResponse = CurrentUser
