<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useOverlay } from '@vcoding/ui'
import { createTopic, getTopic, updateTopic } from '../api/topic'
import {
  CONTENT_TYPE_OPTIONS,
  TARGET_PLATFORM_OPTIONS,
  TONE_STYLE_OPTIONS,
  createEmptyTopicForm,
  toTopicFormPayload,
  type TopicFormPayload
} from '../types/topic'

const props = defineProps<{
  mode: 'create' | 'edit'
  topicId?: number
}>()

const overlay = useOverlay<boolean>()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const form = reactive<TopicFormPayload>(createEmptyTopicForm())

const drawerTitle = computed(() => (props.mode === 'create' ? '新建选题' : '编辑选题'))

const rules: FormRules = {
  title: [
    { required: true, message: '请输入选题标题', trigger: 'blur' },
    { max: 120, message: '选题标题不能超过 120 个字符', trigger: 'blur' }
  ],
  contentType: [{ required: true, message: '请选择内容类型', trigger: 'change' }],
  expectedWordCount: [
    { type: 'number', min: 100, max: 50000, message: '期望字数需在 100 到 50000 之间', trigger: 'change' }
  ]
}

onMounted(() => {
  if (props.mode === 'edit' && props.topicId) {
    void loadTopic(props.topicId)
  }
})

async function loadTopic(topicId: number) {
  loading.value = true

  try {
    const topic = await getTopic(topicId)
    Object.assign(form, toTopicFormPayload(topic))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载选题详情失败')
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
      await createTopic(form)
      ElMessage.success('选题创建成功')
    } else if (props.topicId) {
      await updateTopic(props.topicId, form)
      ElMessage.success('选题更新成功')
    }

    overlay.resolve(true)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存选题失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-drawer
    :model-value="true"
    :title="drawerTitle"
    size="520px"
    destroy-on-close
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="form"
      :rules="rules"
      label-position="top"
      class="topic-form"
    >
      <el-form-item label="选题标题" prop="title">
        <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="例如：AI 编程工具如何提升团队效率" />
      </el-form-item>

      <el-form-item label="内容方向">
        <el-input v-model="form.contentDirection" maxlength="120" placeholder="例如：AI 工具实践" />
      </el-form-item>

      <el-form-item label="目标受众">
        <el-input v-model="form.targetAudience" maxlength="120" placeholder="例如：中小团队技术负责人" />
      </el-form-item>

      <el-form-item label="核心关键词">
        <el-input
          v-model="form.keywords"
          maxlength="500"
          placeholder="多个关键词用逗号分隔，例如：AI 编程,Codex,效率"
        />
      </el-form-item>

      <el-form-item label="目标平台">
        <el-select
          v-model="form.targetPlatforms"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择计划发布的平台"
          style="width: 100%"
        >
          <el-option
            v-for="option in TARGET_PLATFORM_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <div class="topic-form__row">
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

        <el-form-item label="语气风格">
          <el-select v-model="form.toneStyle" placeholder="选择语气风格" style="width: 100%">
            <el-option
              v-for="option in TONE_STYLE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </div>

      <el-form-item label="期望字数" prop="expectedWordCount">
        <el-input-number v-model="form.expectedWordCount" :min="100" :max="50000" :step="100" style="width: 100%" />
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="补充生成要求或发布计划"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="topic-form__footer">
        <el-button @click="handleClosed">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.topic-form {
  padding-right: 4px;
}

.topic-form__row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.topic-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 560px) {
  .topic-form__row {
    grid-template-columns: 1fr;
  }
}
</style>
