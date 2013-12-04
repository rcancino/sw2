create table SX_SOLICITUD_MODIFICACIONES (
ID varchar(255) not null
, SUCURSAL_ID bigint not null
, FECHA date not null
, FOLIO bigint
, MODULO varchar(255) not null
, TIPO varchar(100) not null
, ESTADO varchar(255) not null
, DESCRIPCION varchar(400) not null
, COMENTARIO varchar(255)
, USER_ID bigint not null
, AUTORIZACION datetime
, COMENTARIO_AUTORIZACION varchar(255)
, COMENTARIO_ATENCION varchar(255)
, AUTORIZO_USER_ID bigint
, ATENDIO_USER_ID bigint
, DOCUMENTO varchar(255)
, DOCUMENTO_DESCRIPCION varchar(600)
, version integer not null
, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255)
, createdIp varchar(255), createdMac varchar(255), updatedIp varchar(255), updatedMac varchar(255)
, primary key (ID)) ENGINE=InnoDB

alter table SX_SOLICITUD_MODIFICACIONES add index FK6A724470F64DE75B (AUTORIZO_USER_ID), add constraint FK6A724470F64DE75B foreign key (AUTORIZO_USER_ID) references SX_USUARIOS (id)

alter table SX_SOLICITUD_MODIFICACIONES add index FK6A724470E94D71BF (USER_ID),add constraint FK6A724470E94D71BF foreign key (USER_ID) references SX_USUARIOS (id)

alter table SX_SOLICITUD_MODIFICACIONES add index FK6A72447020C3FF1F (SUCURSAL_ID), add constraint FK6A72447020C3FF1F foreign key (SUCURSAL_ID) references SW_SUCURSALES (SUCURSAL_ID)
