
create table ship_image_2nd (
        id int not null,
        image mediumblob not null,
        member_id bigint not null,
        kind varchar(32) not null,
        version smallint not null,
        primary key (id, kind, version)
) engine = ARIA, default charset=utf8mb4;
