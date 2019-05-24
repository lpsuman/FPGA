package hr.fer.zemris.dipl.lukasuman.fpga.gui.session;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BooleanSolver;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SessionData implements Serializable {

    private static final long serialVersionUID = -4214144882641247429L;

    private Path filePath;
    private boolean isEdited;

    private List<BooleanFunction> boolFunctions;
    private List<BooleanVector> boolVectors;
    private List<BoolVecProblem> boolProblems;
    private List<BooleanSolver> boolSolvers;
    private List<BoolVectorSolution> boolSolutions;

    public SessionData() {
        boolFunctions = new ArrayList<>();
        boolVectors = new ArrayList<>();
        boolProblems = new ArrayList<>();
        boolSolvers = new ArrayList<>();
        boolSolutions = new ArrayList<>();
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public List<BooleanFunction> getBoolFunctions() {
        return boolFunctions;
    }

    public List<BooleanVector> getBoolVectors() {
        return boolVectors;
    }

    public List<BoolVecProblem> getBoolProblems() {
        return boolProblems;
    }

    public List<BooleanSolver> getBoolSolvers() {
        return boolSolvers;
    }

    public List<BoolVectorSolution> getBoolSolutions() {
        return boolSolutions;
    }
}
