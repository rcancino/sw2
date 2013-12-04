alter table SX_INVENTARIO_COM add column ESPECIAL bit default false;
alter table SX_INVENTARIO_COM add column ANCHO double precision default 0;
alter table SX_INVENTARIO_COM add column LARGO double precision default 0;
alter table SX_INVENTARIO_COM add column PRECIO_KILO numeric(19,2) default 0
/* Este corre en produccion para actualizar la replica */
delete from entity_configuration where table_name='SX_INVENTARIO_COM'


alter table SX_INVENTARIO_MAQ add column ESPECIAL bit default false;
alter table SX_INVENTARIO_MAQ add column ANCHO double precision default 0;
alter table SX_INVENTARIO_MAQ add column LARGO double precision default 0;
alter table SX_INVENTARIO_MAQ add column PRECIO_KILO numeric(19,2) default 0
/* Este corre en produccion para actualizar la replica */
delete from entity_configuration where table_name='SX_INVENTARIO_MAQ'

alter table SX_COMPRAS2_DET add column ESPECIAL bit default false;
alter table SX_COMPRAS2_DET add column ANCHO double precision default 0;
alter table SX_COMPRAS2_DET add column LARGO double precision default 0;
alter table SX_COMPRAS2_DET add column PRECIO_KILO numeric(19,2) default 0
/* Este corre en produccion para actualizar la replica */
delete from entity_configuration where table_name='SX_COMPRAS2_DET'


alter table SX_MAQ_ORDENESDET add column ESPECIAL bit default false;
alter table SX_MAQ_ORDENESDET add column ANCHO double precision default 0;
alter table SX_MAQ_ORDENESDET add column LARGO double precision default 0;

/* Este corre en produccion para actualizar la replica */
delete from entity_configuration where table_name='SX_MAQ_ORDENESDET'

alter table SX_MAQ_RECEPCION_CORTEDET add column ESPECIAL bit default false;
alter table SX_MAQ_RECEPCION_CORTEDET add column ANCHO double precision default 0;
alter table SX_MAQ_RECEPCION_CORTEDET add column LARGO double precision default 0;
/* Este corre en produccion para actualizar la replica */
delete from entity_configuration where table_name='SX_MAQ_RECEPCION_CORTEDET'

alter table SX_VENTASDET add column ESPECIAL bit default false;
alter table SX_VENTASDET add column ANCHO double precision default 0;
alter table SX_VENTASDET add column LARGO double precision default 0;
alter table SX_VENTASDET add column PRECIO_KILO numeric(19,2) default 0
/* Este corre en produccion para actualizar la replica */
delete from entity_configuration where table_name='SX_VENTASDET'



