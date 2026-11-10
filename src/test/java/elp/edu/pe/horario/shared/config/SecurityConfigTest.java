package elp.edu.pe.horario.shared.config;

import elp.edu.pe.horario.application.usecase.user.LoadByUsernameUserCase;
import elp.edu.pe.horario.shared.security.JwtAuthenticationFilter;
import elp.edu.pe.horario.shared.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = {SecurityConfig.class, SecurityConfigTest.TestConfig.class})
@WebAppConfiguration
@TestPropertySource(properties = {"jwt.secret=synthetic-test-secret-at-least-32-bytes-long", "jwt.expiration=60000"})
class SecurityConfigTest {
    @Autowired WebApplicationContext context;

    @Configuration @EnableWebMvc @EnableWebSecurity
    static class TestConfig {
        @Bean LoadByUsernameUserCase users() { return mock(LoadByUsernameUserCase.class); }
        @Bean JwtUtils jwtUtils() { return mock(JwtUtils.class); }
        @Bean JwtAuthenticationFilter jwtFilter(LoadByUsernameUserCase users) {
            var filter = new JwtAuthenticationFilter();
            ReflectionTestUtils.setField(filter, "loadByUsernameUserCase", users);
            ReflectionTestUtils.setField(filter, "jwtUtils", mock(JwtUtils.class));
            return filter;
        }
        @Bean Endpoints endpoints() { return new Endpoints(); }
    }

    @RestController
    static class Endpoints {
        @GetMapping({"/auth/probe", "/aula/probe", "/admin/probe"})
        String probe() { return "ok"; }
    }

    @Test void preservesPublicAndRoleProtectedRoutes() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(get("/auth/probe")).andExpect(status().isOk());
        mvc.perform(get("/aula/probe")).andExpect(status().is4xxClientError());
        mvc.perform(get("/aula/probe").with(user("coordinador").roles("COORDINADOR")))
                .andExpect(status().isOk());
        mvc.perform(get("/aula/probe").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/probe").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(get("/admin/probe").with(user("coordinador").roles("COORDINADOR")))
                .andExpect(status().isForbidden());
    }
}
