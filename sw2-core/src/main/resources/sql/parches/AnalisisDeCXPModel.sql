alter table SX_ANALISIS 
        drop 
        foreign key FK3658E5863480C4B4;

alter table SX_ANALISIS 
        drop 
        foreign key FK3658E58697162566;

alter table SX_ANALISISDET 
        drop foreign key FK72FEECAD729CE8DB;    

alter table SX_ANALISISDET 
        drop foreign key FK72FEECAD28F3B61;

drop table if exists SX_ANALISISDET

drop table if exists SX_ANALISIS

create table SX_ANALISIS (
ANALISIS_ID bigint not null auto_increment
, CXP_ID bigint not null
, FECHA date not null
, COMENTARIO varchar(250)
, IMPORTE numeric(19,2) not null
, REQUISICION_DET bigint
, version integer not null
, CREADO datetime
, CREADO_USR varchar(255)
, MODIFICADO datetime
, MODIFICADO_USR varchar(255)
, CREADO_IP varchar(255)
, CREADO_MAC varchar(255)
, MODIFICADO_IP varchar(255)
, MODIFICADO_MAC varchar(255)
, primary key (ANALISIS_ID)) ENGINE=InnoDB 

create table SX_ANALISISDET (
id bigint not null auto_increment
, CANTIDAD double precision not null
, PRECIO numeric(10,2) not null
, COSTO numeric(16,6) not null
, IMPORTE numeric(16,2) not null
, NETO numeric(16,6) not null
, DESC1 double precision
, DESC2 double precision
, DESC3 double precision
, DESC4 double precision
, DESC5 double precision
, DESC6 double precision
, descuentof double precision not null
, ENTRADA_ID varchar(255) not null
, ANALISIS_ID bigint not null
, primary key (id)) ENGINE=InnoDB

alter table SX_ANALISIS add index FK3658E5863480C4B4 (CXP_ID), 
    add constraint FK3658E5863480C4B4 
    foreign key (CXP_ID) references SX_CXP (CXP_ID)
    
alter table SX_ANALISIS add index FK3658E58697162566 (REQUISICION_DET), 
	add constraint FK3658E58697162566 
	foreign key (REQUISICION_DET) references SW_TREQUISICIONDET (requisicionde_id)

alter table SX_ANALISISDET add index FK72FEECAD729CE8DB (ENTRADA_ID), 
    add constraint FK72FEECAD729CE8DB foreign key (ENTRADA_ID) 
    references SX_INVENTARIO_COM (INVENTARIO_ID)

alter table SX_ANALISISDET add index FK72FEECAD28F3B61 (ANALISIS_ID), 
    add constraint FK72FEECAD28F3B61 foreign key (ANALISIS_ID) 
    references SX_ANALISIS (ANALISIS_ID)
 
        