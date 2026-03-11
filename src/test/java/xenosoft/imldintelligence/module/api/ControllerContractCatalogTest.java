package xenosoft.imldintelligence.module.api;

import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.careplan.internal.api.CarePlanApi;
import xenosoft.imldintelligence.module.careplan.internal.api.CarePlanControllerContract;
import xenosoft.imldintelligence.module.clinical.internal.api.ClinicalApi;
import xenosoft.imldintelligence.module.clinical.internal.api.ClinicalControllerContract;
import xenosoft.imldintelligence.module.diagnoses.internal.api.DiagnosesApi;
import xenosoft.imldintelligence.module.diagnoses.internal.api.DiagnosesControllerContract;
import xenosoft.imldintelligence.module.identity.internal.api.IdentityApi;
import xenosoft.imldintelligence.module.identity.internal.api.IdentityControllerContract;
import xenosoft.imldintelligence.module.integration.internal.api.IntegrationApi;
import xenosoft.imldintelligence.module.integration.internal.api.IntegrationControllerContract;
import xenosoft.imldintelligence.module.license.internal.api.LicenseApi;
import xenosoft.imldintelligence.module.license.internal.api.LicenseControllerContract;
import xenosoft.imldintelligence.module.notify.internal.api.NotifyApi;
import xenosoft.imldintelligence.module.notify.internal.api.NotifyControllerContract;
import xenosoft.imldintelligence.module.payment.internal.api.PaymentApi;
import xenosoft.imldintelligence.module.payment.internal.api.PaymentControllerContract;
import xenosoft.imldintelligence.module.report.internal.api.ReportApi;
import xenosoft.imldintelligence.module.report.internal.api.ReportControllerContract;
import xenosoft.imldintelligence.module.screening.internal.api.ScreeningApi;
import xenosoft.imldintelligence.module.screening.internal.api.ScreeningControllerContract;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 控制器契约目录测试。
 *
 * <p>所有未实现控制器都应先落到 controller contract 层，再由具体控制器类实现。</p>
 */
class ControllerContractCatalogTest {

    @Test
    void everyControllerContractShouldExtendItsHttpApiContract() {
        assertThat(CarePlanApi.class).isAssignableFrom(CarePlanControllerContract.class);
        assertThat(ClinicalApi.class).isAssignableFrom(ClinicalControllerContract.class);
        assertThat(DiagnosesApi.class).isAssignableFrom(DiagnosesControllerContract.class);
        assertThat(IdentityApi.class).isAssignableFrom(IdentityControllerContract.class);
        assertThat(IntegrationApi.class).isAssignableFrom(IntegrationControllerContract.class);
        assertThat(LicenseApi.class).isAssignableFrom(LicenseControllerContract.class);
        assertThat(NotifyApi.class).isAssignableFrom(NotifyControllerContract.class);
        assertThat(PaymentApi.class).isAssignableFrom(PaymentControllerContract.class);
        assertThat(ReportApi.class).isAssignableFrom(ReportControllerContract.class);
        assertThat(ScreeningApi.class).isAssignableFrom(ScreeningControllerContract.class);
    }
}
