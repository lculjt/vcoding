import axios, { type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '../types'

const http = axios.create({
  baseURL: '/api',
  withCredentials: true
})

const SUCCESS_CODE = 0

http.interceptors.request.use((config) => {
  // 统一标记前端 AJAX 请求，后续后端可据此区分页面导航和接口调用。
  config.headers.set('X-Requested-With', 'XMLHttpRequest')
  return config
})

http.interceptors.response.use(
  (response) => {
    const responseData = response.data

    if (isApiResponse(responseData)) {
      if (responseData.code !== SUCCESS_CODE) {
        throw new Error(responseData.message || '请求失败')
      }

      return responseData.data
    }

    return responseData
  },
  (error) => Promise.reject(normalizeApiError(error))
)

export const authRequest = {
  get<TResponse>(url: string, config?: AxiosRequestConfig): Promise<TResponse> {
    return http.get<ApiResponse<TResponse>, TResponse>(url, config)
  },

  post<TResponse, TBody = unknown>(url: string, data?: TBody, config?: AxiosRequestConfig): Promise<TResponse> {
    return http.post<ApiResponse<TResponse>, TResponse, TBody>(url, data, config)
  },

  put<TResponse, TBody = unknown>(url: string, data?: TBody, config?: AxiosRequestConfig): Promise<TResponse> {
    return http.put<ApiResponse<TResponse>, TResponse, TBody>(url, data, config)
  },

  delete<TResponse>(url: string, config?: AxiosRequestConfig): Promise<TResponse> {
    return http.delete<ApiResponse<TResponse>, TResponse>(url, config)
  }
}

function isApiResponse(value: unknown): value is ApiResponse<unknown> {
  if (!value || typeof value !== 'object') {
    return false
  }

  return 'code' in value && 'message' in value && 'data' in value
}

function normalizeApiError(error: unknown): Error {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data as Partial<ApiResponse<unknown>> | undefined
    return new Error(responseData?.message || error.message || '请求失败')
  }

  return error instanceof Error ? error : new Error('请求失败')
}
