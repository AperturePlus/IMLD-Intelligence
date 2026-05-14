package xenosoft.imldintelligence.module.community.api;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.common.RequireAnyRole;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.common.dto.PageQueryRequest;
import xenosoft.imldintelligence.common.dto.PagedResultResponse;
import xenosoft.imldintelligence.module.community.api.dto.CommunityApiDtos;
import xenosoft.imldintelligence.module.community.internal.model.CommunityBoard;
import xenosoft.imldintelligence.module.community.internal.model.CommunityContentReport;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPost;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPostComment;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityBoardRepository;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityBookmarkRepository;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityCommentRepository;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityLikeRepository;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityPostRepository;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityReportRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityCommentPageRow;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostSummaryRow;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityReportPageRow;
import xenosoft.imldintelligence.module.community.internal.security.CommunityTenantAccessGuard;
import xenosoft.imldintelligence.module.community.internal.service.CommunityAutoModerationService;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.security.CurrentUserSubjectProvider;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class CommunityController implements CommunityControllerContract {

    private static final String ROLE_TOC_USER = "TOC_USER";
    private static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DELETED = "DELETED";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final CurrentUserSubjectProvider currentUserSubjectProvider;
    private final CommunityTenantAccessGuard tenantAccessGuard;
    private final CommunityAutoModerationService autoModerationService;

    private final CommunityBoardRepository boardRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityLikeRepository likeRepository;
    private final CommunityBookmarkRepository bookmarkRepository;
    private final CommunityReportRepository reportRepository;

    private final TocUserRepository tocUserRepository;

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    public ApiResponse<List<CommunityApiDtos.Response.BoardResponse>> listBoards(Long tenantId,
                                                                                 CommunityApiDtos.Query.BoardQuery query) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        String status = hasText(query == null ? null : query.status()) ? query.status().trim() : STATUS_ACTIVE;
        List<CommunityBoard> boards = boardRepository.listByTenantId(
                resolvedTenantId,
                status,
                query == null ? null : query.diseaseScope()
        );

        List<CommunityApiDtos.Response.BoardResponse> items = boards.stream()
                .map(this::toBoardResponse)
                .toList();
        return ApiResponse.success(items);
    }

    @Override
    @RequireAnyRole({ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.BoardResponse> createBoard(Long tenantId,
                                                                            CommunityApiDtos.Request.CreateBoardRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (boardRepository.findByBoardCode(resolvedTenantId, request.boardCode()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Board code already exists");
        }
        CommunityBoard board = new CommunityBoard();
        board.setTenantId(resolvedTenantId);
        board.setBoardCode(request.boardCode().trim());
        board.setBoardName(request.boardName().trim());
        board.setDescription(trimToNull(request.description()));
        board.setDiseaseScope(trimToNull(request.diseaseScope()));
        board.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        board.setStatus(hasText(request.status()) ? request.status().trim() : STATUS_ACTIVE);
        try {
            boardRepository.save(board);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Board code already exists", ex);
        }

        CommunityBoard created = boardRepository.findByBoardCode(resolvedTenantId, board.getBoardCode()).orElse(board);
        return ApiResponse.success(toBoardResponse(created));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    public ApiResponse<PagedResultResponse<CommunityApiDtos.Response.PostSummaryResponse>> listPosts(
            Long tenantId,
            CommunityApiDtos.Query.PostPageQuery query,
            PageQueryRequest pageQuery) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        int page = pageQuery != null && pageQuery.page() != null ? pageQuery.page() : DEFAULT_PAGE;
        int size = pageQuery != null && pageQuery.size() != null ? pageQuery.size() : DEFAULT_SIZE;
        long offset = (long) page * size;

        String status = resolvePostListStatus(subject, query);

        List<CommunityPostSummaryRow> rows = postRepository.listSummariesByConditionWithTotal(
                resolvedTenantId,
                query == null ? null : query.boardId(),
                query == null ? null : query.authorTocUserId(),
                status,
                trimToNull(query == null ? null : query.keyword()),
                true,
                offset,
                size
        );

        long total = rows.isEmpty() ? 0L : (rows.getFirst().getTotalCount() == null ? 0L : rows.getFirst().getTotalCount());
        List<CommunityApiDtos.Response.PostSummaryResponse> items = rows.stream()
                .map(this::toPostSummaryResponse)
                .toList();
        return ApiResponse.success(new PagedResultResponse<>(page, size, total, items));
    }

    private String resolvePostListStatus(UserSubject subject, CommunityApiDtos.Query.PostPageQuery query) {
        String requestedStatus = trimToNull(query == null ? null : query.status());
        if (requestedStatus == null) {
            return STATUS_PUBLISHED;
        }
        if (STATUS_PUBLISHED.equalsIgnoreCase(requestedStatus)) {
            return STATUS_PUBLISHED;
        }
        if (hasRole(subject, ROLE_SYSTEM_ADMIN)) {
            return requestedStatus;
        }
        Long authorTocUserId = query == null ? null : query.authorTocUserId();
        if (!Objects.equals(authorTocUserId, subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can query non-published posts");
        }
        return requestedStatus;
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    public ApiResponse<CommunityApiDtos.Response.PostDetailResponse> getPost(Long tenantId, Long postId) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        CommunityPost post = postRepository.findById(resolvedTenantId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (STATUS_DELETED.equalsIgnoreCase(post.getStatus()) || post.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        if (STATUS_PUBLISHED.equalsIgnoreCase(post.getStatus())) {
            return ApiResponse.success(toPostDetailResponse(post));
        }

        if (Objects.equals(post.getAuthorTocUserId(), subject.userId())) {
            return ApiResponse.success(toPostDetailResponse(post));
        }

        if (hasRole(subject, ROLE_SYSTEM_ADMIN)) {
            return ApiResponse.success(toPostDetailResponse(post));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Post is not visible");
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.PostDetailResponse> createPost(Long tenantId,
                                                                                CommunityApiDtos.Request.CreatePostRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (!Objects.equals(request.authorTocUserId(), subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "authorTocUserId does not match authenticated user");
        }

        TocUser author = tocUserRepository.findById(resolvedTenantId, request.authorTocUserId())
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ToC user not found or inactive"));

        CommunityBoard board = boardRepository.findById(resolvedTenantId, request.boardId())
                .filter(b -> STATUS_ACTIVE.equalsIgnoreCase(b.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board not found or inactive"));

        boolean anonymous = Boolean.TRUE.equals(request.anonymousFlag());
        String authorDisplayName = anonymous ? "匿名用户" : defaultDisplayName(author);

        CommunityAutoModerationService.AutoReviewDecision review = autoModerationService.reviewPost(request.title(), request.content());
        String postStatus = (review.status() == CommunityAutoModerationService.AutoReviewStatus.PASS)
                ? STATUS_PUBLISHED
                : STATUS_PENDING;

        CommunityPost post = new CommunityPost();
        post.setTenantId(resolvedTenantId);
        post.setBoardId(board.getId());
        post.setAuthorTocUserId(author.getId());
        post.setAuthorDisplayName(authorDisplayName);
        post.setAnonymousFlag(anonymous);
        post.setTitle(request.title().trim());
        post.setContent(request.content());
        post.setContentFormat("TEXT");
        post.setStatus(postStatus);
        post.setPinnedFlag(false);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setReportCount(0);
        post.setLastActivityAt(OffsetDateTime.now(ZoneOffset.UTC));
        post.setReviewedBy(null);
        post.setReviewedAt(null);
        post.setReviewReason(null);
        post.setDeletedAt(null);

        post.setAutoReviewStatus(review.status().name());
        post.setAutoReviewProvider(review.provider());
        post.setAutoReviewReason(review.reason());
        post.setAutoReviewAt(review.reviewedAt());

        postRepository.save(post);

        CommunityPost created = postRepository.findById(resolvedTenantId, post.getId()).orElse(post);
        return ApiResponse.success(toPostDetailResponse(created));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    public ApiResponse<PagedResultResponse<CommunityApiDtos.Response.CommentResponse>> listComments(
            Long tenantId,
            Long postId,
            CommunityApiDtos.Query.CommentPageQuery query,
            PageQueryRequest pageQuery) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        CommunityPost post = postRepository.findById(resolvedTenantId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (STATUS_DELETED.equalsIgnoreCase(post.getStatus()) || post.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        boolean visible = STATUS_PUBLISHED.equalsIgnoreCase(post.getStatus())
                || Objects.equals(post.getAuthorTocUserId(), subject.userId())
                || hasRole(subject, ROLE_SYSTEM_ADMIN);
        if (!visible) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Post is not visible");
        }

        int page = pageQuery != null && pageQuery.page() != null ? pageQuery.page() : DEFAULT_PAGE;
        int size = pageQuery != null && pageQuery.size() != null ? pageQuery.size() : DEFAULT_SIZE;
        long offset = (long) page * size;

        List<CommunityCommentPageRow> rows = commentRepository.listByPostIdWithTotal(
                resolvedTenantId,
                postId,
                query == null ? null : query.parentCommentId(),
                true,
                offset,
                size
        );

        long total = rows.isEmpty() ? 0L : (rows.getFirst().getTotalCount() == null ? 0L : rows.getFirst().getTotalCount());
        List<CommunityApiDtos.Response.CommentResponse> items = rows.stream()
                .map(this::toCommentResponse)
                .toList();

        return ApiResponse.success(new PagedResultResponse<>(page, size, total, items));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.CommentResponse> createComment(
            Long tenantId,
            Long postId,
            CommunityApiDtos.Request.CreateCommentRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (!Objects.equals(request.authorTocUserId(), subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "authorTocUserId does not match authenticated user");
        }

        CommunityPost post = postRepository.findById(resolvedTenantId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!STATUS_PUBLISHED.equalsIgnoreCase(post.getStatus()) || post.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not available for comments");
        }

        TocUser author = tocUserRepository.findById(resolvedTenantId, request.authorTocUserId())
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ToC user not found or inactive"));

        if (request.parentCommentId() != null) {
            CommunityPostComment parent = commentRepository.findById(resolvedTenantId, request.parentCommentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment not found"));
            if (!Objects.equals(parent.getPostId(), postId)
                    || parent.getDeletedAt() != null
                    || !STATUS_PUBLISHED.equalsIgnoreCase(parent.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment is not available");
            }
        }

        autoModerationService.assertCommentContentAllowed(request.content());

        boolean anonymous = Boolean.TRUE.equals(request.anonymousFlag());
        String authorDisplayName = anonymous ? "匿名用户" : defaultDisplayName(author);

        CommunityPostComment comment = new CommunityPostComment();
        comment.setTenantId(resolvedTenantId);
        comment.setPostId(postId);
        comment.setParentCommentId(request.parentCommentId());
        comment.setAuthorTocUserId(author.getId());
        comment.setAuthorDisplayName(authorDisplayName);
        comment.setAnonymousFlag(anonymous);
        comment.setContent(request.content());
        comment.setStatus(STATUS_PUBLISHED);
        commentRepository.save(comment);

        postRepository.incrementCommentCount(resolvedTenantId, postId);

        CommunityPostComment created = commentRepository.findById(resolvedTenantId, comment.getId()).orElse(comment);
        return ApiResponse.success(toCommentResponse(created));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> likePost(
            Long tenantId,
            Long postId,
            CommunityApiDtos.Request.TogglePostLikeRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (!Objects.equals(request.tocUserId(), subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "tocUserId does not match authenticated user");
        }
        requirePublishedPost(resolvedTenantId, postId);

        boolean changed = likeRepository.like(resolvedTenantId, postId, request.tocUserId());
        if (changed) {
            postRepository.incrementLikeCount(resolvedTenantId, postId);
        }
        return ApiResponse.success(new CommunityApiDtos.Response.ToggleResultResponse(changed));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> unlikePost(Long tenantId,
                                                                                  Long postId,
                                                                                  Long tocUserId) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (!Objects.equals(tocUserId, subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "tocUserId does not match authenticated user");
        }
        requirePublishedPost(resolvedTenantId, postId);

        boolean changed = likeRepository.unlike(resolvedTenantId, postId, tocUserId);
        if (changed) {
            postRepository.decrementLikeCount(resolvedTenantId, postId);
        }
        return ApiResponse.success(new CommunityApiDtos.Response.ToggleResultResponse(changed));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> bookmarkPost(
            Long tenantId,
            Long postId,
            CommunityApiDtos.Request.TogglePostBookmarkRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (!Objects.equals(request.tocUserId(), subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "tocUserId does not match authenticated user");
        }
        requirePublishedPost(resolvedTenantId, postId);

        boolean changed = bookmarkRepository.bookmark(resolvedTenantId, postId, request.tocUserId());
        return ApiResponse.success(new CommunityApiDtos.Response.ToggleResultResponse(changed));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.ToggleResultResponse> unbookmarkPost(Long tenantId,
                                                                                      Long postId,
                                                                                      Long tocUserId) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (!Objects.equals(tocUserId, subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "tocUserId does not match authenticated user");
        }
        requirePublishedPost(resolvedTenantId, postId);

        boolean changed = bookmarkRepository.unbookmark(resolvedTenantId, postId, tocUserId);
        return ApiResponse.success(new CommunityApiDtos.Response.ToggleResultResponse(changed));
    }

    @Override
    @RequireAnyRole({ROLE_TOC_USER, ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.ReportResponse> createReport(
            Long tenantId,
            CommunityApiDtos.Request.CreateReportRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        if (!Objects.equals(request.reporterTocUserId(), subject.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "reporterTocUserId does not match authenticated user");
        }

        Long postId = request.postId();
        Long commentId = request.commentId();
        if ((postId == null && commentId == null) || (postId != null && commentId != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either postId or commentId must be provided");
        }

        TocUser reporter = tocUserRepository.findById(resolvedTenantId, request.reporterTocUserId())
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ToC user not found or inactive"));

        Long effectivePostId = postId;
        if (postId != null) {
            requirePublishedPost(resolvedTenantId, postId);
        } else {
            CommunityPostComment comment = commentRepository.findById(resolvedTenantId, commentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment not found"));
            if (!STATUS_PUBLISHED.equalsIgnoreCase(comment.getStatus()) || comment.getDeletedAt() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment is not available");
            }
            effectivePostId = comment.getPostId();
            requirePublishedPost(resolvedTenantId, effectivePostId);
        }

        CommunityContentReport report = new CommunityContentReport();
        report.setTenantId(resolvedTenantId);
        report.setReporterTocUserId(reporter.getId());
        report.setPostId(postId);
        report.setCommentId(commentId);
        report.setReasonCode(request.reasonCode().trim());
        report.setReasonText(trimToNull(request.reasonText()));
        report.setStatus(STATUS_PENDING);
        report.setHandledBy(null);
        report.setHandledAt(null);
        report.setResultAction(null);
        report.setResultNote(null);
        report.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        reportRepository.save(report);

        if (effectivePostId != null) {
            postRepository.incrementReportCount(resolvedTenantId, effectivePostId);
        }

        CommunityContentReport created = reportRepository.findById(resolvedTenantId, report.getId()).orElse(report);
        return ApiResponse.success(toReportResponse(created));
    }

    @Override
    @RequireAnyRole({ROLE_SYSTEM_ADMIN})
    public ApiResponse<PagedResultResponse<CommunityApiDtos.Response.PostSummaryResponse>> listPostsForModeration(
            Long tenantId,
            CommunityApiDtos.Query.PostPageQuery query,
            PageQueryRequest pageQuery) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        int page = pageQuery != null && pageQuery.page() != null ? pageQuery.page() : DEFAULT_PAGE;
        int size = pageQuery != null && pageQuery.size() != null ? pageQuery.size() : DEFAULT_SIZE;
        long offset = (long) page * size;

        List<CommunityPostSummaryRow> rows = postRepository.listSummariesByConditionWithTotal(
                resolvedTenantId,
                query == null ? null : query.boardId(),
                query == null ? null : query.authorTocUserId(),
                trimToNull(query == null ? null : query.status()),
                trimToNull(query == null ? null : query.keyword()),
                true,
                offset,
                size
        );

        long total = rows.isEmpty() ? 0L : (rows.getFirst().getTotalCount() == null ? 0L : rows.getFirst().getTotalCount());
        List<CommunityApiDtos.Response.PostSummaryResponse> items = rows.stream()
                .map(this::toPostSummaryResponse)
                .toList();
        return ApiResponse.success(new PagedResultResponse<>(page, size, total, items));
    }

    @Override
    @RequireAnyRole({ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.PostDetailResponse> moderatePost(Long tenantId,
                                                                                  Long postId,
                                                                                  CommunityApiDtos.Request.ModeratePostRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        CommunityPost post = postRepository.findById(resolvedTenantId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        String status = request.status().trim();
        String reviewReason = trimToNull(request.reason());
        boolean ok = postRepository.updateModeration(resolvedTenantId, postId, status, reviewReason, subject.userId(), request.pinnedFlag());
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update post moderation");
        }
        CommunityPost updated = postRepository.findById(resolvedTenantId, postId).orElse(post);
        return ApiResponse.success(toPostDetailResponse(updated));
    }

    @Override
    @RequireAnyRole({ROLE_SYSTEM_ADMIN})
    public ApiResponse<PagedResultResponse<CommunityApiDtos.Response.ReportResponse>> listReports(
            Long tenantId,
            CommunityApiDtos.Query.ReportPageQuery query,
            PageQueryRequest pageQuery) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        int page = pageQuery != null && pageQuery.page() != null ? pageQuery.page() : DEFAULT_PAGE;
        int size = pageQuery != null && pageQuery.size() != null ? pageQuery.size() : DEFAULT_SIZE;
        long offset = (long) page * size;

        List<CommunityReportPageRow> rows = reportRepository.listByConditionWithTotal(
                resolvedTenantId,
                trimToNull(query == null ? null : query.status()),
                query == null ? null : query.createdFrom(),
                query == null ? null : query.createdTo(),
                offset,
                size
        );

        long total = rows.isEmpty() ? 0L : (rows.getFirst().getTotalCount() == null ? 0L : rows.getFirst().getTotalCount());
        List<CommunityApiDtos.Response.ReportResponse> items = rows.stream()
                .map(this::toReportResponse)
                .toList();
        return ApiResponse.success(new PagedResultResponse<>(page, size, total, items));
    }

    @Override
    @RequireAnyRole({ROLE_SYSTEM_ADMIN})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<CommunityApiDtos.Response.ReportResponse> moderateReport(Long tenantId,
                                                                                Long reportId,
                                                                                CommunityApiDtos.Request.ModerateReportRequest request) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        long resolvedTenantId = tenantAccessGuard.requireTenantMatch(tenantId, subject.tenantId());

        CommunityContentReport report = reportRepository.findById(resolvedTenantId, reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        String status = request.status().trim();
        String resultAction = trimToNull(request.resultAction());
        String resultNote = trimToNull(request.resultNote());

        boolean ok = reportRepository.updateModerationIfStatus(
                resolvedTenantId,
                reportId,
                report.getStatus(),
                status,
                resultAction,
                resultNote,
                subject.userId()
        );
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update report moderation");
        }

        if (resultAction != null) {
            switch (resultAction.trim().toUpperCase()) {
                case "NO_ACTION" -> {
                    // no-op
                }
                case "DELETE_POST" -> {
                    if (report.getPostId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report is not associated with a post");
                    }
                    postRepository.updateModeration(resolvedTenantId, report.getPostId(), STATUS_DELETED, "Deleted by report moderation", subject.userId(), null);
                }
                case "DELETE_COMMENT" -> {
                    if (report.getCommentId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report is not associated with a comment");
                    }
                    commentRepository.softDelete(resolvedTenantId, report.getCommentId());
                }
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported resultAction: " + resultAction);
            }
        }

        CommunityContentReport updated = reportRepository.findById(resolvedTenantId, reportId).orElse(report);
        return ApiResponse.success(toReportResponse(updated));
    }

    private void requirePublishedPost(Long tenantId, Long postId) {
        CommunityPost post = postRepository.findById(tenantId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (!STATUS_PUBLISHED.equalsIgnoreCase(post.getStatus()) || post.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not available");
        }
    }

    private boolean hasRole(UserSubject subject, String roleCode) {
        if (subject == null || subject.roleCodes() == null || subject.roleCodes().isEmpty()) {
            return false;
        }
        Set<String> normalized = subject.roleCodes().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(java.util.stream.Collectors.toSet());
        return normalized.contains(roleCode.toUpperCase());
    }

    private CommunityApiDtos.Response.BoardResponse toBoardResponse(CommunityBoard board) {
        return new CommunityApiDtos.Response.BoardResponse(
                board.getId(),
                board.getBoardCode(),
                board.getBoardName(),
                board.getDescription(),
                board.getDiseaseScope(),
                board.getStatus(),
                board.getSortOrder(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }

    private CommunityApiDtos.Response.PostSummaryResponse toPostSummaryResponse(CommunityPostSummaryRow row) {
        String displayName = Boolean.TRUE.equals(row.getAnonymousFlag()) ? "匿名用户" : row.getAuthorDisplayName();
        return new CommunityApiDtos.Response.PostSummaryResponse(
                row.getId(),
                row.getBoardId(),
                row.getAuthorTocUserId(),
                displayName,
                row.getAnonymousFlag(),
                row.getTitle(),
                row.getContentExcerpt(),
                row.getStatus(),
                row.getPinnedFlag(),
                row.getLikeCount(),
                row.getCommentCount(),
                row.getReportCount(),
                row.getLastActivityAt(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }

    private CommunityApiDtos.Response.PostDetailResponse toPostDetailResponse(CommunityPost post) {
        String displayName = Boolean.TRUE.equals(post.getAnonymousFlag()) ? "匿名用户" : post.getAuthorDisplayName();
        return new CommunityApiDtos.Response.PostDetailResponse(
                post.getId(),
                post.getBoardId(),
                post.getAuthorTocUserId(),
                displayName,
                post.getAnonymousFlag(),
                post.getTitle(),
                post.getContent(),
                post.getContentFormat(),
                post.getStatus(),
                post.getPinnedFlag(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getReportCount(),
                post.getLastActivityAt(),
                post.getReviewedBy(),
                post.getReviewedAt(),
                post.getReviewReason(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getDeletedAt()
        );
    }

    private CommunityApiDtos.Response.CommentResponse toCommentResponse(CommunityCommentPageRow row) {
        String displayName = Boolean.TRUE.equals(row.getAnonymousFlag()) ? "匿名用户" : row.getAuthorDisplayName();
        return new CommunityApiDtos.Response.CommentResponse(
                row.getId(),
                row.getPostId(),
                row.getParentCommentId(),
                row.getAuthorTocUserId(),
                displayName,
                row.getAnonymousFlag(),
                row.getContent(),
                row.getStatus(),
                row.getCreatedAt(),
                row.getUpdatedAt(),
                row.getDeletedAt()
        );
    }

    private CommunityApiDtos.Response.CommentResponse toCommentResponse(CommunityPostComment comment) {
        String displayName = Boolean.TRUE.equals(comment.getAnonymousFlag()) ? "匿名用户" : comment.getAuthorDisplayName();
        return new CommunityApiDtos.Response.CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getParentCommentId(),
                comment.getAuthorTocUserId(),
                displayName,
                comment.getAnonymousFlag(),
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getDeletedAt()
        );
    }

    private CommunityApiDtos.Response.ReportResponse toReportResponse(CommunityContentReport report) {
        return new CommunityApiDtos.Response.ReportResponse(
                report.getId(),
                report.getReporterTocUserId(),
                report.getPostId(),
                report.getCommentId(),
                report.getReasonCode(),
                report.getReasonText(),
                report.getStatus(),
                report.getHandledBy(),
                report.getHandledAt(),
                report.getResultAction(),
                report.getResultNote(),
                report.getCreatedAt()
        );
    }

    private CommunityApiDtos.Response.ReportResponse toReportResponse(CommunityReportPageRow row) {
        return new CommunityApiDtos.Response.ReportResponse(
                row.getId(),
                row.getReporterTocUserId(),
                row.getPostId(),
                row.getCommentId(),
                row.getReasonCode(),
                row.getReasonText(),
                row.getStatus(),
                row.getHandledBy(),
                row.getHandledAt(),
                row.getResultAction(),
                row.getResultNote(),
                row.getCreatedAt()
        );
    }

    private String defaultDisplayName(TocUser user) {
        if (user == null) {
            return "用户";
        }
        if (hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        return "用户" + user.getId();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
