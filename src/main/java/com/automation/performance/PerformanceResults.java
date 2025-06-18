package com.automation.performance;

import java.util.List;
import java.util.ArrayList;

/**
 * Performance test results container.
 * Holds metrics and statistics from JMeter test execution.
 */
public class PerformanceResults {
    
    private String testPlanName;
    private long totalExecutionTime;
    private int threadCount;
    private int rampUpTime;
    private int loopCount;
    private List<String> errors;
    private double averageResponseTime;
    private double minResponseTime;
    private double maxResponseTime;
    private long totalSamples;
    private long errorCount;
    private double errorPercentage;
    private double throughputPerSecond;
    
    public PerformanceResults() {
        this.errors = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getTestPlanName() { return testPlanName; }
    public void setTestPlanName(String testPlanName) { this.testPlanName = testPlanName; }
    
    public long getTotalExecutionTime() { return totalExecutionTime; }
    public void setTotalExecutionTime(long totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }
    
    public int getThreadCount() { return threadCount; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    
    public int getRampUpTime() { return rampUpTime; }
    public void setRampUpTime(int rampUpTime) { this.rampUpTime = rampUpTime; }
    
    public int getLoopCount() { return loopCount; }
    public void setLoopCount(int loopCount) { this.loopCount = loopCount; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public void addError(String error) { this.errors.add(error); }
    
    public double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
    
    public double getMinResponseTime() { return minResponseTime; }
    public void setMinResponseTime(double minResponseTime) { this.minResponseTime = minResponseTime; }
    
    public double getMaxResponseTime() { return maxResponseTime; }
    public void setMaxResponseTime(double maxResponseTime) { this.maxResponseTime = maxResponseTime; }
    
    public long getTotalSamples() { return totalSamples; }
    public void setTotalSamples(long totalSamples) { this.totalSamples = totalSamples; }
    
    public long getErrorCount() { return errorCount; }
    public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
    
    public double getErrorPercentage() { return errorPercentage; }
    public void setErrorPercentage(double errorPercentage) { this.errorPercentage = errorPercentage; }
    
    public double getThroughputPerSecond() { return throughputPerSecond; }
    public void setThroughputPerSecond(double throughputPerSecond) { this.throughputPerSecond = throughputPerSecond; }
    
    /**
     * Check if performance test passed based on error rate threshold.
     */
    public boolean isTestPassed(double maxErrorPercentage) {
        return errorPercentage <= maxErrorPercentage;
    }
    
    /**
     * Check if response time is within acceptable limits.
     */
    public boolean isResponseTimeAcceptable(double maxAverageResponseTime) {
        return averageResponseTime <= maxAverageResponseTime;
    }
    
    /**
     * Check if throughput meets minimum requirements.
     */
    public boolean isThroughputAcceptable(double minThroughput) {
        return throughputPerSecond >= minThroughput;
    }
    
    /**
     * Get performance summary as formatted string.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Performance Test Results Summary:\n");
        summary.append("Test Plan: ").append(testPlanName).append("\n");
        summary.append("Execution Time: ").append(totalExecutionTime).append(" ms\n");
        summary.append("Threads: ").append(threadCount).append("\n");
        summary.append("Ramp-up: ").append(rampUpTime).append(" seconds\n");
        summary.append("Loops: ").append(loopCount).append("\n");
        summary.append("Total Samples: ").append(totalSamples).append("\n");
        summary.append("Error Count: ").append(errorCount).append("\n");
        summary.append("Error Rate: ").append(String.format("%.2f", errorPercentage)).append("%\n");
        summary.append("Average Response Time: ").append(String.format("%.2f", averageResponseTime)).append(" ms\n");
        summary.append("Min Response Time: ").append(String.format("%.2f", minResponseTime)).append(" ms\n");
        summary.append("Max Response Time: ").append(String.format("%.2f", maxResponseTime)).append(" ms\n");
        summary.append("Throughput: ").append(String.format("%.2f", throughputPerSecond)).append(" requests/sec\n");
        
        if (!errors.isEmpty()) {
            summary.append("Errors:\n");
            for (String error : errors) {
                summary.append("  - ").append(error).append("\n");
            }
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
} 