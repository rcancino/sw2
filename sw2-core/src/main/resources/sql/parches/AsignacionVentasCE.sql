create table SX_ASIGNACION_CE 
(ID varchar(255) not null
, ASIGNACION date not null
, FECHA_FAC date not null
, COMENTARIO varchar(250)
, SOLICITO varchar(50)
, VENTA_ID varchar(255)not null
, primary key (ID)) ENGINE=InnoDB

alter table SX_ASIGNACION_CE add index FKCF85DF7BC7B6E (VENTA_ID), add constraint FKCF85DF7BC7B6E foreign key (VENTA_ID) references SX_VENTAS (CARGO_ID)

