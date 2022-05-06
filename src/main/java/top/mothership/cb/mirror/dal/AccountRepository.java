package top.mothership.cb.mirror.dal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import top.mothership.cb.mirror.dal.model.AccountDO;
@Repository
public interface AccountRepository extends CrudRepository<AccountDO, Long> {
}
