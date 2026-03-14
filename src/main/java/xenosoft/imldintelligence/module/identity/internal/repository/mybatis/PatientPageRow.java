package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import lombok.Data;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 患者分页查询行，承载窗口函数总数列，避免重复 count SQL。
 */
@Data
public class PatientPageRow {
    private Long id;
    private Long tenantId;
    private String patientNo;
    private String patientName;
    private String gender;
    private LocalDate birthDate;
    private String idNoEncrypted;
    private String mobileEncrypted;
    private String patientType;
    private String status;
    private String sourceChannel;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long totalCount;

    public Patient toPatient() {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setTenantId(tenantId);
        patient.setPatientNo(patientNo);
        patient.setPatientName(patientName);
        patient.setGender(gender);
        patient.setBirthDate(birthDate);
        patient.setIdNoEncrypted(idNoEncrypted);
        patient.setMobileEncrypted(mobileEncrypted);
        patient.setPatientType(patientType);
        patient.setStatus(status);
        patient.setSourceChannel(sourceChannel);
        patient.setCreatedAt(createdAt);
        patient.setUpdatedAt(updatedAt);
        return patient;
    }
}
