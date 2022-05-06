package top.mothership.cb.mirror.dal.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity(name = "beatmap")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class BeatmapDO {
    @Id
    private Integer bid;
    private Integer sid;
    private String hash;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        BeatmapDO beatmapDO = (BeatmapDO) o;
        return bid != null && Objects.equals(bid, beatmapDO.bid);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
