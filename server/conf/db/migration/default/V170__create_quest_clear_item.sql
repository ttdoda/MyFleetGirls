
create table quest_clear_item(
        id bigint not null auto_increment primary key,
        member_id bigint not null,
        quest_id int not null,
        bounus_count int not null,
        `type` int not null,
        `count` int not null,
        item_id int,
        item_name tinytext,
        created bigint not null,
        index(member_id, `type`)
) engine = ARIA default charset=utf8mb4;
