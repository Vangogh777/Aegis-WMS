package com.aegis.wms.application.masterdata.service;

import com.aegis.wms.application.masterdata.dto.WarehouseDTO;
import com.aegis.wms.application.masterdata.service.impl.WarehouseServiceImpl;
import com.aegis.wms.application.masterdata.vo.WarehouseVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
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
import static org.mockito.Mockito.*;

/**
 * 库区服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse testWarehouse;
    private WarehouseDTO testDTO;

    @BeforeEach
    void setUp() {
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setWarehouseCode("WH001");
        testWarehouse.setWarehouseName("测试粮库");
        testWarehouse.setAddress("测试地址");
        testWarehouse.setTotalCapacity(new BigDecimal("10000.000"));
        testWarehouse.setStatus(1);
        testWarehouse.setCreateTime(LocalDateTime.now());
        testWarehouse.setUpdateTime(LocalDateTime.now());

        testDTO = new WarehouseDTO();
        testDTO.setWarehouseCode("WH001");
        testDTO.setWarehouseName("测试粮库");
        testDTO.setAddress("测试地址");
        testDTO.setTotalCapacity(new BigDecimal("10000.000"));
        testDTO.setStatus(1);
    }

    @Test
    @DisplayName("创建库区-成功")
    void testCreateSuccess() {
        // Mock: 编码不存在
        when(warehouseRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // Mock: insert后设置ID
        when(warehouseRepository.insert(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse warehouse = invocation.getArgument(0);
            warehouse.setId(1L);
            return 1;
        });

        Long id = warehouseService.create(testDTO);

        assertNotNull(id);
        assertEquals(1L, id);
        verify(warehouseRepository, times(1)).insert(any(Warehouse.class));
    }

    @Test
    @DisplayName("创建库区-编码已存在异常")
    void testCreateDuplicateCode() {
        // Mock: 编码已存在
        when(warehouseRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> warehouseService.create(testDTO));
        verify(warehouseRepository, never()).insert(any(Warehouse.class));
    }

    @Test
    @DisplayName("更新库区-成功")
    void testUpdateSuccess() {
        testDTO.setId(1L);
        testDTO.setWarehouseName("更新后的粮库名");

        when(warehouseRepository.selectById(1L)).thenReturn(testWarehouse);
        when(warehouseRepository.updateById(any(Warehouse.class))).thenReturn(1);

        Boolean result = warehouseService.update(testDTO);

        assertTrue(result);
        verify(warehouseRepository, times(1)).updateById(any(Warehouse.class));
    }

    @Test
    @DisplayName("更新库区-ID为空异常")
    void testUpdateWithoutId() {
        assertThrows(RuntimeException.class, () -> warehouseService.update(testDTO));
    }

    @Test
    @DisplayName("更新库区-库区不存在异常")
    void testUpdateNotFound() {
        testDTO.setId(999L);
        when(warehouseRepository.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> warehouseService.update(testDTO));
    }

    @Test
    @DisplayName("删除库区-成功")
    void testDeleteSuccess() {
        when(warehouseRepository.deleteById(1L)).thenReturn(1);

        Boolean result = warehouseService.delete(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("根据ID查询库区")
    void testGetById() {
        when(warehouseRepository.selectById(1L)).thenReturn(testWarehouse);

        WarehouseVO vo = warehouseService.getById(1L);

        assertNotNull(vo);
        assertEquals("WH001", vo.getWarehouseCode());
        assertEquals("测试粮库", vo.getWarehouseName());
    }

    @Test
    @DisplayName("根据ID查询库区-不存在返回null")
    void testGetByIdNotFound() {
        when(warehouseRepository.selectById(999L)).thenReturn(null);

        WarehouseVO vo = warehouseService.getById(999L);

        assertNull(vo);
    }

    @Test
    @DisplayName("根据编码查询库区")
    void testGetByCode() {
        when(warehouseRepository.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testWarehouse);

        WarehouseVO vo = warehouseService.getByCode("WH001");

        assertNotNull(vo);
        assertEquals("WH001", vo.getWarehouseCode());
    }

    @Test
    @DisplayName("查询所有库区")
    void testListAll() {
        Warehouse warehouse2 = new Warehouse();
        warehouse2.setId(2L);
        warehouse2.setWarehouseCode("WH002");
        warehouse2.setWarehouseName("粮库2");

        when(warehouseRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testWarehouse, warehouse2));

        List<WarehouseVO> list = warehouseService.listAll();

        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("分页查询库区")
    void testPage() {
        Page<Warehouse> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testWarehouse));
        mockPage.setTotal(1);

        when(warehouseRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        Page<WarehouseVO> page = new Page<>(1, 10);
        PageResult<WarehouseVO> result = warehouseService.page(page, "测试", 1);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }
}