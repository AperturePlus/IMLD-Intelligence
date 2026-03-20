<template>
  <view class="container">
	  <view class="back-button" @tap="goBack">
	      <text class="back-icon">| </text>
	      <text class="back-text">返回</text>
	  </view>
    <view class="title">编辑资讯</view>

    <!-- 资讯题目 -->
    <view class="form-item">
      <text class="label">资讯题目：</text>
      <input class="input" v-model="form.title" placeholder="请输入题目" />
    </view>

    <!-- 资讯内容 -->
    <view class="form-item">
      <text class="label">资讯内容：</text>
      <textarea class="textarea" v-model="form.solution" placeholder="请输入内容"></textarea>
    </view>

    <!-- 图片上传 -->
    <view class="form-item">
      <text class="label">上传图片：</text>
      <view class="images">
        <view v-if="localImagePath">
          <image :src="localImagePath" class="uploaded-img" mode="aspectFill" />
          <text class="remove-tag" @click="removeImage">×</text>
        </view>
        <button class="btn" v-if="!localImagePath" @tap="chooseImage">选择图片</button>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-wrapper">
      <button class="submit-btn" :loading="isSubmitting" @tap="submitForm">更新资讯</button>
    </view>
  </view>
</template>

<script>
import config from '@/config'

export default {
  data() {
    return {
      form: {
        id: '', // 唯一不同点：多了ID字段
        title: '',
        solution: '',
        imageUrl: ''
      },
      localImagePath: '',
      localFilePath: '',
      isSubmitting: false
    }
  },
  onLoad(options) {
    // 只接收ID，不加载原有数据
    if (options.id) {
      this.form.id = options.id
    }
  },
  methods: {
	  chooseImage() {
	    uni.chooseImage({
	      count: 1,
	      success: (res) => {
	        this.localFilePath = res.tempFilePaths[0]
	        this.localImagePath = this.localFilePath
	      }
	    })
	  },
	  
    getImagePath(imageName) {
      return config.baseUrl + '/advice/' + imageName
    },
    
    removeImage() {
      this.localFilePath = ''
      this.localImagePath = ''
      this.form.imageUrl = ''
    },
    
    async uploadImage() {
      if (!this.localFilePath) {
        throw new Error('请选择图片')
      }
      
      return new Promise((resolve, reject) => {
        uni.uploadFile({
          url: config.baseUrl + '/upload/advice',
          filePath: this.localFilePath,
          name: 'file',
          success: (res) => {
            try {
              const result = JSON.parse(res.data)
              if (result.code === 200) {
                resolve(result.fileName)
              } else {
                uni.showToast({ title: result.msg || '上传失败', icon: 'none' })
                reject(result)
              }
            } catch (e) {
              console.error("JSON解析失败:", res.data)
              reject(e)
            }
          },
          fail: (err) => {
            console.error("上传失败:", err)
            uni.showToast({ title: '图片上传失败', icon: 'none' })
            reject(err)
          }
        })
      })
    },
	
	goBack() {
	    uni.navigateBack({
	        delta: 1
	    });
	    // 或者明确指定跳转到登录页面
	    // uni.redirectTo({
	    //     url: '/pages/login/login'
	    // });
	},
    
    async submitForm() {
      if (!this.form.title || !this.form.solution) {
        return uni.showToast({ title: '请填写完整信息', icon: 'none' })
      }
      if (!this.localFilePath) {
        return uni.showToast({ title: '请选择图片', icon: 'none' })
      }

      this.isSubmitting = true
      uni.showLoading({ title: '提交中...', mask: true })

      try {
        // 上传图片
        const fileName = await this.uploadImage()
        this.form.imageUrl = fileName

        // 提交更新请求
        const res = await uni.request({
          url: config.baseUrl + '/advice/update',
          method: 'PUT',
          data: this.form,
          header: { 'Content-Type': 'application/json' }
        })

        if (res[1].data.code === 200) {
          uni.showToast({ title: '更新成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({ title: '更新失败: ' + res[1].data.msg, icon: 'none' })
        }
      } catch (err) {
        console.error('更新失败:', err)
        uni.showToast({ title: '更新失败', icon: 'none' })
      } finally {
        this.isSubmitting = false
        uni.hideLoading()
      }
    }
  }
}
</script>

<style scoped>
/* 保持与发布页面相同的样式 */
.container {
  padding: 20rpx;
  background: linear-gradient(to bottom right, #e0f7fa, #fce4ec);
  border-radius: 20rpx;
}

.title {
  font-size: 42rpx;
  text-align: center;
  margin-bottom: 40rpx;
  color: #5a9bd6;
  font-weight: bold;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 28rpx;
  margin-bottom: 10rpx;
  color: #333;
  display: block;
}

.input {
  height: 80rpx;
  line-height: 80rpx;
  background: #ffffffcc;
  border-radius: 20rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  border: 1px solid #ccc;
  width: 100%;
  box-sizing: border-box;
}

.textarea {
  width: 100%;
  height: 160rpx;
  border: 2rpx solid #dcdcdc;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
  background-color: #fff;
  box-sizing: border-box;
}

.images {
  position: relative;
}

.uploaded-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
}

.remove-tag {
  position: absolute;
  top: 0;
  right: 0;
  background: red;
  color: white;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(50%, -50%);
}

.btn {
  background: linear-gradient(to right, #4dd0e1, #f48fb1);
  color: #fff;
  padding: 10rpx 30rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
}

.submit-wrapper {
  margin-top: 40rpx;
}

.submit-btn {
  background: linear-gradient(to right, #4dd0e1, #f48fb1);
  text-align: center;
  padding: 20rpx;
  border-radius: 32rpx;
  color: white;
  font-size: 32rpx;
  font-weight: bold;
  width: 100%;
}
</style>