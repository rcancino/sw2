select B.CLASE_ID 
,NVL( (SELECT C.CUENTA_AGR FROM V_CONCEPTOS_GASTOS C WHERE C.CLASE_4=B.CLASE_ID  OR C.CLASE_3=B.CLASE_ID OR C.CLASE_2=B.CLASE_ID GROUP BY C.CUENTA_AGR),'ND') AS REF_CONTABLE
,NVL( (SELECT C.DESCRIPCION_AGR FROM V_CONCEPTOS_GASTOS C WHERE C.CLASE_4=B.CLASE_ID  OR C.CLASE_3=B.CLASE_ID OR C.CLASE_2=B.CLASE_ID GROUP BY C.DESCRIPCION_AGR),'ND') AS RUBRO
,(SELECT X.DESCRIPCION FROM SW_CONCEPTO_DE_GASTOS X WHERE X.CLASE_ID=B.CLASE_ID)  AS DESCRIP_RUBRO
,Y.NOMBRE AS SUCURSAL,D.NOMBRE,A.COMPRA_ID,A.FECHA AS F_COMPRA,B.GCOMPRADET_ID,X.DESCRIPCION,B.IMPORTE,B.IMPUESTO_IMP,B.RET1_IMPP,B.RET2_IMP,B.IMPORTE+B.IMPUESTO_IMP-B.RET1_IMPP-B.RET2_IMP AS TOTAL,A.TOTAL AS TOT_COMP
,CASE WHEN X.IETU=1 THEN B.IMPORTE ELSE 0 END AS IETU
,X.INVERSION
,E.DOCUMENTO,E.FECHA AS F_DOCTO,E.TOTAL AS TOT_FACT
,H.ORIGEN,H.REQUISICION_ID,H.FECHA AS F_REQ,H.TOTAL AS TOT_REQ
,I.FECHA AS F_PAGO
,CASE WHEN H.FORMADEPAGO=1 THEN 'CHEQUE' ELSE 'TRANSF' END AS FORMADP
,I.REFERENCIA,J.DESCRIPCION AS BANCO,I.IMPORTE AS TOT_PAG
FROM SW_TREQUISICION H 
 LEFT JOIN SW_BCARGOABONO I ON(I.CARGOABONO_ID=H.CARGOABONO_ID)
 LEFT JOIN SW_CUENTAS J ON(I.CUENTA_ID=J.ID)
 LEFT JOIN SW_TREQUISICIONDET G ON(H.REQUISICION_ID=G.REQUISICION_ID)
 LEFT JOIN SX_GAS_FACXREQ2 F ON(G.REQUISICIONDE_ID=F.REQUISICIONESDET_ID)
 LEFT JOIN SW_FACTURAS_GASTOS E ON(F.FACTURA_ID=E.ID)
 LEFT JOIN SW_GCOMPRA A ON(A.COMPRA_ID=E.COMPRA_ID)
 LEFT JOIN SW_GCOMPRADET B ON(A.COMPRA_ID=B.COMPRA_ID)
 LEFT JOIN SW_SUCURSALES Y ON(B.SUCURSAL_ID=Y.SUCURSAL_ID)
 LEFT JOIN SW_GPRODUCTOSERVICIO X ON(B.PRODUCTO_ID=X.PRODUCTO_ID)
 LEFT JOIN SW_GPROVEEDOR D ON(A.PROVEEDOR_ID=D.PROVEEDOR_ID)
WHERE H.FECHA BETWEEN ? AND ? 