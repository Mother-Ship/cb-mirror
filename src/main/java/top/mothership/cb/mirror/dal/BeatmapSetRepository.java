package top.mothership.cb.mirror.dal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import top.mothership.cb.mirror.dal.model.BeatmapSetDO;

import java.util.Optional;

@Repository
public interface BeatmapSetRepository extends CrudRepository<BeatmapSetDO, Integer> {

    Optional<BeatmapSetDO> findDistinctTopByOrderBySidDesc();

    Optional<BeatmapSetDO> findDistinctTopByStatusAndSyncedOrderByLastUpdateByMapperDesc(Integer status, boolean synced);
}
