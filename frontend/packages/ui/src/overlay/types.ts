import type { AppContext, Component, Ref, VNodeChild } from 'vue'

export interface OverlayController<TResult = unknown> {
  visible: Ref<boolean>
  resolve: (value: TResult) => void
  reject: (reason?: unknown) => void
  close: (reason?: unknown) => void
}

export interface BaseOpenOverlayOptions {
  container?: Element | string
  appContext?: AppContext
  destroyDelay?: number
}

export interface ComponentOpenOverlayOptions<TProps extends object = Record<string, unknown>> extends BaseOpenOverlayOptions {
  component: Component
  props?: TProps
}

export interface RenderOpenOverlayOptions<TResult = unknown> extends BaseOpenOverlayOptions {
  render: (controller: OverlayController<TResult>) => VNodeChild
}

export type OpenOverlayOptions<TResult = unknown, TProps extends object = Record<string, unknown>> =
  | ComponentOpenOverlayOptions<TProps>
  | RenderOpenOverlayOptions<TResult>
