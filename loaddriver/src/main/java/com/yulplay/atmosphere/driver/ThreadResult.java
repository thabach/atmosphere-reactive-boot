package com.yulplay.atmosphere.driver;

public class ThreadResult {

    private final int targetRequests;
    private final int successfulRequests;
    private final long totalTime;

    public ThreadResult(int targetRequests, int successfulRequests, long totalTime) {
        this.targetRequests = targetRequests;
        this.successfulRequests = successfulRequests;
        this.totalTime = totalTime;
    }

    public int getTargetRequests() {
        return targetRequests;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
