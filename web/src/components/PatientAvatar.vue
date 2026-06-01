<template>
  <el-avatar
    v-bind="$attrs"
    :size="size"
    :src="displaySrc"
    class="patient-avatar"
    :style="avatarStyle"
    @error="handleImageError"
  >
    {{ fallbackText }}
  </el-avatar>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

defineOptions({
  inheritAttrs: false
})

const props = withDefaults(defineProps<{
  name?: string | null
  src?: string | null
  size?: number | string
}>(), {
  name: '',
  src: '',
  size: 40
})

const PALETTE = [
  '#4f9a94',
  '#5b8fc8',
  '#6f8f72',
  '#9a7fbc',
  '#c7836b',
  '#5f91a8',
  '#b07f56',
  '#8c8f5a'
]

const imageFailed = ref(false)

const normalizedName = computed(() => String(props.name || '').trim())

const normalizedSrc = computed(() => String(props.src || '').trim())

const displaySrc = computed(() => (imageFailed.value ? '' : normalizedSrc.value))

const fallbackText = computed(() => {
  const name = normalizedName.value
  if (!name) {
    return '患者'
  }

  const chineseChars = Array.from(name.matchAll(/\p{Script=Han}/gu), (match) => match[0])
  if (chineseChars.length >= 2) {
    return chineseChars.slice(-2).join('')
  }

  const visibleChars = Array.from(name.replace(/\s+/g, ''))
  return visibleChars.slice(0, 2).join('') || '患者'
})

const colorSeed = computed(() => {
  const value = normalizedName.value || fallbackText.value
  return Array.from(value).reduce((hash, char) => hash + char.codePointAt(0)!, 0)
})

const backgroundColor = computed(() => PALETTE[Math.abs(colorSeed.value) % PALETTE.length])

const avatarStyle = computed(() => ({
  '--patient-avatar-bg': backgroundColor.value
}))

const handleImageError = () => {
  imageFailed.value = true
  return true
}

watch(normalizedSrc, () => {
  imageFailed.value = false
})
</script>

<style scoped>
.patient-avatar {
  flex-shrink: 0;
  background: var(--patient-avatar-bg);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0;
  user-select: none;
}
</style>
