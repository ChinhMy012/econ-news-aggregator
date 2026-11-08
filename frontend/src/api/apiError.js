export class ApiError extends Error {
  constructor(message, { status = 0, code = 'NETWORK_ERROR', errors = [], requestId = null, retryAfter = null } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.errors = errors
    this.requestId = requestId
    this.retryAfter = retryAfter
  }
}
