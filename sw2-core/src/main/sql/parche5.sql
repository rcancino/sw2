
********modificacion a la tabla de sx_clientes_descuentos 


drop table if exists SX_CLIENTES_DESCUENTOS;
create table SX_CLIENTES_DESCUENTOS (
CLIENTE_ID bigint not null
, ACTIVO bit not null
, DESCRIPCION varchar(100) not null
, DESCUENTO double precision not null
, orden integer not null
, primary key (CLIENTE_ID, ACTIVO, DESCRIPCION, DESCUENTO, orden)
) 
ENGINE=InnoDB

drop table if exists  SX_productos_DESCUENTOS;
create table SX_PRODUCTOS_DESCUENTOS (
PRODUCTO_ID bigint not null
, ACTIVO bit not null
, DESCRIPCION varchar(100) not null
, DESCUENTO double precision not null
,orden integer not null
, primary key (PRODUCTO_ID, ACTIVO, DESCRIPCION, DESCUENTO, orden)
) ENGINE=InnoDB

alter table SX_CLIENTES_DESCUENTOS add index FK5E4283F72F08CED0 (CLIENTE_ID), add constraint FK5E4283F72F08CED0 foreign key (CLIENTE_ID) references SX_CLIENTES (CLIENTE_ID)

alter table SX_PRODUCTOS_DESCUENTOS add index FK27D598F1F14A8924 (PRODUCTO_ID),add constraint FK27D598F1F14A8924 foreign key (PRODUCTO_ID) references SX_PRODUCTOS (PRODUCTO_ID)