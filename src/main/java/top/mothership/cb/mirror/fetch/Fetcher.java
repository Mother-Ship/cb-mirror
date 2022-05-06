package top.mothership.cb.mirror.fetch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mothership.cb.mirror.common.enums.RankStatus;
import top.mothership.cb.mirror.dal.BeatmapSetRepository;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;
import top.mothership.cb.mirror.osu.OsuApiClient;
import top.mothership.cb.mirror.osu.model.BeatmapDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RestController
public class Fetcher {
    @Autowired
    private BeatmapSetRepository beatmapSetRepository;

    @Autowired
    private BeatmapService beatmapService;

    @Autowired
    private List<BeatmapFetchStrategy> strategies;

    @Autowired
    OsuApiClient osuApiClient;

    @GetMapping("/fetch")
    @Async
    public void fetch() {
        doFetch();
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void doFetch() {

        // 向上滚动直到获取到最新提交的谱面，写入数据库
        rollUpTodayNewMap();


        //获取上次有图rank以来的所有rank图，写入数据库
        // 确保之前刷入的unranked图，rank之后也会被获取到本地，并重新同步
        refreshRankedMapSinceLastRanked();


        //过一遍数据库所有图，走处理策略
        iterateDB();

    }

    private void iterateDB() {
        Iterable<BeatmapSetDO> beatmapSetDOS = beatmapSetRepository.findAll();

        for (BeatmapSetDO beatmapSetDO : beatmapSetDOS) {
            RankStatus status = RankStatus.toStatus(beatmapSetDO.getStatus());
            if (status == null) {
                log.error("谱面集Id{}出现未知的Rank状态{}，需要更新代码", beatmapSetDO.getSid(), beatmapSetDO.getStatus());
                continue;
            }

            for (BeatmapFetchStrategy strategy : strategies) {
                if (strategy.canHandleStatus(status)) {
                    strategy.doHandleBeatmap(beatmapSetDO);
                }
            }
        }
    }

    private void refreshRankedMapSinceLastRanked() {
        BeatmapSetDO localRecentRankedMapSet =
                beatmapSetRepository.findDistinctTopByStatusAndSyncedOrderByLastUpdateByMapperDesc(RankStatus.RANKED.code, true)
                        .orElseThrow(
                () -> new RuntimeException("当前谱面数据库为空！")
        );
        log.info("当前数据库内 已同步的最新的Ranked谱面提交日期：{}，sid = {}",
                localRecentRankedMapSet.getLastUpdateByMapper(),
                localRecentRankedMapSet.getSid());

        List<BeatmapDTO> rankedMapSinceLastRanked =
                osuApiClient.getRankedBeatmapSince(localRecentRankedMapSet.getLastUpdateByMapper());

        Map<Integer, List<BeatmapDTO>> groupedBeatmapList =
                rankedMapSinceLastRanked.stream().collect(Collectors.groupingBy(BeatmapDTO::getSetId));

        for (List<BeatmapDTO> set : groupedBeatmapList.values()) {
            beatmapService.saveBeatmap(set);
        }

    }

    private void rollUpTodayNewMap() {
        //获取当前sid最高的谱面
        BeatmapSetDO localMaxMapSet = beatmapSetRepository.findDistinctTopByOrderBySidDesc().orElseThrow(
                () -> new RuntimeException("当前谱面数据库为空！")
        );

        Integer sid = localMaxMapSet.getSid();
        int missCount = 0;
        while (true) {
            List<BeatmapDTO> currentMapSet = osuApiClient.getBeatmapSet(++sid);
            if (CollectionUtils.isEmpty(currentMapSet)) {
                missCount++;
                log.info("获取谱面失败，当前sid:{}", sid);
                if (missCount > 50) {
                    log.info("连续获取谱面失败，退出本次获取，当前sid:{}", sid);
                    break;
                }
                continue;
            }
            missCount = 0;
            log.info("获取到新谱面，sid = {}", sid);
            beatmapService.saveBeatmap(currentMapSet);
        }
    }


}
