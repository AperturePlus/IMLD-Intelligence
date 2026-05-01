package xenosoft.imldintelligence.module.community.internal.repository;

import xenosoft.imldintelligence.module.community.internal.model.CommunityBoard;

import java.util.List;
import java.util.Optional;

public interface CommunityBoardRepository {
    Optional<CommunityBoard> findById(Long tenantId, Long boardId);

    Optional<CommunityBoard> findByBoardCode(Long tenantId, String boardCode);

    List<CommunityBoard> listByTenantId(Long tenantId, String status, String diseaseScope);

    CommunityBoard save(CommunityBoard board);

    CommunityBoard update(CommunityBoard board);
}

