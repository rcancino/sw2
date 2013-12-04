SELECT 'Inventario Diferencias por Redondeo' AS DESCRIPCION,'IRED01' AS CONCEPTO,X.SUCURSAL_ID,S.NOMBRE AS SUCURSAL,SUM(COSTO)  AS COSTO FROM (
select 'A_INV' AS GRUPO,'INI' AS TIPO,I.SUCURSAL_ID,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.COSTO,2)) as COSTO
from sx_existencias I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true 
 and YEAR(I.FECHA)=(CASE WHEN MONTH('@FECHA_INI')=1 THEN YEAR('@FECHA_INI')-1 ELSE YEAR('@FECHA_INI') END) AND
MONTH(I.FECHA)=(CASE WHEN MONTH('@FECHA_INI')=1 THEN 12 ELSE MONTH('@FECHA_INI')-1 END ) GROUP BY I.SUCURSAL_ID
union
select 'COMPRAS' AS GRUPO,CASE WHEN X.ENTRADA_ID IS NOT NULL  THEN 'COM' ELSE 'SNA' END AS TIPO,I.SUCURSAL_ID,SUM(ROUND(CASE WHEN X.ENTRADA_ID IS NULL THEN I.CANTIDAD/I.FACTORU*(SELECT X.COSTOP FROM sx_costos_p X where X.PRODUCTO_ID=I.PRODUCTO_ID and X.YEAR=2011 AND X.MES=8)  ELSE 
  (X.CANTIDAD/I.FACTORU)*((a.TC * x.COSTO)+round((((((a.FLETE * a.TC)*round(((((x.CANTIDAD / I.FACTORU) * x.PRECIO) * 100)/(select sum((xx.CANTIDAD / I.FACTORU) * xx.PRECIO)  from sx_analisisdet xx JOIN SX_ANALISIS Y ON(Y.ANALISIS_ID=XX.ANALISIS_ID) where (Y.CXP_ID = A.CXP_ID))),2))/100)/x.CANTIDAD)*I.FACTORU),2))END,2)) as COSTO
from sx_inventario_com I LEFT JOIN SX_analisisdet X ON(I.INVENTARIO_ID=X.ENTRADA_ID) JOIN sx_analisis Y  ON (Y.ANALISIS_ID=X.ANALISIS_ID) LEFT JOIN SX_CXP A ON(A.CXP_ID=Y.CXP_ID) join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59'  GROUP BY I.SUCURSAL_ID
union
select 'COMPRAS' AS GRUPO,I.MAQUILA_TIPO AS TIPO,I.SUCURSAL_ID,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.COSTO,2)) as COSTO
from sx_inventario_maq I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' 
group by I.SUCURSAL_ID,I.MAQUILA_TIPO 
union
select 'COMPRAS' AS GRUPO,'SRV' AS TIPO,I.SUCURSAL_ID,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.GASTOS,2)) as COSTO
from sx_inventario_trs I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' AND I.GASTOS>0 GROUP BY I.SUCURSAL_ID
union
select 'VENTAS' AS GRUPO,'FAC' AS TIPO,I.SUCURSAL_ID,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.COSTOP,2)) as COSTO
from sx_ventasdet I  use index (INDX_VDET2)  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' GROUP BY I.SUCURSAL_ID
union
select 'VENTAS' AS GRUPO,'DEV' AS TIPO,I.SUCURSAL_ID
,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.COSTOP,2)) as COSTO
from sx_inventario_dev I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' GROUP BY I.SUCURSAL_ID
union
select 'MOVIMIENTOS' AS GRUPO,'MOV' AS TIPO,I.SUCURSAL_ID
,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.COSTOP,2)) as COSTO
from sx_inventario_mov I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' group by I.SUCURSAL_ID
union
select 'GASTO' AS GRUPO,'DEC' AS TIPO,I.SUCURSAL_ID
,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.COSTOP,2)) as COSTO
from sx_inventario_DEC I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' GROUP BY I.SUCURSAL_ID
union
select 'TRANSFORM' AS GRUPO,CASE WHEN I.CANTIDAD<0 THEN 'TRS' ELSE 'TRE' END AS TIPO,I.SUCURSAL_ID
,SUM(ROUND(CASE WHEN I.CANTIDAD<0 THEN I.CANTIDAD/I.FACTORU*I.COSTOP ELSE I.CANTIDAD/I.FACTORU*I.COSTOORIGEN END,2)) as COSTO
from sx_inventario_trs I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' GROUP BY I.SUCURSAL_ID,CASE WHEN I.CANTIDAD<0 THEN 'TRS' ELSE 'TRE' END
union
select 'TRASLADO' AS GRUPO
,CASE WHEN I.CANTIDAD>0 THEN 'TPE' ELSE 'TPS' END AS TIPO,I.SUCURSAL_ID
,SUM(ROUND(I.CANTIDAD/I.FACTORU*I.COSTOP,2)) as COSTO
from sx_inventario_trd I  join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and I.FECHA BETWEEN '@FECHA_INI 00:00:00' AND '@FECHA_FIN 23:59:59' group by I.SUCURSAL_ID,CASE WHEN I.CANTIDAD>0 THEN 'TPE' ELSE 'TPS' END
union
select 'Z_INV' AS GRUPO,'FIN' AS TIPO,I.SUCURSAL_ID,SUM(ROUND(-I.CANTIDAD/I.FACTORU*I.COSTO,2)) as COSTO
from sx_existencias I  
join sx_productos p on(p.producto_id=i.producto_id) where p.inventariable is true and YEAR(I.FECHA)=YEAR('@FECHA_FIN') AND MONTH(I.FECHA)=MONTH('@FECHA_FIN') GROUP BY I.SUCURSAL_ID
) AS X  JOIN sw_sucursales S ON(S.SUCURSAL_ID=X.SUCURSAL_ID) GROUP BY X.SUCURSAL_ID