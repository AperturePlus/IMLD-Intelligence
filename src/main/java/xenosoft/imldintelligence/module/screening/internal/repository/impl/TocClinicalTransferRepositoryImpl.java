package xenosoft.imldintelligence.module.screening.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.screening.internal.model.TocClinicalTransfer;
import xenosoft.imldintelligence.module.screening.internal.repository.TocClinicalTransferRepository;
import xenosoft.imldintelligence.module.screening.internal.repository.mybatis.TocClinicalTransferMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TOC临床转化仓储实现类，基于 MyBatis-Plus 完成TOC临床转化的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class TocClinicalTransferRepositoryImpl implements TocClinicalTransferRepository {
    private final TocClinicalTransferMapper tocClinicalTransferMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TocClinicalTransfer> findById(Long tenantId, Long id) {
        return Optional.ofNullable(tocClinicalTransferMapper.selectOne(new LambdaQueryWrapper<TocClinicalTransfer>()
                .eq(TocClinicalTransfer::getTenantId, tenantId)
                .eq(TocClinicalTransfer::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TocClinicalTransfer> listByTenantId(Long tenantId) {
        return tocClinicalTransferMapper.selectList(new LambdaQueryWrapper<TocClinicalTransfer>()
                .eq(TocClinicalTransfer::getTenantId, tenantId)
                .orderByDesc(TocClinicalTransfer::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TocClinicalTransfer> listByResponseId(Long tenantId, Long responseId) {
        return tocClinicalTransferMapper.selectList(new LambdaQueryWrapper<TocClinicalTransfer>()
                .eq(TocClinicalTransfer::getTenantId, tenantId)
                .eq(TocClinicalTransfer::getResponseId, responseId)
                .orderByDesc(TocClinicalTransfer::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TocClinicalTransfer> listByPatientId(Long tenantId, Long patientId) {
        return tocClinicalTransferMapper.selectList(new LambdaQueryWrapper<TocClinicalTransfer>()
                .eq(TocClinicalTransfer::getTenantId, tenantId)
                .eq(TocClinicalTransfer::getPatientId, patientId)
                .orderByDesc(TocClinicalTransfer::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TocClinicalTransfer save(TocClinicalTransfer tocClinicalTransfer) {
        tocClinicalTransferMapper.insert(tocClinicalTransfer);
        return tocClinicalTransfer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TocClinicalTransfer update(TocClinicalTransfer tocClinicalTransfer) {
        tocClinicalTransferMapper.update(tocClinicalTransfer, new LambdaUpdateWrapper<TocClinicalTransfer>()
                .eq(TocClinicalTransfer::getTenantId, tocClinicalTransfer.getTenantId())
                .eq(TocClinicalTransfer::getId, tocClinicalTransfer.getId()));
        return tocClinicalTransfer;
    }

    @Override
    public boolean updateStatusIfCurrent(Long tenantId,
                                         Long id,
                                         String expectedStatus,
                                         String targetStatus,
                                         Long approvedBy,
                                         OffsetDateTime approvedAt,
                                         String transferNote) {
        LambdaUpdateWrapper<TocClinicalTransfer> wrapper = new LambdaUpdateWrapper<TocClinicalTransfer>()
                .eq(TocClinicalTransfer::getTenantId, tenantId)
                .eq(TocClinicalTransfer::getId, id)
                .eq(TocClinicalTransfer::getTransferStatus, expectedStatus)
                .set(TocClinicalTransfer::getTransferStatus, targetStatus)
                .set(TocClinicalTransfer::getApprovedBy, approvedBy)
                .set(TocClinicalTransfer::getApprovedAt, approvedAt)
                .set(TocClinicalTransfer::getTransferNote, transferNote);
        return tocClinicalTransferMapper.update(null, wrapper) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return tocClinicalTransferMapper.delete(new LambdaQueryWrapper<TocClinicalTransfer>()
                .eq(TocClinicalTransfer::getTenantId, tenantId)
                .eq(TocClinicalTransfer::getId, id)) > 0;
    }
}
