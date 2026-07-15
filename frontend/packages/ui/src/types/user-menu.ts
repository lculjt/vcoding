export type UserMenuCommand = 'change-password' | 'logout'

export interface UserMenuUser {
  username: string
  displayName?: string
  avatarUrl?: string
  subtitle?: string
}
