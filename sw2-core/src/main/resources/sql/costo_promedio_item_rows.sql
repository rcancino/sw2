select 0 as documento,null as fecha,x.year,x.mes,x.clave,x.cantidad/x.FACTORU as CANTIDAD,x.costo as COSTO ,'INI' as ORIGEN,x.sucursal_id
from sx_existencias x 
where x.year=? and x.mes=? and x.clave=@CLAVE
union 
select z.documento,z.fecha,year(z.fecha) as YEAR,month(z.fecha) as MES
,z.clave,x.cantidad/z.FACTORU as CANTIDAD,(x.costo*y.tc)+(ROUND((((y.FLETE+Y.CARGOS)*y.TC)*(ROUND(((X.CANTIDAD/Z.FACTORU)*X.PRECIO)*100/
(SELECT SUM((XX.CANTIDAD/Z.FACTORU)*XX.PRECIO) FROM sx_cxp_analisisdet XX WHERE XX.CXP_ID=X.CXP_ID ),2))/100)/X.CANTIDAD*Z.FACTORU,2)) 
as COSTO ,'CXP' as ORIGEN,z.sucursal_id
from sx_cxp_analisisdet x join sx_inventario_com z on(x.ENTRADA_ID=z.INVENTARIO_ID) join sx_cxp y on (y.CXP_ID=x.CXP_ID)
where year(z.fecha)=@YEAR and month(z.fecha)=@MONTH and z.clave=@CLAVE
union 
select x.documento,x.fecha,year(x.fecha) as YEAR,month(x.fecha) as MES
,x.clave,x.cantidad/x.FACTORU as CANTIDAD,x.costo as COSTO ,'MAQ' as ORIGEN,x.sucursal_id
from sx_inventario_maq x 
where year(x.fecha)=@YEAR and month(x.fecha)=@MONTH and x.clave=@CLAVE
union 
select x.documento,x.fecha,year(x.fecha) as YEAR,month(x.fecha) as MES
,x.clave,x.cantidad/x.FACTORU as CANTIDAD,x.costo as COSTO ,'TRS' as ORIGEN,x.sucursal_id
from sx_inventario_trs x 
where year(x.fecha)=@YEAR and month(x.fecha)=@MONTH and x.clave=@CLAVE and x.cantidad>0
