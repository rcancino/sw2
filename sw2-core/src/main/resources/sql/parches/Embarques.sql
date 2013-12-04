
alter table SX_CHOFERES add column SUSPENDIDO bit default false

alter table SX_CHOFERES add column SUSPENDIDO_FECHA datetime

alter table SX_CHOFERES add column COMENTARIO varchar(255)

alter table SX_CHOFERES add column email1 varchar(100)

alter table SX_CHOFERES add column TX_IMPORTADO datetime

alter table SX_CHOFERES add column TX_REPLICADO datetime

alter table SX_CHOFERES add column CREADO datetime

alter table SX_CHOFERES add column CREADO_USR varchar(255)

alter table SX_CHOFERES add column MODIFICADO datetime

alter table SX_CHOFERES add column MODIFICADO_USR varchar(255)

alter table SX_CHOFER_FACTURISTA add column email1 varchar(100)

alter table SX_CHOFER_FACTURISTA add column TX_IMPORTADO datetime

alter table SX_CHOFER_FACTURISTA add column TX_REPLICADO datetime

alter table SX_CHOFER_FACTURISTA add column CREADO datetime

alter table SX_CHOFER_FACTURISTA add column CREADO_USR varchar(255)

alter table SX_CHOFER_FACTURISTA add column MODIFICADO datetime

alter table SX_CHOFER_FACTURISTA add column MODIFICADO_USR varchar(255)

create table SX_CHOFER_OBSERVACIONES (CHOFER_ID bigint not null, observacion varchar(255), fecha datetime) ENGINE=InnoDB

alter table SX_CHOFER_OBSERVACIONES add index FK8A25EDAFC5F9F871 (CHOFER_ID), add constraint FK8A25EDAFC5F9F871 foreign key (CHOFER_ID) references SX_CHOFERES (CHOFER_ID)
