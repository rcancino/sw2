
drop table if exists SX_MAQ_ENTRADAS_ANALIZADAS

drop table if exists SX_MAQ_ANALISI_MAT

create table SX_MAQ_ANALISI_MAT (
  ANALISIS_ID bigint not null auto_increment
, CLAVE varchar(4) not null
, NOMBRE varchar(250) not null
, FACTURA varchar(20) not null
, FECHA date not null
, MONEDA varchar(3) not null
, TC double precision not null
, IMPORTE numeric(19,2) not null
, IMPUESTO numeric(19,2) not null
, TOTAL numeric(19,2) not null
, COMENTARIO varchar(250)
, PROVEEDOR_ID bigint not null
, version integer not null
, createdIp varchar(255)
, createdMac varchar(255)
, updatedIp varchar(255)
, updatedMac varchar(255)
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, primary key (ANALISIS_ID)) ENGINE=InnoDB

create table SX_MAQ_ENTRADAS_ANALIZADAS (
    ENTRADADET_ID bigint not null
, ANALISIS_ID bigint
, primary key (ENTRADADET_ID))ENGINE=InnoDB

alter table SX_MAQ_ANALISI_MAT add index FK3DA8136C932176D0 (PROVEEDOR_ID), add constraint FK3DA8136C932176D0 foreign key (PROVEEDOR_ID) references SX_PROVEEDORES (PROVEEDOR_ID)

alter table SX_MAQ_ENTRADAS_ANALIZADAS add index FKECD294A589DA72C5 (ENTRADADET_ID), add constraint FKECD294A589DA72C5 foreign key (ENTRADADET_ID) references SX_MAQ_ENTRADASDET (ENTRADADET_ID)

alter table SX_MAQ_ENTRADAS_ANALIZADAS add index FKECD294A545B51150 (ANALISIS_ID), add constraint FKECD294A545B51150 foreign key (ANALISIS_ID) references SX_MAQ_ANALISI_MAT (ANALISIS_ID)

alter table SX_INVENTARIO_MAQ add column ANALISIS_FLETE_ID bigint

drop table if exists SX_MAQ_ANALISIS_FLETE

create table SX_MAQ_ANALISIS_FLETE (
ANALISIS_ID bigint not null auto_increment
, FECHA date not null
, PROVEEDOR_ID bigint not null
, NOMBRE varchar(250) not null
, CLAVE varchar(4) not null
, COMENTARIO varchar(250)
, FACTURA varchar(20) not null
, FACTURA_FECHA date not null
, IMPORTE numeric(19,2) not null
, RETENCION numeric(19,2) not null
, IMPUESTO numeric(19,2) not null
, TOTAL numeric(19,2) not null
, createdIp varchar(255)
, createdMac varchar(255)
, updatedIp varchar(255)
, updatedMac varchar(255)
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, version integer not null
, primary key (ANALISIS_ID)) 
    ENGINE=InnoDB 

alter table SX_INVENTARIO_MAQ drop  foreign key FKBCF78489CA79ECB2;

alter table SX_INVENTARIO_MAQ drop  index FKBCF78489CA79ECB2;
                                          
alter table SX_INVENTARIO_MAQ add index FKBCF78489CA79ECB2 (ANALISIS_FLETE_ID),add constraint FKBCF78489CA79ECB2 foreign key (ANALISIS_FLETE_ID) references SX_MAQ_ANALISIS_FLETE (ANALISIS_ID)

alter table SX_MAQ_ANALISIS_FLETE add index FK7393161C932176D0 (PROVEEDOR_ID), add constraint FK7393161C932176D0 foreign key (PROVEEDOR_ID) references SX_PROVEEDORES (PROVEEDOR_ID)

alter table SX_INVENTARIO_MAQ add column ANALISIS_HOJEO_ID bigint

create table SX_MAQ_ANALISIS_HOJEO (
ANALISIS_ID bigint not null auto_increment
, FECHA date not null
, PROVEEDOR_ID bigint not null
, CLAVE varchar(4) not null
, NOMBRE varchar(250) not null
, FACTURA varchar(20) not null
, FACTURA_FECHA date not null
, IMPORTE numeric(19,2) not null
, IMPUESTO numeric(19,2) not null
, TOTAL numeric(19,2) not null
, COMENTARIO varchar(250)
,createdIp varchar(255)
, createdMac varchar(255)
, updatedIp varchar(255)
, updatedMac varchar(255)
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, version integer not null
, primary key (ANALISIS_ID)) ENGINE=InnoDB

alter table SX_INVENTARIO_MAQ add index FKBCF7848940C5FB92 (ANALISIS_HOJEO_ID),add constraint FKBCF7848940C5FB92 foreign key (ANALISIS_HOJEO_ID) references SX_MAQ_ANALISIS_HOJEO (ANALISIS_ID)

alter table SX_MAQ_ANALISIS_HOJEO add index FKAE7A1656932176D0 (PROVEEDOR_ID), add constraint FKAE7A1656932176D0 foreign key (PROVEEDOR_ID) references SX_PROVEEDORES (PROVEEDOR_ID)

drop table  SX_MAQ_GASTOS_ANALIZADOS 

drop table sx_maq_analisis_gastos

alter table SX_INVENTARIO_MAQ add column ANALISIS_ID bigint

create table SX_MAQ_ANALISIS_GASTOS (ANALISIS_ID bigint not null auto_increment, createdIp varchar(255), createdMac varchar(255), updatedIp varchar(255), updatedMac varchar(255), COMENTARIO varchar(250), FACTURA_FLETE varchar(20), FACTURA_MAQUILA varchar(20), FECHA date not null, FECHA_FLETE date, FECHA_MAQUILA date, FLETE_IMPORTE numeric(19,2) not null, MAQUILA_IMPORTE numeric(19,2) not null, FLETE_IMPUESTO numeric(19,2) not null, MAQUILA_IMPUESTO numeric(19,2) not null, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255), FLETE_TOTAL numeric(19,2) not null, MAQUILA_TOTAL numeric(19,2) not null, version integer not null, primary key (ANALISIS_ID)) ENGINE=InnoDB

alter table SX_INVENTARIO_MAQ add index FKBCF78489D521BEC8 (ANALISIS_ID), add constraint FKBCF78489D521BEC8 foreign key (ANALISIS_ID) references SX_MAQ_ANALISIS_GASTOS (ANALISIS_ID)



