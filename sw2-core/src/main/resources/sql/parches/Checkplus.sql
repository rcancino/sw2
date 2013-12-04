create table SX_CHECKPLUS_OPCION (
ID varchar(255) not null
, PLAZO integer not null
, CARGO numeric(19,2) not null
, comentario varchar(255)
, version integer not null
, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255)
, createdIp varchar(255), createdMac varchar(255), updatedIp varchar(255), updatedMac varchar(255)
, primary key (ID)) ENGINE=InnoDB

alter table SX_PEDIDOS add column CHECKPLUS_ID varchar(255)

alter table SX_PEDIDOS add index FKB976D9D45A3B6F5A (CHECKPLUS_ID)
, add constraint FKB976D9D45A3B6F5A foreign key (CHECKPLUS_ID) 
	references SX_CHECKPLUS_OPCION (ID)
	
alter table sx_clientes_credito add column CHECKPLUS  bit default false

create table SX_CHECKPLUS_VENTA (
ID varchar(255) not null
, CARGO_ID varchar(255) not null
, RAZON_SOCIAL varchar(255) not null
, CARGO numeric(19,2) not null
, PLAZO integer not null
, ATENDIO_CHECKPLUS varchar(255) not null
, CLAVE_AUTORIZACION varchar(255) not null
, FECHA_PROTECCION datetime not null
, BANCO varchar(100) not null
, NUMERO_DE_CHEQUE varchar(255) not null
, NUMERO_DE_CUENTA varchar(255) not null
, IDENTIFICACION_FOLIO varchar(100) not null
, IDENTIFICACION_TIPO varchar(50) not null
, TELEFONO varchar(30) not null
, CALLE varchar(150), CIUDAD varchar(150), COLONIA varchar(100), CP varchar(6), ESTADO varchar(150), LOCALE varchar(255), DELMPO varchar(150), NUMERO varchar(10), NUMEROINT varchar(10), PAIS varchar(255)
, version integer not null
, CREADO datetime not null, CREADO_USR varchar(255) not null, MODIFICADO datetime not null, MODIFICADO_USR varchar(255) not null
, createdIp varchar(255) not null, createdMac varchar(255) not null, updatedIp varchar(255) not null, updatedMac varchar(255) not null
, primary key (ID)) ENGINE=InnoDB

alter table SX_CHECKPLUS_VENTA add index FKC8152055B7FDD195 (CARGO_ID), add constraint FKC8152055B7FDD195 foreign key (CARGO_ID) references SX_VENTAS (CARGO_ID)

drop table SX_CHECKPLUS_DOCTOS

drop table SX_CHECKPLUS_REFBANCOS

drop table SX_CHECKPLUS_CLIENTE

create table SX_CHECKPLUS_CLIENTE (ID varchar(255) not null
, CLIENTE_ID bigint  not null unique
, NOMBRE varchar(255) not null
, FISICA bit not null, RFC varchar(13) not null, CURP varchar(18) not null
, TELEFONO1 varchar(30), TELEFONO2 varchar(30), FAX varchar(30)
, EMAIL varchar(255)
, LINEA_SOLICITADA numeric(19,2) not null
, LINEA numeric(19,2) not null
, AUTORIZACION datetime 
, DIGITALIZACION blob
, CALLE varchar(150), CIUDAD varchar(150), COLONIA varchar(100), CP varchar(6), ESTADO varchar(150), LOCALE varchar(255), DELMPO varchar(150), NUMERO varchar(10), NUMEROINT varchar(10), PAIS varchar(255)
, COMENTARIO varchar(255)
, AUTORIZACION_REFERENCIA varchar(255)
, SUSPENDIDO bit default false
, SUSPENDIDO_COMENTARIO varchar(255)
, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255)
, createdIp varchar(255), createdMac varchar(255), updatedIp varchar(255), updatedMac varchar(255)
, version integer not null
, primary key (ID)) ENGINE=InnoDB

create table SX_CHECKPLUS_DOCTOS (ID varchar(255) not null
, CLIENTE_CHECKPLUS_ID varchar(255) not null
, DESCRIPCION varchar(255) not null
, tipo varchar(40) not null
, URL varchar(255)
, version integer not null
, primary key (ID)) ENGINE=InnoDB

create table SX_CHECKPLUS_REFBANCOS (ID varchar(255) not null
, CLIENTE_CHECKPLUS_ID varchar(255) not null
, BANCO_ID bigint not null
, NUMERO_DE_CUENTA varchar(255) not null, SUCURSAL varchar(255) not null, EJECUTIVO varchar(255) not null, FECHA_APERTURA date not null, TELEFONO varchar(30) not null, version integer not null
, CALLE varchar(150), CIUDAD varchar(150), COLONIA varchar(100), CP varchar(6), ESTADO varchar(150), LOCALE varchar(255), DELMPO varchar(150), NUMERO varchar(10), NUMEROINT varchar(10), PAIS varchar(255)
, primary key (ID)) ENGINE=InnoDB



alter table SX_CHECKPLUS_CLIENTE add index FK35DCD9C32F08CED0 (CLIENTE_ID), add constraint FK35DCD9C32F08CED0 foreign key (CLIENTE_ID) references SX_CLIENTES (CLIENTE_ID)

alter table SX_CHECKPLUS_DOCTOS add index FK1C5F9C17D8684A1B (CLIENTE_CHECKPLUS_ID), add constraint FK1C5F9C17D8684A1B foreign key (CLIENTE_CHECKPLUS_ID) references SX_CHECKPLUS_CLIENTE (ID)

alter table SX_CHECKPLUS_REFBANCOS add index FK665C96F4D8684A1B (CLIENTE_CHECKPLUS_ID), add constraint FK665C96F4D8684A1B foreign key (CLIENTE_CHECKPLUS_ID) references SX_CHECKPLUS_CLIENTE (ID)

alter table SX_CHECKPLUS_REFBANCOS add index FK665C96F45AF28571 (BANCO_ID), add constraint FK665C96F45AF28571 foreign key (BANCO_ID) references SW_BANCOS (BANCO_ID)


