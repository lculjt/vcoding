<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useOverlay } from '@vcoding/ui'
import { createDraft, getDraft, updateDraft } from '../api/draft'
import {
  DRAFT_STATUS_OPTIONS,
  createEmptyDraftForm,
  toDraftFormPayload,
  type ContentDraftFormPayload
} from '../types/draft'
import { CONTENT_TYPE_OPTIONS, type ContentTypeCode } from '../types/topic'

const props = defineProps<{
  mode: 'create' | 'edit'
  topicId: number
  draftId?: number
  defaultContentType?: ContentTypeCode | null
}>()

const overlay = useOverlay<boolean>()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const form = reactive<ContentDraftFormPayload>(
  createEmptyDraftForm(props.defaultContentType ?? 'article')
)

const drawerTitle = computed(() => (props.mode === 'create' ? '新建内容草稿' : '编辑内容草稿'))

const rules: FormRules = {
  title: [
    { required: true, message: '请输入草稿标题', trigger: 'blur' },
    { max: 200, message: '草稿标题不能超过 200 个字符', trigger: 'blur' }
  ],
  contentType: [{ required: true, message: '请选择内容类型', trigger: 'change' }]
}

onMounted(() => {
  if (props.mode === 'edit' && props.draftId) {
    void loadDraft(props.draftId)
  }
})

async function loadDraft(draftId: number) {
  loading.value = true

  try {
    const draft = await getDraft(draftId)
    Object.assign(form, toDraftFormPayload(draft))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载草稿详情失败')
    overlay.close('load-failed')
  } finally {
    loading.value = false
  }
}

function handleClosed() {
  if (!submitting.value) {
    overlay.close('drawer-closed')
  }
}

async function submitForm() {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true

  try {
    if (props.mode === 'create') {
      await createDraft(props.topicId, form)
      ElMessage.success('草稿创建成功')
    } else if (props.draftId) {
      await updateDraft(props.draftId, form)
      ElMessage.success('草稿更新成功')
    }

    overlay.resolve(true)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存草稿失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-drawer
    :model-value="true"
    :title="drawerTitle"
    size="640px"
    destroy-on-close
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="form"
      :rules="rules"
      label-position="top"
      class="draft-form"
    >
      <el-form-item label="草稿标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="例如：AI 编程工具效率提升指南" />
      </el-form-item>

      <el-form-item label="内容类型" prop="contentType">
        <el-select v-model="form.contentType" placeholder="选择内容类型" style="width: 100%">
          <el-option
            v-for="option in CONTENT_TYPE_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="简要概括这篇内容的核心观点"
        />
      </el-form-item>

      <el-form-item label="正文">
        <el-input
          v-model="form.body"
          type="textarea"
          :rows="8"
          placeholder="文章正文，支持后续接入 AI 生成结果"
        />
      </el-form-item>

      <el-form-item label="短视频脚本">
        <el-input
          v-model="form.scriptContent"
          type="textarea"
          :rows="6"
          placeholder="口播脚本、分镜或钩子设计"
        />
      </el-form-item>

      <el-form-item label="封面图提示词">
        <el-input
          v-model="form.coverPrompt"
          maxlength="500"
          show-word-limit
          placeholder="用于后续生成封面图的描述"
        />
      </el-form-item>

      <el-form-item v-if="mode === 'edit'" label="草稿状态">
        <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
          <el-option
            v-for="option in DRAFT_STATUS_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="draft-form__footer">
        <el-button @click="handleClosed">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.draft-form {
  padding-right: 4px;
}

.draft-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
