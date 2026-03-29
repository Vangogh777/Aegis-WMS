package com.aegis.wms.application.masterdata.service;

import com.aegis.wms.application.masterdata.dto.PositionDTO;
import com.aegis.wms.application.masterdata.service.impl.PositionServiceImpl;
import com.aegis.wms.application.masterdata.vo.PositionVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.masterdata.entity.Position;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.masterdata.repository.PositionRepository;
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
 * 货位服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private BinRepository binRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private PositionServiceImpl positionService;

    private Warehouse testWarehouse;
    private Bin testBin;
    private Position testPosition;
    private PositionDTO testDTO;

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

        testPosition = new Position();
        testPosition.setId(1L);
        testPosition.setBinId(1L);
        testPosition.setWarehouseId(1L);
        testPosition.setPositionCode("P001");
        testPosition.setPositionName("A区");
        testPosition.setCapacity(new BigDecimal("100.000"));
        testPosition.setStatus(1);
        testPosition.setCreateTime(LocalDateTime.now());

        testDTO = new PositionDTO();
        testDTO.setBinId(1L);
        testDTO.setWarehouseId(1L);
        testDTO.setPositionCode("P001");
        testDTO.setPositionName("A区");
        testDTO.setCapacity(new BigDecimal("100.000"));
        testDTO.setStatus(1);
    }

    @Test
    @DisplayName("创建货位-成功")
    void testCreateSuccess() {
        when(binRepository.selectById(1L)).thenReturn(testBin);
        when(positionRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // Mock: insert后设置ID
        when(positionRepository.insert(any(Position.class))).thenAnswer(invocation -> {
            Position position = invocation.getArgument(0);
            position.setId(1L);
            return 1;
        });

        Long id = positionService.create(testDTO);

        assertNotNull(id);
        assertEquals(1L, id);
        verify(positionRepository, times(1)).insert(any(Position.class));
    }

    @Test
    @DisplayName("创建货位-仓房不存在异常")
    void testCreateBinNotFound() {
        when(binRepository.selectById(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> positionService.create(testDTO));
        verify(positionRepository, never()).insert(any(Position.class));
    }

    @Test
    @DisplayName("创建货位-编码已存在异常")
    void testCreateDuplicateCode() {
        when(binRepository.selectById(1L)).thenReturn(testBin);
        when(positionRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> positionService.create(testDTO));
    }

    @Test
    @DisplayName("创建货位-自动填充库区ID")
    void testCreateAutoFillWarehouseId() {
        testDTO.setWarehouseId(null); // 不传库区ID

        when(binRepository.selectById(1L)).thenReturn(testBin);
        when(positionRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // Mock: insert后设置ID
        when(positionRepository.insert(any(Position.class))).thenAnswer(invocation -> {
            Position position = invocation.getArgument(0);
            position.setId(1L);
            return 1;
        });

        Long id = positionService.create(testDTO);

        assertNotNull(id);
        assertEquals(1L, id);
        // 验证insert时warehouseId被自动填充
        verify(positionRepository).insert(argThat(p -> p.getWarehouseId().equals(1L)));
    }

    @Test
    @DisplayName("更新货位-成功")
    void testUpdateSuccess() {
        testDTO.setId(1L);
        testDTO.setPositionName("更新后的货位名");

        when(positionRepository.selectById(1L)).thenReturn(testPosition);
        when(positionRepository.updateById(any(Position.class))).thenReturn(1);

        Boolean result = positionService.update(testDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("更新货位-ID为空异常")
    void testUpdateWithoutId() {
        assertThrows(RuntimeException.class, () -> positionService.update(testDTO));
    }

    @Test
    @DisplayName("删除货位-成功")
    void testDeleteSuccess() {
        when(positionRepository.deleteById(1L)).thenReturn(1);

        Boolean result = positionService.delete(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("根据ID查询货位")
    void testGetById() {
        when(positionRepository.selectById(1L)).thenReturn(testPosition);
        when(binRepository.selectById(1L)).thenReturn(testBin);
        when(warehouseRepository.selectById(1L)).thenReturn(testWarehouse);

        PositionVO vo = positionService.getById(1L);

        assertNotNull(vo);
        assertEquals("P001", vo.getPositionCode());
        assertEquals("B001", vo.getBinCode());
        assertEquals("测试粮库", vo.getWarehouseName());
    }

    @Test
    @DisplayName("根据仓房ID查询货位列表")
    void testListByBinId() {
        Position position2 = new Position();
        position2.setId(2L);
        position2.setBinId(1L);
        position2.setWarehouseId(1L);
        position2.setPositionCode("P002");

        when(positionRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testPosition, position2));
        when(binRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testBin));
        when(warehouseRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testWarehouse));

        List<PositionVO> list = positionService.listByBinId(1L);

        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("分页查询货位")
    void testPage() {
        Page<Position> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testPosition));
        mockPage.setTotal(1);

        when(positionRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);
        when(binRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testBin));
        when(warehouseRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testWarehouse));

        Page<PositionVO> page = new Page<>(1, 10);
        PageResult<PositionVO> result = positionService.page(page, 1L, "A区", 1);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("级联选择查询")
    void testListCascade() {
        when(positionRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testPosition));
        when(binRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testBin));
        when(warehouseRepository.selectBatchIds(anyList()))
                .thenReturn(Arrays.asList(testWarehouse));

        List<PositionVO> list = positionService.listCascade(1L, null);

        assertEquals(1, list.size());
        assertNotNull(list.get(0).getBinCode());
        assertNotNull(list.get(0).getWarehouseName());
    }
}