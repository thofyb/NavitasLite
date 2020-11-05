package com.example.navitaslite;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MeasurementTool {

    public static Reading makeMeasurement() {
        Reading res = new Reading();
        int cores = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < cores; i++) {
            SingleCoreReading reading = makeSingleCoreMeasurement(i);
            if (reading != null) {
                res.addSingleCoreReading(reading);
            }
            else if (!res.duplicateLastReading()) return null;
        }

        return res;
    }

    private static SingleCoreReading makeSingleCoreMeasurement(int core) {
        List<List<Integer>> res = new ArrayList<>();
        String filePath = "/sys/devices/system/cpu/cpu" + core + "/cpufreq/stats/time_in_state";
        try {
            Scanner sc = new Scanner(new File(filePath));
            while (sc.hasNextLine()) {
                List<Integer> state = new ArrayList<>();
                String[] strs = sc.nextLine().split(" ");
                for (String str : strs) {
                    state.add(Integer.valueOf(str));
                }
                res.add(state);

            }
        } catch (FileNotFoundException e) {
            return null;
        }
        return new SingleCoreReading(res, core);
    }

    public static Reading findDiff(Reading r1, Reading r2) {
        if (r1.size() == r2.size()) {
            Reading res = new Reading();
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
            List<List<Integer>> res = new ArrayList<>();
            for (int i = 0; i < r1.data.size(); i++) {
                List<Integer> tmp = new ArrayList<>();
                tmp.add(r1.data.get(i).get(0));
                tmp.add(r2.data.get(i).get(1) - r1.data.get(i).get(1));
                res.add(tmp);
            }

            return new SingleCoreReading(res, r1.core);
        }
        return null;
    }

    public static class Reading {
        List<SingleCoreReading> readings;

        private Reading() {
            this.readings = new ArrayList<>();
        }

        private void addSingleCoreReading(SingleCoreReading reading) {
            readings.add(reading);
        }

        private boolean duplicateLastReading() {
            if(!readings.isEmpty()) {
                readings.add(readings.get(readings.size() - 1));
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
                res.append(reading.toString());
            }
            return res.toString();
        }
    }

    private static class SingleCoreReading {
        List<List<Integer>> data;
        int core;

        SingleCoreReading(List<List<Integer>> data, int core) {
            this.data = data;
            this.core = core;
        }



        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append(core).append("\n");
            for (List<Integer> state : data) {
                for (int value : state) {
                    res.append(value).append(" ");
                }
                res.append("\n");
            }
            return res.toString();
        }

        private boolean sameCoreWith(SingleCoreReading a) {
            return this.core == a.core;
        }
    }
}
