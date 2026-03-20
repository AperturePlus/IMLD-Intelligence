<template>
  <view class="edit-container">
    <view class="form-item">
      <text>姓名：</text>
      <input v-model="name" placeholder="请输入姓名" />
    </view>
    <view class="form-item">
      <text>手机号：</text>
      <input v-model="phone" placeholder="请输入手机号" />
    </view>

    <button @click="save">保存</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      name: '',
      phone: ''
    }
  },
  onLoad(options) {
    // 从路由参数接收数据，初始化
    this.name = options.name || ''
    this.phone = options.phone || ''
  },
  methods: {
    save() {
      if (!this.name.trim() || !this.phone.trim()) {
        uni.showToast({ title: '请完整填写信息', icon: 'none' });
        return;
      }
    
      const pages = getCurrentPages();
      const prevPage = pages[pages.length - 2];
      
      if (prevPage) {
        if (!prevPage.$data.userInfo) {
          prevPage.$data.userInfo = {};
        }
        prevPage.$data.userInfo.name = this.name;
        prevPage.$data.userInfo.phone = this.phone;
    
        if (prevPage.$forceUpdate) {
          prevPage.$forceUpdate();
        }
      }
      
      uni.showToast({ title: '保存成功' });
      uni.navigateBack();
    }


  }
}
</script>

<style scoped>
.edit-container {
  padding: 20px;
}
.form-item {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
input {
  flex: 1;
  border: 1px solid #ccc;
  padding: 6px 10px;
  border-radius: 4px;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #2979ff;
  color: white;
  border: none;
  border-radius: 4px;
}
</style>
