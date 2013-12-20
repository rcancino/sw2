SELECT V.CARGO_ID as ID
,V.TIPO
,V.COBRADOR_ID as cobradorId
,V.REVISADA as revisada
,V.REVISION as revision
,V.FECHA_RECEPCION_CXC as fechaRecepcionCXC
,V.FECHA_REVISION_CXC as fechaRevisionCxc
,V.COMENTARIO_REP_PAGO as comentarioRepPago
,V.COMENTARIO2 as comentario2
,V.DOCTO as DOCUMENTO
,V.NFISCAL as NUMEROFISCAL
,V.post_fechado as postFechado
,V.FECHA
,V.VTO as vencimiento
,V.REPROGRAMAR_PAGO as reprogramarPago
,CASE WHEN ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO),0) <0 THEN 0 ELSE ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO),0) END AS ATRASO
,S.CLAVE AS sucursal
,S.NOMBRE as sucursalNombre
,V.CLAVE AS clave
,V.NOMBRE
,V.TOTAL
,IFNULL((select sum(X.importe) FROM sx_cxc_aplicaciones x where x.CARGO_ID=v.CARGO_ID AND X.TIPO='NOTA' AND ABN_DESCRIPCION LIKE 'DEV%'),0) as devoluciones
,IFNULL((select sum(X.importe) FROM sx_cxc_aplicaciones x where x.CARGO_ID=v.CARGO_ID AND X.TIPO='NOTA' AND ABN_DESCRIPCION LIKE 'BON%'),0) as bonificaciones
,ifnull((select sum(X.importe) FROM sx_cxc_aplicaciones x where x.CARGO_ID=v.CARGO_ID AND X.TIPO='NOTA' AND ABN_DESCRIPCION LIKE 'DES%'),0) as descuentos
,IFNULL((select sum(X.importe) FROM sx_cxc_aplicaciones x where x.CARGO_ID=v.CARGO_ID AND X.TIPO='PAGO'),0) as PAGOS
,V.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM sx_cxc_aplicaciones B WHERE B.CARGO_ID=V.CARGO_ID),0) AS SALDO
,IFNULL((select sum(X.importe) FROM sx_notadecargo_det x where x.VENTA_ID=v.CARGO_ID),0) as cargoAplicado
,CASE WHEN TRUNCATE((TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO))/7,0)<=0 THEN 0 ELSE TRUNCATE((TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO))/7,0) END AS Cargo
,(CASE WHEN TRUNCATE((TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO))/7,0)<=0 THEN 0 ELSE TRUNCATE((TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO))/7,0) END)
*(V.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM sx_cxc_aplicaciones B WHERE B.CARGO_ID=V.CARGO_ID AND B.TIPO='NOTA' AND B.ABN_DESCRIPCION LIKE 'DEV%'),0))/100 AS ImporteCargo
FROM sx_ventas V 
LEFT JOIN SX_CLIENTES_CREDITO C ON(C.CLAVE=V.CLAVE)
JOIN sw_sucursales S ON(V.SUCURSAL_ID=S.SUCURSAL_ID)
WHERE V.FECHA> ?  
  AND V.ORIGEN=? 
  AND V.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM sx_cxc_aplicaciones B WHERE B.CARGO_ID=V.CARGO_ID),0)<>0
  AND V.CARGO_ID NOT IN(SELECT X.CARGO_ID FROM sx_juridico X)