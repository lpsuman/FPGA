package hr.fer.zemris.dipl.lukasuman.fpga.gui.session;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BooleanSolver;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SessionData implements Serializable {

    private static final long serialVersionUID = -4214144882641247429L;

    private String filePath;

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
        if (filePath == null) {
            return null;
        }

        return Paths.get(filePath);
    }

    public void setFilePath(Path filePath) {
        if (filePath == null) {
            this.filePath = null;
            return;
        }

        this.filePath = filePath.toString();
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

    public static SessionData deserializeFromFile(String filePath) throws IOException, ClassNotFoundException {
        SessionData result = null;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            result = (SessionData) in.readObject();
        }

        return result;
    }

    public static void serializeToFile(SessionData sessionData, String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(sessionData);
            System.out.println("Session data is saved in: " + filePath);
        }
    }
}
