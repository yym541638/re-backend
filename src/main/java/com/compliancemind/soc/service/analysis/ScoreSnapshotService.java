package com.compliancemind.soc.service.analysis;

import com.compliancemind.soc.entity.analysis.ScoreSnapshot;
import com.compliancemind.soc.mapper.analysis.ScoreSnapshotMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.mapper.controltesting.ControlTestMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 控制测试通过率等指标快照的计算与持久化。
 */
@Service
public class ScoreSnapshotService {

    private final ScoreSnapshotMapper scoreSnapshotMapper;
    private final ControlTestMapper controlTestMapper;
    private final GapAnalysisService gapAnalysisService;

    public ScoreSnapshotService(ScoreSnapshotMapper scoreSnapshotMapper,
                                ControlTestMapper controlTestMapper,
                                GapAnalysisService gapAnalysisService) {
        this.scoreSnapshotMapper = scoreSnapshotMapper;
        this.controlTestMapper = controlTestMapper;
        this.gapAnalysisService = gapAnalysisService;
    }

    public ScoreSnapshot recordSnapshot(Long projectId) {
        List<Map<String, Object>> statusRows = controlTestMapper.countByResultStatus(projectId);
        long pass = getCount(statusRows, SocConstants.ControlTest.RESULT_PASS);
        long fail = getCount(statusRows, SocConstants.ControlTest.RESULT_FAIL);
        long pending = getCount(statusRows, SocConstants.ControlTest.RESULT_PENDING);
        long total = pass + fail + pending;
        long gapCount = gapAnalysisService.countByProjectId(projectId);

        ScoreSnapshot snapshot = new ScoreSnapshot();
        snapshot.setProjectId(projectId);
        snapshot.setSnapshotDate(LocalDate.now());
        snapshot.setTotalCount(total);
        snapshot.setPassedCount(pass);
        snapshot.setFailedCount(fail);
        snapshot.setPendingCount(pending);
        snapshot.setGapCount(gapCount);
        snapshot.setPassRate(total == 0 ? BigDecimal.ZERO : new BigDecimal(pass).multiply(new BigDecimal(100)).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP));
        snapshot.setAssessment(resolveAssessment(snapshot.getPassRate()));
        scoreSnapshotMapper.insert(snapshot);
        return snapshot;
    }

    public ScoreSnapshot latest(Long projectId) {
        return scoreSnapshotMapper.selectLatest(projectId);
    }

    public List<ScoreSnapshot> trend(Long projectId) {
        return scoreSnapshotMapper.listByProjectId(projectId);
    }

    private String resolveAssessment(BigDecimal passRate) {
        if (passRate.compareTo(SocConstants.Scoring.PASS_RATE_GREEN_MIN) >= 0) {
            return SocConstants.Scoring.ASSESSMENT_CERTAIN_PASS_ZH;
        }
        if (passRate.compareTo(SocConstants.Scoring.PASS_RATE_YELLOW_MIN) >= 0) {
            return SocConstants.Scoring.ASSESSMENT_LIKELY_PASS_ZH;
        }
        return SocConstants.Scoring.ASSESSMENT_HIGH_RISK_ZH;
    }

    private long getCount(List<Map<String, Object>> rows, String key) {
        for (Map<String, Object> row : rows) {
            if (key.equalsIgnoreCase(String.valueOf(row.get(SocConstants.SqlAgg.KEY_NAME)))) {
                Number number = (Number) row.get(SocConstants.SqlAgg.KEY_TOTAL);
                return number == null ? 0L : number.longValue();
            }
        }
        return 0L;
    }
}

