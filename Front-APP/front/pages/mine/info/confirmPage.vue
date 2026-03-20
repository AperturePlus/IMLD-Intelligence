<template>
  <view class="container">
    <view class="header">
      <text style="font-size: 18px;">请确认您的信息：</text>
    </view>

    <!-- 用户信息展示 -->
    <view class="info">
      <view class="info-item">
        <text>昵称：</text>
        <text>{{ user.nickname }}</text>
      </view>
      <view class="info-item">
        <text>并发症：</text>
        <text>{{ user.complications }}</text>
      </view>
      <view class="info-item">
        <text>联系电话：</text>
        <text>{{ user.phone }}</text>
      </view>
      <view class="info-item">
          <text v-if="user.sex === 0">男</text>
          <text v-else>女</text>
      </view>
      <view class="info-item">
        <text>活动强度：</text>
        <text>{{ user.activityIntensity }}</text>
      </view>
      <view class="info-item">
        <text>身高:(cm)</text>
        <text>{{ user.height }} cm</text>
      </view>
      <view class="info-item">
        <text>体重:(kg)</text>
        <text>{{ user.weight }} kg</text>
      </view>
      <view class="info-item">
        <text>透析状态：</text>
        <text>{{ user.dialysisStatus }}</text>
      </view>
      <view class="info-item">
        <text>合并症：</text>
        <text>{{ user.hebingzheng }}</text>
      </view>
      <view class="info-item">
        <text>肾病分期（GFR值）：</text>
        <text>{{ user.gfrValue }}</text>
		<text style="">【若您填写的不确定，已自动根据肌酐值计算分期】</text>
      </view>
      <view class="info-item">
        <text>血清肌酐：</text>
        <text>{{ user.serumCreatinine }}</text>
      </view>
      <view class="info-item">
        <text>年龄：</text>
        <text>{{ user.age }}</text>
      </view>
      <view class="info-item">
        <text>血压：</text>
        <text>{{ user.bloodPressure }}</text>
      </view>
      <view class="info-item">
        <text>尿酸：</text>
        <text>{{ user.uricAcid }}</text>
      </view>
      <view class="info-item">
        <text>尿蛋白：</text>
        <text>{{ user.urineProtein }}</text>
      </view>
      <view class="info-item">
        <text>血磷(umol/L)：</text>
        <text>{{ user.bloodPhosphorus }}</text>
      </view>
      <view class="info-item">
        <text>血钾(umol/L)：</text>
        <text>{{ user.bloodPotassium }}</text>
      </view>
      <view class="info-item">
        <text>血钠(umol/L)：</text>
        <text>{{ user.bloodSodium }}</text>
      </view>
      <view class="info-item">
        <text>总胆固醇：</text>
        <text>{{ user.totalCholesterol }}</text>
      </view>
      <view class="info-item">
        <text>白蛋白：</text>
        <text>{{ user.albumin }}</text>
      </view>
      <!-- 动态展示 selectedAllergies 列表 -->
      <view class="info-item">
        <text>过敏物质：</text>
       <view v-if="user.allergy.length > 0">
            <text>{{ user.allergy }}</text>
         </view>
         <view v-else>
           <text>无过敏记录</text>
         </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="buttons">
      <button class="edit-btn" @click="editInfo">重新填写</button>
      <button class="submit-btn" @click="submit">确认提交</button>
    </view>
  </view>
</template>

<script>
import { getUserProfile, updateUserProfile} from "@/api/system/user";
export default {
  data() {
    return {
      user: {}, // 用户数据
    };
  },
  onLoad(options) {
    // 接收传递过来的用户数据
    this.user = JSON.parse(decodeURIComponent(options.user));
  },
  
  methods: {
    editInfo() {
      // 返回填写页面
      uni.navigateBack();
    },
	
	submit() {
	  updateUserProfile(this.user).then(response => {
	    this.$modal.msgSuccess("修改成功");
	  });
	     },
    },
};
</script>

<style scoped>
.container {
  padding: 20rpx;
}

.header {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.info {
  margin-bottom: 40rpx;
}

.info-item {
  display: flex;
  flex-direction: column;
  padding: 10rpx 0;
  border-bottom: 1px solid #e0e0e0;
}

.info-item text {
  margin-bottom: 10rpx;
}

.buttons {
  display: flex;
  justify-content: space-between;
}

.edit-btn,
.submit-btn {
  width: 45%;
  padding: 20rpx;
  font-size: 28rpx;
  text-align: center;
  background-color: #00aa00;
  color: #fff;
  border-radius: 10rpx;
}

.edit-btn {
  background-color: #c8c8c8;
}
</style>
