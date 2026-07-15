import { authRequest } from '@vcoding/auth-client'
import type { ContentGenerationRequest, ContentGenerationResult } from '../types/generation'

export function generateContent(topicId: number, payload: ContentGenerationRequest): Promise<ContentGenerationResult> {
  return authRequest.post<ContentGenerationResult, ContentGenerationRequest>(
    `/content/topics/${topicId}/generate`,
    payload
  )
}

export function retryGeneration(runId: number): Promise<ContentGenerationResult> {
  return authRequest.post<ContentGenerationResult>(`/content/generation-runs/${runId}/retry`)
}

export function listGenerationRuns(topicId: number): Promise<ContentGenerationResult[]> {
  return authRequest.get<ContentGenerationResult[]>(`/content/topics/${topicId}/generation-runs`)
}

export function getGenerationRun(runId: number): Promise<ContentGenerationResult> {
  return authRequest.get<ContentGenerationResult>(`/content/generation-runs/${runId}`)
}
