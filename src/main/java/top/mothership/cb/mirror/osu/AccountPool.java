package top.mothership.cb.mirror.osu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.mothership.cb.mirror.dal.AccountRepository;
import top.mothership.cb.mirror.dal.model.AccountDO;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AccountPool {

    @Autowired
    private AccountRepository accountRepository;

    @Transactional(readOnly = false)
    public AccountDO getAccount() {
        long count = accountRepository.count();
        Iterator<AccountDO> accounts = accountRepository.findAll().iterator();

        if (count == 0 || !accounts.hasNext()) {
            throw new RuntimeException("账号池获取账号失败！");
        }

        long index = ThreadLocalRandom.current().nextLong(count);
        for (int i = 0; i < index; i++) {
            accounts.next();
        }
        return accounts.next();
    }

}
