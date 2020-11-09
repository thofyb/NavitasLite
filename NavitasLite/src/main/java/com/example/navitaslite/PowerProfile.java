package com.example.navitaslite;

import java.util.List;

public class PowerProfile {
    private List<CpuCoreCluster> clusters;

    public PowerProfile(List<CpuCoreCluster> clusters) {
        this.clusters = clusters;
    }

    public float getPowerAtSpeed(int coreIndex, long speed) {
        CpuCoreCluster cluster = getClusterWithCore(coreIndex);
        assert cluster != null;
        List<Long> speeds = cluster.speeds;
        List<Float> powers = cluster.powers;

        if (speeds.contains(speed)) {
            return powers.get(speeds.indexOf(speed));
        } else {
            if (speed < speeds.get(0))
                return powers.get(0);
            if (speed > speeds.get(speeds.size() - 1))
                return powers.get(powers.size() - 1);

            int i = 0;
            long currSpeed = speeds.get(i);
            while (currSpeed < speed)
                currSpeed = speeds.get(++i);

            long upperSpeed = speeds.get(i);
            long lowerSpeed = speeds.get(i - 1);
            float upperPower = powers.get(i);
            float lowerPower = powers.get(i - 1);

            return ((upperPower - lowerPower) * (speed - lowerSpeed)) / (upperSpeed - lowerSpeed);
        }
    }

    private CpuCoreCluster getClusterWithCore(int coreIndex) {
        int index = coreIndex;
        for (CpuCoreCluster cluster : clusters) {
            if (cluster.numCores - 1 >= index) return cluster;
            else index -= cluster.numCores;
        }
        return null;
    }
}
