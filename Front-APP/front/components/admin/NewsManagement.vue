<template>
  <view class="news-management">
    <!-- 搜索框 -->
    <view class="search-bar">
      <uni-search-bar 
        placeholder="搜索资讯" 
        radius="100" 
        @confirm="handleSearch"
        cancelButton="none"
      />
    </view>
    
    <!-- 发布资讯按钮 -->
    <view class="publish-btn-container">
      <button class="publish-btn" @click="navigateToPublish">发布资讯</button>
    </view>
    
    <!-- 资讯列表 -->
    <view class="news-list">
      <view class="news-card" v-for="(item, index) in newsList" :key="item.id">
        <image class="news-image" :src="getImagePath(item.imageUrl)" mode="aspectFill"/>
        <view class="news-content">
          <text class="news-title">{{ item.title }}</text>
          <text class="news-desc">{{ item.solution }}</text>
        </view>
        <view class="news-actions">
          <button class="edit-btn" @click="editNews(item)">修改</button>
          <button class="delete-btn" @click="deleteNews(item.id, index)">删除</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
	import {
		getAdvice,deleteAdvice
	} from '@/api/system/advice'
	import config from '@/config'
export default {
  data() {
    return {
      newsList: []
    }
  },
  
  mounted() {
	console.log('开始加载资讯数据');
  	this.loadData()
  },
  
  methods: {
    handleSearch(e) {
      console.log('搜索内容:', e.value)
      // 实际项目中这里调用搜索接口
    },
	
	getImagePath(imageName) {
	  return config.baseUrl + '/advice/' + imageName;
	},
	
	
    navigateToPublish() {
      uni.navigateTo({
        url: '/pages/admin/publish'
      })
    },
	
    editNews(item) {
      uni.navigateTo({
        url: `/pages/admin/publish_edit?id=${item.id}`
      })
    },
	
    deleteNews(id, index) {
         uni.showModal({
           title: '提示',
           content: '确定要删除这条资讯吗？',
           success: (res) => {
             if (res.confirm) {
               // 调用后端接口
               deleteAdvice(id).then(() => {
                 // 删除成功后从前端列表移除
                 this.newsList.splice(index, 1)
                 uni.showToast({
                   title: '删除成功',
                   icon: 'success'
                 })
               }).catch(err => {
                 uni.showToast({
                   title: '删除失败',
                   icon: 'error'
                 })
                 console.error('删除失败：', err)
               })
             }
           }
         })
       },
	
	loadData() {
		console.log('开始加载资讯数据');
		// 获取健康资讯
		getAdvice().then(res => {
			console.log('获取到的数据:', res);
			this.newsList = res.data;
		});
	},
	
	search() {
		console.log(this.query)
		getAdvice({param:this.query}).then(res => {
			this.newsList = res.data;
		});
	}
  }
}
</script>

<style scoped>
.news-management {
  padding-bottom: 20px;
}

.search-bar {
  padding: 10px 0;
}

.publish-btn-container {
  padding: 15px 0;
}

.publish-btn {
  background-color: #409EFF;
  color: white;
  border-radius: 20px;
  height: 40px;
  line-height: 40px;
  font-size: 16px;
}

.news-list {
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
}

.news-content {
  padding: 12px;
}

.news-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 8px;
}

.news-desc {
  font-size: 14px;
  color: #666;
  display: block;
  line-height: 1.4;
}

.news-actions {
  display: flex;
  padding: 10px 12px;
  border-top: 1px solid #f5f5f5;
}

.news-actions button {
  flex: 1;
  height: 36px;
  line-height: 36px;
  font-size: 14px;
  margin: 0 5px;
  border-radius: 4px;
}

.edit-btn {
  background-color: #f0f0f0;
  color: #666;
}

.delete-btn {
  background-color: #fef0f0;
  color: #f56c6c;
}
</style>