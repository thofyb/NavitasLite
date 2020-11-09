package com.example.navitaslite;

import java.util.ArrayList;
import java.util.List;

public class CpuCoreCluster {
    protected int numCores;
    protected List<Long> speeds;
    protected List<Float> powers;

    public CpuCoreCluster(int numCores) {
        this.numCores = numCores;
        this.speeds = new ArrayList<>();
        this.powers = new ArrayList<>();
    }

}
