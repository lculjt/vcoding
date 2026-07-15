<script setup lang="ts">
import { reactive } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { CONTENT_TYPE_OPTIONS, TOPIC_STATUS_OPTIONS, type TopicQueryParams } from '../types/topic'

const emit = defineEmits<{
  search: [params: TopicQueryParams]
}>()

const filters = reactive<TopicQueryParams>({
  keyword: '',
  status: undefined,
  contentType: undefined
})

function submitSearch() {
  emit('search', {
    keyword: filters.keyword?.trim() || undefined,
    status: filters.status,
    contentType: filters.contentType
  })
}

function resetFilters() {
  filters.keyword = ''
  filters.status = undefined
  filters.contentType = undefined
  submitSearch()
}
</script>

<template>
  <section class="topic-filter vc-panel">
    <div class="topic-filter__field topic-filter__field--keyword">
      <el-input
        v-model="filters.keyword"
        clearable
        maxlength="60"
        placeholder="搜索标题或关键词"
        @keyup.enter="submitSearch"
      >
        <template #prefix>
          <el-icon>
            <Search />
          </el-icon>
        </template>
      </el-input>
    </div>

    <div class="topic-filter__field">
      <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 100%">
        <el-option
          v-for="option in TOPIC_STATUS_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>

    <div class="topic-filter__field">
      <el-select v-model="filters.contentType" clearable placeholder="全部类型" style="width: 100%">
        <el-option
          v-for="option in CONTENT_TYPE_OPTIONS"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>

    <div class="topic-filter__actions">
      <el-button type="primary" @click="submitSearch">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.topic-filter {
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) 160px 160px auto;
  gap: 12px;
  align-items: center;
  padding: 16px;
}

.topic-filter__actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 960px) {
  .topic-filter {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .topic-filter__field--keyword {
    grid-column: 1 / -1;
  }

  .topic-filter__actions {
    grid-column: 1 / -1;
    justify-content: flex-end;
  }
}

@media (max-width: 560px) {
  .topic-filter {
    grid-template-columns: 1fr;
  }
}
</style>
