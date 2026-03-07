package xenosoft.imldintelligence.module.identity.internal.dto;

import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;

import java.util.LinkedHashSet;
import java.util.Set;

public final class IdentityDtoMapper {
    private IdentityDtoMapper() {
    }

    public static AuthenticatedUserDto toAuthenticatedUserDto(Tenant tenant, UserAccount userAccount, Set<String> roleCodes) {
        if (tenant == null || userAccount == null) {
            return null;
        }
        return new AuthenticatedUserDto(
                userAccount.getId(),
                userAccount.getTenantId(),
                tenant.getTenantCode(),
                userAccount.getUsername(),
                userAccount.getDisplayName(),
                userAccount.getUserType(),
                userAccount.getDeptName(),
                copySet(roleCodes),
                userAccount.getLastLoginAt()
        );
    }

    public static TenantDto toDto(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        return new TenantDto(
                tenant.getId(),
                tenant.getTenantCode(),
                tenant.getTenantName(),
                tenant.getDeployMode(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }

    public static RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleDto(
                role.getId(),
                role.getTenantId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus(),
                role.getCreatedAt()
        );
    }

    public static UserAccountDto toDto(UserAccount userAccount, Set<String> roleCodes) {
        if (userAccount == null) {
            return null;
        }
        return new UserAccountDto(
                userAccount.getId(),
                userAccount.getTenantId(),
                userAccount.getUserNo(),
                userAccount.getUsername(),
                userAccount.getDisplayName(),
                userAccount.getUserType(),
                userAccount.getDeptName(),
                hasText(userAccount.getEmail()),
                hasText(userAccount.getMobileEncrypted()),
                userAccount.getStatus(),
                userAccount.getLastLoginAt(),
                userAccount.getCreatedAt(),
                userAccount.getUpdatedAt(),
                copySet(roleCodes)
        );
    }

    public static UserSubjectDto toDto(UserSubject userSubject) {
        if (userSubject == null) {
            return null;
        }
        return new UserSubjectDto(
                userSubject.userId(),
                userSubject.tenantId(),
                userSubject.userType(),
                userSubject.deptName(),
                copySet(userSubject.roleCodes())
        );
    }

    public static PermissionDecisionDto toPermissionDecisionDto(Long tenantId,
                                                                Long userId,
                                                                String resourceType,
                                                                String action,
                                                                boolean allowed,
                                                                Set<String> roleCodes,
                                                                String evaluationMode) {
        return new PermissionDecisionDto(
                tenantId,
                userId,
                resourceType,
                action,
                allowed,
                copySet(roleCodes),
                evaluationMode
        );
    }

    public static PatientDto toDto(Patient patient) {
        if (patient == null) {
            return null;
        }
        return new PatientDto(
                patient.getId(),
                patient.getTenantId(),
                patient.getPatientNo(),
                patient.getPatientName(),
                patient.getGender(),
                patient.getBirthDate(),
                patient.getPatientType(),
                patient.getStatus(),
                patient.getSourceChannel(),
                hasText(patient.getIdNoEncrypted()),
                hasText(patient.getMobileEncrypted()),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }

    public static PatientExternalIdDto toDto(PatientExternalId patientExternalId) {
        if (patientExternalId == null) {
            return null;
        }
        return new PatientExternalIdDto(
                patientExternalId.getId(),
                patientExternalId.getTenantId(),
                patientExternalId.getPatientId(),
                patientExternalId.getIdType(),
                maskIdentifier(patientExternalId.getIdValue()),
                patientExternalId.getSourceOrg(),
                patientExternalId.getIsPrimary(),
                patientExternalId.getCreatedAt()
        );
    }

    public static TocUserDto toDto(TocUser tocUser) {
        if (tocUser == null) {
            return null;
        }
        return new TocUserDto(
                tocUser.getId(),
                tocUser.getTenantId(),
                tocUser.getTocUid(),
                tocUser.getNickname(),
                tocUser.getVipStatus(),
                tocUser.getStatus(),
                hasText(tocUser.getMobileEncrypted()),
                hasText(tocUser.getOpenid()),
                hasText(tocUser.getUnionid()),
                tocUser.getCreatedAt(),
                tocUser.getUpdatedAt()
        );
    }

    public static GuardianRelationDto toDto(GuardianRelation guardianRelation) {
        if (guardianRelation == null) {
            return null;
        }
        return new GuardianRelationDto(
                guardianRelation.getId(),
                guardianRelation.getTenantId(),
                guardianRelation.getPatientId(),
                guardianRelation.getGuardianName(),
                guardianRelation.getRelationType(),
                guardianRelation.getIsPrimary(),
                guardianRelation.getStatus(),
                hasText(guardianRelation.getGuardianMobileEncrypted()),
                hasText(guardianRelation.getGuardianIdNoEncrypted()),
                guardianRelation.getCreatedAt()
        );
    }

    public static ConsentRecordDto toDto(ConsentRecord consentRecord) {
        if (consentRecord == null) {
            return null;
        }
        return new ConsentRecordDto(
                consentRecord.getId(),
                consentRecord.getTenantId(),
                consentRecord.getPatientId(),
                consentRecord.getTocUserId(),
                consentRecord.getConsentType(),
                consentRecord.getConsentVersion(),
                consentRecord.getSignedOffline(),
                consentRecord.getSignedAt(),
                consentRecord.getFileId(),
                consentRecord.getStatus(),
                consentRecord.getRemark(),
                consentRecord.getCreatedAt()
        );
    }

    public static EncounterDto toDto(Encounter encounter) {
        if (encounter == null) {
            return null;
        }
        return new EncounterDto(
                encounter.getId(),
                encounter.getTenantId(),
                encounter.getPatientId(),
                encounter.getEncounterNo(),
                encounter.getEncounterType(),
                encounter.getDeptName(),
                encounter.getAttendingDoctorId(),
                encounter.getStartAt(),
                encounter.getEndAt(),
                encounter.getSourceSystem(),
                encounter.getCreatedAt()
        );
    }

    public static AbacPolicyDto toDto(AbacPolicy abacPolicy) {
        if (abacPolicy == null) {
            return null;
        }
        return new AbacPolicyDto(
                abacPolicy.getId(),
                abacPolicy.getTenantId(),
                abacPolicy.getPolicyCode(),
                abacPolicy.getPolicyName(),
                abacPolicy.getSubjectExpr(),
                abacPolicy.getResourceExpr(),
                abacPolicy.getActionExpr(),
                abacPolicy.getEffect(),
                abacPolicy.getPriority(),
                abacPolicy.getStatus(),
                abacPolicy.getCreatedAt()
        );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Set<String> copySet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(new LinkedHashSet<>(values));
    }

    private static String maskIdentifier(String value) {
        if (!hasText(value)) {
            return null;
        }
        int length = value.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        int prefix = Math.min(3, Math.max(1, length / 4));
        int suffix = Math.min(2, Math.max(1, length / 5));
        int maskedLength = Math.max(1, length - prefix - suffix);
        return value.substring(0, prefix)
                + "*".repeat(maskedLength)
                + value.substring(length - suffix);
    }
}
