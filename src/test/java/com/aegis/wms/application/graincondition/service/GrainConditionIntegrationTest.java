package com.aegis.wms.application.graincondition.service;

import com.aegis.wms.application.graincondition.dto.AlarmHandleDTO;
import com.aegis.wms.application.graincondition.dto.AlarmThresholdDTO;
import com.aegis.wms.application.graincondition.dto.GrainConditionRecordDTO;
import com.aegis.wms.application.graincondition.service.impl.AlarmCheckServiceImpl;
import com.aegis.wms.application.graincondition.service.impl.AlarmRecordServiceImpl;
import com.aegis.wms.application.graincondition.service.impl.AlarmThresholdServiceImpl;
import com.aegis.wms.application.graincondition.service.impl.GrainConditionRecordServiceImpl;
import com.aegis.wms.application.graincondition.vo.AlarmRecordVO;
import com.aegis.wms.application.graincondition.vo.AlarmThresholdVO;
import com.aegis.wms.application.graincondition.vo.GrainConditionRecordVO;
import com.aegis.wms.application.graincondition.vo.GrainConditionTrendVO;
import com.aegis.wms.application.masterdata.dto.BinDTO;
import com.aegis.wms.application.masterdata.dto.WarehouseDTO;
import com.aegis.wms.application.masterdata.service.impl.BinServiceImpl;
import com.aegis.wms.application.masterdata.service.impl.WarehouseServiceImpl;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.graincondition.entity.GrainConditionRecord;
import com.aegis.wms.domain.graincondition.repository.GrainConditionRecordRepository;
import com.aegis.wms.TestConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 粮情检测领域集成测试
 * 注意：每个测试方法独立运行，initTestData()每次都会创建新的测试数据
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestConfig.class)
class GrainConditionIntegrationTest {

    @Autowired
    private GrainConditionRecordServiceImpl grainConditionRecordService;

    @Autowired
    private AlarmThresholdServiceImpl alarmThresholdService;

    @Autowired
    private AlarmRecordServiceImpl alarmRecordService;

    @Autowired
    private AlarmCheckServiceImpl alarmCheckService;

    @Autowired
    private WarehouseServiceImpl warehouseService;

    @Autowired
    private BinServiceImpl binService;

    @Autowired
    private GrainConditionRecordRepository recordRepository;

    // 每个测试方法内使用的临时变量
    private Long testWarehouseId;
    private Long testBinId;
    private Long testThresholdId;

    // ========================================
    // 报警阈值测试
    // ========================================

    @Test
    @Order(1)
    @DisplayName("报警阈值-创建和查询")
    void testCreateAndQueryThreshold() {
        AlarmThresholdDTO dto = new AlarmThresholdDTO();
        dto.setThresholdCode("TH_TEST_" + System.currentTimeMillis());
        dto.setThresholdName("测试阈值-CO2超限");
        dto.setMetricType("co2_concentration");
        dto.setOperator(">");
        dto.setThresholdValue(new BigDecimal("5.00"));
        dto.setAlarmLevel(2);
        dto.setStatus(1);

        Long id = alarmThresholdService.create(dto);
        assertNotNull(id);

        AlarmThresholdVO vo = alarmThresholdService.getById(id);
        assertNotNull(vo);
        assertEquals("co2_concentration", vo.getMetricType());
        assertEquals(0, new BigDecimal("5.00").compareTo(vo.getThresholdValue()));

        System.out.println("✅ 报警阈值创建查询成功: " + vo.getThresholdName());
    }

    @Test
    @Order(2)
    @DisplayName("报警阈值-查询所有启用的阈值")
    void testListAllEnabledThresholds() {
        // 先创建一个阈值确保有数据
        AlarmThresholdDTO dto = new AlarmThresholdDTO();
        dto.setThresholdCode("TH_LIST_TEST_" + System.currentTimeMillis());
        dto.setThresholdName("测试阈值-查询列表");
        dto.setMetricType("max_grain_temp");
        dto.setOperator(">");
        dto.setThresholdValue(new BigDecimal("30.00"));
        dto.setAlarmLevel(1);
        dto.setStatus(1);
        alarmThresholdService.create(dto);

        List<AlarmThresholdVO> list = alarmThresholdService.listAllEnabled();
        assertNotNull(list);
        assertTrue(list.size() >= 1);

        System.out.println("✅ 查询启用阈值成功，数量: " + list.size());
    }

