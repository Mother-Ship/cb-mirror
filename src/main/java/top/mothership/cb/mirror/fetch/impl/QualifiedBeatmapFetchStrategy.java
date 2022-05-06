package top.mothership.cb.mirror.fetch.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mothership.cb.mirror.common.enums.RankStatus;
import top.mothership.cb.mirror.common.util.DateUtil;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;
import top.mothership.cb.mirror.fetch.BeatmapFetchStrategy;
import top.mothership.cb.mirror.fetch.BeatmapService;
import top.mothership.cb.mirror.osu.OsuApiClient;
import top.mothership.cb.mirror.sayo.SayoClient;

import java.util.Objects;

import static top.mothership.cb.mirror.common.enums.RankStatus.QUALIFIED;

@Component
@Slf4j
public class QualifiedBeatmapFetchStrategy implements BeatmapFetchStrategy {
    @Autowired
    SayoClient sayoClient;
    @Autowired
    OsuApiClient osuApiClient;
    @Autowired
    BeatmapService beatmapService;

    @Override
    public void doHandleBeatmap(BeatmapSetDO beatmapSetDO) {
        // qua图

        // 未同步到sayo，则下载并同步；
        if (!beatmapSetDO.getSynced()){
            sayoClient.notifyUpdate(beatmapSetDO.getSid());
            return;
        }

        // 上次本地更新是1周以上，则请求接口更新谱面状态；
        if (DateUtil.isOneWeekEarlier(beatmapSetDO.getLastFetch())){
            beatmapService.saveAndNotifyIfRankedOrLoved(beatmapSetDO.getSid());
        }

    }

    @Override
    public boolean canHandleStatus(RankStatus status) {
        return Objects.equals(status,QUALIFIED);
    }
}
