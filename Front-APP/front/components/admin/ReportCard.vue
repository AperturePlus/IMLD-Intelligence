<template>
  <view class="report-card">
    <view class="report-header">
      <text class="report-title">{{ title }}</text>
      <text class="report-time">{{ item.createtime }}</text>
    </view>
    <text class="report-content">举报原因: {{ item.content }}</text>
    <text class="report-user">举报人: {{ item.reporternickname }}</text>
    <view class="report-actions">
      <button class="ignore-btn" @click="onAction('ignore')">忽略</button>
      <button class="process-btn" @click="onAction('process')">处理</button>
    </view>
  </view>
</template>

<script>
export default {
  props: {
    item: Object,
    type: String
  },
  computed: {
    title() {
      if (this.type === 'post') return '举报帖子: ' + this.item.title
      if (this.type === 'comment') return '举报评价: ' + this.item.title
      if (this.type === 'user') return '举报用户: ' + this.item.reporteenickname
      return ''
    }
  },
  methods: {
    onAction(action) {
      this.$emit('onAction', { id: this.item.id, action })
    }
  }
}
</script>

<style scoped>
.report-card {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 20rpx;
}

.report-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.report-title {
  font-weight: bold;
  font-size: 30rpx;
}

.report-time {
  font-size: 24rpx;
  color: #999;
}

.report-content,
.report-user {
  display: block;
  font-size: 26rpx;
  color: #555;
  margin-top: 8rpx;
}

.report-actions {
  margin-top: 20rpx;
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.ignore-btn {
  background-color: #f0f0f0;
  color: #666;
  padding: 10rpx 20rpx;
  border-radius: 6rpx;
}

.process-btn {
  background-color: #409EFF;
  color: #fff;
  padding: 10rpx 20rpx;
  border-radius: 6rpx;
}
</style>
