<template>
  <view class="container">
    <!-- 显示聊天消息 -->
    <view class="cu-chat">
      <view v-for="(message, index) in messages" :key="index">
        <!-- 非用户消息 -->
        <view v-if="message.role !== 'user'" class="cu-item" style="padding-bottom: 5rpx;">
          <view class="cu-avatar radius" style="background-image: url(/static/images/image/早餐3.png)"></view>
          <view class="main">
            <view class="content shadow">
              <text>{{ message.content }}</text>
            </view>
          </view>
        </view>
        <!-- 用户消息 -->
        <view v-else class="cu-item self" style="padding-bottom: 5rpx;">
          <view class="main">
            <view class="content shadow bg-green">
              <text>{{ message.content }}</text>
            </view>
          </view>
          <view class="cu-avatar radius" style="background-image: url(/static/images/image/早餐2.png)"></view>
        </view>
      </view>
    </view>
    
    <!-- 输入区域 -->
    <view class="cu-bar foot input" :style="{ 'padding-bottom': InputBottom + 'px' }">
      <view class="action">
        <text class="cuIcon-sound text-grey"></text>
      </view>
      <input class="solid-bottom" :adjust-position="false" v-model="inputMessage" maxlength="300" cursor-spacing="10" @focus="InputFocus" @blur="InputBlur"></input>
      <view class="action">
        <text class="cuIcon-emojifill text-grey"></text>
      </view>
      <button class="cu-btn bg-green shadow" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script>
  export default {
    data() {
      return {
        InputBottom: 0,
        messages: [],
        inputMessage: ''
      };
    },
    methods: {
      InputFocus(e) {
        this.InputBottom = e.detail.height;
      },
      InputBlur(e) {
        this.InputBottom = 0;
      },
      sendMessage() {
        const userMessage = {
          content: this.inputMessage,
          role: 'user'
        };
        this.messages.push(userMessage);

        // 清空输入框
        this.inputMessage = '';

        // 添加“回复生成中”消息到消息列表中
        const loadingMessage = {
          content: '回复生成中...',
          role: 'system'
        };
        this.messages.push(loadingMessage);

        // 构造要发送的 JSON 数据，确保 messages 包含有效数据
        const jsonData = {
          model: "qwen-plus", // 可以根据需求设置模型
          messages: this.messages.map(message => ({
            role: message.role,
            content: message.content
          })),
        };

        // 发送 POST 请求
        uni.request({
          url: 'http://localhost:9090/api/chat',
          method: 'POST',
          data: jsonData,
          header: {
            'content-type': 'application/json'
          },
          success: (res) => {
            if (res.data && Array.isArray(res.data)) {
              res.data.forEach((item) => {
                // 只将角色为助手的回答添加到消息列表中
                if (item.role === 'assistant') {
                  // 替换“回复生成中”消息为真正的助手回复
                  const index = this.messages.findIndex(message => message.content === '回复生成中...');
                  if (index !== -1) {
                    this.messages[index].content = item.content;
                  } else {
                    this.messages.push(item);
                  }
                }
              });
            }
          },
          fail: (error) => {
            console.error('Error:', error);
          }
        });
      }
    }
  }
</script>

<style>
  page {
    background-image: linear-gradient(to top, #f3e7e9 0%, #e3eeff 99%, #e3eeff 100%);
  }
  .page {
    padding-bottom: 100upx;
  }
  .container {
    display: flex;
    flex-direction: column;
    height: 90vh; /* 设置容器高度为整个视口高度 */
  }
  .system {
    animation: fadeIn 0.5s ease forwards;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>