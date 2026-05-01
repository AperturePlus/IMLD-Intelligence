<template>
  <view class="community-home">
    <view class="cu-bar search bg-white">
      <view class="search-form round">
        <text class="cuIcon-search"></text>
        <input
          type="text"
          placeholder="搜索帖子关键词"
          v-model="keyword"
          confirm-type="search"
          @confirm="handleSearch"
        ></input>
      </view>
      <view class="action">
        <button class="cu-btn bg-gradual-green shadow-blur round" @click="handleSearch">搜索</button>
      </view>
    </view>

    <scroll-view scroll-x class="board-scroll bg-white" v-if="boards.length > 0">
      <view class="board-tabs">
        <view
          class="board-tab"
          v-for="board in boards"
          :key="board.id"
          :class="board.id === selectedBoardId ? 'active' : ''"
          @tap="selectBoard(board.id)"
        >
          {{ board.boardName }}
        </view>
      </view>
    </scroll-view>

    <view class="entry-card">
      <view class="entry-item" @tap="goToArticles">
        <view class="entry-title">科普文章</view>
        <view class="entry-subtitle">了解 IMLD</view>
      </view>
      <view class="entry-item" @tap="goToAssessment">
        <view class="entry-title">自测</view>
        <view class="entry-subtitle">记录与评估</view>
      </view>
    </view>

    <view v-if="boards.length === 0" class="empty-tip">
      <text>暂无板块，请联系管理员创建。</text>
    </view>

    <view v-else>
      <view
        class="cu-card article no-card"
        v-for="post in posts"
        :key="post.id"
        @tap="goToPostDetail(post.id)"
      >
        <view class="cu-item shadow">
          <view class="title">
            <view class="text-cut">{{ post.title }}</view>
          </view>
          <view class="content" style="margin-top: 6px;">
            <view class="desc">
              <view class="text-content">{{ post.contentExcerpt || '' }}</view>
            </view>
          </view>
          <view class="post-meta flex justify-between">
            <text class="meta-left">{{ post.authorDisplayName || '用户' }} · {{ formatTime(post.lastActivityAt || post.createdAt) }}</text>
            <text class="meta-right">赞 {{ post.likeCount || 0 }} · 评论 {{ post.commentCount || 0 }}</text>
          </view>
        </view>
      </view>

      <view class="load-more text-center">
        <text v-if="loading">加载中...</text>
        <text v-else-if="finished">没有更多了</text>
      </view>
    </view>

    <view class="fab" @tap="goToCreatePost" v-if="boards.length > 0">
      <text class="cuIcon-add"></text>
    </view>
  </view>
</template>

<script>
import { listBoards, listPosts } from '@/api/community'

export default {
  data() {
    return {
      keyword: '',
      boards: [],
      selectedBoardId: null,
      posts: [],
      page: 0,
      size: 20,
      total: 0,
      loading: false,
      finished: false
    }
  },
  onLoad() {
    this.loadBoards()
  },
  onPullDownRefresh() {
    this.handleRefresh()
  },
  onReachBottom() {
    this.loadMore()
  },
  methods: {
    formatTime(value) {
      if (!value) {
        return ''
      }
      const text = String(value)
      return text.replace('T', ' ').substring(0, 16)
    },
    handleRefresh() {
      this.page = 0
      this.total = 0
      this.posts = []
      this.finished = false
      this.loading = false
      this.loadBoards()
      uni.stopPullDownRefresh()
    },
    handleSearch() {
      this.page = 0
      this.total = 0
      this.posts = []
      this.finished = false
      this.loadMore()
    },
    loadBoards() {
      listBoards()
        .then((res) => {
          this.boards = (res && res.data) || []
          if (this.boards.length > 0) {
            if (!this.selectedBoardId) {
              this.selectedBoardId = this.boards[0].id
            }
            this.handleSearch()
          }
        })
        .catch(() => {
          this.$modal.msgError('加载板块失败')
        })
    },
    selectBoard(boardId) {
      if (this.selectedBoardId === boardId) {
        return
      }
      this.selectedBoardId = boardId
      this.handleSearch()
    },
    loadMore() {
      if (this.loading || this.finished || !this.selectedBoardId) {
        return
      }
      this.loading = true
      const keyword = String(this.keyword || '').trim()
      listPosts({
        boardId: this.selectedBoardId,
        keyword: keyword || undefined,
        page: this.page,
        size: this.size
      })
        .then((res) => {
          const data = (res && res.data) || {}
          const items = data.items || []
          const total = data.total || 0
          this.total = total
          this.posts = this.page === 0 ? items : this.posts.concat(items)
          this.page += 1
          this.finished = this.posts.length >= this.total
        })
        .catch(() => {
          this.$modal.msgError('加载帖子失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    goToCreatePost() {
      if (!this.selectedBoardId) {
        this.$modal.msgError('暂无可用板块')
        return
      }
      uni.navigateTo({
        url: `/pages/community/post-create?boardId=${this.selectedBoardId}`
      })
    },
    goToPostDetail(postId) {
      uni.navigateTo({
        url: `/pages/community/post-detail?postId=${postId}`
      })
    },
    goToArticles() {
      uni.navigateTo({
        url: '/pages/articles/all'
      })
    },
    goToAssessment() {
      uni.switchTab({
        url: '/pages/assessment-result'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.community-home {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding-bottom: 120rpx;
}

.board-scroll {
  white-space: nowrap;
  padding: 10rpx 20rpx;
}

.board-tabs {
  display: inline-flex;
  gap: 10rpx;
}

.board-tab {
  padding: 10rpx 20rpx;
  border-radius: 999px;
  background: #f3f4f6;
  color: #333;
  font-size: 26rpx;
}

.board-tab.active {
  background: rgba(43, 133, 228, 0.12);
  color: #2b85e4;
  font-weight: bold;
}

.entry-card {
  margin: 16rpx 20rpx;
  display: flex;
  gap: 12rpx;
}

.entry-item {
  flex: 1;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 22rpx 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.entry-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.entry-subtitle {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #999;
}

.empty-tip {
  padding: 40rpx 20rpx;
  text-align: center;
  color: #999;
}

.post-meta {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #999;
}

.fab {
  position: fixed;
  right: 28rpx;
  bottom: 160rpx;
  width: 96rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background: #2b85e4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  box-shadow: 0 10rpx 24rpx rgba(43, 133, 228, 0.35);
}
</style>

