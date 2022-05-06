package top.mothership.cb.mirror.dal;

import org.springframework.data.repository.CrudRepository;
import top.mothership.cb.mirror.dal.model.BeatmapDO;
import top.mothership.cb.mirror.dal.model.ConfigDO;

public interface ConfigRepository  extends CrudRepository<ConfigDO, String> {
}
