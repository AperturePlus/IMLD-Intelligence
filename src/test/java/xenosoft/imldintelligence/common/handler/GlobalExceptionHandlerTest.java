package xenosoft.imldintelligence.common.handler;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldReturnBadRequestForRequestBodyValidationErrors() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("name: name must not be blank"));
    }

    @Test
    void shouldRespectExplicitResponseStatusException() throws Exception {
        mockMvc.perform(get("/test/errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("demo resource not found"));
    }

    @Test
    void shouldReturnInternalErrorForUnexpectedExceptions() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("internal server error"));
    }

    @RestController
    @RequestMapping("/test/errors")
    static class TestController {

        @PostMapping("/validation")
        void validate(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "demo resource not found");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("boom");
        }
    }

    record TestRequest(
            @NotBlank(message = "name must not be blank")
            String name
    ) {
    }
}
