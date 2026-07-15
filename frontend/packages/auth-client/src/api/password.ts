import { authRequest } from '../request'
import type { PasswordPublicKey } from '../types'

export function getPasswordPublicKey(): Promise<PasswordPublicKey> {
  return authRequest.get<PasswordPublicKey>('/auth/password/public-key')
}
