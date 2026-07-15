export type SmsScene = 'register' | 'login' | 'reset-password' | 'bind-phone' | 'change-phone'

export interface SendSmsCodeRequest {
  scene: SmsScene
  phone: string
  captchaId: string
  captchaCode: string
}

export interface SendSmsCodeResponse {
  scene: SmsScene
  phone: string
  expiresInSeconds: number
  cooldownSeconds: number
}
