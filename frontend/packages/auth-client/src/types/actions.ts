export interface AuthActionOptions {
  authWebUrl?: string
}

export interface RedirectToLoginOptions extends AuthActionOptions {
  redirectUrl?: string
}

export interface LogoutAndRedirectOptions extends RedirectToLoginOptions {
  onLoggedOut?: () => void
}

export type ChangePasswordHandler = () => Promise<void> | void
