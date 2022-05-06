package top.mothership.cb.mirror.fetch.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mothership.cb.mirror.common.enums.RankStatus;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;
import top.mothership.cb.mirror.fetch.BeatmapFetchStrategy;
import top.mothership.cb.mirror.osu.OsuApiClient;
import top.mothership.cb.mirror.sayo.SayoClient;

import java.util.Objects;

import static top.mothership.cb.mirror.common.enums.RankStatus.RANKED;

@Component
@Slf4j
public class RankedBeatmapFetchStrategy implements BeatmapFetchStrategy {
    @Autowired
    OsuApiClient osuApiClient;
    @Autowired
    SayoClient sayoClient;

    @Override
    public void doHandleBeatmap(BeatmapSetDO beatmapSetDO) {
        //ranked图，已同步到Sayo则跳过，未同步则下载并推送到Sayo
        if (beatmapSetDO.getSynced()) {
            return;
        }

        sayoClient.notifyUpdate(beatmapSetDO.getSid());
    }

    @Override
    public boolean canHandleStatus(RankStatus status) {
        return Objects.equals(status,RANKED);
    }
}
