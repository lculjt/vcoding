<script setup lang="ts">
import type { LoginTab, PasswordLoginForm, SmsLoginForm } from '../types/auth'

defineProps<{
  passwordForm: PasswordLoginForm
  smsForm: SmsLoginForm
  smsButtonText: string
  smsCountdown: number
  smsSending: boolean
  loginSubmitting: boolean
}>()

const loginTab = defineModel<LoginTab>('loginTab', { required: true })

const emit = defineEmits<{
  submit: []
  sendSmsCode: []
  switchRegister: []
}>()
</script>

<template>
  <div class="auth-card-content">
    <el-tabs v-model="loginTab" stretch>
      <el-tab-pane label="密码登录" name="password">
        <el-form class="auth-form" label-position="top" @submit.prevent="emit('submit')">
          <el-form-item label="账号">
            <el-input v-model="passwordForm.account" size="large" placeholder="用户名或手机号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="passwordForm.password"
              size="large"
              type="password"
              placeholder="请输入登录密码"
              show-password
            />
          </el-form-item>
          <div class="form-row between">
            <el-checkbox v-model="passwordForm.remember">保持登录</el-checkbox>
            <button class="vc-link-button" type="button">忘记密码</button>
          </div>
          <el-button class="submit-button" type="primary" size="large" :loading="loginSubmitting" @click="emit('submit')">
            登录
          </el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="短信登录" name="sms">
        <el-form class="auth-form" label-position="top" @submit.prevent="emit('submit')">
          <el-form-item label="手机号">
            <el-input v-model="smsForm.phone" maxlength="11" size="large" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="短信验证码">
            <div class="inline-field">
              <el-input
                v-model="smsForm.smsCode"
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
          <el-button class="submit-button" type="primary" size="large" :loading="loginSubmitting" @click="emit('submit')">
            登录
          </el-button>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <footer class="auth-card-footer">
      <span>没有账号？</span>
      <button class="vc-link-button" type="button" @click="emit('switchRegister')">创建账号</button>
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
  margin-bottom: 10px;
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

.auth-card-content :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(148, 163, 184, 0.18);
}

.auth-card-content :deep(.el-tabs__item) {
  color: var(--vc-text-inverse-muted);
}

.auth-card-content :deep(.el-tabs__item.is-active) {
  color: var(--vc-text-inverse);
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

.form-row {
  display: flex;
  align-items: center;
  gap: var(--vc-space-3);
  margin: -2px 0 10px;
}

.between {
  justify-content: space-between;
}

.form-row :deep(.el-checkbox__label) {
  color: var(--vc-text-inverse-muted);
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

  .form-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
