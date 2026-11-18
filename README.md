# mini-mall

一个从零手写的单体商城后端，基于 Spring Boot 3 + MyBatis-Plus + MySQL。

包含 **4 张表、11 个接口**，覆盖注册登录、Token 鉴权、商品查询、下单、订单查询。
重点不在于功能数量，而在于把几件后端必须做对的事做对：**事务边界、并发下不超卖、鉴权不越权、异常统一收口、分页真的分页。**

所有接口都有真实测试截图，见 [测试与截图](#测试与截图)。

---

## 目录

- [技术栈](#技术栈)
- [功能清单](#功能清单)
- [项目结构](#项目结构)
- [快速启动](#快速启动)
- [接口清单](#接口清单)
- [统一返回格式](#统一返回格式)
- [核心实现](#核心实现)
- [数据库设计](#数据库设计)
- [测试与截图](#测试与截图)
- [踩过的坑](#踩过的坑)
- [已知限制](#已知限制)

---

## 技术栈

| 组件 | 版本 | 用途 |
|---|---|---|
| JDK | 21 | 运行环境 |
| Spring Boot | 3.3.4 | Web、事务、参数校验 |
| MyBatis-Plus | 3.5.5 | ORM、条件构造器、分页插件 |
| MySQL | 8.0 | 存储 |
| Lombok | 随 Boot 版本 | 省略 getter / setter |

## 功能清单

- **用户**：登录签发 Token、登出销毁 Token、查询当前用户、按 id 查用户（密码脱敏）
- **商品**：分页列表（支持关键词模糊搜索）、详情、新增（带参数校验）
- **订单**：下单（多商品、合并同商品、扣库存）、我的订单（分页）、订单详情（含明细、校验归属）
- **横切能力**：拦截器统一鉴权、全局异常处理、统一返回体、参数校验

## 项目结构

```
src/main/java/com/demo/minimall
├── MiniMallApplication.java      启动类
├── common                        公共层
│   ├── Result.java               统一返回体 {code, msg, data}
│   ├── BusinessException.java    业务异常（库存不足、无权限等）
│   └── GlobalExceptionHandler.java  全局异常处理（3 个 handler）
├── config                        配置与拦截器
│   ├── LoginInterceptor.java     登录校验 + 放行规则
│   ├── WebMvcConfig.java         注册拦截器与白名单
│   ├── TokenStore.java           Token -> userId 的存储
│   └── MybatisPlusConfig.java    分页插件
├── controller                    路由层，只做参数接收与返回
├── service / service.impl        业务层，事务边界在这一层
├── mapper                        继承 BaseMapper，无 XML
├── entity                        与表一一对应
├── dto                           接收前端参数 + 校验注解
└── vo                            返回给前端的组合对象
```

根目录另有 `schema.sql`（建库建表 + 演示数据）和 `docs/images/`（接口与边界测试截图）。

## 快速启动

**1. 建库建表**

四张表、演示账号、8 件商品都在根目录的 `schema.sql` 里，一条命令执行：

```bash
mysql -uroot -p < schema.sql
```

也可以用 Navicat / DataGrip 打开这个文件直接运行。脚本是幂等的（`IF NOT EXISTS` + `INSERT IGNORE`），重复执行不会清掉你本地的订单数据。

**演示账号**

| 用户名 | 密码 | 用途 |
|---|---|---|
| `test` | `123456` | 主账号，用来演示「我的订单」 |
| `other` | `123456` | 第二账号，用来验证越权：用 `test` 的 token 查 `other` 的订单，应返回「无权限查看」 |

> 密码明文存储。这是练手项目有意停下的边界，不是可上线的方案 —— 真实项目应存 BCrypt 哈希。

**2. 配置数据库连接**

`src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mini_mall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:123456}
```

数据库账号密码走环境变量 `DB_USER` / `DB_PASSWORD`，冒号后是默认值。
本机不配任何环境变量也能跑，仓库里不出现真实密码。

**3. 启动**

```bash
mvn spring-boot:run
# 端口 8081
```

**4. 验证**

```bash
curl http://localhost:8081/ping
# {"code":200,"msg":"success","data":"pong"}
```

**5. 拿 Token**

```bash
curl -X POST http://localhost:8081/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'
```

返回的 `data` 就是 token，后续请求放到请求头 `Authorization: <token>`。

## 接口清单

除标注「游客」外，都需要在请求头带 `Authorization: <token>`。

| # | 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|---|
| 1 | GET | `/ping` | 健康检查 | 游客 |
| 2 | POST | `/users/login` | 登录，返回 token | 游客 |
| 3 | POST | `/users/logout` | 登出 | 登录 |
| 4 | GET | `/users/me` | 当前登录用户 | 登录 |
| 5 | GET | `/users/{id}` | 按 id 查用户 | 登录 |
| 6 | GET | `/products` | 商品列表（分页 + 关键词） | 游客 |
| 7 | GET | `/products/{id}` | 商品详情 | 游客 |
| 8 | POST | `/products` | 新增商品 | 登录 |
| 9 | POST | `/orders` | 下单 | 登录 |
| 10 | GET | `/orders/my` | 我的订单（分页） | 登录 |
| 11 | GET | `/orders/{id}` | 订单详情（含明细） | 登录 |

**下单请求体**

```json
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

## 统一返回格式

所有接口，包括出错时，都返回同一个结构：

```json
{ "code": 200, "msg": "success", "data": {} }
```

| code | 含义 | HTTP 状态码 |
|---|---|---|
| 200 | 成功 | 200 |
| 400 | 参数校验失败 / 业务失败（库存不足、无权限） | 400 |
| 401 | 未登录 / token 失效 | 401 |
| 500 | 系统异常 | 500 |

`code` 与 HTTP 状态码保持一致，前端只判断 HTTP 状态码即可分流，不必解析 body。

## 核心实现

### 1. 防超卖：条件更新，而不是「先查再改」

```java
@Update("UPDATE mall_product SET stock = stock - #{qty} " +
        "WHERE id = #{productId} AND stock >= #{qty}")
int decreaseStock(@Param("productId") Integer productId, @Param("qty") Integer qty);
```

`decreaseStock(productId, qty)`（**自定义**，`ProductMapper` 第 12 行，是两个接口方法；`productId`/`qty` 来自 `OrderServiceImpl.createOrder` 循环里的 `merged` Map；作用：一条 SQL 内完成「判断库存够不够 + 扣减」，返回受影响行数）

判断库存够不够这件事，**只有 `WHERE stock >= #{qty}` 能保证**。
Service 里那句 `if (product.getStock() < qty) throw` 只是提前给出友好提示，它在并发下完全不可靠——两个线程可能同时读到 10，然后都认为够。

`rows == 0` 表示没扣成功（库存已被别人抢走），必须抛异常让事务回滚。

### 2. 事务边界在 Service

```java
@Transactional(rollbackFor = Exception.class)
public String createOrder(OrderCreateDTO dto, Integer userId) { ... }
```

下单要做四件事：写订单主表 → 写订单明细 → 扣库存 → 返回订单号。**这四步要么全成，要么全不成。**

`@Transactional` 放在 Service 而不是 Controller，因为事务描述的是「一个业务操作」，Controller 的职责只是接参数、转结果，它不应该知道业务需要几步。

`rollbackFor = Exception.class` 不能省：Spring 默认只对 `RuntimeException` 和 `Error` 回滚，**受检异常不会回滚**——这会导致「抛了异常，但数据留在库里」。

### 3. 鉴权在拦截器，身份只从 token 来

```java
if ("GET".equalsIgnoreCase(request.getMethod()) && url.startsWith("/products")) {
    return true;   // 只有「GET + 商品路径」才允许游客
}
```

放行规则必须同时判断**方法**和**路径**：

- 只按路径放行（`excludePathPatterns("/products/**")`）→ `POST /products` 也跟着放行了，未登录就能新增商品
- 只按方法放行（`GET` 就放行）→ `GET /users/me` 也放行，Controller 拿不到 `currentUser`，直接 500

校验通过后把用户对象放进 `request.setAttribute("currentUser", user)`，Controller 用 `@RequestAttribute` 取。
**为什么不用请求参数传 userId**：那样前端改个数字就能查别人的订单。从 token 解析出来的身份，前端伪造不了。

### 4. 统一异常处理

| 异常 | 处理 | 返回 |
|---|---|---|
| `BusinessException` | 可预期的业务错误 | 400 + 具体原因 |
| `MethodArgumentNotValidException` | 参数校验失败 | 400 + 第一条校验提示 |
| `Exception` | 兜底 | 500 + 「系统繁忙」 |

兜底 handler 里有一行不能删：

```java
log.error("未预期异常", e);
```

只返回「系统繁忙」而把异常吞掉，线上出问题连哪一行报错都不知道。

`@ExceptionHandler` 的匹配看**类型层级**，不看书写顺序。所以兜底必须用最顶层的 `Exception`，否则 NPE 会一路往上找不到匹配。

### 5. 分页是真的分页

```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
}
```

`selectPage` 本身不会产生 `LIMIT`，没有这个插件就是**假分页**——查出全表再在内存里截取。表小的时候看不出问题，数据一多就崩。

### 6. 明细表存商品快照

`mall_order_item` 里存了 `product_name` 和 `product_price`，而不是只存 `product_id`。

因为**订单是历史凭证**。商品明天涨价或改名，昨天那笔订单的金额和名称不能跟着变。

## 数据库设计

四张表：`mall_user`、`mall_product`、`mall_order`、`mall_order_item`。建表脚本见 `schema.sql`。

**业务表故意不加物理外键。**

理由：外键会在插入明细时对主表加隐式共享锁，高并发下容易出现锁等待。约束改由应用层保证（下单时先校验商品存在）。这也是一个真实踩过的坑，见下节。

三处关键索引：

| 表 | 索引 | 作用 |
|---|---|---|
| `mall_user` | `UNIQUE uk_username` | 登录按用户名查，同时保证用户名不重复 |
| `mall_order` | `UNIQUE order_no` | 订单号唯一。幂等方案落地时，这是最后一道防线 |
| `mall_order` | `idx_user_id` | 「我的订单」按 `user_id` 过滤，否则全表扫 |
| `mall_order_item` | `idx_order_id` | 详情页按 `order_id` 捞明细 |

## 测试与截图

测试工具 Postman，截图存放在 `docs/images/`。

### 接口回归（20 条）

| # | 请求 | 预期结果 | 截图 |
|---|---|---|---|
| 1 | `GET /ping` | `pong` | [01](docs/images/api/01-ping.png) |
| 2 | `POST /users/login` 正确密码 | 200 + 32 位 token | [02](docs/images/api/02-login-success.png) |
| 3 | `POST /users/login` 错误密码 | 400 用户名或密码错误 | [03](docs/images/api/03-login-wrong-password.png) |
| 4 | `POST /users/login` 空 username | 400 用户名不能为空 | [04](docs/images/api/04-login-empty-username.png) |
| 5 | `GET /users/me` 带 token | 200 + 用户信息，密码为 null | [05](docs/images/api/05-users-me-with-token.png) |
| 6 | `GET /users/me` 不带 token | 401 未登录 | [06](docs/images/api/06-users-me-no-token.png) |
| 7 | `GET /products` | 200 + total + 5 条 | [07](docs/images/api/07-products-list.png) |
| 8 | `GET /products?page=2` | 200 + 第二页 | [08](docs/images/api/08-products-page-2.png) |
| 9 | `GET /products?keyword=手机` | 200 + total = 1 | [09](docs/images/api/09-products-keyword.png) |
| 10 | `GET /products/1` | 200 + iPhone 15 | [10](docs/images/api/10-product-detail.png) |
| 11 | `POST /products` 带 token | 200 添加成功 | [11](docs/images/api/11-create-product-success.png) |
| 12 | `POST /products` 不带 token | 401 未登录 | [12](docs/images/api/12-create-product-no-token.png) |
| 13 | `POST /orders` 正常 | 200 + 订单号 | [13](docs/images/api/13-create-order-success.png) |
| 14 | `POST /orders` 库存 999 | 400 库存不足 | [14](docs/images/api/14-create-order-stock-insufficient.png) |
| 15 | `POST /orders` quantity = 0 | 400 数量必须至少为 1 | [15](docs/images/api/15-create-order-quantity-zero.png) |
| 16 | `POST /orders` 不带 token | 401 未登录 | [16](docs/images/api/16-create-order-no-token.png) |
| 17 | `GET /orders/my` | 200 + 只有自己的订单 | [17](docs/images/api/17-orders-my.png) |
| 18 | `GET /orders/1` 自己的订单 | 200 + order + items | [18](docs/images/api/18-order-detail.png) |
| 19 | `GET /orders/12345` 不存在 | 400 订单不存在 | [19](docs/images/api/19-order-not-exist.png) |
| 20 | `GET /orders/my` 是否被 `/{id}` 抢走 | 返回分页结构而非订单详情 | 同 [17](docs/images/api/17-orders-my.png) |

第 20 条是个容易忽略的点：`/orders/my` 和 `/orders/{id}` 只差一个路径段，如果被 `/{id}` 匹配走，会报「订单不存在」。
Spring 的路径匹配会优先选择字面量 `/my`，但必须实测确认过。

### 边界测试

| # | 攻击方式 | 请求 | 实际结果 | 截图 |
|---|---|---|---|---|
| 1 | 负数量 | `quantity: -5` | 400 数量必须至少为 1 | [01](docs/images/boundary/01-quantity-negative.png) |
| 2 | 超大数量 | `quantity: 999999999` | 400 库存不足（**本该在入口拦掉**） | [02](docs/images/boundary/02-quantity-too-large.png) |
| 3 | 不存在的商品 | `productId: 9999999` | 400 商品不存在 | [03](docs/images/boundary/03-product-not-exist.png) |
| 4 | 空 items | `{"items":[]}` | 400 订单不能为空 | [04](docs/images/boundary/04-items-empty.png) |
| 5 | body 里没有 items | `{}` | 400 订单不能为空 | [05](docs/images/boundary/05-body-empty.png) |
| 6 | token 乱填 | `Authorization: 123456789` | 401 登录已过期 | [06](docs/images/boundary/06-token-invalid.png) |
| 7 | `Bearer ` 前缀带 / 不带 | `Authorization: Bearer <token>` 与 `Authorization: <token>` | 两种都 200 通过 | [07](docs/images/boundary/07-token-with-bearer.png) |
| 8 | 商品名全空格 | `{"name":"   "}` | 400 商品名不能为空 | [08](docs/images/boundary/08-product-name-blank.png) |

第 2 条是这轮测试最有价值的发现：`@Min(1)` 只保证不小于 1，没有上限，所以 9 亿这个请求会一路走到扣库存，被 `rows == 0` 挡住。
**结果是对的，但代价是白查了商品、白开了事务、白拼了一次 UPDATE。** 正确做法是加 `@Max(999)`，在入口就拦掉——校验要在最便宜的地方做。

第 7 条记的是 `LoginInterceptor` 的宽容之处：

```java
if (token.startsWith("Bearer ")) {
    token = token.substring(7);   // 有前缀才截
}
User user = userService.getByToken(token);   // 没前缀就原样查
```

带前缀和不带前缀都能通过，因为只有判断成立时才截断，不成立时 token 原样使用。
这不算漏洞（token 本身仍然要有效），但接口约定上应当统一推荐一种写法，避免调用方各写各的。

## 踩过的坑

| 坑 | 现象 | 原因与解决 |
|---|---|---|
| 假分页 | `getRecords()` 只有 5 条，但 SQL 日志里没有 `LIMIT` | 没注册 `PaginationInnerInterceptor`。看起来在分页，其实是查全表再截取 |
| 路径级放行放掉了写接口 | `excludePathPatterns("/products/**")` 后，不带 token 也能 `POST /products` | 白名单是路径级的，不分方法。改成拦截器内判断「GET + 路径前缀」 |
| 只判断方法没判断路径 | 所有 GET 都放行，`GET /users/me` 穿透到 Controller 报 500 `Missing request attribute 'currentUser'` | 补上 `uri.startsWith("/products")` |
| 去掉 `@Transactional` 造出脏数据 | 报错后库里多出一张 650 万的订单，2 个商品只写进 1 条明细，库存只扣了第一个 | 事务不是让代码变正确，是让做了一半的改动可以撤销 |
| 验证回滚时靠记忆记库存 | 以为没回滚，其实是之前的测试数据改了基线 | 实验前用一条 SQL 拍快照（订单数、明细数、各商品库存），前后对比。**不记绝对值，只比差值** |
| 改注解后没有重启 | 注释掉 `@Transactional` 后行为完全没变，差点得出错误结论 | IDEA 热加载只替换方法体，**不重建 Spring 代理**。改注解必须真正重启 |
| 嵌套对象校验静默失效 | 只在 `items` 上加 `@Valid`，内层 `quantity = 0` 不报错，接口返回 200 | `@Valid` 不会自动向下传递，必须内外都标 |
| 外键引发锁等待 | 并发插入明细时 `1205 Lock wait timeout`（50 秒） | 物理外键对主表加隐式共享锁。本项目四张表都不加外键，约束放到应用层 |

## 已知限制

这个项目是练手项目，有意停在一个明确的边界上：

- **Token 存在内存 Map**，重启即失效，多实例无法共享。后续可换 Redis：`TokenStore` 是接口，换实现不影响业务代码
- **下单没有幂等**，用户连点两次会生成两单。需要 `requestId` + 唯一索引
- **没有商品分类**，关键词搜索基于 `LIKE`，数据量大时需要索引或搜索引擎
- **没有订单状态流转**（支付、发货、取消），`status` 字段已预留
- **没有库存回补**，取消订单不会还原库存

---

## 关于这个项目

这不是一个跟着教程抄下来的 CRUD 项目。

`mall-demo` 是我最初拿来做参考的代码，功能更多，但我讲不清里面每一行的来龙去脉。所以在面试前，我把整个项目推倒，从建库、建表开始，一个文件一个文件重写成了现在这个 mini-mall。

写的过程中踩了上面那些坑，也因此在几个地方做了取舍：表不加物理外键、明细存商品快照、异常必须有一个兜底 handler 且不能吞日志。

**代码量比原项目少，但每一行都能解释。**
