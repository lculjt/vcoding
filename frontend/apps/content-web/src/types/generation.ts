/** AI 生成任务类型，与后端 GenerationTaskType 保持一致。 */
export type GenerationTaskTypeCode =
  | 'article'
  | 'video-script'
  | 'outline'
  | 'title-candidates'
  | 'summary'
  | 'tags'
  | 'cover-prompt'

export type GenerationRunStatusCode = 'running' | 'success' | 'failed'

export interface ContentGenerationResult {
  runId: number
  topicId: number
  draftId: number | null
  taskType: GenerationTaskTypeCode
  status: GenerationRunStatusCode
  title: string | null
  summary: string | null
  body: string | null
  scriptContent: string | null
  coverPrompt: string | null
  tags: string | null
  outline: string | null
  titleCandidates: string | null
  errorMessage: string | null
  modelName: string | null
  durationMs: number | null
  createdAt: string
}

export interface ContentGenerationRequest {
  taskType: GenerationTaskTypeCode
}

export interface SelectOption<T extends string = string> {
  label: string
  value: T
  description?: string
}

export const GENERATION_TASK_OPTIONS: SelectOption<GenerationTaskTypeCode>[] = [
  { label: '生成文章', value: 'article', description: '生成正文并自动保存为草稿' },
  { label: '生成短视频脚本', value: 'video-script', description: '生成口播脚本并自动保存为草稿' },
  { label: '生成大纲', value: 'outline', description: '生成分级大纲' },
  { label: '标题候选', value: 'title-candidates', description: '生成 5 个标题候选' },
  { label: '生成摘要', value: 'summary', description: '生成内容摘要' },
  { label: '生成标签', value: 'tags', description: '生成传播标签' },
  { label: '封面提示词', value: 'cover-prompt', description: '生成封面图提示词' }
]

const taskLabelMap = Object.fromEntries(GENERATION_TASK_OPTIONS.map((item) => [item.value, item.label])) as Record<
  GenerationTaskTypeCode,
  string
>

const statusLabelMap: Record<GenerationRunStatusCode, string> = {
  running: '生成中',
  success: '成功',
  failed: '失败'
}

const statusTagType: Record<GenerationRunStatusCode, 'info' | 'success' | 'danger' | 'warning'> = {
  running: 'warning',
  success: 'success',
  failed: 'danger'
}

export function getGenerationTaskLabel(taskType: GenerationTaskTypeCode): string {
  return taskLabelMap[taskType] ?? taskType
}

export function getGenerationStatusLabel(status: GenerationRunStatusCode): string {
  return statusLabelMap[status] ?? status
}

export function getGenerationStatusTagType(status: GenerationRunStatusCode) {
  return statusTagType[status] ?? 'info'
}

export function formatDuration(durationMs: number | null | undefined): string {
  if (!durationMs && durationMs !== 0) {
    return '-'
  }

  if (durationMs < 1000) {
    return `${durationMs} ms`
  }

  return `${(durationMs / 1000).toFixed(1)} s`
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '-'
  }

  const normalized = value.replace('T', ' ')
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized
}

export function summarizeGenerationResult(result: ContentGenerationResult): string {
  if (result.status === 'failed') {
    return result.errorMessage || '生成失败'
  }

  const source =
    result.summary?.trim() ||
    result.body?.trim() ||
    result.scriptContent?.trim() ||
    result.outline?.trim() ||
    result.titleCandidates?.trim() ||
    result.coverPrompt?.trim() ||
    result.tags?.trim()

  if (!source) {
    return '生成完成，暂无摘要内容'
  }

  return source.length > 100 ? `${source.slice(0, 100)}...` : source
}
