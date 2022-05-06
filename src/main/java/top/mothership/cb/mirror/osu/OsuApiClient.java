package top.mothership.cb.mirror.osu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.mothership.cb.mirror.common.util.DateUtil;
import top.mothership.cb.mirror.dal.AccountRepository;
import top.mothership.cb.mirror.dal.model.AccountDO;
import top.mothership.cb.mirror.osu.model.BeatmapDTO;
import top.mothership.cb.mirror.osu.model.OsuWebSessionBO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class OsuApiClient {

    @Autowired
    private HttpClient client;

    @Autowired
    private AccountPool accountPool;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String BEATMAP_API_URL = "https://osu.ppy.sh/api/get_beatmaps?k=%s&b=%s";
    private static final String BEATMAP_SET_API_URL = "https://osu.ppy.sh/api/get_beatmaps?k=%s&s=%s";
    private static final String RANKED_BEATMAP_SET_API_URL = "https://osu.ppy.sh/api/get_beatmaps?k=%s&since=%s";
    private static final String HOME_PAGE_URL = "https://osu.ppy.sh/home";
    private static final String LOGIN_URL = "https://osu.ppy.sh/session";
    private static final String DOWNLOAD_URL = "https://osu.ppy.sh/beatmapsets/%s/download";

    public BeatmapDTO getBeatmap(Integer bid) {
        AccountDO account = accountPool.getAccount();
        String url = String.format(BEATMAP_API_URL, account.getAkV1(), bid);
        List<BeatmapDTO> beatmap = get(url, new TypeReference<>() {
        });

        beatmap.iterator().next().setTimeZone(account.getTimeZone());
        return beatmap.iterator().next();
    }

    public List<BeatmapDTO> getBeatmapSet(Integer sid) {
        AccountDO account = accountPool.getAccount();
        String url = String.format(BEATMAP_SET_API_URL, account.getAkV1(), sid);
        List<BeatmapDTO> list = get(url, new TypeReference<>() {
        });

        list.forEach(b -> b.setTimeZone(account.getTimeZone()));
        return list;
    }

    public List<BeatmapDTO> getRankedBeatmapSince(long timestampSecond) {
        AccountDO account = accountPool.getAccount();
        String url = String.format(RANKED_BEATMAP_SET_API_URL,
                account.getAkV1(),
                URLEncoder.encode(DateUtil.toOsuDateString(timestampSecond, account.getTimeZone()), StandardCharsets.UTF_8));

        List<BeatmapDTO> list = get(url, new TypeReference<>() {
        });

        list.forEach(b -> b.setTimeZone(account.getTimeZone()));
        return list;
    }

    @SneakyThrows
    public InputStream download(Integer sid) {

        AccountDO account = accountPool.getAccount();


        return download(sid, account);
    }

    private InputStream download(Integer sid, AccountDO account) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("referer", "https://osu.ppy.sh/beatmapsets/" + sid)
                .header("cookie", "osu_session=" + account.getSession())
                .uri(URI.create(String.format(DOWNLOAD_URL, sid))).build();

        HttpResponse<InputStream> response =
                client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        log.info("开始下载谱面集{} ，response: {}", sid, response);

        if (!Objects.equals(response.statusCode(), 200)){
            OsuWebSessionBO homePageSession = visitHomePage(sid);
            login(sid, account, homePageSession);

            request = HttpRequest.newBuilder()
                    .header("referer", "https://osu.ppy.sh/beatmapsets/" + sid)
                    .header("cookie", "osu_session=" + account.getSession())
                    .uri(URI.create(String.format(DOWNLOAD_URL, sid))).build();
            response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        }

        return response.body();
    }

    private void login(Integer sid, AccountDO account, OsuWebSessionBO homePageSession) throws IOException, InterruptedException {
        Map<Object, Object> data = new LinkedHashMap<>();
        data.put("_token", homePageSession.getCsrfToken());
        data.put("username", account.getUsername());
        data.put("password", account.getPassword());

        HttpRequest request
                = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_URL))
                .header("referer", HOME_PAGE_URL)
                .header("cookie", "XSRF-TOKEN=" + homePageSession.getCsrfToken() + "; osu_session=" + homePageSession.getSession())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(ofFormData(data))
                .build();
        HttpResponse<String> response = send(request);
        log.info("下载谱面集{} 登录官网，response: {}", sid, response);
        if (!Objects.equals(response.statusCode(), 200)) {
            log.warn("下载谱面集合" + sid + "时登陆失败，返回code：" + response.statusCode());
            throw new RuntimeException("下载谱面集" + sid + "时登录失败");
        }

        List<String> cookies = response.headers().map().get("Set-Cookie");
        if (CollectionUtils.isEmpty(cookies)) {
            throw new RuntimeException("下载谱面集" + sid + "时访问官网失败");
        }

        String rawSession = cookies.stream().filter(s -> s.startsWith("osu_session")).findFirst()
                .orElseThrow(() -> new RuntimeException("下载谱面集" + sid + "时访问官网失败"));
        String session = rawSession.substring(rawSession.indexOf("=") + 1, rawSession.indexOf(";"));

        account.setSession(session);
        accountRepository.save(account);

    }

    @SneakyThrows
    private OsuWebSessionBO visitHomePage(Integer sid) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(HOME_PAGE_URL));
        HttpRequest request = builder.build();
        HttpResponse<String> response = send(request);
        log.info("下载谱面集{} 访问官网，response: {}", sid, response);
        if (!Objects.equals(response.statusCode(), 200)) {
            throw new RuntimeException("下载谱面集" + sid + "时访问官网失败");
        }
        List<String> cookies = response.headers().map().get("Set-Cookie");
        if (CollectionUtils.isEmpty(cookies)) {
            throw new RuntimeException("下载谱面集" + sid + "时访问官网失败");
        }

        String rawToken = cookies.stream().filter(s -> s.startsWith("XSRF-TOKEN")).findFirst()
                .orElseThrow(() -> new RuntimeException("下载谱面集" + sid + "时访问官网失败"));
        String token = rawToken.substring(rawToken.indexOf("=") + 1, rawToken.indexOf(";"));

        String rawSession = cookies.stream().filter(s -> s.startsWith("osu_session")).findFirst()
                .orElseThrow(() -> new RuntimeException("下载谱面集" + sid + "时访问官网失败"));
        String session = rawSession.substring(rawSession.indexOf("=") + 1, rawSession.indexOf(";"));

        return OsuWebSessionBO.builder().session(session).csrfToken(token).build();
    }


    @SneakyThrows
    private <T> T get(String url,
                      TypeReference<T> type) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        HttpRequest request = builder.build();

        HttpResponse<String> response =
                send(request);

        return objectMapper.readValue(response.body(), type);
    }



    private static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            String encodeValue = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
            builder.append(encodeValue);
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }


    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;
    }
}
