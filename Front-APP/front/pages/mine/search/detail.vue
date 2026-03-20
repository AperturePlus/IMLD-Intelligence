<template>
  <view class="user-detail-page">
    <view v-if="user">
      <!-- 用户卡片展示 -->
      <view class="news-card">
        <image class="news-image" :src="getImagePath(user.imageUrl)" mode="aspectFill" />

        <view class="news-content">
          <text class="news-title">{{ user.nickname }}</text>
          <text class="news-tags">
            <text class="tag" v-for="(tag, tagIndex) in user.tags" :key="tagIndex">{{ tag }}</text>
          </text>
          <text class="news-author">年龄：{{ user.age }}</text>
          <text class="news-author">性别：{{ user.sex }}</text>
          <text class="news-author">学校：{{ user.school }}</text>
          <text class="news-desc">自我简介：{{ user.content }}</text>
        </view>
      </view>
      
      <!-- 关注按钮 -->
      <view class="follow-btn-container">
        <button class="follow-btn" @click="handleFollow">
          {{ isFollowing ? '已关注' : '关注' }}
        </button>
      </view>
    </view>

    <view v-else>
      <text>加载中...</text>
    </view>
  </view>
</template>

<script>
import config from '@/config'
import { getUserProfile } from '@/api/system/user' // 确保这个方法支持按 nickname 查询

export default {
  data() {
    return {
      user: null,
	  follower:'',
	  followee:'',
      isFollowing: false // 初始状态为未关注
    }
  },
  onLoad(options) {
    if (options.nickname) {
      console.log("nickname",options.nickname)
      this.loadUser(options.nickname)
      console.log("执行了加载程序")
      // 这里可以添加检查是否已关注的逻辑
      // this.checkFollowStatus(options.nickname)
    }
  },
  methods: {
    getImagePath(filename) {
      return config.baseUrl + '/userinfo/' + filename
    },
	
	
	getUser() {
	    getUserProfile().then(response => {
	      this.form.nickname = response.data.nickname;
	    });
	  },
	
	
	
    async loadUser(nickname) {
      try {
        const res = await new Promise((resolve, reject) => {
          uni.request({
            url: config.baseUrl + '/user/one',
            method: 'GET',
            header: {
              'Authorization': 'Bearer ' + uni.getStorageSync('token') // 如果有 token，需要带上
            },
            data: {
              param: nickname // ✅ 关键点：参数名为 param，值为 nickname
            },
            success: (res) => resolve(res),
            fail: (err) => reject(err)
          })
        })
    
        if (res && res.data && res.data.code === 200) {
          const userData = res.data.data
          console.log('userData', userData)
    
          // 处理 tags 字符串为数组
          if (userData.tags && typeof userData.tags === 'string') {
            userData.tags = userData.tags.split(',').map(tag => tag.trim())
          } else {
            userData.tags = []
          }
    
          this.user = userData
        } else {
          console.error('请求成功但返回异常', res)
        }
      } catch (error) {
        console.error('获取用户失败：', error)
      }
    },
    
    // 关注/取消关注处理函数
    handleFollow() {
      if (this.isFollowing) {
        // 执行取消关注逻辑
        this.unfollowUser()
      } else {
        // 执行关注逻辑
        this.followUser()
      }
    },
    
    // 关注用户
    followUser() {
      uni.showLoading({ title: '处理中...' })
      // 这里替换为实际的关注API调用
      console.log('正在关注用户:', this.user.nickname)
      // 模拟API请求
      setTimeout(() => {
        this.isFollowing = true
        uni.hideLoading()
        uni.showToast({ title: '关注成功', icon: 'success' })
      }, 500)
    },
    
    // 取消关注
    unfollowUser() {
      uni.showModal({
        title: '提示',
        content: '确定要取消关注吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            // 这里替换为实际的取消关注API调用
            console.log('取消关注用户:', this.user.nickname)
            // 模拟API请求
            setTimeout(() => {
              this.isFollowing = false
              uni.hideLoading()
              uni.showToast({ title: '已取消关注', icon: 'success' })
            }, 500)
          }
        }
      })
    },
    
    // 检查关注状态（根据实际API实现）
    checkFollowStatus(nickname) {
      // 这里实现检查是否已关注该用户的逻辑
      // 示例：
      // api.checkFollow({nickname}).then(res => {
      //   this.isFollowing = res.data.isFollowing
      // })
    }
  }
}
</script>

<style scoped>
.user-detail-page {
  padding: 20px;
}

.news-card {
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
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

/* 关注按钮样式 */
.follow-btn-container {
  padding: 0 20px;
}

.follow-btn {
  background-color: #07C160;
  color: white;
  border-radius: 5px;
  font-size: 16px;
  height: 45px;
  line-height: 45px;
  width: 100%;
  border: none;
}

.follow-btn:active {
  background-color: #06AD56;
}

/* 已关注状态样式 */
.follow-btn[disabled] {
  background-color: #EEEEEE;
  color: #999999;
}
</style>