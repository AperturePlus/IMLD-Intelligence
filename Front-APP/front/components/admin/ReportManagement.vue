<template>
  <view class="report-management">
    <!-- 内部分类标签 -->
    <view class="report-tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'post' }"
        @click="currentTab = 'post'"
      >帖子举报</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'comment' }"
        @click="currentTab = 'comment'"
      >评价举报</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'user' }"
        @click="currentTab = 'user'"
      >用户举报</view>
    </view>

    <!-- 举报列表 -->
    <view class="report-list">
      <template v-if="currentTab === 'post'">
        <report-card 
          v-for="item in postReports" 
          :key="item.id" 
          :item="item" 
          type="post" 
          @onAction="handleReportAction"
        />
      </template>
      <template v-else-if="currentTab === 'comment'">
        <report-card 
          v-for="item in commentReports" 
          :key="item.id" 
          :item="item" 
          type="comment" 
          @onAction="handleReportAction"
        />
      </template>
      <template v-else-if="currentTab === 'user'">
        <report-card 
          v-for="item in userReports" 
          :key="item.id" 
          :item="item" 
          type="user" 
          @onAction="handleReportAction"
        />
      </template>
      <view v-if="getCurrentList().length === 0" class="no-data">暂无举报</view>
    </view>
  </view>
</template>

<script>
import config from '@/config'
import ReportCard from './ReportCard.vue'

export default {
  components: { ReportCard },
  data() {
    return {
      currentTab: 'post',
      allReports: []
    }
  },
  computed: {
    postReports() {
      return this.allReports.filter(r => r.type === 'post_report')
    },
    commentReports() {
      return this.allReports.filter(r => r.type === 'evaluate_report')
    },
    userReports() {
      return this.allReports.filter(r => r.type === 'user_report')
    }
  },
  mounted() {
    this.fetchReports()
  },
  methods: {
    getCurrentList() {
      return this.currentTab === 'post' ? this.postReports :
             this.currentTab === 'comment' ? this.commentReports :
             this.userReports
    },
    async fetchReports() {
      try {
        const res = await uni.request({
          url: config.baseUrl + '/report/all_pending',
          method: 'GET'
        })
        if (res[1].statusCode === 200 && res[1].data.code === 200) {
          this.allReports = res[1].data.data
        } else {
          uni.showToast({ title: '获取失败', icon: 'error' })
        }
      } catch (err) {
        uni.showToast({ title: '网络错误', icon: 'error' })
      }
    },
    async handleReportAction({ id, action }) {
      const newState = action === 'process' ? '1' : '2'
      const confirmText = action === 'process' ? '确定处理此举报？' : '确定忽略此举报？'

      uni.showModal({
        title: '确认操作',
        content: confirmText,
        success: async (res) => {
          if (res.confirm) {
            const r = await uni.request({
              url: config.baseUrl + '/report/update_state',
              method: 'POST',
              data: { id, state: newState },
              header: { 'Content-Type': 'application/json' }
            })
            if (r[1].data.code === 200) {
              uni.showToast({ title: '操作成功', icon: 'success' })
              this.fetchReports()
            } else {
              uni.showToast({ title: '操作失败', icon: 'error' })
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.report-management {
  padding: 20rpx;
}

.report-tabs {
  display: flex;
  background-color: #fff;
  margin-bottom: 20rpx;
  border-radius: 8rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 28rpx;
  color: #666;
}

.tab-item.active {
  color: #409EFF;
  font-weight: bold;
  border-bottom: 4rpx solid #409EFF;
}

.no-data {
  text-align: center;
  color: #888;
  margin-top: 100rpx;
}
</style>
