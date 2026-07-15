import { authRequest } from '../request'
import type { SendSmsCodeRequest, SendSmsCodeResponse } from '../types'

export function sendSmsCode(request: SendSmsCodeRequest): Promise<SendSmsCodeResponse> {
  return authRequest.post<SendSmsCodeResponse, SendSmsCodeRequest>('/auth/sms/send', request)
}
