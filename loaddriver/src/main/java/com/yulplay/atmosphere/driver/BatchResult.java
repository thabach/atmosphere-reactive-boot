package com.yulplay.atmosphere.driver;

import java.util.Collection;

public class BatchResult {

    private final int batchTargetRequests;
    private final int batchSuccessfulRequests;
    private final long totalTimeForAllThreads;
    private final float averageTimePerThread;
    private final float averageTimePerRequest;
    private final long totalBatchTime;

    public BatchResult(Collection<ThreadResult> threadResults, long totalTime) {
        int targetRequests = 0;
        int successfulRequests = 0;
        long totalTimeForAllThreads = 0;

        for (ThreadResult threadResult : threadResults) {
            targetRequests += threadResult.getTargetRequests();
            successfulRequests += threadResult.getSuccessfulRequests();
            totalTimeForAllThreads += threadResult.getTotalTime();
        }

        this.batchTargetRequests = targetRequests;
        this.batchSuccessfulRequests = successfulRequests;
        this.totalTimeForAllThreads = totalTimeForAllThreads;
        this.averageTimePerThread = totalTimeForAllThreads / (float) threadResults.size();
        this.averageTimePerRequest = totalTime / (float) successfulRequests;
        this.totalBatchTime = totalTime;
    }

    public int getBatchTargetRequests() {
        return batchTargetRequests;
    }

    public int getBatchSuccessfulRequests() {
        return batchSuccessfulRequests;
    }

    public int getFailures(){
        return batchTargetRequests - batchSuccessfulRequests;
    }

    public float getAverageTimePerRequest() {
        return averageTimePerRequest;
    }

    public long getTotalBatchTime() {
        return totalBatchTime;
    }

    @Override
    public String toString() {
        return "\n\tRequest Per Second : " + BenchmarkResult.decimal(1000000000f / this.averageTimePerRequest) +
                "\n\tTarget Requests : " + batchTargetRequests +
                "\n\tSuccessful Requests : " + batchSuccessfulRequests +
                "\n\tFailed Requests : " + getFailures() +
                "\n\tTotal Time For All Threads : " + BenchmarkResult.decimal(totalTimeForAllThreads / 1000000f) +
                "ms\n\tAverage Time Per Thread : " + BenchmarkResult.decimal(averageTimePerThread / 1000000f) +
                "ms\n\tAverage Time Per Request : " + BenchmarkResult.decimal(averageTimePerRequest / 1000000f) +
                "ms\n\tTotal BatchTime : " + BenchmarkResult.decimal(totalBatchTime / 1000000f) +
                "ms\n\t";
    }
}
