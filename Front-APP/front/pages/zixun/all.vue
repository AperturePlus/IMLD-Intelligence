<template>
  <view>
    <view class="cu-bar bg-white " :style="[{top:CustomBar + 'px'}]">
      <view class="search-form round">
        <text class="cuIcon-search"></text>
        <input type="text" placeholder="输入搜索的关键词" confirm-type="search" v-model="param"/>
      </view>
      <view class="action">
        <button class="cu-btn bg-gradual-green shadow-blur round" @click="search">搜索</button>
      </view>
    </view>
	
    <view class="zixun" v-for="item in zixunlist"
          style="background-color: #ffffff; width: 90%; margin: 10px auto; display: flex; justify-content: space-between; align-items: center; border-radius: 20px;">
      <view class="left1" style="width: 100%; padding: 10px 5%;">
        <div style="padding: 2px;">
          <view style="font-size: 16px; font-weight: bold;">{{ item.title }}</view>
          <view style="font-size: 12px;">{{ item.solution }}</view>
        </div>
      </view>
    </view>


  </view>

</template>

<style>
.button-container {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.button-wrapper {
  flex: 1;
  margin-right: 10px;
}

.rounded-button {
  background-color: rgba(229, 229, 229);
  border-radius: 10px;
  overflow: hidden;
  display: flex;
  align-items: center;
}

.navigateToQuestion,
.navicateToRecommend {
  width: 100%;
  background-color: rgba(229, 229, 229);
  padding: 10px;
  border-radius: 10px;
  display: flex;
}

.rounded-container {
  background-color: #ffffff;
  border-radius: 10px;
  overflow: hidden;
  display: flex;
}

.container {
  padding: 30px;
}

.icon {
  width: 20px;
  height: 20px;
  vertical-align: middle;
}

.rounded-input {
  width: 100%;
  height: 30px;
  flex: 1;
  padding: 5px;
  font-size: 14px;
  border: 1px solid transparent;
  box-sizing: border-box;
}

.navigateToQuestion,
.navicateToRecommend {
  display: flex;
  align-items: center;
}

.icon1 {
  width: 40px;
  height: 40px;
  border-radius: 10px;
}

.right-column {
  flex: 1;
}

text {
  display: block;
  margin-bottom: 5px;
}
</style>

<script>
import {
  listAdvice
} from "@/api/system/advice.js"

export default {
  data() {
    return {
      inputText: "了解感兴趣的健康咨询",
      zixunlist: [],
      param: "",
    };
  },
  onLoad() {
    this.search();
  },
  methods: {
    navigateToQuestion() {
      uni.switchTab({
        url: "/pages/question/index",
      });
    },
    navigateToRecommend() {
      uni.switchTab({
        url: "/pages/recommend/index",
      });
    },
    goToAllPage() {
      uni.navigateTo({
        url: "/pages/zixun/all",
      });
    },
    search() {
      listAdvice({
        param: this.param,
        page: 1,
        size: 10
      }).then((res) => {
        this.zixunlist = res.data.records
      })
    },
  },
};
</script>