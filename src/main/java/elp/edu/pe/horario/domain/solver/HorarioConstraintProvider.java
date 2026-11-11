package elp.edu.pe.horario.domain.solver;

import elp.edu.pe.horario.domain.enums.TipoRestriccion;
import elp.edu.pe.horario.domain.model.AsignacionHorario;
import elp.edu.pe.horario.domain.model.Docente;
import elp.edu.pe.horario.domain.model.Seccion;
import elp.edu.pe.horario.domain.model.BloqueHorario;
import elp.edu.pe.horario.domain.model.CursoSeccionDocente;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

public class HorarioConstraintProvider implements ConstraintProvider {

    // Constantes para scores
    private static final int HARD_SCORE_HIGH = 100000;
    private static final int SOFT_SCORE_HIGH = 5000;
    private static final int SOFT_SCORE_MEDIUM = 3000;
    private static final int HORAS_POR_BLOQUE = 2;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // Restricciones HARD - No pueden violarse
                limitarHorasRequeridasPorCurso(constraintFactory),
                evitarBloquesConsecutivosMismaSeccion(constraintFactory),
                distribuirCargaDocente(constraintFactory),
                evitarSobreusoDeBloques(constraintFactory),
                asignacionDebeEstarCompleta(constraintFactory),
                aulaNoSuperpuesta(constraintFactory),
                docenteNoSuperpuesto(constraintFactory),
                seccionNoSuperpuesta(constraintFactory),
                evitarRestriccionesDocente(constraintFactory),
                limitarHorasContratadasPorDocente(constraintFactory),
                evitarConflictoMismoDocenteMismaSeccionCursoDistinto(constraintFactory),

