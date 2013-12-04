drop table if exists devoluciones_por_depurar

create table devoluciones_por_depurar as
select devo_id,venta_id ,(select x.sucursal_id from sx_ventas x where x.cargo_id=a.venta_id) as sucursal_id
from sx_devoluciones  a where venta_id in (select cargo_id from sx_ventas where sucursal_id<>5)

delete from sx_inventario_dev where devo_id in(select devo_id from devoluciones_por_depurar)

delete from sx_cxc_abonos  where DEVOLUCION_ID in(select devo_id from devoluciones_por_depurar)

delete from sx_devoluciones where DEVO_ID in(select devo_id from devoluciones_por_depurar)
