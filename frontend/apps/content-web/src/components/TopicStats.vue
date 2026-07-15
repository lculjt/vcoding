<script setup lang="ts">
import type { TopicMetric } from '../types/topic'

defineProps<{
  metrics: TopicMetric[]
}>()
</script>

<template>
  <section class="topic-stats" aria-label="选题统计">
    <article v-for="metric in metrics" :key="metric.label" class="topic-stats__item">
      <span class="topic-stats__label">{{ metric.label }}</span>
      <strong>{{ metric.value }}</strong>
      <small>{{ metric.trend }}</small>
    </article>
  </section>
</template>

<style scoped lang="scss">
.topic-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.topic-stats__item {
  min-width: 0;
  padding: 16px;
  border: 1px solid var(--vc-border);
  border-radius: 8px;
  background: #ffffff;
}

.topic-stats__label,
.topic-stats__item small {
  display: block;
  color: var(--vc-text-muted);
  font-size: 12px;
  line-height: 18px;
}

.topic-stats__item strong {
  display: block;
  margin: 8px 0 4px;
  color: var(--vc-text-strong);
  font-size: 24px;
  font-weight: 650;
  line-height: 30px;
}

@media (max-width: 1120px) {
  .topic-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .topic-stats {
    grid-template-columns: 1fr;
  }
}
</style>
