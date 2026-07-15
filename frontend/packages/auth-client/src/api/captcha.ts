import { authRequest } from '../request'
import type { ImageCaptcha } from '../types'

export function createImageCaptcha(): Promise<ImageCaptcha> {
  return authRequest.get<ImageCaptcha>('/auth/captcha/image')
}
