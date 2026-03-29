package com.aegis.wms.application.masterdata.service;

import com.aegis.wms.application.masterdata.dto.BinDTO;
import com.aegis.wms.application.masterdata.service.impl.BinServiceImpl;
import com.aegis.wms.application.masterdata.vo.BinVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 仓房服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class BinServiceTest {

    @Mock
    private BinRepository binRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private BinServiceImpl binService;

    private Warehouse testWarehouse;
    private Bin testBin;
    private BinDTO testDTO;

    @BeforeEach
    void setUp() {
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setWarehouseCode("WH001");
        testWarehouse.setWarehouseName("测试粮库");

        testBin = new Bin();
        testBin.setId(1L);
        testBin.setWarehouseId(1L);
        testBin.setBinCode("B001");
        testBin.setBinName("01仓");
        testBin.setBinType("高大平房仓");
        testBin.setCapacity(new BigDecimal("500.000"));
        testBin.setStatus(1);
        testBin.setCreateTime(LocalDateTime.now());

        testDTO = new BinDTO();
        testDTO.setWarehouseId(1L);
        testDTO.setBinCode("B001");
        testDTO.setBinName("01仓");
        testDTO.setBinType("高大平房仓");
        testDTO.setCapacity(new BigDecimal("500.000"));
        testDTO.setStatus(1);
    }

    @Test
    @DisplayName("创建仓房-成功")
    void testCreateSuccess() {
        when(warehouseRepository.selectById(1L)).thenReturn(testWarehouse);
        when(binRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // Mock: insert后设置ID
        when(binRepository.insert(any(Bin.class))).thenAnswer(invocation -> {
            Bin bin = invocation.getArgument(0);
            bin.setId(1L);
            return 1;
        });

        Long id = binService.create(testDTO);

        assertNotNull(id);
        assertEquals(1L, id);
        verify(binRepository, times(1)).insert(any(Bin.class));
    }

    @Test
    @DisplayName("创建仓房-库区不存在异常")
    void testCreateWarehouseNotFound() {
        when(warehouseRepository.selectById(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> binService.create(testDTO));
        verify(binRepository, never()).insert(any(Bin.class));
    }

    @Test
    @DisplayName("创建仓房-编码已存在异常")
    void testCreateDuplicateCode() {
        when(warehouseRepository.selectById(1L)).thenReturn(testWarehouse);
        when(binRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> binService.create(testDTO));
    }

    @Test
    @DisplayName("更新仓房-成功")
    void testUpdateSuccess() {
        testDTO.setId(1L);
        testDTO.setBinName("更新后的仓房名");

        when(binRepository.selectById(1L)).thenReturn(testBin);
        when(binRepository.updateById(any(Bin.class))).thenReturn(1);

        Boolean result = binService.update(testDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("更新仓房-ID为空异常")
    void testUpdateWithoutId() {
        assertThrows(RuntimeException.class, () -> binService.update(testDTO));
    }

    @Test
    @DisplayName("删除仓房-成功")
    void testDeleteSuccess() {
        when(binRepository.deleteById(1L)).thenReturn(1);

        Boolean result = binService.delete(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("根据ID查询仓房")
    void testGetById() {
        when(binRepository.selectById(1L)).thenReturn(testBin);
        when(warehouseRepository.selectById(1L)).thenReturn(testWarehouse);

        BinVO vo = binService.getById(1L);

        assertNotNull(vo);
        assertEquals("B001", vo.getBinCode());
        assertEquals("测试粮库", vo.getWarehouseName());
    }

    @Test
    @DisplayName("根据库区ID查询仓房列表")
    void testListByWarehouseId() {
        Bin bin2 = new Bin();
        bin2.setId(2L);
        bin2.setWarehouseId(1L);
        bin2.setBinCode("B002");

        when(binRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testBin, bin2));
        when(warehouseRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testWarehouse));

        List<BinVO> list = binService.listByWarehouseId(1L);

        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("分页查询仓房")
    void testPage() {
        Page<Bin> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testBin));
        mockPage.setTotal(1);

        when(binRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);
        when(warehouseRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testWarehouse));

        Page<BinVO> page = new Page<>(1, 10);
        PageResult<BinVO> result = binService.page(page, 1L, "01仓", 1);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }
}