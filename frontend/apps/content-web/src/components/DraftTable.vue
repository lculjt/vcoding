<script setup lang="ts">
import { Delete, EditPen, Plus } from '@element-plus/icons-vue'
import {
  formatDateTime,
  getDraftStatusLabel,
  getGenerationSourceLabel,
  summarizeDraftContent,
  type ContentDraft,
  type DraftStatusCode
} from '../types/draft'
import { getContentTypeLabel } from '../types/topic'

defineProps<{
  drafts: ContentDraft[]
  loading: boolean
}>()

const emit = defineEmits<{
  create: []
  edit: [draft: ContentDraft]
  delete: [draft: ContentDraft]
}>()

const statusTagType: Record<DraftStatusCode, 'info' | 'success'> = {
  0: 'info',
  1: 'success'
}

function getStatusTagType(status: DraftStatusCode) {
  return statusTagType[status] ?? 'info'
}
</script>

<template>
  <section class="draft-table vc-panel">
    <div class="draft-table__head">
      <div>
        <h2>内容草稿</h2>
        <p>在一个选题下维护多份内容资产，后续可用于平台适配与发布。</p>
      </div>
      <el-button type="primary" @click="emit('create')">
        <el-icon>
          <Plus />
        </el-icon>
        新建草稿
      </el-button>
    </div>

    <el-table v-loading="loading" :data="drafts" class="draft-table__grid" empty-text="暂无草稿，点击右上角新建">
      <el-table-column prop="title" label="草稿标题" min-width="220">
        <template #default="{ row }">
          <div class="draft-table__title">
            <strong>{{ row.title }}</strong>
            <span>{{ summarizeDraftContent(row) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="内容类型" width="130">
        <template #default="{ row }">
          {{ getContentTypeLabel(row.contentType) }}
        </template>
      </el-table-column>
      <el-table-column label="来源" width="110">
        <template #default="{ row }">
          {{ getGenerationSourceLabel(row.generationSource) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" effect="light" round>
            {{ getDraftStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <div class="draft-table__actions">
            <el-button text title="编辑" @click="emit('edit', row)">
              <el-icon>
                <EditPen />
              </el-icon>
            </el-button>
            <el-button text title="删除" @click="emit('delete', row)">
              <el-icon>
                <Delete />
              </el-icon>
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<style scoped lang="scss">
.draft-table {
  overflow: hidden;
}

.draft-table__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 18px 14px;
  border-bottom: 1px solid var(--vc-border);
}

.draft-table__head h2,
.draft-table__head p {
  margin: 0;
}

.draft-table__head h2 {
  color: var(--vc-text-strong);
  font-size: 16px;
  line-height: 24px;
}

.draft-table__head p {
  margin-top: 3px;
  color: var(--vc-text-muted);
  font-size: 13px;
  line-height: 20px;
}

.draft-table__grid {
  width: 100%;
}

.draft-table__title {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.draft-table__title strong {
  overflow: hidden;
  color: var(--vc-text-main);
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-table__title span {
  color: var(--vc-text-muted);
  font-size: 12px;
}

.draft-table__actions {
  display: flex;
  align-items: center;
}

@media (max-width: 760px) {
  .draft-table__head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
