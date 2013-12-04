
create table SX_PRODUCTOS_CLASIFICACION(
ID varchar(255) not null
, CLASIFICACION varchar(255) unique
, primary key (ID)) 
ENGINE=InnoDB;

alter table sx_productos add column CLASIFICACION VARCHAR(50);

update sx_productos set CLASIFICACION='SIN_CLASIFICAR';

insert into sx_productos_clasificacion values( uuid(),'FCC_100');
insert into sx_productos_clasificacion values( uuid(),'SIN_CLASIFICAR');

create table SX_VENTA_ESTADO (
ID varchar(255) not null
, VENTA_ID varchar(255) not null unique
, CORTADO datetime
, SURTIDO datetime
, version integer not null
, createdIp varchar(255) , createdMac varchar(255) , updatedIp varchar(255) , updatedMac varchar(255) 
, CREADO datetime , CREADO_USR varchar(255) , MODIFICADO datetime , MODIFICADO_USR varchar(255) 
, primary key (ID)) ENGINE=InnoDB


alter table SX_VENTA_ESTADO add index FK913A5A737BC7B6E (VENTA_ID)
, add constraint FK913A5A737BC7B6E foreign key (VENTA_ID) 
references SX_VENTAS (CARGO_ID)
