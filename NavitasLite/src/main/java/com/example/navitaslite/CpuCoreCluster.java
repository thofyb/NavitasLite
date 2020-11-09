package com.example.navitaslite;

import java.util.ArrayList;
import java.util.List;

public class CpuCoreCluster {
    public int numCores;
    public List<Long> speeds;
    public List<Float> powers;

    public CpuCoreCluster(int numCores) {
        this.numCores = numCores;
        this.speeds = new ArrayList<>();
        this.powers = new ArrayList<>();
    }

}
