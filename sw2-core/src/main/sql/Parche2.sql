
## Modificacio a la tabla de descuentos por volumen (Anexar el drop if exists)

drop table if exists sx_desc_vol;
create table SX_DESC_VOL 
(DESCUENTO_ID bigint not null auto_increment
, ACTIVO bit not null
, creado datetime
, descuento double precision not null
, IMPORTE numeric(19,2) not null
, TIPO varchar(7) not null
, VIGENCIA datetime not null
, primary key (DESCUENTO_ID), unique (TIPO, VIGENCIA, IMPORTE, descuento)) ENGINE=InnoDB

## Columna nueva a ventas para saber si el registro fue importado de DBF o ORACLE 

alter table SX_VENTAS add column ORIGEN_IMPORTACION varchar(6)