package org.kie.server.api.model.instance;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.Score;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Arrays;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "solver-instance")
@XStreamAlias( "solver-instance" )
public class SolverInstance {

    public static enum SolverStatus {
        NOT_SOLVING, TERMINATING_EARLY, SOLVING
    }

    @XmlElement(name = "container-id")
    @XStreamAlias("container-id")
    private String containerId;

    @XmlElement(name = "solver-id")
    @XStreamAlias("solver-id")
    private String solverId;

    @XmlElement(name = "solver-config-file")
    @XStreamAlias("solver-config-file")
    private String solverConfigFile;

    @XmlElement(name = "status")
    @XStreamAlias("status")
    private SolverStatus status;

    @XmlElement(name = "score")
    @XStreamAlias("score")
    // TODO https://issues.jboss.org/browse/PLANNER-604 this might be corrupted during marshalling and it's not tested
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Score score;

    @XmlElement(name = "planning-problem")
    @XStreamAlias("planning-problem")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Solution planningProblem;

    @XmlElement(name = "best-solution")
    @XStreamAlias("best-solution")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Solution bestSolution;

    public SolverInstance() {
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getSolverId() {
        return solverId;
    }

    public void setSolverId(String solverId) {
        this.solverId = solverId;
    }

    public String getSolverConfigFile() {
        return solverConfigFile;
    }

    public void setSolverConfigFile(String solverConfigFile) {
        this.solverConfigFile = solverConfigFile;
    }

    public SolverStatus getStatus() {
        return status;
    }

    public void setStatus(SolverStatus status) {
        this.status = status;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Solution getPlanningProblem() {
        return planningProblem;
    }

    public void setPlanningProblem(Solution planningProblem) {
        this.planningProblem = planningProblem;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(Solution bestSolution) {
        this.bestSolution = bestSolution;
    }

    @Override
    public String toString() {
        return "SolverInstance{" +
               "containerId='" + containerId + '\'' +
               ", solverId='" + solverId + '\'' +
               ", solverConfigFile='" + solverConfigFile + '\'' +
               ", status=" + status +
               ", score=" + score +
               '}';
    }

    public String getSolverInstanceKey() {
        return getSolverInstanceKey( this.containerId, this.solverId );
    }

    public static String getSolverInstanceKey( String containerId, String solverId ) {
        return containerId + "/" + solverId;
    }

}
