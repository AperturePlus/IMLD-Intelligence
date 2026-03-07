package xenosoft.imldintelligence.module.audit.internal.service;

import xenosoft.imldintelligence.module.audit.internal.service.command.AuditRecordCommand;
import xenosoft.imldintelligence.module.audit.internal.service.command.ModelInvocationRecordCommand;
import xenosoft.imldintelligence.module.audit.internal.service.command.SensitiveAccessRecordCommand;
import xenosoft.imldintelligence.common.model.AuditLog;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;
import xenosoft.imldintelligence.common.model.SensitiveDataAccessLog;

public interface AuditTrailService {
    AuditLog recordAudit(AuditRecordCommand command);

    SensitiveDataAccessLog recordSensitiveAccess(SensitiveAccessRecordCommand command);

    ModelInvocationLog recordModelInvocation(ModelInvocationRecordCommand command);
}
