export interface CurrentUser {
  id: string
  username: string
  nickname?: string
}

export interface AuthSession {
  authenticated: boolean
  user: CurrentUser | null
}

export const anonymousSession: AuthSession = {
  authenticated: false,
  user: null
}
