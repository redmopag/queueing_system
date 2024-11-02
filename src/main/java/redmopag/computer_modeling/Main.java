package redmopag.computer_modeling;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import redmopag.computer_modeling.poisson_processes.HomogeneousPoissonProcess;
import redmopag.computer_modeling.poisson_processes.NonHomogeneousPoissonProcess;
import redmopag.computer_modeling.poisson_processes.PoissonProcess;
import redmopag.computer_modeling.intensity.CafeIntensity;

import javax.swing.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        double tStart = 0;
        double T = 24;
        double h = 21.65;

        System.out.println("start: " + tStart);
        System.out.println("T: " + T);

        System.out.println("Однородный пуассоновский процесс");
        System.out.println("h: " + h);

        PoissonProcess poissonProcess = new HomogeneousPoissonProcess(h, tStart, T);
        List<Double> process = poissonProcess.generateProcess();
        showHistogram(process, "Однородный пуассоновский процесс");
        System.out.println(process);

        System.out.println();

        System.out.println("Неоднородный пуассоновский процесс");
        poissonProcess = new NonHomogeneousPoissonProcess(new CafeIntensity(), tStart, T);
        process = poissonProcess.generateProcess();
        showHistogram(process, "Неоднородный пуассоновский процесс");
        System.out.println(process);
    }

    private static void showHistogram(List<Double> events, String title){
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries(
                "События",
                events.stream().mapToDouble(Double::doubleValue).toArray(),
                24,
                0,
                24
        );

        // Создаем гистограмму
        JFreeChart histogram = ChartFactory.createHistogram(
                title,
                "Час",
                "Кол-во событий",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ChartPanel(histogram));
        frame.pack();
        frame.setVisible(true);
    }
}
