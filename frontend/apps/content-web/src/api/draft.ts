import { authRequest } from '@vcoding/auth-client'
import type { ContentDraft, ContentDraftFormPayload } from '../types/draft'

export function listDraftsByTopic(topicId: number): Promise<ContentDraft[]> {
  return authRequest.get<ContentDraft[]>(`/content/topics/${topicId}/drafts`)
}

export function getDraft(id: number): Promise<ContentDraft> {
  return authRequest.get<ContentDraft>(`/content/drafts/${id}`)
}

export function createDraft(topicId: number, payload: ContentDraftFormPayload): Promise<ContentDraft> {
  return authRequest.post<ContentDraft, ContentDraftFormPayload>(`/content/topics/${topicId}/drafts`, payload)
}

export function updateDraft(id: number, payload: ContentDraftFormPayload): Promise<ContentDraft> {
  return authRequest.put<ContentDraft, ContentDraftFormPayload>(`/content/drafts/${id}`, payload)
}

export function deleteDraft(id: number): Promise<void> {
  return authRequest.delete<void>(`/content/drafts/${id}`)
}
