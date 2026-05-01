<template>
  <view class="page">
    <view class="form-card">
      <view class="form-item">
        <text class="label">板块</text>
        <picker
          mode="selector"
          :range="boards"
          range-key="boardName"
          :value="boardIndex"
          @change="handleBoardChange"
        >
          <view class="picker-value">
            {{ selectedBoardName || '请选择板块' }}
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">标题</text>
        <input v-model="title" class="input" type="text" maxlength="200" placeholder="请输入标题（200字以内）" />
      </view>

      <view class="form-item">
        <text class="label">正文</text>
        <textarea
          v-model="content"
          class="textarea"
          maxlength="20000"
          placeholder="请输入正文（请勿包含手机号/微信号等隐私信息）"
        ></textarea>
      </view>

      <view class="form-item switch-row">
        <text class="label">匿名发布</text>
        <switch :checked="anonymousFlag" @change="handleAnonymousChange"></switch>
      </view>
    </view>

    <view class="submit-wrap">
      <button class="cu-btn block bg-blue lg round" :disabled="submitting" @click="handleSubmit">发布</button>
    </view>
  </view>
</template>

<script>
import { createPost, listBoards } from '@/api/community'
import { getTocUserId } from '@/utils/auth'

export default {
  data() {
    return {
      initialBoardId: null,
      boards: [],
      boardIndex: 0,
      title: '',
      content: '',
      anonymousFlag: false,
      submitting: false
    }
  },
  onLoad(options) {
    const boardId = options && options.boardId ? Number(options.boardId) : null
    this.initialBoardId = Number.isFinite(boardId) ? boardId : null
    this.loadBoards()
  },
  computed: {
    selectedBoardId() {
      const board = this.boards[this.boardIndex]
      return board ? board.id : null
    },
    selectedBoardName() {
      const board = this.boards[this.boardIndex]
      return board ? board.boardName : ''
    }
  },
  methods: {
    loadBoards() {
      listBoards()
        .then((res) => {
          this.boards = (res && res.data) || []
          if (this.boards.length === 0) {
            this.$modal.msgError('暂无可用板块，请联系管理员创建')
            return
          }
          if (this.initialBoardId) {
            const idx = this.boards.findIndex((b) => b.id === this.initialBoardId)
            if (idx >= 0) {
              this.boardIndex = idx
            }
          }
        })
        .catch(() => {
          this.$modal.msgError('加载板块失败')
        })
    },
    handleBoardChange(e) {
      const value = e && e.detail ? Number(e.detail.value) : 0
      this.boardIndex = Number.isFinite(value) ? value : 0
    },
    handleAnonymousChange(e) {
      this.anonymousFlag = !!(e && e.detail ? e.detail.value : false)
    },
    handleSubmit() {
      if (!this.selectedBoardId) {
        this.$modal.msgError('请选择板块')
        return
      }
      const tocUserId = Number(getTocUserId())
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常，请重新登录')
        uni.reLaunch({ url: '/pages/login' })
        return
      }
      const title = String(this.title || '').trim()
      const content = String(this.content || '').trim()
      if (!title) {
        this.$modal.msgError('请输入标题')
        return
      }
      if (!content) {
        this.$modal.msgError('请输入正文')
        return
      }

      this.submitting = true
      uni.showLoading({ title: '发布中...' })
      createPost({
        boardId: this.selectedBoardId,
        authorTocUserId: tocUserId,
        title,
        content,
        anonymousFlag: !!this.anonymousFlag
      })
        .then((res) => {
          const data = (res && res.data) || {}
          const status = String(data.status || '')
          if (status === 'PUBLISHED') {
            uni.showToast({ title: '已发布', icon: 'success' })
          } else {
            uni.showToast({ title: '已提交审核', icon: 'none' })
          }
          if (data.id) {
            uni.navigateTo({
              url: `/pages/community/post-detail?postId=${data.id}`
            })
          }
        })
        .catch(() => {
          this.$modal.msgError('发布失败，请稍后重试')
        })
        .finally(() => {
          uni.hideLoading()
          this.submitting = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding: 20rpx;
}

.form-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.form-item {
  padding: 18rpx 0;
  border-bottom: 1px solid #f2f2f2;
}

.form-item:last-child {
  border-bottom: none;
}

.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.input {
  font-size: 28rpx;
  padding: 10rpx 0;
}

.textarea {
  width: 100%;
  min-height: 260rpx;
  font-size: 28rpx;
  padding: 10rpx 0;
}

.picker-value {
  font-size: 28rpx;
  padding: 12rpx 0;
  color: #333;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.submit-wrap {
  margin-top: 24rpx;
}
</style>
