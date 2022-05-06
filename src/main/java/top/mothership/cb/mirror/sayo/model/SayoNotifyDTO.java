package top.mothership.cb.mirror.sayo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SayoNotifyDTO {
    private Integer sid;

    private String url;

    private String callbackUri;
}
