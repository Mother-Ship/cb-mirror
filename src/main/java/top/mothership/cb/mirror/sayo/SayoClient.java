package top.mothership.cb.mirror.sayo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mothership.cb.mirror.dal.BeatmapRepository;
import top.mothership.cb.mirror.dal.BeatmapSetRepository;
import top.mothership.cb.mirror.dal.model.BeatmapDO;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;
import top.mothership.cb.mirror.osu.OsuApiClient;
import top.mothership.cb.mirror.sayo.model.SayoNotifyDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Slf4j
@Component
public class SayoClient {
    @Autowired
    private HttpClient client;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OsuApiClient osuApiClient;

    @Autowired
    private BeatmapSetRepository beatmapSetRepository;

    @Autowired
    private BeatmapRepository beatmapRepository;

    @SneakyThrows
    public void notifyUpdate(Integer sid) {
        log.info("谱面 {} 开始同步到Sayo", sid);
        BeatmapSetDO set = beatmapSetRepository.findById(sid).orElseThrow();
        set.setSynced(true);
        beatmapSetRepository.save(set);

        if (!needNotify(sid)) {
            log.info("谱面 {} 无需同步到Sayo", sid);
            return;
        }

        try (InputStream is = osuApiClient.download(sid)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            is.transferTo(outputStream);
            Files.write(Paths.get("J:\\" + sid + ".osz"),
                    outputStream.toByteArray(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.SYNC);
        } catch (IOException e) {
            log.error("谱面 {} 下载失败，请检查网络", sid);
            set.setSynced(false);
            beatmapSetRepository.save(set);
            return;
        }
        String url = "http://k3.mothership.top:8093/" + sid + ".osz";


        SayoNotifyDTO notifyDTO = SayoNotifyDTO.builder()
                .sid(sid)
                .url(url)
                .build();
        log.info("谱面 {} 同步开始通知Sayo", sid);
        post("https://tc1.sayobot.cn:25225/BMUPDATE/MKCHEE7J4K7N/",
                objectMapper.writeValueAsString(notifyDTO));

    }


    @SneakyThrows
    private boolean needNotify(Integer sid) {

        List<BeatmapDO> maps = beatmapRepository.findAllBySid(sid);
        List<String> localHashes = maps.stream().map(BeatmapDO::getHash).toList();

        String sayoInfoUrl = "https://dl.sayobot.cn/beatmaps/info/" + sid;
        String result = get(sayoInfoUrl);

        //由于Sayo侧接口会保留已删除难度，不做反向对比
        //如果本地存在Sayo侧不存在的难度，则需要通知
        for (BeatmapDO map : maps) {
            if (!result.contains(map.getHash())) {
                log.info("谱面 {} 本地难度{}在Sayo侧不存在，需要通知", sid, map.getHash());
                return true;
            }
        }
        return false;

    }

    @SneakyThrows
    private String get(String url) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        HttpRequest request = builder.build();

        HttpResponse<String> response =
                send(request);

        return response.body();
    }

    @SneakyThrows
    private void post(String url,
                      String body
    ) {

        HttpRequest request
                = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = send(request);
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;
    }
}
