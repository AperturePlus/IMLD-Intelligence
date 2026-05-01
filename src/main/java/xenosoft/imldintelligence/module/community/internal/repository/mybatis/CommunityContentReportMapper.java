package xenosoft.imldintelligence.module.community.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import xenosoft.imldintelligence.module.community.internal.model.CommunityContentReport;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface CommunityContentReportMapper extends BaseMapper<CommunityContentReport> {

    @Select({
            "<script>",
            "SELECT",
            " r.id,",
            " r.tenant_id,",
            " r.reporter_toc_user_id,",
            " r.post_id,",
            " r.comment_id,",
            " r.reason_code,",
            " r.reason_text,",
            " r.status,",
            " r.handled_by,",
            " r.handled_at,",
            " r.result_action,",
            " r.result_note,",
            " r.created_at,",
            " COUNT(*) OVER() AS total_count",
            "FROM community_content_report r",
            "<where>",
            " r.tenant_id = #{tenantId}",
            " <if test='status != null and status.length() > 0'> AND r.status = #{status}</if>",
            " <if test='createdFrom != null'> AND r.created_at &gt;= #{createdFrom}</if>",
            " <if test='createdTo != null'> AND r.created_at &lt;= #{createdTo}</if>",
            "</where>",
            "ORDER BY r.created_at DESC, r.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<CommunityReportPageRow> listByConditionWithTotal(@Param("tenantId") Long tenantId,
                                                          @Param("status") String status,
                                                          @Param("createdFrom") OffsetDateTime createdFrom,
                                                          @Param("createdTo") OffsetDateTime createdTo,
                                                          @Param("offset") long offset,
                                                          @Param("limit") int limit);

    @Update({
            "<script>",
            "UPDATE community_content_report",
            "SET status = #{status},",
            "    result_action = #{resultAction},",
            "    result_note = #{resultNote},",
            "    handled_by = #{handledBy},",
            "    handled_at = now()",
            "WHERE tenant_id = #{tenantId} AND id = #{reportId}",
            "</script>"
    })
    int updateModeration(@Param("tenantId") Long tenantId,
                         @Param("reportId") Long reportId,
                         @Param("status") String status,
                         @Param("resultAction") String resultAction,
                         @Param("resultNote") String resultNote,
                         @Param("handledBy") Long handledBy);
}

