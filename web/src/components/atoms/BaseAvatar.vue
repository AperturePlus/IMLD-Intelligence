<template>
  <div class="base-avatar" :style="avatarStyle">
    <img
      v-if="displaySrc"
      :src="displaySrc"
      :alt="displayText"
      class="avatar-img"
      @error="handleError"
    />
    <span v-else class="avatar-text">{{ displayText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    src?: string | null;
    text?: string | null;
    size?: number | string;
    bg?: string;
  }>(),
  {
    src: "",
    text: "",
    size: 40,
    bg: "var(--imld-teal-1)",
  }
);

const imageFailed = ref(false);

const normalizedSrc = computed(() => String(props.src || "").trim());
const normalizedText = computed(() => String(props.text || "").trim());

const displaySrc = computed(() =>
  imageFailed.value ? "" : normalizedSrc.value
);

const displayText = computed(() => {
  const text = normalizedText.value;
  if (!text) {
    return "?";
  }
  const chars = Array.from(text);
  const han = chars.filter((c) => /\p{Script=Han}/u.test(c));
  if (han.length >= 2) {
    return han.slice(-2).join("");
  }
  return chars.slice(0, 2).join("") || "?";
});

const numSize = computed(() => {
  const n = Number(props.size);
  return Number.isFinite(n) && n > 0 ? n : 40;
});

const avatarStyle = computed(() => ({
  width: `${numSize.value}px`,
  height: `${numSize.value}px`,
  backgroundColor: displaySrc.value ? "transparent" : props.bg,
  fontSize: `${Math.max(12, numSize.value * 0.4)}px`,
}));

const handleError = () => {
  imageFailed.value = true;
};

watch(normalizedSrc, () => {
  imageFailed.value = false;
});
</script>

<style scoped>
.base-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  color: var(--imld-white);
  font-weight: 600;
  user-select: none;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-text {
  line-height: 1;
}
</style>
