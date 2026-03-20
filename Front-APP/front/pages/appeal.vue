<template>
    <view class="normal-login-container">
        <view class="logo-content align-center justify-center flex">
            <!-- 修改返回按钮，添加"返回"文字 -->
			 <!-- 返回按钮 -->
			 <view class="back-button" @click="goBack">
			    <text class="back-icon">| </text>
			    <text class="back-text">返回</text>
			</view>
            <text class="title" style="margin-left: auto; margin-right: auto;">账号申诉</text>
            <!-- 添加一个空的view保持标题居中 -->
        </view>

        <view class="appeal-content">
            <text class="appeal-text">如果想对账号封禁进行申诉，请联系我们</text>
            <text class="contact-text">联系电话：028-12345678</text>
            <text class="contact-text">联系邮箱：yanling@gmail.com</text>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            // 无需数据
        }
    },
    methods: {
        goBack() {
            uni.navigateBack({
                delta: 1
            });
            // 或者明确指定跳转到登录页面
            // uni.redirectTo({
            //     url: '/pages/login/login'
            // });
        }
    }
}
</script>

<style lang="scss">
page {
    background-image: linear-gradient(to top, #f3cbee 0%, #ace0f9 100%);
  }

.normal-login-container {
    width: 100%;

    .logo-content {
        width: 100%;
        font-size: 21px;
        text-align: center;
        padding-top: 15%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding-left: 20rpx;
        padding-right: 20rpx;

        .title {
            font-weight: bold;
        }

        .back-button {
            display: flex;
            align-items: center;
            cursor: pointer;

            .back-icon {
                font-size: 24px;
                margin-right: 6rpx;
                color: #333;
            }

            .back-text {
                font-size: 16px;
                color: #333;
            }
        }
    }

    .appeal-content {
        text-align: center;
        margin: 20px auto;
        margin-top: 15%;
        width: 80%;

        .appeal-text {
            font-size: 16px;
            margin-bottom: 15px;
            color: #333;
            display: block;
        }

        .contact-text {
            font-size: 14px;
            margin-bottom: 10px;
            color: #666;
            display: block;
        }
    }
}
</style>
