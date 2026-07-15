<script setup lang="ts">
import { Delete, EditPen, Plus, View } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  formatDateTime,
  formatTargetPlatforms,
  getContentTypeLabel,
  getTopicStatusLabel,
  type Topic,
  type TopicStatusCode
} from '../types/topic'

defineProps<{
  topics: Topic[]
  loading: boolean
  pageNo: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  create: []
  edit: [topic: Topic]
  delete: [topic: Topic]
  'page-change': [pageNo: number]
  'page-size-change': [pageSize: number]
}>()

const router = useRouter()

function goDetail(topic: Topic) {
  void router.push({ name: 'topic-detail', params: { id: topic.id } })
}

const statusTagType: Record<TopicStatusCode, 'info' | 'success' | 'warning' | 'primary' | 'danger'> = {
  0: 'info',
  1: 'warning',
  2: 'primary',
  3: 'warning',
  4: 'success',
  5: 'info'
}

function getStatusTagType(status: TopicStatusCode) {
  return statusTagType[status] ?? 'info'
}
</script>

<template>
  <section class="topic-table vc-panel">
    <div class="topic-table__head">
      <div>
        <h2>选题列表</h2>
        <p>用于沉淀文章、图片和视频生成前的内容方向。</p>
      </div>
      <el-button type="primary" @click="emit('create')">
        <el-icon>
          <Plus />
        </el-icon>
        新建选题
      </el-button>
    </div>

    <el-table v-loading="loading" :data="topics" class="topic-table__grid" empty-text="暂无选题，点击右上角新建">
      <el-table-column prop="title" label="选题名称" min-width="260">
        <template #default="{ row }">
          <button class="topic-table__title-button" type="button" @click="goDetail(row)">
            <div class="topic-table__title">
              <strong>{{ row.title }}</strong>
              <span>{{ row.contentDirection || '未填写内容方向' }}</span>
            </div>
          </button>
        </template>
      </el-table-column>
      <el-table-column label="目标平台" width="180">
        <template #default="{ row }">
          {{ formatTargetPlatforms(row.targetPlatforms) }}
        </template>
      </el-table-column>
      <el-table-column label="内容类型" width="130">
        <template #default="{ row }">
          {{ getContentTypeLabel(row.contentType) }}
        </template>
      </el-table-column>
      <el-table-column prop="expectedWordCount" label="期望字数" width="110">
        <template #default="{ row }">
          <span class="topic-table__word-count">{{ row.expectedWordCount ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" effect="light" round>
            {{ getTopicStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="topic-table__actions">
            <el-button text title="查看草稿" @click="goDetail(row)">
              <el-icon>
                <View />
              </el-icon>
            </el-button>
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

    <div class="topic-table__pagination">
      <el-pagination
        :current-page="pageNo"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="emit('page-change', $event)"
        @size-change="emit('page-size-change', $event)"
      />
    </div>
  </section>
</template>

<style scoped lang="scss">
.topic-table {
  overflow: hidden;
}

.topic-table__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 18px 14px;
  border-bottom: 1px solid var(--vc-border);
}

.topic-table__head h2,
.topic-table__head p {
  margin: 0;
}

.topic-table__head h2 {
  color: var(--vc-text-strong);
  font-size: 16px;
  line-height: 24px;
}

.topic-table__head p {
  margin-top: 3px;
  color: var(--vc-text-muted);
  font-size: 13px;
  line-height: 20px;
}

.topic-table__grid {
  width: 100%;
}

.topic-table__title-button {
  display: block;
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.topic-table__title-button:hover strong {
  color: var(--vc-primary);
}

.topic-table__title {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.topic-table__title strong {
  overflow: hidden;
  color: var(--vc-text-main);
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-table__title span {
  color: var(--vc-text-muted);
  font-size: 12px;
}

.topic-table__word-count {
  color: var(--vc-primary);
  font-family: var(--vc-font-mono);
  font-weight: 600;
}

.topic-table__actions {
  display: flex;
  align-items: center;
}

.topic-table__pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 18px 18px;
}

@media (max-width: 760px) {
  .topic-table__head {
    align-items: flex-start;
    flex-direction: column;
  }

  .topic-table__pagination {
    justify-content: center;
  }
}
</style>
