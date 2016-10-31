package com.yulplay.atmosphere.driver;

import java.util.List;

public class BenchmarkResult {
    private final int threads;
    private final int batches;
    private final long targetRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final float averageRequestTime;
    private final float averageBatchTime;
    private final long totalBenchmarkTime;
    private final float requestsPerSecond;

    public BenchmarkResult(int threads, int batches, List<BatchResult> results) {
        this.threads = threads;
        this.batches = batches;

        long totalTargetRequests = 0;
        long totalSuccessfulRequests = 0;
        long totalFailedRequests = 0;
        long totalBenchmarkTime = 0;
        float totalRequestAverage = 0;

        for (BatchResult result : results) {
            totalTargetRequests += result.getBatchTargetRequests();
            totalSuccessfulRequests += result.getBatchSuccessfulRequests();
            totalFailedRequests += result.getBatchTargetRequests() - result.getBatchSuccessfulRequests();
            totalBenchmarkTime += result.getTotalBatchTime();
            totalRequestAverage += result.getAverageTimePerRequest();
        }

        this.targetRequests = totalTargetRequests;
        this.successfulRequests = totalSuccessfulRequests;
        this.failedRequests = totalFailedRequests;

        this.averageRequestTime = totalRequestAverage / (float) results.size();
        this.averageBatchTime = totalBenchmarkTime / (float) results.size();
        this.totalBenchmarkTime = totalBenchmarkTime;
        this.requestsPerSecond = 1000000000f / this.averageRequestTime;
    }

    public static String decimal(float f) {
        return decimal(f, 2);
    }

    public static String decimal(float f, int decimalUnits) {
        if (decimalUnits <= 0) {
            return Float.toString(f);
        }

        String s = Float.toString(f);
        int pos = s.indexOf('.');
        return s.substring(0, Math.min(pos + 1 + decimalUnits, s.length())); // xxxx.yy
    }

    @Override
    public String toString() {
        return "\nTotal" +
               "\n\trequestsPerSecond : " + decimal(requestsPerSecond) +
               "\n\twebsockets : " + threads +
               "\n\tbatches : " + batches +
               "\n\ttargetRequests : " + targetRequests +
               "\n\tsuccessfulRequests : " + successfulRequests +
               "\n\tfailedRequests : " + failedRequests +
               "\n\taverageRequestTime : " + decimal(averageRequestTime / 1000000f) +
               "ms\n\taverageBatchTime : " + decimal(averageBatchTime / 1000000f) +
               "ms\n\ttotalBenchmarkTime : " + decimal(totalBenchmarkTime / 1000000f) +
               "ms";
    }
}
