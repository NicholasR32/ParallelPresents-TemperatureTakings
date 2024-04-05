// Nicholas Rolland
// Assignment 3
// COP 4520, Spring 2024, Prof. Juan Parra

// Problem 2: Atmospheric Temperature Reading Module
// On the Mars Rover are 8 temperature sensors, each with its own CPU
// Each sensor takes a reading every 1 minute (between -100F and 70F)
// Readings are stored in a shared memory space
// At the end of every hour, compile a report containing:
//      - 5 highest temps
//      - 5 lowest temps
//      - 10-minute interval of largest temp difference



import java.util.*;

public class Problem2
{
    public static final int NUM_SENSORS = 8;
    public static final int NUM_HOURS = 5;
    public static final int NUM_MINUTES = 60;
    public static final int INTERVAL = 10;

    public static final boolean DEBUG = false;
    public static void main(String[] args)
    {
        // Start temp readings
        for (int h = 0; h < NUM_HOURS; h++)
        {
            double[][] hourlyReadings = new double[NUM_MINUTES][NUM_SENSORS];
            for (int m = 0; m < NUM_MINUTES; m++)
            {
                // Create threads
                Sensor[] sensors = new Sensor[8];
                for (int i = 0; i < NUM_SENSORS; i++)
                {
                    sensors[i] = new Sensor(i);
                }
                // Get readings from all sensors for this minute.
                for (Sensor sensor : sensors)
                {
                    sensor.start();
                }
                for (Sensor sensor : sensors)
                {
                    try
                    {
                        sensor.join();
                    }
                    catch (InterruptedException e)
                    {}
                }

                // Copy results to main thread.
                for (int i = 0; i < NUM_SENSORS; i++)
                {
                    hourlyReadings[m][i] = Sensor.sensorReadings[i];
                }
            }

            // Compile report at end of each hour.
            double maxTempDiff = 0;
            int maxStart = 0, maxEnd = 0;
            int start = 0, end = 9;
            while (end < NUM_MINUTES)
            {
                // Note highest and lowest temp recorded by any sensor in this interval.
                double minTemp = 999;
                double maxTemp = -999;
                for (int i = start; i <= end; i++)
                {
                    // Go over all sensors.
                    for (double reading : hourlyReadings[i])
                    {
                        minTemp = Math.min(minTemp, reading);
                        maxTemp = Math.max(maxTemp, reading);
                    }
                    double difference = maxTemp - minTemp;
                    if (difference > maxTempDiff)
                    {
                        maxTempDiff = difference;
                        maxStart = start;
                        maxEnd = end;
                    }
                }
                start++;
                end++;
            }

            // Copy all readings to 1D array then sort it to get top and bottom 5.
            double[] allReadings = new double[NUM_MINUTES * NUM_SENSORS];
            for (int i = 0; i < NUM_MINUTES; i++)
            {
                for (int j = 0; j < NUM_SENSORS; j++)
                {
                    allReadings[8*i + j] = hourlyReadings[i][j];
                }
            }
            Arrays.sort(allReadings);

            // Output hourly report.
            System.out.printf("----Hour %d----\n", h);
            System.out.printf("Largest temperature difference: %.2f°F, from minute %d to %d\n", maxTempDiff, maxStart, maxEnd);
            System.out.print("5 lowest temperatures recorded: ");
            for (int i = 0; i < 5; i++)
                System.out.printf("%.2f°F ", allReadings[i]);
            System.out.println();

            System.out.print("5 highest temperatures recorded: ");
            for (int i = allReadings.length-1; i >= allReadings.length-5; i--)
                System.out.printf("%.2f°F ", allReadings[i]);
            System.out.println();

        }
    }
}

class Sensor extends Thread
{
    public int id;
    // Shared space for storing readings from all sensors.
    public static double[] sensorReadings = new double[8];
    public static int MIN_TEMP = -100;
    public static int MAX_TEMP = 70;

    public Sensor(int id)
    {
        this.id = id;
    }

    public void run()
    {
        double reading = (Math.random() * (MAX_TEMP - MIN_TEMP)) + MIN_TEMP;
        storeReading(reading);
    }

    // Ensure mutual exclusion using synchronized method.
    private synchronized void storeReading(double reading)
    {
        if (Problem2.DEBUG) System.out.println(id + " read " + reading);
        sensorReadings[id] = reading;
    }
}