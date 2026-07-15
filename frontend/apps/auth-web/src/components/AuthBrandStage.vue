<template>
  <section class="brand-stage" aria-label="vcoding 平台视觉区">
    <div class="brand-mark">
      <span class="brand-symbol">vc</span>
      <span class="brand-name">vcoding</span>
    </div>

    <div class="tech-orbit" aria-hidden="true">
      <span class="orbit-node node-core"></span>
      <span class="orbit-node node-a"></span>
      <span class="orbit-node node-b"></span>
      <span class="orbit-node node-c"></span>
      <span class="orbit-line line-a"></span>
      <span class="orbit-line line-b"></span>
      <span class="orbit-line line-c"></span>
    </div>

    <div class="stage-status" aria-hidden="true">
      <span class="vc-status-dot"></span>
      <span>gateway</span>
      <span>auth</span>
      <span>systems</span>
    </div>
  </section>
</template>

<style scoped>
.brand-stage {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  padding: var(--vc-space-10);
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: var(--vc-space-3);
  color: var(--vc-text-inverse);
}

.brand-symbol {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: var(--vc-radius-panel);
  background: rgba(15, 23, 42, 0.54);
  color: var(--vc-accent);
  font-family: var(--vc-font-mono);
  font-size: 13px;
  font-weight: 700;
}

.brand-name {
  font-size: 18px;
  font-weight: 650;
}

.tech-orbit {
  position: absolute;
  top: 50%;
  left: 48%;
  width: min(42vw, 520px);
  aspect-ratio: 1;
  transform: translate(-50%, -48%);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 50%;
}

.tech-orbit::before,
.tech-orbit::after {
  position: absolute;
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 50%;
  content: "";
}

.tech-orbit::before {
  inset: 16%;
}

.tech-orbit::after {
  inset: 32%;
  border-color: rgba(20, 184, 166, 0.18);
}

.orbit-node {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: var(--vc-radius-round);
  background: var(--vc-accent);
  box-shadow:
    0 0 0 6px rgba(20, 184, 166, 0.1),
    0 0 28px rgba(20, 184, 166, 0.42);
}

.node-core {
  top: 50%;
  left: 50%;
  width: 16px;
  height: 16px;
  transform: translate(-50%, -50%);
  background: var(--vc-primary);
  box-shadow:
    0 0 0 10px rgba(37, 99, 235, 0.1),
    0 0 44px rgba(37, 99, 235, 0.46);
}

.node-a {
  top: 22%;
  left: 30%;
}

.node-b {
  top: 64%;
  right: 18%;
}

.node-c {
  bottom: 20%;
  left: 24%;
}

.orbit-line {
  position: absolute;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(20, 184, 166, 0.52), transparent);
  transform-origin: left center;
}

.line-a {
  top: 38%;
  left: 32%;
  width: 34%;
  transform: rotate(28deg);
}

.line-b {
  top: 55%;
  left: 50%;
  width: 30%;
  transform: rotate(146deg);
}

.line-c {
  top: 58%;
  left: 32%;
  width: 38%;
  transform: rotate(-24deg);
}

.stage-status {
  position: absolute;
  bottom: var(--vc-space-10);
  left: var(--vc-space-10);
  display: inline-flex;
  align-items: center;
  gap: var(--vc-space-3);
  padding: 10px 12px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: var(--vc-radius-panel);
  color: var(--vc-text-inverse-muted);
  background: rgba(15, 23, 42, 0.48);
  font-family: var(--vc-font-mono);
  font-size: 12px;
}

@media (max-width: 920px) {
  .brand-stage {
    position: absolute;
    inset: 0;
    min-height: 100vh;
    overflow: hidden;
  }

  .tech-orbit {
    left: 50%;
    width: min(82vw, 420px);
    opacity: 0.46;
  }

  .stage-status {
    display: none;
  }
}

@media (max-width: 520px) {
  .brand-stage {
    padding: var(--vc-space-5);
  }

  .brand-name {
    font-size: 16px;
  }
}
</style>
