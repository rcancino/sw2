select e.year,e.mes,e.clave,e.descripcion,e.UNIDAD,L.NOMBRE as LINEA,M.NOMBRE as MARCA,C.NOMBRE as CLASE,sum(e.cantidad) as CANTIDAD 
from sx_existencias e join sx_productos p on (p.producto_id=e.producto_id) 
JOIN SX_LINEAS L ON(L.LINEA_ID=P.LINEA_ID) 
JOIN SX_MARCAS M ON(M.MARCA_ID=P.MARCA_ID) 
JOIN SX_CLASES C ON(C.CLASE_ID=P.CLASE_ID) 
where  e.clave=? and e.year=? and e.mes=? 
group by e.year,e.mes,e.clave,e.descripcion ,e.unidad,p.linea_id,L.NOMBRE,M.NOMBRE,C.NOMBRE
order by LINEA 
