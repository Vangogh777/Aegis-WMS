package com.aegis.wms.application.masterdata.service;

import com.aegis.wms.application.masterdata.dto.WarehouseDTO;
import com.aegis.wms.application.masterdata.dto.BinDTO;
import com.aegis.wms.application.masterdata.dto.PositionDTO;
import com.aegis.wms.application.masterdata.vo.WarehouseVO;
import com.aegis.wms.application.masterdata.vo.BinVO;
import com.aegis.wms.application.masterdata.vo.PositionVO;
import com.aegis.wms.application.masterdata.service.impl.WarehouseServiceImpl;
import com.aegis.wms.application.masterdata.service.impl.BinServiceImpl;
import com.aegis.wms.application.masterdata.service.impl.PositionServiceImpl;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.masterdata.repository.PositionRepository;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础主数据集成测试
 * 连接真实MySQL数据库进行测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // 每个测试方法执行后自动回滚，保持数据库干净
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MasterDataIntegrationTest {

    @Autowired
    private WarehouseServiceImpl warehouseService;

    @Autowired
    private BinServiceImpl binService;

    @Autowired
    private PositionServiceImpl positionService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private PositionRepository positionRepository;

    // 用于存储测试过程中创建的ID
    private static Long testWarehouseId;
    private static Long testBinId;
    private static Long testPositionId;

    // ========================================
    // 库区测试
    // ========================================

    @Test
    @Order(1)
    @DisplayName("集成测试-创建库区")
    void testCreateWarehouse() {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setWarehouseCode("TEST_WH_001");
        dto.setWarehouseName("测试粮库-集成测试");
        dto.setAddress("测试地址-集成测试");
        dto.setTotalCapacity(new BigDecimal("10000.000"));
        dto.setStatus(1);

        testWarehouseId = warehouseService.create(dto);

        assertNotNull(testWarehouseId, "库区ID不应为空");
        assertTrue(testWarehouseId > 0, "库区ID应该大于0");

        System.out.println("✅ 创建库区成功，ID: " + testWarehouseId);
    }

    @Test
    @Order(2)
    @DisplayName("集成测试-查询库区")
    void testGetWarehouseById() {
        // 先创建一个库区用于查询
        WarehouseDTO dto = new WarehouseDTO();
        dto.setWarehouseCode("TEST_WH_QUERY");
        dto.setWarehouseName("测试查询粮库");
        dto.setAddress("测试查询地址");
        dto.setTotalCapacity(new BigDecimal("5000.000"));
        dto.setStatus(1);

        Long id = warehouseService.create(dto);
        testWarehouseId = id;

        WarehouseVO vo = warehouseService.getById(id);

        assertNotNull(vo, "库区VO不应为空");
        assertEquals("TEST_WH_QUERY", vo.getWarehouseCode(), "库区编码应匹配");
        assertEquals("测试查询粮库", vo.getWarehouseName(), "粮库名称应匹配");
        assertEquals(new BigDecimal("5000.000"), vo.getTotalCapacity(), "容量应匹配");

        System.out.println("✅ 查询库区成功: " + vo.getWarehouseName());
    }

    @Test
    @Order(3)
    @DisplayName("集成测试-更新库区")
    void testUpdateWarehouse() {
        // 先创建
        WarehouseDTO createDto = new WarehouseDTO();
        createDto.setWarehouseCode("TEST_WH_UPDATE");
        createDto.setWarehouseName("更新前名称");
        createDto.setAddress("更新前地址");
        createDto.setTotalCapacity(new BigDecimal("3000.000"));
        createDto.setStatus(1);

        Long id = warehouseService.create(createDto);

        // 更新
        WarehouseDTO updateDto = new WarehouseDTO();
        updateDto.setId(id);
        updateDto.setWarehouseName("更新后名称-集成测试");
        updateDto.setAddress("更新后地址");
        updateDto.setTotalCapacity(new BigDecimal("8000.000"));

        Boolean result = warehouseService.update(updateDto);

        assertTrue(result, "更新应该成功");

        // 验证更新结果
        WarehouseVO vo = warehouseService.getById(id);
        assertEquals("更新后名称-集成测试", vo.getWarehouseName(), "名称应该已更新");
        assertEquals(new BigDecimal("8000.000"), vo.getTotalCapacity(), "容量应该已更新");

        System.out.println("✅ 更新库区成功");
    }

    @Test
    @Order(4)
    @DisplayName("集成测试-查询所有库区")
    void testListAllWarehouses() {
        // 创建几个库区
        for (int i = 1; i <= 3; i++) {
            WarehouseDTO dto = new WarehouseDTO();
            dto.setWarehouseCode("TEST_WH_LIST_" + i);
            dto.setWarehouseName("列表测试粮库_" + i);
            dto.setAddress("测试地址_" + i);
            dto.setTotalCapacity(new BigDecimal("1000.000"));
            dto.setStatus(1);
            warehouseService.create(dto);
        }

        List<WarehouseVO> list = warehouseService.listAll();

        assertNotNull(list, "列表不应为空");
        assertTrue(list.size() >= 3, "至少应有3个库区");

        System.out.println("✅ 查询所有库区成功，数量: " + list.size());
    }

    @Test
    @Order(5)
    @DisplayName("集成测试-分页查询库区")
    void testPageWarehouses() {
        // 创建多个库区用于分页测试
        for (int i = 10; i <= 15; i++) {
            WarehouseDTO dto = new WarehouseDTO();
            dto.setWarehouseCode("TEST_WH_PAGE_" + i);
            dto.setWarehouseName("分页测试粮库_" + i);
            dto.setAddress("分页测试地址_" + i);
            dto.setTotalCapacity(new BigDecimal("500.000"));
            dto.setStatus(1);
            warehouseService.create(dto);
        }

        Page<WarehouseVO> page = new Page<>(1, 5);
        PageResult<WarehouseVO> result = warehouseService.page(page, "分页测试", 1);

        assertNotNull(result, "分页结果不应为空");
        assertEquals(5, result.getSize(), "每页大小应为5");
        // 调试输出
        System.out.println("分页查询结果 - Total: " + result.getTotal() + ", Records: " + result.getRecords().size());
        // 修正断言：由于事务回滚，每个测试是独立的，检查创建的记录数
        assertTrue(result.getTotal() >= 6, "总数应大于等于6，实际: " + result.getTotal());
        assertTrue(result.getRecords().size() <= 5, "当前页记录数不应超过每页大小");

        System.out.println("✅ 分页查询库区成功，总数: " + result.getTotal() + ", 当前页记录数: " + result.getRecords().size());
    }

    // ========================================
    // 仓房测试
    // ========================================

    @Test
    @Order(6)
    @DisplayName("集成测试-创建仓房")
    void testCreateBin() {
        // 先创建库区
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_FOR_BIN");
        whDto.setWarehouseName("仓房测试粮库");
        whDto.setAddress("仓房测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        testWarehouseId = warehouseService.create(whDto);

        // 创建仓房
        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(testWarehouseId);
        binDto.setBinCode("TEST_BIN_001");
        binDto.setBinName("测试01仓");
        binDto.setBinType("高大平房仓");
        binDto.setCapacity(new BigDecimal("500.000"));
        binDto.setStatus(1);

        testBinId = binService.create(binDto);

        assertNotNull(testBinId, "仓房ID不应为空");
        assertTrue(testBinId > 0, "仓房ID应该大于0");

        System.out.println("✅ 创建仓房成功，ID: " + testBinId);
    }

    @Test
    @Order(7)
    @DisplayName("集成测试-查询仓房(带库区信息)")
    void testGetBinById() {
        // 先创建库区和仓房
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_BIN_QUERY");
        whDto.setWarehouseName("仓房查询测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        Long whId = warehouseService.create(whDto);

        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(whId);
        binDto.setBinCode("TEST_BIN_QUERY");
        binDto.setBinName("查询测试仓");
        binDto.setBinType("浅圆仓");
        binDto.setCapacity(new BigDecimal("300.000"));
        binDto.setStatus(1);
        Long binId = binService.create(binDto);

        BinVO vo = binService.getById(binId);

        assertNotNull(vo, "仓房VO不应为空");
        assertEquals("TEST_BIN_QUERY", vo.getBinCode(), "仓房编码应匹配");
        assertEquals("仓房查询测试粮库", vo.getWarehouseName(), "库区名称应被关联查询出来");

        System.out.println("✅ 查询仓房成功，仓房: " + vo.getBinName() + ", 所属库区: " + vo.getWarehouseName());
    }

    @Test
    @Order(8)
    @DisplayName("集成测试-根据库区ID查询仓房列表")
    void testListBinsByWarehouseId() {
        // 创建库区
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_BIN_LIST");
        whDto.setWarehouseName("仓房列表测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        Long whId = warehouseService.create(whDto);

        // 创建多个仓房
        for (int i = 1; i <= 3; i++) {
            BinDTO binDto = new BinDTO();
            binDto.setWarehouseId(whId);
            binDto.setBinCode("TEST_BIN_LIST_" + i);
            binDto.setBinName("列表测试仓_" + i);
            binDto.setBinType("高大平房仓");
            binDto.setCapacity(new BigDecimal("200.000"));
            binDto.setStatus(1);
            binService.create(binDto);
        }

        List<BinVO> list = binService.listByWarehouseId(whId);

        assertNotNull(list, "列表不应为空");
        assertEquals(3, list.size(), "应有3个仓房");
        // 验证每个仓房都有库区名称
        for (BinVO vo : list) {
            assertEquals("仓房列表测试粮库", vo.getWarehouseName(), "库区名称应被关联查询");
        }

        System.out.println("✅ 根据库区查询仓房列表成功，数量: " + list.size());
    }

    // ========================================
    // 货位测试
    // ========================================

    @Test
    @Order(9)
    @DisplayName("集成测试-创建货位")
    void testCreatePosition() {
        // 创建库区
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_FOR_POS");
        whDto.setWarehouseName("货位测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        testWarehouseId = warehouseService.create(whDto);

        // 创建仓房
        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(testWarehouseId);
        binDto.setBinCode("TEST_BIN_FOR_POS");
        binDto.setBinName("货位测试仓");
        binDto.setBinType("高大平房仓");
        binDto.setCapacity(new BigDecimal("500.000"));
        binDto.setStatus(1);
        testBinId = binService.create(binDto);

        // 创建货位
        PositionDTO posDto = new PositionDTO();
        posDto.setBinId(testBinId);
        posDto.setWarehouseId(testWarehouseId);
        posDto.setPositionCode("TEST_POS_001");
        posDto.setPositionName("测试A区");
        posDto.setCapacity(new BigDecimal("100.000"));
        posDto.setStatus(1);

        testPositionId = positionService.create(posDto);

        assertNotNull(testPositionId, "货位ID不应为空");
        assertTrue(testPositionId > 0, "货位ID应该大于0");

        System.out.println("✅ 创建货位成功，ID: " + testPositionId);
    }

    @Test
    @Order(10)
    @DisplayName("集成测试-查询货位(带仓房和库区信息)")
    void testGetPositionById() {
        // 创建完整的三级结构
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_POS_QUERY");
        whDto.setWarehouseName("货位查询测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        Long whId = warehouseService.create(whDto);

        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(whId);
        binDto.setBinCode("TEST_BIN_POS_QUERY");
        binDto.setBinName("货位查询测试仓");
        binDto.setBinType("浅圆仓");
        binDto.setCapacity(new BigDecimal("300.000"));
        binDto.setStatus(1);
        Long binId = binService.create(binDto);

        PositionDTO posDto = new PositionDTO();
        posDto.setBinId(binId);
        posDto.setWarehouseId(whId);
        posDto.setPositionCode("TEST_POS_QUERY");
        posDto.setPositionName("查询测试区");
        posDto.setCapacity(new BigDecimal("50.000"));
        posDto.setStatus(1);
        Long posId = positionService.create(posDto);

        PositionVO vo = positionService.getById(posId);

        assertNotNull(vo, "货位VO不应为空");
        assertEquals("TEST_POS_QUERY", vo.getPositionCode(), "货位编码应匹配");
        assertEquals("TEST_BIN_POS_QUERY", vo.getBinCode(), "仓房编码应被关联查询");
        assertEquals("货位查询测试粮库", vo.getWarehouseName(), "库区名称应被关联查询");

        System.out.println("✅ 查询货位成功，货位: " + vo.getPositionName() +
                          ", 所属仓房: " + vo.getBinCode() +
                          ", 所属库区: " + vo.getWarehouseName());
    }

    @Test
    @Order(11)
    @DisplayName("集成测试-根据仓房ID查询货位列表")
    void testListPositionsByBinId() {
        // 创建三级结构
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_POS_LIST");
        whDto.setWarehouseName("货位列表测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        Long whId = warehouseService.create(whDto);

        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(whId);
        binDto.setBinCode("TEST_BIN_POS_LIST");
        binDto.setBinName("货位列表测试仓");
        binDto.setBinType("高大平房仓");
        binDto.setCapacity(new BigDecimal("300.000"));
        binDto.setStatus(1);
        Long binId = binService.create(binDto);

        // 创建多个货位
        for (int i = 1; i <= 3; i++) {
            PositionDTO posDto = new PositionDTO();
            posDto.setBinId(binId);
            posDto.setWarehouseId(whId);
            posDto.setPositionCode("TEST_POS_LIST_" + i);
            posDto.setPositionName("列表测试区_" + i);
            posDto.setCapacity(new BigDecimal("50.000"));
            posDto.setStatus(1);
            positionService.create(posDto);
        }

        List<PositionVO> list = positionService.listByBinId(binId);

        assertNotNull(list, "列表不应为空");
        assertEquals(3, list.size(), "应有3个货位");

        System.out.println("✅ 根据仓房查询货位列表成功，数量: " + list.size());
    }

    @Test
    @Order(12)
    @DisplayName("集成测试-级联选择查询")
    void testListCascade() {
        // 创建三级结构
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_CASCADE");
        whDto.setWarehouseName("级联选择测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        Long whId = warehouseService.create(whDto);

        BinDTO binDto1 = new BinDTO();
        binDto1.setWarehouseId(whId);
        binDto1.setBinCode("TEST_BIN_CASCADE_1");
        binDto1.setBinName("级联测试仓1");
        binDto1.setBinType("高大平房仓");
        binDto1.setCapacity(new BigDecimal("200.000"));
        binDto1.setStatus(1);
        Long binId1 = binService.create(binDto1);

        BinDTO binDto2 = new BinDTO();
        binDto2.setWarehouseId(whId);
        binDto2.setBinCode("TEST_BIN_CASCADE_2");
        binDto2.setBinName("级联测试仓2");
        binDto2.setBinType("浅圆仓");
        binDto2.setCapacity(new BigDecimal("200.000"));
        binDto2.setStatus(1);
        Long binId2 = binService.create(binDto2);

        // 在两个仓房下各创建货位
        PositionDTO posDto1 = new PositionDTO();
        posDto1.setBinId(binId1);
        posDto1.setWarehouseId(whId);
        posDto1.setPositionCode("TEST_POS_CASCADE_1A");
        posDto1.setPositionName("级联测试区1A");
        posDto1.setCapacity(new BigDecimal("50.000"));
        posDto1.setStatus(1);
        positionService.create(posDto1);

        PositionDTO posDto2 = new PositionDTO();
        posDto2.setBinId(binId2);
        posDto2.setWarehouseId(whId);
        posDto2.setPositionCode("TEST_POS_CASCADE_2A");
        posDto2.setPositionName("级联测试区2A");
        posDto2.setCapacity(new BigDecimal("50.000"));
        posDto2.setStatus(1);
        positionService.create(posDto2);

        // 测试级联查询：只指定库区ID
        List<PositionVO> listByWarehouse = positionService.listCascade(whId, null);
        assertEquals(2, listByWarehouse.size(), "该库区下应有2个货位");

        // 测试级联查询：指定库区和仓房ID
        List<PositionVO> listByBin = positionService.listCascade(whId, binId1);
        assertEquals(1, listByBin.size(), "指定仓房下应有1个货位");

        System.out.println("✅ 级联选择查询成功");
        System.out.println("   - 按库区查询货位数: " + listByWarehouse.size());
        System.out.println("   - 按库区+仓房查询货位数: " + listByBin.size());
    }

    @Test
    @Order(13)
    @DisplayName("集成测试-删除测试")
    void testDelete() {
        // 创建库区
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("TEST_WH_DELETE");
        whDto.setWarehouseName("删除测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("1000.000"));
        whDto.setStatus(1);
        Long whId = warehouseService.create(whDto);

        // 删除库区
        Boolean result = warehouseService.delete(whId);
        assertTrue(result, "删除应该成功");

        // 验证删除（逻辑删除）
        WarehouseVO vo = warehouseService.getById(whId);
        assertNull(vo, "删除后查询应为空");

        System.out.println("✅ 删除测试成功");
    }

    // ========================================
    // 异常场景测试
    // ========================================

    @Test
    @Order(14)
    @DisplayName("集成测试-创建库区编码重复异常")
    void testCreateWarehouseDuplicateCode() {
        WarehouseDTO dto1 = new WarehouseDTO();
        dto1.setWarehouseCode("TEST_WH_DUP");
        dto1.setWarehouseName("重复测试粮库1");
        dto1.setAddress("测试地址");
        dto1.setTotalCapacity(new BigDecimal("1000.000"));
        dto1.setStatus(1);
        warehouseService.create(dto1);

        // 尝试创建相同编码的库区
        WarehouseDTO dto2 = new WarehouseDTO();
        dto2.setWarehouseCode("TEST_WH_DUP");  // 相同编码
        dto2.setWarehouseName("重复测试粮库2");
        dto2.setAddress("测试地址2");
        dto2.setTotalCapacity(new BigDecimal("1000.000"));
        dto2.setStatus(1);

        assertThrows(RuntimeException.class, () -> warehouseService.create(dto2),
                     "编码重复应抛出异常");

        System.out.println("✅ 编码重复异常测试通过");
    }

    @Test
    @Order(15)
    @DisplayName("集成测试-创建仓房时库区不存在异常")
    void testCreateBinWarehouseNotFound() {
        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(99999L);  // 不存在的库区ID
        binDto.setBinCode("TEST_BIN_NO_WH");
        binDto.setBinName("测试仓");
        binDto.setBinType("高大平房仓");
        binDto.setCapacity(new BigDecimal("100.000"));
        binDto.setStatus(1);

        assertThrows(RuntimeException.class, () -> binService.create(binDto),
                     "库区不存在应抛出异常");

        System.out.println("✅ 库区不存在异常测试通过");
    }

    @Test
    @Order(16)
    @DisplayName("集成测试-创建货位时仓房不存在异常")
    void testCreatePositionBinNotFound() {
        PositionDTO posDto = new PositionDTO();
        posDto.setBinId(99999L);  // 不存在的仓房ID
        posDto.setWarehouseId(99999L);
        posDto.setPositionCode("TEST_POS_NO_BIN");
        posDto.setPositionName("测试货位");
        posDto.setCapacity(new BigDecimal("50.000"));
        posDto.setStatus(1);

        assertThrows(RuntimeException.class, () -> positionService.create(posDto),
                     "仓房不存在应抛出异常");

        System.out.println("✅ 仓房不存在异常测试通过");
    }
}