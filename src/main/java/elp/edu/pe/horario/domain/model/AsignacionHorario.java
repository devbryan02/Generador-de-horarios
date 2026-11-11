package elp.edu.pe.horario.domain.model;

import elp.edu.pe.horario.domain.solver.AsignacionHorarioDifficultyComparator;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import java.util.UUID;

@PlanningEntity(comparatorClass = AsignacionHorarioDifficultyComparator.class)
public class AsignacionHorario {

    @PlanningId
    private UUID id;

    private CursoSeccionDocente cursoSeccionDocente;

    @PlanningVariable(valueRangeProviderRefs = "aulaRange", allowsUnassigned = false)
    private Aula aula;

    @PlanningVariable(valueRangeProviderRefs = "bloqueHorarioRange", allowsUnassigned = false)
    private BloqueHorario bloqueHorario;

    // Constructor por defecto requerido por OptaPlanner
    public AsignacionHorario() {
    }

    public AsignacionHorario(UUID id, CursoSeccionDocente cursoSeccionDocente, Aula aula, BloqueHorario bloqueHorario) {
        this.id = id;
        this.cursoSeccionDocente = cursoSeccionDocente;
        this.aula = aula;
        this.bloqueHorario = bloqueHorario;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CursoSeccionDocente getCursoSeccionDocente() {
        return cursoSeccionDocente;
    }

    public void setCursoSeccionDocente(CursoSeccionDocente cursoSeccionDocente) {
        this.cursoSeccionDocente = cursoSeccionDocente;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public BloqueHorario getBloqueHorario() {
        return bloqueHorario;
    }

    public void setBloqueHorario(BloqueHorario bloqueHorario) {
        this.bloqueHorario = bloqueHorario;
    }

    @Override
    public String toString() {
        return "AsignacionHorario{" +
                "id=" + id +
                ", cursoSeccionDocente=" + cursoSeccionDocente +
                ", aula=" + aula +
                ", bloqueHorario=" + bloqueHorario +
                '}';
    }
}