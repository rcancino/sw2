Dr
1
Cobranza camioneta ${fecha} ${sucursalName} 

<#list bancos as banco>
${banco.cuentaContable}                , 0
${banco.concepto}
${banco.importeAsDouble?c},1.00
</#list>
206-0001-001                , 0
IVA ventas CAM 

${ivaEnVentas?c},1.00
206-0001-001                , 0
IVA ventas CAM X Ant

${ivaEnVentasAnticipos?c},1.00
206-0002-001                , 0
IVA en ventas pend 
${ivaEnVentas?c},1.00
206-0002-001                , 0
IVA en ventas pend x ant 
${ivaEnVentasAnticipos?c},1.00
902-0002-000                , 0
ACUMULABLE IETU MOS
${ietu?c},1.00
903-0002-000                , 0
IETU ACUMULABLE MOS

${ietu?c},1.00
204-D001-000                , 0
ANTICIPOS DE CLIENTES
${anticipos?c},1.00
206-0001-005                , 0
IVA EN ANTI CLIENTES
${ivaAnticipos?c},1.00
405-0003-${sucursal}                , 0
DEVOLUCIONES SOBRE VENTAS
${devoluciones?c},1.00
206-0002-002                , 0
IVA EN DEVO PEND X TRASLADAR
${ivaEnDevPendienteTrasladar?c},1.00
105-0${sucursal}-000                , 0
CLIENTES CAM ${sucursalName}

${clientesPagos?c},1.00
203-D001-000                , 0
ACREDORES PAG TARJETA
${acredores?c},1.00
702-0001-000                , 0
OTROS INGRESOS DIVERSOS

${otrosIngresosDiversos?c},1.00
702-0005-000                , 0
OTROS INGRESOS SALDO FAVOR

${otrosIngresosSaldoAFavor?c},1.00
206-0001-001                , 0
IVA EN COB. OTROS ING

${ivaOtrosIngresos?c},1.00
206-0002-001                , 0
IVA PEND EN COB. OTROS ING
${ivaOtrosIngresos?c},1.00
FIN

