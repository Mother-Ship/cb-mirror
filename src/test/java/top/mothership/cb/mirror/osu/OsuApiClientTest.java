package top.mothership.cb.mirror.osu;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import top.mothership.cb.mirror.dal.model.AccountDO;
import top.mothership.cb.mirror.osu.model.BeatmapDTO;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class OsuApiClientTest {
    @Autowired
    private OsuApiClient client;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AccountPool accountPool;

    @BeforeEach
    public void mockAccountPool() {
        Mockito.when(accountPool.getAccount())
                .thenReturn(
                        AccountDO.builder()
                                .username("")
                                .password("")
                                .akV1("")
                                .build());
    }

    @Test
    @SneakyThrows
    public void testGetBeatmap() {
        BeatmapDTO beatmap = client.getBeatmap(53554);
        assertNotNull(beatmap);
    }

    @Test
    @SneakyThrows
    public void testDownloadBeatmap() {
        InputStream is = client.download(13223);
        assert is.available() == 0;
    }

    @Test
    public void testGetBeatmapSet() {
        List<BeatmapDTO> beatmaps = client.getBeatmapSet(13223);
        assert beatmaps.size() == 3;
    }
}
