package xenosoft.imldintelligence.module.identity.internal.dto;

import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityDtoMapperTest {

    @Test
    void mapsAuthenticatedUserWithTenantContext() {
        Tenant tenant = new Tenant();
        tenant.setId(10L);
        tenant.setTenantCode("TENANT_A");

        UserAccount userAccount = new UserAccount();
        userAccount.setId(20L);
        userAccount.setTenantId(10L);
        userAccount.setUsername("doctor.a");
        userAccount.setDisplayName("Doctor A");
        userAccount.setUserType("DOCTOR");
        userAccount.setDeptName("Cardiology");
        userAccount.setLastLoginAt(OffsetDateTime.parse("2026-03-06T12:00:00+08:00"));

        AuthenticatedUserDto dto = IdentityDtoMapper.toAuthenticatedUserDto(
                tenant,
                userAccount,
                new LinkedHashSet<>(Set.of("DOCTOR", "CASE_MANAGER"))
        );

        assertThat(dto.tenantCode()).isEqualTo("TENANT_A");
        assertThat(dto.username()).isEqualTo("doctor.a");
        assertThat(dto.roleCodes()).containsExactlyInAnyOrder("DOCTOR", "CASE_MANAGER");
    }

    @Test
    void mapsSensitiveBindingsWithoutLeakingCiphertext() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1L);
        userAccount.setTenantId(2L);
        userAccount.setUserNo("U-01");
        userAccount.setUsername("admin");
        userAccount.setDisplayName("Admin");
        userAccount.setUserType("ADMIN");
        userAccount.setEmail("admin@example.com");
        userAccount.setMobileEncrypted("cipher-mobile");

        UserAccountDto dto = IdentityDtoMapper.toDto(userAccount, Set.of("SYSTEM_ADMIN"));

        assertThat(dto.emailBound()).isTrue();
        assertThat(dto.mobileBound()).isTrue();
        assertThat(dto.roleCodes()).containsExactly("SYSTEM_ADMIN");
    }

    @Test
    void masksExternalIdentifiersAndCopiesSubjectRoles() {
        PatientExternalId externalId = new PatientExternalId();
        externalId.setId(30L);
        externalId.setTenantId(40L);
        externalId.setPatientId(50L);
        externalId.setIdType("ID_CARD");
        externalId.setIdValue("310101199001011234");
        externalId.setSourceOrg("HIS");
        externalId.setIsPrimary(true);

        PatientExternalIdDto idDto = IdentityDtoMapper.toDto(externalId);
        assertThat(idDto.idValueMasked()).startsWith("310");
        assertThat(idDto.idValueMasked()).endsWith("34");
        assertThat(idDto.idValueMasked()).doesNotContain("1990010112");

        UserSubject subject = new UserSubject(1L, 2L, "DOCTOR", "Oncology", Set.of("DOCTOR"));
        UserSubjectDto subjectDto = IdentityDtoMapper.toDto(subject);
        assertThat(subjectDto.roleCodes()).containsExactly("DOCTOR");
    }
}
