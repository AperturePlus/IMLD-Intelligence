import request from "@/utils/request";

export const listBoards = (params?: {
  status?: string;
  diseaseScope?: string;
}): Promise<any> =>
  request({
    url: "/api/v1/app/community/boards",
    method: "get",
    params,
  });

export const listPosts = (params?: {
  boardId?: number | string;
  authorTocUserId?: number | string;
  status?: string;
  keyword?: string;
  page?: number;
  size?: number;
}): Promise<any> =>
  request({
    url: "/api/v1/app/community/posts",
    method: "get",
    params,
  });

export const getPost = (postId: number | string): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}`,
    method: "get",
  });

export const createPost = (payload: {
  boardId: number | string;
  authorTocUserId: number | string;
  title: string;
  content: string;
  anonymousFlag?: boolean;
}): Promise<any> =>
  request({
    url: "/api/v1/app/community/posts",
    method: "post",
    data: payload,
  });

export const listComments = (
  postId: number | string,
  params?: { parentCommentId?: number | string; page?: number; size?: number }
): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}/comments`,
    method: "get",
    params,
  });

export const createComment = (
  postId: number | string,
  payload: {
    authorTocUserId: number | string;
    parentCommentId?: number | string;
    content: string;
    anonymousFlag?: boolean;
  }
): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}/comments`,
    method: "post",
    data: payload,
  });

export const likePost = (
  postId: number | string,
  tocUserId: number | string
): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}/likes`,
    method: "post",
    data: { tocUserId },
  });

export const unlikePost = (
  postId: number | string,
  tocUserId: number | string
): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}/likes`,
    method: "delete",
    params: { tocUserId },
  });

export const bookmarkPost = (
  postId: number | string,
  tocUserId: number | string
): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}/bookmarks`,
    method: "post",
    data: { tocUserId },
  });

export const unbookmarkPost = (
  postId: number | string,
  tocUserId: number | string
): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}/bookmarks`,
    method: "delete",
    params: { tocUserId },
  });

export const createReport = (payload: {
  reporterTocUserId: number | string;
  postId?: number | string;
  commentId?: number | string;
  reasonCode: string;
  reasonText?: string;
}): Promise<any> =>
  request({
    url: "/api/v1/app/community/reports",
    method: "post",
    data: payload,
  });

export const listPostImages = (postId: number | string): Promise<any> =>
  request({
    url: `/api/v1/app/community/posts/${postId}/images`,
    method: "get",
  });

export const listMyNotifications = (params?: {
  tocUserId: number | string;
  isRead?: boolean;
  page?: number;
  size?: number;
}): Promise<any> =>
  request({
    url: "/api/v1/app/community/notifications",
    method: "get",
    params,
  });

export const markNotificationRead = (
  notificationId: number | string
): Promise<any> =>
  request({
    url: `/api/v1/app/community/notifications/${notificationId}/read`,
    method: "patch",
  });

export const listMyBookmarks = (params?: {
  tocUserId: number | string;
  page?: number;
  size?: number;
}): Promise<any> =>
  request({
    url: "/api/v1/app/community/bookmarks",
    method: "get",
    params,
  });

export const subscribeBoard = (
  boardId: number | string,
  payload: { tocUserId: number | string }
): Promise<any> =>
  request({
    url: `/api/v1/app/community/boards/${boardId}/subscriptions`,
    method: "post",
    data: payload,
  });

export const unsubscribeBoard = (
  boardId: number | string,
  tocUserId: number | string
): Promise<any> =>
  request({
    url: `/api/v1/app/community/boards/${boardId}/subscriptions`,
    method: "delete",
    params: { tocUserId },
  });

export const listSubscribedBoards = (
  tocUserId: number | string
): Promise<any> =>
  request({
    url: "/api/v1/app/community/boards/subscribed",
    method: "get",
    params: { tocUserId },
  });
