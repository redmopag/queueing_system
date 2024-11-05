package redmopag.computer_modeling.poisson_processes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomogeneousPoissonProcess implements PoissonProcess{

    private final double T;
    private final double h;
    private final double tStart;
    private final Random random = new Random();

    public HomogeneousPoissonProcess(double h, double tStart, double T){
        this.h = h;
        this.T = T;
        this.tStart = tStart;
    }

    // Генерирование времени прибытия клиента по экспоненциальному распределению
    private double generateTime(){
        double u = random.nextDouble(); // Uniform[0,1] - равномерное распределение
        return -Math.log(1-u) / h; // Экспоненциальное распределение
    }

    public List<Double> generateProcess(int day) {
        List<Double> times = new ArrayList<>();
        double t = tStart;

        while(true){
            t += generateTime();

            if(t > T){
                break;
            } else{
                times.add(t);
            }
        }

        return times;
    }
}
