CREATE TABLE `jianmu_engine_lock`
(
    `id`                 INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `business_code`         VARCHAR(25) NOT NULL COMMENT '加锁业务代码',
    `update_time`           DATETIME NOT NULL COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='分布式引擎锁';


CREATE TABLE `jianmu_engine_task` (
      `id` int NOT NULL AUTO_INCREMENT,
      `uuid` varchar(45) NOT NULL,
      `transaction_id` bigint NOT NULL,
      `type` varchar(45) NOT NULL,
      `priority` int NOT NULL,
      `cron` varchar(45) NOT NULL,
      `worker_id` varchar(45) NOT NULL,
      `script` text NOT NULL,
      `status` varchar(45) NOT NULL,
      `start_time` datetime NOT NULL,
      `end_time` datetime NOT NULL,
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci