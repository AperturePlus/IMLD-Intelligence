<template>
  <view class="page">
    <view class="post-card" v-if="post">
      <view class="title">{{ post.title }}</view>
      <view class="meta">
        <text>{{ post.authorDisplayName || '用户' }}</text>
        <text class="dot">·</text>
        <text>{{ formatTime(post.createdAt) }}</text>
        <text class="status" v-if="post.status && post.status !== 'PUBLISHED'">{{ post.status }}</text>
      </view>
      <view class="content">{{ post.content }}</view>

      <view class="actions">
        <button class="cu-btn sm round" :disabled="!canInteract" @click="toggleLike">
          {{ liked ? '已赞' : '点赞' }}（{{ post.likeCount || 0 }}）
        </button>
        <button class="cu-btn sm round" :disabled="!canInteract" @click="toggleBookmark">
          {{ bookmarked ? '已收藏' : '收藏' }}
        </button>
        <button class="cu-btn sm round" @click="handleReport">举报</button>
      </view>

      <view class="pending-tip" v-if="post.status && post.status !== 'PUBLISHED'">
        <text>该帖子处于审核状态，暂不支持评论/点赞/收藏。</text>
      </view>
    </view>

    <view class="comment-card">
      <view class="comment-header">
        <text class="comment-title">评论</text>
        <text class="comment-count">{{ (post && post.commentCount) || 0 }}</text>
      </view>

      <view v-if="comments.length === 0" class="empty-comment">
        <text v-if="loadingComments">加载中...</text>
        <text v-else>暂无评论</text>
      </view>

      <view class="comment-item" v-for="c in comments" :key="c.id">
        <view class="c-meta">
          <text class="c-author">{{ c.authorDisplayName || '用户' }}</text>
          <text class="dot">·</text>
          <text class="c-time">{{ formatTime(c.createdAt) }}</text>
        </view>
        <view class="c-content">{{ c.content }}</view>
      </view>

      <view class="load-more text-center">
        <text v-if="loadingComments">加载中...</text>
        <text v-else-if="finishedComments">没有更多了</text>
      </view>
    </view>

    <view class="comment-editor" v-if="post && post.status === 'PUBLISHED'">
      <input v-model="commentText" class="comment-input" type="text" maxlength="5000" placeholder="写下你的评论..." />
      <button class="cu-btn sm round bg-blue" :disabled="submittingComment" @click="submitComment">发送</button>
    </view>
  </view>
</template>

<script>
import { bookmarkPost, createComment, createReport, getPost, likePost, listComments, unbookmarkPost, unlikePost } from '@/api/community'
import { getTocUserId } from '@/utils/auth'

export default {
  data() {
    return {
      postId: null,
      post: null,
      liked: false,
      bookmarked: false,
      comments: [],
      commentText: '',
      submittingComment: false,
      commentPage: 0,
      commentSize: 20,
      commentTotal: 0,
      loadingComments: false,
      finishedComments: false
    }
  },
  computed: {
    canInteract() {
      return this.post && this.post.status === 'PUBLISHED'
    }
  },
  onLoad(options) {
    const postId = options && options.postId ? Number(options.postId) : null
    this.postId = Number.isFinite(postId) ? postId : null
    if (!this.postId) {
      this.$modal.msgError('帖子不存在')
      return
    }
    this.loadPost()
  },
  onReachBottom() {
    this.loadMoreComments()
  },
  methods: {
    formatTime(value) {
      if (!value) {
        return ''
      }
      const text = String(value)
      return text.replace('T', ' ').substring(0, 16)
    },
    loadPost() {
      getPost(this.postId)
        .then((res) => {
          this.post = (res && res.data) || null
          this.resetComments()
          this.loadMoreComments()
        })
        .catch(() => {
          this.$modal.msgError('加载帖子失败或无权限查看')
        })
    },
    resetComments() {
      this.comments = []
      this.commentPage = 0
      this.commentTotal = 0
      this.finishedComments = false
      this.loadingComments = false
    },
    loadMoreComments() {
      if (!this.postId || this.loadingComments || this.finishedComments) {
        return
      }
      this.loadingComments = true
      listComments(this.postId, { page: this.commentPage, size: this.commentSize })
        .then((res) => {
          const data = (res && res.data) || {}
          const items = data.items || []
          const total = data.total || 0
          this.commentTotal = total
          this.comments = this.commentPage === 0 ? items : this.comments.concat(items)
          this.commentPage += 1
          this.finishedComments = this.comments.length >= this.commentTotal
        })
        .catch(() => {
          // ignore
        })
        .finally(() => {
          this.loadingComments = false
        })
    },
    submitComment() {
      const tocUserId = Number(getTocUserId())
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常，请重新登录')
        uni.reLaunch({ url: '/pages/login' })
        return
      }
      const content = String(this.commentText || '').trim()
      if (!content) {
        this.$modal.msgError('请输入评论内容')
        return
      }
      this.submittingComment = true
      createComment(this.postId, {
        authorTocUserId: tocUserId,
        content,
        anonymousFlag: false
      })
        .then((res) => {
          const item = (res && res.data) || null
          if (item) {
            this.comments = [item].concat(this.comments)
            this.commentText = ''
            if (this.post) {
              this.post.commentCount = (this.post.commentCount || 0) + 1
            }
          }
          uni.showToast({ title: '已发送', icon: 'success' })
        })
        .catch((error) => {
          const message = error && error.message ? error.message : ''
          this.$modal.msgError(message || '评论失败')
        })
        .finally(() => {
          this.submittingComment = false
        })
    },
    toggleLike() {
      const tocUserId = Number(getTocUserId())
      if (!this.canInteract) {
        return
      }
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常，请重新登录')
        uni.reLaunch({ url: '/pages/login' })
        return
      }
      const action = this.liked ? unlikePost : likePost
      action(this.postId, tocUserId)
        .then((res) => {
          const changed = (res && res.data && res.data.changed) || false
          if (this.liked) {
            if (changed && this.post) {
              this.post.likeCount = Math.max(0, (this.post.likeCount || 0) - 1)
            }
            this.liked = false
            return
          }
          if (changed && this.post) {
            this.post.likeCount = (this.post.likeCount || 0) + 1
          }
          this.liked = true
        })
        .catch(() => {
          this.$modal.msgError('操作失败')
        })
    },
    toggleBookmark() {
      const tocUserId = Number(getTocUserId())
      if (!this.canInteract) {
        return
      }
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常，请重新登录')
        uni.reLaunch({ url: '/pages/login' })
        return
      }
      const action = this.bookmarked ? unbookmarkPost : bookmarkPost
      action(this.postId, tocUserId)
        .then(() => {
          this.bookmarked = !this.bookmarked
        })
        .catch(() => {
          this.$modal.msgError('操作失败')
        })
    },
    handleReport() {
      const tocUserId = Number(getTocUserId())
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常，请重新登录')
        uni.reLaunch({ url: '/pages/login' })
        return
      }
      const options = ['垃圾广告', '隐私泄露', '不实信息', '其他']
      const codes = ['SPAM', 'PRIVACY', 'MISINFO', 'OTHER']
      uni.showActionSheet({
        itemList: options,
        success: (res) => {
          const idx = res && res.tapIndex !== undefined ? res.tapIndex : -1
          if (idx < 0) {
            return
          }
          createReport({
            reporterTocUserId: tocUserId,
            postId: this.postId,
            reasonCode: codes[idx],
            reasonText: options[idx]
          })
            .then(() => {
              uni.showToast({ title: '已举报', icon: 'success' })
              if (this.post) {
                this.post.reportCount = (this.post.reportCount || 0) + 1
              }
            })
            .catch(() => {
              this.$modal.msgError('举报失败')
            })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding: 20rpx 20rpx 140rpx;
}

.post-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx 22rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.meta {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #999;
}

.dot {
  margin: 0 10rpx;
}

.status {
  margin-left: 14rpx;
  padding: 2rpx 10rpx;
  border-radius: 999px;
  background: rgba(255, 153, 0, 0.12);
  color: #ff9900;
  font-size: 22rpx;
}

.content {
  margin-top: 18rpx;
  font-size: 28rpx;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
}

.actions {
  margin-top: 18rpx;
  display: flex;
  gap: 12rpx;
}

.pending-tip {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #999;
}

.comment-card {
  margin-top: 18rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.comment-header {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.comment-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.comment-count {
  font-size: 24rpx;
  color: #999;
}

.empty-comment {
  padding: 30rpx 0;
  text-align: center;
  color: #999;
  font-size: 24rpx;
}

.comment-item {
  padding: 18rpx 0;
  border-bottom: 1px solid #f2f2f2;
}

.comment-item:last-child {
  border-bottom: none;
}

.c-meta {
  font-size: 22rpx;
  color: #999;
}

.c-author {
  color: #666;
}

.c-content {
  margin-top: 10rpx;
  font-size: 28rpx;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
}

.comment-editor {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
  padding: 14rpx 16rpx;
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.comment-input {
  flex: 1;
  height: 70rpx;
  background: #f5f6f7;
  border-radius: 14rpx;
  padding: 0 16rpx;
  font-size: 26rpx;
}
</style>

