package top.mothership.cb.mirror.fetch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.mothership.cb.mirror.common.enums.RankStatus;
import top.mothership.cb.mirror.common.util.DateUtil;
import top.mothership.cb.mirror.dal.BeatmapRepository;
import top.mothership.cb.mirror.dal.BeatmapSetRepository;
import top.mothership.cb.mirror.dal.model.BeatmapDO;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;
import top.mothership.cb.mirror.osu.OsuApiClient;
import top.mothership.cb.mirror.osu.model.BeatmapDTO;
import top.mothership.cb.mirror.sayo.SayoClient;

import javax.transaction.Transactional;
import java.util.List;

@Component
@Slf4j
public class BeatmapService {
    @Autowired
    private BeatmapSetRepository beatmapSetRepository;
    @Autowired
    SayoClient sayoClient;
    @Autowired
    OsuApiClient osuApiClient;
    @Autowired
    private BeatmapRepository beatmapRepository;
    @Transactional
    @Modifying
    public void saveBeatmap(List<BeatmapDTO> currentMapSet){
        BeatmapDTO sampleBeatmap = currentMapSet.iterator().next();

        BeatmapSetDO currentMapSetDO = BeatmapSetDO.builder()
                .sid(sampleBeatmap.getSetId())
                .status(sampleBeatmap.getApproved())
                .lastFetch(System.currentTimeMillis()/1000L)
                .lastUpdateByMapper(DateUtil.getSecondFromOsuDateString(sampleBeatmap.getLastUpdate(), sampleBeatmap.getTimeZone()))
                .synced(false).build();
        beatmapSetRepository.save(currentMapSetDO);

        List<BeatmapDO> beatmapDOList = currentMapSet.stream().map(
                beatmapDTO -> BeatmapDO.builder()
                        .bid(beatmapDTO.getBeatmapId())
                        .sid(beatmapDTO.getSetId())
                        .hash(beatmapDTO.getMd5())
                        .build()
        ).toList();
        beatmapRepository.deleteAllBySid(currentMapSetDO.getSid());
        beatmapRepository.saveAll(beatmapDOList);
    }

    @Transactional
    @Modifying
    public void saveAndNotifyIfRankedOrLoved(Integer sid){
        List<BeatmapDTO> currentMapSet = osuApiClient.getBeatmapSet(sid);
        if (CollectionUtils.isEmpty(currentMapSet)) {
            log.error("请求osu!Api获取已有谱面出错，请检查sid:{}和网络", sid);
            return;
        }

        saveBeatmap(currentMapSet);

        // 如果变为Ranked或Loved则下载并推送到Sayo
        if (RankStatus.toStatus(currentMapSet.iterator().next().getApproved()).isRankedOrLoved()){
            sayoClient.notifyUpdate(sid);
        }
    }
}
