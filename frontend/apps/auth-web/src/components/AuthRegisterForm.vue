<script setup lang="ts">
import type { RegisterForm } from '../types/auth'

defineProps<{
  registerForm: RegisterForm
  smsButtonText: string
  smsCountdown: number
  smsSending: boolean
  registerSubmitting: boolean
}>()

const emit = defineEmits<{
  submit: []
  sendSmsCode: []
  switchLogin: []
}>()
</script>

<template>
  <div class="auth-card-content">
    <el-form class="auth-form" label-position="top" @submit.prevent="emit('submit')">
      <el-form-item label="手机号">
        <el-input v-model="registerForm.phone" maxlength="11" size="large" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="短信验证码">
        <div class="inline-field">
          <el-input
            v-model="registerForm.smsCode"
            maxlength="6"
            inputmode="numeric"
            size="large"
            placeholder="输入短信验证码"
          />
          <el-button :disabled="smsCountdown > 0" :loading="smsSending" size="large" @click="emit('sendSmsCode')">
            {{ smsButtonText }}
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="registerForm.username" size="large" placeholder="设置用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
          v-model="registerForm.password"
          size="large"
          type="password"
          placeholder="设置登录密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
          v-model="registerForm.confirmPassword"
          size="large"
          type="password"
          placeholder="再次输入密码"
          show-password
        />
      </el-form-item>
      <el-button
        class="submit-button"
        type="primary"
        size="large"
        :loading="registerSubmitting"
        @click="emit('submit')"
      >
        创建账号
      </el-button>
    </el-form>

    <footer class="auth-card-footer">
      <span>已有账号？</span>
      <button class="vc-link-button" type="button" @click="emit('switchLogin')">返回登录</button>
    </footer>
  </div>
</template>

<style scoped>
.auth-card-content {
  color: var(--vc-text-inverse);
}

.auth-form {
  display: grid;
  gap: 0;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 8px;
}

.auth-form :deep(.el-form-item__label) {
  margin-bottom: 3px;
  color: var(--vc-text-inverse-muted);
  font-size: 12px;
  line-height: 18px;
}

.auth-form :deep(.el-input__wrapper),
.auth-form :deep(.el-button) {
  min-height: 36px;
  height: 36px;
}

.auth-form :deep(.el-input__wrapper) {
  background: rgba(15, 23, 42, 0.66);
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.28) inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px var(--vc-primary) inset,
    0 0 0 3px rgba(37, 99, 235, 0.18);
}

.auth-form :deep(.el-input__inner) {
  color: var(--vc-text-inverse);
}

.auth-form :deep(.el-input__inner::placeholder) {
  color: rgba(203, 213, 225, 0.52);
}

.inline-field {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 116px;
  gap: var(--vc-space-3);
  width: 100%;
}

.inline-field :deep(.el-button) {
  border-color: rgba(148, 163, 184, 0.28);
  color: var(--vc-text-inverse);
  background: rgba(15, 23, 42, 0.66);
}

.inline-field :deep(.el-button:hover) {
  border-color: rgba(37, 99, 235, 0.62);
  color: var(--vc-text-inverse);
  background: rgba(37, 99, 235, 0.18);
}

.inline-field :deep(.el-button.is-disabled) {
  border-color: rgba(148, 163, 184, 0.14);
  color: rgba(203, 213, 225, 0.48);
  background: rgba(15, 23, 42, 0.42);
}

.submit-button {
  width: 100%;
  margin-top: 4px;
}

.auth-card-footer {
  display: flex;
  justify-content: center;
  gap: var(--vc-space-1);
  margin-top: var(--vc-space-4);
  color: var(--vc-text-inverse-muted);
  font-size: var(--vc-font-size-label);
}

@media (max-width: 520px) {
  .inline-field {
    grid-template-columns: 1fr;
  }
}
</style>
