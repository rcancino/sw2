SELECT A.ORIGEN,A.PROVEEDOR
,SUM(CASE WHEN A.CUENTA=117 AND A.CONCEPTO_ID  IN(224) THEN -(A.DEBE-A.HABER) ELSE 0 END) AS IVA_NOTA
,SUM(CASE WHEN A.CUENTA=117 AND A.CONCEPTO_ID NOT IN(227,222,224) THEN A.DEBE-A.HABER ELSE 0 END) AS IVA_ACRED
,SUM(CASE WHEN A.CUENTA=117 AND A.CONCEPTO_ID  IN(227) THEN A.DEBE-A.HABER ELSE 0 END) AS IVA_RET
,SUM(CASE WHEN A.CUENTA=117 AND A.CONCEPTO_ID  IN(222) THEN A.DEBE-A.HABER ELSE 0 END) AS IVA_ANT
,SUM(CASE WHEN a.cuenta=117 THEN A.BASE_CALCULADA ELSE 0 END) AS BASE_CALCULADA
,SUM(CASE WHEN A.CUENTA=900 AND A.CONCEPTO_ID NOT IN(423) THEN A.DEBE-HABER ELSE 0 END) AS IETU
,SUM(CASE WHEN A.CUENTA=900 AND A.CONCEPTO_ID IN(423) THEN A.DEBE-HABER ELSE 0 END) AS IETU_ANT
,SUM(CASE WHEN A.CUENTA=900 AND A.CONCEPTO_ID NOT IN(423) THEN A.DEBE-HABER ELSE 0 END) -
SUM(CASE WHEN a.cuenta=117 THEN A.BASE_CALCULADA ELSE 0 END)
AS EXENTO
FROM (
SELECT case when PD.REFERENCIA='MOS' AND PD.descripcion2 like '%- AMEX%' THEN 'AMERICAN EXPRESS COMPANY MEXICO SA DE CV' WHEN PD.REFERENCIA='MOS' AND PD.DESCRIPCION2 NOT LIKE '%- AMEX%' THEN 'BANCO NACIONAL DE MEXICO SA' ELSE PD.REFERENCIA END AS PROVEEDOR
,(CASE WHEN C.DESCRIPCION LIKE '%COMPRA%' THEN 'COMPRAS' WHEN C.DESCRIPCION LIKE '%ACTIVO%' THEN 'ACTIVO FIJO' ELSE 'GASTOS' END) AS  ORIGEN
,P.TIPO_ID AS TIPO,P.FOLIO AS POLIZA,P.FECHA,PD.DEBE,PD.HABER,P.CLASE,PD.ASIENTO,T.DESCRIPCION AS CTA_DESCRIPCION,C.DESCRIPCION AS CONC_DESCRIPCION
,T.CLAVE AS CUENTA,C.CONCEPTO_ID,C.CLAVE AS CONCEPTO,PD.DESCRIPCION2,PD.REFERENCIA2,PD.POLIZADET_ID,P.POLIZA_ID,CASE WHEN T.CLAVE=117 THEN ROUND(PD.DEBE/0.16,2) ELSE PD.DEBE END AS BASE_CALCULADA
FROM sx_polizas P JOIN sx_polizasdet PD ON(P.POLIZA_ID=PD.POLIZA_ID) JOIN sx_conceptos_contables C ON(PD.CONCEPTO_ID=C.CONCEPTO_ID) JOIN sx_cuentas_contables T ON(T.CUENTA_ID=C.CUENTA_ID)
WHERE YEAR(P.FECHA)=? AND MONTH(P.FECHA)=? AND (PD.CONCEPTO_ID IN(222,223,225,227,739,420,422,421,424,425,426,427,428,725,423) OR (PD.CONCEPTO_ID IN(224) AND PD.ASIENTO='Notas'))  AND PD.ASIENTO NOT LIKE '%DETERMINACION%' ORDER BY P.CLASE,T.CLAVE,PD.CONCEPTO_ID,PD.ASIENTO
) AS A
GROUP BY A.PROVEEDOR