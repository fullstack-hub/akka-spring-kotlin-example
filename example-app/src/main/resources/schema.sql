DROP TABLE IF EXISTS `chats`;
CREATE TABLE IF NOT EXISTS `events`
(
    `id`      bigint      NOT NULL AUTO_INCREMENT,
    `name`    varchar(20) NOT NULL,
    `tickets` int         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;