alter table SX_PEDIDOS add column ANTICIPO bit default false

alter table SX_VENTAS add column ANTICIPO bit default false


alter table SX_VENTAS add column ANTICIPO_APLICADO numeric(19,2) default 0

alter table SX_ANTICIPOS_APLICADOS 
        drop  foreign key FKE157E23178BD7250

alter table SX_ANTICIPOS_APLICADOS 
        drop  foreign key FKE157E231B7FDD195

drop table if exists SX_ANTICIPOS_APLICADOS

create table SX_ANTICIPOS_APLICADOS (
APLICACION_ID varchar(255) not null
, ORIGEN_ID varchar(255) not null
, CARGO_ID varchar(255) not null
, APLICADO numeric(16,2) not null
, version integer not null
, primary key (APLICACION_ID))
 ENGINE=InnoDB

alter table SX_ANTICIPOS_APLICADOS 
    add index FKE157E23178BD7250 (ORIGEN_ID), 
    add constraint FKE157E23178BD7250 
    foreign key (ORIGEN_ID) references SX_VENTAS (CARGO_ID)

alter table SX_ANTICIPOS_APLICADOS 
    add index FKE157E231B7FDD195 (CARGO_ID), 
    add constraint FKE157E231B7FDD195 
    foreign key (CARGO_ID) references SX_VENTAS (CARGO_ID)
alter table SX_PEDIDOS add column ANTICIPO bit default false





alter table SX_ANTICIPOS_APLICADOS 
        drop  foreign key FKE157E23178BD7250

alter table SX_ANTICIPOS_APLICADOS 
        drop  foreign key FKE157E231B7FDD195

drop table if exists SX_ANTICIPOS_APLICADOS

create table SX_ANTICIPOS_APLICADOS (
APLICACION_ID varchar(255) not null
, ORIGEN_ID varchar(255) not null
, CARGO_ID varchar(255) not null
, APLICADO numeric(16,2) not null
, version integer not null
, primary key (APLICACION_ID))
 ENGINE=InnoDB

alter table SX_ANTICIPOS_APLICADOS 
    add index FKE157E23178BD7250 (ORIGEN_ID), 
    add constraint FKE157E23178BD7250 
    foreign key (ORIGEN_ID) references SX_VENTAS (CARGO_ID)

alter table SX_ANTICIPOS_APLICADOS 
    add index FKE157E231B7FDD195 (CARGO_ID), 
    add constraint FKE157E231B7FDD195 
    foreign key (CARGO_ID) references SX_VENTAS (CARGO_ID)
