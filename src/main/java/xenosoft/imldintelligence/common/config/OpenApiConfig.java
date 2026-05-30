package xenosoft.imldintelligence.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;

/**
 * OpenAPI/Swagger configuration for IMLD Intelligence API documentation.
 *
 * <p>This configuration supports both SaaS and private deployment from one codebase.
 * Private deployment can optionally enable a controlled cloud bridge for allowlisted
 * outbound scenarios.</p>
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:IMLD Intelligence}")
    private String applicationName;

    @Value("${imld.deployment.mode:saas}")
    private String deploymentMode;

    @Value("${imld.cloud-bridge.enabled:false}")
    private boolean cloudBridgeEnabled;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    private static final String TENANT_HEADER_SCHEME_NAME = "Tenant ID Header";

    @Bean
    public OpenAPI customOpenAPI() {
        String description = buildDescription();

        return new OpenAPI()
                .info(new Info()
                        .title("IMLD Intelligence API")
                        .description(description)
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("Xenosoft")
                                .email("support@xenosoft.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.xenosoft.com/license")))
                .servers(buildServers())
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication token obtained from login endpoint"))
                        .addSecuritySchemes(TENANT_HEADER_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Tenant-Id")
                                .description("Tenant identifier for multi-tenant operations")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME)
                        .addList(TENANT_HEADER_SCHEME_NAME));
    }

    private String buildDescription() {

        return "IMLD Intelligence is a modular medical health platform designed for " +
                "primary healthcare institutions, individual users, and privacy-focused hospitals.\n\n" +
                "**Deployment Mode**: " + normalizedDeploymentMode().toUpperCase(Locale.ROOT) + "\n" +
                "**Cloud Bridge Enabled**: " + cloudBridgeEnabled + "\n\n" +
                "### Key Features\n\n" +
                "- **Identity & Access Management**: User authentication, patient management, consent tracking\n" +
                "- **Clinical Operations**: Patient care, diagnosis, and clinical workflows\n" +
                "- **Audit & Compliance**: Comprehensive audit logging for regulatory compliance\n" +
                "- **Care Planning & Screening**: Questionnaires, care plans, and health screening\n" +
                "- **Payment & Subscriptions**: Payment processing and VIP membership management\n" +
                "- **Notifications & Integration**: Multi-channel notifications and external system integration\n\n" +
                "### Security\n\n" +
                "- All API endpoints (except auth) require JWT Bearer token authentication\n" +
                "- Multi-tenant operations require X-Tenant-Id header\n" +
                "- Role-based access control (RBAC) and attribute-based access control (ABAC)\n" +
                "- Sensitive data access is audited and logged\n\n" +
                "### Deployment Modes\n\n" +
                "- **SaaS**: Public cloud deployment for community healthcare institutions and individual users\n" +
                "- **Private**: On-premise deployment for privacy-focused hospitals\n" +
                "- **Private + Cloud Bridge**: Private mode with controlled outbound communication for allowlisted scenarios only\n";
    }

    private List<Server> buildServers() {
        Server server = new Server();
        String url = contextPath != null && !contextPath.isEmpty() ? contextPath : "/";
        server.setUrl(url);

        String serverDescription = switch (normalizedDeploymentMode()) {
            case "saas" -> "Public SaaS Environment";
            case "private" -> cloudBridgeEnabled
                    ? "Private On-Premise Environment (Controlled Cloud Bridge Enabled)"
                    : "Private On-Premise Environment (Local-Only Core Services)";
            default -> "Development Environment";
        };

        server.setDescription(serverDescription);
        return List.of(server);
    }

    private String normalizedDeploymentMode() {
        if (deploymentMode == null) {
            return "saas";
        }
        String normalized = deploymentMode.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "saas";
        }
        if ("hybrid".equals(normalized)) {
            return "private";
        }
        return normalized;
    }
}
