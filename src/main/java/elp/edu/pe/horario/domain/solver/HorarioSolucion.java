package elp.edu.pe.horario.domain.solver;

import elp.edu.pe.horario.domain.model.*;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@PlanningSolution
public class HorarioSolucion {

    //Datos fijos para usar de referencia
    @ProblemFactCollectionProperty
    private List<Aula> aulaList;

    @ProblemFactCollectionProperty
    private List<BloqueHorario> bloqueHorarioList;

    @ProblemFactCollectionProperty
    private List<CursoSeccionDocente> cursoSeccionDocentes;

    // Entidad a planificar
    @PlanningEntityCollectionProperty
    private List<AsignacionHorario> asignacionHorarioList;

    //Score final
    @PlanningScore
    private HardSoftScore score;

    // Constructor por defecto requerido por OptaPlanner
    public HorarioSolucion() {
        this.score = HardSoftScore.ZERO;
    }

    //constructor con todos los atributos
    public HorarioSolucion(List<Aula> aulaList, HardSoftScore score, List<AsignacionHorario> asignacionHorarioList, List<CursoSeccionDocente> cursoSeccionDocentes, List<BloqueHorario> bloqueHorarioList) {
        this.aulaList = aulaList;
        this.score = score;
        this.asignacionHorarioList = asignacionHorarioList;
        this.cursoSeccionDocentes = cursoSeccionDocentes;
        this.bloqueHorarioList = bloqueHorarioList;
    }

    @ValueRangeProvider(id = "aulaRange")
    public List<Aula> getAulaList() {
        return aulaList;
    }

    public void setAulaList(List<Aula> aulaList) {
        this.aulaList = aulaList;
    }

    @ValueRangeProvider(id = "bloqueHorarioRange")
    public List<BloqueHorario> getBloqueHorarioList() {
        return bloqueHorarioList;
    }

    public void setBloqueHorarioList(List<BloqueHorario> bloqueHorarioList) {
        this.bloqueHorarioList = bloqueHorarioList;
    }

    public List<CursoSeccionDocente> getCursoSeccionDocentes() {
        return cursoSeccionDocentes;
    }

    public void setCursoSeccionDocentes(List<CursoSeccionDocente> cursoSeccionDocentes) {
        this.cursoSeccionDocentes = cursoSeccionDocentes;
    }

    public List<AsignacionHorario> getAsignacionHorarioList() {
        return asignacionHorarioList;
    }

    public void setAsignacionHorarioList(List<AsignacionHorario> asignacionHorarioList) {
        this.asignacionHorarioList = asignacionHorarioList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
