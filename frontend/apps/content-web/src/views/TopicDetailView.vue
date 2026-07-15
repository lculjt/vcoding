<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { openOverlay } from '@vcoding/ui'
import { deleteDraft, listDraftsByTopic } from '../api/draft'
import { listGenerationRuns, retryGeneration } from '../api/generation'
import { getTopic } from '../api/topic'
import ContentGenerationPanel from '../components/ContentGenerationPanel.vue'
import ContentShell from '../components/ContentShell.vue'
import DraftFormDrawer from '../components/DraftFormDrawer.vue'
import DraftTable from '../components/DraftTable.vue'
import GenerationRunTable from '../components/GenerationRunTable.vue'
import TopicSummaryCard from '../components/TopicSummaryCard.vue'
import type { ContentDraft } from '../types/draft'
import type { ContentGenerationResult } from '../types/generation'
import type { Topic } from '../types/topic'

const route = useRoute()
const router = useRouter()

const topic = ref<Topic | null>(null)
const drafts = ref<ContentDraft[]>([])
const generationRuns = ref<ContentGenerationResult[]>([])
const loading = ref(false)
const generating = ref(false)
const retryingRunId = ref<number | null>(null)

const topicId = computed(() => Number(route.params.id))

onMounted(() => {
  void refreshAll()
})

async function refreshAll() {
  if (!Number.isFinite(topicId.value) || topicId.value <= 0) {
    ElMessage.error('选题 ID 无效')
    void router.replace({ name: 'topic-library' })
    return
  }

  loading.value = true

  try {
    const [topicResult, draftResult, runResult] = await Promise.all([
      getTopic(topicId.value),
      listDraftsByTopic(topicId.value),
      listGenerationRuns(topicId.value)
    ])
    topic.value = topicResult
    drafts.value = draftResult
    generationRuns.value = runResult
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载选题详情失败')
    void router.replace({ name: 'topic-library' })
  } finally {
    loading.value = false
  }
}

function goBack() {
  void router.push({ name: 'topic-library' })
}

async function openCreateDrawer() {
  if (!topic.value) {
    return
  }

  try {
    await openOverlay({
      component: DraftFormDrawer,
      props: {
        mode: 'create',
        topicId: topic.value.id,
        defaultContentType: topic.value.contentType
      }
    })
    await refreshAll()
  } catch {
    // 用户取消弹层时不刷新。
  }
}

async function openEditDrawer(draft: ContentDraft) {
  if (!topic.value) {
    return
  }

  try {
    await openOverlay({
      component: DraftFormDrawer,
      props: {
        mode: 'edit',
        topicId: topic.value.id,
        draftId: draft.id,
        defaultContentType: topic.value.contentType
      }
    })
    await refreshAll()
  } catch {
    // 用户取消弹层时不刷新。
  }
}

async function handleDelete(draft: ContentDraft) {
  try {
    await ElMessageBox.confirm(`确定删除草稿「${draft.title}」吗？删除后不可恢复。`, '删除草稿', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await deleteDraft(draft.id)
    ElMessage.success('草稿已删除')
    await refreshAll()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除草稿失败')
  }
}

async function handleGenerated() {
  await refreshAll()
}

async function handleRetry(run: ContentGenerationResult) {
  retryingRunId.value = run.runId

  try {
    const result = await retryGeneration(run.runId)
    if (result.status === 'success') {
      ElMessage.success(result.draftId ? '重试成功，已保存为草稿' : '重试成功')
    } else {
      ElMessage.error(result.errorMessage || '重试失败')
    }
    await refreshAll()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '重试失败')
  } finally {
    retryingRunId.value = null
  }
}
</script>

<template>
  <ContentShell>
    <section v-if="topic" class="topic-detail">
      <TopicSummaryCard :topic="topic" :loading="loading" @back="goBack" @refresh="refreshAll" />
      <ContentGenerationPanel
        :topic-id="topic.id"
        :generating="generating"
        @generated="handleGenerated"
        @update:generating="generating = $event"
      />
      <GenerationRunTable
        :runs="generationRuns"
        :loading="loading"
        :retrying-run-id="retryingRunId"
        @retry="handleRetry"
      />
      <DraftTable
        :drafts="drafts"
        :loading="loading"
        @create="openCreateDrawer"
        @edit="openEditDrawer"
        @delete="handleDelete"
      />
    </section>
  </ContentShell>
</template>

<style scoped lang="scss">
.topic-detail {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 18px;
}
</style>
