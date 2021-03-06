SELECT A.CLAVE,SUM(A.CANTIDAD)as CANTIDAD FROM (
select i.INVENTARIO_ID,'A_INV_INI' AS GRUPO,i.FECHA AS FECHA,'INI' AS TIPO,1 AS DOCUMENTO,1 AS RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,
i.FACTORU,i.CANTIDAD,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_ini I where i.FECHA = '@FECHA_INI' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID,'COMPRAS' AS GRUPO,i.FECHA AS FECHA,'COM' AS TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD,
i.KILOS AS KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_com I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID,'COMPRAS' AS GRUPO,i.FECHA AS FECHA,i.MAQUILA_TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_maq I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE'  AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID,'VENTAS' AS GRUPO,i.FECHA AS FECHA,'FAC' AS TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_ventasdet I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN'  AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID,'VENTAS' AS GRUPO,i.FECHA AS FECHA,'DEV' AS TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION AS DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_dev I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID,'MOVIMIENTOS' AS GRUPO,i.FECHA AS FECHA,i.CONCEPTO AS TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_mov I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID,'TRANSFORMACION' AS GRUPO,i.FECHA AS FECHA,i.TRTIP AS TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,i.gastos,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_trs I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID AS inventario_id,'DEV_PROV' AS GRUPO,i.FECHA AS FECHA,'DEC' AS TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_dec I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID,'TRASLADO' AS GRUPO,i.FECHA AS FECHA,I.TIPO,i.DOCUMENTO,i.RENGLON,i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_trd I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
UNION
select i.INVENTARIO_ID AS inventario_id,'TRANSFORMACION' AS GRUPO,i.FECHA AS FECHA,'KIT' AS TIPO,i.DOCUMENTO,i.RENGLON, i.SUCURSAL_ID,i.PRODUCTO_ID,i.CLAVE,i.DESCRIPCION,i.UNIDAD_ID,i.FACTORU,i.CANTIDAD
,i.KILOS,i.COSTO,i.COSTOP,0 AS GASTOS,year(i.FECHA) AS YEAR,month(i.FECHA) AS MES,i.NACIONAL,i.COMENTARIO from sx_inventario_kit I where DATE(I.FECHA) BETWEEN  '@CORTE'  AND  '@CORTE_FIN' AND I.CLAVE='@CLAVE' AND I.SUCURSAL_ID=@SUCURSAL
) A
GROUP BY A.CLAVE