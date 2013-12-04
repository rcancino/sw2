
alter table SX_LP_VENT drop foreign key FKFA25A2B6FABDF9E8;

alter table SX_LP_VENT_DET  drop foreign key FK54B58EEAE266A76E

alter table SX_LP_VENT_DET  drop foreign key FK54B58EEAF14A8924

drop table if exists sx_lp_vent

drop table if exists sx_lp_vent_det

drop table if exists sx_lp_tipos

create table SX_LP_VENT (
LISTA_ID bigint not null auto_increment
, COMENTARIO varchar(255)
, APLICADA datetime
, AUTORIZADA varchar(255)
, TC_DOLARES double precision not null
, TC_EUROS double precision not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, CREADO_IP varchar(255)
, CREADO_MAC varchar(255)
, MODIFICADO_IP varchar(255)
, MODIFICADO_MAC varchar(255)
, version integer not null
, primary key (LISTA_ID)) ENGINE=InnoDB

create table SX_LP_VENT_DET (
LISTADET_ID bigint not null auto_increment
, LISTA_ID bigint not null
, CLAVE varchar(255) not null
, DESCRIPCION varchar(255) not null
, GRAMOS double precision not null
, KILOS double precision not null
, COMENTARIO varchar(255)
, PAGINA integer not null
, COLUMNA integer not null
, GRUPO integer not null
, PRECIO numeric(19,2) not null
, PRECIO_CREDITO numeric(19,2) not null
, PRECIO_ANTERIOR numeric(19,2) not null
, PRECIO_ANTERIOR_CRE numeric(19,2) not null
, INCREMENTO double precision not null
, FACTOR double precision not null
, FACTOR_CREDITO double precision not null
, COSTO numeric(19,2) not null
, COSTOU numeric(19,2) not null
, MONEDA varchar(255) not null
, PRESENTACION varchar(255) not null
, PROV_CLAVE varchar(4)
, PROV_NOMBRE varchar(250)
, PRODUCTO_ID bigint not null
, primary key (LISTADET_ID)) 
ENGINE=InnoDB



alter table SX_LP_VENT_DET 
    add index FK54B58EEAE266A76E (LISTA_ID), 
    add constraint FK54B58EEAE266A76E 
    foreign key (LISTA_ID) references SX_LP_VENT (LISTA_ID)

alter table SX_LP_VENT_DET 
    add index FK54B58EEAF14A8924 (PRODUCTO_ID), 
    add constraint FK54B58EEAF14A8924 
    foreign key (PRODUCTO_ID) references SX_PRODUCTOS (PRODUCTO_ID)
