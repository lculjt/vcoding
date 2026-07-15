/** 选题状态编码，与后端 TopicStatus 枚举保持一致。 */
export type TopicStatusCode = 0 | 1 | 2 | 3 | 4 | 5

export type ContentTypeCode =
  | 'article'
  | 'wechat-article'
  | 'xiaohongshu-note'
  | 'zhihu-answer'
  | 'video-script'
  | 'mixed'

export type ToneStyleCode = 'professional' | 'casual' | 'marketing' | 'educational' | 'review' | 'story'

/** 目标平台编码，与后端 TargetPlatform 枚举保持一致。 */
export type TargetPlatformCode = 'wechat' | 'xiaohongshu' | 'zhihu' | 'douyin' | 'bilibili'

export interface Topic {
  id: number
  userId: number
  title: string
  contentDirection: string | null
  targetAudience: string | null
  keywords: string | null
  targetPlatforms: TargetPlatformCode[]
  contentType: ContentTypeCode | null
  toneStyle: ToneStyleCode | null
  expectedWordCount: number | null
  status: TopicStatusCode
  remark: string | null
  createdAt: string
  updatedAt: string
}

export interface TopicFormPayload {
  title: string
  contentDirection?: string
  targetAudience?: string
  keywords?: string
  targetPlatforms?: TargetPlatformCode[]
  contentType: ContentTypeCode
  toneStyle?: ToneStyleCode
  expectedWordCount?: number
  status?: TopicStatusCode
  remark?: string
}

export interface TopicQueryParams {
  pageNo?: number
  pageSize?: number
  keyword?: string
  status?: TopicStatusCode
  contentType?: ContentTypeCode
}

export interface PageResult<T> {
  pageNo: number
  pageSize: number
  total: number
  pages: number
  records: T[]
}

export interface TopicMetric {
  label: string
  value: string
  trend: string
}

export interface SelectOption<T extends string | number = string> {
  label: string
  value: T
}

export const TOPIC_STATUS_OPTIONS: SelectOption<TopicStatusCode>[] = [
  { label: '草稿', value: 0 },
  { label: '待生成', value: 1 },
  { label: '生成中', value: 2 },
  { label: '待审核', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已归档', value: 5 }
]

export const CONTENT_TYPE_OPTIONS: SelectOption<ContentTypeCode>[] = [
  { label: '标准文章', value: 'article' },
  { label: '公众号长文', value: 'wechat-article' },
  { label: '小红书笔记', value: 'xiaohongshu-note' },
  { label: '知乎回答', value: 'zhihu-answer' },
  { label: '短视频脚本', value: 'video-script' },
  { label: '混合内容', value: 'mixed' }
]

export const TONE_STYLE_OPTIONS: SelectOption<ToneStyleCode>[] = [
  { label: '专业', value: 'professional' },
  { label: '轻松', value: 'casual' },
  { label: '营销', value: 'marketing' },
  { label: '科普', value: 'educational' },
  { label: '测评', value: 'review' },
  { label: '故事化', value: 'story' }
]

export const TARGET_PLATFORM_OPTIONS: SelectOption<TargetPlatformCode>[] = [
  { label: '微信公众号', value: 'wechat' },
  { label: '小红书', value: 'xiaohongshu' },
  { label: '知乎', value: 'zhihu' },
  { label: '抖音 / 视频号', value: 'douyin' },
  { label: 'B 站', value: 'bilibili' }
]

const statusLabelMap = Object.fromEntries(TOPIC_STATUS_OPTIONS.map((item) => [item.value, item.label])) as Record<
  TopicStatusCode,
  string
>

const contentTypeLabelMap = Object.fromEntries(CONTENT_TYPE_OPTIONS.map((item) => [item.value, item.label])) as Record<
  ContentTypeCode,
  string
>

const platformLabelMap = Object.fromEntries(TARGET_PLATFORM_OPTIONS.map((item) => [item.value, item.label])) as Record<
  TargetPlatformCode,
  string
>

export function getTopicStatusLabel(status: TopicStatusCode): string {
  return statusLabelMap[status] ?? '未知'
}

export function getContentTypeLabel(contentType: ContentTypeCode | null | undefined): string {
  if (!contentType) {
    return '-'
  }

  return contentTypeLabelMap[contentType] ?? contentType
}

export function formatTargetPlatforms(platforms: TargetPlatformCode[] | null | undefined): string {
  if (!platforms?.length) {
    return '-'
  }

  return platforms.map((code) => platformLabelMap[code] ?? code).join('、')
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '-'
  }

  const normalized = value.replace('T', ' ')
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized
}

export function createEmptyTopicForm(): TopicFormPayload {
  return {
    title: '',
    contentDirection: '',
    targetAudience: '',
    keywords: '',
    targetPlatforms: [],
    contentType: 'article',
    toneStyle: 'professional',
    expectedWordCount: 1500,
    remark: ''
  }
}

export function toTopicFormPayload(topic: Topic): TopicFormPayload {
  return {
    title: topic.title,
    contentDirection: topic.contentDirection ?? '',
    targetAudience: topic.targetAudience ?? '',
    keywords: topic.keywords ?? '',
    targetPlatforms: [...topic.targetPlatforms],
    contentType: topic.contentType ?? 'article',
    toneStyle: topic.toneStyle ?? 'professional',
    expectedWordCount: topic.expectedWordCount ?? undefined,
    status: topic.status,
    remark: topic.remark ?? ''
  }
}
