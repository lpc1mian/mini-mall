-- ============================================================
-- mini-mall 建库建表脚本
-- 用法：mysql -uroot -p < schema.sql
-- 说明：脚本可重复执行（表用 IF NOT EXISTS，数据用 INSERT IGNORE），
--       已存在的表和数据不会被覆盖，不会清空你本地的订单。
-- 注意：表之间没有物理外键，详见 README「数据库设计」一节。
-- ============================================================

CREATE DATABASE IF NOT EXISTS mini_mall
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE mini_mall;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mall_user (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    password    VARCHAR(100) NOT NULL                COMMENT '密码',
    phone       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 商品表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mall_product (
    id          INT            NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';

-- ------------------------------------------------------------
-- 订单表
-- order_no 加唯一索引：下单幂等的最后一道防线（应用层尚未使用，字段已就位）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mall_order (
    id           INT            NOT NULL AUTO_INCREMENT,
    user_id      INT            NOT NULL,
    order_no     VARCHAR(32)    NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status       INT            NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已发货 3已完成 4已取消',
    create_time  DATETIME       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY order_no (order_no),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- ------------------------------------------------------------
-- 订单明细表
-- product_name / product_price 是下单那一刻的快照，
-- 商品后续改名或改价，历史订单金额不受影响。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mall_order_item (
    id            INT            NOT NULL AUTO_INCREMENT,
    order_id      INT            NOT NULL,
    product_id    INT            NOT NULL,
    product_name  VARCHAR(100)   NOT NULL,
    product_price DECIMAL(10, 2) NOT NULL,
    quantity      INT            NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单明细表';

-- ------------------------------------------------------------
-- 演示账号
-- 密码明文存储：本项目未接入加密，仅用于本地演示，不是可上线方案。
-- test  = 主账号，用来看「我的订单」
-- other = 第二账号，用来验证越权：用 test 的 token 查 other 的订单应返回「无权限查看」
-- ------------------------------------------------------------
INSERT IGNORE INTO mall_user (id, username, password, phone) VALUES
(1, 'test',  '123456', '13800000000'),
(2, 'other', '123456', NULL);

-- ------------------------------------------------------------
-- 商品初始数据（stock 与 README 的边界测试用例对齐：iPhone 100、华为 30）
-- ------------------------------------------------------------
INSERT IGNORE INTO mall_product (id, name, price, stock) VALUES
(1, 'iPhone 15',    5999.00, 100),
(2, '小米 14',      3999.00,  50),
(3, '华为 Mate 60', 6499.00,  30),
(4, '机械键盘',       399.00, 200),
(5, '罗技鼠标',       199.00, 150),
(6, '27寸显示器',    1299.00,  40),
(7, '蓝牙耳机',       499.00,  80),
(8, '手机壳',          39.00, 500);
