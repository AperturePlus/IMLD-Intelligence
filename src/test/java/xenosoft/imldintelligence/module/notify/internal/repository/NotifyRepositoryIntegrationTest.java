package xenosoft.imldintelligence.module.notify.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationDelivery;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationMessage;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class NotifyRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private NotificationMessageRepository notificationMessageRepository;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @Test
    void notificationMessageCrudAndTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        NotificationMessage message = new NotificationMessage();
        message.setTenantId(tenantA.getId());
        message.setBizType("FOLLOWUP");
        message.setBizId("BIZ_" + unique("id"));
        message.setReceiverType("TOC_USER");
        message.setReceiverRefId(1001L);
        message.setTitle("title");
        message.setContent("content");
        message.setChannel("APP");
        message.setStatus("PENDING");
        message.setScheduledAt(OffsetDateTime.now().plusMinutes(5).withNano(0));
        notificationMessageRepository.save(message);

        assertThat(message.getId()).isNotNull();
        assertThat(notificationMessageRepository.findById(tenantA.getId(), message.getId())).isPresent();
        assertThat(notificationMessageRepository.findById(tenantB.getId(), message.getId())).isEmpty();
        assertThat(notificationMessageRepository.listByTenantId(tenantA.getId())).extracting(NotificationMessage::getId).contains(message.getId());
        assertThat(notificationMessageRepository.listByReceiver(tenantA.getId(), message.getReceiverType(), message.getReceiverRefId())).extracting(NotificationMessage::getId).contains(message.getId());

        message.setStatus("SENT");
        message.setSentAt(OffsetDateTime.now().withNano(0));
        notificationMessageRepository.update(message);
        assertThat(notificationMessageRepository.findById(tenantA.getId(), message.getId())).get().extracting(NotificationMessage::getStatus).isEqualTo("SENT");

        assertThat(notificationMessageRepository.deleteById(tenantA.getId(), message.getId())).isTrue();
        assertThat(notificationMessageRepository.findById(tenantA.getId(), message.getId())).isEmpty();
    }

    @Test
    void notificationDeliveryCrudWithJsonb() {
        Tenant tenant = createTenant();
        NotificationMessage message = createNotificationMessage(tenant.getId());

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setTenantId(tenant.getId());
        delivery.setMessageId(message.getId());
        delivery.setProviderName("SMS");
        delivery.setProviderMessageId("PID_" + unique("pid"));
        delivery.setDeliveryStatus("ACCEPTED");
        delivery.setFailReason(null);
        delivery.setCallbackPayload(OBJECT_MAPPER.createObjectNode().put("status", "accepted"));
        delivery.setDeliveredAt(OffsetDateTime.now().withNano(0));
        notificationDeliveryRepository.save(delivery);

        assertThat(delivery.getId()).isNotNull();
        assertThat(notificationDeliveryRepository.findById(tenant.getId(), delivery.getId())).isPresent();
        assertThat(notificationDeliveryRepository.listByTenantId(tenant.getId())).extracting(NotificationDelivery::getId).contains(delivery.getId());
        assertThat(notificationDeliveryRepository.listByMessageId(tenant.getId(), message.getId())).extracting(NotificationDelivery::getId).contains(delivery.getId());

        delivery.setDeliveryStatus("DELIVERED");
        delivery.setCallbackPayload(OBJECT_MAPPER.createObjectNode().put("status", "delivered"));
        notificationDeliveryRepository.update(delivery);
        assertThat(notificationDeliveryRepository.findById(tenant.getId(), delivery.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getDeliveryStatus()).isEqualTo("DELIVERED");
                    assertThat(value.getCallbackPayload().get("status").asText()).isEqualTo("delivered");
                });

        assertThat(notificationDeliveryRepository.deleteById(tenant.getId(), delivery.getId())).isTrue();
        assertThat(notificationDeliveryRepository.findById(tenant.getId(), delivery.getId())).isEmpty();
    }

    private NotificationMessage createNotificationMessage(Long tenantId) {
        NotificationMessage message = new NotificationMessage();
        message.setTenantId(tenantId);
        message.setBizType("FOLLOWUP");
        message.setReceiverType("TOC_USER");
        message.setReceiverRefId(1001L);
        message.setContent("content");
        message.setChannel("APP");
        message.setStatus("PENDING");
        notificationMessageRepository.save(message);
        return message;
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

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}

