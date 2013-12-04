drop table if exists SX_SOLICITUDES_DEPOSITO

create table SX_SOLICITUDES_DEPOSITO (
SOL_ID varchar(255) not null
, SUCURSAL_ID bigint not null
, DOCUMENTO bigint not null
, FECHA datetime not null
, FECHA_DEPOSITO date not null
, CLAVE varchar(7) not null
, NOMBRE varchar(255) not null
, CHEQUE numeric(19,2) not null default 0.0
, TRANSFERENCIA numeric(19,2) not null default 0.0
, EFECTIVO numeric(19,2) not null default 0.0
, TOTAL numeric(19,2) not null
, COMENTARIO varchar(255)
, ANTICIPO bit not null default false
, CANCELACION datetime
, COMENTARIO_CANCELACION varchar(250) 
, REFERENCIA varchar(20) 
, SOLICITA varchar(25) not null
, ORIGEN varchar(3) not null
, SALVO_COBRO bit
, ABONO_ID varchar(255)
, BANCO_ID bigint not null
, CUENTA_ID bigint not null
, CLIENTE_ID bigint not null
, TX_IMPORTADO datetime
, TX_REPLICADO datetime
, version integer not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, createdIp varchar(255)
, createdMac varchar(255)
, updatedIp varchar(255)
, updatedMac varchar(255)
, primary key (SOL_ID)) ENGINE=InnoDB


alter table SX_SOLICITUDES_DEPOSITO add index FKA98C62982F08CED0 (CLIENTE_ID), add constraint FKA98C62982F08CED0 foreign key (CLIENTE_ID) references SX_CLIENTES (CLIENTE_ID)

alter table SX_SOLICITUDES_DEPOSITO add index FKA98C629820C3FF1F (SUCURSAL_ID),add constraint FKA98C629820C3FF1F foreign key (SUCURSAL_ID) references SW_SUCURSALES (SUCURSAL_ID)

alter table SX_SOLICITUDES_DEPOSITO add index FKA98C62985AF28571 (BANCO_ID), add constraint FKA98C62985AF28571 foreign key (BANCO_ID) references SW_BANCOS (BANCO_ID)

alter table SX_SOLICITUDES_DEPOSITO add index FKA98C6298F562F4BF (ABONO_ID), add constraint FKA98C6298F562F4BF foreign key (ABONO_ID) references SX_CXC_ABONOS (ABONO_ID)

alter table SX_SOLICITUDES_DEPOSITO add index FKA98C6298EEF94E03 (CUENTA_ID), add constraint FKA98C6298EEF94E03 foreign key (CUENTA_ID) references SW_CUENTAS (id)
