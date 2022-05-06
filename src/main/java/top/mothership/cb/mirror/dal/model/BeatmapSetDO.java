package top.mothership.cb.mirror.dal.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "beatmap_set")
@Builder
@AllArgsConstructor
public class BeatmapSetDO {
    @Id
    private Integer sid;
    private Long lastFetch;
    private Long lastUpdateByMapper;
    private Integer status;
    private Boolean synced;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        BeatmapSetDO that = (BeatmapSetDO) o;
        return sid != null && Objects.equals(sid, that.sid);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
