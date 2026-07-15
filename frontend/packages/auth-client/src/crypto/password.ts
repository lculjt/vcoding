import { getPasswordPublicKey } from '../api/password'
import type { EncryptedPassword, PasswordPublicKey } from '../types'

const TEXT_ENCODER = new TextEncoder()

export async function encryptPassword(password: string): Promise<EncryptedPassword> {
  const passwordPublicKey = await getPasswordPublicKey()
  const cryptoKey = await importPublicKey(passwordPublicKey)
  const encryptedBytes = await globalThis.crypto.subtle.encrypt(
    {
      name: 'RSA-OAEP'
    },
    cryptoKey,
    TEXT_ENCODER.encode(password)
  )

  return {
    passwordCiphertext: bytesToBase64(new Uint8Array(encryptedBytes)),
    passwordKeyId: passwordPublicKey.keyId
  }
}

async function importPublicKey(passwordPublicKey: PasswordPublicKey): Promise<CryptoKey> {
  if (passwordPublicKey.algorithm !== 'RSA-OAEP-256') {
    throw new Error('不支持的密码加密算法')
  }

  return globalThis.crypto.subtle.importKey(
    'spki',
    base64ToBytes(passwordPublicKey.publicKey),
    {
      name: 'RSA-OAEP',
      hash: 'SHA-256'
    },
    false,
    ['encrypt']
  )
}

function base64ToBytes(value: string): ArrayBuffer {
  const binary = globalThis.atob(value)
  const bytes = new Uint8Array(binary.length)

  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }

  return bytes.buffer
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = ''

  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte)
  })

  return globalThis.btoa(binary)
}
