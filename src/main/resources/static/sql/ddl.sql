DROP TABLE IF EXISTS `User`;

CREATE TABLE `User` (
                        `user_id`	bigint	NOT NULL,
                        `user_name`	varchar(30)	NOT NULL	COMMENT '사용자명',
                        `password`	varchar(255	NULL,
                        `email`	varchar(255)	NOT NULL	COMMENT '사용자 이메일, 중복X',
                        `mobile_number`	varchar(50)	NOT NULL,
                        `address`	varchar(500)	NOT NULL,
                        `created_at`	datetime	NULL,
                        `modified_at`	datetime	NULL
);

DROP TABLE IF EXISTS `Product`;

CREATE TABLE `Product` (
                            `product_id`	bigint	NOT NULL,
                            `product_name`	varchar(500)	NULL,
                            `stock`	int	NULL,
                            `image_url`	varchar(500)	NULL
);

DROP TABLE IF EXISTS `Order`;

CREATE TABLE `Order` (
                         `order_id`	bigint	NOT NULL,
                         `order_quantity`	int	NULL,
                         `dilivery_status`	enum	NULL,
                         `user_id`	bigint	NOT NULL,
                         `product_id`	bigint	NOT NULL,
                         `created_at`	datetime	NULL,
                         `modified_at`	datetime	NULL
);

DROP TABLE IF EXISTS `Wish_list`;

CREATE TABLE `Wish_list` (
                             `wish_list_id`	bigint	NOT NULL,
                             `wish_quantity`	VARCHAR(255)	NULL,
                             `product_id`	bigint	NOT NULL,
                             `user_id`	bigint	NOT NULL,
                             `created_at`	datetime	NULL,
                             `modified_at`	datetime	NULL
);

DROP TABLE IF EXISTS `Brand`;

CREATE TABLE `Brand` (
                          `brand_id`	bigint	NOT NULL,
                          `brand_name`	varchar(255)	NULL,
                          `bramd_detail`	varchar	NULL
);

DROP TABLE IF EXISTS `product_detail`;

CREATE TABLE `product_detail` (
                                  `product_detail_id`	bigint	NOT NULL,
                                  `product_id`	bigint	NOT NULL,
                                  `product_detail`	varchar	NULL
);

DROP TABLE IF EXISTS `event`;

CREATE TABLE `event` (
                          `event_id`	bigint	NOT NULL,
                          `event_name`	varchar(255)	NULL,
                          `start_date`	datetime	NULL,
                          `end_date`	datetime	NULL,
                          `event_detail`	varchar	NULL,
                          `created_at`	datetime	NULL,
                          `modified_at`	datetime	NULL,
                          `collaboration_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `collaboration_product`;

CREATE TABLE `collaboration_product` (
                                         `collaboration_id`	bigint	NOT NULL,
                                         `collaboration_detail`	varchar	NULL,
                                         `product_id`	bigint	NOT NULL,
                                         `brand_id`	bigint	NOT NULL
);

ALTER TABLE `User` ADD CONSTRAINT `PK_USER` PRIMARY KEY (
                                                         `user_id`
    );

ALTER TABLE `Product` ADD CONSTRAINT `PK_PRODUCT` PRIMARY KEY (
                                                                 `product_id`
    );

ALTER TABLE `Order` ADD CONSTRAINT `PK_ORDER` PRIMARY KEY (
                                                           `order_id`
    );

ALTER TABLE `Wish_list` ADD CONSTRAINT `PK_WISH_LIST` PRIMARY KEY (
                                                                   `wish_list_id`
    );

ALTER TABLE `Brand` ADD CONSTRAINT `PK_BRAND` PRIMARY KEY (
                                                             `brand_id`
    );

ALTER TABLE `product_detail` ADD CONSTRAINT `PK_PRODUCT_DETAIL` PRIMARY KEY (
                                                                             `product_detail_id`
    );

ALTER TABLE `event` ADD CONSTRAINT `PK_EVENT` PRIMARY KEY (
                                                             `event_id`
    );

ALTER TABLE `collaboration_product` ADD CONSTRAINT `PK_COLLABORATION_PRODUCT` PRIMARY KEY (
                                                                                           `collaboration_id`
    );

ALTER TABLE `Order` ADD CONSTRAINT `FK_User_TO_Order_1` FOREIGN KEY (
                                                                     `user_id`
    )
    REFERENCES `User` (
                       `user_id`
        );

ALTER TABLE `Order` ADD CONSTRAINT `FK_Product_TO_Order_1` FOREIGN KEY (
                                                                         `product_id`
    )
    REFERENCES `Product` (
                           `product_id`
        );

ALTER TABLE `Wish_list` ADD CONSTRAINT `FK_Product_TO_Wish_list_1` FOREIGN KEY (
                                                                                 `product_id`
    )
    REFERENCES `Product` (
                           `product_id`
        );

ALTER TABLE `Wish_list` ADD CONSTRAINT `FK_User_TO_Wish_list_1` FOREIGN KEY (
                                                                             `user_id`
    )
    REFERENCES `User` (
                       `user_id`
        );

ALTER TABLE `product_detail` ADD CONSTRAINT `FK_Product_TO_product_detail_1` FOREIGN KEY (
                                                                                           `product_id`
    )
    REFERENCES `Product` (
                           `product_id`
        );

ALTER TABLE `event` ADD CONSTRAINT `FK_collaboration_product_TO_event_1` FOREIGN KEY (
                                                                                        `collaboration_id`
    )
    REFERENCES `collaboration_product` (
                                        `collaboration_id`
        );

ALTER TABLE `collaboration_product` ADD CONSTRAINT `FK_Product_TO_collaboration_product_1` FOREIGN KEY (
                                                                                                         `product_id`
    )
    REFERENCES `Product` (
                           `product_id`
        );

ALTER TABLE `collaboration_product` ADD CONSTRAINT `FK_Brand_TO_collaboration_product_1` FOREIGN KEY (
                                                                                                       `brand_id`
    )
    REFERENCES `Brand` (
                         `brand_id`
        );

