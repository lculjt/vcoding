export interface PasswordPublicKey {
  keyId: string
  publicKey: string
  algorithm: 'RSA-OAEP-256'
  expiresInSeconds: number
}

export interface EncryptedPassword {
  passwordCiphertext: string
  passwordKeyId: string
}
