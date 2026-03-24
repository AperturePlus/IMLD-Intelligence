# IMLD Web Frontend

Vue 3 + TypeScript + Vite 的网页前端，已切换为 **Bun** 运行时。

## 运行环境

- Bun >= 1.3

## 启动方式

```bash
bun install
bun run dev
```

## 常用命令

```bash
bun run typecheck
bun run build
bun run preview
```

## Electron（Windows）

开发模式（Electron + Vite，继续使用 Mock）：

```bash
bun run dev:electron
```

打包 Windows 安装包（NSIS）：

```bash
bun run build:electron:win
```

打包 Windows 目录版本（免安装）：

```bash
bun run build:electron:win:dir
```

## Mock 说明

开发环境默认启用 Mock（见 `.env.development`），Electron 打包模式使用 `.env.electron`：

- `POST /dj-rest-auth/login/`
- `POST /dj-rest-auth/registration/`
- `GET /dj-rest-auth/user/`

默认测试账号：

- 用户名：`doctor`
- 密码：`123456`

Mock 数据会写入浏览器本地存储，用于模拟注册、登录和 token 校验。

## 环境变量

参考 `.env.example`：

- `VITE_USE_MOCK`: 是否启用 Mock
- `VITE_MOCK_DELAY_MS`: Mock 接口延迟（毫秒）
- `VITE_API_BASE_URL`: 真实后端地址（关闭 Mock 时生效）
