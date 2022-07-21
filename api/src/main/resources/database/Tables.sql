CREATE TABLE `jianmu_engine_lock`
(
    `id`                 INT NOT NULL COMMENT 'ID',
    `business_code`         VARCHAR(25) NOT NULL COMMENT '加锁业务代码',
    `update_time`           DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `workflow_ref_UNIQUE` (`business_code`),
    UNIQUE KEY `workflow_name_UNIQUE` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='分布式引擎锁';