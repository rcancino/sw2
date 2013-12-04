alter table SW_EMPRESAS add column REGIMEN varchar(255)
update sw_empresas set regimen='Régimen General de Ley Personas Morales';
/*
alter table SX_PRODUCTOS add column UNIDAD_FISCAL varchar(20);
alter table SX_PRODUCTOS add column FACTOR_FISCAL double precision default 1;
alter table SX_PRODUCTOS add column PRECIO_CON_FISCAL double precision default 0;
alter table SX_PRODUCTOS add column PRECIO_CRE_FISCAL double precision default 0;

update sx_productos set UNIDAD_FISCAL=UNIDAD;
update sx_productos set FACTOR_FISCAL=1000 WHERE UNIDAD='MIL';
update sx_productos set FACTOR_FISCAL=1 WHERE UNIDAD<>'MIL';

UPDATE SX_PRODUCTOS SET PRECIO_CRE_FISCAL=precioCredito;
UPDATE SX_PRODUCTOS SET PRECIO_CON_FISCAL=precioContado;


alter table SX_INVENTARIO_COM add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_COM add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_COM add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_DEC add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_DEC add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_DEC add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_DEV add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_DEV add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_DEV add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_FAC add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_FAC add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_FAC add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_INI add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_INI add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_INI add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_KIT add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_KIT add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_KIT add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_MAQ add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_MAQ add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_MAQ add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_MOV add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_MOV add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_MOV add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_TRD add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_TRD add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_TRD add column UNIDAD_FISCAL varchar(20);
alter table SX_INVENTARIO_TRS add column CANTIDAD_FISCAL double precision default 0;
alter table SX_INVENTARIO_TRS add column FACTOR_FISCAL double precision default 0;
alter table SX_INVENTARIO_TRS add column UNIDAD_FISCAL varchar(20);
alter table SX_VENTASDET add column CANTIDAD_FISCAL double precision default 0;
alter table SX_VENTASDET add column FACTOR_FISCAL double precision default 0;
alter table SX_VENTASDET add column UNIDAD_FISCAL varchar(20);


alter table SX_PEDIDOSDET add column CANTIDAD_FISCAL double precision default 0;
alter table SX_PEDIDOSDET add column FACTOR_FISCAL double precision default 0;
alter table SX_PEDIDOSDET add column UNIDAD_FISCAL varchar(20);
alter table SX_PEDIDOSDET add column IMPORTE_FISCAL numeric(19,6);
alter table SX_PEDIDOSDET add column PRECIO_FISCAL numeric(19,6) default 0;

alter table SX_VENTASDET add column IMPORTE_FISCAL numeric(19,6);
alter table SX_VENTASDET add column PRECIO_FISCAL numeric(19,6);
*/