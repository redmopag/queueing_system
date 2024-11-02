package redmopag.computer_modeling.intensity;

import java.util.List;

public interface Intensity {
    double intensity(double t);
    List<Double> getValues();
}