                // Restricciones SOFT - Preferencias
                incentivarDistribucionDeBloques(constraintFactory), //pruebas
                fomentarUsoDistribuidoDeAulas(constraintFactory), //pruebas
                incentivarCompartirBloquesSinCruces(constraintFactory),
                cumplirHorasRequeridas(constraintFactory),
                incentivarUsoDeMasBloques(constraintFactory)
        };
    }

    // ================= MÉTODOS AUXILIARES =================

    /**
     * Extrae el docente de una asignación de forma segura
     */
    private Docente extraerDocente(AsignacionHorario asignacion) {
        if (asignacion == null) return null;
        CursoSeccionDocente csd = asignacion.getCursoSeccionDocente();
        return csd != null ? csd.getDocente() : null;
    }

    /**
     * Extrae la sección de una asignación de forma segura
     */
    private Seccion extraerSeccion(AsignacionHorario asignacion) {
        if (asignacion == null) return null;
        CursoSeccionDocente csd = asignacion.getCursoSeccionDocente();
        return csd != null ? csd.getSeccion() : null;
    }

    /**
     * Calcula las horas de duración de un bloque horario
     */
    private int calcularHorasBloque(BloqueHorario bloque) {
        if (bloque == null || bloque.getHoraInicio() == null || bloque.getHoraFin() == null) {
            return 0;
        }
        return Math.toIntExact(Duration.between(bloque.getHoraInicio(), bloque.getHoraFin()).toHours());
    }

    /**
     * Verifica si hay solapamiento entre horarios
     */
    private boolean bloquesSeSolapan(LocalTime inicio1, LocalTime fin1, LocalTime inicio2, LocalTime fin2) {
        if (inicio1 == null || fin1 == null || inicio2 == null || fin2 == null) {
            return false;
        }
        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }

    /**
     * Verifica si una asignación es válida (no nula y con datos completos)
     */
    private boolean esAsignacionValida(AsignacionHorario asignacion) {
        return asignacion != null &&
                asignacion.getBloqueHorario() != null &&
                asignacion.getCursoSeccionDocente() != null;
    }

    /**
     * Verifica si una asignación tiene restricciones bloqueadas
     */
    private boolean tieneRestriccionBloqueada(AsignacionHorario asignacion) {
        if (!esAsignacionValida(asignacion)) return false;

        Docente docente = extraerDocente(asignacion);
        if (docente == null || docente.getRestricciones() == null) return false;

        BloqueHorario bloque = asignacion.getBloqueHorario();

        return docente.getRestricciones().stream().anyMatch(restriccion ->
                restriccion.getTipoRestriccion() == TipoRestriccion.BLOQUEADO &&
                        restriccion.getDiaSemana() == bloque.getDiaSemana() &&
                        bloquesSeSolapan(bloque.getHoraInicio(), bloque.getHoraFin(),
                                restriccion.getHoraInicio(), restriccion.getHoraFin()));
    }

    // ================= RESTRICCIONES HARD =================

    private Constraint evitarConflictoMismoDocenteMismaSeccionCursoDistinto(ConstraintFactory factory) {
        return factory
                .forEachUniquePair(AsignacionHorario.class,
                        Joiners.equal(this::extraerDocente),
                        Joiners.equal(this::extraerSeccion))
                .filter((a1, a2) -> {
                    var b1 = a1.getBloqueHorario();
                    var b2 = a2.getBloqueHorario();
                    var curso1 = a1.getCursoSeccionDocente().getCurso();
                    var curso2 = a2.getCursoSeccionDocente().getCurso();

                    return b1 != null && b2 != null &&
                            b1.getDiaSemana() == b2.getDiaSemana() &&
                            b1.getHoraInicio().equals(b2.getHoraInicio()) &&
                            !curso1.equals(curso2); // <== clave: cursos distintos
                })
                .penalize(HardSoftScore.ONE_HARD.multiply(HARD_SCORE_HIGH))
                .asConstraint("evitarConflictoMismoDocenteMismaSeccionCursoDistinto");
    }


    private Constraint limitarHorasRequeridasPorCurso(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .groupBy(
                        AsignacionHorario::getCursoSeccionDocente,
                        ConstraintCollectors.count()
                )
                .filter((csd, count) -> {
                    int maxBloques = (int) Math.ceil((double) csd.getCurso().getHorasSemanales() / HORAS_POR_BLOQUE);
                    return count > maxBloques;
                })
                .penalize(HardSoftScore.ONE_HARD.multiply(HARD_SCORE_HIGH))
                .asConstraint("limitarHorasRequeridasPorCurso");
    }


    private Constraint evitarBloquesConsecutivosMismaSeccion(ConstraintFactory factory) {
        return factory.forEachUniquePair(AsignacionHorario.class,
                        equal(this::extraerSeccion),
                        equal(a -> a.getBloqueHorario().getDiaSemana()))
                .filter((a1, a2) -> {
                    var h1 = a1.getBloqueHorario().getHoraInicio();
                    var h2 = a2.getBloqueHorario().getHoraInicio();
                    return h1 != null && h2 != null &&
                            Math.abs(Duration.between(h1, h2).toMinutes()) <= 120;
                })
                .penalize(HardSoftScore.ONE_SOFT.multiply(SOFT_SCORE_MEDIUM))
                .asConstraint("evitarBloquesConsecutivosMismaSeccion");
    }


    private Constraint distribuirCargaDocente(ConstraintFactory factory) {
        return factory.forEach(AsignacionHorario.class)
                .filter(this::esAsignacionValida)
                .groupBy(
                        this::extraerDocente,
                        a -> a.getBloqueHorario().getDiaSemana(),
                        ConstraintCollectors.count()
                )
                .filter((docente, dia, bloquesEnUnDia) -> bloquesEnUnDia > 3)
                .penalize(HardSoftScore.ONE_SOFT,
                        (docente, dia, bloquesEnUnDia) -> SOFT_SCORE_MEDIUM * (bloquesEnUnDia - 3))
                .asConstraint("distribuirCargaDocente");
    }


    private Constraint evitarSobreusoDeBloques(ConstraintFactory factory) {
        return factory.forEach(AsignacionHorario.class)
                .filter(this::esAsignacionValida)
                .groupBy(
                        a -> a.getBloqueHorario().getDiaSemana(),
                        ConstraintCollectors.count()
                )
                .filter((dia, totalAsignaciones) -> totalAsignaciones > 5)
                .penalize(HardSoftScore.ONE_SOFT,
                        (dia, totalAsignaciones) -> SOFT_SCORE_MEDIUM * (totalAsignaciones - 5))
                .asConstraint("evitarSobreusoDeBloques");
    }


    private Constraint asignacionDebeEstarCompleta(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .filter(asignacion -> asignacion.getBloqueHorario() == null || asignacion.getAula() == null)
                .penalize(HardSoftScore.ONE_HARD.multiply(HARD_SCORE_HIGH))
                .asConstraint("asignacionDebeEstarCompleta");
    }

    private Constraint aulaNoSuperpuesta(ConstraintFactory factory) {
        return factory
                .forEachUniquePair(AsignacionHorario.class,
                        equal(AsignacionHorario::getAula))
                .filter((a1, a2) -> {
                    BloqueHorario b1 = a1.getBloqueHorario();
                    BloqueHorario b2 = a2.getBloqueHorario();
                    return b1 != null && b1.equals(b2);
                })
                .penalize(HardSoftScore.ONE_HARD.multiply(HARD_SCORE_HIGH))
                .asConstraint("aulaNoSuperpuesta");
    }

    private Constraint docenteNoSuperpuesto(ConstraintFactory factory) {
        return factory
                .forEachUniquePair(AsignacionHorario.class,
                        equal(this::extraerDocente))
                .filter((a1, a2) -> {
                    BloqueHorario b1 = a1.getBloqueHorario();
                    BloqueHorario b2 = a2.getBloqueHorario();

                    return b1 != null && b2 != null &&
                            b1.getDiaSemana() == b2.getDiaSemana() &&
                            bloquesSeSuperponen(b1, b2);
                })
                .penalize(HardSoftScore.ONE_HARD.multiply(HARD_SCORE_HIGH))
                .asConstraint("docenteNoSuperpuesto");
    }
    private boolean bloquesSeSuperponen(BloqueHorario b1, BloqueHorario b2) {
        return !(b1.getHoraFin().isBefore(b2.getHoraInicio()) || b2.getHoraFin().isBefore(b1.getHoraInicio()));
    }


    private Constraint seccionNoSuperpuesta(ConstraintFactory factory) {
        return factory.forEachUniquePair(AsignacionHorario.class,
                        equal(this::extraerSeccion))
                .filter((a1, a2) -> {
                    BloqueHorario b1 = a1.getBloqueHorario();
                    BloqueHorario b2 = a2.getBloqueHorario();
                    return b1 != null && b1.equals(b2);
                })
                .penalize(HardSoftScore.ONE_HARD.multiply(HARD_SCORE_HIGH))
                .asConstraint("seccionNoSuperpuesta");
    }


    private Constraint limitarHorasContratadasPorDocente(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .groupBy(this::extraerDocente,
                        ConstraintCollectors.sum(a -> calcularHorasBloque(a.getBloqueHorario())))
                .filter((docente, totalHoras) ->
                        docente != null && docente.getHorasContratadas() != null &&
                                totalHoras > docente.getHorasContratadas())
                .penalize(HardSoftScore.ONE_HARD.multiply(5000),
                        (docente, totalHoras) -> totalHoras - docente.getHorasContratadas())
                .asConstraint("limitarHorasContratadasPorDocente");
    }

    private Constraint evitarRestriccionesDocente(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .filter(this::tieneRestriccionBloqueada)
                .penalize(HardSoftScore.ONE_HARD.multiply(100))
                .asConstraint("evitarRestriccionesDocente");
    }

    // ================= RESTRICCIONES SOFT =================

    private Constraint incentivarDistribucionDeBloques(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .groupBy(AsignacionHorario::getBloqueHorario, ConstraintCollectors.count())
                .penalize(HardSoftScore.ONE_SOFT.multiply(1), (bloque, count) -> count * count)
                .asConstraint("incentivarDistribucionDeBloques");
    }


    private Constraint fomentarUsoDistribuidoDeAulas(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .groupBy(AsignacionHorario::getAula, ConstraintCollectors.count())
                .penalize(HardSoftScore.ONE_SOFT, (aula, count) -> count * count)
                .asConstraint("fomentarUsoDistribuidoDeAulas");
    }


    private Constraint incentivarUsoDeMasBloques(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .groupBy(AsignacionHorario::getBloqueHorario)
                .reward(HardSoftScore.ONE_SOFT.multiply(SOFT_SCORE_MEDIUM))
                .asConstraint("incentivarUsoDeMasBloques");
    }


    private Constraint cumplirHorasRequeridas(ConstraintFactory factory) {
        return factory
                .forEach(AsignacionHorario.class)
                .groupBy(AsignacionHorario::getCursoSeccionDocente,
                        ConstraintCollectors.sum(a -> calcularHorasBloque(a.getBloqueHorario())))
                .filter((csd, horasAsignadas) -> {
                    Integer horasRequeridas = csd.getCurso().getHorasSemanales();
                    return horasRequeridas != null && horasAsignadas < horasRequeridas;
                })
                .penalize(HardSoftScore.ONE_SOFT.multiply(SOFT_SCORE_HIGH),
                        (csd, horasAsignadas) -> {
                            Integer horasRequeridas = csd.getCurso().getHorasSemanales();
                            return horasRequeridas - horasAsignadas;
                        })
                .asConstraint("cumplirHorasRequeridas");
    }

    private Constraint incentivarCompartirBloquesSinCruces(ConstraintFactory factory) {
        return factory
                .forEachUniquePair(AsignacionHorario.class,
                        Joiners.equal(AsignacionHorario::getBloqueHorario),
                        Joiners.filtering((a1, a2) ->
                                !a1.getCursoSeccionDocente().equals(a2.getCursoSeccionDocente()) &&
                                        !Objects.equals(a1.getAula(), a2.getAula()) &&
                                        !Objects.equals(extraerDocente(a1), extraerDocente(a2)) &&
                                        !Objects.equals(extraerSeccion(a1), extraerSeccion(a2))
                        )
                )
                .reward(HardSoftScore.ONE_SOFT.multiply(SOFT_SCORE_MEDIUM))
                .asConstraint("incentivarCompartirBloquesSinCruces");
    }

}