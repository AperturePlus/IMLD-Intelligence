CREATE INDEX IF NOT EXISTS idx_patient_tenant_status ON patient(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_patient_external_id_patient ON patient_external_id(patient_id);
CREATE INDEX IF NOT EXISTS idx_encounter_patient_time ON encounter(patient_id, start_at DESC);

CREATE INDEX IF NOT EXISTS idx_lab_result_patient_indicator_time ON lab_result(patient_id, indicator_code, collected_at DESC);
CREATE INDEX IF NOT EXISTS idx_lab_result_encounter ON lab_result(encounter_id);
CREATE INDEX IF NOT EXISTS idx_genetic_report_patient_time ON genetic_report(patient_id, report_date DESC);
CREATE INDEX IF NOT EXISTS idx_genetic_variant_report_gene ON genetic_variant(report_id, gene);

CREATE INDEX IF NOT EXISTS idx_diag_session_patient_time ON diagnosis_session(patient_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_diag_result_session_rank ON diagnosis_result(session_id, rank_no);
CREATE INDEX IF NOT EXISTS idx_report_patient_time ON report(patient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_report_version_report_ver ON report_version(report_id, version_num DESC);

CREATE INDEX IF NOT EXISTS idx_followup_patient_status_time ON followup_task(patient_id, status, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_alert_patient_status_time ON alert_event(patient_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_questionnaire_response_user_time ON questionnaire_response(toc_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_vip_order_user_time ON vip_order(toc_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_receiver_status ON notification_message(receiver_type, receiver_ref_id, status);

CREATE INDEX IF NOT EXISTS idx_integration_job_source_time ON integration_job(source_system, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_resource_time ON audit_log(resource_type, resource_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_sensitive_access_resource_time ON sensitive_data_access_log(resource_type, resource_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_model_invocation_session_time ON model_invocation_log(session_id, created_at DESC);
