<template>
  <view class="container">
    <uni-list>
      <uni-list-item showExtraIcon="true" :extraIcon="{type: 'person-filled'}" title="昵称" :rightText="this.nickname" />
      <uni-list-item showExtraIcon="true" :extraIcon="{type: 'phone-filled'}" title="手机号码" :rightText="this.phone" />
    </uni-list>
  </view>
</template>

<script>
  import { getUserProfile } from "@/api/system/user"

  export default {
    data() {
      return {
        imageUrl:'',
		nickname:'',
		phone:''
     }
    },
	
    onLoad() {
      this.getUser()
    },
	
    methods: {
      getUser() {
        getUserProfile().then(response => {
          const user = response.data
          this.phone = data.phone
          this.nickname = data.nickname
        })
      }
    }
  }
</script>

<style lang="scss">
  page {
    background-color: #ffffff;
  }
</style>
