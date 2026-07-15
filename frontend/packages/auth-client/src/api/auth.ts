import { authRequest } from '../request'
import type {
  CurrentUser,
  LoginByPasswordRequest,
  LoginBySmsCodeRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse
} from '../types'

export function loginByPassword(request: LoginByPasswordRequest): Promise<LoginResponse> {
  return authRequest.post<LoginResponse, LoginByPasswordRequest>('/auth/login', request)
}

export function loginBySmsCode(request: LoginBySmsCodeRequest): Promise<LoginResponse> {
  return authRequest.post<LoginResponse, LoginBySmsCodeRequest>('/auth/login/sms', request)
}

export function register(request: RegisterRequest): Promise<RegisterResponse> {
  return authRequest.post<RegisterResponse, RegisterRequest>('/auth/register', request)
}

export function getCurrentUser(): Promise<CurrentUser> {
  return authRequest.get<CurrentUser>('/auth/me')
}

export function logout(): Promise<void> {
  return authRequest.post<void>('/auth/logout')
}
