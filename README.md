### 说明

本工程为 2021 年统计**日本** Galgame 业界作品信息的工程。其中能统计的有：

1. 2016~至今 vndb 中的有中文补丁/英文补丁的游戏
2. 2020年之后 Getchu 所有新发售 Galgame
3. 2020年之后 FanzaGames 所有新发售的 Galgame，排除了3D CG类型的游戏
4. 2020年之后 DLsite 所有新发售的 Galgame，排除了tag中包含有“3D作品”的游戏

<br/>
本工程的使用流程以及目的是：通过爬虫获得以上列出的数据，然后进行基本的清洗/统计，最终导出为Excel文档

<br/>
<br/>

### 入口

**HandleMain.kt** 是主入口文件。运行其中的多个 `main()` 即可实现数据抓取、Excel导出。

注： 所有数据抓取的处理流程都是幂等的

<br/>
<br/>

### 数据归档模型

某些字段会不会去获取是根据数据的来源决定的。  
如果 **item_origin** 字段为 vndb，那么 game_price 就必定为空。  
如果是其他几样，那么 patch_zh_date、patch_en_date 就必定为空。

```sql
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
```

使用的数据库为 **PostgreSQL**


<br/>
<br/>