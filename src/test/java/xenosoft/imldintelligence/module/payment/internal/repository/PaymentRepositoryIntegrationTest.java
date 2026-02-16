package xenosoft.imldintelligence.module.payment.internal.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.payment.internal.model.VipOrder;
import xenosoft.imldintelligence.module.payment.internal.model.VipPlan;
import xenosoft.imldintelligence.module.payment.internal.model.VipSubscription;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class PaymentRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TocUserRepository tocUserRepository;

    @Autowired
    private VipPlanRepository vipPlanRepository;

    @Autowired
    private VipOrderRepository vipOrderRepository;

    @Autowired
    private VipSubscriptionRepository vipSubscriptionRepository;

    @Test
    void vipPlanCrudAndTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        VipPlan vipPlan = new VipPlan();
        vipPlan.setTenantId(tenantA.getId());
        vipPlan.setPlanCode("PLAN_" + unique("code"));
        vipPlan.setPlanName("Plan");
        vipPlan.setDurationDays(30);
        vipPlan.setPriceAmount(new BigDecimal("199.00"));
        vipPlan.setCurrencyCode("CNY");
        vipPlan.setStatus("ACTIVE");
        vipPlanRepository.save(vipPlan);

        assertThat(vipPlan.getId()).isNotNull();
        assertThat(vipPlanRepository.findById(tenantA.getId(), vipPlan.getId())).isPresent();
        assertThat(vipPlanRepository.findById(tenantB.getId(), vipPlan.getId())).isEmpty();
        assertThat(vipPlanRepository.findByPlanCode(tenantA.getId(), vipPlan.getPlanCode())).isPresent();
        assertThat(vipPlanRepository.listByTenantId(tenantA.getId())).extracting(VipPlan::getId).contains(vipPlan.getId());

        vipPlan.setPlanName("Plan Updated");
        vipPlanRepository.update(vipPlan);
        assertThat(vipPlanRepository.findById(tenantA.getId(), vipPlan.getId())).get().extracting(VipPlan::getPlanName).isEqualTo("Plan Updated");

        assertThat(vipPlanRepository.deleteById(tenantA.getId(), vipPlan.getId())).isTrue();
        assertThat(vipPlanRepository.findById(tenantA.getId(), vipPlan.getId())).get().extracting(VipPlan::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void vipOrderCrud() {
        Tenant tenant = createTenant();
        TocUser tocUser = createTocUser(tenant.getId());
        VipPlan vipPlan = createVipPlan(tenant.getId());

        VipOrder vipOrder = new VipOrder();
        vipOrder.setTenantId(tenant.getId());
        vipOrder.setOrderNo("ORD_" + unique("no"));
        vipOrder.setTocUserId(tocUser.getId());
        vipOrder.setPlanId(vipPlan.getId());
        vipOrder.setOrderStatus("CREATED");
        vipOrder.setAmount(new BigDecimal("199.00"));
        vipOrder.setChannel("WECHAT");
        vipOrderRepository.save(vipOrder);

        assertThat(vipOrder.getId()).isNotNull();
        assertThat(vipOrderRepository.findById(tenant.getId(), vipOrder.getId())).isPresent();
        assertThat(vipOrderRepository.findByOrderNo(tenant.getId(), vipOrder.getOrderNo())).isPresent();
        assertThat(vipOrderRepository.listByTenantId(tenant.getId())).extracting(VipOrder::getId).contains(vipOrder.getId());
        assertThat(vipOrderRepository.listByTocUserId(tenant.getId(), tocUser.getId())).extracting(VipOrder::getId).contains(vipOrder.getId());

        vipOrder.setOrderStatus("PAID");
        vipOrder.setPaidAt(OffsetDateTime.now().withNano(0));
        vipOrderRepository.update(vipOrder);
        assertThat(vipOrderRepository.findById(tenant.getId(), vipOrder.getId())).get().extracting(VipOrder::getOrderStatus).isEqualTo("PAID");

        assertThat(vipOrderRepository.deleteById(tenant.getId(), vipOrder.getId())).isTrue();
        assertThat(vipOrderRepository.findById(tenant.getId(), vipOrder.getId())).isEmpty();
    }

    @Test
    void vipSubscriptionCrud() {
        Tenant tenant = createTenant();
        TocUser tocUser = createTocUser(tenant.getId());
        VipPlan vipPlan = createVipPlan(tenant.getId());
        VipOrder vipOrder = createVipOrder(tenant.getId(), tocUser.getId(), vipPlan.getId());

        VipSubscription vipSubscription = new VipSubscription();
        vipSubscription.setTenantId(tenant.getId());
        vipSubscription.setTocUserId(tocUser.getId());
        vipSubscription.setPlanId(vipPlan.getId());
        vipSubscription.setOrderId(vipOrder.getId());
        vipSubscription.setSubscriptionStatus("ACTIVE");
        vipSubscription.setStartAt(OffsetDateTime.now().withNano(0));
        vipSubscription.setEndAt(OffsetDateTime.now().plusDays(30).withNano(0));
        vipSubscriptionRepository.save(vipSubscription);

        assertThat(vipSubscription.getId()).isNotNull();
        assertThat(vipSubscriptionRepository.findById(tenant.getId(), vipSubscription.getId())).isPresent();
        assertThat(vipSubscriptionRepository.listByTenantId(tenant.getId())).extracting(VipSubscription::getId).contains(vipSubscription.getId());
        assertThat(vipSubscriptionRepository.listByTocUserId(tenant.getId(), tocUser.getId())).extracting(VipSubscription::getId).contains(vipSubscription.getId());
        assertThat(vipSubscriptionRepository.listByOrderId(tenant.getId(), vipOrder.getId())).extracting(VipSubscription::getId).contains(vipSubscription.getId());

        vipSubscription.setSubscriptionStatus("EXPIRED");
        vipSubscriptionRepository.update(vipSubscription);
        assertThat(vipSubscriptionRepository.findById(tenant.getId(), vipSubscription.getId())).get().extracting(VipSubscription::getSubscriptionStatus).isEqualTo("EXPIRED");

        assertThat(vipSubscriptionRepository.deleteById(tenant.getId(), vipSubscription.getId())).isTrue();
        assertThat(vipSubscriptionRepository.findById(tenant.getId(), vipSubscription.getId())).isEmpty();
    }

    private VipOrder createVipOrder(Long tenantId, Long tocUserId, Long planId) {
        VipOrder vipOrder = new VipOrder();
        vipOrder.setTenantId(tenantId);
        vipOrder.setOrderNo("ORD_" + unique("no"));
        vipOrder.setTocUserId(tocUserId);
        vipOrder.setPlanId(planId);
        vipOrder.setOrderStatus("PAID");
        vipOrder.setAmount(new BigDecimal("199.00"));
        vipOrderRepository.save(vipOrder);
        return vipOrder;
    }

    private VipPlan createVipPlan(Long tenantId) {
        VipPlan vipPlan = new VipPlan();
        vipPlan.setTenantId(tenantId);
        vipPlan.setPlanCode("PLAN_" + unique("code"));
        vipPlan.setPlanName("Plan");
        vipPlan.setDurationDays(30);
        vipPlan.setPriceAmount(new BigDecimal("199.00"));
        vipPlan.setCurrencyCode("CNY");
        vipPlan.setStatus("ACTIVE");
        vipPlanRepository.save(vipPlan);
        return vipPlan;
    }

    private Tenant createTenant() {
        String suffix = unique("tenant");
        Tenant tenant = new Tenant();
        tenant.setTenantCode("TEN_" + suffix);
        tenant.setTenantName("Tenant " + suffix);
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        return tenant;
    }

    private TocUser createTocUser(Long tenantId) {
        String suffix = unique("toc");
        TocUser tocUser = new TocUser();
        tocUser.setTenantId(tenantId);
        tocUser.setTocUid("TOC_" + suffix);
        tocUser.setNickname("Nick");
        tocUser.setVipStatus("NORMAL");
        tocUser.setStatus("ACTIVE");
        tocUserRepository.save(tocUser);
        return tocUser;
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}

