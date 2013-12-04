
drop table sx_conteodet

drop table sx_conteo

create table SX_CONTEO (
CONTEO_ID varchar(255) not null
, DOCUMENTO bigint not null
, SUCURSAL_ID bigint not null
, FECHA date not null
, SECTOR integer
, AUDITOR1 varchar(50)
, AUDITOR2 varchar(50)
, CAPTURISTA varchar(50)
, COMENTARIO varchar(255)
, CONTADOR1 varchar(50)
, CONTADOR varchar(50)
, version integer not null
, TX_IMPORTADO datetime
, TX_REPLICADO datetime
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, createdIp varchar(255)
, createdMac varchar(255)
, updatedIp varchar(255)
, updatedMac varchar(255)
, primary key (CONTEO_ID)) ENGINE=InnoDB

create table SX_CONTEODET (
CONTEODET_ID varchar(255) not null
, DOCUMENTO bigint not null
, RNGL integer
, CLAVE varchar(255) not null
, CANTIDAD double precision
, DESCRIPCION varchar(250) not null
, FACTOR double precision not null
, KILOS double precision not null
, UNIDAD varchar(3) not null
, version integer not null
, PRODUCTO_ID bigint not null
, CONTEO_ID varchar(255) not null
, primary key (CONTEODET_ID)) ENGINE=InnoDB


alter table SX_CONTEO 
    add index FKDFDA127620C3FF1F (SUCURSAL_ID)
    , add constraint FKDFDA127620C3FF1F 
    foreign key (SUCURSAL_ID) references SW_SUCURSALES  (SUCURSAL_ID)

alter table SX_CONTEODET 
    add index FKE24B59BD671DFE2B (CONTEO_ID)
    , add constraint FKE24B59BD671DFE2B 
    foreign key (CONTEO_ID) references SX_CONTEO (CONTEO_ID)

alter table SX_CONTEODET 
    add index FKE24B59BDF14A8924 (PRODUCTO_ID)
    , add constraint FKE24B59BDF14A8924 
    foreign key (PRODUCTO_ID) references SX_PRODUCTOS (PRODUCTO_ID)

drop table sx_existencia_conteo

create table SX_EXISTENCIA_CONTEO (
EXISTENCIA_ID varchar(255) not null
, FECHA date not null
, SUCURSAL_ID bigint not null
, PRODUCTO_ID bigint not null
, CLAVE varchar(10) not null
, DESCRIPCION varchar(250) not null
, UNIDAD varchar(3) not null
, FACTORU double precision not null
, CANTIDAD double precision not null
, CONTEO double precision not null
, DIFERENCIA double precision not null
, AJUSTE double precision not null
, EXISTENCIA_FINAL double precision not null
, SECTORES varchar(255) 
, FIJADO datetime 
, version integer not null
, primary key (EXISTENCIA_ID)) ENGINE=InnoDB


alter table SX_EXISTENCIA_CONTEO 
    add index FK434D45E6F14A8924 (PRODUCTO_ID)
    , add constraint FK434D45E6F14A8924 
    foreign key (PRODUCTO_ID) references SX_PRODUCTOS (PRODUCTO_ID)

alter table SX_EXISTENCIA_CONTEO 
    add index FK434D45E620C3FF1F (SUCURSAL_ID)
    , add constraint FK434D45E620C3FF1F 
    foreign key (SUCURSAL_ID) references SW_SUCURSALES (SUCURSAL_ID)

