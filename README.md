# OnlineMall (电子商城系统)

# 陈锦 202330304951
本项目为《网络应用架构开发》课程实验项目

---

## 📖 项目简介
- 这是一个基于 Spring Boot + MyBatis-Plus + 原生前端技术栈实现的简易 B2C 电子商务平台。支持用户购物流程、管理员后台管理、邮件通知及数据报表导出。

- 本项目采用前后端分离的开发思想（但在本项目中为了部署方便，前端资源静态托管在 Spring Boot 中），实现了电子商务网站的核心业务流程。
- 本网站已部署至服务器，欢迎各位访问 http://shop.fenrirchen.com

---

## ✨ 核心功能

### 👨‍💻 顾客端 (Customer)

- **账户体系**：注册、登录、注销。
- **商品浏览**：首页商品展示、模糊搜索、库存显示。
- **购物车**：添加商品（自定义数量）、购物车内数量调整、删除商品、金额自动计算。
- **订单流程**：填写收货信息、模拟支付、邮件确认通知、查看历史订单状态及详情。

### 👮 管理员端 (Admin)

- **仪表盘**：查看核心指标（销售额、订单数）、热销商品排行、近7日销售业绩报表（支持 Excel 导出）。
- **商品管理**：商品的增删改查 (CRUD)、图片链接管理、库存调整。
- **订单管理**：查看全平台订单、处理发货状态。
- **日志审计**：查看用户登录、下单等关键行为日志。

---

## 🛠️ 技术栈

- **后端**：Java 17/22, Spring Boot 3.3.5
- **数据库**：MySQL 8.0
- **ORM 框架**：MyBatis-Plus
- **工具库**：Lombok, Apache POI (Excel导出), JavaMail (邮件发送)
- **前端**：HTML5, CSS3, JavaScript (原生 Fetch API), Bootstrap 5 (UI 框架)

---

## 📂 项目文件结构说明

以下是项目的核心目录结构及其功能描述：
```text
shopping/
├── src/main/java/com/example/shopping/
│ ├── config/
│ │ └── WebConfig.java # [配置] 静态资源映射配置，确保前端HTML能正确访问
│ ├── controller/ # [控制层] 处理前端 HTTP 请求
│ │ ├── AdminController.java # 后台数据接口：统计报表、Excel导出、日志查询
│ │ ├── AuthController.java # 认证接口：登录、注册、注销、当前用户状态
│ │ ├── CartController.java # 购物车接口：增删改查、数量更新
│ │ ├── OrderController.java # 订单接口：下单、发货、详情查询
│ │ └── ProductController.java # 商品接口：商品列表、搜索、管理(CRUD)
│ ├── entity/ # [实体层] 数据库表对应的 Java 类
│ │ ├── User.java # 用户实体 (实现了 Serializable 接口防止掉线)
│ │ ├── Product.java # 商品实体
│ │ ├── Orders.java # 订单主表实体 (处理了驼峰映射)
│ │ ├── OrderItem.java # 订单明细实体
│ │ ├── CartItem.java # 购物车项实体
│ │ └── SysLog.java # 系统日志实体
│ ├── mapper/ # [持久层] MyBatis-Plus 接口，直接操作数据库
│ │ └── ... (UserMapper, ProductMapper 等)
│ ├── service/ # [业务层] 核心业务逻辑
│ │ ├── CartService.java # 购物车逻辑：数量更新、清空购物车
│ │ ├── OrderService.java # 订单逻辑：事务控制、扣库存、发送邮件
│ │ ├── StatsService.java # 统计逻辑：计算销售额、生成 Excel 文件
│ │ └── ... (UserService, LogService 等)
│ └── ShoppingApplication.java # Spring Boot 主启动类
│
├── src/main/resources/
│ ├── mapper/ # (可选) 存放 MyBatis XML 映射文件
│ ├── static/ # [前端资源] 存放所有 HTML/CSS/JS 文件
│ │ ├── admin/ # 后台管理页面目录
│ │ │ ├── dashboard.html # 仪表盘：数据统计、图表、导出 Excel
│ │ │ ├── logs.html # 日志页：查看用户行为日志
│ │ │ ├── orders.html # 订单管理：查看所有订单、发货操作
│ │ │ └── products.html # 商品管理：添加/编辑/删除商品
│ │ ├── css/ # 公共样式文件 (如有)
│ │ ├── js/ # 公共脚本文件 (如有)
│ │ ├── cart.html # 顾客-购物车页面
│ │ ├── index.html # 顾客-商城首页
│ │ ├── login.html # 公共-登录/注册页
│ │ └── orders.html # 顾客-我的订单历史页
│ └── application.properties # 项目核心配置文件 (DB连接、邮箱SMTP、Session设置)
│
└── pom.xml # Maven 依赖配置文件
```
---

## 🚀 快速开始

### 1. 环境准备
- JDK 17 或以上
- MySQL 8.0
- Maven 3.6+
- IDE (IntelliJ IDEA 推荐)

### 2. 数据库配置
- 创建一个名为 `mall_db` 的数据库。
- 运行提供的 `init.sql` 脚本建表并导入初始数据。

### 3. 修改配置
- 打开 `src/main/resources/application.properties`，修改以下信息：
```properties
spring.datasource.password=你的数据库密码
spring.mail.username=你的发件邮箱
spring.mail.password=你的SMTP授权码
```


### 4. 运行项目

- 在 IDEA 中运行 ShoppingApplication.java。
- 等待控制台输出 Started ShoppingApplication in ...。

### 5. 访问系统

- 打开浏览器访问：http://localhost:8080

预置测试账号：

- 管理员：admin / 123456

- 顾客：zhangsan / 123456
