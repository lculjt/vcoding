import { inject, type InjectionKey } from 'vue'
import type { OverlayController } from './types'

export const overlayControllerKey: InjectionKey<OverlayController<unknown>> = Symbol('vcoding-overlay-controller')

export function useOverlay<TResult = unknown>(): OverlayController<TResult> {
  const controller = inject(overlayControllerKey)

  if (!controller) {
    throw new Error('useOverlay 必须在 openOverlay 打开的弹出层组件中使用')
  }

  return controller as OverlayController<TResult>
}
