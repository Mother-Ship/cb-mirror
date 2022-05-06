package top.mothership.cb.mirror.fetch.impl;

import org.springframework.stereotype.Component;
import top.mothership.cb.mirror.common.enums.RankStatus;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;
import top.mothership.cb.mirror.fetch.BeatmapFetchStrategy;
@Component
public class UnRankBeatmapFetchStrategy implements BeatmapFetchStrategy {
    @Override
    public void doHandleBeatmap(BeatmapSetDO beatmapSetDO) {
        //unranked图，来自Timer的调用则直接跳过，只有接受通知才更新+下图
    }

    @Override
    public boolean canHandleStatus(RankStatus status) {
        return status.isUnranked();
    }
}
