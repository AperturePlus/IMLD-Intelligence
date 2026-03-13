package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.repository.ConsentRecordRepository;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentRecordServiceImplTest {

    @Mock private ConsentRecordRepository consentRecordRepository;

    private ConsentRecordServiceImpl consentRecordService;

    @BeforeEach
    void setUp() {
        consentRecordService = new ConsentRecordServiceImpl(consentRecordRepository);
    }

    @Test
    void countConsentsForwardsToRepository() {
        var query = new IdentityApiDtos.Query.ConsentRecordPageQuery(10L, "CLINICAL", null);
        when(consentRecordRepository.countByCondition(1L, 10L, "CLINICAL", null)).thenReturn(3L);

        assertThat(consentRecordService.countConsents(1L, query)).isEqualTo(3L);
    }

    @Test
    void listConsentsForwardsToRepository() {
        var query = new IdentityApiDtos.Query.ConsentRecordPageQuery(null, null, "VALID");
        ConsentRecord r = new ConsentRecord();
        r.setId(1L);
        when(consentRecordRepository.listByCondition(1L, null, null, "VALID", 0L, 20))
                .thenReturn(List.of(r));

        List<ConsentRecord> result = consentRecordService.listConsents(1L, query, 0L, 20);
        assertThat(result).hasSize(1);
    }

    @Test
    void upsertConsentCreatesNewRecord() {
        OffsetDateTime signedAt = OffsetDateTime.now();
        var request = new IdentityApiDtos.Request.UpsertConsentRecordRequest(
                10L, 20L, "CLINICAL", "v1.0", true, signedAt, null, "VALID", "remark");

        when(consentRecordRepository.save(any())).thenAnswer(invocation -> {
            ConsentRecord r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        ConsentRecord result = consentRecordService.upsertConsent(1L, request);

        assertThat(result.getId()).isEqualTo(100L);

        ArgumentCaptor<ConsentRecord> captor = ArgumentCaptor.forClass(ConsentRecord.class);
        verify(consentRecordRepository).save(captor.capture());
        ConsentRecord saved = captor.getValue();
        assertThat(saved.getTenantId()).isEqualTo(1L);
        assertThat(saved.getPatientId()).isEqualTo(10L);
        assertThat(saved.getTocUserId()).isEqualTo(20L);
        assertThat(saved.getConsentType()).isEqualTo("CLINICAL");
        assertThat(saved.getConsentVersion()).isEqualTo("v1.0");
        assertThat(saved.getSignedOffline()).isTrue();
        assertThat(saved.getStatus()).isEqualTo("VALID");
        assertThat(saved.getRemark()).isEqualTo("remark");
    }

    @Test
    void upsertConsentDefaultsSignedOfflineToTrue() {
        var request = new IdentityApiDtos.Request.UpsertConsentRecordRequest(
                10L, 20L, "TOC", "v2.0", null, null, null, "VALID", null);

        when(consentRecordRepository.save(any())).thenAnswer(i -> {
            ConsentRecord r = i.getArgument(0);
            r.setId(101L);
            return r;
        });

        ConsentRecord result = consentRecordService.upsertConsent(1L, request);

        assertThat(result.getSignedOffline()).isTrue();
    }
}