    // ========================================
    // 粮情记录测试
    // ========================================

    @Test
    @Order(10)
    @DisplayName("粮情记录-创建和查询")
    void testCreateAndQueryRecord() {
        initTestData();

        GrainConditionRecordDTO dto = createTestRecordDTO();
        dto.setRemark("集成测试粮情记录");

        Long recordId = grainConditionRecordService.create(dto);
        assertNotNull(recordId);

        GrainConditionRecordVO vo = grainConditionRecordService.getById(recordId);
        assertNotNull(vo);
        assertEquals(0, new BigDecimal("30.50").compareTo(vo.getAvgGrainTemp()));
        assertEquals("无虫", vo.getInsectLevelName());

        System.out.println("✅ 粮情记录创建成功，记录编号: " + vo.getRecordNo());
    }

    @Test
    @Order(11)
    @DisplayName("粮情记录-分页查询")
    void testPageRecords() {
        initTestData();

        // 创建多条记录
        for (int i = 1; i <= 3; i++) {
            GrainConditionRecordDTO dto = createTestRecordDTO();
            dto.setRecordDate(LocalDate.now().minusDays(i));
            dto.setRemark("分页测试记录_" + i);
            grainConditionRecordService.create(dto);
        }

        Page<GrainConditionRecordVO> page = new Page<>(1, 10);
        PageResult<GrainConditionRecordVO> result = grainConditionRecordService.page(page, testWarehouseId, testBinId, null, null);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 3);

