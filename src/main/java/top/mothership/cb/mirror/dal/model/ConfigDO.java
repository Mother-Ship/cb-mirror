package top.mothership.cb.mirror.dal.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity(name = "config")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class ConfigDO {

    @Id
    private String name;
    private String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ConfigDO configDO = (ConfigDO) o;
        return name != null && Objects.equals(name, configDO.name);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
