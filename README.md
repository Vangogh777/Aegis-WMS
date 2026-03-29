# Aegis-WMS 神盾仓储管理系统

一个基于领域驱动设计(DDD)的粮食仓储管理系统后端，提供完整的粮食仓储业务管理功能。

## 技术栈

- **Java 17+**
- **Spring Boot 3.2**
- **MyBatis-Plus 3.5.5**
- **MySQL 8.0+**
- **Kafka (可选，用于报警消息)**
- **Redis (可选，用于缓存)**

## 项目结构

采用DDD分层架构：

```
src/main/java/com/aegis/wms/
├── domain/                      # 领域层
│   ├── masterdata/              # 基础主数据领域
│   │   ├── entity/              # 实体: Warehouse, Bin, Position, GrainVariety
│   │   └── repository/          # 仓储接口
│   ├── inventory/               # 库存管理领域
│   │   ├── entity/              # 实体: Stock, InboundOrder, OutboundOrder
│   │   └── repository/
│   ├── graincondition/          # 粮情检测领域
│   │   ├── entity/              # 实体: GrainConditionRecord, Alarm, AlarmThreshold
│   │   └── repository/
│   ├── operation/               # 仓储作业领域
│   │   ├── entity/              # 实体: OperationScheme, OperationRecord, OperationDetail
│   │   └── repository/
│   └── report/                  # 数据报表领域(无实体，聚合查询)
│
├── application/                 # 应用层
│   ├── masterdata/              # 基础主数据应用服务
│   │   ├── dto/                 # 数据传输对象
│   │   ├── vo/                  # 视图对象
│   │   ├── service/             # 服务接口
│   │   └── service/impl/        # 服务实现
│   ├── inventory/               # 库存管理应用服务
│   ├── graincondition/          # 粮情检测应用服务
│   ├── operation/               # 仓储作业应用服务
│   └── report/                  # 数据报表应用服务
│
├── interfaces/                  # 接口层
│   └── controller/
│       ├── masterdata/          # 基础主数据控制器
│       ├── inventory/           # 库存管理控制器
│       ├── graincondition/      # 粮情检测控制器
│       ├── operation/           # 仓储作业控制器
│       └── report/              # 数据报表控制器
│
└── common/                      # 公共组件
    ├── result/                  # 统一响应结果
    └── util/                    # 工具类
```

## 业务领域说明

### 1. 基础主数据领域
- **库区管理**: 粮库基本信息、总仓容、地址等
- **仓房管理**: 仓房信息、仓型(高大平房仓/浅圆仓/立筒仓)、仓容
- **货位管理**: 货位信息、容量、归属仓房
- **粮食品种**: 品种编码、名称、安全水分、安全温度

### 2. 库存管理领域
- **库存查询**: 各货位库存数量、品种、入库日期
- **入库管理**: 入库单创建、审核、完成
- **出库管理**: 出库单创建、审核、完成

### 3. 粮情检测领域
- **粮情记录**: 粮温、仓温、仓湿、虫害等级检测记录
- **报警管理**: 温度/湿度/虫害报警，报警阈值配置
- **自动报警**: 通过Kafka消息自动触发报警检查

### 4. 仓储作业领域
- **作业方案**: 通风/气调/控温/熏蒸作业方案配置
- **作业记录**: 作业执行记录，状态流转(待执行→作业中→已完成/已取消)
- **作业明细**: 每次启停记录，时长、能耗计算

### 5. 数据报表领域
- **库区概览**: 仓房数量、库存总量、库容使用率
- **粮情汇总**: 平均粮温、最高/最低粮温、待处理报警数
- **作业汇总**: 出入库统计、作业时长统计
- **日报表/月报表**: 运营数据汇总

## 快速开始

### 1. 环境准备
- JDK 17 或更高版本
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
创建数据库并执行schema.sql：
```sql
CREATE DATABASE aegis_wms DEFAULT CHARACTER SET utf8mb4;
USE aegis_wms;
SOURCE schema.sql;
```

### 3. 配置文件
修改 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aegis_wms?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

### 4. 启动项目
```bash
mvn spring-boot:run
```

服务启动后访问：http://localhost:8080

## API 接口

所有API遵循RESTful风格，基础路径 `/api`：

| 领域 | 路径 | 说明 |
|------|------|------|
| 库区 | `/api/masterdata/warehouse` | 库区CRUD |
| 仓房 | `/api/masterdata/bin` | 仓房CRUD |
| 货位 | `/api/masterdata/position` | 货位CRUD |
| 粮品种 | `/api/masterdata/grain-variety` | 粮品种CRUD |
| 库存 | `/api/inventory/stock` | 库存查询 |
| 入库 | `/api/inventory/inbound` | 入库单管理 |
| 出库 | `/api/inventory/outbound` | 出库单管理 |
| 粮情 | `/api/graincondition/record` | 粮情记录 |
| 报警 | `/api/graincondition/alarm` | 报警管理 |
| 阈值 | `/api/graincondition/threshold` | 报警阈值 |
| 作业方案 | `/api/operation/scheme` | 作业方案 |
| 作业记录 | `/api/operation/record` | 作业记录 |
| 作业明细 | `/api/operation/detail` | 启停记录 |
| 报表 | `/api/report` | 各类报表 |

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## 分页参数

分页接口统一使用 `current` 和 `size` 参数：
- `current`: 当前页码，默认1
- `size`: 每页大小，默认10

## 许可证

MIT License