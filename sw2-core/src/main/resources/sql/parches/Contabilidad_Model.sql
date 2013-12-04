alter table SX_CUENTAS_CONTABLES 
        drop 
        foreign key FK7BDE10B96DCB9B66;

alter table SX_POLIZAS 
        drop foreign key FKCAF85CF6FE40EE1C;    

alter table SX_POLIZASDET 
        drop foreign key FKDC4AFF3DDE49A658;

alter table SX_POLIZASDET 
        drop foreign key FKDC4AFF3DB06C29E4;

drop table if exists SX_CUENTAS_CONTABLES;

drop table if exists SX_POLIZAS_TIPOS;

drop table if exists SX_POLIZAS;

drop table if exists SX_POLIZASDET;

create table SX_CUENTAS_CONTABLES (
CUENTA_ID bigint not null auto_increment
, CLAVE varchar(20) not null unique
, TIPO varchar(20) not null
, SUB_TIPO varchar(20) not null
, DESCRIPCION varchar(255) not null
, DESCRIPCION2 varchar (255)
, PADRE_ID bigint
, DE_RESULTADO bit not null
, DETALLE bit not null
, NATURALEZA varchar(20) not null
, PRES_CONTABLE bit not null
, PRES_FINANCIERA bit not null
, PRES_FISCAL bit not null
, PRES_PRESUPUESTAL bit not null
, version integer not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, CREADO_IP varchar(255)
, CREADO_MAC varchar(255)
, MODIFICADO_IP varchar(255)
, MODIFICADO_MAC varchar(255)
, primary key (CUENTA_ID)) ENGINE=InnoDB

alter table SX_CUENTAS_CONTABLES 
    add index FK7BDE10B96DCB9B66 (PADRE_ID), 
    add constraint FK7BDE10B96DCB9B66 foreign key (PADRE_ID) 
    references SX_CUENTAS_CONTABLES (CUENTA_ID)



create table SX_POLIZAS (
POLIZA_ID bigint not null auto_increment
, TIPO_ID varchar(20) not null
, CLASE varchar(100) not null
, FOLIO bigint not null
, FECHA datetime not null
, DESCRIPCION varchar(255) not null
, DEBE numeric(16,6) not null
, HABER numeric(16,6) not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, CREADO_IP varchar(255)
, CREADO_MAC varchar(255)
, MODIFICADO_IP varchar(255)
, MODIFICADO_MAC varchar(255)
, version integer not null
, primary key (POLIZA_ID)) ENGINE=InnoDB;

create table SX_POLIZASDET (
POLIZADET_ID bigint not null auto_increment
, CUENTA_ID bigint not null
, DEBE numeric(16,6) not null
, HABER numeric(16,6) not null
, DESCRIPCION varchar(255) not null
, DESCRIPCION2 varchar(255)
, REFERENCIA varchar(255)
, REFERENCIA2 varchar(255)
, ASIENTO varchar(255)
, RENGLON integer
, POLIZA_ID bigint not null
, version integer not null
, primary key (POLIZADET_ID)) ENGINE=InnoDB;



alter table SX_POLIZASDET 
    add index FKDC4AFF3DDE49A658 (POLIZA_ID), 
    add constraint FKDC4AFF3DDE49A658 
    foreign key (POLIZA_ID)references SX_POLIZAS (POLIZA_ID);

alter table SX_POLIZASDET 
    add index FKDC4AFF3DB06C29E4 (CUENTA_ID), 
    add constraint FKDC4AFF3DB06C29E4 
    foreign key (CUENTA_ID) references SX_CUENTAS_CONTABLES (CUENTA_ID);

alter table SX_CONTABILIDAD_SALDOS 
        drop 
        foreign key FK1BEA0FBBB06C29E4;



create table SX_CONCEPTOS_CONTABLES (
CONCEPTO_ID bigint not null auto_increment
, CUENTA_ID bigint not null
, CLAVE varchar(50) not null
, DESCRIPCION varchar(255) not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, CREADO_IP varchar(255)
, CREADO_MAC varchar(255)
, MODIFICADO_IP varchar(255)
, MODIFICADO_MAC varchar(255)
, primary key (CONCEPTO_ID)) ENGINE=InnoDB

alter table SX_POLIZASDET add column CONCEPTO_ID bigint

alter table SX_CONCEPTOS_CONTABLES add index FKEBC96B1AB06C29E4 (CUENTA_ID)
, add constraint FKEBC96B1AB06C29E4 foreign key (CUENTA_ID) 
references SX_CUENTAS_CONTABLES (CUENTA_ID)

alter table SX_POLIZASDET add index FKDC4AFF3D873A2704 (CONCEPTO_ID)
, add constraint FKDC4AFF3D873A2704 foreign key (CONCEPTO_ID) 
references SX_CONCEPTOS_CONTABLES (CONCEPTO_ID)
    
drop table if exists sx_contabilidad_saldosdet;
drop table if exists sx_contabilidad_saldos;

create table SX_CONTABILIDAD_SALDOS (SALDO_ID bigint not null auto_increment
, CUENTA_ID bigint not null
, YEAR integer not null
, MES integer not null check (MES>=1 and MES<=12)
, SALDO_INICIAL numeric(16,6) not null 
, DEBE numeric(16,6) not null
, HABER numeric(16,6) not null
, SALDO_FINAL numeric(16,6) not null
, CIERRE datetime
, version integer not null
, primary key (SALDO_ID)) ENGINE=InnoDB

create table SX_CONTABILIDAD_SALDOSDET (
SALDODET_ID bigint not null auto_increment
, SALDO_ID bigint not null
, CONCEPTO_ID bigint not null
, YEAR integer not null
, MES integer not null check (MES>=1 and MES<=12)
, DEBE numeric(16,6) not null
, HABER numeric(16,6) not null
, SALDO_FINAL numeric(16,6) not null
, SALDO_INICIAL numeric(16,6) not null
, version integer not null
, primary key (SALDODET_ID), unique (CONCEPTO_ID, YEAR, MES)) ENGINE=InnoDB

alter table SX_CONTABILIDAD_SALDOS add index FK1BEA0FBBB06C29E4 (CUENTA_ID), add constraint FK1BEA0FBBB06C29E4 foreign key (CUENTA_ID) references SX_CUENTAS_CONTABLES (CUENTA_ID);
alter table SX_CONTABILIDAD_SALDOSDET add index FK6AFD9A58873A2704 (CONCEPTO_ID), add constraint FK6AFD9A58873A2704 foreign key (CONCEPTO_ID) references SX_CONCEPTOS_CONTABLES (CONCEPTO_ID);
alter table SX_CONTABILIDAD_SALDOSDET add index FK6AFD9A58445A0725 (SALDO_ID), add constraint FK6AFD9A58445A0725 foreign key (SALDO_ID) references SX_CONTABILIDAD_SALDOS (SALDO_ID);

select * from sx_contabilidad_saldosdet