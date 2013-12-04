
drop table if exists SX_MAQ_SALIDA_BOBINAS;

create table SX_MAQ_SALIDA_BOBINAS (
SALIDA_ID bigint not null auto_increment
, FECHA datetime not null
, CANTIDAD double precision default 0
, COSTO double precision default 0
, PRODUCTO_ID bigint not null
, ENTRADADET_ID bigint not null
, INVENTARIO_ID varchar(255) not null
, COMENTARIO varchar(255)
, version integer not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, createdIp varchar(255)
, createdMac varchar(255)
, updatedIp varchar(255)
, updatedMac varchar(255)
, primary key (SALIDA_ID)) ENGINE=InnoDB


alter table SX_MAQ_SALIDA_BOBINAS 
    add index FK258B17514D9BB4A0 (ENTRADADET_ID),
    add constraint FK258B17514D9BB4A0 foreign key (ENTRADADET_ID) 
    references SX_MAQ_ENTRADASDET (ENTRADADET_ID)

alter table SX_MAQ_SALIDA_BOBINAS 
    add index FK258B1751F14A8924 (PRODUCTO_ID)
    ,add constraint FK258B1751F14A8924 foreign key (PRODUCTO_ID) 
    references SX_PRODUCTOS (PRODUCTO_ID)

alter table SX_MAQ_SALIDA_BOBINAS 
    add index FK258B1751E7B5DFA4 (INVENTARIO_ID)
    ,add constraint FK258B1751E7B5DFA4 foreign key (INVENTARIO_ID) 
    references SX_INVENTARIO_MAQ (INVENTARIO_ID)
