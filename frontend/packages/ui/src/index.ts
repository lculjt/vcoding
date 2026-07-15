export const uiPackageName = '@vcoding/ui'

export interface UiPackageInfo {
  name: string
  description: string
}

export const uiPackageInfo: UiPackageInfo = {
  name: uiPackageName,
  description: 'vcoding 平台通用 UI 组件包'
}

export { UserMenu } from './components'
export {
  openOverlay,
  OverlayCancelledError,
  setOverlayAppContext,
  useOverlay,
  VcodingOverlayPlugin
} from './overlay'
export type { OpenOverlayOptions, OverlayController } from './overlay'
export type { UserMenuCommand, UserMenuUser } from './types/user-menu'
