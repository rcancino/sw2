SELECT X.periodo,X.origenId,X.DESCRIP AS descripcion,ROUND(SUM(X.IMP_NETO),2) AS ventaNeta,ROUND(SUM(X.COSTO),2) AS costo
,(ROUND(SUM(X.IMP_NETO),2)-ROUND(SUM(X.COSTO),2)) AS importeUtilidad
,((ROUND(SUM(X.IMP_NETO),2)-ROUND(SUM(X.COSTO),2))*100)/ROUND(SUM(X.IMP_NETO),2) AS porcentajeUtilidad
,ROUND((X.KILOS),2) AS kilos,ROUND(SUM(X.IMP_NETO),2)/ROUND((X.KILOS),2) AS precio_kilos
,ROUND(SUM(X.COSTO),2)/ROUND((X.KILOS),2) AS costo_kilos
,0 AS inventarioCosteado
FROM (
SELECT 
'@MES - @YEAR' AS PERIODO,@DESCRIPCION AS DESCRIP
,SUM(D.IMP_NETO) AS IMP_NETO,SUM(D.COSTO_NETO) AS COSTO,0.0 AS INV_COSTO ,SUM(D.KILOS) AS KILOS
FROM  FACT_VENTASDET D  USE INDEX (FECHA) 
WHERE D.CLAVE<>'ANTICIPO' AND D.FECHA BETWEEN '@YEAR/01/01' AND '@FECHA_FIN' @VENTA @TIPO_PROD
GROUP BY  DESCRIP
) AS X  GROUP BY X.DESCRIP
