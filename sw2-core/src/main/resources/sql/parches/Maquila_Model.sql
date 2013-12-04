
alter table SX_INVENTARIO_MAQ add column COSTO_CORTE numeric(16,6) default 0

alter table SX_INVENTARIO_MAQ add column COSTO_FLETE numeric(16,6) default 0

alter table SX_INVENTARIO_MAQ add column COSTO_MP numeric(16,6) default 0

alter table SX_INVENTARIO_MAQ add column RECEPCION_ID varchar(255)


drop table if exists SX_RECEPCION_MAQUILA

create table SX_RECEPCION_MAQUILA (ID varchar(255) not null, comentario varchar(100), DOCUMENTO bigint, FECHA date not null, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255), REMISION varchar(25) not null, version integer not null, SUCURSAL_ID bigint not null, PROVEEDOR_ID bigint not null, primary key (ID)) ENGINE=InnoDB

alter table SX_INVENTARIO_MAQ add index FKBCF78489B86B98EC (RECEPCION_ID), add constraint FKBCF78489B86B98EC foreign key (RECEPCION_ID) references SX_RECEPCION_MAQUILA (ID)

alter table SX_RECEPCION_MAQUILA add index FK409DC18D932176D0 (PROVEEDOR_ID), add constraint FK409DC18D932176D0 foreign key (PROVEEDOR_ID) references SX_PROVEEDORES (PROVEEDOR_ID)

alter table SX_RECEPCION_MAQUILA add index FK409DC18D20C3FF1F (SUCURSAL_ID), add constraint FK409DC18D20C3FF1F foreign key (SUCURSAL_ID) references SW_SUCURSALES (SUCURSAL_ID)

alter table SX_RECEPCION_MAQUILA add column createdIp varchar(255)

alter table SX_RECEPCION_MAQUILA add column createdMac varchar(255)

alter table SX_RECEPCION_MAQUILA add column updatedIp varchar(255)

alter table SX_RECEPCION_MAQUILA add column updatedMac varchar(255)


DROP TRIGGER ACTUALIZA_EXISTENCIA_MAQ_AU

CREATE TRIGGER ACTUALIZA_EXISTENCIA_MAQ_AU
AFTER  update ON sx_inventario_maq
FOR EACH ROW
BEGIN
call ACTUALIZA_EXIS(NEW.CLAVE,NEW.SUCURSAL_ID);
 END


create table SX_MAQ_SALIDA_HOJEADODET (SALIDADET_ID bigint not null auto_increment, CANTIDAD double precision, COMENTARIO varchar(255), COSTO double precision, FECHA datetime not null, RECEPCIONDET_ID bigint not null, INVENTARIO_ID varchar(255) not null, PRODUCTO_ID bigint not null, primary key (SALIDADET_ID)) ENGINE=InnoDB


alter table SX_MAQ_ENTRADASDET add index FK40554B9BC73C5394 (ENTRADA_ID), add constraint FK40554B9BC73C5394 foreign key (ENTRADA_ID) references SX_MAQ_ENTRADAS (ENTRADA_ID)

alter table SX_MAQ_SALIDA_HOJEADODET add index FK451F8A4F14A8924 (PRODUCTO_ID), add constraint FK451F8A4F14A8924 foreign key (PRODUCTO_ID) references SX_PRODUCTOS (PRODUCTO_ID)

alter table SX_MAQ_SALIDA_HOJEADODET add index FK451F8A4C5696C36 (RECEPCIONDET_ID), add constraint FK451F8A4C5696C36 foreign key (RECEPCIONDET_ID) references SX_MAQ_RECEPCION_CORTEDET (RECEPCIONDET_ID)

alter table SX_MAQ_SALIDA_HOJEADODET add index FK451F8A4E7B5DFA4 (INVENTARIO_ID), add constraint FK451F8A4E7B5DFA4 foreign key (INVENTARIO_ID) references SX_INVENTARIO_MAQ (INVENTARIO_ID)


