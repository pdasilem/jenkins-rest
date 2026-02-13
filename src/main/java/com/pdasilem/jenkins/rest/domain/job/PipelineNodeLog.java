package com.pdasilem.jenkins.rest.domain.job;

public record PipelineNodeLog(String nodeId, String nodeStatus,
                              int length, boolean hasMore,
                              String text, String consoleUrl) {

    public static PipelineNodeLog create(String nodeId, String nodeStatus, int length, boolean hasMore, String text, String consoleUrl) {
        return new PipelineNodeLog(nodeId, nodeStatus, length, hasMore, text, consoleUrl);
    }
}
