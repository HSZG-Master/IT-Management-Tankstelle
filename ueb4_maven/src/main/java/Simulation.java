import java.util.LinkedList;
import java.util.PriorityQueue;

public class Simulation {

    private static final long SIMULATION_TIME_IN_SECONDS = 3600 * 365;
    private static final int CUSTOMER_ARRIVAL_INTERVAL_IN_SECONDS = 3600 / 40;
    private static final int PERCENTAGE_OF_DRIVERS_GAS_ONLY = 75;
    private static final int LEAVING_CUSTOMER_PERCENTAGE = 33;
    private static final int PERCENTAGE_OF_DRIVERS_GAS_AND_WASHING = 20;
    private static final int PERCENTAGE_OF_DRIVERS_WASHING_ONLY = 5;
    private static final int MEAN_FUELING_TIME_IN_SECONDS = 180;
    private static final int STANDARD_DEVIATION_FUELING_TIME_IN_SECONDS = 36;
    private static final int WASHING_TIME_IN_SECONDS = 240;
    private static final int MINIMUM_TIME_FOR_PAYMENT_IN_SECONDS = 60;
    private static final int MAXIMUM_TIME_FOR_PAYMENT_IN_SECONDS = 180;

    private boolean carWashstationInUse = false;
    private int waitingCustomerForCarWash = 0;
    private int fuelStationsInUse = 0;
    private int lostCustomerCount = 0;
    private int totalCustomerCount = 0;

    private long currentTimeInSeconds = 0;

    private PriorityQueue<Event> eventqueue;
    private LinkedList<EventType> pumpWaitingQueue;

    public static void main(String[] args) {
        new Simulation().start();
    }

    public void start() {

        eventqueue = new PriorityQueue<>();
        eventqueue.add(new Event(0, EventType.ARRIVAL_AT_GASSTATION));

        pumpWaitingQueue = new LinkedList<>();

        while (!eventqueue.isEmpty() && currentTimeInSeconds <= SIMULATION_TIME_IN_SECONDS) {

            Event actuallyEvent = eventqueue.poll();
            currentTimeInSeconds = actuallyEvent.getTime();

            switch (actuallyEvent.getType()) {
                case ARRIVAL_AT_GASSTATION:
                    arrivalAtGasstation();
                    break;
                case END_OF_PAYMENT_FUEL_ONLY:
                    endOfPaymentFuelOnly();
                    break;
                case END_OF_PAYMENT_WASHING_AND_REFUELING:
                    endOfPaymentWashingAndRefueling();
                    break;
                case END_OF_PAYMENT_WASHING_ONLY:
                    endPaymentWashingOnly();
                    break;
                case END_OF_REFUELING_FUEL_ONLY:
                    endOfRefuelingOnly();
                    break;
                case END_OF_REFUELING_WASHING_AND_REFUELING:
                    endOfRefuelingWashingAndRefueling();
                    break;
                case END_OF_WASHING_ONLY:
                    endOfwashingOnly();
                    break;
                default:
                    System.out.println("Unsupported event");
            }
        }

        System.out.println("Anzahl totaler Kunden: " + totalCustomerCount);
        System.out.println("Anzahl verlorener Kunden: " + lostCustomerCount);

    }

    private void addEventToQueue(long timeDelta, EventType eventType) {
        eventqueue.add(new Event(timeDelta + currentTimeInSeconds, eventType));
    }

    private void endOfwashingOnly() {
        if (waitingCustomerForCarWash > 0) {
            addEventToQueue(WASHING_TIME_IN_SECONDS, EventType.END_OF_WASHING_ONLY);
            waitingCustomerForCarWash--;
        } else {
            carWashstationInUse = false;
        }
    }

    private void arrivalAtGasstation() {
        // 75% tanken, 5%waschen, 20% tanken UND Waschen

        int customerTypeValue = Distributions.newUniformDistributedValue(0, 100);

        totalCustomerCount++;

        //nächster Kunde der an die Tankstelle fährt
        addEventToQueue(Distributions.newPoissonDistributedRandomValue(CUSTOMER_ARRIVAL_INTERVAL_IN_SECONDS), EventType.ARRIVAL_AT_GASSTATION);

        if (customerTypeValue <= PERCENTAGE_OF_DRIVERS_WASHING_ONLY) {
            // Waschen
            // Car washs have to be paid in advance and take time exactly 4 minutes

            // Payment 1 - 3 Minuten
            // random.nextInt(max - min + 1) + min
            addEventToQueue(Distributions.newUniformDistributedValue(60, 180), EventType.END_OF_PAYMENT_WASHING_ONLY);
        } else if (fuelStationsInUse < 4) {
            if (customerTypeValue <= (PERCENTAGE_OF_DRIVERS_GAS_AND_WASHING + PERCENTAGE_OF_DRIVERS_WASHING_ONLY)) {
                // Tanken und Waschen
                addEventToQueue(Distributions.newNormalDistributedValue(180, 0.6), EventType.END_OF_REFUELING_WASHING_AND_REFUELING);

            } else {
                // Tanken
                addEventToQueue(Distributions.newNormalDistributedValue(180, 0.6), EventType.END_OF_REFUELING_FUEL_ONLY);

            }

        } else {

            int customerLeavingValue = Distributions.newUniformDistributedValue(0, 100);

            if (customerLeavingValue > 33) {

                if (customerTypeValue <= (PERCENTAGE_OF_DRIVERS_GAS_AND_WASHING + PERCENTAGE_OF_DRIVERS_WASHING_ONLY)) {

                    pumpWaitingQueue.add(EventType.END_OF_REFUELING_WASHING_AND_REFUELING);
                } else {

                    pumpWaitingQueue.add(EventType.END_OF_REFUELING_FUEL_ONLY);
                }
            } else {
                lostCustomerCount++;
            }

        }

    }

    private void endOfRefuelingWashingAndRefueling() {

        addEventToQueue(Distributions.newUniformDistributedValue(60, 180), EventType.END_OF_PAYMENT_WASHING_AND_REFUELING);
    }

    private void endOfRefuelingOnly() {

        int payingTypeValue = Distributions.newUniformDistributedValue(0, 100);

        if (payingTypeValue <= 50) {

            addEventToQueue(Distributions.newUniformDistributedValue(60, 180), EventType.END_OF_PAYMENT_FUEL_ONLY);
        } else {

            endOfPaymentFuelOnly();
        }

    }

    private void endOfPaymentWashingAndRefueling() {

        endOfPaymentFuelOnly();
        endPaymentWashingOnly();

    }

    private void endPaymentWashingOnly() {
        // Car washs have to be paid in advance and take time exactly 4 minutes

        // Waschanlage frei?
        if (carWashstationInUse) {
            waitingCustomerForCarWash++;
        } else {
            carWashstationInUse = true;
            addEventToQueue(WASHING_TIME_IN_SECONDS, EventType.END_OF_WASHING_ONLY);
        }
    }

    private void endOfPaymentFuelOnly() {

        if (!pumpWaitingQueue.isEmpty()) {

            EventType event = pumpWaitingQueue.pop();
            addEventToQueue(Distributions.newNormalDistributedValue(180, 0.6), event);

        } else {

            fuelStationsInUse--;
        }

    }
}
