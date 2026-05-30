package xenosoft.imldintelligence.module.community.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityBoard;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityBoardRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityBoardMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommunityBoardRepositoryImpl implements CommunityBoardRepository {
    private final CommunityBoardMapper communityBoardMapper;

    @Override
    public Optional<CommunityBoard> findById(Long tenantId, Long boardId) {
        return Optional.ofNullable(communityBoardMapper.selectOne(new LambdaQueryWrapper<CommunityBoard>()
                .eq(CommunityBoard::getTenantId, tenantId)
                .eq(CommunityBoard::getId, boardId)));
    }

    @Override
    public Optional<CommunityBoard> findByBoardCode(Long tenantId, String boardCode) {
        if (boardCode == null || boardCode.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(communityBoardMapper.selectOne(new LambdaQueryWrapper<CommunityBoard>()
                .eq(CommunityBoard::getTenantId, tenantId)
                .eq(CommunityBoard::getBoardCode, boardCode.trim())));
    }

    @Override
    public List<CommunityBoard> listByTenantId(Long tenantId, String status, String diseaseScope) {
        LambdaQueryWrapper<CommunityBoard> query = new LambdaQueryWrapper<CommunityBoard>()
                .eq(CommunityBoard::getTenantId, tenantId);
        if (status != null && !status.isBlank()) {
            query.eq(CommunityBoard::getStatus, status.trim());
        }
        if (diseaseScope != null && !diseaseScope.isBlank()) {
            query.eq(CommunityBoard::getDiseaseScope, diseaseScope.trim());
        }
        query.orderByAsc(CommunityBoard::getSortOrder)
                .orderByDesc(CommunityBoard::getId);
        return communityBoardMapper.selectList(query);
    }

    @Override
    public CommunityBoard save(CommunityBoard board) {
        communityBoardMapper.insert(board);
        return board;
    }

    @Override
    public CommunityBoard update(CommunityBoard board) {
        OffsetDateTime now = OffsetDateTime.now();
        communityBoardMapper.update(null, new LambdaUpdateWrapper<CommunityBoard>()
                .eq(CommunityBoard::getTenantId, board.getTenantId())
                .eq(CommunityBoard::getId, board.getId())
                .set(CommunityBoard::getBoardCode, board.getBoardCode())
                .set(CommunityBoard::getBoardName, board.getBoardName())
                .set(CommunityBoard::getDescription, board.getDescription())
                .set(CommunityBoard::getDiseaseScope, board.getDiseaseScope())
                .set(CommunityBoard::getStatus, board.getStatus())
                .set(CommunityBoard::getSortOrder, board.getSortOrder())
                .set(CommunityBoard::getUpdatedAt, now));
        board.setUpdatedAt(now);
        return board;
    }
}

