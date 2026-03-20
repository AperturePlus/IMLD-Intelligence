<template>
  <view class="container">
    <view class="header">
      <text class="title">AI 智能营养处方</text>
      <text class="subtitle">基于您的临床指标与代谢性肾病特征，AI 已为您生成今日专属的【低蛋白、低磷钾】护肾食谱。</text>
    </view>

    <view class="content-wrapper">
      <view class="summary-card">
        <view class="summary-header">
          <view class="iconfont icon-ai"></view>
          <text class="summary-title">AI 膳食评估原则</text>
        </view>
        <view class="summary-text">
          <text class="dot">•</text> 控制植物蛋白，补充高生物价优质蛋白（如蛋清、瘦肉）。
        </view>
        <view class="summary-text">
          <text class="dot">•</text> 采用麦淀粉/藕粉替代部分主食，提供充足热量防营养不良。
        </view>
        <view class="summary-text">
          <text class="dot">•</text> 严格限制高钾（如香蕉、坚果）及高磷（如蛋黄、内脏）食物。
        </view>
      </view>

      <view class="meal-card">
        <view class="card-title">
          <view class="title-left">
            <text class="title-icon">|</text> 营养早餐
          </view>
          <text class="calories-total">约 450 kcal</text>
        </view>
        
        <view class="food-item" v-for="(item, index) in breakfast" :key="index">
          <image class="food-img" :src="item.img" mode="aspectFill"></image>
          <view class="food-info">
            <view class="food-name">{{ item.name }}</view>
            <view class="food-desc">{{ item.desc }}</view>
            <view class="tags">
              <text class="tag" v-for="(tag, tIndex) in item.tags" :key="tIndex">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="meal-card">
        <view class="card-title">
          <view class="title-left">
            <text class="title-icon">|</text> 活力午餐
          </view>
          <text class="calories-total">约 600 kcal</text>
        </view>
        
        <view class="food-item" v-for="(item, index) in lunch" :key="index">
          <image class="food-img" :src="item.img" mode="aspectFill"></image>
          <view class="food-info">
            <view class="food-name">{{ item.name }}</view>
            <view class="food-desc">{{ item.desc }}</view>
            <view class="tags">
              <text class="tag blue-tag" v-for="(tag, tIndex) in item.tags" :key="tIndex">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="meal-card">
        <view class="card-title">
          <view class="title-left">
            <text class="title-icon">|</text> 轻盈晚餐
          </view>
          <text class="calories-total">约 500 kcal</text>
        </view>
        
        <view class="food-item" v-for="(item, index) in dinner" :key="index">
          <image class="food-img" :src="item.img" mode="aspectFill"></image>
          <view class="food-info">
            <view class="food-name">{{ item.name }}</view>
            <view class="food-desc">{{ item.desc }}</view>
            <view class="tags">
              <text class="tag green-tag" v-for="(tag, tIndex) in item.tags" :key="tIndex">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="footer-tips">
        <text class="tips-text">⚠️ 医疗免责声明：本食谱由 AI 基于医学指南生成，仅供参考。具体执行请结合您的主治医师或临床营养师的当面指导。</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      // 早餐数据
      breakfast: [
        {
          name: '麦淀粉煎饼 (100g)',
          desc: '提供充足碳水化合物，且几乎不含非优质植物蛋白，减轻肾脏代谢负担。',
          img: 'https://images.unsplash.com/photo-1528207776546-365bb710ee93?w=400&q=80', // 煎饼/面饼图
          tags: ['高热量', '极低蛋白']
        },
        {
          name: '水煮水嫩蛋清 (2个)',
          desc: '弃去富含磷的蛋黄，仅保留蛋清，补充人体必需的高生物价优质蛋白。',
          img: 'https://images.unsplash.com/photo-1516684732162-798a0062be99?w=400&q=80', // 鸡蛋/蛋清图
          tags: ['优质蛋白', '无磷']
        }
      ],
      // 午餐数据
      lunch: [
        {
          name: '清蒸鲈鱼 (50g)',
          desc: '优选深水或淡水白肉鱼，控制分量，提供优质蛋白及 Omega-3 脂肪酸。',
          img: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80', // 鱼肉/健康餐图
          tags: ['优质低蛋白', '易消化']
        },
        {
          name: '水煮焯水冬瓜 (150g)',
          desc: '冬瓜利尿消肿且含钾极低。烹饪前需“开水焯烫去汤”以进一步去除钾离子。',
          img: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&q=80', // 绿色蔬菜沙拉图
          tags: ['低钾', '利尿消肿']
        },
        {
          name: '低蛋白大米饭 (1碗)',
          desc: '特制低蛋白大米，口感软糯，帮助患者在控制蛋白的同时吃饱吃好。',
          img: 'https://images.unsplash.com/photo-1536304929831-ee1ca9d44906?w=400&q=80', // 米饭图
          tags: ['主食替换']
        }
      ],
      // 晚餐数据
      dinner: [
        {
          name: '蒜蓉炒西葫芦 (150g)',
          desc: '西葫芦属于低钾瓜果类蔬菜，适合肾病患者，使用少量植物油清炒。',
          img: 'https://images.unsplash.com/photo-1547592180-85f173990554?w=400&q=80', // 蔬菜/炒菜图
          tags: ['低钾低磷', '高膳食纤维']
        },
        {
          name: '纯藕粉羹 (1碗)',
          desc: '作为晚餐主食，藕粉几乎不含蛋白质，且能提供饱腹感和热量，护肾佳品。',
          img: 'https://images.unsplash.com/photo-1582515073490-39981397c445?w=400&q=80', // 汤羹/糊状食物图
          tags: ['零蛋白', '护肾淀粉']
        }
      ]
    };
  }
};
</script>

