-- ============================================================
-- Aegis-WMS (神盾仓储) 智能仓储核心业务系统
-- 数据库初始化脚本 V1.0
-- 创建日期: 2026-03-28
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、基础主数据领域 (Master Data)
-- ============================================================

-- --------------------------------------------
-- 库区表
-- --------------------------------------------
DROP TABLE IF EXISTS warehouse;
CREATE TABLE warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库区ID',
    warehouse_code VARCHAR(32) NOT NULL COMMENT '库区编码',
    warehouse_name VARCHAR(100) NOT NULL COMMENT '粮库名称',
    address VARCHAR(255) COMMENT '详细地址',
    total_capacity DECIMAL(12,3) COMMENT '总设计容量(吨)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-正常 1-已删除',
    UNIQUE KEY uk_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库区表';

-- --------------------------------------------
-- 仓房表
-- --------------------------------------------
DROP TABLE IF EXISTS bin;
CREATE TABLE bin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仓房ID',
    warehouse_id BIGINT NOT NULL COMMENT '归属库区ID',
    bin_code VARCHAR(32) NOT NULL COMMENT '仓房编号(如01仓)',
    bin_name VARCHAR(100) COMMENT '仓房名称',
    bin_type VARCHAR(50) COMMENT '仓型: 高大平房仓/浅圆仓/立筒仓等',
    capacity DECIMAL(12,3) COMMENT '仓容(吨)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_warehouse_id (warehouse_id),
    UNIQUE KEY uk_bin_code (warehouse_id, bin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓房表';

-- --------------------------------------------
-- 货位表 - 库存管理的最小粒度单元
-- --------------------------------------------
DROP TABLE IF EXISTS position;
CREATE TABLE position (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '货位ID',
    bin_id BIGINT NOT NULL COMMENT '归属仓房ID',
    warehouse_id BIGINT NOT NULL COMMENT '归属库区ID(冗余,便于查询)',
    position_code VARCHAR(32) NOT NULL COMMENT '货位编号(如A区/B区)',
    position_name VARCHAR(100) COMMENT '货位名称',
    capacity DECIMAL(12,3) COMMENT '货位容量(吨)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_bin_id (bin_id),
    INDEX idx_warehouse_id (warehouse_id),
    UNIQUE KEY uk_position_code (bin_id, position_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货位表';

-- ============================================================
-- 二、出入库库存领域 (Inbound/Outbound)
-- ============================================================

-- --------------------------------------------
-- 粮食品种字典表
-- --------------------------------------------
DROP TABLE IF EXISTS grain_variety;
CREATE TABLE grain_variety (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '品种ID',
    variety_code VARCHAR(32) NOT NULL COMMENT '品种编码',
    variety_name VARCHAR(50) NOT NULL COMMENT '品种名称(小麦/玉米/稻谷等)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_variety_code (variety_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮食品种字典表';

-- --------------------------------------------
-- 库存表(货位级) - 支持高并发分段锁
-- --------------------------------------------
DROP TABLE IF EXISTS stock;
CREATE TABLE stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    position_id BIGINT NOT NULL COMMENT '货位ID',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID(冗余)',
    bin_id BIGINT NOT NULL COMMENT '仓房ID(冗余)',
    grain_variety_id BIGINT COMMENT '粮食品种ID',
    harvest_year INT COMMENT '收获年份',
    grade VARCHAR(20) COMMENT '粮食等级(一等/二等/三等)',
    quantity DECIMAL(12,3) DEFAULT 0 COMMENT '当前库存量(吨)',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_position_stock (position_id),
    INDEX idx_warehouse_bin (warehouse_id, bin_id),
    INDEX idx_grain_variety (grain_variety_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表(货位级)';

-- --------------------------------------------
-- 库存变动流水表 - 强一致性审计日志
-- --------------------------------------------
DROP TABLE IF EXISTS stock_movement;
CREATE TABLE stock_movement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
    movement_no VARCHAR(32) NOT NULL COMMENT '流水单号(唯一)',
    position_id BIGINT NOT NULL COMMENT '货位ID',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    bin_id BIGINT NOT NULL COMMENT '仓房ID',
    movement_type TINYINT NOT NULL COMMENT '变动类型: 1-入库 2-出库',
    grain_variety_id BIGINT COMMENT '粮食品种ID',
    harvest_year INT COMMENT '收获年份',
    grade VARCHAR(20) COMMENT '粮食等级',
    quantity DECIMAL(12,3) NOT NULL COMMENT '变动数量(吨)',
    quantity_before DECIMAL(12,3) COMMENT '变动前库存',
    quantity_after DECIMAL(12,3) COMMENT '变动后库存',
    order_type VARCHAR(20) COMMENT '关联单据类型',
    order_id BIGINT COMMENT '关联单据ID',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    idempotent_key VARCHAR(64) COMMENT '幂等键',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_movement_no (movement_no),
    UNIQUE KEY uk_idempotent_key (idempotent_key),
    INDEX idx_position_time (position_id, create_time),
    INDEX idx_warehouse_time (warehouse_id, create_time),
    INDEX idx_order (order_type, order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变动流水表';

-- --------------------------------------------
-- 入库单
-- --------------------------------------------
DROP TABLE IF EXISTS inbound_order;
CREATE TABLE inbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '入库单号',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    bin_id BIGINT NOT NULL COMMENT '仓房ID',
    position_id BIGINT NOT NULL COMMENT '货位ID',
    grain_variety_id BIGINT NOT NULL COMMENT '粮食品种ID',
    harvest_year INT COMMENT '收获年份',
    grade VARCHAR(20) COMMENT '粮食等级',
    quantity DECIMAL(12,3) NOT NULL COMMENT '入库数量(吨)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已取消 1-待审核 2-已完成',
    remark VARCHAR(500) COMMENT '备注',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_warehouse (warehouse_id),
    INDEX idx_bin (bin_id),
    INDEX idx_position (position_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单';

-- --------------------------------------------
-- 出库单
-- --------------------------------------------
DROP TABLE IF EXISTS outbound_order;
CREATE TABLE outbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '出库单号',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    bin_id BIGINT NOT NULL COMMENT '仓房ID',
    position_id BIGINT NOT NULL COMMENT '货位ID',
    grain_variety_id BIGINT COMMENT '粮食品种ID',
    harvest_year INT COMMENT '收获年份',
    grade VARCHAR(20) COMMENT '粮食等级',
    quantity DECIMAL(12,3) NOT NULL COMMENT '出库数量(吨)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已取消 1-待审核 2-已完成',
    remark VARCHAR(500) COMMENT '备注',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_warehouse (warehouse_id),
    INDEX idx_bin (bin_id),
    INDEX idx_position (position_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单';

-- ============================================================
-- 三、粮情检测领域 (Grain Condition)
-- ============================================================

-- --------------------------------------------
-- 粮情记录表
-- --------------------------------------------
DROP TABLE IF EXISTS grain_condition_record;
CREATE TABLE grain_condition_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '粮情记录ID',
    record_no VARCHAR(32) NOT NULL COMMENT '记录编号',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    bin_id BIGINT NOT NULL COMMENT '仓房ID',
    record_date DATE NOT NULL COMMENT '查仓日期',

    -- 环境温湿度
    outer_temp DECIMAL(5,2) COMMENT '仓外温度(℃)',
    outer_humidity DECIMAL(5,2) COMMENT '仓外湿度(%)',
    inner_temp DECIMAL(5,2) COMMENT '仓内温度(℃)',
    inner_humidity DECIMAL(5,2) COMMENT '仓内湿度(%)',

    -- 粮温信息
    max_grain_temp DECIMAL(5,2) COMMENT '最高粮温(℃)',
    min_grain_temp DECIMAL(5,2) COMMENT '最低粮温(℃)',
    avg_grain_temp DECIMAL(5,2) COMMENT '平均粮温(℃)',

    -- 虫情气体
    insect_level VARCHAR(20) COMMENT '虫粮等级(无虫/基本无虫/一般虫粮/严重虫粮)',
    insect_desc VARCHAR(500) COMMENT '虫害描述',
    o2_concentration DECIMAL(5,2) COMMENT 'O2浓度(%)',
    co2_concentration DECIMAL(5,2) COMMENT 'CO2浓度(%)',

    -- 其他信息
    moisture_content DECIMAL(5,2) COMMENT '粮食水分(%)',
    remark VARCHAR(500) COMMENT '异常备注说明',
    data_source VARCHAR(20) DEFAULT 'manual' COMMENT '数据来源: manual-手工录入 iot-传感器采集',

    -- 操作信息
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_record_no (record_no),
    INDEX idx_bin_date (bin_id, record_date),
    INDEX idx_warehouse_date (warehouse_id, record_date),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮情记录表';

-- --------------------------------------------
-- 粮情附件表 - 存储现场照片
-- --------------------------------------------
DROP TABLE IF EXISTS grain_condition_attachment;
CREATE TABLE grain_condition_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    record_id BIGINT NOT NULL COMMENT '粮情记录ID',
    file_name VARCHAR(200) COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小(字节)',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_record_id (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮情附件表';

-- --------------------------------------------
-- 报警阈值配置表
-- --------------------------------------------
DROP TABLE IF EXISTS alarm_threshold;
CREATE TABLE alarm_threshold (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '阈值ID',
    threshold_code VARCHAR(50) NOT NULL COMMENT '阈值编码',
    threshold_name VARCHAR(100) NOT NULL COMMENT '阈值名称',
    metric_type VARCHAR(50) NOT NULL COMMENT '指标类型: max_grain_temp/min_grain_temp/avg_grain_temp/co2/o2等',
    operator VARCHAR(10) NOT NULL COMMENT '比较运算符: >/</>=/<=',
    threshold_value DECIMAL(10,2) NOT NULL COMMENT '阈值',
    alarm_level TINYINT DEFAULT 1 COMMENT '报警级别: 1-一般 2-严重 3-紧急',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    remark VARCHAR(200) COMMENT '备注说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_threshold_code (threshold_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警阈值配置表';

-- --------------------------------------------
-- 报警记录表
-- --------------------------------------------
DROP TABLE IF EXISTS alarm_record;
CREATE TABLE alarm_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报警ID',
    alarm_no VARCHAR(32) NOT NULL COMMENT '报警编号',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    bin_id BIGINT NOT NULL COMMENT '仓房ID',
    record_id BIGINT COMMENT '关联粮情记录ID',
    threshold_id BIGINT COMMENT '触发阈值ID',
    metric_type VARCHAR(50) COMMENT '触发指标类型',
    metric_value DECIMAL(10,2) COMMENT '实际值',
    threshold_value DECIMAL(10,2) COMMENT '阈值',
    alarm_level TINYINT COMMENT '报警级别',
    alarm_content VARCHAR(500) COMMENT '报警内容描述',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未处理 1-已确认 2-已处理',
    handler_id BIGINT COMMENT '处理人ID',
    handler_name VARCHAR(50) COMMENT '处理人姓名',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_alarm_no (alarm_no),
    INDEX idx_bin_time (bin_id, create_time),
    INDEX idx_warehouse_time (warehouse_id, create_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警记录表';

-- ============================================================
-- 四、仓储作业领域 (Warehouse Operation)
-- ============================================================

-- --------------------------------------------
-- 作业方案表 - 可复用的作业模板
-- --------------------------------------------
DROP TABLE IF EXISTS warehouse_operation_scheme;
CREATE TABLE warehouse_operation_scheme (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '方案ID',
    scheme_code VARCHAR(32) NOT NULL COMMENT '方案编码',
    scheme_name VARCHAR(100) NOT NULL COMMENT '方案名称',
    operation_type VARCHAR(20) NOT NULL COMMENT '作业类型: ventilation-通风/aeration-气调/temperature-控温/fumigation-熏蒸',

    -- 方案参数配置(JSON格式,不同作业类型差异化参数)
    config_params JSON COMMENT '方案配置参数: 通风-风机功率/风道类型; 气调-目标气体浓度等',

    -- 阈值配置
    temp_threshold DECIMAL(5,2) COMMENT '温度阈值(℃)',
    humidity_threshold DECIMAL(5,2) COMMENT '湿度阈值(%)',
    duration_max INT COMMENT '最长作业时长(分钟)',

    description VARCHAR(500) COMMENT '方案说明',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',

    create_by BIGINT COMMENT '创建人ID',
    create_by_name VARCHAR(50) COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',

    UNIQUE KEY uk_scheme_code (scheme_code),
    INDEX idx_operation_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业方案表';

-- --------------------------------------------
-- 作业记录表 - 作业主记录,状态机管控
-- 状态流转: 待执行(0) -> 作业中(1) -> 已完成(2) / 已取消(3)
-- --------------------------------------------
DROP TABLE IF EXISTS warehouse_operation_record;
CREATE TABLE warehouse_operation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    record_no VARCHAR(32) NOT NULL COMMENT '记录编号',
    scheme_id BIGINT COMMENT '关联方案ID(可为空,表示临时作业)',

    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    bin_id BIGINT NOT NULL COMMENT '仓房ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '作业类型: ventilation-通风/aeration-气调/temperature-控温/fumigation-熏蒸',

    -- 状态机
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待执行 1-作业中 2-已完成 3-已取消',

    -- 作业汇总(由明细聚合或Kafka计算)
    start_time DATETIME COMMENT '首次开始时间',
    end_time DATETIME COMMENT '最后结束时间',
    total_duration_minutes INT COMMENT '累计作业时长(分钟)',
    total_power DECIMAL(12,2) COMMENT '累计耗电量(度)',
    detail_count INT DEFAULT 0 COMMENT '启停次数',

    -- 开始状态
    start_avg_temp DECIMAL(5,2) COMMENT '起始平均粮温(℃)',
    start_moisture DECIMAL(5,2) COMMENT '起始水分(%)',

    -- 结束状态
    end_avg_temp DECIMAL(5,2) COMMENT '结束平均粮温(℃)',
    end_moisture DECIMAL(5,2) COMMENT '结束水分(%)',

    -- 效果指标
    temp_drop DECIMAL(5,2) COMMENT '温度变化(℃)',
    moisture_drop DECIMAL(5,2) COMMENT '水分变化(%)',

    -- 操作信息
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_record_no (record_no),
    INDEX idx_scheme_id (scheme_id),
    INDEX idx_warehouse_bin (warehouse_id, bin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业记录表';

-- --------------------------------------------
-- 作业明细表 - 记录每次启停详情
-- 说明: 一个作业可多次启停,如通风作业可能分多天执行
-- --------------------------------------------
DROP TABLE IF EXISTS warehouse_operation_detail;
CREATE TABLE warehouse_operation_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    record_id BIGINT NOT NULL COMMENT '作业记录ID',
    seq_no INT NOT NULL COMMENT '序号(第几次启停)',

    -- 启停时间
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_minutes INT COMMENT '本次时长(分钟)',

    -- 电表读数
    start_meter_reading DECIMAL(12,2) COMMENT '起始电表读数(度)',
    end_meter_reading DECIMAL(12,2) COMMENT '结束电表读数(度)',
    power_consumption DECIMAL(12,2) COMMENT '本次耗电量(度)',

    -- 开始状态
    start_avg_temp DECIMAL(5,2) COMMENT '开始平均粮温(℃)',
    start_moisture DECIMAL(5,2) COMMENT '开始水分(%)',
    start_inner_temp DECIMAL(5,2) COMMENT '开始仓内温度(℃)',
    start_inner_humidity DECIMAL(5,2) COMMENT '开始仓内湿度(%)',
    start_outer_temp DECIMAL(5,2) COMMENT '开始仓外温度(℃)',
    start_outer_humidity DECIMAL(5,2) COMMENT '开始仓外湿度(%)',

    -- 结束状态
    end_avg_temp DECIMAL(5,2) COMMENT '结束平均粮温(℃)',
    end_moisture DECIMAL(5,2) COMMENT '结束水分(%)',
    end_inner_temp DECIMAL(5,2) COMMENT '结束仓内温度(℃)',
    end_inner_humidity DECIMAL(5,2) COMMENT '结束仓内湿度(%)',
    end_outer_temp DECIMAL(5,2) COMMENT '结束仓外温度(℃)',
    end_outer_humidity DECIMAL(5,2) COMMENT '结束仓外湿度(%)',

    -- 本次变化量
    temp_change DECIMAL(5,2) COMMENT '本次温度变化(℃)',
    moisture_change DECIMAL(5,2) COMMENT '本次水分变化(%)',

    -- 状态
    status TINYINT DEFAULT 1 COMMENT '状态: 1-进行中 2-已完成 3-已取消',

    -- 操作信息
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '本次作业备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_record_id (record_id),
    UNIQUE KEY uk_record_seq (record_id, seq_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业明细表';

-- ============================================================
-- 五、系统支撑表 (System)
-- ============================================================

-- --------------------------------------------
-- 用户表
-- --------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密存储)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role_type VARCHAR(20) COMMENT '角色: admin-管理员/keeper-保管员/director-库主任/safety-安全员',
    warehouse_id BIGINT COMMENT '所属库区ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------
-- 数据字典表
-- --------------------------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签(显示值)',
    dict_value VARCHAR(100) COMMENT '字典值(存储值)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dict_type_code (dict_type, dict_code),
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- ============================================================
-- 六、初始化数据
-- ============================================================

-- --------------------------------------------
-- 粮食品种初始数据
-- --------------------------------------------
INSERT INTO grain_variety (variety_code, variety_name, sort_order, status) VALUES
('WHEAT', '小麦', 1, 1),
('CORN', '玉米', 2, 1),
('PADDY', '稻谷', 3, 1),
('RICE', '大米', 4, 1),
('SOYBEAN', '大豆', 5, 1),
('RAPESEED', '油菜籽', 6, 1);

-- --------------------------------------------
-- 数据字典 - 作业类型
-- --------------------------------------------
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, sort_order, remark) VALUES
('operation_type', 'ventilation', '通风作业', 'ventilation', 1, '机械通风,降低粮温'),
('operation_type', 'aeration', '气调作业', 'aeration', 2, '调节仓内气体成分'),
('operation_type', 'temperature', '控温作业', 'temperature', 3, '仓房温度控制'),
('operation_type', 'fumigation', '熏蒸作业', 'fumigation', 4, '化学药剂熏蒸杀虫'),
('operation_type', 'drying', '烘干作业', 'drying', 5, '粮食烘干处理'),
('operation_type', 'cleaning', '清理作业', 'cleaning', 6, '仓房清理整备');

-- --------------------------------------------
-- 数据字典 - 粮食等级
-- --------------------------------------------
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, sort_order, remark) VALUES
('grain_grade', 'FIRST', '一等', 'FIRST', 1, '一等粮'),
('grain_grade', 'SECOND', '二等', 'SECOND', 2, '二等粮'),
('grain_grade', 'THIRD', '三等', 'THIRD', 3, '三等粮'),
('grain_grade', 'FOURTH', '四等', 'FOURTH', 4, '四等粮'),
('grain_grade', 'FIFTH', '五等', 'FIFTH', 5, '五等粮');

-- --------------------------------------------
-- 数据字典 - 虫粮等级
-- --------------------------------------------
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, sort_order, remark) VALUES
('insect_level', 'NONE', '无虫', 'NONE', 1, '无虫粮'),
('insect_level', 'BASIC', '基本无虫', 'BASIC', 2, '基本无虫粮'),
('insect_level', 'NORMAL', '一般虫粮', 'NORMAL', 3, '一般虫粮'),
('insect_level', 'SERIOUS', '严重虫粮', 'SERIOUS', 4, '严重虫粮');

-- --------------------------------------------
-- 数据字典 - 报警级别
-- --------------------------------------------
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, sort_order, remark) VALUES
('alarm_level', 'LEVEL_1', '一般', '1', 1, '一般报警'),
('alarm_level', 'LEVEL_2', '严重', '2', 2, '严重报警'),
('alarm_level', 'LEVEL_3', '紧急', '3', 3, '紧急报警');

-- --------------------------------------------
-- 默认报警阈值配置
-- --------------------------------------------
INSERT INTO alarm_threshold (threshold_code, threshold_name, metric_type, operator, threshold_value, alarm_level, remark) VALUES
('MAX_GRAIN_TEMP_28', '最高粮温超28度', 'max_grain_temp', '>', 28.00, 2, '最高粮温超过28℃触发报警'),
('MAX_GRAIN_TEMP_30', '最高粮温超30度', 'max_grain_temp', '>', 30.00, 3, '最高粮温超过30℃触发紧急报警'),
('AVG_GRAIN_TEMP_25', '平均粮温超25度', 'avg_grain_temp', '>', 25.00, 1, '平均粮温超过25℃触发提示'),
('CO2_5000', 'CO2浓度超限', 'co2_concentration', '>', 5.00, 2, 'CO2浓度超过5%触发报警'),
('O2_LOW', '氧气浓度过低', 'o2_concentration', '<', 19.50, 2, '氧气浓度低于19.5%触发报警');

-- --------------------------------------------
-- 默认管理员账号 (密码: admin123，实际部署需加密)
-- --------------------------------------------
INSERT INTO sys_user (username, password, real_name, role_type, status) VALUES
('admin', 'admin123', '系统管理员', 'admin', 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- End of Schema
-- ============================================================