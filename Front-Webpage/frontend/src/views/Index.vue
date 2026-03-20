<template>
  <div class="login-container">
    <div class="page-title">
      <h1>数智肝循</h1>
      <h2>遗传代谢性肝病管理平台</h2>
    </div>

    <div class="container" :class="{ 'right-panel-active': isRightPanelActive }" id="container">
      
      <div class="container__form container--signup">
        <el-form 
          class="form" 
          id="form1" 
          :model="registerForm"
          label-width="80px"
          label-position="left"
        >
          <h2 class="form__title">注册</h2>
          
          <el-form-item prop="username" label="账号">
            <el-input v-model="registerForm.username" placeholder="请输入账号" clearable />
          </el-form-item>
          
          <el-form-item prop="email" label="邮箱">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>
          
          <el-form-item prop="password1" label="密码">
            <el-input v-model="registerForm.password1" placeholder="输入密码" show-password />
          </el-form-item>
          
          <el-form-item prop="password2" label="确认密码">
            <el-input v-model="registerForm.password2" placeholder="再次输入密码" show-password />
          </el-form-item>
          
          <el-form-item label-width="0" class="btn-item">
            <el-button 
              type="primary" 
              class="btn" 
              :loading="isLoading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="container__form container--signin">
        <el-form 
          class="form" 
          id="form2" 
          :model="loginForm"
          label-width="60px"
          label-position="left"
        >
          <h2 class="form__title">登录</h2>
          
          <el-form-item prop="username" label="账号">
            <el-input v-model="loginForm.username" placeholder="请输入账号" clearable />
          </el-form-item>
          
          <el-form-item prop="password" label="密码">
            <el-input v-model="loginForm.password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          
          <el-form-item label-width="0" style="margin-bottom: 0 !important;">
            <a href="#" class="link">忘记密码?</a>
          </el-form-item>
          
          <el-form-item label-width="0" class="btn-item">
            <el-button 
              type="primary" 
              class="btn" 
              :loading="isLoading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="container__overlay">
        <div class="overlay">
          <div class="overlay__panel overlay--left">
            <el-button class="btn ghost" @click="togglePanel(false)">已有账号? 登录</el-button>
          </div>
          <div class="overlay__panel overlay--right">
            <el-button class="btn ghost" @click="togglePanel(true)">没有账号? 注册</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="page-footer">
      © 四川大学华西临床医学院2026IMLD项目组 All Rights Reserved.
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import userApi from '../api/user'; 
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus'; 

const router = useRouter();

// 控制加载状态
const isLoading = ref(false);
const isRightPanelActive = ref(false);

const registerForm = reactive({
  username: '',
  email: '',
  password1: '',
  password2: ''
});

const loginForm = reactive({
  username: '',
  password: ''
});

const togglePanel = (isRightActive: boolean) => {
  isRightPanelActive.value = isRightActive;
};

// 处理注册
// 处理注册
const handleRegister = async () => {
  console.log('点击了注册按钮');

  if (!registerForm.username || !registerForm.password1) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  if (registerForm.password1 !== registerForm.password2) {
    ElMessage.error('两次密码不一致！');
    return;
  }

  isLoading.value = true; // 开启 Loading

  try {
    const res = await userApi.register({
      username: registerForm.username,
      email: registerForm.email,
      password1: registerForm.password1,
      password2: registerForm.password2
    });
    
    console.log('注册响应:', res);
    ElMessage.success('注册成功，请登录');
    
    // 注册成功后自动清空并切换到登录页
    registerForm.username = ''; 
    registerForm.email = '';
    registerForm.password1 = ''; 
    registerForm.password2 = '';
    togglePanel(false); 

  } catch (error: any) {
    console.error('注册失败详情:', error);
    
    // --- 修改开始：通用错误解析逻辑 ---
    let errorMsg = '注册失败，请检查网络或账号';

    if (error.response && error.response.data) {
      const data = error.response.data;
      const messages: string[] = [];

      // 遍历后端返回的所有字段 (如 username, email, password, non_field_errors 等)
      Object.keys(data).forEach((key) => {
        const value = data[key];
        // 后端通常返回数组 ["错误1", "错误2"]，也可能是字符串
        if (Array.isArray(value)) {
          messages.push(...value);
        } else if (typeof value === 'string') {
          messages.push(value);
        }
      });

      if (messages.length > 0) {
        // 使用 <br> 换行，拼接所有错误
        errorMsg = messages.join('<br>');
      }
    }

    // 使用 dangerouslyUseHTMLString: true 来支持换行显示
    ElMessage.error({
      message: errorMsg,
      dangerouslyUseHTMLString: true,
      duration: 5000 // 错误信息较多时，延长时间给用户阅读
    });
    // --- 修改结束 ---

  } finally {
    isLoading.value = false; // 关闭 Loading
  }
};

// 处理登录
const handleLogin = async () => {
  console.log('点击了登录按钮'); // 调试日志

  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码');
    return;
  }

  isLoading.value = true; // 开启 Loading

  try {
    const res = await userApi.login({
      username: loginForm.username,
      password: loginForm.password
    });

    console.log('登录响应:', res);

    if (res.data.key) {
      localStorage.setItem('token', res.data.key);
      localStorage.setItem('username', loginForm.username);
      // --- 新增代码结束 ---
      ElMessage.success('登录成功');
      
      // 延迟跳转
      setTimeout(() => {
        router.replace('/center'); 
      }, 500);
    } else {
      ElMessage.error('登录失败：未获取到 Token');
    }
  } catch (error: any) {
    console.error('登录失败详情:', error);
    const errorMsg = error.response?.data?.non_field_errors?.[0] || '登录失败，请检查账号密码';
    ElMessage.error(errorMsg);
  } finally {
    isLoading.value = false; // 关闭 Loading
  }
};

