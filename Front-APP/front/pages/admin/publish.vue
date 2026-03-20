<template>
  <view class="container">
	  <view class="back-button" @tap="goBack">
	      <text class="back-icon">| </text>
	      <text class="back-text">返回</text>
	  </view>
    <view class="title">发布资讯</view>

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

    <!-- 单图上传 -->
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
      <button class="submit-btn" :loading="isSubmitting" @tap="submitForm">提交</button>
    </view>
  </view>
</template>

<script>
import config from '@/config'
import upload from '@/utils/upload'

export default {
  data() {
    return {
      form: {
        title: '',
        solution: '',
        imageUrl: ''
      },
      localImagePath: '', // 本地预览用
      localFilePath: '',  // 真实路径，用于上传
      isSubmitting: false
    }
  },
  methods: {
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          this.localFilePath = res.tempFilePaths[0]
          this.localImagePath = this.localFilePath
		  console.log("当前图片路径：", this.localFilePath)
        }
      })
    },
	goBack() {
	    uni.navigateBack({
	        delta: 1
	    });
	},
    removeImage() {
      this.localFilePath = ''
      this.localImagePath = ''
      this.form.imageUrl = ''
    },
    async uploadImage() {
      if (!this.localFilePath) {
        throw new Error('未选择图片')
      }
      console.log("开始上传图片，路径：", this.localFilePath)
      return new Promise((resolve, reject) => {
        uni.uploadFile({
          url: config.baseUrl + '/upload/advice', // 拼接完整后端接口
          filePath: this.localFilePath,
          name: 'file', // 后端用 @RequestParam("file") 对应的字段
          success: (res) => {
            console.log("uploadFile 返回：", res)
            try {
              const result = JSON.parse(res.data)
              if (result.code === 200) {
                console.log("文件名：", result.fileName)
                resolve(result.fileName)
              } else {
                uni.showToast({ title: result.msg || '上传失败', icon: 'none' })
                reject(result)
              }
            } catch (e) {
              console.error("JSON 解析失败：", res.data)
              reject(e)
            }
          },
          fail: (err) => {
            console.error("上传失败：", err)
            uni.showToast({ title: '图片上传失败', icon: 'none' })
            reject(err)
          }
        })
      })
    },
	
    async submitForm() {
      if (!this.form.solution || !this.form.title) {
        return uni.showToast({ title: '请填写完整信息', icon: 'none' })
      }
      if (!this.localFilePath) {
        return uni.showToast({ title: '请先选择图片', icon: 'none' })
      }

      this.isSubmitting = true

      try {
        // 上传图片并获取文件名
        const fileName = await this.uploadImage()
        this.form.imageUrl = fileName
        console.log("提交的数据：", this.form)
        // 提交表单数据
        await uni.request({
          url: config.baseUrl + '/advice/add',
          method: 'POST',
          data: this.form,
          header: { 'Content-Type': 'application/json' }
        })

        uni.showToast({ title: '发布成功', icon: 'success' })

        // 重置表单
        this.form = { title: '', solution: '', imageUrl: '' }
        this.localImagePath = ''
        this.localFilePath = ''
      } catch (err) {
        uni.showToast({ title: '发布失败', icon: 'none' })
      } finally {
        this.isSubmitting = false
      }
    }
  }
}
</script>

<style scoped>
.input {
  height: 80rpx;
  line-height: 80rpx;
  background: #ffffffcc;
  border-radius: 20rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  border: 1px solid #ccc;
}
.btn {
  background: linear-gradient(to right, #4dd0e1, #f48fb1);
  color: #fff;
  padding: 10rpx 30rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  margin-left: 20rpx;
}
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
}
.textarea {
  width: 100%;
  height: 160rpx;
  border: 2rpx solid #dcdcdc;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
  background-color: #fff;
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
  color: red;
  font-size: 36rpx;
  font-weight: bold;
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
}
</style>
