package redmopag.computer_modeling.intensity;

import java.util.List;

public interface Intensity {

    double intensity(double t, int day); // Получение значения интенсивности исходя из времени и дня

    List<Double> getValues(); // Возможные значения функции интенсивности
}
