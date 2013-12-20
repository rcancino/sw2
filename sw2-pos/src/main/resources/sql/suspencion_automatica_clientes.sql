SELECT R.CREDITO_ID,R.CLAVE,R.NOMBRE,R.PLAZO,R.LINEA AS LIMITE,CASE WHEN R.VENCE_FACTURA=1 THEN 'REV' ELSE 'FAC' END AS VTO
,MAX(CASE WHEN ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO),0)<1 THEN 0 ELSE ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO),0) END) AS ATR_MX
,SUM(V.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM sx_cxc_aplicaciones B WHERE B.CARGO_ID=V.CARGO_ID),0)) AS SALDO
,MAX(V.FECHA) AS ULT_VTA
FROM SX_VENTAS V 
JOIN SX_CLIENTES_CREDITO R ON(R.CLAVE=V.CLAVE) 
LEFT JOIN SX_JURIDICO J ON(J.CARGO_ID=V.CARGO_ID)
WHERE V.FECHA>'2008/12/31'  AND V.ORIGEN='CRE' AND J.CARGO_ID IS NULL AND  
v.total-IFNULL((SELECT SUM(B.IMPORTE) FROM sx_cxc_aplicaciones B WHERE B.CARGO_ID=V.CARGO_ID),0)>0 
group by R.CREDITO_ID,R.CLAVE,R.NOMBRE,R.PLAZO,R.LINEA, R.VENCE_FACTURA
HAVING MAX(CASE WHEN ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO),0)<1 THEN 0 ELSE ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO),0) END)>15
ORDER BY 7 DESC