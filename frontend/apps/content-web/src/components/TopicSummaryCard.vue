<script setup lang="ts">
import { ArrowLeft, Plus, Refresh } from '@element-plus/icons-vue'
import {
  formatTargetPlatforms,
  getContentTypeLabel,
  getTopicStatusLabel,
  type Topic
} from '../types/topic'

defineProps<{
  topic: Topic
  loading?: boolean
}>()

const emit = defineEmits<{
  back: []
  refresh: []
}>()
</script>

<template>
  <section class="topic-summary vc-panel">
    <div class="topic-summary__head">
      <div class="topic-summary__title-row">
        <el-button text class="topic-summary__back" @click="emit('back')">
          <el-icon>
            <ArrowLeft />
          </el-icon>
          返回选题库
        </el-button>
        <el-button :loading="loading" @click="emit('refresh')">
          <el-icon>
            <Refresh />
          </el-icon>
          刷新
        </el-button>
      </div>

      <div class="topic-summary__main">
        <div>
          <h1>{{ topic.title }}</h1>
          <p>{{ topic.contentDirection || '未填写内容方向' }}</p>
        </div>
        <el-tag effect="light" round>{{ getTopicStatusLabel(topic.status) }}</el-tag>
      </div>
    </div>

    <dl class="topic-summary__meta">
      <div>
        <dt>目标平台</dt>
        <dd>{{ formatTargetPlatforms(topic.targetPlatforms) }}</dd>
      </div>
      <div>
        <dt>内容类型</dt>
        <dd>{{ getContentTypeLabel(topic.contentType) }}</dd>
      </div>
      <div>
        <dt>目标受众</dt>
        <dd>{{ topic.targetAudience || '-' }}</dd>
      </div>
      <div>
        <dt>核心关键词</dt>
        <dd>{{ topic.keywords || '-' }}</dd>
      </div>
      <div>
        <dt>期望字数</dt>
        <dd>{{ topic.expectedWordCount ?? '-' }}</dd>
      </div>
      <div>
        <dt>备注</dt>
        <dd>{{ topic.remark || '-' }}</dd>
      </div>
    </dl>
  </section>
</template>

<style scoped lang="scss">
.topic-summary {
  overflow: hidden;
}

.topic-summary__head {
  padding: 18px 18px 0;
}

.topic-summary__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.topic-summary__back {
  padding-left: 0;
}

.topic-summary__main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.topic-summary__main h1,
.topic-summary__main p {
  margin: 0;
}

.topic-summary__main h1 {
  color: var(--vc-text-strong);
  font-size: 24px;
  font-weight: 650;
  line-height: 32px;
}

.topic-summary__main p {
  margin-top: 6px;
  color: var(--vc-text-muted);
  font-size: 14px;
  line-height: 22px;
}

.topic-summary__meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px 18px;
  margin: 0;
  padding: 18px;
  border-top: 1px solid var(--vc-border);
}

.topic-summary__meta div {
  min-width: 0;
}

.topic-summary__meta dt,
.topic-summary__meta dd {
  margin: 0;
}

.topic-summary__meta dt {
  color: var(--vc-text-muted);
  font-size: 12px;
  line-height: 18px;
}

.topic-summary__meta dd {
  margin-top: 4px;
  color: var(--vc-text-main);
  font-size: 14px;
  line-height: 22px;
  word-break: break-word;
}

@media (max-width: 900px) {
  .topic-summary__meta {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .topic-summary__main {
    flex-direction: column;
  }

  .topic-summary__meta {
    grid-template-columns: 1fr;
  }
}
</style>