        System.out.println("✅ 粮情记录分页查询成功，总数: " + result.getTotal());
    }

    @Test
    @Order(12)
    @DisplayName("粮情记录-按日期范围查询")
    void testQueryByDateRange() {
        initTestData();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 创建今天的记录
        GrainConditionRecordDTO dto1 = createTestRecordDTO();
        dto1.setRecordDate(today);
        grainConditionRecordService.create(dto1);

        // 创建昨天的记录
        GrainConditionRecordDTO dto2 = createTestRecordDTO();
        dto2.setRecordDate(yesterday);
        grainConditionRecordService.create(dto2);

        // 查询今天的记录
        Page<GrainConditionRecordVO> page = new Page<>(1, 10);
        PageResult<GrainConditionRecordVO> result = grainConditionRecordService.page(page, testWarehouseId, testBinId, today, today);

        assertTrue(result.getTotal() >= 1);

        System.out.println("✅ 按日期范围查询成功，今日记录数: " + result.getTotal());
    }

    @Test
    @Order(13)
    @DisplayName("粮情记录-查询趋势数据")
    void testGetTrend() {
        initTestData();

        LocalDate today = LocalDate.now();

        // 创建多条连续记录
        for (int i = 0; i < 5; i++) {
            GrainConditionRecordDTO dto = createTestRecordDTO();
            dto.setRecordDate(today.minusDays(i));
            dto.setOuterTemp(new BigDecimal("20.00").add(new BigDecimal(i)));
            dto.setInnerTemp(new BigDecimal("22.00").add(new BigDecimal(i)));
            dto.setAvgGrainTemp(new BigDecimal("24.00").add(new BigDecimal(i)));
            grainConditionRecordService.create(dto);
        }

        List<GrainConditionTrendVO> trendList = grainConditionRecordService.getTrend(testBinId, today.minusDays(5), today);

        assertNotNull(trendList);
        assertTrue(trendList.size() >= 5);

        System.out.println("✅ 趋势数据查询成功，记录数: " + trendList.size());
    }

    @Test
    @Order(14)
    @DisplayName("粮情记录-查询最新记录")
    void testGetLatest() {
        initTestData();

        GrainConditionRecordDTO dto = createTestRecordDTO();
        dto.setRecordDate(LocalDate.now());
        dto.setRemark("最新记录测试");
        grainConditionRecordService.create(dto);

        GrainConditionRecordVO latest = grainConditionRecordService.getLatestByBinId(testBinId);

        assertNotNull(latest);

        System.out.println("✅ 查询最新粮情成功: " + latest.getRecordDate());
    }

    // ========================================
    // 报警检测测试
    // ========================================

    @Test
    @Order(20)
    @DisplayName("报警检测-温度超限触发报警")
    void testAlarmTriggered() {
        initTestData();

        // 创建一条高温记录（超过28℃）
        GrainConditionRecordDTO dto = new GrainConditionRecordDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setBinId(testBinId);
        dto.setRecordDate(LocalDate.now());
        dto.setMaxGrainTemp(new BigDecimal("30.00")); // 超过阈值28℃
        dto.setMinGrainTemp(new BigDecimal("25.00"));
        dto.setAvgGrainTemp(new BigDecimal("27.50"));
        dto.setOuterTemp(new BigDecimal("20.00"));
        dto.setInnerTemp(new BigDecimal("25.00"));
        dto.setOuterHumidity(new BigDecimal("60.00"));
        dto.setInnerHumidity(new BigDecimal("55.00"));
        dto.setInsectLevel("NONE");

        Long recordId = grainConditionRecordService.create(dto);

        // 手动触发报警检测（模拟Kafka消费者）
        GrainConditionRecord record = recordRepository.selectById(recordId);
        alarmCheckService.checkAndCreateAlarms(record);

        // 验证报警记录已创建
        List<AlarmRecordVO> alarms = alarmRecordService.listUnhandled(testWarehouseId);
        assertFalse(alarms.isEmpty());

        AlarmRecordVO alarm = alarms.stream()
                .filter(a -> a.getRecordId().equals(recordId))
                .findFirst()
                .orElse(null);

        assertNotNull(alarm);
        assertEquals("max_grain_temp", alarm.getMetricType());
        assertEquals(0, new BigDecimal("30.00").compareTo(alarm.getMetricValue()));

        System.out.println("✅ 报警触发成功: " + alarm.getAlarmContent());
    }

    @Test
    @Order(21)
    @DisplayName("报警检测-正常温度不触发报警")
    void testNoAlarmTriggered() {
        initTestData();

        // 创建一条正常温度记录
        GrainConditionRecordDTO dto = new GrainConditionRecordDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setBinId(testBinId);
        dto.setRecordDate(LocalDate.now());
        dto.setMaxGrainTemp(new BigDecimal("25.00")); // 未超过阈值
        dto.setMinGrainTemp(new BigDecimal("20.00"));
        dto.setAvgGrainTemp(new BigDecimal("22.00"));
        dto.setOuterTemp(new BigDecimal("18.00"));
        dto.setInnerTemp(new BigDecimal("22.00"));
        dto.setOuterHumidity(new BigDecimal("60.00"));
        dto.setInnerHumidity(new BigDecimal("55.00"));
        dto.setInsectLevel("NONE");

        Long recordId = grainConditionRecordService.create(dto);

        // 手动触发报警检测
        GrainConditionRecord record = recordRepository.selectById(recordId);
        alarmCheckService.checkAndCreateAlarms(record);

        // 验证没有该记录的报警
        List<AlarmRecordVO> alarms = alarmRecordService.listUnhandled(testWarehouseId);
        boolean hasAlarm = alarms.stream().anyMatch(a -> a.getRecordId().equals(recordId));
        assertFalse(hasAlarm);

        System.out.println("✅ 正常温度未触发报警");
    }

    // ========================================
    // 报警处理测试
    // ========================================

    @Test
    @Order(30)
    @DisplayName("报警处理-确认报警")
    void testHandleAlarm() {
        initTestData();

        // 创建高温记录触发报警
        GrainConditionRecordDTO dto = new GrainConditionRecordDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setBinId(testBinId);
        dto.setRecordDate(LocalDate.now());
        dto.setMaxGrainTemp(new BigDecimal("32.00"));
        dto.setMinGrainTemp(new BigDecimal("28.00"));
        dto.setAvgGrainTemp(new BigDecimal("30.00"));
        dto.setInsectLevel("NONE");

        Long recordId = grainConditionRecordService.create(dto);

        GrainConditionRecord record = recordRepository.selectById(recordId);
        alarmCheckService.checkAndCreateAlarms(record);

        // 获取报警记录
        List<AlarmRecordVO> alarms = alarmRecordService.listUnhandled(testWarehouseId);
        AlarmRecordVO alarm = alarms.stream()
                .filter(a -> a.getRecordId().equals(recordId))
                .findFirst()
                .orElse(null);

        assertNotNull(alarm);

        // 处理报警
        AlarmHandleDTO handleDto = new AlarmHandleDTO();
        handleDto.setAlarmId(alarm.getId());
        handleDto.setStatus(1); // 已确认
        handleDto.setHandleRemark("已确认，安排处理");

        Boolean result = alarmRecordService.handle(handleDto);
        assertTrue(result);

        // 验证状态已更新
        AlarmRecordVO updated = alarmRecordService.getById(alarm.getId());
        assertEquals(1, updated.getStatus());
        assertEquals("已确认", updated.getStatusName());

        System.out.println("✅ 报警处理成功");
    }

    @Test
    @Order(31)
    @DisplayName("报警处理-统计未处理数量")
    void testCountUnhandled() {
        initTestData();

        Long countBefore = alarmRecordService.countUnhandled(testWarehouseId);

        // 创建高温记录
        GrainConditionRecordDTO dto = new GrainConditionRecordDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setBinId(testBinId);
        dto.setRecordDate(LocalDate.now());
        dto.setMaxGrainTemp(new BigDecimal("35.00"));
        dto.setInsectLevel("NONE");

        Long recordId = grainConditionRecordService.create(dto);
        GrainConditionRecord record = recordRepository.selectById(recordId);
        alarmCheckService.checkAndCreateAlarms(record);

        Long countAfter = alarmRecordService.countUnhandled(testWarehouseId);
        assertTrue(countAfter > countBefore);

        System.out.println("✅ 未处理报警统计成功，数量: " + countAfter);
    }

    // ========================================
    // 辅助方法
    // ========================================

    /**
     * 初始化测试数据，每次调用都创建新数据
     */
    private void initTestData() {
        long timestamp = System.currentTimeMillis();

        WarehouseDTO whDto = new WarehouseDTO();
        whDto.setWarehouseCode("GC_TEST_WH_" + timestamp);
        whDto.setWarehouseName("粮情测试粮库");
        whDto.setAddress("测试地址");
        whDto.setTotalCapacity(new BigDecimal("10000.000"));
        whDto.setStatus(1);
        testWarehouseId = warehouseService.create(whDto);

        BinDTO binDto = new BinDTO();
        binDto.setWarehouseId(testWarehouseId);
        binDto.setBinCode("GC_TEST_BIN_" + timestamp);
        binDto.setBinName("粮情测试仓");
        binDto.setBinType("高大平房仓");
        binDto.setCapacity(new BigDecimal("1000.000"));
        binDto.setStatus(1);
        testBinId = binService.create(binDto);

        AlarmThresholdDTO thresholdDto = new AlarmThresholdDTO();
        thresholdDto.setThresholdCode("GC_TEST_TH_" + timestamp);
        thresholdDto.setThresholdName("测试-最高粮温超28度");
        thresholdDto.setMetricType("max_grain_temp");
        thresholdDto.setOperator(">");
        thresholdDto.setThresholdValue(new BigDecimal("28.00"));
        thresholdDto.setAlarmLevel(2);
        thresholdDto.setStatus(1);
        testThresholdId = alarmThresholdService.create(thresholdDto);
    }

    private GrainConditionRecordDTO createTestRecordDTO() {
        GrainConditionRecordDTO dto = new GrainConditionRecordDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setBinId(testBinId);
        dto.setRecordDate(LocalDate.now());
        dto.setOuterTemp(new BigDecimal("20.00"));
        dto.setOuterHumidity(new BigDecimal("60.00"));
        dto.setInnerTemp(new BigDecimal("25.00"));
        dto.setInnerHumidity(new BigDecimal("55.00"));
        dto.setMaxGrainTemp(new BigDecimal("32.00"));
        dto.setMinGrainTemp(new BigDecimal("28.00"));
        dto.setAvgGrainTemp(new BigDecimal("30.50"));
        dto.setInsectLevel("NONE");
        dto.setO2Concentration(new BigDecimal("20.50"));
        dto.setCo2Concentration(new BigDecimal("0.50"));
        dto.setMoistureContent(new BigDecimal("14.00"));
        return dto;
    }
}