package redmopag.computer_modeling.poisson_processes;

import redmopag.computer_modeling.intensity.Intensity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NonHomogeneousPoissonProcess implements PoissonProcess{

    private final Intensity intensity; // Среднее число клиентов за единицу времени
    private final double T; // Период работы СМО
    private final double tStart; // Начало работы СМО
    private final double h;

    private final Random random = new Random();

    public NonHomogeneousPoissonProcess(Intensity intensity, double tStart, double T){
        this.intensity = intensity;
        this.tStart = tStart;
        this.T = T;

        double max = 0;
        for(var value : this.intensity.getValues()){
            if(value > max){
                max = value;
            }
        }

        h = max;
    }

    // Генерирование времени прибытия клиента по экспоненциальному распределению
    private double generateTime(){
        double u = random.nextDouble(); // Uniform[0,1] - равномерное распределение
        return -Math.log(1-u) / h; // Экспоненциальное распределение. Генерируется в часах
    }

    // Генерирование неоднородного Пуассоновского процесса: прибытие клиентов в кафе
    public List<Double> generateProcess(){
        if(!isIntensityCorrect()){
            throw new RuntimeException("Функция интенсивности не удовлетворяет условию 0 <= h(t)/h <= 1");
        }

        List<Double> times = new ArrayList<>();
        double t = this.tStart;

        while(true){
            t += generateTime();

            if(t > T){ // Если время прихода клиента превысило период работы СМО, то конец
                break;
            } else { // Иначе генерируем неоднородность
                double u = random.nextDouble();
                if (u <= intensity.intensity(t) / h) {
                    times.add(t);
                }
            }
        }

        return times;
    }

    private boolean isIntensityCorrect(){
        double result;

        for(var value : intensity.getValues()){
            result = value / h;
            System.out.println("h(t) = " + value + ", h = " + h + ", h(t)/h = " + result);

            if(0 > result || result > 1){
                return false;
            }
        }

        return true;
    }
}
