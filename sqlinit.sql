-- 某些字段会不会去获取是根据数据的来源决定的。
CREATE TABLE release_game_2021
(
    id              serial       NOT NULL,
    game_name       varchar(512) NOT NULL,
    item_origin     varchar(12)  NOT NULL,
    item_link       varchar(256) NOT NULL,
    patch_zh_date   date         NULL,
    patch_en_date   date         NULL,
    release_date    date         NULL,
    game_price      int          NULL,
    handle_complete boolean      NOT NULL default false,
    PRIMARY KEY (id)
);


comment on column release_game_2021.id is '主键';
comment on column release_game_2021.game_name is '游戏名，原名';
comment on column release_game_2021.item_origin is '此条数据来源';
comment on column release_game_2021.item_link is '此条数据指向的链接';
comment on column release_game_2021.patch_zh_date is '中文补丁发布时间（最新的）';
comment on column release_game_2021.patch_en_date is '英文补丁发布时间（最新的）';
comment on column release_game_2021.release_date is '作品发售时间';
comment on column release_game_2021.game_price is '游戏价格，整数，单位日元。';
comment on column release_game_2021.handle_complete is '此条数据已经处理完成，该填充的都填充了';
comment on table release_game_2021 is '2021年发售游戏表';
