<template>
  <view class="container">
    <view class="header">
      <text class="title">遗传代谢性肝病筛查评估</text>
      <text class="subtitle">请准确填写以下临床及检验指标，以便系统进行精准的 AI 辅助分析评估。</text>
    </view>

    <view class="form-wrapper">
      <view class="form-card">
        <view class="card-title">
          <text class="title-icon">|</text>基本信息
        </view>
        <view class="form-item">
          <text class="label">患者姓名 <text class="required">*</text></text>
          <input class="input-box" v-model="formData.patientName" placeholder="请输入真实姓名" placeholder-class="placeholder-style" />
        </view>
        <view class="form-item">
          <text class="label">联系方式 <text class="required">*</text></text>
          <input class="input-box" type="number" maxlength="11" v-model="formData.phone" placeholder="请输入手机号码" placeholder-class="placeholder-style" />
        </view>
        
        <view class="form-item">
          <text class="label">性别 <text class="required">*</text></text>
          <radio-group class="radio-group" @change="radioChange($event, 'gender')">
            <label class="radio-label"><radio value="1" color="#2b85e4" style="transform:scale(0.8)" :checked="formData.gender === '1'" /> 男</label>
            <label class="radio-label"><radio value="2" color="#2b85e4" style="transform:scale(0.8)" :checked="formData.gender === '2'" /> 女</label>
          </radio-group>
        </view>

        <view class="form-item">
          <text class="label">年龄 <text class="required">*</text></text>
          <view class="input-group">
            <input class="input-box flex-1" type="number" maxlength="3" v-model="formData.age" placeholder="请输入年龄" placeholder-class="placeholder-style" />
            <text class="unit">岁</text>
          </view>
        </view>

        <view class="form-item">
          <text class="label">身高 <text class="required">*</text></text>
          <view class="input-group">
            <input class="input-box flex-1" type="digit" v-model="formData.height" placeholder="例如: 175" placeholder-class="placeholder-style" />
            <text class="unit">cm</text>
          </view>
        </view>

        <view class="form-item">
          <text class="label">体重 <text class="required">*</text></text>
          <view class="input-group">
            <input class="input-box flex-1" type="digit" v-model="formData.weight" placeholder="例如: 65.5" placeholder-class="placeholder-style" />
            <text class="unit">kg</text>
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="card-title">
          <text class="title-icon">|</text>核心肝功能指标
          <text class="title-tips">(请参照近期检验报告单填写)</text>
        </view>
        <view class="form-item">
          <text class="label">谷丙转氨酶 (ALT) <text class="required">*</text></text>
          <view class="input-group">
            <input class="input-box flex-1" type="digit" v-model="formData.alt" placeholder="例如: 35" placeholder-class="placeholder-style" />
            <text class="unit">U/L</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">总胆红素 (TBIL) <text class="required">*</text></text>
          <view class="input-group">
            <input class="input-box flex-1" type="digit" v-model="formData.bilirubin" placeholder="例如: 15.2" placeholder-class="placeholder-style" />
            <text class="unit">μmol/L</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">铜蓝蛋白 (CER) <text class="required">*</text></text>
          <view class="input-group">
            <input class="input-box flex-1" type="digit" v-model="formData.ceruloplasmin" placeholder="例如: 0.25" placeholder-class="placeholder-style" />
            <text class="unit">g/L</text>
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="card-title">
          <text class="title-icon">|</text>临床症状与辅助测序
        </view>
        <view class="form-item">
          <text class="label">是否出现黄疸症状？</text>
          <radio-group class="radio-group" @change="radioChange($event, 'jaundice')">
            <label class="radio-label"><radio value="1" color="#2b85e4" style="transform:scale(0.8)" /> 是</label>
            <label class="radio-label"><radio value="0" color="#2b85e4" style="transform:scale(0.8)" :checked="formData.jaundice === '0'" /> 否</label>
          </radio-group>
        </view>
        <view class="form-item">
          <text class="label">是否有肝区不适？</text>
          <radio-group class="radio-group" @change="radioChange($event, 'liverDiscomfort')">
            <label class="radio-label"><radio value="1" color="#2b85e4" style="transform:scale(0.8)" /> 是</label>
            <label class="radio-label"><radio value="0" color="#2b85e4" style="transform:scale(0.8)" :checked="formData.liverDiscomfort === '0'" /> 否</label>
          </radio-group>
        </view>
        <view class="form-item">
          <text class="label">是否已进行基因测序？</text>
          <radio-group class="radio-group" @change="radioChange($event, 'hasGeneticData')">
            <label class="radio-label"><radio value="1" color="#2b85e4" style="transform:scale(0.8)" /> 已测序</label>
            <label class="radio-label"><radio value="0" color="#2b85e4" style="transform:scale(0.8)" :checked="formData.hasGeneticData === '0'" /> 未测序</label>
          </radio-group>
        </view>
      </view>

      <view class="consent-section">
        <label class="consent-label" @click="toggleConsent">
          <radio :checked="formData.dataConsent" color="#2b85e4" style="transform:scale(0.7)" />
          <text class="consent-text">我已阅读并同意<text class="link">《知情同意书》</text>，授权系统对上述数据进行隐私脱敏处理并用于辅助风险分析。</text>
        </label>
      </view>

      <button class="submit-btn" :class="{ 'btn-disabled': !isFormValid }" @click="submitForm">
        提交评估
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      // 表单响应式数据
      formData: {
        patientName: '',
        phone: '',
        gender: '1', // 新增：性别，默认 1 为男，2 为女
        age: '',     // 新增：年龄
        height: '',  // 新增：身高
        weight: '',  // 新增：体重
        alt: '',
        bilirubin: '',
        ceruloplasmin: '',
        jaundice: '0',
        liverDiscomfort: '0',
        hasGeneticData: '0',
        dataConsent: false
      }
    };
  },
  computed: {
    // 简易表单验证：确保必填项已填且已勾选知情同意书
    isFormValid() {
      return this.formData.patientName.trim() !== '' && 
             this.formData.phone.trim().length === 11 &&
             this.formData.age !== '' &&
             this.formData.height !== '' &&
             this.formData.weight !== '' &&
             this.formData.alt !== '' &&
             this.formData.bilirubin !== '' &&
             this.formData.ceruloplasmin !== '' &&
             this.formData.dataConsent;
    }
  },
  methods: {
    // 单选框切换事件
    radioChange(e, field) {
      this.formData[field] = e.detail.value;
    },
    // 切换知情同意书状态
    toggleConsent() {
      this.formData.dataConsent = !this.formData.dataConsent;
    },
    // 提交表单逻辑
    submitForm() {
      if (!this.formData.dataConsent) {
        uni.showToast({ title: '请先勾选知情同意书', icon: 'none' });
        return;
      }
      if (!this.isFormValid) {
        uni.showToast({ title: '请完整填写必填化验指标及基础信息', icon: 'none' });
        return;
      }

      // 模拟提交前的数据脱敏打包环节
      uni.showLoading({ title: '正在处理脱敏...' });
      
      setTimeout(() => {
        uni.hideLoading();
        console.log('前端完成初步脱敏打包准备流转的数据:', this.formData);
        uni.showToast({ title: '数据提交成功', icon: 'success' });
        // TODO: 使用 Axios 将数据传输给后端的临床数据模块 (Domain-A) 和智能诊断模块 (Domain-B)
      }, 1500);
    }
  }
};
</script>

