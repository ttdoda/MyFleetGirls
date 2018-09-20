
alter table map_data drop primary key, add suffix int not null default 0 after info_no, add primary key (area_id, info_no, suffix, name, version);

alter table cell_position_2nd drop primary key, add suffix int not null default 0 after info_no, add primary key (area_id, info_no, suffix, cell, version);

alter table map_image_2nd drop primary key, add suffix int not null default 0 after info_no, add primary key (area_id, info_no, suffix, version);
