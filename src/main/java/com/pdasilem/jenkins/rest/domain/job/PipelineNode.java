package com.pdasilem.jenkins.rest.domain.job;

import java.util.List;

public record PipelineNode(String name, String status,
                           long startTimeMillis, long durationTimeMillis,
                           List<StageFlowNode> stageFlowNodes) {

    public PipelineNode {
        stageFlowNodes = stageFlowNodes != null ? List.copyOf(stageFlowNodes) : List.of();
    }

    public static PipelineNode create(String name, String status, long startTimeMillis, long durationTimeMillis, List<StageFlowNode> stageFlowNodes) {
        return new PipelineNode(name, status, startTimeMillis, durationTimeMillis, stageFlowNodes);
    }
}
