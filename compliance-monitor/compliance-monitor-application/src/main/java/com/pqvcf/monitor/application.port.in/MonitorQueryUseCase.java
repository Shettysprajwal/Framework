package com.pqvcf.monitor.application.port.in;

import com.pqvcf.monitor.application.port.in.IngestEventUseCase.EventDto;
import com.pqvcf.monitor.application.port.in.IngestEventUseCase.SlaMetricsDto;
import com.pqvcf.monitor.application.port.in.IngestEventUseCase.ViolationDto;

import java.util.List;

public interface MonitorQueryUseCase {
    List<EventDto> listRecentEvents();
    List<ViolationDto> listViolations();
    SlaMetricsDto getCurrentMetrics();
    void reset();
}
