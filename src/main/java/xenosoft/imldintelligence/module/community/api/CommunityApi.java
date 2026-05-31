package xenosoft.imldintelligence.module.community.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.common.dto.PageQueryRequest;
import xenosoft.imldintelligence.common.dto.PagedResultResponse;
import xenosoft.imldintelligence.module.community.api.dto.CommunityApiDtos;

import java.util.List;

/**
 * 患者社群/论坛模块 HTTP 契约。
 *
 * <p>该模块面向 C 端患者侧内容互动，默认按租户隔离，所有写操作均需显式携带租户上下文。</p>
 */
@Validated
@RequestMapping({"/api/v1/community", "/api/v1/app/community", "/api/v1/web/community"})
public interface CommunityApi {

    @GetMapping("/boards")
    ApiResponse<List<CommunityApiDtos.Response.BoardResponse>> listBoards(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @ModelAttribute CommunityApiDtos.Query.BoardQuery query
    );

    @PostMapping("/boards")
    ApiResponse<CommunityApiDtos.Response.BoardResponse> createBoard(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody CommunityApiDtos.Request.CreateBoardRequest request
    );

    @GetMapping("/posts")
    ApiResponse<PagedResultResponse<CommunityApiDtos.Response.PostSummaryResponse>> listPosts(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @ModelAttribute CommunityApiDtos.Query.PostPageQuery query,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    @GetMapping("/posts/{postId}")
    ApiResponse<CommunityApiDtos.Response.PostDetailResponse> getPost(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId
    );

    @PostMapping("/posts")
    ApiResponse<CommunityApiDtos.Response.PostDetailResponse> createPost(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody CommunityApiDtos.Request.CreatePostRequest request
    );

    @GetMapping("/posts/{postId}/comments")
    ApiResponse<PagedResultResponse<CommunityApiDtos.Response.CommentResponse>> listComments(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId,
            @Valid @ModelAttribute CommunityApiDtos.Query.CommentPageQuery query,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    @PostMapping("/posts/{postId}/comments")
    ApiResponse<CommunityApiDtos.Response.CommentResponse> createComment(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId,
            @Valid @RequestBody CommunityApiDtos.Request.CreateCommentRequest request
    );

    @PostMapping("/posts/{postId}/likes")
    ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> likePost(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId,
            @Valid @RequestBody CommunityApiDtos.Request.TogglePostLikeRequest request
    );

    @DeleteMapping("/posts/{postId}/likes")
    ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> unlikePost(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId,
            @RequestParam("tocUserId")
            @Positive(message = "tocUserId must be positive")
            Long tocUserId
    );

    @PostMapping("/posts/{postId}/bookmarks")
    ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> bookmarkPost(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId,
            @Valid @RequestBody CommunityApiDtos.Request.TogglePostBookmarkRequest request
    );

    @DeleteMapping("/posts/{postId}/bookmarks")
    ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> unbookmarkPost(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId,
            @RequestParam("tocUserId")
            @Positive(message = "tocUserId must be positive")
            Long tocUserId
    );

    @PostMapping("/reports")
    ApiResponse<CommunityApiDtos.Response.ReportResponse> createReport(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody CommunityApiDtos.Request.CreateReportRequest request
    );

    @GetMapping("/moderation/posts")
    ApiResponse<PagedResultResponse<CommunityApiDtos.Response.PostSummaryResponse>> listPostsForModeration(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @ModelAttribute CommunityApiDtos.Query.PostPageQuery query,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    @PostMapping("/moderation/posts/{postId}")
    ApiResponse<CommunityApiDtos.Response.PostDetailResponse> moderatePost(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId,
            @Valid @RequestBody CommunityApiDtos.Request.ModeratePostRequest request
    );

    @GetMapping("/moderation/reports")
    ApiResponse<PagedResultResponse<CommunityApiDtos.Response.ReportResponse>> listReports(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @ModelAttribute CommunityApiDtos.Query.ReportPageQuery query,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    @PostMapping("/moderation/reports/{reportId}")
    ApiResponse<CommunityApiDtos.Response.ReportResponse> moderateReport(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("reportId")
            @Positive(message = "reportId must be positive")
            Long reportId,
            @Valid @RequestBody CommunityApiDtos.Request.ModerateReportRequest request
    );

    @GetMapping("/posts/{postId}/images")
    ApiResponse<List<CommunityApiDtos.Response.PostImageResponse>> listPostImages(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("postId")
            @Positive(message = "postId must be positive")
            Long postId
    );

    @GetMapping("/notifications")
    ApiResponse<PagedResultResponse<CommunityApiDtos.Response.NotificationResponse>> listMyNotifications(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @RequestParam("tocUserId")
            @Positive(message = "tocUserId must be positive")
            Long tocUserId,
            @RequestParam(name = "isRead", required = false)
            Boolean isRead,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    @PatchMapping("/notifications/{notificationId}/read")
    ApiResponse<CommunityApiDtos.Response.NotificationResponse> markNotificationRead(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("notificationId")
            @Positive(message = "notificationId must be positive")
            Long notificationId
    );

    @GetMapping("/bookmarks")
    ApiResponse<PagedResultResponse<CommunityApiDtos.Response.PostSummaryResponse>> listMyBookmarks(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @RequestParam("tocUserId")
            @Positive(message = "tocUserId must be positive")
            Long tocUserId,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    @PostMapping("/boards/{boardId}/subscriptions")
    ApiResponse<CommunityApiDtos.Response.BoardSubscriptionResponse> subscribeBoard(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("boardId")
            @Positive(message = "boardId must be positive")
            Long boardId,
            @Valid @RequestBody CommunityApiDtos.Request.SubscribeBoardRequest request
    );

    @DeleteMapping("/boards/{boardId}/subscriptions")
    ApiResponse<Boolean> unsubscribeBoard(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("boardId")
            @Positive(message = "boardId must be positive")
            Long boardId,
            @RequestParam("tocUserId")
            @Positive(message = "tocUserId must be positive")
            Long tocUserId
    );

    @GetMapping("/boards/subscribed")
    ApiResponse<List<CommunityApiDtos.Response.BoardSubscriptionResponse>> listSubscribedBoards(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @RequestParam("tocUserId")
            @Positive(message = "tocUserId must be positive")
            Long tocUserId
    );
}