<style scoped>
/* 全局背景与基础排版 */
.container {
  min-height: 100vh;
  background-color: #f4f6f9;
  padding-bottom: 40rpx;
}

/* 头部样式 */
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

/* 表单主体包装器（向上偏移产生层叠效果） */
.form-wrapper {
  padding: 0 30rpx;
  margin-top: -40rpx;
}

/* 卡片样式 */
.form-card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
}

.title-icon {
  color: #2b85e4;
  font-weight: 900;
  margin-right: 12rpx;
  font-size: 28rpx;
}

.title-tips {
  font-size: 22rpx;
  color: #999999;
  font-weight: normal;
  margin-left: 10rpx;
}

/* 表单项布局 */
.form-item {
  margin-bottom: 36rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 16rpx;
}

.required {
  color: #fa3534;
  margin-left: 6rpx;
}

/* 输入框样式 */
.input-box {
  background-color: #f8f9fa;
  border: 1px solid #ebedf0;
  border-radius: 12rpx;
  padding: 0 24rpx;
  height: 80rpx;
  font-size: 28rpx;
  color: #333;
  transition: all 0.3s;
}

.input-box:focus {
  border-color: #2b85e4;
  background-color: #ffffff;
}

.placeholder-style {
  color: #c0c4cc;
}

/* 带有单位的组合输入框 */
.input-group {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  border: 1px solid #ebedf0;
  border-radius: 12rpx;
  padding-right: 24rpx;
}

.input-group .input-box {
  border: none;
  background-color: transparent;
}

.flex-1 {
  flex: 1;
}

.unit {
  font-size: 26rpx;
  color: #666666;
  margin-left: 10rpx;
}

/* 单选框样式 */
.radio-group {
  display: flex;
  gap: 40rpx;
}

.radio-label {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #333333;
}

/* 隐私合规选项 */
.consent-section {
  padding: 0 10rpx 30rpx;
}

.consent-label {
  display: flex;
  align-items: flex-start;
}

.consent-text {
  font-size: 24rpx;
  color: #666666;
  line-height: 1.6;
  margin-left: 8rpx;
  flex: 1;
}

.link {
  color: #2b85e4;
}

/* 提交按钮 */
.submit-btn {
  background: linear-gradient(to right, #2b85e4, #3c9cff);
  color: #ffffff;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
  box-shadow: 0 8rpx 20rpx rgba(43, 133, 228, 0.3);
  border: none;
}

.submit-btn::after {
  border: none;
}

.btn-disabled {
  background: #c8c9cc;
  box-shadow: none;
  color: #f2f3f5;
}
</style>