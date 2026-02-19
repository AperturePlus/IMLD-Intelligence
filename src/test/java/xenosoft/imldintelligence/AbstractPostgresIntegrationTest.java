package xenosoft.imldintelligence;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractPostgresIntegrationTest {
    @Container
    private static final SingletonPostgresContainer POSTGRESQL_CONTAINER = SingletonPostgresContainer.getInstance();

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
    }

    private static final class SingletonPostgresContainer extends PostgreSQLContainer<SingletonPostgresContainer> {
        private static SingletonPostgresContainer instance;

        private SingletonPostgresContainer() {
            super("postgres:16-alpine");
            withDatabaseName("imld_core");
            withUsername("postgres");
            withPassword("postgres");
        }

        private static synchronized SingletonPostgresContainer getInstance() {
            if (instance == null) {
                instance = new SingletonPostgresContainer();
                instance.start();
            }
            return instance;
        }

        @Override
        public void stop() {
            // Keep container alive for the full JVM lifetime so Spring's cached context
            // never points to a stopped random port.
        }
    }
}
