import type { ContentTypeCode } from './topic'

/** 内容草稿状态，与后端 DraftStatus 枚举保持一致。 */
export type DraftStatusCode = 0 | 1

/** 内容草稿生成来源，与后端 GenerationSource 枚举保持一致。 */
export type GenerationSourceCode = 'manual' | 'ai'

export interface ContentDraft {
  id: number
  topicId: number
  userId: number
  contentType: ContentTypeCode | null
  title: string
  summary: string | null
  body: string | null
  scriptContent: string | null
  coverPrompt: string | null
  generationSource: GenerationSourceCode
  status: DraftStatusCode
  createdAt: string
  updatedAt: string
}

export interface ContentDraftFormPayload {
  title: string
  contentType: ContentTypeCode
  summary?: string
  body?: string
  scriptContent?: string
  coverPrompt?: string
  status?: DraftStatusCode
}

export interface SelectOption<T extends string | number = string> {
  label: string
  value: T
}

export const DRAFT_STATUS_OPTIONS: SelectOption<DraftStatusCode>[] = [
  { label: '草稿', value: 0 },
  { label: '已定稿', value: 1 }
]

const draftStatusLabelMap = Object.fromEntries(DRAFT_STATUS_OPTIONS.map((item) => [item.value, item.label])) as Record<
  DraftStatusCode,
  string
>

const generationSourceLabelMap: Record<GenerationSourceCode, string> = {
  manual: '人工录入',
  ai: 'AI 生成'
}

export function getDraftStatusLabel(status: DraftStatusCode): string {
  return draftStatusLabelMap[status] ?? '未知'
}

export function getGenerationSourceLabel(source: GenerationSourceCode): string {
  return generationSourceLabelMap[source] ?? source
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '-'
  }

  const normalized = value.replace('T', ' ')
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized
}

export function createEmptyDraftForm(defaultContentType: ContentTypeCode = 'article'): ContentDraftFormPayload {
  return {
    title: '',
    contentType: defaultContentType,
    summary: '',
    body: '',
    scriptContent: '',
    coverPrompt: '',
    status: 0
  }
}

export function toDraftFormPayload(draft: ContentDraft): ContentDraftFormPayload {
  return {
    title: draft.title,
    contentType: draft.contentType ?? 'article',
    summary: draft.summary ?? '',
    body: draft.body ?? '',
    scriptContent: draft.scriptContent ?? '',
    coverPrompt: draft.coverPrompt ?? '',
    status: draft.status
  }
}

export function summarizeDraftContent(draft: ContentDraft): string {
  const source = draft.summary?.trim() || draft.body?.trim() || draft.scriptContent?.trim()
  if (!source) {
    return '暂无内容摘要'
  }

  return source.length > 80 ? `${source.slice(0, 80)}...` : source
}
