drop table if exists pedidos_por_depurar

create table pedidos_por_depurar as select pedido_id,sucursal_id,AUTORIZACION_ID from sx_pedidos WHERE sucursal_id<>5 

delete from sx_pedidosdet where pedido_id in(select pedido_id from pedidos_por_depurar)

update sx_pedidos set AUTORIZACION_ID=null where pedido_id in(select pedido_id from pedidos_por_depurar)

/* Autorizaciones de pagos contra entrega*/
delete from sx_pedidos_pagoce where pedido_id in(select pedido_id from pedidos_por_depurar)

delete from sx_autorizaciones2 where tipo='AUT_DE_PEDIDO' and AUT_ID in(select autorizacion_id from pedidos_por_depurar)

delete from sx_pedidos_pendientes where PEDIDO_ID in(select pedido_id from pedidos_por_depurar)

update sx_ventas set pedido_id=null where pedido_id in(select pedido_id from pedidos_por_depurar)

delete from sx_pedidos where PEDIDO_ID in(select pedido_id from pedidos_por_depurar)