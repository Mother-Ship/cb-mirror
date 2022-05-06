package top.mothership.cb.mirror.fetch;

import top.mothership.cb.mirror.common.enums.RankStatus;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;

public interface BeatmapFetchStrategy {
    void doHandleBeatmap(BeatmapSetDO beatmapSetDO);

    boolean canHandleStatus(RankStatus status);
}
