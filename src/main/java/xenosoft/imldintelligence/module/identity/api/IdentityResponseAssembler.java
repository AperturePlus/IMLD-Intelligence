package xenosoft.imldintelligence.module.identity.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.repository.GuardianRelationRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientExternalIdRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IdentityResponseAssembler {

    private final PatientExternalIdRepository patientExternalIdRepository;
    private final GuardianRelationRepository guardianRelationRepository;
    private final UserRoleRelRepository userRoleRelRepository;
    private final RoleRepository roleRepository;
    private final SensitiveDataEncryptor sensitiveDataEncryptor;

    public IdentityApiDtos.Response.PatientDetailResponse toPatientDetail(Patient patient) {
        Long tenantId = patient.getTenantId();
        Long patientId = patient.getId();

        List<IdentityApiDtos.Shared.ExternalIdItem> externalIds =
                patientExternalIdRepository.listByPatientId(tenantId, patientId)
                        .stream()
                        .map(this::toExternalIdItem)
                        .toList();

        List<IdentityApiDtos.Shared.GuardianItem> guardians =
                guardianRelationRepository.listByPatientId(tenantId, patientId)
                        .stream()
                        .map(this::toGuardianItem)
                        .toList();

        String idNoMasked = decryptAndMask(patient.getIdNoEncrypted(),
                SensitiveDataEncryptor.MaskType.ID_CARD);
        String mobileMasked = decryptAndMask(patient.getMobileEncrypted(),
                SensitiveDataEncryptor.MaskType.MOBILE);

        return new IdentityApiDtos.Response.PatientDetailResponse(
                patient.getId(),
                patient.getPatientNo(),
                patient.getPatientName(),
                patient.getGender(),
                patient.getBirthDate(),
                idNoMasked,
                mobileMasked,
                patient.getPatientType(),
                patient.getStatus(),
                patient.getSourceChannel(),
                externalIds,
                guardians,
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }

    public IdentityApiDtos.Response.UserAccountResponse toUserAccountResponse(UserAccount user) {
        Long tenantId = user.getTenantId();
        Long userId = user.getId();

        List<IdentityApiDtos.Shared.RoleItem> roles =
                userRoleRelRepository.listByUserId(tenantId, userId)
                        .stream()
                        .map(UserRoleRel::getRoleId)
                        .filter(Objects::nonNull)
                        .map(roleId -> roleRepository.findById(tenantId, roleId).orElse(null))
                        .filter(Objects::nonNull)
                        .map(this::toRoleItem)
                        .toList();

        return new IdentityApiDtos.Response.UserAccountResponse(
                user.getId(),
                user.getUserNo(),
                user.getUsername(),
                user.getDisplayName(),
                user.getUserType(),
                user.getDeptName(),
                user.getEmail(),
                user.getStatus(),
                user.getLastLoginAt(),
                roles
        );
    }

    public IdentityApiDtos.Response.ConsentRecordResponse toConsentRecordResponse(ConsentRecord record) {
        return new IdentityApiDtos.Response.ConsentRecordResponse(
                record.getId(),
                record.getPatientId(),
                record.getTocUserId(),
                record.getConsentType(),
                record.getConsentVersion(),
                record.getSignedOffline(),
                record.getSignedAt(),
                record.getFileId(),
                record.getStatus(),
                record.getRemark(),
                record.getCreatedAt()
        );
    }

    public IdentityApiDtos.Shared.AuthenticatedUserItem toAuthenticatedUserItem(
            UserAccount user, Set<String> roleCodes) {
        return new IdentityApiDtos.Shared.AuthenticatedUserItem(
                user.getId(),
                user.getTenantId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getUserType(),
                roleCodes != null ? List.copyOf(roleCodes) : List.of()
        );
    }

    private IdentityApiDtos.Shared.ExternalIdItem toExternalIdItem(PatientExternalId ext) {
        return new IdentityApiDtos.Shared.ExternalIdItem(
                ext.getId(),
                ext.getIdType(),
                maskGeneral(ext.getIdValue()),
                ext.getSourceOrg(),
                ext.getIsPrimary()
        );
    }

    private IdentityApiDtos.Shared.GuardianItem toGuardianItem(GuardianRelation g) {
        String mobileMasked = decryptAndMask(g.getGuardianMobileEncrypted(),
                SensitiveDataEncryptor.MaskType.MOBILE);
        String idNoMasked = decryptAndMask(g.getGuardianIdNoEncrypted(),
                SensitiveDataEncryptor.MaskType.ID_CARD);

        return new IdentityApiDtos.Shared.GuardianItem(
                g.getId(),
                g.getGuardianName(),
                mobileMasked,
                idNoMasked,
                g.getRelationType(),
                g.getIsPrimary(),
                g.getStatus()
        );
    }

    private IdentityApiDtos.Shared.RoleItem toRoleItem(Role role) {
        return new IdentityApiDtos.Shared.RoleItem(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getStatus()
        );
    }

    private String decryptAndMask(String encryptedValue, SensitiveDataEncryptor.MaskType type) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return null;
        }
        try {
            String plaintext = sensitiveDataEncryptor.decrypt(encryptedValue);
            return sensitiveDataEncryptor.mask(plaintext, type);
        } catch (Exception e) {
            // If decryption fails (e.g., key not configured), treat as already masked
            return maskGeneral(encryptedValue);
        }
    }

    private String maskGeneral(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return sensitiveDataEncryptor.mask(value, SensitiveDataEncryptor.MaskType.GENERAL);
    }
}
