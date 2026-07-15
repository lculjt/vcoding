<script setup lang="ts">
import AuthBrandStage from '../components/AuthBrandStage.vue'
import AuthCard from '../components/AuthCard.vue'
</script>

<template>
  <main class="auth-page vc-tech-grid-bg">
    <AuthBrandStage />
    <section class="auth-stage" aria-label="统一认证表单">
      <AuthCard />
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  height: 100dvh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(440px, 0.86fr);
  isolation: isolate;
}

.auth-stage {
  position: relative;
  z-index: 1;
  display: grid;
  height: 100vh;
  height: 100dvh;
  min-height: 0;
  align-items: center;
  justify-items: start;
  padding: var(--vc-space-5) var(--vc-space-10);
  overflow: hidden;
}

@media (max-width: 920px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-stage {
    justify-items: center;
    padding: var(--vc-space-5);
  }
}

@media (max-width: 520px) {
  .auth-stage {
    padding: var(--vc-space-4);
  }
}
</style>
