import { logout } from '../api/auth'
import type { ChangePasswordHandler, LogoutAndRedirectOptions, RedirectToLoginOptions } from '../types/actions'

const DEFAULT_AUTH_WEB_URL = 'http://localhost:5173/'

/**
 * 构造统一登录页跳转地址。业务系统未登录或退出后都应走同一套 redirect 规则。
 */
export function buildLoginUrl(options: RedirectToLoginOptions = {}): string {
  const authWebUrl = options.authWebUrl || DEFAULT_AUTH_WEB_URL
  const redirectUrl = options.redirectUrl || currentHref()
  const loginUrl = new URL(authWebUrl)

  if (redirectUrl) {
    loginUrl.searchParams.set('redirect', redirectUrl)
  }

  return loginUrl.toString()
}

/**
 * 跳转到统一登录页。该函数只处理浏览器跳转，不清理服务端登录态。
 */
export function redirectToLogin(options: RedirectToLoginOptions = {}): void {
  window.location.assign(buildLoginUrl(options))
}

/**
 * 统一退出动作：先调用 auth 后端清理 HttpOnly Cookie，再清理应用状态并跳转统一登录页。
 */
export async function logoutAndRedirect(options: LogoutAndRedirectOptions = {}): Promise<void> {
  await logout()
  options.onLoggedOut?.()
  redirectToLogin({
    authWebUrl: options.authWebUrl,
    redirectUrl: options.redirectUrl
  })
}

/**
 * 创建统一修改密码入口。当前后端尚未实现修改密码接口，应用可先注入打开弹窗的 handler。
 */
export function createChangePasswordAction(handler?: ChangePasswordHandler): ChangePasswordHandler {
  return async () => {
    if (!handler) {
      throw new Error('修改密码功能尚未接入')
    }
    await handler()
  }
}

function currentHref(): string {
  if (typeof window === 'undefined') {
    return ''
  }

  return window.location.href
}
