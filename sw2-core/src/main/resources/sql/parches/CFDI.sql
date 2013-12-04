alter table SW_EMPRESAS add column NO_CERTIFICADO varchar(20);
alter table SW_EMPRESAS add column COMPROBANTE_FISCAL varchar(10);
alter table SW_EMPRESAS add column CFDI_PK mediumblob;
alter table SW_EMPRESAS add column CERTIFICADO_DIGITAL mediumblob;

drop table sx_cfdi

create table SX_CFDI (
CFD_ID varchar(255) not null
, TIPO_CFD varchar(1)
, TIPO varchar(15) not null
, SERIE varchar(15) not null
, FOLIO varchar(20) not null
, ESTADO varchar(1)
, RECEPTOR varchar(255) not null
, RFC varchar(13)
, SUBTOTAL numeric(19,2)
, IMPUESTO numeric(19,2)
, TOTAL numeric(19,2)
, ADUANA varchar(255)
, CUENTA_PREDIAL varchar(255)
, PEDIMENTO varchar(255)
, PEDIMENTO_FECHA datetime
, EMISOR varchar(255) not null
, NO_CERTIFICADO varchar(20) not null
, TIMBRADO varchar(50)
, ORIGEN_ID varchar(255) not null unique
, XML_FILE varchar(255) 
, XML_SCHEMA_VERSION varchar(5) not null
, CADENA_ORIGINAL mediumtext
, XML mediumblob
, version integer not null
, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255)
, TX_IMPORTADO datetime, TX_REPLICADO datetime
, primary key (CFD_ID)) ENGINE=InnoDB

select * from sx_cfdi