package elp.edu.pe.horario.infrastructure.controller;

import elp.edu.pe.horario.application.dto.HorarioDto;
import elp.edu.pe.horario.application.usecase.asignacion_horario.ObtenerHorariosUseCase;
import elp.edu.pe.horario.domain.enums.DiaSemana;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HorarioControllerTest {
    @Test void preservesScheduleResponseAndQueryRoutes() throws Exception {
        var id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var rows = List.of(new HorarioDto(id, "Matematica", "Docente", "Aula 1", "A",
                DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0)));
        var useCase = mock(ObtenerHorariosUseCase.class);
        when(useCase.obtenerHorariosPorSeccion(id)).thenReturn(rows);
        when(useCase.obtenerHorariosPorDocente(id)).thenReturn(rows);
        when(useCase.obtenerHorariosPorPeriodo(id)).thenReturn(rows);
        var mapper = JsonMapper.builder().build();
        var mvc = MockMvcBuilders.standaloneSetup(new HorarioController(useCase))
                .setMessageConverters(new JacksonJsonHttpMessageConverter(mapper)).build();
        for (var route : List.of("seccion", "docente", "periodo")) {
            mvc.perform(get("/horarios/" + route + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].curso").value("Matematica"))
                    .andExpect(jsonPath("$[0].diaSemana").value("LUNES"))
                    .andExpect(jsonPath("$[0].horaInicio").value("08:00:00"))
                    .andExpect(jsonPath("$[0].horaFin").value("10:00:00"));
        }
    }
}
