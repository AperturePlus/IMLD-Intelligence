<template>
  <span class="streaming-text">{{ displayedText }}</span>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { splitTextChunks } from "./StreamingTextUtils";

const props = withDefaults(
  defineProps<{
    text: string;
    speed?: number;
    autoplay?: boolean;
  }>(),
  {
    speed: 30,
    autoplay: true,
  }
);

const emit = defineEmits<{
  (e: "complete"): void;
}>();

const index = ref(0);
const timer = ref<ReturnType<typeof setInterval> | null>(null);

const chunks = computed(() => splitTextChunks(props.text));

const displayedText = computed(() =>
  chunks.value.slice(0, index.value).join("")
);

const start = () => {
  stop();
  index.value = 0;
  if (chunks.value.length === 0) {
    emit("complete");
    return;
  }
  timer.value = setInterval(() => {
    if (index.value < chunks.value.length) {
      index.value++;
    } else {
      stop();
      emit("complete");
    }
  }, props.speed);
};

const stop = () => {
  if (timer.value) {
    clearInterval(timer.value);
    timer.value = null;
  }
};

watch(
  () => props.text,
  () => {
    index.value = 0;
    if (props.autoplay) {
      start();
    }
  },
  { immediate: true }
);

onMounted(() => {
  if (props.autoplay && index.value === 0) {
    start();
  }
});

onBeforeUnmount(() => {
  stop();
});
</script>

<style scoped>
.streaming-text {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
