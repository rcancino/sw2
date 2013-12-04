drop table  if exists C_VENTAS 

create table C_VENTAS (
SUCURSAL varchar(20) not null,
FECHA date not null,
ORIGEN varchar(3) not null,
DOCUMENTO bigint not null,
IMPORTE numeric(19,2) not null,
IMPUESTO numeric(19,2) not null,
TOTAL NUMERIC(19,2) not null,
CLIENTE_ID bigint not null,
NOMBRE varchar(255) not null,
SUCURSAL_ID bigint not null,
COMENTARIO varchar(255),
CARGO_ID varchar(255) not null,
CREADO datetime not null,
primary key (CARGO_ID)
) ENGINE=InnoDB;

create index c_ventas_fecha_idx on c_ventas (fecha);
