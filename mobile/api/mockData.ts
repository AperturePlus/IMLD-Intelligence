// mock 用户数据
export const mockUser = {
  accessToken: "mock_access_token_xxx",
  refreshToken: "mock_refresh_token_xxx",
  tenantId: 1,
  tocUserId: 999,
  nickname: "Mock用户",
};

// mock 社区首页帖子列表
export const mockPostList = [
  {
    id: 1,
    title: "欢迎使用Mock模式测试",
    content: "这是一条模拟帖子，用于无后端测试。",
    authorNickname: "Mock用户",
    boardName: "综合交流",
    likeCount: 12,
    commentCount: 3,
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    title: "Mock模式体验分享",
    content: "在没有后端的情况下，前端可以独立开发和测试页面。",
    authorNickname: "测试用户A",
    boardName: "经验分享",
    likeCount: 8,
    commentCount: 5,
    createdAt: new Date().toISOString(),
  },
  {
    id: 3,
    title: "关于饮食健康的讨论",
    content: "mock数据可以帮助我们在各种场景下验证UI和交互逻辑。",
    authorNickname: "测试用户B",
    boardName: "饮食健康",
    likeCount: 20,
    commentCount: 7,
    createdAt: new Date().toISOString(),
  },
];

// mock 帖子详情
export const mockPostDetail = {
  id: 1,
  title: "欢迎使用Mock模式测试",
  content: "这是一条模拟帖子，用于无后端测试。可以测试页面渲染效果。",
  authorNickname: "Mock用户",
  boardName: "综合交流",
  likeCount: 12,
  commentCount: 3,
  images: [],
  createdAt: new Date().toISOString(),
};

// mock 评论列表
export const mockCommentList = [
  {
    id: 1,
    content: "这是一条模拟评论",
    authorNickname: "测试用户A",
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    content: "Mock模式很方便开发调试",
    authorNickname: "测试用户B",
    createdAt: new Date().toISOString(),
  },
];

// mock 通知列表
export const mockNotificationList = [
  {
    id: 1,
    type: "COMMENT",
    title: "新评论",
    content: "有人评论了你的帖子",
    isRead: false,
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    type: "LIKE",
    title: "新点赞",
    content: "有人赞了你的帖子",
    isRead: true,
    createdAt: new Date().toISOString(),
  },
];

// mock 收藏列表
export const mockBookmarkList = [
  {
    id: 1,
    postId: 1,
    postTitle: "欢迎使用Mock模式测试",
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    postId: 2,
    postTitle: "Mock模式体验分享",
    createdAt: new Date().toISOString(),
  },
];

// mock 板块列表
export const mockBoardList = [
  { id: 1, boardCode: "general", boardName: "综合交流" },
  { id: 2, boardCode: "experience", boardName: "经验分享" },
  { id: 3, boardCode: "diet", boardName: "饮食健康" },
  { id: 4, boardCode: "qa", boardName: "问答互助" },
];

// 根据 URL 返回对应的 mock 数据
export function getMockResponse(url: string, method: string): any {
  const upperMethod = method.toUpperCase();

  // toc auth - 手机验证码登录
  if (url.includes("/api/v1/app/toc/auth/phone/send-code")) {
    return {
      code: 200,
      data: {
        purpose: "LOGIN",
        expiresAt: new Date(Date.now() + 5 * 60 * 1000).toISOString(),
        resendAfterSeconds: 60,
      },
      message: "success",
    };
  }

  // toc auth - 手机登录 / 微信登录 / 刷新会话
  if (
    url.includes("/api/v1/app/toc/auth/phone/login") ||
    url.includes("/api/v1/app/toc/auth/wechat/login") ||
    url.includes("/api/v1/app/toc/auth/refresh")
  ) {
    return { code: 200, data: mockUser, message: "success" };
  }

  // toc auth - 登出
  if (url.includes("/api/v1/app/toc/auth/logout")) {
    return { code: 200, data: true, message: "success" };
  }

  // 帖子图片上传
  if (url.includes("/api/v1/app/community/posts") && url.includes("/images")) {
    return { code: 200, data: [], message: "success" };
  }

  // 通知列表
  if (url.includes("/api/v1/app/community/notifications")) {
    return {
      code: 200,
      data: {
        records: mockNotificationList,
        total: mockNotificationList.length,
      },
      message: "success",
    };
  }

  // 收藏列表
  if (
    url.includes("/api/v1/app/community/bookmarks") &&
    upperMethod === "GET"
  ) {
    return {
      code: 200,
      data: { records: mockBookmarkList, total: mockBookmarkList.length },
      message: "success",
    };
  }

  // 板块列表 / 订阅板块
  if (url.includes("/api/v1/app/community/boards")) {
    return { code: 200, data: mockBoardList, message: "success" };
  }

  // 帖子列表 (GET /api/v1/app/community/posts)
  if (
    url.includes("/api/v1/app/community/posts") &&
    upperMethod === "GET" &&
    !url.includes("/comments") &&
    !url.includes("/likes") &&
    !url.includes("/bookmarks") &&
    !url.includes("/images")
  ) {
    // 检查是否为详情 (包含 /posts/数字)
    const detailMatch = url.match(/\/posts\/(\d+)$/);
    if (detailMatch) {
      return {
        code: 200,
        data: { ...mockPostDetail, id: Number(detailMatch[1]) },
        message: "success",
      };
    }
    return {
      code: 200,
      data: { records: mockPostList, total: mockPostList.length },
      message: "success",
    };
  }

  // 创建帖子
  if (
    url.includes("/api/v1/app/community/posts") &&
    upperMethod === "POST" &&
    !url.includes("/comments") &&
    !url.includes("/likes") &&
    !url.includes("/bookmarks")
  ) {
    return {
      code: 200,
      data: { ...mockPostDetail, id: Date.now() },
      message: "success",
    };
  }

  // 评论列表 / 创建评论
  if (
    url.includes("/api/v1/app/community/posts") &&
    url.includes("/comments")
  ) {
    if (upperMethod === "GET") {
      return {
        code: 200,
        data: { records: mockCommentList, total: mockCommentList.length },
        message: "success",
      };
    }
    if (upperMethod === "POST") {
      return {
        code: 200,
        data: {
          id: Date.now(),
          content: "模拟评论",
          createdAt: new Date().toISOString(),
        },
        message: "success",
      };
    }
  }

  // 点赞 / 取消点赞 / 收藏 / 取消收藏
  if (
    (url.includes("/api/v1/app/community/posts") && url.includes("/likes")) ||
    (url.includes("/api/v1/app/community/posts") && url.includes("/bookmarks"))
  ) {
    return { code: 200, data: true, message: "success" };
  }

  // 举报
  if (url.includes("/api/v1/app/community/reports")) {
    return { code: 200, data: true, message: "success" };
  }

  // 默认返回成功
  return { code: 200, data: null, message: "success" };
}
