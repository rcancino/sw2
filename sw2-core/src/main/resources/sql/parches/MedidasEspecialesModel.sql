alter table SX_PEDIDOS add column ESPECIAL bit default false

alter table SX_PEDIDOSDET add column ANCHO double precision default 0

alter table SX_PEDIDOSDET add column ESPECIAL bit default false

alter table SX_PEDIDOSDET add column LARGO double precision default 0

alter table SX_PEDIDOSDET add column PRECIO_KILO numeric(19,2) default 0

alter table SX_PEDIDOSDET add column ENTRADA varchar(255)

alter table SX_PEDIDOSDET add column TIPO_E varchar(3)

alter table SX_PRODUCTOS add column ESPECIAL bit default false

alter table SX_PRODUCTOS add column PRECIO_KILO_CON numeric(19,2) default 0

alter table SX_PRODUCTOS add column PRECIO_KILO_CRE numeric(19,2) default 0

alter table SX_PRODUCTOS add column FECHA_LP datetime 

