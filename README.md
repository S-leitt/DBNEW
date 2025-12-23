# Database Sync System

数据库同步系统是一款基于 Spring Boot 3 的示例服务，展示如何在多种数据库之间执行数据同步、数据管理以及权限控制。项目提供统一的 RESTful API，支持定时同步、冲突处理和基础的安全配置，便于进一步扩展为生产级的数据交换平台。

## 功能特性

- **多数据库同步**：通过 `SyncManager` 统一调度 MySQL、Oracle、SQL Server 等数据源的全量或单表同步任务，并记录同步日志。 
- **数据管理接口**：`DataController` 提供表结构、分页数据查询以及插入、更新、删除等操作，用于验证或补充同步结果。 
- **冲突处理与日志**：结合 `ConflictResolutionService` 与同步日志模型 `SyncLog` 记录同步状态，便于排查和审计。 
- **安全与监控**：集成 Spring Security 基础认证，`HealthController` 等端点便于外部探活和运维监控。 
- **可配置调度**：使用 `@Scheduled` 定时任务，支持每日全量同步与定期状态检查，调度参数在配置文件中可调。

## 环境要求

- JDK 21 及以上
- Maven 3.9 及以上
- 可选：本地或远程的 MySQL、Oracle、SQL Server 数据库实例用于实际同步演示

## 配置说明

应用的主要配置位于 `src/main/resources/application.properties`，包含：

- **数据源**：`spring.datasource.dynamic.datasource.*` 下定义了 MySQL、Oracle、SQL Server 的连接信息。使用前请将示例的 `url`、`username`、`password` 更新为可访问的真实数据库，并根据需要新增数据源。
- **同步调度**：`sync.scheduled.*` 控制每日全量同步时间、定期一致性检查以及变更检测频率。
- **安全认证**：`spring.security.user.*` 设置了内置用户的用户名、密码和角色，需在部署前替换为安全凭据。
- **邮件通知**：`spring.mail.*` 提供冲突或异常通知的 SMTP 配置，可按需要开启或关闭。

## 快速开始

1. 安装依赖并编译项目：
   ```bash
   mvn clean package
   ```
2. 启动应用（默认端口 8080，上下文路径 `/sync`）：
   ```bash
   mvn spring-boot:run
   ```
3. 验证健康检查：
   ```bash
   curl http://localhost:8080/sync/api/health
   ```

## 主要 API

- **同步任务**（`/api/sync`）：
  - `POST /all`：启动所有数据库之间的全量同步。
  - `POST /database`：`{sourceDatabase, targetDatabase}` 启动指定数据库间同步。
  - `POST /table`：`{sourceDatabase, targetDatabase, tableName}` 启动单表同步。
  - `GET /logs`：获取最近 100 条同步日志。
  - `GET /status`：查看同步服务状态。
- **数据管理**（`/api/data`）：
  - `GET /tables/{databaseId}`：列出指定数据源的表名。
  - `GET /table/{databaseId}/{tableName}`：分页获取表数据，支持 `page`、`pageSize` 参数。
  - `GET /table/{databaseId}/{tableName}/columns`：查看表字段。
  - `POST /table/{databaseId}/{tableName}`：插入数据。
  - `PUT /table/{databaseId}/{tableName}`：更新数据，需提供主键名和值。
  - `DELETE /table/{databaseId}/{tableName}`：按主键删除数据。
- **权限管理**（`/api/permissions`）：通过 `PermissionController` 管理用户、角色与权限分配。

更多端点和模型可以在 `src/main/java/com/database/sync/controller` 及 `model`、`service` 包中查看和扩展。

## 开发与测试建议

- 使用 IDE 或编辑器启用 Lombok 注解处理器，以避免编译期缺失 getter/setter。 
- 开发环境可以将 `logging.level.*` 提升为 `DEBUG` 以便跟踪 SQL 和同步流程。 
- 在生产部署前，请为数据库连接、内置账户和邮件服务配置安全凭据，并限制对管理端点的访问。

## 许可证

本项目目前未附带许可证，可根据团队需求补充开源或商业授权条款。
