package top.mothership.cb.mirror.sayo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mothership.cb.mirror.dal.BeatmapRepository;
import top.mothership.cb.mirror.dal.BeatmapSetRepository;
import top.mothership.cb.mirror.dal.model.BeatmapDO;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootTest
public class SayoImporterTest {
    @Autowired
    private BeatmapSetRepository beatmapSetRepository;

    @Autowired
    private BeatmapRepository beatmapRepository;

    @SneakyThrows
    @Test
    public void importSetStatus() {

        Path path = Paths.get("status.txt");
        Scanner scanner = new Scanner(path);
        List<BeatmapSetDO> list = new ArrayList<>();
        while (scanner.hasNextLine()) {
            //process each line
            String line = scanner.nextLine();
            String[] data = line.split(" ");
            int sid = Integer.parseInt(data[0]);
            long lastFetch = Long.parseLong(data[1]);
            long lastUpdateByMapper = Long.parseLong(data[2]);
            int status = Integer.parseInt(data[3]);
            BeatmapSetDO set = new BeatmapSetDO();
            set.setSid(sid);
            set.setLastFetch(lastFetch);
            set.setLastUpdateByMapper(lastUpdateByMapper);
            set.setStatus(status);
            list.add(set);
        }
        beatmapSetRepository.saveAll(list);
        scanner.close();
    }


    @SneakyThrows
    @Test
    public void importBeatmapHash() {

        Path path = Paths.get("hash.txt");
        Scanner scanner = new Scanner(path);
        List<BeatmapDO> list = new ArrayList<>();
        while (scanner.hasNextLine()) {
            //process each line
            String line = scanner.nextLine();
            String[] data = line.split(" ");
            int bid = Integer.parseInt(data[0]);
            int sid = Integer.parseInt(data[1]);
            String hash = data[2];
            BeatmapDO beatmap = new BeatmapDO();
            beatmap.setBid(bid);
            beatmap.setSid(sid);
            beatmap.setHash(hash);
            if (beatmap.getSid() > beatmap.getBid()){
                continue;
            }
            list.add(beatmap);
        }
        beatmapRepository.saveAll( list);
        scanner.close();
    }
}
