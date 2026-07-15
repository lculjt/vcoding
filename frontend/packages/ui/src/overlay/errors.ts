export class OverlayCancelledError extends Error {
  readonly reason?: unknown

  constructor(reason?: unknown) {
    super('弹出层已取消')
    this.name = 'OverlayCancelledError'
    this.reason = reason
  }
}
