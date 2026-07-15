export type AuthMode = 'login' | 'register'

export type LoginTab = 'password' | 'sms'

export interface PasswordLoginForm {
  account: string
  password: string
  remember: boolean
}

export interface SmsLoginForm {
  phone: string
  smsCode: string
}

export interface RegisterForm {
  phone: string
  smsCode: string
  username: string
  password: string
  confirmPassword: string
}

export interface CaptchaVerifyResult {
  captchaId: string
  captchaCode: string
}
