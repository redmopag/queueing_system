package redmopag.computer_modeling.smo;

import redmopag.computer_modeling.intensity.Intensity;
import redmopag.computer_modeling.models.Client;
import redmopag.computer_modeling.poisson_processes.NonHomogeneousPoissonProcess;
import redmopag.computer_modeling.poisson_processes.PoissonProcess;

import java.math.BigDecimal;
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
    private double ta; // Время прихода клиента
    private int n; // Общее кол-во клиентов в системе
    private int Na = 0, Nd = 0; // Кол-во пришедших и ушедших клиентов
    private double Tp = 0.0; // Время переработки
    // Изменения кол-ва клиентов в системе: время, кол-во клиентов
    private List<Map.Entry<Double, Integer>> Q = new ArrayList<>();
    // Интервалы занятости устройства: время занятия, время освобождения
    private List<Map.Entry<Double, Double>> busyIntervals = new ArrayList<>();
    private Queue<Double> arrivals;
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
            arrivals = new LinkedList<>(poissonProcess.generateProcess(day)); // Генерация прибытия клиентов
            ta = arrivals.remove(); td = Double.POSITIVE_INFINITY; n = 0; // Инициализация переменных

            while(true){
                if(ta <= td && ta <= T){ // Случай - прибытие клиента
                    clientArrivalHandler(ta, day);
                } else if(td < ta && td <= T){ // Случай - уход клиента
                    clientDepartureHandler(td, day);
                } else if(Math.min(ta, td) > T && n > 0){ // Случай - переработка (обслуживание после T)
                    overworkHandler(td, day);
                } else if(Math.min(ta, td) > T && n == 0){ // Случай - конец работы
                    endWorkHandler();
                    break;
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

        clients.add(client); // Запоминаем клиента

        // Фиксируем изменение кол-ва человек в системе
        Q.add(new AbstractMap.SimpleEntry<>(t, n));

        // Получение следующего времени
        ta = arrivals.isEmpty() ? (T + 1) : arrivals.remove();

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
        Q.add(new AbstractMap.SimpleEntry<>(t, n)); // Фиксируем изменения кол-во человек в системе

        if (n == 0){ // Если пользователей нет, то обрабатываем приход клиента
            busyIntervals.add(new AbstractMap.SimpleEntry<>(startBusy, t)); // Фиксируем интервал занятости устройства
            clientArrivalHandler(ta, serviceDay); // Устройство снова занимается
        } else { // Иначе подсчитываем обслуживание пришедшего клиента, учитывая его ожидание
            Client nextClient = clientsQueue.element();
            double serviceTime = serviceTimeGeneration();

            td = t + serviceTime; // td = ta + (W = td - ta) + serviceTime

            nextClient.setWaitingTime(t - nextClient.getArrivalTime()); // Время ожидания клиента в очереди: W = td - ta
            nextClient.setDepartureTime(td);
            nextClient.setServiceDay(serviceDay);
        }
    }

    /*
    Высчитывание переработки
     */
    private void overworkHandler(double t, int serviceDay){
        --n; ++Nd;

        clientsQueue.remove();
        Q.add(new AbstractMap.SimpleEntry<>(t,n));

        if(n > 0){ // Если в системе остались пользователи после времени T, то нужно их обслужить
            Client nextClient = clientsQueue.element();
            double serviceTime = serviceTimeGeneration();

            td = t + serviceTime; // td = ta + (W = td - ta) + serviceTime

            nextClient.setWaitingTime(t - nextClient.getArrivalTime()); // Время ожидания клиента в очереди
            nextClient.setDepartureTime(td);
            nextClient.setServiceDay(serviceDay);
        }
    }

    /*
    Завершение дневной симуляции
     */
    private void endWorkHandler(){
        double value = Math.max(td - T, 0);
        double scale = Math.pow(10, 3);

        Tp += Math.round(value * scale) / scale;
    }

    /*
    Генерация времени обслуживания клиента устройством
     */
    private double serviceTimeGeneration(){
        Random random = new Random();
        double u = random.nextDouble();

        return -meanServiceTime*Math.log(1-u);
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

        for(var qCurrent : Q){ // пары: время, кол-во клиентов в системе
            // Не считываем тех, кто стоял в очереди после конца дневной работы СМО
            if(qCurrent.getKey() > T){
                continue;
            }

            totalQ += (qCurrent.getKey() - lastTime) * (qCurrent.getValue() - 1); // Вычитаем одного на обслуживании
            lastTime = qCurrent.getKey();
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

    /*
    Получения массива изменения кол-ва клиентов в системе со временем
     */
    public List<Map.Entry<Double, Integer>> getQ(){
        return Q;
    }

    public void printStatistic(){
        System.out.println("Результаты моделирования:");
        System.out.println("Общее число прибытий Na: " + Na);
        System.out.println("Общее число уходов Nd: " + Nd);
        System.out.printf("Время переработки Tp: %.2f\n", Tp);
        System.out.printf("Среднее время пребывания клиента в системе St: %.2f\n", averageTimeInSystem());
        System.out.printf("Среднее время ожидания в очереди Wср: %.2f\n", averageWaitingTime());
        System.out.printf("Среднее число клиентов в очереди Qср: %.2f\n", averageClientsInQueue());
        System.out.printf("Занятость устройства p: %.3f\n", deviceOccupancy());

        // Вывод таблицы данных о клиентах: день, время прихода, время ожидания, время ухода из системы
        System.out.println("Данные о клиентах:");

        String[] headers = {"Клиент", "День", "Приход", "Ожидание", "Уход"};
        for (String header : headers) {
            System.out.printf("%-9s", header);
        }

        for(int i = 0; i < clients.size() && clients.get(i).getServiceDay() == 1; ++i){
            System.out.println();
            System.out.printf("%-9d", i + 1);
            System.out.printf("%-9d", clients.get(i).getServiceDay());
            System.out.printf("%-9.2f", clients.get(i).getArrivalTime());
            System.out.printf("%-9.2f", clients.get(i).getWaitingTime());
            System.out.printf("%-9.2f", clients.get(i).getDepartureTime());
        }
    }
}
