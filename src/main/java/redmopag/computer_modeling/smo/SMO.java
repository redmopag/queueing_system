package redmopag.computer_modeling.smo;

import redmopag.computer_modeling.models.Client;
import redmopag.computer_modeling.poisson_processes.PoissonProcess;

import java.util.*;

public class SMO {

    private final double T; // Конец дневной работы СМО
    private final double tStart; // Начало дневной работы СМО
    private final int simulationDays; // Кол-во дней симуляции
    private final PoissonProcess poissonProcess; // Для генерации времени прибытия клиентов
    private final double meanServiceTime; // Среднее время обслуживания клиентов в часах

    private List<Client> clients = new LinkedList<>(); // Список клиентов
    private Queue<Client> clientsQueue = new LinkedList<>(); // Очередь клиентов на обслуживание

    private double td; // Время ухода обслуживаемого клиента
    private int n; // Общее кол-во клиентов в системе
    private int Na = 0, Nd = 0; // Кол-во пришедших и ушедших клиентов
    private double Tp = 0; // Время переработки

    public SMO(double tStart, double T, int simulationDays,
               PoissonProcess poissonProcess, double meanServiceTime) {
        this.T = T;
        this.tStart = tStart;
        this.simulationDays = simulationDays;
        this.poissonProcess = poissonProcess;
        this.meanServiceTime = meanServiceTime;
    }

    /*
    Основная функция - симуляция СМО
     */
    public void doSimulation(){
        for(int i = 1; i <= simulationDays; ++i){
            td = Double.MAX_VALUE; n = 0; // Инициализация переменных
            List<Double> arrivals = poissonProcess.generateProcess(); // Генерация прибытия клиентов

            for(var clientArrival : arrivals){
                if(clientArrival <= td && clientArrival <= T){ // Случай - прибытие клиента
                    clientArrivalHandler(clientArrival, i);
                } else if(td < clientArrival && td <= T){ // Случай - уход клиента
                    clientDepartureHandler(clientArrival, i);
                } else if(Math.min(clientArrival, td) > T && n > 0){ // Случай - переработка (обслуживание после T)
                    overworkHandler(i);
                } else if(Math.min(clientArrival, td) > T && n == 0){ // Случай - конец работы
                    endWorkHandler(i);
                }
            }
        }
    }

    /*
    Обработчик прибытия клиента
     */
    private void clientArrivalHandler(double t, int serviceDay){
        ++n; ++Na;
        Client client = new Client();
        client.setArrivalTime(t);
        clientsQueue.add(client);

        if(n == 1){ // Если устройство свободно
            double serviceTime = serviceTimeGeneration();
            td = t + serviceTime;

            client.setWaitingTime(0);
            client.setDepartureTime(td);
            client.setServiceDay(serviceDay);
        }
    }

    /*
    Обработчик ухода клиента
     */
    private void clientDepartureHandler(double t, int serviceDay){
        --n; ++Nd;
        clientsQueue.remove();

        if (n == 0){ // Если пользователей нет, то обрабатываем приход клиента
            clientArrivalHandler(t, serviceDay);
        } else { // Иначе подсчитываем обслуживание пришедшего клиента, учитывая его ожидание
            Client nextClient = clientsQueue.remove();
            double serviceTime = serviceTimeGeneration();

            nextClient.setWaitingTime(td - nextClient.getArrivalTime()); // Время ожидания клиента в очереди
            td = td + serviceTime; // td = ta + (W = td - ta) + serviceTime
            nextClient.setDepartureTime(td);
            nextClient.setServiceDay(serviceDay);
        }
    }

    /*
    Высчитывание переработки
     */
    private void overworkHandler(int serviceDay){
        --n; ++Nd;
        clientsQueue.remove();

        if(n > 0){
            Client nextClient = clientsQueue.remove();
            double serviceTime = serviceTimeGeneration();

            nextClient.setWaitingTime(td - nextClient.getArrivalTime()); // Время ожидания клиента в очереди
            td = td + serviceTime; // td = ta + (W = td - ta) + serviceTime
            nextClient.setDepartureTime(td);
            nextClient.setServiceDay(serviceDay);
        }
    }

    /*
    Завершение дневной симуляции
     */
    private void endWorkHandler(int serviceDay){
        Tp += Math.max(td - T, 0);
    }

    /*
    Генерация времени обслуживания клиента устройством
     */
    private double serviceTimeGeneration(){
        Random random = new Random();
        double u = random.nextDouble();

        return -(1/meanServiceTime)*Math.log(1-u);
    }

    public void printStatistic(){
        System.out.println("Результаты моделирования:");
        System.out.println("Общее число прибытий Na: " + Na);
        System.out.println("Общее число уходов Nd: " + Nd);
        System.out.println("Время переработки Tp" + Tp);
        System.out.println();
    }
}
