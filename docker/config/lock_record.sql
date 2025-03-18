DROP TABLE IF EXISTS lock_record;

CREATE TABLE lock_record (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             resource_id VARCHAR(255) NOT NULL UNIQUE COMMENT '被锁定的资源',
                             version INT NOT NULL DEFAULT 0 COMMENT '用于乐观锁的版本号',
                             description VARCHAR(255) COMMENT '描述信息'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `lock_record` (`id`, `resource_id`, `version`, `description`) VALUES (1, 'optimisticLock', 0, '乐观锁');
INSERT INTO `lock_record` (`id`, `resource_id`, `version`, `description`) VALUES (2, 'pessimisticLock', 0, '悲观锁');