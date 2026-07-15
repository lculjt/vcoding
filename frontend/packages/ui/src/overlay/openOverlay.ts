import {
  defineComponent,
  h,
  provide,
  ref,
  render,
  type App,
  type AppContext,
  type Plugin,
  type VNode
} from 'vue'
import { overlayControllerKey } from './context'
import { OverlayCancelledError } from './errors'
import type { OpenOverlayOptions, OverlayController } from './types'

const DEFAULT_DESTROY_DELAY = 240

let defaultAppContext: AppContext | undefined

export function setOverlayAppContext(appOrContext: App | AppContext): void {
  defaultAppContext = '_context' in appOrContext ? appOrContext._context : appOrContext
}

export const VcodingOverlayPlugin: Plugin = {
  install(app) {
    setOverlayAppContext(app)
  }
}

export function openOverlay<TResult = unknown, TProps extends object = Record<string, unknown>>(
  options: OpenOverlayOptions<TResult, TProps>
): Promise<TResult> {
  if (typeof document === 'undefined') {
    return Promise.reject(new Error('openOverlay 只能在浏览器环境中使用'))
  }

  const mountTarget = resolveContainer(options.container)
  const mountNode = document.createElement('div')
  mountNode.className = 'vc-overlay-mount'
  mountTarget.appendChild(mountNode)

  const visible = ref(true)
  const destroyDelay = options.destroyDelay ?? DEFAULT_DESTROY_DELAY
  let settled = false
  let destroyTimer: number | undefined

  return new Promise<TResult>((resolvePromise, rejectPromise) => {
    const cleanup = () => {
      visible.value = false

      destroyTimer = window.setTimeout(() => {
        render(null, mountNode)
        mountNode.remove()
      }, destroyDelay)
    }

    const controller: OverlayController<TResult> = {
      visible,
      resolve(value) {
        if (settled) {
          return
        }

        settled = true
        cleanup()
        resolvePromise(value)
      },
      reject(reason) {
        if (settled) {
          return
        }

        settled = true
        cleanup()
        rejectPromise(reason)
      },
      close(reason) {
        controller.reject(new OverlayCancelledError(reason))
      }
    }

    const OverlayHost = defineComponent({
      name: 'VcOverlayHost',
      setup() {
        // 通过 provide 让弹层内容自行决定何时 resolve 或 reject。
        provide(overlayControllerKey, controller as OverlayController<unknown>)

        return () => ('render' in options ? options.render(controller) : h(options.component, options.props ?? {}))
      }
    })

    const vnode = h(OverlayHost)
    attachAppContext(vnode, options.appContext ?? defaultAppContext)
    render(vnode, mountNode)

    if (destroyTimer) {
      window.clearTimeout(destroyTimer)
    }
  })
}

function resolveContainer(container?: Element | string): Element {
  if (!container) {
    return document.body
  }

  if (typeof container !== 'string') {
    return container
  }

  const element = document.querySelector(container)

  if (!element) {
    throw new Error(`找不到弹出层挂载容器: ${container}`)
  }

  return element
}

function attachAppContext(vnode: VNode, appContext?: AppContext): void {
  if (appContext) {
    vnode.appContext = appContext
  }
}
