select x.inventario_id,0 as documento,x.year,x.mes,x.clave,x.cantidad/x.FACTORU as CANTIDAD,x.costo as COSTO ,'INI' as ORIGEN
from sx_existencias x 
where x.year=? and x.mes=? and x.clave=@CLAVE
union 
select z.inventario_id,z.documento,year(z.fecha) as YEAR,month(z.fecha) as MES
,z.clave,x.cantidad/z.FACTORU as CANTIDAD,(x.costo*y.tc)+(ROUND((((y.FLETE+Y.CARGOS)*y.TC)*(ROUND(((X.CANTIDAD/Z.FACTORU)*X.PRECIO)*100/
(SELECT SUM((XX.CANTIDAD/Z.FACTORU)*XX.PRECIO) FROM sx_analisisdet XX join sx_analisis aa on(aa.ANALISIS_ID=xx.ANALISIS_ID) WHERE a.CXP_ID=aa.CXP_ID ),2))/100)/X.CANTIDAD*Z.FACTORU,2)) 
as COSTO ,'CXP' as ORIGEN
from sx_analisisdet x join sx_inventario_com z on(x.ENTRADA_ID=z.INVENTARIO_ID) join sx_analisis a on(a.analisis_id=x.analisis_id) join sx_cxp y on (y.CXP_ID=a.CXP_ID)
where year(z.fecha)=@YEAR and month(z.fecha)=@MONTH and z.clave=@CLAVE
union 
select x.inventario_id,x.documento,year(x.fecha) as YEAR,month(x.fecha) as MES
,x.clave,x.cantidad/x.FACTORU as CANTIDAD,x.costo as COSTO ,'MAQ' as ORIGEN
from sx_inventario_maq x 
where year(x.fecha)=@YEAR and month(x.fecha)=@MONTH and x.clave=@CLAVE
union 
select x.inventario_id,x.documento,year(x.fecha) as YEAR,month(x.fecha) as MES
,x.clave,x.cantidad/x.FACTORU as CANTIDAD,x.costo as COSTO ,'TRS' as ORIGEN
from sx_inventario_trs x 
where year(x.fecha)=@YEAR and month(x.fecha)=@MONTH and x.clave=@CLAVE and x.cantidad>0