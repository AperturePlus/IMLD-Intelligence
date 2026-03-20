<template>
  <view class="square-page">
    <!-- 搜索框区域 -->
    <view class="search-bar">
      <view class="search-input-container">
        <input v-model="searchKeyword" class="search-input" placeholder="请输入昵称关键词" />
      </view>
      <button class="search-button" @tap="searchUser">搜索</button>
    </view>

    <!-- 内容卡片区域 -->
    <view class="news-list">
      <view class="news-card" v-for="(item, index) in filteredUserList" :key="index">
        <image class="news-image" :src="getImagePath(item.imageUrl)" mode="aspectFill" />

        <view class="news-content">
          <text class="news-title">{{ item.nickname }}</text>
          <text class="news-tags">
            <text class="tag" v-for="(tag, tagIndex) in item.tags" :key="tagIndex">{{ tag }}</text>
          </text>
          <text class="news-author">年龄：{{ item.age }}</text>
          <text class="news-author">性别：{{ item.sex }}</text>
          <text class="news-author">学校：{{ item.school }}</text>
          <text class="news-desc">自我简介：{{ item.content }}</text>
        </view>

        <!-- 底部按钮区域 -->
        <view class="card-buttons">
          <button class="btn btn-view" @tap="handleView(item)">查看</button>
          <button class="btn btn-report" @tap="handleReport(item)">举报</button>
          <button class="btn btn-evaluate" @tap="handleEvaluate(item)">评价</button>
          <button 
            class="btn btn-follow" 
            @tap="toggleFollow(item)"
            :class="{ disabled: item.nickname === followship.follower }"
            :disabled="item.nickname === followship.follower"
          >
            {{ isFollowing(item) ? '已关注' : '关注' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import config from '@/config'
import { getUser } from '@/api/system/user'
import { getUserProfile } from '@/api/system/user'
import { AddFollowship, GetFollowships, DeleteFollowship } from '@/api/system/follow'


export default {
  data() {
    return {
      userlist: [],
      searchKeyword: '',
      filteredUserList: [],
      followship: {
        follower: '',
        follower_image_url: '',
        followee: '',
        followee_image_url: '',
      },
      followingList: [] // 已关注的用户列表
    }
  },
 mounted() {
   this.getUser().then(() => {
     this.loadData()
     this.loadFollowingList()
   })
 },
  methods: {
    async loadData() {
      try {
        const res = await getUser()
        const processed = res.data.map(item => {
          if (item.tags && typeof item.tags === 'string') {
            item.tags = item.tags.split(',').map(tag => tag.trim())
          } else {
            item.tags = []
          }
          return item
        })
        this.userlist = processed
        this.filteredUserList = processed
      } catch (error) {
        console.error('加载用户数据失败:', error)
      }
    },

    async getUser() {
      try {
        const response = await getUserProfile()
        this.followship.follower = response.data.nickname
        this.followship.follower_image_url = response.data.imageUrl
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },

    async loadFollowingList() {
      try {
        await this.getUser() // 确保有 follower 信息
        const response = await GetFollowships({ follower: this.followship.follower })
        if (response.code === 200) {
          this.followingList = response.data.map(item => item.followee)
        }
      } catch (error) {
        console.error('加载关注列表失败:', error)
      }
    },

    isFollowing(user) {
      return this.followingList.includes(user.nickname)
    },

    getImagePath(filename) {
      return config.baseUrl + '/userinfo/' + filename
    },
    
    handleView(item) {
      const nickname = encodeURIComponent(item.nickname)
      uni.navigateTo({
        url: `/pages/mine/search/detail?nickname=${nickname}`
      })
    },
    
    handleReport(item) {
      const nickname = encodeURIComponent(item.nickname)
      uni.navigateTo({
        url: `/pages/mine/search/report?nickname=${nickname}`
      })
    },
    
    handleEvaluate(item) {
      const nickname = encodeURIComponent(item.nickname)
      uni.navigateTo({
        url: `/pages/mine/search/evaluate?nickname=${nickname}`
      })
    },
    
   async toggleFollow(item) {
     try {
       const userInfoRes = await getUserProfile()
       const follower = userInfoRes.data.nickname
       const follower_image_url = userInfoRes.data.imageUrl
   
       if (item.nickname === follower) {
         uni.showToast({
           title: '不能关注自己',
           icon: 'none'
         })
         return
       }
   
       if (this.isFollowing(item)) {
         // 如果已关注，执行取消关注逻辑
         const res = await DeleteFollowship({
           follower,
           followee: item.nickname
         })
         if (res.code === 200) {
           uni.showToast({ title: '取消关注成功', icon: 'success' })
           // 移除本地关注列表
           this.followingList = this.followingList.filter(name => name !== item.nickname)
         } else {
           uni.showToast({ title: res.msg || '取消失败', icon: 'none' })
         }
       } else {
         // 未关注，则执行关注逻辑
         const followData = {
           follower,
           follower_image_url,
           followee: item.nickname,
           followee_image_url: item.imageUrl
         }
   
         const response = await AddFollowship(followData)
   
         if (response.code === 200) {
           uni.showToast({ title: '关注成功', icon: 'success' })
           this.followingList.push(item.nickname)
         } else {
           uni.showToast({ title: response.msg || '操作失败', icon: 'none' })
         }
       }
     } catch (error) {
       console.error('关注/取消失败:', error)
       uni.showToast({
         title: '操作失败',
         icon: 'none'
       })
     }
   },


    
    searchUser() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      if (!keyword) {
        this.filteredUserList = this.userlist
      } else {
        this.filteredUserList = this.userlist.filter(user =>
          user.nickname && user.nickname.toLowerCase().includes(keyword)
        )
      }
    }
  }
}
</script>
<style scoped>
.square-page {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

/* 搜索栏样式 */
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.search-input-container {
  flex: 1;
  margin-right: 20rpx;
}

.search-input {
  background-color: #fff;
  border-radius: 10rpx;
  padding: 20rpx;
  font-size: 28rpx;
  border: 1rpx solid #ccc;
}

/* 搜索按钮 */
.search-button {
  background-color: #007aff;
  color: white;
  padding: 20rpx 30rpx;
  border-radius: 10rpx;
  font-size: 28rpx;
}

/* 用户卡片列表区域 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.news-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

/* 用户头像 */
.news-image {
  width: 100%;
  height: 300rpx;
  border-radius: 12rpx;
  object-fit: cover;
  margin-bottom: 20rpx;
}

/* 文字信息区域 */
.news-content {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.news-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.news-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.tag {
  font-size: 24rpx;
  background-color: #e0f3ff;
  color: #007aff;
  padding: 6rpx 12rpx;
  border-radius: 20rpx;
}

.news-author {
  font-size: 26rpx;
  color: #666;
}

.news-desc {
  font-size: 26rpx;
  color: #444;
  line-height: 1.6;
}

/* 按钮区域 */
.card-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-top: 10rpx;
}

.btn {
  flex: 1;
  padding: 16rpx;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: white;
  text-align: center;
}

.btn-view {
  background-color: #4cd964;
}

.btn-report {
  background-color: #ff3b30;
}

.btn-evaluate {
  background-color: #ff9500;
}

.btn-follow {
  background-color: #007aff;
}

.disabled {
  background-color: #cccccc !important;
}
</style>
