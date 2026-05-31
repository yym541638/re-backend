package com.compliancemind.soc.service.analysis;

import com.compliancemind.soc.dto.analysis.PassRateResponse;
import com.compliancemind.soc.dto.analysis.TrendPoint;
import com.compliancemind.soc.entity.analysis.ScoreSnapshot;
import com.compliancemind.soc.security.AuthorizationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过率、趋势等聚合分析入口。
 */
@Service
public class AnalysisService {

    private final ScoreSnapshotService scoreSnapshotService;
    private final AuthorizationService authorizationService;

    public AnalysisService(ScoreSnapshotService scoreSnapshotService,
                           AuthorizationService authorizationService) {
        this.scoreSnapshotService = scoreSnapshotService;
        this.authorizationService = authorizationService;
    }

    public PassRateResponse getPassRate(Long projectId) {
        authorizationService.requireProjectRead(projectId);
        ScoreSnapshot snapshot = scoreSnapshotService.latest(projectId);
        if (snapshot == null) {
            snapshot = scoreSnapshotService.recordSnapshot(projectId);
        }
        PassRateResponse response = new PassRateResponse();
        response.setProjectId(projectId);
        response.setTotal(snapshot.getTotalCount());
        response.setPassed(snapshot.getPassedCount());
        response.setFailed(snapshot.getFailedCount());
        response.setPending(snapshot.getPendingCount());
        response.setGapCount(snapshot.getGapCount());
        response.setPassRate(snapshot.getPassRate());
        response.setAssessment(snapshot.getAssessment());
        return response;
    }

    public List<TrendPoint> getTrend(Long projectId) {
        authorizationService.requireProjectRead(projectId);
        List<ScoreSnapshot> snapshots = scoreSnapshotService.trend(projectId);
        List<TrendPoint> result = new ArrayList<>();
        for (ScoreSnapshot snapshot : snapshots) {
            TrendPoint point = new TrendPoint();
            point.setSnapshotDate(snapshot.getSnapshotDate());
            point.setPassRate(snapshot.getPassRate());
            point.setTotal(snapshot.getTotalCount());
            point.setPassed(snapshot.getPassedCount());
            point.setFailed(snapshot.getFailedCount());
            point.setPending(snapshot.getPendingCount());
            point.setGapCount(snapshot.getGapCount());
            result.add(point);
        }
        return result;
    }
}
