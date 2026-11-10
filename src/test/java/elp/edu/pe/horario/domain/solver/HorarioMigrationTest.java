package elp.edu.pe.horario.domain.solver;

import elp.edu.pe.horario.domain.enums.*;
import elp.edu.pe.horario.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import ai.timefold.solver.core.api.score.ScoreManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Characterizes current rules, including known defects; needs no Spring or SQL. */
class HorarioMigrationTest {
    private static UUID id(int n) { return new UUID(0, n); }

    private static BloqueHorario bloque(int hour) {
        return new BloqueHorario(id(hour), DiaSemana.LUNES,
                LocalTime.of(hour, 0), LocalTime.of(hour + 2, 0), Turno.values()[0]);
    }

    private static AsignacionHorario clase(int n, Aula aula, BloqueHorario bloque) {
        var docente = new Docente(id(100 + n), "Docente " + n, 20, 6);
        docente.setRestricciones(List.of());
        var curso = new Curso(id(200 + n), "Curso " + n, 2, "TEORICO");
        var seccion = new Seccion(id(300 + n), "Seccion " + n, null);
        var relacion = new CursoSeccionDocente(id(400 + n), curso, seccion,
                docente, ModoClase.values()[0]);
        return new AsignacionHorario(id(500 + n), relacion, aula, bloque);
    }

    private static Aula aula(int n) {
        return new Aula(id(n), "Aula " + n, 30, TipoAula.values()[0]);
    }

    private static HorarioSolucion solucion(List<Aula> aulas, List<BloqueHorario> bloques,
                                           AsignacionHorario... clases) {
        var solucion = new HorarioSolucion();
        solucion.setAulaList(aulas);
        solucion.setBloqueHorarioList(bloques);
        solucion.setAsignacionHorarioList(new ArrayList<>(List.of(clases)));
        solucion.setCursoSeccionDocentes(List.of(clases).stream()
                .map(AsignacionHorario::getCursoSeccionDocente).toList());
        return solucion;
    }

    private static SolverConfig config() {
        return new SolverConfig().withSolutionClass(HorarioSolucion.class)
                .withEntityClasses(AsignacionHorario.class)
                .withConstraintProviderClass(HorarioConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(1));
    }

    private static void score(HorarioSolucion solucion, long hard, long soft) {
        var factory = SolverFactory.<HorarioSolucion>create(config());
        ScoreManager.create(factory).updateScore(solucion);
        var actual = solucion.getScore();
        assertEquals(hard, actual.hardScore());
        assertEquals(soft, actual.softScore());
    }

    @Test void singleClassHasStableScore() {
        var a = aula(1); var b = bloque(8);
        score(solucion(List.of(a), List.of(b), clase(1, a, b)), 0, 2998);
    }

    @Test void independentClassesShareBlock() {
        var a = aula(1); var a2 = aula(2); var b = bloque(8);
        score(solucion(List.of(a, a2), List.of(b), clase(1, a, b), clase(2, a2, b)), 0, 5994);
    }

    @Test void blockedTeacherIsPenalized() {
        var a = aula(1); var b = bloque(8); var c = clase(1, a, b);
        var docente = c.getCursoSeccionDocente().getDocente();
        docente.setRestricciones(List.of(new RestriccionDocente(id(700), DiaSemana.LUNES,
                LocalTime.of(10, 0), LocalTime.of(8, 0), TipoRestriccion.BLOQUEADO, docente)));
        score(solucion(List.of(a), List.of(b), c), -100, 2998);
    }

    @Test void contractedHoursAreEnforced() {
        var a = aula(1); var b = bloque(8); var c = clase(1, a, b);
        c.getCursoSeccionDocente().getDocente().setHorasContratadas(1);
        score(solucion(List.of(a), List.of(b), c), -5000, 2998);
    }

    @Test void characterizesKnownContiguousTeacherConflict() {
        var a = aula(1); var a2 = aula(2); var b = bloque(8); var b2 = bloque(10);
        var c = clase(1, a, b); var c2 = clase(2, a2, b2);
        c2.getCursoSeccionDocente().setDocente(c.getCursoSeccionDocente().getDocente());
        score(solucion(List.of(a, a2), List.of(b, b2), c, c2), -100000, 5996);
    }

    @Test void characterizesKnownPartialRoomOverlap() {
        var a = aula(1); var b = bloque(8); var b2 = bloque(9);
        score(solucion(List.of(a), List.of(b, b2), clase(1, a, b), clase(2, a, b2)), 0, 5994);
    }

    @Test void solverFindsFeasibleAssignment() {
        var a = aula(1); var b = bloque(8);
        var input = solucion(List.of(a), List.of(b), clase(1, null, null));
        var result = SolverFactory.<HorarioSolucion>create(config()).buildSolver().solve(input);
        assertEquals(0, result.getScore().hardScore());
        assertNotNull(result.getAsignacionHorarioList().get(0).getAula());
        assertNotNull(result.getAsignacionHorarioList().get(0).getBloqueHorario());
    }

    @Test void solverReportsInfeasibleResources() {
        var a = aula(1); var b = bloque(8);
        var input = solucion(List.of(a), List.of(b), clase(1, null, null), clase(2, null, null));
        var result = SolverFactory.<HorarioSolucion>create(config()).buildSolver().solve(input);
        assertTrue(result.getScore().hardScore() < 0);
    }

    @Test @Tag("benchmark") void compareSolverOnSyntheticData() {
        for (int seed = -1; seed < 3; seed++) {
            var aulas = List.of(aula(1), aula(2), aula(3));
            var bloques = List.of(bloque(8), bloque(11), bloque(14), bloque(17));
            var clases = new ArrayList<AsignacionHorario>();
            for (int n = 1; n <= 10; n++) clases.add(clase(n, null, null));
            var input = solucion(aulas, bloques, clases.toArray(AsignacionHorario[]::new));
            var cfg = config().withRandomSeed((long) Math.max(seed, 0))
                    .withTerminationSpentLimit(Duration.ofSeconds(seed < 0 ? 2 : 30));
            var solver = SolverFactory.<HorarioSolucion>create(cfg).buildSolver();
            long started = System.nanoTime();
            long[] firstFeasible = {-1};
            solver.addEventListener(event -> {
                if (event.getNewBestSolution().getScore().hardScore() == 0 && firstFeasible[0] < 0) {
                    firstFeasible[0] = (System.nanoTime() - started) / 1_000_000;
                }
            });
            var result = solver.solve(input);
            assertEquals(0, result.getScore().hardScore());
            if (seed >= 0) System.out.printf("BENCHMARK seed=%d hard=%d soft=%d firstFeasibleMs=%d elapsedMs=%d%n",
                    seed, result.getScore().hardScore(), result.getScore().softScore(),
                    firstFeasible[0], (System.nanoTime() - started) / 1_000_000);
        }
    }
}
