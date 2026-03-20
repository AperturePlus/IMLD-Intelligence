<template>
  <view class="pwd-retrieve-container">
	<uni-forms ref="form" :value="user" labelWidth="80px">
	  <uni-forms-item name="newHeight" label="新">
	    <uni-easyinput type="height" v-model="user.newHeight" placeholder="请输入新身高" />
	  </uni-forms-item>
	  <uni-forms-item name="newWeight" label="新">
	    <uni-easyinput type="weight" v-model="user.newWeight" placeholder="请输入新体重" />
	  </uni-forms-item>
	  <uni-forms-item name="newGFR" label="新">
	    <uni-easyinput type="GFR" v-model="user.newGFR" placeholder="请输入GFR" />
	  </uni-forms-item>
	  <uni-forms-item name="newComplications" label="新">
	    <uni-easyinput type="complications" v-model="user.newComplications" placeholder="请输入新并发症情况" />
	  </uni-forms-item>
	  <button type="primary" @click="submit">提交</button>
	</uni-forms>
  </view>
</template>

<script>
  import { updateUserPwd } from "@/api/system/user"

  export default {
    data() {
      return {
        user: {
          oldPassword: undefined,
          newPassword: undefined,
          confirmPassword: undefined
        },
        rules: {
          oldPassword: {
            rules: [{
              required: true,
              errorMessage: '旧密码不能为空'
            }]
          },
          newPassword: {
            rules: [{
                required: true,
                errorMessage: '新密码不能为空',
              },
              {
                minLength: 6,
                maxLength: 20,
                errorMessage: '长度在 6 到 20 个字符'
              }
            ]
          },
          confirmPassword: {
            rules: [{
                required: true,
                errorMessage: '确认密码不能为空'
              }, {
                validateFunction: (rule, value, data) => data.newPassword === value,
                errorMessage: '两次输入的密码不一致'
              }
            ]
          }
        }
      }
    },
    onReady() {
      this.$refs.form.setRules(this.rules)
    },
    methods: {
      submit() {
        this.$refs.form.validate().then(res => {
          updateUserPwd(this.user.oldPassword, this.user.newPassword).then(response => {
            this.$modal.msgSuccess("修改成功")
          })
        })
      }
    }
  }
</script>

<style lang="scss">
  page {
    background-color: #ffffff;
  }

  .pwd-retrieve-container {
    padding-top: 36rpx;
    padding: 15px;
  }
</style>
