package redmopag.computer_modeling;

import redmopag.computer_modeling.intensity.CafeIntensity;
import redmopag.computer_modeling.intensity.Intensity;
import redmopag.computer_modeling.smo.SMO;

public class Main {
    public static void main(String[] args) {
        double tStart = 8;
        double T = 22;
        int simulationDays = 100;
        double meanServiceTime = 0.05;
        Intensity cafeIntensity = new CafeIntensity();

        SMO smo = new SMO(tStart, T, simulationDays, cafeIntensity, meanServiceTime);
        smo.doSimulation();
        smo.printStatistic();
    }
}
