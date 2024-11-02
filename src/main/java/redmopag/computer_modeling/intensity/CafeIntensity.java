package redmopag.computer_modeling.intensity;

import java.util.List;

public class CafeIntensity  implements Intensity{

    @Override
    public double intensity(double t) { // t измеряется в часах
        if(10.5 <= t && t < 11){
            return 100;
        } else if(11 <= t && t < 11.5){
            return 75;
        } else if(14.5 <= t && t < 15){
            return 80;
        } else if(15 <= t && t < 15.5){
            return 50;
        } else {
            return 30;
        }
    }

    @Override
    public List<Double> getValues(){
        return List.of(100.0, 70.0, 80.0, 50.0, 30.0);
    }
}
