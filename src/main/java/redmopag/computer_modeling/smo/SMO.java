package redmopag.computer_modeling.smo;

import redmopag.computer_modeling.poisson_processes.PoissonProcess;

import java.util.List;

public class SMO {

    private final double T;
    private final double tStart;
    private final int simulationDays;
    private final PoissonProcess poissonProcess;

    public SMO(double tStart, double T, int simulationDays, PoissonProcess poissonProcess) {
        this.T = T;
        this.tStart = tStart;
        this.simulationDays = simulationDays;
        this.poissonProcess = poissonProcess;
    }

    public void doSimulation(){
        for(int i = 1; i <= simulationDays; ++i){
        List<Double> arrivals = poissonProcess.generateProcess();
        }
    }
}
