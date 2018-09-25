
create table map_background_name (
        area_id int not null,
        info_no int not null,
        suffix int not null,
        priority int not null,
        image_name varchar(32) not null,
        version smallint not null,
        primary key(area_id, info_no, suffix, image_name, version)
) engine = ARIA, default charset=utf8mb4;
