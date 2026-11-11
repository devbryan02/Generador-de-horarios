package elp.edu.pe.horario.shared.config;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.spring.boot.autoconfigure.TimefoldSolverAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class TimefoldSpringConfigurationTest {
    @Configuration(proxyBeanMethods = false)
    @AutoConfigurationPackage(basePackages = "elp.edu.pe.horario")
    static class ScanConfiguration { }

    @Test void starterAndProductionBeansCoexistWithoutDatabase() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TimefoldSolverAutoConfiguration.class))
                .withUserConfiguration(ScanConfiguration.class, TimefoldConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(SolverConfig.class);
                    assertThat(context).hasSingleBean(SolverFactory.class);
                    assertThat(context).hasSingleBean(SolverManager.class);
                    context.getBean(SolverFactory.class).buildSolver();
                });
    }
}
