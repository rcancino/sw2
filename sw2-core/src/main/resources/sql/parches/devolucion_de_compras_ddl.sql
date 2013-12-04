
drop table if exists SX_INVENTARIO_DEC;

drop table if exists SX_DEVOLUCION_COMPRAS

create table SX_DEVOLUCION_COMPRAS (
DEVOLUCION_ID varchar(255) not null
, PROVEEDOR_ID bigint not null
, SUCURSAL_ID bigint not null
, REFERENCIA varchar(20)
, DOCUMENTO bigint
, FECHA date not null
, CLAVE varchar(4) not null
, NOMBRE varchar(250) not null
, COMENTARIO varchar(255)
, TX_IMPORTADO datetime
, TX_REPLICADO datetime
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, CREATED_IP varchar(255)
, CREATED_MAC varchar(255)
, UPDATED_IP varchar(255)
, UPDATED_MAC varchar(255)
, version integer not null
, primary key (DEVOLUCION_ID)) ENGINE=InnoDB

create table SX_INVENTARIO_DEC (INVENTARIO_ID varchar(255) not null, ALMACEN_ID bigint, CANTIDAD double precision not null, CLAVE varchar(10) not null, COMENTARIO varchar(250), COSTO numeric(14,6) not null, COSTOP numeric(14,6) not null, COSTOU numeric(14,6) not null, CREADO time, CREADO_USERID varchar(255), DESCRIPCION varchar(250) not null, DOCUMENTO bigint, EXISTENCIA DOUBLE default 0 not null, FACTORU double precision not null, FECHA datetime not null, KILOS double precision, MODIFICADO time, NACIONAL bit not null, RENGLON integer, MODIFICADO_USERID varchar(255), version integer not null,SUCURSAL_ID bigint not null, PRODUCTO_ID bigint not null, UNIDAD_ID varchar(3) not null, DEVOLUCION_ID varchar(255) not null, primary key (INVENTARIO_ID)) ENGINE=InnoDB


alter table SX_DEVOLUCION_COMPRAS add index FKB54776B8932176D0 (PROVEEDOR_ID), add constraint FKB54776B8932176D0 foreign key (PROVEEDOR_ID) references SX_PROVEEDORES (PROVEEDOR_ID)

alter table SX_DEVOLUCION_COMPRAS add index FKB54776B820C3FF1F (SUCURSAL_ID), add constraint FKB54776B820C3FF1F foreign key (SUCURSAL_ID) references SW_SUCURSALES (SUCURSAL_ID)

alter table SX_INVENTARIO_DEC add index FKBCF7632EC79C5677 (DEVOLUCION_ID), add constraint FKBCF7632EC79C5677 foreign key (DEVOLUCION_ID) references SX_DEVOLUCION_COMPRAS (DEVOLUCION_ID)

alter table SX_INVENTARIO_DEC add index FKE13E8F911023FC44bcf7632e (UNIDAD_ID), add constraint FKE13E8F911023FC44bcf7632e foreign key (UNIDAD_ID) references SX_UNIDADES (UNIDAD)

alter table SX_INVENTARIO_DEC add index FKE13E8F91F14A8924bcf7632e (PRODUCTO_ID), add constraint FKE13E8F91F14A8924bcf7632e foreign key (PRODUCTO_ID)references SX_PRODUCTOS (PRODUCTO_ID)

alter table SX_INVENTARIO_DEC add index FKE13E8F9120C3FF1Fbcf7632e (SUCURSAL_ID), add constraint FKE13E8F9120C3FF1Fbcf7632e foreign key (SUCURSAL_ID)references SW_SUCURSALES (SUCURSAL_ID)


