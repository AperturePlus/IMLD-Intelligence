<template>
  <scroll-view class="container" scroll-y>
    <!-- 用户基本信息区 -->
    <view class="profile-section">
      <image class="avatar" :src="user.avatarUrl" />
      <view class="info">
        <view class="name">
          {{ user.nickname }}
          <text class="highlight">{{ user.alias }}</text>
        </view>
        <view class="details">{{ user.age }}岁 {{ user.gender }}</view>
        <view class="stars">
          <text v-for="i in 5" :key="i" class="star">★</text>
        </view>
      </view>
    </view>

    <!-- 个性标签 -->
    <view class="tags">
      <view v-for="tag in user.tags" :key="tag" class="tag">{{ tag }}</view>
    </view>

    <!-- 简介部分 -->
    <view class="intro">
      <view class="title">咨询报酬：</view>
      <text class="price">{{ user.price }}</text>
      <view class="title">自我经历：</view>
      <view class="desc" v-for="(para, index) in user.description" :key="index">{{ para }}</view>
    </view>

    <!-- 评价部分 -->
    <view class="comment-section">
      <view class="comment-title">评价</view>
      <view class="comment" v-for="(comment, index) in comments" :key="index">
        <image class="comment-avatar" :src="comment.avatarUrl" />
        <view class="comment-content">
          <view class="comment-name">{{ comment.name }}</view>
          <view class="comment-text">{{ comment.content }}</view>
        </view>
        <text class="heart">💚</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn green">预约</button>
      <button class="btn green">沟通</button>
      <button class="btn red">举报</button>
    </view>
  </scroll-view>
</template>

<script>
export default {
  data() {
    return {
      user: {
        avatarUrl: '/static/avatar.png',
        nickname: '爱笑的',
        alias: 'Cheems',
        age: 21,
        gender: '女',
        tags: ['婚恋', '计算机', '四川大学'],
        price: '40元/小时',
        description: [
          '考研进四川大学黄彦辉老师的研究生',
          '有过一段因为性格不合而分手的恋爱，并花了很长时间走不出来',
          '比较自卑，有从自卑到自信的经历'
        ]
      },
      comments: [
        { name: '爱笑的Cheems', content: 'abcdefgadsaadcaefwg', avatarUrl: '/static/avatar.png' },
        { name: '爱笑的Cheems', content: 'abcdefgadsaadcaefwg', avatarUrl: '/static/avatar.png' },
        { name: '爱笑的Cheems', content: 'abcdefgadsaadcaefwg', avatarUrl: '/static/avatar.png' }
      ]
    };
  }
}
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
}

.profile-section {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background-color: #eee;
  margin-right: 20rpx;
}

.info .name {
  font-size: 36rpx;
  font-weight: bold;
}

.highlight {
  font-weight: bold;
  color: #000;
}

.details {
  font-size: 26rpx;
  color: #666;
  margin: 10rpx 0;
}

.stars {
  color: #4a90e2;
  font-size: 28rpx;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.tag {
  background: #4a90e2;
  color: white;
  padding: 10rpx 20rpx;
  border-radius: 10rpx;
  font-size: 26rpx;
}

.intro {
  background: #fff;
  padding: 20rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.title {
  font-weight: bold;
  margin-top: 20rpx;
}

.price {
  font-size: 32rpx;
  font-weight: bold;
  color: #000;
  margin-top: 10rpx;
}

.desc {
  margin-top: 10rpx;
  font-size: 28rpx;
  color: #333;
}

.comment-section {
  background: #fff;
  padding: 20rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.comment-title {
  font-weight: bold;
  font-size: 32rpx;
  margin-bottom: 20rpx;
}

.comment {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.comment-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 30rpx;
  margin-right: 20rpx;
  background: #ddd;
}

.comment-content {
  flex: 1;
}

.comment-name {
  font-weight: bold;
}

.comment-text {
  font-size: 26rpx;
  color: #444;
}

.heart {
  font-size: 40rpx;
  color: green;
  margin-left: 20rpx;
}

.action-buttons {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
}

.btn {
  flex: 1;
  margin: 0 10rpx;
  padding: 20rpx 0;
  border-radius: 10rpx;
  font-size: 30rpx;
}

.green {
  background-color: #3eb575;
  color: white;
}

.red {
  background-color: #ff3b30;
  color: white;
}
</style>
