
DROP TABLE IF EXISTS BI_VENTAS

CREATE TABLE BI_VENTAS AS
select A.CARGO_ID
,sucursal_id
,docto
,A.FECHA
,A.CLIENTE_ID
,A.ORIGEN
,IFNULL((select sum(x.CORTES*X.PRECIO_CORTES) from sx_ventasdet x where x.venta_id=a.cargo_id),0) as IMPORTE_CORTES
,(select sum(x.importe) from sx_ventasdet x where x.venta_id=a.cargo_id AND X.PRODUCTO_ID NOT IN(722,4411)) as IMPORTE_BRUTO
,(select sum((x.importe*x.dscto)/100) from sx_ventasdet x where x.venta_id=a.cargo_id) as DESCUENTOS
,cargos
,FLETE
,ROUND(((select sum(x.CORTES*X.PRECIO_CORTES) from sx_ventasdet x where x.venta_id=a.cargo_id)
    +(select sum(x.importe) from sx_ventasdet x where x.venta_id=a.cargo_id AND X.PRODUCTO_ID NOT IN(722,4411))
    -(select sum((x.importe*x.dscto)/100) from sx_ventasdet x where x.venta_id=a.cargo_id)
    +cargos
    +FLETE),2) AS IMPORTE_CALCULADO
,a.importe
,a.impuesto
,a.total
,IFNULL((SELECT SUM(D.IMPORTE) FROM sx_devoluciones D WHERE D.VENTA_ID=A.CARGO_ID),0) as DEVOLUCION2
,IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.CARGO_ID=A.CARGO_ID AND X.TIPO='NOTA' AND X.ABN_DESCRIPCION NOT LIKE 'DEVO%'),0) AS BONIFICACION
,IFNULL((select sum(-x.CANTIDAD/X.FACTORU*X.COSTOP) from sx_ventasdet x where x.venta_id=a.cargo_id AND X.PRODUCTO_ID NOT IN(722,4411)),0)
        -IFNULL((select sum(D.CANTIDAD/X.FACTORU*X.COSTOP) from sx_ventasdet x JOIN sx_inventario_dev D ON(X.INVENTARIO_ID=D.VENTADET_ID) where x.venta_id=a.cargo_id AND X.PRODUCTO_ID NOT IN(722,4411)),0)    as COSTO
,A.SALDO
,A.VTO
,A.SOCIO_ID
,A.COBRADOR_ID
,A.FPAGO
,IFNULL((select sum(-x.cantidad/x.factoru*p.kilos) from sx_ventasdet x join sx_productos p on(p.producto_id=x.producto_id) where x.venta_id=a.cargo_id),0) as KILOS
,A.MODIFICADO_USERID
,(select X.FECHA from sx_cxc_cargos_cancelados x where x.CARGO_id=a.cargo_id) as CANCELADO
from sx_ventas a 
where a.fecha between ? and ? and tipo='FAC'

SELECT (SELECT max(v.origen) FROM sx_ventasdet x join sx_ventas v on(v.CARGO_ID=x.VENTA_ID) where x.INVENTARIO_ID=d.VENTADET_ID) as origen
,d.*
 FROM sx_inventario_dev d where fecha BETWEEN '2011/01/01' AND '2011/05/31'

SELECT * FROM SX_VENTASDET V WHERE VENTA_ID IN(
SELECT X.VENTA_ID FROM SX_VENTASDET X WHERE XX.FECHA<'2011/02/24' AND X.PRODUCTO_ID IN(722,4411) 
) 
GROUP BY DOCUMENTO

SELECT * FROM SX_VENTASDET WHERE VENTA_ID ='8a8a81e7-2e3589b3-012e-35dcf4c0-00b0'

DROP TABLE IF EXISTS BI_VENTASDET

CREATE TABLE BI_VENTASDET AS
select 
C.CLIENTE_ID,V.SUCURSAL_ID,C.ORIGEN
,C.POST_FECHADO AS POSTF,C.DOCTO,V.RENGLON,DATE(V.FECHA) AS FECHA
,V.PRODUCTO_ID
,V.NACIONAL
,V.PRECIO_L AS PRECIO_LISTA
,V.PRECIO
,V.CANTIDAD/V.FACTORU AS CANTIDAD
,IFNULL((SELECT SUM(D.CANTIDAD) FROM sx_inventario_dev D WHERE D.VENTADET_ID=V.INVENTARIO_ID ),0)/V.FACTORU AS DEVOLUCION
,V.CANTIDAD/V.FACTORU*V.PRECIO AS IMPORTE_BRUTO
,V.DSCTO AS DESCUENTO
,V.CORTES
,V.PRECIO_CORTES*V.CORTES AS IMPORTE_CORTES
,V.IMPORTE_NETO,V.SUBTOTAL
,V.COSTOP
,V.COSTOP AS COSTOR
,V.COSTOP AS COSTOU
,C.CARGO_ID
,v.INVENTARIO_ID
from sx_ventasdet v  use index (INDX_VDET2)
join sx_ventas c on(c.CARGO_ID=v.VENTA_ID)
where v.fecha BETWEEN ? AND ?





CREATE INDEX BIVENTAS_IDX1_CLIENTE ON BI_VENTAS(cliente_id);

CREATE INDEX BIVENTAS_IDX2_FECHA ON BI_VENTAS(FECHA);

select cliente_id 
,sum(importe_bruto) as importeBruto  
,sum(descuentos) as descuentos
,sum(cargos) as cargos
,sum(flete) as flete
,sum(importe) as importe
,sum(impuesto) as impuesto
,sum(total) as total
,sum(devolucion2) as devoluciones
,sum(bonificacion) as bonificaciones
,sum(costo) as costo
,sum(kilos) as kilos
from bi_ventas a 
where a.fecha between '2011-01-01' and '2011-06-30'
group by cliente_id
