alter table SX_CXP  drop index FK92A685A1EFC72FF1;

alter table SX_CXP  drop foreign key FK92A685A1EFC72FF1;

alter table SX_CXP_ANTICIPOS  drop foreign key FK3F0548CEB983341D;

alter table SX_CXP_ANTICIPOS  drop foreign key FK3F0548CE932176D0;

alter table SX_CXP_ANTICIPOS  drop foreign key FK3F0548CEFA74F6B7;

alter table SX_CXP add column ANTICIPO_ID bigint

drop table if exists SX_CXP_ANTICIPOS

create table SX_CXP_ANTICIPOS (
ANTICIPO_ID bigint not null auto_increment
, PROVEEDOR_ID bigint not null
, DOCUMENTO varchar(20) not null
, FECHA date not null
, IMPORTE numeric(19,2) not null
, TC double precision not null
, MONEDA varchar(3) not null
, DESCUENTO_FINANCIERO double precision not null
, FACTURA_ID bigint not null
, NOTA_ID bigint
, COMENTARIO varchar(250)
, version integer not null
, CREADO datetime, CREADO_USR varchar(255), MODIFICADO datetime, MODIFICADO_USR varchar(255)
, CREADO_IP varchar(255), CREADO_MAC varchar(255), MODIFICADO_IP varchar(255), MODIFICADO_MAC varchar(255)
, primary key (ANTICIPO_ID)) ENGINE=InnoDB

alter table SX_CXP add index FK92A685A1EFC72FF1 (ANTICIPO_ID)
, add constraint FK92A685A1EFC72FF1 
foreign key (ANTICIPO_ID) references SX_CXP_ANTICIPOS (ANTICIPO_ID)

alter table SX_CXP_ANTICIPOS add index FK3F0548CEB983341D (NOTA_ID)
, add constraint FK3F0548CEB983341D 
foreign key (NOTA_ID) references SX_CXP (CXP_ID)

alter table SX_CXP_ANTICIPOS add index FK3F0548CE932176D0 (PROVEEDOR_ID)
, add constraint FK3F0548CE932176D0 
foreign key (PROVEEDOR_ID) references SX_PROVEEDORES (PROVEEDOR_ID)

alter table SX_CXP_ANTICIPOS add index FK3F0548CEFA74F6B7 (FACTURA_ID)
, add constraint FK3F0548CEFA74F6B7 foreign key (FACTURA_ID) references SX_CXP (CXP_ID)

