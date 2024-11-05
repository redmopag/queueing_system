package redmopag.computer_modeling.intensity;

import java.util.List;

public class CafeIntensity  implements Intensity{

    @Override
    public double intensity(double t, int day) { // t измеряется в часах
        return timeIntensity(t) * dayIntensity(day);
    }

    /*
    Возвращает интенсивность по времени
     */
    private double timeIntensity(double t){
        if(10.5 <= t && t < 10.80){
            return 50;
        } else if(10.80 <= t && t < 11){
            return 40;
        } else if (11 <= t && t < 11.5){
            return 30;
        } else if(14.5 <= t && t < 14.80){
            return 35;
        } else if(14.80 <= t && t < 15){
            return 27;
        } else if (15 <= t && t < 15.5) {
            return 20;
        } else {
            return 15;
        }
    }

    /*
    Интенсивность по дням
     */
    private double dayIntensity(int day){
        if (1 <= day && day < 32){
            return 0.6;
        } else if (122 <= day && day < 141){
            return 0.7;
        } else if (162 <= day && day < 216){
            return 0.5;
        } else if (336 <= day && day < 351) {
            return 0.8;
        } else {
            return 1;
        }
    }

    /*
    Возвращает возможные значения timeIntensity
     */
    @Override
    public List<Double> getValues(){
        return List.of(50.0, 40.0, 30.0, 35.0, 27.0, 20.0, 15.0);
    }
}
