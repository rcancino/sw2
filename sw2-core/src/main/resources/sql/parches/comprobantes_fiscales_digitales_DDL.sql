create table SX_CFD (
CFD_ID varchar(255) not null
, TIPO varchar(15) not null
, SERIE varchar(15) not null
, FOLIO varchar(20) not null
, ANO_APROBACION integer not null
, XML_PATH varchar(255) not null unique
, XML_SCHEMA_VERSION varchar(5) not null
, NO_APROBACION integer not null
, NO_CERTIFICADO varchar(20) not null
, ORIGEN_ID varchar(255) not null
, RECEPTOR varchar(255) not null
, EMISOR varchar(255) not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, TX_REPLICADO datetime
, TX_IMPORTADO datetime
, version integer not null
, primary key (CFD_ID)) ENGINE=InnoDB

create table SX_CFD_CERTIFICADOS (CERTIFICADO_ID varchar(255) not null, COMENTARIO varchar(255), EXPEDICION date not null, TX_IMPORTADO datetime, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255), NO_CERTIFICADO varchar(20) not null unique, TX_REPLICADO datetime, CERTIFICADO_PATH varchar(255) not null, VENCIMIENTO date not null, version integer not null, primary key (CERTIFICADO_ID)) ENGINE=InnoDB

create table SX_CFD_FOLIOS (SERIE varchar(15) not null, SUCURSAL_ID bigint not null, ANO_APROBACION integer not null, ASIGNACION date not null, FOLIO bigint not null, FOLIO_FIN bigint not null, FOLIO_INI bigint not null, TX_IMPORTADO datetime, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255), NO_APROBACION integer not null, TX_REPLICADO datetime, version integer not null, primary key (SERIE, SUCURSAL_ID), unique (SUCURSAL_ID, SERIE)) ENGINE=InnoDB

