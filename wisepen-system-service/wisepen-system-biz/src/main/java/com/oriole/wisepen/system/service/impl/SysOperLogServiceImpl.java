package com.oriole.wisepen.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.system.api.domain.dto.SysOperLogDTO;
import com.oriole.wisepen.system.api.domain.dto.SysOperLogQueryDTO;
import com.oriole.wisepen.system.domain.entity.SysOperLogEntity;
import com.oriole.wisepen.system.mapper.SysOperLogMapper;
import com.oriole.wisepen.system.service.SysOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SysOperLogServiceImpl implements SysOperLogService {

    @Autowired
    private SysOperLogMapper sysOperLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveLog(SysOperLogDTO dto) {
        SysOperLogEntity entity = BeanUtil.copyProperties(dto, SysOperLogEntity.class);
        int rows = sysOperLogMapper.insert(entity);
        return rows > 0;
    }

    @Override
    public PageR<SysOperLogDTO> listLogs(SysOperLogQueryDTO query) {
        SysOperLogQueryDTO safeQuery = query == null ? new SysOperLogQueryDTO() : query;
        int page = safeQuery.getPage() > 0 ? safeQuery.getPage() : 1;
        int size = safeQuery.getSize() > 0 ? safeQuery.getSize() : 20;
        LambdaQueryWrapper<SysOperLogEntity> wrapper = buildQueryWrapper(safeQuery);
        IPage<SysOperLogEntity> result = sysOperLogMapper.selectPage(new Page<>(page, size), wrapper);
        return toPageR(result, page, size);
    }

    private LambdaQueryWrapper<SysOperLogEntity> buildQueryWrapper(SysOperLogQueryDTO query) {
        LambdaQueryWrapper<SysOperLogEntity> wrapper = new LambdaQueryWrapper<SysOperLogEntity>()
                .in(query.getOperUrls() != null && !query.getOperUrls().isEmpty(),
                        SysOperLogEntity::getOperUrl, query.getOperUrls())
                .eq(query.getOperUserId() != null,
                        SysOperLogEntity::getOperUserId, query.getOperUserId())
                .ge(query.getStartTime() != null,
                        SysOperLogEntity::getOperTime, query.getStartTime())
                .le(query.getEndTime() != null,
                        SysOperLogEntity::getOperTime, query.getEndTime())
                .eq(query.getStatus() != null,
                        SysOperLogEntity::getStatus, query.getStatus());

        return wrapper.orderByDesc(SysOperLogEntity::getOperTime);
    }

    private PageR<SysOperLogDTO> toPageR(IPage<SysOperLogEntity> result, int page, int size) {
        PageR<SysOperLogDTO> pageR = new PageR<>(result.getTotal(), page, size);
        List<SysOperLogDTO> list = result.getRecords().stream()
                .map(entity -> BeanUtil.copyProperties(entity, SysOperLogDTO.class))
                .toList();
        pageR.addAll(list);
        return pageR;
    }
}
