import { authRequest } from '@vcoding/auth-client'
import type { PageResult, Topic, TopicFormPayload, TopicQueryParams } from '../types/topic'

export function pageTopics(params: TopicQueryParams = {}): Promise<PageResult<Topic>> {
  return authRequest.get<PageResult<Topic>>('/content/topics', { params })
}

export function getTopic(id: number): Promise<Topic> {
  return authRequest.get<Topic>(`/content/topics/${id}`)
}

export function createTopic(payload: TopicFormPayload): Promise<Topic> {
  return authRequest.post<Topic, TopicFormPayload>('/content/topics', payload)
}

export function updateTopic(id: number, payload: TopicFormPayload): Promise<Topic> {
  return authRequest.put<Topic, TopicFormPayload>(`/content/topics/${id}`, payload)
}

export function deleteTopic(id: number): Promise<void> {
  return authRequest.delete<void>(`/content/topics/${id}`)
}
