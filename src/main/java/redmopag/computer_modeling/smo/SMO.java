package redmopag.computer_modeling.smo;

import redmopag.computer_modeling.intensity.Intensity;
import redmopag.computer_modeling.models.Client;
import redmopag.computer_modeling.poisson_processes.NonHomogeneousPoissonProcess;
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
    private List<Map.Entry<Double, Integer>> Q = new ArrayList<>(); // Изменения кол-ва клиентов в системе: время, кол-во клиентов
    // Интервалы занятости устройства: время занятия, время освобождения
    private List<Map.Entry<Double, Double>> busyIntervals = new ArrayList<>();
    private double startBusy;

    public SMO(double tStart, double T, int simulationDays,
               Intensity intensity, double meanServiceTime) {
        this.T = T;
        this.tStart = tStart;
        this.simulationDays = simulationDays;
        this.poissonProcess = new NonHomogeneousPoissonProcess(intensity, tStart, T);
        this.meanServiceTime = meanServiceTime;
    }

    /*
    Основная функция - симуляция СМО
     */
    public void doSimulation(){
        for(int day = 1; day <= simulationDays; ++day){
            td = Double.MAX_VALUE; n = 0; // Инициализация переменных
            List<Double> arrivals = poissonProcess.generateProcess(day); // Генерация прибытия клиентов

            for(var clientArrival : arrivals){
                if(clientArrival <= td && clientArrival <= T){ // Случай - прибытие клиента
                    clientArrivalHandler(clientArrival, day);
                } else if(td < clientArrival && td <= T){ // Случай - уход клиента
                    clientDepartureHandler(clientArrival, day);
                } else if(Math.min(clientArrival, td) > T && n > 0){ // Случай - переработка (обслуживание после T)
                    overworkHandler(day);
                } else if(Math.min(clientArrival, td) > T && n == 0){ // Случай - конец работы
                    endWorkHandler(day);
                }
            }
        }
    }

    /*
    Обработчик прибытия клиента
     */
    private void clientArrivalHandler(double t, int serviceDay){
        ++n; ++Na;

        // Добавляем клиента в очередь
        Client client = new Client();
        client.setArrivalTime(t);
        clientsQueue.add(client);

        // Фиксируем изменение кол-ва человек в системе
        Q.add(new AbstractMap.SimpleEntry<>(t, n));

        if(n == 1){ // Если устройство свободно, то обслуживаем клиента
            double serviceTime = serviceTimeGeneration();
            td = t + serviceTime;

            client.setWaitingTime(0);
            client.setDepartureTime(td);
            client.setServiceDay(serviceDay);

            // Фиксируем начало занятости устройства
            startBusy = t;
        }
    }

    /*
    Обработчик ухода клиента
     */
    private void clientDepartureHandler(double t, int serviceDay){
        --n; ++Nd;
        clientsQueue.remove(); // Убираем обслуженного человека из очереди
        Q.add(new AbstractMap.SimpleEntry<>(td, n)); // Фиксируем изменения кол-во человек в системе

        if (n == 0){ // Если пользователей нет, то обрабатываем приход клиента
            clientArrivalHandler(t, serviceDay);
            busyIntervals.add(new AbstractMap.SimpleEntry<>(startBusy, td)); // Фиксируем интервал занятости устройства
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
        Q.add(new AbstractMap.SimpleEntry<>(td,n));

        if(n > 0){ // Если в системе остались пользователи после времени T, то нужно их обслужить
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

    /*
    Среднее время клиента в системе
     */
    public double averageTimeInSystem(){
        double sum = 0;
        for(var client : clients){
            sum += client.getDepartureTime() - client.getArrivalTime();
        }

        return sum / Na;
    }

    /*
    Среднее время ожидания клиента в системе
     */
    public double averageWaitingTime(){
        double sum = 0;
        for(var client : clients){
            sum += client.getWaitingTime();
        }

        return sum / Na;
    }

    /*
    Среднее число клиентов в очереди
     */
    public double averageClientsInQueue(){
        double lastTime = 0;
        double totalQ = 0;
        double totalTime = (T - tStart) * simulationDays; // Всё время работы СМО

        for(var qCurrent : Q){
            totalQ += (qCurrent.getKey() - lastTime) * (qCurrent.getValue() - 1);
        }

        return totalQ / totalTime;
    }

    /*
    Оценка занятости устройства
     */
    public double deviceOccupancy(){
        double totalBusyTime = 0;
        double totalTime = (T - tStart) * simulationDays;

        for(var busyInterval : busyIntervals){
            // сумма (tEnd - tStart) занятости устройства
            totalBusyTime += (busyInterval.getValue() - busyInterval.getKey());
        }

        return totalBusyTime / totalTime;
    }

    public void printStatistic(){
        System.out.println("Результаты моделирования:");
        System.out.println("Общее число прибытий Na: " + Na);
        System.out.println("Общее число уходов Nd: " + Nd);
        System.out.println("Время переработки Tp" + Tp);
        System.out.println("Среднее время пребывания клиента в системе St: " + averageTimeInSystem());
        System.out.println("Среднее время ожидания в очереди Wср: " + averageWaitingTime());
        System.out.println("Среднее число клиентов в очереди Qср: " + averageClientsInQueue());
        System.out.println("Занятость устройства p: " + deviceOccupancy());

        System.out.println("Данные о клиентах:");

        String[] headers = {"Клиент", "День", "Приход", "Ожидание", "Уход"};
        int[] columnWidths = {6, 4, 6, 8, 4};

        for(int i = 0; i < headers.length; ++i){
            System.out.printf("%-" + columnWidths[i] + "s", headers[i]);
        }

        for(int i = 0; i < clients.size(); ++i){
            System.out.printf("%-6d", i + 1);
            System.out.printf("%-4d", clients.get(i).getServiceDay());
            System.out.printf("%-6.2f", clients.get(i).getArrivalTime());
            System.out.printf("%-8.2f", clients.get(i).getWaitingTime());
            System.out.printf("%-4.2f", clients.get(i).getDepartureTime());
        }
    }
}
