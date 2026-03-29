package com.aegis.wms.application.inventory.service;

import com.aegis.wms.application.inventory.dto.GrainVarietyDTO;
import com.aegis.wms.application.inventory.dto.InboundOrderDTO;
import com.aegis.wms.application.inventory.dto.OutboundOrderDTO;
import com.aegis.wms.application.inventory.service.impl.GrainVarietyServiceImpl;
import com.aegis.wms.application.inventory.service.impl.InboundOrderServiceImpl;
import com.aegis.wms.application.inventory.service.impl.OutboundOrderServiceImpl;
import com.aegis.wms.application.inventory.service.impl.StockServiceImpl;
import com.aegis.wms.application.inventory.service.impl.StockMovementServiceImpl;
import com.aegis.wms.application.inventory.vo.GrainVarietyVO;
import com.aegis.wms.application.inventory.vo.InboundOrderVO;
import com.aegis.wms.application.inventory.vo.OutboundOrderVO;
import com.aegis.wms.application.inventory.vo.StockVO;
import com.aegis.wms.application.inventory.vo.StockMovementVO;
import com.aegis.wms.application.masterdata.dto.WarehouseDTO;
import com.aegis.wms.application.masterdata.dto.BinDTO;
import com.aegis.wms.application.masterdata.dto.PositionDTO;
import com.aegis.wms.application.masterdata.service.impl.WarehouseServiceImpl;
import com.aegis.wms.application.masterdata.service.impl.BinServiceImpl;
import com.aegis.wms.application.masterdata.service.impl.PositionServiceImpl;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 出入库库存领域集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryIntegrationTest {

    @Autowired
    private GrainVarietyServiceImpl grainVarietyService;

    @Autowired
    private StockServiceImpl stockService;

    @Autowired
    private InboundOrderServiceImpl inboundOrderService;

    @Autowired
    private OutboundOrderServiceImpl outboundOrderService;

    @Autowired
    private StockMovementServiceImpl stockMovementService;

    @Autowired
    private WarehouseServiceImpl warehouseService;

    @Autowired
    private BinServiceImpl binService;

    @Autowired
    private PositionServiceImpl positionService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static Long testWarehouseId;
    private static Long testBinId;
    private static Long testPositionId;
    private static Long testGrainVarietyId;

    // ========================================
    // 初始化测试数据
    // ========================================

    @Test
    @Order(1)
    @DisplayName("初始化测试数据-库区/仓房/货位/粮食品种")
    void testInitData() {
        // 创建库区
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("INV_TEST_WH");
        whDto.setWarehouseName("库存测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        testWarehouseId = warehouseService.create(whDto);
        assertNotNull(testWarehouseId);

        // 创建仓房
        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(testWarehouseId);
        binDto.setBinCode("INV_TEST_BIN");
        binDto.setBinName("库存测试仓");
        binDto.setBinType("高大平房仓");
        binDto.setCapacity(new BigDecimal("1000.000"));
        binDto.setStatus(1);
        testBinId = binService.create(binDto);
        assertNotNull(testBinId);

        // 创建货位
        PositionDTO posDto = new PositionDTO();
        posDto.setBinId(testBinId);
        posDto.setWarehouseId(testWarehouseId);
        posDto.setPositionCode("INV_TEST_POS");
        posDto.setPositionName("库存测试货位");
        posDto.setCapacity(new BigDecimal("100.000"));
        posDto.setStatus(1);
        testPositionId = positionService.create(posDto);
        assertNotNull(testPositionId);

        // 创建粮食品种
        GrainVarietyDTO varietyDto = new GrainVarietyDTO();
        varietyDto.setVarietyCode("INV_TEST_GV");
        varietyDto.setVarietyName("库存测试小麦");
        varietyDto.setSortOrder(1);
        varietyDto.setStatus(1);
        testGrainVarietyId = grainVarietyService.create(varietyDto);
        assertNotNull(testGrainVarietyId);

        System.out.println("✅ 初始化测试数据成功");
        System.out.println("   库区ID: " + testWarehouseId);
        System.out.println("   仓房ID: " + testBinId);
        System.out.println("   货位ID: " + testPositionId);
        System.out.println("   粮食品种ID: " + testGrainVarietyId);
    }

    // ========================================
    // 粮食品种测试
    // ========================================

    @Test
    @Order(10)
    @DisplayName("粮食品种-查询列表")
    void testListGrainVarieties() {
        // 先创建一个品种
        GrainVarietyDTO dto = new GrainVarietyDTO();
        dto.setVarietyCode("GV_LIST_TEST");
        dto.setVarietyName("列表测试小麦");
        dto.setSortOrder(1);
        dto.setStatus(1);
        grainVarietyService.create(dto);

        List<GrainVarietyVO> list = grainVarietyService.listAll();

        assertNotNull(list);
        assertTrue(list.size() >= 1);

        System.out.println("✅ 粮食品种列表查询成功，数量: " + list.size());
    }

    // ========================================
    // 入库测试
    // ========================================

    @Test
    @Order(20)
    @DisplayName("入库-创建入库单并增加库存")
    void testCreateInboundOrder() {
        // 初始化数据
        initTestData();

        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setBinId(testBinId);
        dto.setPositionId(testPositionId);
        dto.setGrainVarietyId(testGrainVarietyId);
        dto.setHarvestYear(2025);
        dto.setGrade("一等");
        dto.setQuantity(new BigDecimal("50.500"));
        dto.setRemark("集成测试入库");
        dto.setIdempotentKey(UUID.randomUUID().toString());

        Long orderId = inboundOrderService.create(dto);

        assertNotNull(orderId);

        // 验证库存已增加
        StockVO stock = stockService.getByPositionId(testPositionId);
        assertNotNull(stock);
        assertEquals(0, new BigDecimal("50.500").compareTo(stock.getQuantity()));

        // 验证入库单可查询
        InboundOrderVO order = inboundOrderService.getById(orderId);
        assertNotNull(order);
        assertEquals("一等", order.getGrade());
        assertEquals(0, new BigDecimal("50.500").compareTo(order.getQuantity()));

        System.out.println("✅ 入库成功，单号: " + order.getOrderNo() + ", 入库后库存: " + stock.getQuantity());
    }

    @Test
    @Order(21)
    @DisplayName("入库-多次入库累计库存")
    void testMultipleInbound() {
        initTestData();

        String idempotentKey1 = UUID.randomUUID().toString();
        String idempotentKey2 = UUID.randomUUID().toString();

        // 第一次入库
        InboundOrderDTO dto1 = new InboundOrderDTO();
        dto1.setWarehouseId(testWarehouseId);
        dto1.setBinId(testBinId);
        dto1.setPositionId(testPositionId);
        dto1.setGrainVarietyId(testGrainVarietyId);
        dto1.setHarvestYear(2025);
        dto1.setGrade("一等");
        dto1.setQuantity(new BigDecimal("30.000"));
        dto1.setIdempotentKey(idempotentKey1);
        inboundOrderService.create(dto1);

        // 第二次入库
        InboundOrderDTO dto2 = new InboundOrderDTO();
        dto2.setWarehouseId(testWarehouseId);
        dto2.setBinId(testBinId);
        dto2.setPositionId(testPositionId);
        dto2.setGrainVarietyId(testGrainVarietyId);
        dto2.setHarvestYear(2025);
        dto2.setGrade("一等");
        dto2.setQuantity(new BigDecimal("20.000"));
        dto2.setIdempotentKey(idempotentKey2);
        inboundOrderService.create(dto2);

        // 验证累计库存
        StockVO stock = stockService.getByPositionId(testPositionId);
        assertEquals(0, new BigDecimal("50.000").compareTo(stock.getQuantity()));

        System.out.println("✅ 多次入库成功，累计库存: " + stock.getQuantity());
    }

    @Test
    @Order(22)
    @DisplayName("入库-幂等性测试(重复提交)")
    void testInboundIdempotent() {
        initTestData();

        String idempotentKey = UUID.randomUUID().toString();

        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setBinId(testBinId);
        dto.setPositionId(testPositionId);
        dto.setGrainVarietyId(testGrainVarietyId);
        dto.setHarvestYear(2025);
        dto.setGrade("一等");
        dto.setQuantity(new BigDecimal("100.000"));
        dto.setIdempotentKey(idempotentKey);

        // 第一次入库
        Long orderId1 = inboundOrderService.create(dto);

        // 使用相同幂等键再次入库
        Long orderId2 = inboundOrderService.create(dto);

        // 应该返回相同的结果，库存不会重复增加
        assertEquals(orderId1, orderId2);

        StockVO stock = stockService.getByPositionId(testPositionId);
        assertEquals(0, new BigDecimal("100.000").compareTo(stock.getQuantity()));

        System.out.println("✅ 幂等性测试通过，库存未重复增加: " + stock.getQuantity());
    }

    // ========================================
    // 出库测试
    // ========================================

    @Test
    @Order(30)
    @DisplayName("出库-创建出库单并减少库存")
    void testCreateOutboundOrder() {
        initTestData();

        // 先入库
        InboundOrderDTO inboundDto = new InboundOrderDTO();
        inboundDto.setWarehouseId(testWarehouseId);
        inboundDto.setBinId(testBinId);
        inboundDto.setPositionId(testPositionId);
        inboundDto.setGrainVarietyId(testGrainVarietyId);
        inboundDto.setHarvestYear(2025);
        inboundDto.setGrade("一等");
        inboundDto.setQuantity(new BigDecimal("100.000"));
        inboundDto.setIdempotentKey(UUID.randomUUID().toString());
        inboundOrderService.create(inboundDto);

        // 出库
        OutboundOrderDTO outboundDto = new OutboundOrderDTO();
        outboundDto.setWarehouseId(testWarehouseId);
        outboundDto.setBinId(testBinId);
        outboundDto.setPositionId(testPositionId);
        outboundDto.setQuantity(new BigDecimal("30.000"));
        outboundDto.setRemark("集成测试出库");
        outboundDto.setIdempotentKey(UUID.randomUUID().toString());

        Long orderId = outboundOrderService.create(outboundDto);

        assertNotNull(orderId);

        // 验证库存已减少
        StockVO stock = stockService.getByPositionId(testPositionId);
        assertEquals(0, new BigDecimal("70.000").compareTo(stock.getQuantity()));

        System.out.println("✅ 出库成功，出库后库存: " + stock.getQuantity());
    }

    @Test
    @Order(31)
    @DisplayName("出库-库存不足异常")
    void testOutboundInsufficientStock() {
        initTestData();

        // 先入库少量
        InboundOrderDTO inboundDto = new InboundOrderDTO();
        inboundDto.setWarehouseId(testWarehouseId);
        inboundDto.setBinId(testBinId);
        inboundDto.setPositionId(testPositionId);
        inboundDto.setGrainVarietyId(testGrainVarietyId);
        inboundDto.setHarvestYear(2025);
        inboundDto.setGrade("一等");
        inboundDto.setQuantity(new BigDecimal("10.000"));
        inboundDto.setIdempotentKey(UUID.randomUUID().toString());
        inboundOrderService.create(inboundDto);

        // 尝试出库更多
        OutboundOrderDTO outboundDto = new OutboundOrderDTO();
        outboundDto.setWarehouseId(testWarehouseId);
        outboundDto.setBinId(testBinId);
        outboundDto.setPositionId(testPositionId);
        outboundDto.setQuantity(new BigDecimal("50.000"));
        outboundDto.setIdempotentKey(UUID.randomUUID().toString());

        assertThrows(RuntimeException.class, () -> outboundOrderService.create(outboundDto));

        System.out.println("✅ 库存不足异常测试通过");
    }

    // ========================================
    // 库存流水测试
    // ========================================

    @Test
    @Order(40)
    @DisplayName("库存流水-查询变动记录")
    void testStockMovements() {
        initTestData();

        // 入库
        InboundOrderDTO inboundDto = new InboundOrderDTO();
        inboundDto.setWarehouseId(testWarehouseId);
        inboundDto.setBinId(testBinId);
        inboundDto.setPositionId(testPositionId);
        inboundDto.setGrainVarietyId(testGrainVarietyId);
        inboundDto.setHarvestYear(2025);
        inboundDto.setGrade("一等");
        inboundDto.setQuantity(new BigDecimal("100.000"));
        inboundDto.setIdempotentKey(UUID.randomUUID().toString());
        inboundOrderService.create(inboundDto);

        // 出库
        OutboundOrderDTO outboundDto = new OutboundOrderDTO();
        outboundDto.setWarehouseId(testWarehouseId);
        outboundDto.setBinId(testBinId);
        outboundDto.setPositionId(testPositionId);
        outboundDto.setQuantity(new BigDecimal("30.000"));
        outboundDto.setIdempotentKey(UUID.randomUUID().toString());
        outboundOrderService.create(outboundDto);

        // 查询流水
        List<StockMovementVO> movements = stockMovementService.listByPositionId(testPositionId);

        assertEquals(2, movements.size());

        // 验证入库流水
        StockMovementVO inMovement = movements.stream()
                .filter(m -> m.getMovementType() == 1)
                .findFirst().orElse(null);
        assertNotNull(inMovement);
        assertEquals(0, new BigDecimal("100.000").compareTo(inMovement.getQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(inMovement.getQuantityBefore()));
        assertEquals(0, new BigDecimal("100.000").compareTo(inMovement.getQuantityAfter()));

        // 验证出库流水
        StockMovementVO outMovement = movements.stream()
                .filter(m -> m.getMovementType() == 2)
                .findFirst().orElse(null);
        assertNotNull(outMovement);
        assertEquals(0, new BigDecimal("30.000").compareTo(outMovement.getQuantity()));
        assertEquals(0, new BigDecimal("100.000").compareTo(outMovement.getQuantityBefore()));
        assertEquals(0, new BigDecimal("70.000").compareTo(outMovement.getQuantityAfter()));

        System.out.println("✅ 库存流水查询成功，记录数: " + movements.size());
    }

    // ========================================
    // 库存汇总测试
    // ========================================

    @Test
    @Order(50)
    @DisplayName("库存汇总-库区/仓房汇总")
    void testStockSummary() {
        initTestData();

        // 入库
        InboundOrderDTO inboundDto = new InboundOrderDTO();
        inboundDto.setWarehouseId(testWarehouseId);
        inboundDto.setBinId(testBinId);
        inboundDto.setPositionId(testPositionId);
        inboundDto.setGrainVarietyId(testGrainVarietyId);
        inboundDto.setHarvestYear(2025);
        inboundDto.setGrade("一等");
        inboundDto.setQuantity(new BigDecimal("80.000"));
        inboundDto.setIdempotentKey(UUID.randomUUID().toString());
        inboundOrderService.create(inboundDto);

        // 库区汇总
        BigDecimal warehouseSum = stockService.sumByWarehouseId(testWarehouseId);
        assertEquals(0, new BigDecimal("80.000").compareTo(warehouseSum));

        // 仓房汇总
        BigDecimal binSum = stockService.sumByBinId(testBinId);
        assertEquals(0, new BigDecimal("80.000").compareTo(binSum));

        System.out.println("✅ 库存汇总测试成功");
        System.out.println("   库区汇总: " + warehouseSum);
        System.out.println("   仓房汇总: " + binSum);
    }

    @Test
    @Order(51)
    @DisplayName("库存分页查询")
    void testStockPage() {
        initTestData();

        // 入库
        InboundOrderDTO inboundDto = new InboundOrderDTO();
        inboundDto.setWarehouseId(testWarehouseId);
        inboundDto.setBinId(testBinId);
        inboundDto.setPositionId(testPositionId);
        inboundDto.setGrainVarietyId(testGrainVarietyId);
        inboundDto.setHarvestYear(2025);
        inboundDto.setGrade("一等");
        inboundDto.setQuantity(new BigDecimal("50.000"));
        inboundDto.setIdempotentKey(UUID.randomUUID().toString());
        inboundOrderService.create(inboundDto);

        Page<StockVO> page = new Page<>(1, 10);
        PageResult<StockVO> result = stockService.page(page, testWarehouseId, testBinId);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 1);

        System.out.println("✅ 库存分页查询成功，总数: " + result.getTotal());
    }

    // ========================================
    // 辅助方法
    // ========================================

    private void initTestData() {
        // 每次都重新创建，因为事务会回滚
        // 创建库区
        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("INV_TEST_WH_" + System.currentTimeMillis());
        whDto.setWarehouseName("库存测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        testWarehouseId = warehouseService.create(whDto);

        // 创建仓房
        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(testWarehouseId);
        binDto.setBinCode("INV_TEST_BIN_" + System.currentTimeMillis());
        binDto.setBinName("库存测试仓");
        binDto.setBinType("高大平房仓");
        binDto.setCapacity(new BigDecimal("1000.000"));
        binDto.setStatus(1);
        testBinId = binService.create(binDto);

        // 创建货位
        PositionDTO posDto = new PositionDTO();
        posDto.setBinId(testBinId);
        posDto.setWarehouseId(testWarehouseId);
        posDto.setPositionCode("INV_TEST_POS_" + System.currentTimeMillis());
        posDto.setPositionName("库存测试货位");
        posDto.setCapacity(new BigDecimal("100.000"));
        posDto.setStatus(1);
        testPositionId = positionService.create(posDto);

        // 创建粮食品种
        GrainVarietyDTO varietyDto = new GrainVarietyDTO();
        varietyDto.setVarietyCode("INV_TEST_GV_" + System.currentTimeMillis());
        varietyDto.setVarietyName("库存测试小麦");
        varietyDto.setSortOrder(1);
        varietyDto.setStatus(1);
        testGrainVarietyId = grainVarietyService.create(varietyDto);
    }
}