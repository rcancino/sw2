drop table if exists compras_por_depurar

create table compras_por_depurar
select compra_id,sucursal_id from sx_compras2 where sucursal_id<>5

delete from sx_comprasdet where compra_id in(select compra_id from compras_por_depurar)

delete from sx_compras2 where compra_id in(select compra_id from compras_por_depurar)