<script setup lang="ts">
import { ref } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { generateContent } from '../api/generation'
import {
  GENERATION_TASK_OPTIONS,
  summarizeGenerationResult,
  type ContentGenerationResult,
  type GenerationTaskTypeCode
} from '../types/generation'

const props = defineProps<{
  topicId: number
  generating: boolean
}>()

const emit = defineEmits<{
  generated: [result: ContentGenerationResult]
  'update:generating': [value: boolean]
}>()

const taskType = ref<GenerationTaskTypeCode>('article')
const latestResult = ref<ContentGenerationResult | null>(null)

async function handleGenerate() {
  emit('update:generating', true)

  try {
    const result = await generateContent(props.topicId, { taskType: taskType.value })
    latestResult.value = result

    if (result.status === 'success') {
      ElMessage.success(result.draftId ? '生成成功，已保存为草稿' : '生成成功')
    } else {
      ElMessage.error(result.errorMessage || '生成失败')
    }

    emit('generated', result)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '生成失败')
  } finally {
    emit('update:generating', false)
  }
}
</script>

<template>
  <section class="generation-panel vc-panel">
    <div class="generation-panel__head">
      <div>
        <h2>
          <el-icon>
            <MagicStick />
          </el-icon>
          AI 内容生成
        </h2>
        <p>基于当前选题触发 AI 生成。文章和脚本会自动保存为草稿。</p>
      </div>
    </div>

    <div class="generation-panel__controls">
      <el-select v-model="taskType" placeholder="选择生成类型" style="width: 240px">
        <el-option
          v-for="option in GENERATION_TASK_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        >
          <div class="generation-panel__option">
            <strong>{{ option.label }}</strong>
            <span>{{ option.description }}</span>
          </div>
        </el-option>
      </el-select>

      <el-button type="primary" :loading="generating" @click="handleGenerate">开始生成</el-button>
    </div>

    <article v-if="latestResult" class="generation-panel__preview">
      <div class="generation-panel__preview-head">
        <strong>最近一次生成结果</strong>
        <el-tag
          :type="latestResult.status === 'success' ? 'success' : latestResult.status === 'failed' ? 'danger' : 'warning'"
          effect="light"
          round
        >
          {{ latestResult.status === 'success' ? '成功' : latestResult.status === 'failed' ? '失败' : '生成中' }}
        </el-tag>
      </div>
      <p>{{ summarizeGenerationResult(latestResult) }}</p>
      <small v-if="latestResult.draftId">已关联草稿 ID：{{ latestResult.draftId }}</small>
      <small v-if="latestResult.modelName">模型：{{ latestResult.modelName }}</small>
    </article>
  </section>
</template>

<style scoped lang="scss">
.generation-panel {
  overflow: hidden;
}

.generation-panel__head {
  padding: 18px 18px 0;
}

.generation-panel__head h2,
.generation-panel__head p {
  margin: 0;
}

.generation-panel__head h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--vc-text-strong);
  font-size: 16px;
  line-height: 24px;
}

.generation-panel__head p {
  margin-top: 4px;
  color: var(--vc-text-muted);
  font-size: 13px;
  line-height: 20px;
}

.generation-panel__controls {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px 18px;
}

.generation-panel__option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.generation-panel__option strong {
  font-size: 13px;
}

.generation-panel__option span {
  color: var(--vc-text-muted);
  font-size: 12px;
}

.generation-panel__preview {
  margin: 0 18px 18px;
  padding: 14px 16px;
  border: 1px solid var(--vc-border);
  border-radius: 8px;
  background: var(--vc-bg-subtle);
}

.generation-panel__preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.generation-panel__preview p {
  margin: 0;
  color: var(--vc-text-main);
  font-size: 14px;
  line-height: 22px;
  white-space: pre-wrap;
}

.generation-panel__preview small {
  display: block;
  margin-top: 8px;
  color: var(--vc-text-muted);
  font-size: 12px;
}
</style>
