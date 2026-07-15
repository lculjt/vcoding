<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createImageCaptcha, type ImageCaptcha, type SmsScene } from '@vcoding/auth-client'
import { useOverlay } from '@vcoding/ui/overlay'
import type { CaptchaVerifyResult } from '../types/auth'

const props = defineProps<{
  scene: SmsScene
  phone: string
}>()

const overlay = useOverlay<CaptchaVerifyResult>()
const captcha = ref<ImageCaptcha | null>(null)
const captchaCode = ref('')
const loading = ref(false)
const submitting = ref(false)
const errorText = ref('')

const dialogTitle = computed(() => (props.scene === 'register' ? '注册安全验证' : '登录安全验证'))
const captchaCodePattern = /^[0-9a-zA-Z]{4,8}$/

onMounted(() => {
  void refreshCaptcha()
})

async function refreshCaptcha() {
  loading.value = true
  errorText.value = ''

  try {
    captcha.value = await createImageCaptcha()
    captchaCode.value = ''
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '图形验证码加载失败'
  } finally {
    loading.value = false
  }
}

function confirmCaptcha() {
  if (!captcha.value) {
    errorText.value = '请先加载图形验证码'
    return
  }

  const normalizedCode = captchaCode.value.trim()
  if (!normalizedCode) {
    errorText.value = '请输入图形验证码'
    return
  }

  if (!captchaCodePattern.test(normalizedCode)) {
    errorText.value = '图形验证码应为 4-8 位字母或数字'
    return
  }

  submitting.value = true
  // 图形验证码只负责确认用户输入，短信发送接口会在服务端完成最终校验。
  overlay.resolve({
    captchaId: captcha.value.captchaId,
    captchaCode: normalizedCode
  })
}

function cancelCaptcha() {
  overlay.close('cancel-captcha')
}

function handleClosed() {
  if (!submitting.value) {
    overlay.close('dialog-closed')
  }
}

function handleRefreshClick() {
  void refreshCaptcha().catch((error) => {
    ElMessage.error(error instanceof Error ? error.message : '刷新验证码失败')
  })
}
</script>

<template>
  <el-dialog
    v-model="overlay.visible.value"
    :title="dialogTitle"
    width="360px"
    append-to-body
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="captcha-dialog">
      <p class="captcha-dialog__desc">发送短信验证码前，请先完成图形验证码校验。</p>

      <div class="captcha-dialog__image-row">
        <button class="captcha-dialog__image-button" type="button" :disabled="loading" @click="handleRefreshClick">
          <img v-if="captcha?.imageDataUrl" :src="captcha.imageDataUrl" alt="图形验证码" />
          <span v-else>{{ loading ? '加载中' : '点击刷新' }}</span>
        </button>
        <el-button text type="primary" :loading="loading" @click="handleRefreshClick">换一张</el-button>
      </div>

      <el-input
        v-model="captchaCode"
        maxlength="8"
        clearable
        size="large"
        placeholder="请输入图形验证码"
        @keyup.enter="confirmCaptcha"
      />

      <p v-if="errorText" class="captcha-dialog__error">{{ errorText }}</p>
    </div>

    <template #footer>
      <el-button @click="cancelCaptcha">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="confirmCaptcha">确认发送</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.captcha-dialog {
  display: grid;
  gap: var(--vc-space-4);
}

.captcha-dialog__desc {
  margin: 0;
  color: var(--vc-text-muted);
  font-size: var(--vc-font-size-label);
  line-height: var(--vc-line-height-label);
}

.captcha-dialog__image-row {
  display: flex;
  align-items: center;
  gap: var(--vc-space-3);
}

.captcha-dialog__image-button {
  display: grid;
  width: 132px;
  height: 44px;
  place-items: center;
  border: 1px solid var(--vc-border);
  border-radius: var(--vc-radius-control);
  color: var(--vc-text-muted);
  background: var(--vc-bg-subtle);
  cursor: pointer;
}

.captcha-dialog__image-button:disabled {
  cursor: not-allowed;
}

.captcha-dialog__image-button img {
  display: block;
  width: 120px;
  height: 40px;
}

.captcha-dialog__error {
  margin: 0;
  color: var(--vc-danger);
  font-size: var(--vc-font-size-helper);
  line-height: var(--vc-line-height-helper);
}
</style>
