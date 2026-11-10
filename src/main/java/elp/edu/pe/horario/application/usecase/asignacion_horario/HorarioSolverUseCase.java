package elp.edu.pe.horario.application.usecase.asignacion_horario;

import elp.edu.pe.horario.application.dto.response.GeneracionHorarioResponse;
import elp.edu.pe.horario.domain.model.AsignacionHorario;
import elp.edu.pe.horario.domain.repository.AsignacionHorarioRepository;
import elp.edu.pe.horario.domain.solver.HorarioSolucion;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HorarioSolverUseCase {

    private final SolverManager<HorarioSolucion, UUID> solverManager;
    private final AsignacionHorarioRepository asignacionHorarioRepository;
    private final MensajeGeneracionUseCase mostrarMensajeGeneracionUseCase;
    private final AnalizarSolucionUseCase analizarSolucionHorarioUseCase;

    @Transactional
    public GeneracionHorarioResponse ejecutar(HorarioSolucion solucion) throws ExecutionException, InterruptedException {
        try {
            UUID solucionId = UUID.randomUUID();
            SolverJob<HorarioSolucion, UUID> solverJob = solverManager.solve(solucionId, solucion);
            HorarioSolucion solucionFinal = solverJob.getFinalBestSolution();

            if (solucionFinal == null) {
                throw new IllegalStateException("No se pudo resolver el horario");
            }

            List<AsignacionHorario> nuevasAsignaciones = solucionFinal.getAsignacionHorarioList();
            nuevasAsignaciones.forEach(asignacion -> {
                asignacion.setId(null);
                asignacionHorarioRepository.save(asignacion);
            });

            int cantidadAulasUsadas = analizarSolucionHorarioUseCase.obtenerAulasUsadas(nuevasAsignaciones).size();
            int cantidadBloquesUsados = analizarSolucionHorarioUseCase.obtenerBloquesUsados(nuevasAsignaciones).size();
            int cantidadDocentesAsignados = analizarSolucionHorarioUseCase.obtenerCantidadDocentesAsignados(nuevasAsignaciones);

            long hardScore = solucionFinal.getScore().hardScore();
            long softScore = solucionFinal.getScore().softScore();

            String calidadGeneracion = mostrarMensajeGeneracionUseCase.determinarCalidadGeneracion(hardScore, softScore);
            String mensajeEvaluacion = mostrarMensajeGeneracionUseCase.generarMensajeEvaluacion(hardScore, softScore);

            return new GeneracionHorarioResponse(
                    "Horario generado exitosamente",
                    nuevasAsignaciones.size(),
                    cantidadAulasUsadas,
                    cantidadBloquesUsados,
                    cantidadDocentesAsignados,
                    calidadGeneracion,
                    mensajeEvaluacion
            );

        } catch (Exception e) {
            throw new RuntimeException("Error resolviendo horario: " + e.getMessage(), e);
        }
    }
}
