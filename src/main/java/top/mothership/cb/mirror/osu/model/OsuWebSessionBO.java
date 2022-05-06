package top.mothership.cb.mirror.osu.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OsuWebSessionBO {
    private String csrfToken;
    private String session;

}
