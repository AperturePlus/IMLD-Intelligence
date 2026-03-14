package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;

/**
 * 患者 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    @Select({
            "<script>",
            "SELECT",
            " id,",
            " tenant_id,",
            " patient_no,",
            " patient_name,",
            " gender,",
            " birth_date,",
            " id_no_encrypted,",
            " mobile_encrypted,",
            " patient_type,",
            " status,",
            " source_channel,",
            " created_at,",
            " updated_at,",
            " COUNT(*) OVER() AS total_count",
            "FROM patient",
            "<where>",
            " tenant_id = #{tenantId}",
            " <if test='patientNo != null and patientNo.length() > 0'> AND patient_no = #{patientNo}</if>",
            " <if test='patientNameKeyword != null and patientNameKeyword.length() > 0'> AND patient_name LIKE CONCAT('%', #{patientNameKeyword}, '%')</if>",
            " <if test='patientType != null and patientType.length() > 0'> AND patient_type = #{patientType}</if>",
            " <if test='status != null and status.length() > 0'> AND status = #{status}</if>",
            "</where>",
            "ORDER BY id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<PatientPageRow> listByConditionWithTotal(@Param("tenantId") Long tenantId,
                                                   @Param("patientNo") String patientNo,
                                                   @Param("patientNameKeyword") String patientNameKeyword,
                                                   @Param("patientType") String patientType,
                                                   @Param("status") String status,
                                                   @Param("offset") long offset,
                                                   @Param("limit") int limit);
}
