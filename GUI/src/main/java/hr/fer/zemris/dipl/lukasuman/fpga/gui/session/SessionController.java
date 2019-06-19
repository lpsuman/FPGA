package hr.fer.zemris.dipl.lukasuman.fpga.gui.session;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BooleanSolver;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BooleanSolverConfig;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.SolverMode;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve.RunSolverAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemAdapter;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.AnnealedThreadPoolConfig;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.ParallelGAConfig;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

public class SessionController {

    private SessionData sessionData;
    private JLabel iconLabel;
    private boolean isEdited;
    private JPanel mainPanel;

    private JFPGA jfpga;
    private BooleanFunctionController booleanFunctionController;
    private BooleanVectorController booleanVectorController;
    private SolverController solverController;

    private BooleanSolver solver;

    public SessionController(SessionData sessionData, JFPGA jfpga, JLabel iconPanel) {
        this.sessionData = Utility.checkNull(sessionData, "session data");
        this.jfpga = Utility.checkNull(jfpga, "jfpga");
        this.iconLabel = Utility.checkNull(iconPanel, "icon panel");
        loadSessionData();
        initGUI();
    }

    private void loadSessionData() {
        booleanFunctionController = new BooleanFunctionController(this);
        booleanVectorController = new BooleanVectorController(this);
        solverController = new SolverController(this);

        booleanFunctionController.addBooleanFunctionListener(new BooleanFunctionAdapter() {
            @Override
            public void itemAdded(BooleanFunction item, int indexInTable) {
                updateVectorBeingEdited();
            }

            @Override
            public void itemRemoved(BooleanFunction item, int indexInTable) {
                updateVectorBeingEdited();
            }

            @Override
            public void itemListChanged() {
                booleanVectorController.stopEditingVector();
            }

            @Override
            public void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int index, List<String> oldInputs) {
                updateVectorBeingEdited();
            }

            @Override
            public void booleanFunctionTableEdited(BooleanFunction booleanFunction, int index, BitSet oldTable) {
                updateVectorBeingEdited();
            }
        });

        booleanFunctionController.addBooleanFunctionListener(new BooleanFunctionAdapter() {
            @Override
            public void itemRemoved(BooleanFunction item, int indexInTable) {
                jfpga.getUndoRemoveFunctionAction().setEnabled(true);
            }
        });

        booleanVectorController.addBooleanVectorListener(new BooleanVectorAdapter() {
            @Override
            public void itemRemoved(BooleanVector item, int indexInTable) {
                jfpga.getUndoRemoveVectorAction().setEnabled(true);
            }
        });

        solverController.addSolutionListener(new TableItemAdapter<>() {
            @Override
            public void itemRemoved(BoolVectorSolution item, int indexInTable) {
                jfpga.getUndoRemoveSolutionAction().setEnabled(true);
            }
        });
    }

    private void updateVectorBeingEdited() {
        if (booleanFunctionController.getItems() != booleanFunctionController.getAllItems()) {
            booleanVectorController.updateVectorBeingEdited(booleanFunctionController.getAllItems());
        }
    }

    private void initGUI() {
        JPanelPair outerAndInnerPair = GUIUtility.putGridBagInBorderCenter();
        mainPanel = outerAndInnerPair.getUpperPanel();

        GridBagConstraints gbc = GUIUtility.getGBC(0, 0, 0.3, 0.5);
        mainPanel.add(booleanFunctionController.getMainPanel(), gbc);

        gbc = GUIUtility.getGBC(1, 0, 0.1, 0.5);
        mainPanel.add(booleanVectorController.getMainPanel(), gbc);

        gbc = GUIUtility.getGBC(2, 0, 0.4, 0.5);
        mainPanel.add(solverController.getMainPanel(), gbc);

        mainPanel = outerAndInnerPair.getLowerPanel();
    }

    public void updateAsCurrentSession() {
        booleanFunctionController.updateActionsAreEnabled();
        booleanVectorController.updateActionsAreEnabled();
        solverController.updateActionsAreEnabled();
    }

    public void runBooleanSolver(BoolVecProblem problem, SolverMode solverMode) {
        BooleanSolverConfig booleanSolverConfig = new BooleanSolverConfig()
                .solveIndividually(solverController.getSolveIndividuallyCheckBox().isSelected())
                .printOnlyBestSolution(solverController.getPrintOnlyBestCheckBox().isSelected())
                .useStatistics(solverController.getUseStatisticsCheckBox().isSelected())
                .printOnlyGlobalStatistics(solverController.getPrintOnlyGlobalStatisticsCheckBox().isSelected())
                .maxNumFails((Integer) solverController.getMaxNumFailsFTF().getValue())
                .maxNumBelowThresholdAttempts((Double) solverController.getMaxBelowAttemptsFTF().getValue())
                .noBestThresholdToStopTrying((Double) solverController.getNoBestThresholdFTF().getValue())
                .bestExistsThresholdToStopTrying((Double) solverController.getBestExistsThresholdFTF().getValue());

        solver = new BooleanSolver(solverMode, solution -> {
            if (solution != null) {
                getSolverController().addItem(solution);
            }
        }, booleanSolverConfig);

        ParallelGAConfig parallelGAConfig = new ParallelGAConfig()
                .populationSize((Integer) solverController.getPopulationSizeFTF().getValue())
                .maxGenerations((Integer) solverController.getMaxGenerationsFTF().getValue())
                .elitismSize((Integer) solverController.getElitismSizeFTF().getValue());
        solver.setAlgorithmConfig(parallelGAConfig);

        AnnealedThreadPoolConfig threadPoolConfig = new AnnealedThreadPoolConfig()
                .numThreads((Integer) solverController.getNumThreadsFTF().getValue())
                .annealingThreshold((Double) solverController.getAnnealingThresholdFTF().getValue());
        solver.setThreadPoolConfig(threadPoolConfig);

        solver.setMutationChance((Double) solverController.getMutationChanceFTF().getValue());

        SwingWorker<BoolVectorSolution, Void> worker = new SwingWorker<>() {
            @Override
            protected BoolVectorSolution doInBackground() {
                try {
                    return solver.solve(problem);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }

                return null;
            }

            @Override
            protected void done() {
                booleanSolverStopped();
            }
        };

        jfpga.getRunSolverAction().setEnabled(false);
        jfpga.getStopSolverAction().setEnabled(true);
        System.setOut(solverController.getTextAreaOutputStream());

        if (GUIConstants.SHOW_ERRORS_IN_GUI) {
            System.setErr(solverController.getTextAreaOutputStream());
        }

        worker.execute();
    }

    public void stopBooleanSolver() {
        if (solver != null) {
            solver.stop();
            booleanSolverStopped();
        }
    }

    private void booleanSolverStopped() {
        jfpga.getRunSolverAction().setEnabled(true);
        jfpga.getStopSolverAction().setEnabled(false);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    public JFPGA getJfpga() {
        return jfpga;
    }

    public LocalizationProvider getLocProv() {
        return jfpga.getFlp();
    }

    public BooleanFunctionController getBooleanFunctionController() {
        return booleanFunctionController;
    }

    public BooleanVectorController getBooleanVectorController() {
        return booleanVectorController;
    }

    public SolverController getSolverController() {
        return solverController;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
        if (isEdited) {
            iconLabel.setIcon(jfpga.getRedDiskette());
        } else {
            iconLabel.setIcon(jfpga.getBlueDiskette());
        }
    }

    public JLabel getIconLabel() {
        return iconLabel;
    }
}
