<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Connection, DataLine, Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { openOverlay } from '@vcoding/ui'
import { deleteTopic, pageTopics } from '../api/topic'
import ContentShell from '../components/ContentShell.vue'
import TopicFilterBar from '../components/TopicFilterBar.vue'
import TopicFormDrawer from '../components/TopicFormDrawer.vue'
import TopicStats from '../components/TopicStats.vue'
import TopicTable from '../components/TopicTable.vue'
import type { Topic, TopicMetric, TopicQueryParams } from '../types/topic'

const topics = ref<Topic[]>([])
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)

const statsTotals = reactive({
  all: 0,
  draft: 0,
  pendingGenerate: 0,
  completed: 0
})

const query = reactive<TopicQueryParams>({
  keyword: undefined,
  status: undefined,
  contentType: undefined
})

const metrics = computed<TopicMetric[]>(() => [
  { label: '选题总数', value: String(statsTotals.all), trend: '当前账号下全部选题' },
  { label: '草稿', value: String(statsTotals.draft), trend: '尚未进入生成流程' },
  { label: '待生成', value: String(statsTotals.pendingGenerate), trend: '等待 AI 内容生成' },
  { label: '已完成', value: String(statsTotals.completed), trend: '可进入平台适配与发布' }
])

onMounted(() => {
  void refreshAll()
})

async function refreshAll() {
  await Promise.all([loadTopics(), loadStats()])
}

async function loadStats() {
  try {
    const [allResult, draftResult, pendingResult, completedResult] = await Promise.all([
      pageTopics({ pageNo: 1, pageSize: 1 }),
      pageTopics({ pageNo: 1, pageSize: 1, status: 0 }),
      pageTopics({ pageNo: 1, pageSize: 1, status: 1 }),
      pageTopics({ pageNo: 1, pageSize: 1, status: 4 })
    ])

    statsTotals.all = allResult.total
    statsTotals.draft = draftResult.total
    statsTotals.pendingGenerate = pendingResult.total
    statsTotals.completed = completedResult.total
  } catch {
    // 统计失败不阻断列表展示，保持页面可用。
  }
}

async function loadTopics() {
  loading.value = true

  try {
    const result = await pageTopics({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      keyword: query.keyword,
      status: query.status,
      contentType: query.contentType
    })

    topics.value = result.records
    total.value = result.total
    pageNo.value = result.pageNo
    pageSize.value = result.pageSize
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载选题列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch(params: TopicQueryParams) {
  query.keyword = params.keyword
  query.status = params.status
  query.contentType = params.contentType
  pageNo.value = 1
  void loadTopics()
}

function handlePageChange(nextPageNo: number) {
  pageNo.value = nextPageNo
  void loadTopics()
}

function handlePageSizeChange(nextPageSize: number) {
  pageSize.value = nextPageSize
  pageNo.value = 1
  void loadTopics()
}

async function openCreateDrawer() {
  try {
    await openOverlay({
      component: TopicFormDrawer,
      props: { mode: 'create' }
    })
    await refreshAll()
  } catch {
    // 用户取消弹层时不刷新列表。
  }
}

async function openEditDrawer(topic: Topic) {
  try {
    await openOverlay({
      component: TopicFormDrawer,
      props: { mode: 'edit', topicId: topic.id }
    })
    await refreshAll()
  } catch {
    // 用户取消弹层时不刷新列表。
  }
}

async function handleDelete(topic: Topic) {
  try {
    await ElMessageBox.confirm(`确定删除选题「${topic.title}」吗？删除后不可恢复。`, '删除选题', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await deleteTopic(topic.id)
    ElMessage.success('选题已删除')

    if (topics.value.length === 1 && pageNo.value > 1) {
      pageNo.value -= 1
    }

    await refreshAll()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除选题失败')
  }
}
</script>

<template>
  <ContentShell>
    <section class="topic-page">
      <div class="topic-page__hero">
        <div>
          <h1>选题库</h1>
          <p>先管理内容方向，再进入文章、图片和视频生成流程。</p>
        </div>

        <div class="topic-page__hero-actions">
          <el-button :loading="loading" @click="refreshAll">
            <el-icon>
              <Refresh />
            </el-icon>
            刷新列表
          </el-button>
          <el-button type="primary" @click="openCreateDrawer">
            <el-icon>
              <Plus />
            </el-icon>
            新建选题
          </el-button>
        </div>
      </div>

      <TopicStats :metrics="metrics" />

      <section class="topic-page__signals">
        <article class="topic-page__signal">
          <el-icon>
            <DataLine />
          </el-icon>
          <div>
            <strong>趋势雷达</strong>
            <span>后续接入平台热榜、搜索词和账号历史表现。</span>
          </div>
        </article>
        <article class="topic-page__signal">
          <el-icon>
            <Connection />
          </el-icon>
          <div>
            <strong>统一发布链路</strong>
            <span>选题通过后进入多平台内容生成与发布任务。</span>
          </div>
        </article>
      </section>

      <TopicFilterBar @search="handleSearch" />

      <TopicTable
        :topics="topics"
        :loading="loading"
        :page-no="pageNo"
        :page-size="pageSize"
        :total="total"
        @create="openCreateDrawer"
        @edit="openEditDrawer"
        @delete="handleDelete"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      />
    </section>
  </ContentShell>
</template>

<style scoped lang="scss">
.topic-page {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 18px;
}

.topic-page__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.topic-page__hero h1,
.topic-page__hero p {
  margin: 0;
}

.topic-page__hero h1 {
  color: var(--vc-text-strong);
  font-size: 28px;
  font-weight: 650;
  line-height: 36px;
}

.topic-page__hero p {
  margin-top: 6px;
  color: var(--vc-text-muted);
  font-size: 14px;
  line-height: 22px;
}

.topic-page__hero-actions {
  display: flex;
  flex: 0 0 auto;
  gap: 10px;
}

.topic-page__signals {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.topic-page__signal {
  display: flex;
  align-items: flex-start;
  min-width: 0;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--vc-border);
  border-radius: 8px;
  background: #ffffff;
}

.topic-page__signal .el-icon {
  display: grid;
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 6px;
  background: var(--vc-primary-soft);
  color: var(--vc-primary);
}

.topic-page__signal strong,
.topic-page__signal span {
  display: block;
}

.topic-page__signal strong {
  color: var(--vc-text-main);
  font-size: 14px;
  line-height: 22px;
}

.topic-page__signal span {
  margin-top: 2px;
  color: var(--vc-text-muted);
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 860px) {
  .topic-page__hero {
    flex-direction: column;
  }

  .topic-page__signals {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .topic-page__hero-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
