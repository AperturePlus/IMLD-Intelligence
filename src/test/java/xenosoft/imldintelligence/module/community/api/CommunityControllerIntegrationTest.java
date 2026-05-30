package xenosoft.imldintelligence.module.community.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisabledInAotMode
@TestPropertySource(properties = {
        "imld.security.enabled=true",
        "imld.community.scope=GLOBAL",
        "imld.community.global-tenant-code=IMLD_COMMUNITY"
})
class CommunityControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TocUserRepository tocUserRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Long tenantId;
    private Long tocUserId;
    private Long adminUserId;
    private String tocToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        Tenant tenant = tenantRepository.findByTenantCode("IMLD_COMMUNITY").orElseGet(() -> {
            Tenant created = new Tenant();
            created.setTenantCode("IMLD_COMMUNITY");
            created.setTenantName("IMLD Community");
            created.setDeployMode("SAAS");
            created.setStatus("ACTIVE");
            tenantRepository.save(created);
            return created;
        });
        this.tenantId = tenant.getId();

        TocUser tocUser = tocUserRepository.findByTocUid(tenantId, "TEST_TOC_UID").orElseGet(() -> {
            TocUser created = new TocUser();
            created.setTenantId(tenantId);
            created.setTocUid("TEST_TOC_UID");
            created.setNickname("Alice");
            created.setVipStatus("NORMAL");
            created.setStatus("ACTIVE");
            created.setCreatedAt(OffsetDateTime.now().withNano(0));
            created.setUpdatedAt(OffsetDateTime.now().withNano(0));
            tocUserRepository.save(created);
            return created;
        });
        this.tocUserId = tocUser.getId();

        UserAccount admin = userAccountRepository.findByUsername(tenantId, "admin").orElseGet(() -> {
            UserAccount created = new UserAccount();
            created.setTenantId(tenantId);
            created.setUserNo("USR_ADMIN");
            created.setUsername("admin");
            created.setPasswordHash("noop");
            created.setDisplayName("Admin");
            created.setUserType("SYSTEM");
            created.setDeptName(null);
            created.setStatus("ACTIVE");
            userAccountRepository.save(created);
            return created;
        });
        this.adminUserId = admin.getId();

        this.tocToken = jwtUtil.generateAccessToken(new UserSubject(
                tocUserId,
                tenantId,
                "TOC",
                null,
                Set.of("TOC_USER")
        ));
        this.adminToken = jwtUtil.generateAccessToken(new UserSubject(
                adminUserId,
                tenantId,
                "SYSTEM",
                null,
                Set.of("SYSTEM_ADMIN")
        ));
    }

    @Test
    void listBoardsDefaultsToActiveOnly() throws Exception {
        String activeCode = "BOARD_ACTIVE_" + System.nanoTime();
        String inactiveCode = "BOARD_INACTIVE_" + System.nanoTime();

        mockMvc.perform(post("/api/v1/app/community/boards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"boardCode":"%s","boardName":"Active","status":"ACTIVE"}
                                """.formatted(activeCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.boardCode").value(activeCode));

        mockMvc.perform(post("/api/v1/app/community/boards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"boardCode":"%s","boardName":"Inactive","status":"INACTIVE"}
                                """.formatted(inactiveCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/v1/app/community/boards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[?(@.boardCode=='%s')]".formatted(activeCode)).exists())
                .andExpect(jsonPath("$.data[?(@.boardCode=='%s')]".formatted(inactiveCode)).doesNotExist());
    }

    @Test
    void postLifecycleModerationAndReportFlow() throws Exception {
        String boardCode = "BOARD_" + System.nanoTime();

        String boardJson = mockMvc.perform(post("/api/v1/app/community/boards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"boardCode":"%s","boardName":"Test","status":"ACTIVE"}
                                """.formatted(boardCode)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long boardId = OBJECT_MAPPER.readTree(boardJson).path("data").path("id").asLong();

        String pendingPostJson = mockMvc.perform(post("/api/v1/app/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardId": %d,
                                  "authorTocUserId": %d,
                                  "title": "Need help",
                                  "content": "Contact me 13812341234",
                                  "anonymousFlag": true
                                }
                                """.formatted(boardId, tocUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long pendingPostId = OBJECT_MAPPER.readTree(pendingPostJson).path("data").path("id").asLong();

        String publishedPostJson = mockMvc.perform(post("/api/v1/app/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardId": %d,
                                  "authorTocUserId": %d,
                                  "title": "Hello",
                                  "content": "This is a safe post",
                                  "anonymousFlag": false
                                }
                                """.formatted(boardId, tocUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long publishedPostId = OBJECT_MAPPER.readTree(publishedPostJson).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/app/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id==%d)]".formatted(publishedPostId)).exists())
                .andExpect(jsonPath("$.data.items[?(@.id==%d)]".formatted(pendingPostId)).doesNotExist());

        mockMvc.perform(get("/api/v1/app/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .param("authorTocUserId", tocUserId.toString())
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id==%d)]".formatted(pendingPostId)).exists());

        mockMvc.perform(get("/api/v1/app/community/moderation/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id==%d)]".formatted(pendingPostId)).exists());

        mockMvc.perform(post("/api/v1/app/community/posts/%d/comments".formatted(publishedPostId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorTocUserId":%d,"content":"QQ:1234567"}
                                """.formatted(tocUserId)))
                .andExpect(status().isBadRequest());

        String commentJson = mockMvc.perform(post("/api/v1/app/community/posts/%d/comments".formatted(publishedPostId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorTocUserId":%d,"content":"Nice to meet you"}
                                """.formatted(tocUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long commentId = OBJECT_MAPPER.readTree(commentJson).path("data").path("id").asLong();

        String reportJson = mockMvc.perform(post("/api/v1/app/community/reports")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reporterTocUserId":%d,"postId":%d,"reasonCode":"SPAM","reasonText":"spam"}
                                """.formatted(tocUserId, publishedPostId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(publishedPostId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long reportId = OBJECT_MAPPER.readTree(reportJson).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/app/community/moderation/reports/%d".formatted(reportId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"HANDLED","resultAction":"DELETE_POST","resultNote":"remove"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HANDLED"))
                .andExpect(jsonPath("$.data.resultAction").value("DELETE_POST"));

        mockMvc.perform(get("/api/v1/app/community/posts/%d".formatted(publishedPostId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/app/community/moderation/reports/%d".formatted(reportId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"HANDLED","resultAction":"DELETE_COMMENT","resultNote":"remove"}
                                """))
                .andExpect(status().isBadRequest());

        // Comment still exists for the earlier published post before deletion, but listComments filters by post visibility
        mockMvc.perform(get("/api/v1/app/community/posts/%d/comments".formatted(publishedPostId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectWhenTenantHeaderDoesNotMatchToken() throws Exception {
        mockMvc.perform(get("/api/v1/app/community/boards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", String.valueOf(tenantId + 999)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectWhenAuthorDoesNotMatchToken() throws Exception {
        String boardCode = "BOARD_" + System.nanoTime();
        String boardJson = mockMvc.perform(post("/api/v1/app/community/boards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"boardCode":"%s","boardName":"Test","status":"ACTIVE"}
                                """.formatted(boardCode)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long boardId = OBJECT_MAPPER.readTree(boardJson).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/app/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tocToken)
                        .header("X-Tenant-Id", tenantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardId": %d,
                                  "authorTocUserId": %d,
                                  "title": "Hello",
                                  "content": "Hi"
                                }
                                """.formatted(boardId, tocUserId + 1)))
                .andExpect(status().isForbidden());
    }
}

