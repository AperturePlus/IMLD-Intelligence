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

/**
 * OpenAPI/Swagger configuration for IMLD Intelligence API documentation.
 *
 * <p>This configuration supports multiple deployment modes (SaaS, Private, Hybrid)
 * and provides comprehensive API documentation with security schemas.</p>
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:IMLD Intelligence}")
    private String applicationName;

    @Value("${imld.deployment.mode:saas}")
    private String deploymentMode;

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
        StringBuilder desc = new StringBuilder();
        desc.append("IMLD Intelligence is a modular medical health platform designed for ");
        desc.append("primary healthcare institutions, individual users, and privacy-focused hospitals.\n\n");
        desc.append("**Deployment Mode**: ").append(deploymentMode.toUpperCase()).append("\n\n");

        desc.append("### Key Features\n\n");
        desc.append("- **Identity & Access Management**: User authentication, patient management, consent tracking\n");
        desc.append("- **Clinical Operations**: Patient care, diagnosis, and clinical workflows\n");
        desc.append("- **Audit & Compliance**: Comprehensive audit logging for regulatory compliance\n");
        desc.append("- **Care Planning & Screening**: Questionnaires, care plans, and health screening\n");
        desc.append("- **Payment & Subscriptions**: Payment processing and VIP membership management\n");
        desc.append("- **Notifications & Integration**: Multi-channel notifications and external system integration\n\n");

        desc.append("### Security\n\n");
        desc.append("- All API endpoints (except auth) require JWT Bearer token authentication\n");
        desc.append("- Multi-tenant operations require X-Tenant-Id header\n");
        desc.append("- Role-based access control (RBAC) and attribute-based access control (ABAC)\n");
        desc.append("- Sensitive data access is audited and logged\n\n");

        desc.append("### Deployment Modes\n\n");
        desc.append("- **SaaS**: Cloud-based deployment for primary healthcare and individual users\n");
        desc.append("- **Private**: On-premise deployment for privacy-focused hospitals\n");
        desc.append("- **Hybrid**: Private deployment with controlled cloud communication for specific features\n");

        return desc.toString();
    }

    private List<Server> buildServers() {
        Server server = new Server();
        String url = contextPath != null && !contextPath.isEmpty() ? contextPath : "/";
        server.setUrl(url);

        String serverDescription = switch (deploymentMode.toLowerCase()) {
            case "saas" -> "SaaS Cloud Environment";
            case "private" -> "Private On-Premise Environment";
            case "hybrid" -> "Hybrid Environment (Private with Cloud Bridge)";
            default -> "Development Environment";
        };

        server.setDescription(serverDescription);
        return List.of(server);
    }
}
