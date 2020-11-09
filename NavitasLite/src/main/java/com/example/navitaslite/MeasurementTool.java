package com.example.navitaslite;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MeasurementTool {

    public static Reading makeMeasurement(int mask) {
        Reading res = new Reading(mask);
        int cores = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < cores; i++) {
            boolean isMeasurable = (mask & (1 << i)) != 0 ;
            SingleCoreReading reading = makeSingleCoreMeasurement(i, isMeasurable);
            if (reading != null) {
                res.addSingleCoreReading(reading);
            }
            else if (!res.duplicateLastReading(isMeasurable)) return null;
        }

        return res;
    }

    private static SingleCoreReading makeSingleCoreMeasurement(int core, boolean isMeasurable) {
        List<CoreTimeAtFrequency> res = new ArrayList<>();
        String filePath = "/sys/devices/system/cpu/cpu" + core + "/cpufreq/stats/time_in_state";
        try {
            Scanner sc = new Scanner(new File(filePath));
            while (sc.hasNextLine()) {
                String[] strs = sc.nextLine().split(" ");

                CoreTimeAtFrequency state = new CoreTimeAtFrequency(
                        Long.parseLong(strs[0]),
                        Long.parseLong(strs[1])
                );

                res.add(state);
            }
        } catch (FileNotFoundException e) {
            return null;
        }
        return new SingleCoreReading(res, core, isMeasurable);
    }

    public static Reading findDiff(Reading r1, Reading r2) {
        if (r1.size() == r2.size() && r1.mask == r2.mask) {
            Reading res = new Reading(r1.mask);
            for (int i = 0; i < r1.size(); i++) {
                SingleCoreReading tmp = findSingleCoreDiff(r1.get(i), r2.get(i));
                if (tmp != null) res.addSingleCoreReading(tmp);
                else return null;
            }
            return res;
        }
        return null;
    }

    private static SingleCoreReading findSingleCoreDiff(SingleCoreReading r1, SingleCoreReading r2) {
        if (r1.sameCoreWith(r2)) {
            List<CoreTimeAtFrequency> res = new ArrayList<>();
            for (int i = 0; i < r1.data.size(); i++) {

                CoreTimeAtFrequency tmp = new CoreTimeAtFrequency(
                        r1.data.get(i).frequency,
                        r2.data.get(i).timestamp - r1.data.get(i).timestamp
                );

                res.add(tmp);
            }

            return new SingleCoreReading(res, r1.core, r1.isMeasurable);
        }
        return null;
    }


    public static float analyzeMeasurement(Reading readings, PowerProfile profile) {
        float cpuEnergy = 0;
        for (int i = 0; i < readings.size(); i++) {
            if (readings.readings.get(i).isMeasurable) {
                for (CoreTimeAtFrequency log : readings.readings.get(i).data) {
                    long freq = log.frequency;
                    long time = log.timestamp;
                    cpuEnergy += time * profile.getPowerAtSpeed(i, freq);
                }
            }
        }

        return cpuEnergy;
    }


    public static class Reading {
        List<SingleCoreReading> readings;
        int mask;

        private Reading(int mask) {
            this.readings = new ArrayList<>();
            this.mask = mask;
        }

        private void addSingleCoreReading(SingleCoreReading reading) {
            readings.add(reading);
        }

        private boolean duplicateLastReading(boolean isMeasurable) {
            if(!readings.isEmpty()) {
                SingleCoreReading tmp = readings.get(readings.size() - 1).clone();
                tmp.isMeasurable = isMeasurable;
                readings.add(tmp);
                return true;
            }
            return false;
        }

        private int size() {
            return readings.size();
        }

        private SingleCoreReading get(int index) {
            return readings.get(index);
        }

        public String toString() {
            StringBuilder res = new StringBuilder();
            for (SingleCoreReading reading : readings) {
                res.append(reading.toString() + "\n");
            }
            return res.toString();
        }
    }

    private static class SingleCoreReading {
        List<CoreTimeAtFrequency> data;
        int core;
        boolean isMeasurable;

        SingleCoreReading(List<CoreTimeAtFrequency> data, int core, boolean isMeasurable) {
            this.data = data;
            this.core = core;
            this.isMeasurable = isMeasurable;
        }



        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append(core).append("\n");
            for (CoreTimeAtFrequency state : data) {
                res.append(state.toString());
            }

            return res.toString();
        }

        protected SingleCoreReading clone() {
            return new SingleCoreReading(
                    this.data,
                    this.core,
                    this.isMeasurable
            );
        }

        private boolean sameCoreWith(SingleCoreReading a) {
            return this.core == a.core && this.isMeasurable == a.isMeasurable;
        }
    }

    private static class CoreTimeAtFrequency {
        long frequency;
        long timestamp;

        CoreTimeAtFrequency(long frequency, long timestamp) {
            this.frequency = frequency;
            this.timestamp = timestamp;
        }

        public String toString() {
            return frequency + " " + timestamp + "\n";
        }
    }
}
