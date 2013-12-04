
#agrega un indice a la tabla SX_CXC_ABONOS_AUT CON NOMBRE FK60902F103E3E9751 (AUT_ID) y una restriccion de foreign key 
#con la tabla SX_autorizaciones , si existe un registro en sx_cxc_autorizaciones con referencia en autorizaciones no se podrá
# eliminar de abonos_aut.
#crea un indice en la tabla sx_devoluciones y un foreign key referenciado a la tabla sx_autorizaciones que no permitirá
#eliminar un registro de sx_devoluciones si tiene una referencia a la tabla sx_autorizaciones.

****************************************************************************************************


alter table SX_CXC_ABONOS_AUT 
add index FK60902F103E3E9751 (AUT_ID)
, add constraint FK60902F103E3E9751 
foreign key (AUT_ID) 
references SX_AUTORIZACIONES2 (AUT_ID)

alter table SX_DEVOLUCIONES add index FK256803903E3E9751 (AUT_ID)
, add constraint FK256803903E3E9751 foreign key (AUT_ID) 
references SX_AUTORIZACIONES2 (AUT_ID)