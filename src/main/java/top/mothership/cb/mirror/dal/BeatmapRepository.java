package top.mothership.cb.mirror.dal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import top.mothership.cb.mirror.dal.model.BeatmapDO;

import java.util.List;

@Repository
public interface BeatmapRepository extends CrudRepository<BeatmapDO, Integer> {

    void deleteAllBySid(Integer sid);

    List<BeatmapDO> findAllBySid(Integer sid);

}