onMounted(() => {
  console.log('Login Page Loaded');
});
</script>

<style scoped>
:root {
  --white: #e9e9e9;
  --gray: #333;
  --blue: #0367a6;
  --lightblue: #008997;
  --button-radius: 0.7rem;
  --max-width: 900px; 
  --max-height: 420px;

  font-size: 16px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.login-container {
  align-items: center;
  background-color: #e9e9e9;
  /* 如果图片找不到，给个兜底色 */
  background: #e9e9e9 url("../assets/login.webp") no-repeat center center fixed; 
  background-size: cover;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100vh;
  position: relative;
}

.page-title {
  text-align: center;
  color: white;
  margin-bottom: 2rem;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
}
.page-title h1 { font-size: 3rem; font-weight: 600; margin: 0; }
.page-title h2 { font-size: 1.5rem; font-weight: 300; margin: 5px 0 0 0; }

.page-footer {
  position: absolute;
  bottom: 1rem;
  left: 0; right: 0;
  text-align: center;
  color: #f0f0f0;
  font-size: 0.85rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.6);
}

/* 容器样式 */
.container {
  background-color: #ffffff; 
  border-radius: 0.7rem;
  box-shadow: 0 0.9rem 1.7rem rgba(0, 0, 0, 0.25), 0 0.7rem 0.7rem rgba(0, 0, 0, 0.22);
  position: relative;
  overflow: hidden;
  width: 100%;
  max-width: 900px; 
  min-height: 450px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.container__form {
  position: absolute;
  top: 0;
  height: 100%;
  transition: all 0.6s ease-in-out;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.container--signin { left: 0; width: 50%; z-index: 2; }
.container.right-panel-active .container--signin { transform: translateX(100%); }
.container--signup { left: 0; opacity: 0; width: 50%; z-index: 1; }
.container.right-panel-active .container--signup { animation: show 0.6s; opacity: 1; z-index: 5; transform: translateX(100%); }

.container__overlay {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  overflow: hidden;
  transition: transform 0.6s ease-in-out;
  z-index: 100;
}
.container.right-panel-active .container__overlay { transform: translateX(-100%); }

.overlay {
  background: #008997; 
  background: linear-gradient(to right, #0367a6, #008997);
  background: url("../assets/login.webp") no-repeat center center fixed;
  background-size: cover;
  
  color: #ffffff;
  position: relative;
  left: -100%;
  height: 100%;
  width: 200%;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
}
.container.right-panel-active .overlay { transform: translateX(50%); }

.overlay__panel {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  text-align: center;
  top: 0;
  height: 100%;
  width: 50%;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
  padding: 0 40px;
  box-sizing: border-box;
}
.overlay--left { transform: translateX(-20%); }
.container.right-panel-active .overlay--left { transform: translateX(0); }
.overlay--right { right: 0; transform: translateX(0); }
.container.right-panel-active .overlay--right { transform: translateX(20%); }

/* 按钮样式 */
.btn {
  background-color: #0367a6; 
  background-image: linear-gradient(90deg, #0367a6 0%, #008997 74%);
  border-radius: 24px;
  border: 1px solid #0367a6;
  color: #ffffff;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: bold;
  letter-spacing: 0.1rem;
  
  width: 160px;
  height: 48px; /* 固定高度，防止变形 */
  padding: 0;   /* Element Button 内容居中 */
  
  text-transform: uppercase;
  transition: transform 80ms ease-in;
  margin-top: 15px;
}

.btn:active { transform: scale(0.95); }
.btn:focus { outline: none; }

/* 覆盖层幽灵按钮 */
.btn.ghost {
  background-color: transparent;
  background-image: none;
  border-color: #ffffff;
  color: #ffffff;
  width: auto;
  height: auto;
  padding: 12px 35px;
  margin-top: 0;
}
.btn.ghost:hover { background-color: #ffffff; color: #0367a6; }

/* Form 布局 */
.form {
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 0 50px;
  height: 100%;
  text-align: center;
  width: 100%;
  box-sizing: border-box;
}

.form__title {
  font-weight: bold;
  font-size: 24px;
  margin: 0 0 20px 0;
  color: #333;
}

.link {
  color: #333;
  font-size: 14px;
  text-decoration: none;
  margin: 15px 0;
  display: block;
  text-align: right;
  width: 100%;
}

/* Element Plus 覆盖 */
:deep(.el-form-item) {
  display: flex; 
  align-items: center; 
  width: 100%;
  margin-bottom: 20px !important; 
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0 !important;
}

:deep(.el-form-item.btn-item .el-form-item__content) {
  justify-content: center !important; 
  margin-left: 0 !important; 
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
  line-height: normal; 
  padding-right: 12px; 
  justify-content: flex-start; 
}

:deep(.el-form-item__content) {
  flex: 1;
  margin-left: 0 !important;
  line-height: normal;
}

:deep(.el-input__wrapper) {
  background-color: #eee; 
  border: none;
  box-shadow: none; 
  border-radius: 4px; 
  padding: 8px 15px;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #0367a6 inset; 
  background-color: #fff;
}

@keyframes show {
  0%, 49.99% { opacity: 0; z-index: 1; }
  50%, 100% { opacity: 1; z-index: 5; }
}
</style>