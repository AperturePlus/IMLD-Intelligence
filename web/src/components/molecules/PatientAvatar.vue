<template>
  <BaseAvatar
    :src="props.src"
    :text="fallbackText"
    :size="props.size"
    :bg="backgroundColor"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseAvatar from '@/components/atoms/BaseAvatar.vue'

const props = withDefaults(
  defineProps<{
    name?: string | null
    src?: string | null
    size?: number | string
  }>(),
  {
    name: '',
    src: '',
    size: 40
  }
)

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

const normalizedName = computed(() => String(props.name || '').trim())

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
</script>