<style scoped>
/* 全局背景 */
.container {
  min-height: 100vh;
  background-color: #f4f6f9;
  padding-bottom: 60rpx;
}

/* 头部样式（与表单页保持一致） */
.header {
  background: linear-gradient(135deg, #2b85e4 0%, #005eaa 100%);
  padding: 60rpx 40rpx 80rpx;
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
}

.header .title {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.header .subtitle {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
}

/* 内容包装器（向上偏移产生悬浮层叠效果） */
.content-wrapper {
  padding: 0 30rpx;
  margin-top: -40rpx;
}

/* AI 摘要卡片（特殊样式） */
.summary-card {
  background: linear-gradient(to right, #ffffff, #f0f7ff);
  border: 1px solid #d6eaff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(43, 133, 228, 0.08);
}

.summary-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.summary-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2b85e4;
}

.summary-text {
  font-size: 26rpx;
  color: #555555;
  line-height: 1.6;
  margin-top: 8rpx;
  display: flex;
}

.dot {
  color: #2b85e4;
  margin-right: 12rpx;
  font-weight: bold;
}

/* 菜谱卡片 */
.meal-card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

/* 卡片标题行 */
.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px dashed #eeeeee;
}

.title-left {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  display: flex;
  align-items: center;
}

.title-icon {
  color: #2b85e4;
  font-weight: 900;
  margin-right: 12rpx;
  font-size: 28rpx;
}

.calories-total {
  font-size: 26rpx;
  color: #fa3534; /* 红色醒目提示热量 */
  font-weight: bold;
  background-color: #fef0f0;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

/* 食物列表项布局 */
.food-item {
  display: flex;
  margin-bottom: 30rpx;
}

.food-item:last-child {
  margin-bottom: 0;
}

/* 食物图片 */
.food-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  background-color: #f5f5f5;
  flex-shrink: 0;
}

/* 食物信息 */
.food-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.food-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
}

.food-desc {
  font-size: 24rpx;
  color: #888888;
  line-height: 1.5;
  margin: 10rpx 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2; /* 限制两行 */
  overflow: hidden;
}

/* 标签组 */
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background-color: #fff5e6;
  color: #ff9900;
}

.blue-tag {
  background-color: #e6f3ff;
  color: #2b85e4;
}

.green-tag {
  background-color: #eafff0;
  color: #19be6b;
}

/* 底部免责声明 */
.footer-tips {
  padding: 20rpx 10rpx;
  text-align: center;
}

.tips-text {
  font-size: 22rpx;
  color: #aaaaaa;
  line-height: 1.6;
}
</style>