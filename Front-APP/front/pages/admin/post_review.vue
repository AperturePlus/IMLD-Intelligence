<template>
  <view class="square-page">
    <view class="news-list">
      <view class="news-card" v-for="(item, index) in postlist" :key="index">
        <image class="news-image" :src="getImagePath(item.imageUrl)" mode="aspectFill" />
        <view class="news-content">
          <text class="news-title">帖子标题：{{ item.title }}</text>
          <text class="news-tags">
            <text class="tag" v-for="(tag, tagIndex) in item.tags" :key="tagIndex">{{ tag }}</text>
          </text>
          <text class="news-author">类型：{{ item.type === 'demand' ? '需求' : '提供' }}</text>
          <text class="news-author">作者：{{ item.nickname }}</text>
          <text class="news-author">内容：{{ item.content }}</text>
          <text class="news-author">交流时长：{{ item.duration }} min</text>
          <text class="news-desc">交流时间：{{ item.date }} {{ item.time }}</text>
          <text class="news-reward">| 报酬：{{ item.reward }} ￥</text>
          <text class="news-author">发布时间：{{ item.createtime }}</text>
        </view>

        <!-- 审核操作按钮 -->
        <view class="card-actions">
          <button class="btn-green" @click="reviewPost(item.id, 1)">审核通过</button>
          <button class="btn-red" @click="reviewPost(item.id, 2)">审核不通过</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import config from '@/config'
import { getPendingPosts, reviewPostById } from '@/api/system/post'

export default {
  data() {
    return {
      postlist: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getPendingPosts().then(res => {
        this.postlist = res.data.map(item => {
          item.tags = item.tags?.split(',') || []
          return item
        })
      })
    },
    getImagePath(filename) {
      return config.baseUrl + '/post/' + filename
    },
    reviewPost(id, state) {
      reviewPostById(id, state).then(() => {
        uni.showToast({ title: '操作成功', icon: 'success' })
        this.loadData() // 刷新
      }).catch(() => {
        uni.showToast({ title: '操作失败', icon: 'none' })
      })
    }
  }
}
</script>

<style scoped>
.square-page {
  padding-bottom: 20px;
}

.tabs {
  display: flex;
  justify-content: space-around;
  background-color: #f8f8f8;
  padding: 10px 0;
  border-bottom: 1px solid #ddd;
}

.tab {
  padding: 10px 20px;
  cursor: pointer;
  color: #96563e;
}

.tab.active {
  border-bottom: 2px solid #96562e;
  font-weight: bold;
}

.news-list {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.news-card {
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.news-image {
  width: 100%;
  height: 180px;
  background-color: #f2f2f2;
}

.news-content {
  padding: 12px;
  font-size: 15px;
  font-weight: bold;
  color: #000000;
}

.news-title {
  font-size: 20px;
  color: #0d9ab9;
  margin-bottom: 6px;
  font-weight: bold;
}

.news-tags {
  margin-bottom: 8px;
}

.tag {
  display: inline-block;
  background-color: #e00a4a;
  padding: 6px 10px;
  border-radius: 12px;
  margin-right: 8px;
  font-size: 12px;
  color: #ffffff;
}

.news-author {
  font-size: 13px;
  color: #777;
  margin-bottom: 6px;
  display: block;
}

.news-desc {
  font-size: 14px;
  color: #6f6f00;
  line-height: 1.5;
}
.news-reward {
  font-size: 14px;
  color: #008dd4;
  line-height: 1.5;
}

.card-actions {
  display: flex;
  justify-content: space-around;
  padding: 10px 15px 15px;
}

.btn-green {
  background-color: #4caf50;
  color: white;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  border: none;
}

.btn-red {
  background-color: #f44336;
  color: white;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  border: none;
}
.btn-yellow {
  background-color: #ffc107;
  color: white;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  border: none;
}

</style>