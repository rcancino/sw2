alter table SW_BCARGOABONO add column CLASIFICACION varchar(50);
alter table SW_BCARGOABONO add column TRASPASO_ID bigint;
 
alter table SW_BCARGOABONO drop foreign key FK9867A4DA147E942D;
alter table SX_TRASPASOS_CUENTAS drop foreign key FKB41C4B28CDDCD09A;
alter table SX_TRASPASOS_CUENTAS drop foreign key FKB41C4B28E549DD2A;

drop  table if exists SX_TRASPASOS_CUENTAS;
create table SX_TRASPASOS_CUENTAS (
TIPO varchar(20) not null
, TRASPASO_ID bigint not null auto_increment
, CUENTA_DESTINO_ID bigint not null
, CUENTA_ORIGEN_ID bigint not null
, FECHA date not null
, IMPORTE numeric(19,2) not null
, IMPUESTO numeric(19,2) not null
, COMISION numeric(19,2) not null
, MONEDA varchar(3) not null
, TC double precision not null
, REFERENCIA varchar(100)
, PLAZO integer
, TASA double precision
, ISR double precision
, RENDIMIENTO_CALCULADO numeric(19,2) 
, RENDIMIENTO_REAL numeric(19,2) 
, RENDIMIENTO_IMPUESTO numeric(19,2) 
, RENDIMIENTO_FECHA date 
, VENCIMIENTO date 
, COMENTARIO varchar(250)
, version integer not null
, primary key (TRASPASO_ID)) ENGINE=InnoDB

alter table SW_BCARGOABONO		 add index FK9867A4DA147E942D (TRASPASO_ID), 		add constraint FK9867A4DA147E942D foreign key (TRASPASO_ID) 		references SX_TRASPASOS_CUENTAS (TRASPASO_ID)
alter table SX_TRASPASOS_CUENTAS add index FKB41C4B28CDDCD09A (CUENTA_DESTINO_ID), 	add constraint FKB41C4B28CDDCD09A foreign key (CUENTA_DESTINO_ID) 	references SW_CUENTAS (id)
alter table SX_TRASPASOS_CUENTAS add index FKB41C4B28E549DD2A (CUENTA_ORIGEN_ID), 	add constraint FKB41C4B28E549DD2A foreign key (CUENTA_ORIGEN_ID) 	references SW_CUENTAS (id)


alter table SX_COMISIONES_BANCARIAS drop foreign key FK7F98943C984FADE3;
alter table SX_COMISIONES_BANCARIAS drop foreign key FK7F98943C3B6AD598;
alter table SX_COMISIONES_BANCARIAS drop foreign key FK7F98943CEEF94E03;

drop table if exists SX_COMISIONES_BANCARIAS;

create table SX_COMISIONES_BANCARIAS (
COMISION_ID bigint not null auto_increment
, CUENTA_ID bigint not null
, FECHA date not null
, COMISION numeric(19,2) not null
, IMPUESTO numeric(19,2) not null
, MONEDA varchar(3) not null
, TC double precision not null
, REFERENCIA varchar(100)
, COMENTARIO varchar(250)
, CA_COMISION_ID bigint not null
, CA_IMPUESTO_ID bigint not null
, version integer not null
, primary key (COMISION_ID)) ENGINE=InnoDB;

alter table SX_COMISIONES_BANCARIAS add index FK7F98943C984FADE3 (CA_IMPUESTO_ID), add constraint FK7F98943C984FADE3 foreign key (CA_IMPUESTO_ID) references SW_BCARGOABONO (CARGOABONO_ID);
alter table SX_COMISIONES_BANCARIAS add index FK7F98943C3B6AD598 (CA_COMISION_ID), add constraint FK7F98943C3B6AD598 foreign key (CA_COMISION_ID) references SW_BCARGOABONO (CARGOABONO_ID);
alter table SX_COMISIONES_BANCARIAS add index FK7F98943CEEF94E03 (CUENTA_ID), add constraint FK7F98943CEEF94E03 foreign key (CUENTA_ID) references SW_CUENTAS (id);

alter table SW_CONCEPTOS add column CLASE varchar(30);


drop table if exists SW_CUENTAS_SALDOS

create table SW_CUENTAS_SALDOS (
 SALDO_ID bigint not null auto_increment
, CUENTA_ID bigint not null
, MES integer not null check (MES>=1 and MES<=12)
, ANO integer not  null
, SALDO_INICIAL numeric(16,6) not null
, DEPOSITOS numeric(16,6) not null
, RETIROS numeric(16,6) not null
, SALDO_FINAL numeric(16,6) not null
, CIERRE datetime
, version integer not null
, primary key (SALDO_ID)) ENGINE=InnoDB

alter table SW_CUENTAS_SALDOS add index FK95736599EEF94E03 (CUENTA_ID), add constraint FK95736599EEF94E03 foreign key (CUENTA_ID) references SW_CUENTAS (id)
