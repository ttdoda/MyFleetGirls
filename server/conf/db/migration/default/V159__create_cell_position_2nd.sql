
create table cell_position_2nd (
        area_id int not null,
        info_no int not null,
        cell int not null,
        pos_x int not null,
        pos_y int not null,
        version smallint not null,
        primary key(area_id, info_no, cell, version)
) engine = ARIA, default charset=utf8mb4;

create table map_data (
        area_id int not null,
        info_no int not null,
        name varchar(32) not null,
        frame_x int not null,
        frame_y int not null,
        frame_w int not null,
        frame_h int not null,
        version smallint not null,
        primary key(area_id, info_no, name, version)
) engine = ARIA, default charset=utf8mb4;

create table map_image_2nd (
        area_id int not null,
        info_no int not null,
        image mediumblob not null,
        version smallint not null,
        primary key (area_id, info_no, version)
) engine = ARIA, default charset=utf8mb4;
