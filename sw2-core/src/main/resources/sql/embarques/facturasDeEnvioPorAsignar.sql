SELECT (SELECT S.NOMBRE FROM SW_SUCURSALES S WHERE S.SUCURSAL_ID=V.SUCURSAL_ID) AS sucursal,V.nombre,V.origen,V.CE as contraEntrega,V.fpago
,v.fecha ,V.PEDIDO_CREADO AS fecha_ped,V.PEDIDO_FOLIO AS pedido,V.DOCTO AS documento,V.CREADO AS facturado
,V.total,V.TOTAL-IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxc_aplicaciones A WHERE A.CARGO_ID=V.CARGO_ID),0) AS saldo
,v.importe,ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=V.cargo_id),0) as entregado 
,V.IMPORTE-ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=V.cargo_id),0) AS pendiente
,(SELECT MAX(A.CREADO) FROM sx_cxc_aplicaciones A WHERE A.CARGO_ID=V.CARGO_ID) AS ultimoPago
,V.INSTRUCCION_ENTREGA AS instruccion,C.delmpo,SUBSTR(C.CP,1,2) AS del,SUBSTR(C.CP,3,5) AS zona,V.CARGO_ID as id
,IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxc_aplicaciones A WHERE A.CARGO_ID=V.CARGO_ID and a.ABN_DESCRIPCION like '%DEV%'),0) AS devolucionAplicada
FROM sx_ventas v JOIN sx_clientes C ON(C.CLIENTE_ID=V.CLIENTE_ID) where V.FECHA>='2014/01/01' 
AND V.INSTRUCCION_ENTREGA IS NOT NULL AND IFNULL(V.COMENTARIO2,'') NOT LIKE '%CANCELAD%'
AND ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=V.cargo_id),0)=0
and  v.total-IFNULL((SELECT SUM(A.total) FROM sx_devoluciones A WHERE A.venta_ID=V.CARGO_ID ),0)>1


