package org.kie.server.api.model.instance;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.optaplanner.core.api.score.Score;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
    private ScoreWrapper scoreWrapper;

    @XmlElement(name = "planning-problem")
    @XStreamAlias("planning-problem")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Object planningProblem;

    @XmlElement(name = "best-solution")
    @XStreamAlias("best-solution")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Object bestSolution;

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

    public ScoreWrapper getScoreWrapper() {
        return scoreWrapper;
    }

    public void setScoreWrapper( ScoreWrapper scoreWrapper ) {
        this.scoreWrapper = scoreWrapper;
    }

    public Object getPlanningProblem() {
        return planningProblem;
    }

    public void setPlanningProblem(Object planningProblem) {
        this.planningProblem = planningProblem;
    }

    public Object getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(Object bestSolution) {
        this.bestSolution = bestSolution;
    }

    @Override
    public String toString() {
        return "SolverInstance{" +
               "containerId='" + containerId + '\'' +
               ", solverId='" + solverId + '\'' +
               ", solverConfigFile='" + solverConfigFile + '\'' +
               ", status=" + status +
               ", scoreWrapper=" + scoreWrapper +
               '}';
    }

    public String getSolverInstanceKey() {
        return getSolverInstanceKey( this.containerId, this.solverId );
    }

    public static String getSolverInstanceKey( String containerId, String solverId ) {
        return containerId + "/" + solverId;
    }

}
