package xenosoft.imldintelligence.module.community.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 患者社群/论坛模块 DTO 目录。
 *
 * <p>契约默认按租户隔离，并尽量避免在列表接口中返回大字段（如正文全文）。</p>
 */
public final class CommunityApiDtos {
    private CommunityApiDtos() {
    }

    public static final class Query {
        private Query() {
        }

        public record BoardQuery(
                String status,
                String diseaseScope
        ) {
        }

        public record PostPageQuery(
                @Positive(message = "boardId must be positive")
                Long boardId,
                @Positive(message = "authorTocUserId must be positive")
                Long authorTocUserId,
                String status,
                String keyword
        ) {
        }

        public record CommentPageQuery(
                @Positive(message = "parentCommentId must be positive")
                Long parentCommentId
        ) {
        }

        public record ReportPageQuery(
                String status,
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                OffsetDateTime createdFrom,
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                OffsetDateTime createdTo
        ) {
        }
    }

    public static final class Request {
        private Request() {
        }

        public record CreateBoardRequest(
                @NotBlank(message = "boardCode must not be blank")
                @Size(max = 64, message = "boardCode must be at most 64 characters")
                String boardCode,
                @NotBlank(message = "boardName must not be blank")
                @Size(max = 200, message = "boardName must be at most 200 characters")
                String boardName,
                @Size(max = 2000, message = "description must be at most 2000 characters")
                String description,
                @Size(max = 64, message = "diseaseScope must be at most 64 characters")
                String diseaseScope,
                Integer sortOrder,
                @Size(max = 32, message = "status must be at most 32 characters")
                String status
        ) {
        }

        public record CreatePostRequest(
                @NotNull(message = "boardId is required")
                @Positive(message = "boardId must be positive")
                Long boardId,
                @NotNull(message = "authorTocUserId is required")
                @Positive(message = "authorTocUserId must be positive")
                Long authorTocUserId,
                @NotBlank(message = "title must not be blank")
                @Size(max = 200, message = "title must be at most 200 characters")
                String title,
                @NotBlank(message = "content must not be blank")
                @Size(max = 20000, message = "content must be at most 20000 characters")
                String content,
                Boolean anonymousFlag
        ) {
        }

        public record CreateCommentRequest(
                @NotNull(message = "authorTocUserId is required")
                @Positive(message = "authorTocUserId must be positive")
                Long authorTocUserId,
                @Positive(message = "parentCommentId must be positive")
                Long parentCommentId,
                @NotBlank(message = "content must not be blank")
                @Size(max = 5000, message = "content must be at most 5000 characters")
                String content,
                Boolean anonymousFlag
        ) {
        }

        public record TogglePostLikeRequest(
                @NotNull(message = "tocUserId is required")
                @Positive(message = "tocUserId must be positive")
                Long tocUserId
        ) {
        }

        public record TogglePostBookmarkRequest(
                @NotNull(message = "tocUserId is required")
                @Positive(message = "tocUserId must be positive")
                Long tocUserId
        ) {
        }

        public record CreateReportRequest(
                @NotNull(message = "reporterTocUserId is required")
                @Positive(message = "reporterTocUserId must be positive")
                Long reporterTocUserId,
                @Positive(message = "postId must be positive")
                Long postId,
                @Positive(message = "commentId must be positive")
                Long commentId,
                @NotBlank(message = "reasonCode must not be blank")
                @Size(max = 32, message = "reasonCode must be at most 32 characters")
                String reasonCode,
                @Size(max = 2000, message = "reasonText must be at most 2000 characters")
                String reasonText
        ) {
        }

        public record ModeratePostRequest(
                @NotBlank(message = "status must not be blank")
                @Size(max = 32, message = "status must be at most 32 characters")
                String status,
                @Size(max = 2000, message = "reason must be at most 2000 characters")
                String reason,
                Boolean pinnedFlag
        ) {
        }

        public record ModerateReportRequest(
                @NotBlank(message = "status must not be blank")
                @Size(max = 32, message = "status must be at most 32 characters")
                String status,
                @Size(max = 2000, message = "resultNote must be at most 2000 characters")
                String resultNote,
                @Size(max = 32, message = "resultAction must be at most 32 characters")
                String resultAction
        ) {
        }
    }

    public static final class Response {
        private Response() {
        }

        public record BoardResponse(
                Long id,
                String boardCode,
                String boardName,
                String description,
                String diseaseScope,
                String status,
                Integer sortOrder,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt
        ) {
        }

        public record PostSummaryResponse(
                Long id,
                Long boardId,
                Long authorTocUserId,
                String authorDisplayName,
                Boolean anonymousFlag,
                String title,
                String contentExcerpt,
                String status,
                Boolean pinnedFlag,
                Integer likeCount,
                Integer commentCount,
                Integer reportCount,
                OffsetDateTime lastActivityAt,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt
        ) {
        }

        public record PostDetailResponse(
                Long id,
                Long boardId,
                Long authorTocUserId,
                String authorDisplayName,
                Boolean anonymousFlag,
                String title,
                String content,
                String contentFormat,
                String status,
                Boolean pinnedFlag,
                Integer likeCount,
                Integer commentCount,
                Integer reportCount,
                OffsetDateTime lastActivityAt,
                Long reviewedBy,
                OffsetDateTime reviewedAt,
                String reviewReason,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt,
                OffsetDateTime deletedAt
        ) {
        }

        public record CommentResponse(
                Long id,
                Long postId,
                Long parentCommentId,
                Long authorTocUserId,
                String authorDisplayName,
                Boolean anonymousFlag,
                String content,
                String status,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt,
                OffsetDateTime deletedAt
        ) {
        }

        public record ReportResponse(
                Long id,
                Long reporterTocUserId,
                Long postId,
                Long commentId,
                String reasonCode,
                String reasonText,
                String status,
                Long handledBy,
                OffsetDateTime handledAt,
                String resultAction,
                String resultNote,
                OffsetDateTime createdAt
        ) {
        }

        public record ToggleResultResponse(
                boolean changed
        ) {
        }

        public record ModerationSummary(
                PostDetailResponse post,
                List<ReportResponse> reports
        ) {
        }
    }
}

