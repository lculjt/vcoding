<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  encryptPassword,
  loginByPassword,
  loginBySmsCode,
  register,
  sendSmsCode,
  type SmsScene
} from '@vcoding/auth-client'
import { openOverlay, OverlayCancelledError } from '@vcoding/ui/overlay'
import AuthLoginForm from './AuthLoginForm.vue'
import AuthRegisterForm from './AuthRegisterForm.vue'
import CaptchaVerifyDialog from './CaptchaVerifyDialog.vue'
import { useAuthStore } from '../stores/auth'
import type { AuthMode, CaptchaVerifyResult, LoginTab } from '../types/auth'

const route = useRoute()
const authStore = useAuthStore()
const authMode = ref<AuthMode>('login')
const loginTab = ref<LoginTab>('password')
const smsCountdown = ref(0)
const smsSending = ref(false)
const loginSubmitting = ref(false)
const registerSubmitting = ref(false)
let smsCountdownTimer: number | undefined

const passwordForm = reactive({
  account: '',
  password: '',
  remember: true
})

const smsForm = reactive({
  phone: '',
  smsCode: ''
})

const registerForm = reactive({
  phone: '',
  smsCode: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const defaultLoginRedirect = import.meta.env.VITE_DEFAULT_LOGIN_REDIRECT || 'http://localhost:5176/topics'
const allowedRedirectOrigins = parseAllowedRedirectOrigins(import.meta.env.VITE_ALLOWED_REDIRECT_ORIGINS)

const redirectText = computed(() => {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && isSafeRedirect(redirect) ? redirect : defaultLoginRedirect
})

const smsButtonText = computed(() => (smsCountdown.value > 0 ? `${smsCountdown.value}s 后重发` : '发送验证码'))
const cardTitle = computed(() => (authMode.value === 'register' ? '创建账号' : '欢迎回来'))
const cardDescription = computed(() =>
  authMode.value === 'register' ? '使用手机号验证码创建统一账号。' : '登录后进入默认系统或继续访问目标系统。'
)
const phonePattern = /^1[3-9]\d{9}$/
const smsCodePattern = /^\d{6}$/
const usernamePattern = /^[A-Za-z][A-Za-z0-9_]*$/

async function sendSmsCodeWithCaptcha(scene: SmsScene) {
  if (smsCountdown.value > 0 || smsSending.value) {
    return
  }

  const phone = scene === 'register' ? registerForm.phone.trim() : smsForm.phone.trim()
  if (!validatePhone(phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    const captchaResult = await openOverlay<CaptchaVerifyResult>({
      component: CaptchaVerifyDialog,
      props: {
        scene,
        phone
      }
    })

    smsSending.value = true
    const response = await sendSmsCode({
      scene,
      phone,
      captchaId: captchaResult.captchaId,
      captchaCode: captchaResult.captchaCode
    })

    ElMessage.success('短信验证码已发送')
    startSmsCountdown(response.cooldownSeconds)
  } catch (error) {
    if (error instanceof OverlayCancelledError) {
      return
    }

    ElMessage.error(error instanceof Error ? error.message : '短信验证码发送失败')
  } finally {
    smsSending.value = false
  }
}

function startSmsCountdown(seconds: number) {
  if (smsCountdownTimer) {
    window.clearInterval(smsCountdownTimer)
  }

  smsCountdown.value = seconds
  smsCountdownTimer = window.setInterval(() => {
    smsCountdown.value -= 1
    if (smsCountdown.value <= 0) {
      window.clearInterval(smsCountdownTimer)
      smsCountdownTimer = undefined
    }
  }, 1000)
}

async function submitLogin() {
  if (loginSubmitting.value) {
    return
  }

  if (loginTab.value === 'password' && !validatePasswordLoginForm()) {
    return
  }

  if (loginTab.value === 'sms' && !validateSmsLoginForm()) {
    return
  }

  loginSubmitting.value = true
  try {
    const encryptedPassword =
      loginTab.value === 'password' ? await encryptPassword(passwordForm.password) : undefined

    // 登录成功后后端会写入 HttpOnly Cookie，前端只保存当前用户摘要用于页面状态。
    const response =
      loginTab.value === 'password'
        ? await loginByPassword({
            account: passwordForm.account.trim(),
            passwordCiphertext: encryptedPassword!.passwordCiphertext,
            passwordKeyId: encryptedPassword!.passwordKeyId
          })
        : await loginBySmsCode({
            phone: smsForm.phone.trim(),
            smsCode: smsForm.smsCode.trim()
          })

    authStore.setSession(response.user)
    ElMessage.success('登录成功')
    window.location.assign(redirectText.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    loginSubmitting.value = false
  }
}

async function submitRegister() {
  if (registerSubmitting.value) {
    return
  }

  if (!validateRegisterForm()) {
    return
  }

  registerSubmitting.value = true
  try {
    const encryptedPassword = await encryptPassword(registerForm.password)

    await register({
      username: registerForm.username.trim(),
      phone: registerForm.phone.trim(),
      passwordCiphertext: encryptedPassword.passwordCiphertext,
      passwordKeyId: encryptedPassword.passwordKeyId,
      smsCode: registerForm.smsCode.trim()
    })

    ElMessage.success('账号创建成功，请登录')
    passwordForm.account = registerForm.username.trim()
    passwordForm.password = ''
    authMode.value = 'login'
    loginTab.value = 'password'
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败')
  } finally {
    registerSubmitting.value = false
  }
}

function validatePhone(phone: string): boolean {
  return phonePattern.test(phone.trim())
}

function validateSmsCode(smsCode: string): boolean {
  return smsCodePattern.test(smsCode.trim())
}

function validatePasswordLoginForm(): boolean {
  if (!passwordForm.account.trim()) {
    ElMessage.warning('请输入账号')
    return false
  }

  if (!validatePassword(passwordForm.password)) {
    ElMessage.warning('请输入 8-64 位登录密码')
    return false
  }

  return true
}

function validateSmsLoginForm(): boolean {
  if (!validatePhone(smsForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return false
  }

  if (!validateSmsCode(smsForm.smsCode)) {
    ElMessage.warning('请输入 6 位数字短信验证码')
    return false
  }

  return true
}

function validateRegisterForm(): boolean {
  if (!validateUsername(registerForm.username)) {
    ElMessage.warning('用户名必须为 4-32 位，并以字母开头')
    return false
  }

  if (!validatePhone(registerForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return false
  }

  if (!validateSmsCode(registerForm.smsCode)) {
    ElMessage.warning('请输入 6 位数字短信验证码')
    return false
  }

  if (!validatePassword(registerForm.password)) {
    ElMessage.warning('密码长度必须为 8-64 位')
    return false
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return false
  }

  return true
}

function validateUsername(username: string): boolean {
  const normalizedUsername = username.trim()
  return normalizedUsername.length >= 4 && normalizedUsername.length <= 32 && usernamePattern.test(normalizedUsername)
}

function validatePassword(password: string): boolean {
  return password.length >= 8 && password.length <= 64
}

function isSafeRedirect(redirect: string): boolean {
  // 登录回跳默认只允许站内路径；多域名部署时必须显式配置允许的系统来源。
  if (redirect.startsWith('/') && !redirect.startsWith('//')) {
    return true
  }

  try {
    const redirectUrl = new URL(redirect)
    return allowedRedirectOrigins.includes(redirectUrl.origin)
  } catch {
    return false
  }
}

function parseAllowedRedirectOrigins(value: string | undefined): string[] {
  const configuredOrigins = value
    ?.split(',')
    .map((origin) => origin.trim())
    .filter(Boolean)

  return configuredOrigins?.length
    ? configuredOrigins
    : [
        'http://localhost:5175',
        'http://localhost:5176',
        'http://localhost:5177'
      ]
}
</script>

<template>
  <div class="auth-card-shell">
    <header class="auth-card-header">
      <div>
        <h1>{{ cardTitle }}</h1>
        <p>{{ cardDescription }}</p>
      </div>
      <span class="auth-card-lock">SSO</span>
    </header>

    <Transition name="auth-card" mode="out-in">
      <AuthLoginForm
        v-if="authMode === 'login'"
        key="login"
        v-model:login-tab="loginTab"
        :password-form="passwordForm"
        :sms-form="smsForm"
        :sms-button-text="smsButtonText"
        :sms-countdown="smsCountdown"
        :sms-sending="smsSending"
        :login-submitting="loginSubmitting"
        @submit="submitLogin"
        @send-sms-code="sendSmsCodeWithCaptcha('login')"
        @switch-register="authMode = 'register'"
      />

      <AuthRegisterForm
        v-else
        key="register"
        :register-form="registerForm"
        :sms-button-text="smsButtonText"
        :sms-countdown="smsCountdown"
        :sms-sending="smsSending"
        :register-submitting="registerSubmitting"
        @submit="submitRegister"
        @send-sms-code="sendSmsCodeWithCaptcha('register')"
        @switch-login="authMode = 'login'"
      />
    </Transition>
  </div>
</template>

<style scoped>
.auth-card-shell {
  width: min(420px, 100%);
  padding: var(--vc-space-5) var(--vc-space-6);
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: var(--vc-radius-panel);
  background: rgba(15, 23, 42, 0.78);
  box-shadow: var(--vc-shadow-float);
  backdrop-filter: blur(18px);
}

.auth-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--vc-space-4);
  margin-bottom: var(--vc-space-4);
}

.auth-card-header h1 {
  margin: 0;
  color: var(--vc-text-inverse);
  font-size: 22px;
  font-weight: 650;
  line-height: 28px;
}

.auth-card-header p {
  margin: var(--vc-space-1) 0 0;
  color: var(--vc-text-inverse-muted);
  font-size: 12px;
  line-height: 18px;
}

.auth-card-lock {
  display: inline-flex;
  height: 24px;
  align-items: center;
  padding: 0 8px;
  border: 1px solid rgba(20, 184, 166, 0.28);
  border-radius: var(--vc-radius-round);
  color: var(--vc-accent);
  font-family: var(--vc-font-mono);
  font-size: 11px;
  font-weight: 700;
}

.auth-card-enter-active,
.auth-card-leave-active {
  transition:
    opacity var(--vc-duration-normal) var(--vc-ease-standard),
    transform var(--vc-duration-normal) var(--vc-ease-standard);
}

.auth-card-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.auth-card-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 520px) {
  .auth-card-shell {
    padding: var(--vc-space-4) var(--vc-space-5);
  }

  .auth-card-header {
    margin-bottom: var(--vc-space-4);
  }
}
</style>
