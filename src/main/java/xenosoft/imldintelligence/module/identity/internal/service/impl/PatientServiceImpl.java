package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientExternalIdRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.service.PatientService;
import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientExternalIdRepository patientExternalIdRepository;
    private final SensitiveDataEncryptor sensitiveDataEncryptor;

    @Override
    public long countPatients(Long tenantId, IdentityApiDtos.Query.PatientPageQuery query) {
        return patientRepository.countByCondition(tenantId,
                query.patientNo(), query.patientNameKeyword(),
                query.patientType(), query.status());
    }

    @Override
    public List<Patient> listPatients(Long tenantId, IdentityApiDtos.Query.PatientPageQuery query,
                                       long offset, int limit) {
        return patientRepository.listByCondition(tenantId,
                query.patientNo(), query.patientNameKeyword(),
                query.patientType(), query.status(),
                offset, limit);
    }

    @Override
    public Patient createPatient(Long tenantId, IdentityApiDtos.Request.CreatePatientRequest request) {
        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setPatientNo(request.patientNo());
        patient.setPatientName(request.patientName());
        patient.setGender(request.gender());
        patient.setBirthDate(request.birthDate());
        patient.setPatientType(request.patientType());
        patient.setStatus("ACTIVE");
        patient.setSourceChannel(request.sourceChannel() != null ? request.sourceChannel() : "HOSPITAL");

        if (request.idNoPlaintext() != null && !request.idNoPlaintext().isBlank()) {
            patient.setIdNoEncrypted(sensitiveDataEncryptor.encrypt(request.idNoPlaintext()));
        }
        if (request.mobilePlaintext() != null && !request.mobilePlaintext().isBlank()) {
            patient.setMobileEncrypted(sensitiveDataEncryptor.encrypt(request.mobilePlaintext()));
        }

        return patientRepository.save(patient);
    }

    @Override
    public Patient bindExternalId(Long tenantId, Long patientId,
                                   IdentityApiDtos.Request.BindExternalIdRequest request) {
        Patient patient = patientRepository.findById(tenantId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        PatientExternalId externalId = new PatientExternalId();
        externalId.setTenantId(tenantId);
        externalId.setPatientId(patientId);
        externalId.setIdType(request.idType());
        externalId.setIdValue(request.idValue());
        externalId.setSourceOrg(request.sourceOrg());
        externalId.setIsPrimary(request.primary() != null ? request.primary() : false);

        patientExternalIdRepository.save(externalId);

        return patient;
    }
}
