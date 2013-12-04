drop table sx_corte_tarjetas_det

drop table sx_corte_tarjetas

create table SX_CORTE_TARJETAS (
  CORTE_ID bigint not null auto_increment
, COMENTARIO varchar(250)
, FECHA_CORTE date
, FECHA date not null
, TARJETA_TIPO varchar(20) not null
, TOTAL numeric(19,2) not null
, CARGOABONO_ID bigint
, CUENTA_ID bigint not null
, SUCURSAL_ID bigint not null
, version integer not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, createdIp varchar(255)
, createdMac varchar(255)
, updatedIp varchar(255)
, updatedMac varchar(255)
, primary key (CORTE_ID)) ENGINE=InnoDB

create table SX_CORTE_TARJETASDET (
CORTEDET_ID bigint not null auto_increment
, CARGOABONO_ID bigint, CORTE_ID bigint not null
, ABONO_ID varchar(255) not null unique
, COMENTARIO varchar(250)
, version integer not null
, primary key (CORTEDET_ID)) ENGINE=InnoDB

alter table SX_CORTE_TARJETAS add index FK4B95608568F1D83 (CARGOABONO_ID), add constraint FK4B95608568F1D83 foreign key (CARGOABONO_ID) references SW_BCARGOABONO (CARGOABONO_ID)

alter table SX_CORTE_TARJETAS  add index FK4B9560820C3FF1F (SUCURSAL_ID),     add constraint FK4B9560820C3FF1F foreign key (SUCURSAL_ID)   references SW_SUCURSALES (SUCURSAL_ID)

alter table SX_CORTE_TARJETAS add index FK4B95608EEF94E03 (CUENTA_ID), add constraint FK4B95608EEF94E03 foreign key (CUENTA_ID) references SW_CUENTAS (id)

alter table SX_CORTE_TARJETASDET add index FKBBC394EBD1B80842 (CORTE_ID), add constraint FKBBC394EBD1B80842 foreign key (CORTE_ID) references SX_CORTE_TARJETAS(CORTE_ID)

alter table SX_CORTE_TARJETASDET add index FKBBC394EBE8DA1B54 (ABONO_ID), add constraint FKBBC394EBE8DA1B54 foreign key (ABONO_ID) references SX_CXC_ABONOS (ABONO_ID)


alter table SX_FICHAS add column CARGOABONO_ID bigint

alter table SX_FICHAS add index FKE49EE1B4568F1D83 (CARGOABONO_ID), add constraint FKE49EE1B4568F1D83 foreign key (CARGOABONO_ID) references SW_BCARGOABONO (CARGOABONO_ID)

alter table SX_FICHAS add column FECHA_CORTE datetime

alter table SX_FICHAS add column CANCELADA datetime

alter table SW_BCARGOABONO add column PAGO_ID varchar(255)

alter table SW_BCARGOABONO add index FK9867A4DA7A35E5F (PAGO_ID), add constraint FK9867A4DA7A35E5F foreign key (PAGO_ID) references SX_CXC_ABONOS (ABONO_ID)
