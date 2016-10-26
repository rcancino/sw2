select a.docto as documento,b.folio as pedido,b.folio,a.fecha,a.creado as facturado,a.nombre,a.origen,a.total ,a.INSTRUCCION_ENTREGA as instruccion ,a.fpago,a.ce as contraEntrega,a.cargo_id as id 
from sx_ventas a 
join  sx_pedidos b on(a.pedido_id=b.pedido_id) 
join sx_pedidos_entregas c on (b.INSTRUCCION_ID=c.INSTRUCCION_ID) 
where b.instruccion_id is not null
	and a.fecha>DATE(DATE_ADD(?, INTERVAL -30 DAY))
	and a.ce=false 
	and (a.importe+(ifnull(a.anticipo_aplicado,0)/1.16))-ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=a.cargo_id),0)>1 				   
union
select a.docto as documento,b.folio as pedido,b.folio,a.fecha,a.creado as facturado,a.nombre,a.origen,a.total ,a.INSTRUCCION_ENTREGA as instruccion ,a.fpago,a.ce as contraEntrega,a.cargo_id as id   
from sx_ventas a 
join  sx_pedidos b on(a.pedido_id=b.pedido_id)  
join sx_pedidos_entregas c on (b.INSTRUCCION_ID=c.INSTRUCCION_ID) 
join sx_asignacion_ce s on(s.venta_id=a.cargo_id)  
where b.instruccion_id is not null 	
	and s.asignacion between ? and ? 
	and (a.importe+(ifnull(a.anticipo_aplicado,0)/1.16))-ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=a.cargo_id),0)>1	