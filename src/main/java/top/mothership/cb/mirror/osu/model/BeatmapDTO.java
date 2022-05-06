package top.mothership.cb.mirror.osu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BeatmapDTO {
    @JsonProperty("beatmapset_id")
    private Integer setId;

    @JsonProperty("beatmap_id")
    private Integer beatmapId;

    private Integer approved;

    @JsonProperty("file_md5")
    private String md5;

    @JsonProperty("last_update")
    private String lastUpdate;

    private Integer timeZone;
}
