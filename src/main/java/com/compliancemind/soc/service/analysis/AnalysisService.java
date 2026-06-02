package com.compliancemind.soc.service.analysis;

import com.compliancemind.soc.dto.analysis.PassRateResponse;
import com.compliancemind.soc.dto.analysis.TrendPoint;
import com.compliancemind.soc.entity.analysis.ScoreSnapshot;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.service.project.ProjectService;
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
    private final ProjectService projectService;

    public AnalysisService(ScoreSnapshotService scoreSnapshotService,
                           AuthorizationService authorizationService,
                           ProjectService projectService) {
        this.scoreSnapshotService = scoreSnapshotService;
        this.authorizationService = authorizationService;
        this.projectService = projectService;
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
        // 控制测试全部打分完成后，访问 Passing Scores 时将项目置为 End 并写入结束日期
        if (snapshot.getTotalCount() > 0 && snapshot.getPendingCount() == 0) {
            projectService.markProjectEnded(projectId);
        }
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
