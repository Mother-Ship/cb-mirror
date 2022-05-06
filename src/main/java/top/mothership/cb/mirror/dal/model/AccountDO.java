package top.mothership.cb.mirror.dal.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "account")
public class AccountDO {
    @Id
    private Long uid;
    private String username;
    private String akV1;
    private String akV2;
    private String password;
    private String session;
    private Integer timeZone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AccountDO accountDO = (AccountDO) o;
        return uid != null && Objects.equals(uid, accountDO.uid);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
