package xenosoft.imldintelligence.module.community.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPost;

import java.util.List;

@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {

    @Select({
            "<script>",
            "SELECT",
            " p.id,",
            " p.tenant_id,",
            " p.board_id,",
            " p.author_toc_user_id,",
            " p.author_display_name,",
            " p.anonymous_flag,",
            " p.title,",
            " LEFT(p.content, 200) AS content_excerpt,",
            " p.status,",
            " p.pinned_flag,",
            " p.like_count,",
            " p.comment_count,",
            " p.report_count,",
            " p.last_activity_at,",
            " p.created_at,",
            " p.updated_at,",
            " p.deleted_at,",
            " COUNT(*) OVER() AS total_count",
            "FROM community_post p",
            "<where>",
            " p.tenant_id = #{tenantId}",
            " <if test='boardId != null'> AND p.board_id = #{boardId}</if>",
            " <if test='authorTocUserId != null'> AND p.author_toc_user_id = #{authorTocUserId}</if>",
            " <if test='status != null and status.length() > 0'> AND p.status = #{status}</if>",
            " <if test='keyword != null and keyword.length() > 0'>",
            "   AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%'))",
            " </if>",
            " <if test='excludeDeleted'> AND p.deleted_at IS NULL AND p.status &lt;&gt; 'DELETED'</if>",
            "</where>",
            "ORDER BY p.pinned_flag DESC, COALESCE(p.last_activity_at, p.created_at) DESC, p.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<CommunityPostSummaryRow> listSummariesByConditionWithTotal(@Param("tenantId") Long tenantId,
                                                                     @Param("boardId") Long boardId,
                                                                     @Param("authorTocUserId") Long authorTocUserId,
                                                                     @Param("status") String status,
                                                                     @Param("keyword") String keyword,
                                                                     @Param("excludeDeleted") boolean excludeDeleted,
                                                                     @Param("offset") long offset,
                                                                     @Param("limit") int limit);

    @Update("UPDATE community_post SET like_count = like_count + 1, updated_at = now(), last_activity_at = now() " +
            "WHERE tenant_id = #{tenantId} AND id = #{postId} AND deleted_at IS NULL AND status <> 'DELETED'")
    int incrementLikeCount(@Param("tenantId") Long tenantId, @Param("postId") Long postId);

    @Update("UPDATE community_post SET like_count = GREATEST(like_count - 1, 0), updated_at = now(), last_activity_at = now() " +
            "WHERE tenant_id = #{tenantId} AND id = #{postId} AND deleted_at IS NULL AND status <> 'DELETED'")
    int decrementLikeCount(@Param("tenantId") Long tenantId, @Param("postId") Long postId);

    @Update("UPDATE community_post SET comment_count = comment_count + 1, updated_at = now(), last_activity_at = now() " +
            "WHERE tenant_id = #{tenantId} AND id = #{postId} AND deleted_at IS NULL AND status <> 'DELETED'")
    int incrementCommentCount(@Param("tenantId") Long tenantId, @Param("postId") Long postId);

    @Update("UPDATE community_post SET report_count = report_count + 1, updated_at = now() " +
            "WHERE tenant_id = #{tenantId} AND id = #{postId} AND deleted_at IS NULL AND status <> 'DELETED'")
    int incrementReportCount(@Param("tenantId") Long tenantId, @Param("postId") Long postId);

    @Update({
            "<script>",
            "UPDATE community_post",
            "SET status = #{status},",
            "    pinned_flag = COALESCE(#{pinnedFlag}, pinned_flag),",
            "    review_reason = #{reviewReason},",
            "    reviewed_by = #{reviewedBy},",
            "    reviewed_at = now(),",
            "    updated_at = now(),",
            "    deleted_at = CASE WHEN #{status} = 'DELETED' THEN now() ELSE deleted_at END",
            "WHERE tenant_id = #{tenantId} AND id = #{postId}",
            "</script>"
    })
    int updateModeration(@Param("tenantId") Long tenantId,
                         @Param("postId") Long postId,
                         @Param("status") String status,
                         @Param("reviewReason") String reviewReason,
                         @Param("reviewedBy") Long reviewedBy,
                         @Param("pinnedFlag") Boolean pinnedFlag);
}

