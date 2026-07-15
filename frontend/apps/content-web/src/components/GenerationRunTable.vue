<script setup lang="ts">
import { RefreshRight } from '@element-plus/icons-vue'
import {
  formatDateTime,
  formatDuration,
  getGenerationStatusLabel,
  getGenerationStatusTagType,
  getGenerationTaskLabel,
  summarizeGenerationResult,
  type ContentGenerationResult
} from '../types/generation'

defineProps<{
  runs: ContentGenerationResult[]
  loading: boolean
  retryingRunId: number | null
}>()

const emit = defineEmits<{
  retry: [run: ContentGenerationResult]
}>()
</script>

<template>
  <section class="generation-run-table vc-panel">
    <div class="generation-run-table__head">
      <div>
        <h2>生成记录</h2>
        <p>保留最近 20 次 AI 运行结果，失败任务可直接重试。</p>
      </div>
    </div>

    <el-table v-loading="loading" :data="runs" class="generation-run-table__grid" empty-text="暂无生成记录">
      <el-table-column label="任务类型" width="140">
        <template #default="{ row }">
          {{ getGenerationTaskLabel(row.taskType) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getGenerationStatusTagType(row.status)" effect="light" round>
            {{ getGenerationStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果摘要" min-width="260">
        <template #default="{ row }">
          <span :class="{ 'generation-run-table__error': row.status === 'failed' }">
            {{ summarizeGenerationResult(row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="草稿" width="90">
        <template #default="{ row }">
          {{ row.draftId ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column label="耗时" width="90">
        <template #default="{ row }">
          {{ formatDuration(row.durationMs) }}
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'failed'"
            text
            :loading="retryingRunId === row.runId"
            @click="emit('retry', row)"
          >
            <el-icon>
              <RefreshRight />
            </el-icon>
            重试
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<style scoped lang="scss">
.generation-run-table {
  overflow: hidden;
}

.generation-run-table__head {
  padding: 18px 18px 14px;
  border-bottom: 1px solid var(--vc-border);
}

.generation-run-table__head h2,
.generation-run-table__head p {
  margin: 0;
}

.generation-run-table__head h2 {
  color: var(--vc-text-strong);
  font-size: 16px;
  line-height: 24px;
}

.generation-run-table__head p {
  margin-top: 3px;
  color: var(--vc-text-muted);
  font-size: 13px;
  line-height: 20px;
}

.generation-run-table__grid {
  width: 100%;
}

.generation-run-table__error {
  color: var(--el-color-danger);
}
</style>
