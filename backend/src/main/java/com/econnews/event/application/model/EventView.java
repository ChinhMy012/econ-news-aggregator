package com.econnews.event.application.model;

import com.econnews.event.domain.model.*;
import java.util.List;

public record EventView(
    EconomicEvent event,
    List<Citation> overviewSources,
    List<MilestoneView> milestones
) {
    public record MilestoneView(Milestone milestone, List<Citation> sources) {}
}
