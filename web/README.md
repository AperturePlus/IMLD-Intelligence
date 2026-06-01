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

开发模式（Electron + Vite，默认使用 Mock + 本地 ONNX 推理）：

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

## 诊断推理模式

通过 `VITE_IMLD_INFERENCE_MODE` 控制诊断推理链路：

- `browser-onnx`：`VITE_USE_MOCK=true` 时，诊断 mock 接口会调用前端打包的 ONNX 模型和本地 ONNX Runtime Web wasm 资源；适合 Electron 离线演示与私有化联调。
- `backend`：关闭前端 mock 拦截，诊断接口直接请求 `VITE_API_BASE_URL` 指向的 Spring Boot 后端。
- `mock`：不加载 ONNX 模型，直接返回静态疾病 mock payload；仅用于无模型演示。

Electron 打包默认使用 `browser-onnx`，模型、metadata、ONNX Runtime wasm/mjs 均随 Vite/Electron 资源打包，不依赖 CDN。

## 诊断置信度展示

通过以下变量控制报告页“数据置信度”字段：

- `VITE_IMLD_CONFIDENCE_VISIBLE`: 是否展示置信度字段，默认 `true`
- `VITE_IMLD_CONFIDENCE_MIN_THRESHOLD`: 最低可用阈值，默认 `0.7`
- `VITE_IMLD_CONFIDENCE_BOOST_ENABLED`: 是否允许演示抬分，默认 `false`

默认不抬高模型原始输出；低于阈值时显示“需复核”。抬分只在 `mock` / `browser-onnx` 演示模式生效，`backend` 模式始终保留后端返回值。

## 环境变量

参考 `.env.example`：

- `VITE_USE_MOCK`: 是否启用 Mock
- `VITE_IMLD_INFERENCE_MODE`: 诊断推理模式（`browser-onnx` / `backend` / `mock`）
- `VITE_IMLD_DEMO_INFERENCE_ENABLED`: 兼容旧配置，未设置 `VITE_IMLD_INFERENCE_MODE` 时可开启浏览器侧 ONNX 推理
- `VITE_IMLD_CONFIDENCE_VISIBLE`: 是否展示报告置信度
- `VITE_IMLD_CONFIDENCE_MIN_THRESHOLD`: 报告置信度最低阈值
- `VITE_IMLD_CONFIDENCE_BOOST_ENABLED`: 是否在 mock / browser-onnx 演示模式抬高展示置信度
- `VITE_MOCK_DELAY_MS`: Mock 接口延迟（毫秒）
- `VITE_API_BASE_URL`: 真实后端地址（关闭 Mock 时生效）
