Ig
1
Ventas del dia  ${fecha} ${sucursalName}

<#list bancos as banco>
${banco.cuentaContable}                , 0
${banco.concepto}
${banco.importeAsDouble?c},1.00
</#list>
<#list facturasCam as fac>
105-${sucursal}-000                , 0
Fac:${fac.numero} ${fac.origen} ${fac.clave}
${fac.totalAsDouble?c},1.00
</#list>
<#list facturasCre as fac>
106-${fac.cuenta?substring(4,8)}-000                , 0
Fac:${fac.numero} ${fac.origen} ${sucursal}
${fac.totalAsDouble?c},1.00
</#list>
6${sucursalShort}-0020-000                , 0
COMISIONES BANCARIAS (${sucursalName})
${comisionesBancarias?c},1.00
117-0001-003                , 0
I.V.A. EN GASTOS 
${comisionesBancariasSinIva?c},1.00
<#list pagosConTarjetaCre as pago>
203-D002-000                , 0
Pg Tar(CRE) Fac:${pago.documento} ${pago.cuenta?substring(4,8)}

${pago.importeAsDouble?c},1.00
</#list>
206-0002-001                , 0
I.V.A Pend/Pago:Targ CRE (${sucursalName})

${ivaPagosConTarjetaCre?c},1.00
401-0001-${sucursal}                , 0
MOSTRADOR

${ventasMos?c},1.00
206-0001-001                , 0
IVA EN VENTAS (${sucursalName})

${ivaEnVentasMos?c},1.00
401-0002-${sucursal}                , 0
CREDITO (${sucursalName})

${ventasCre?c},1.00
401-0003-${sucursal}                , 0
CAMIONETA (${sucursalName})

${ventasCam?c},1.00
206-0002-001                , 0
I.V.A Pendiente CRE (${sucursalName})

${ventasIvaCre?c},1.00
206-0002-001                , 0
I.V.A Pendiente CAM (${sucursalName})

${ventasIvaCam?c},1.00
<#list pagosConTarjetaCam as pago>
105-${sucursal}-000                , 0
Pg Tar(CAM) Fac:${pago.documento} ${pago.clave}

${pago.importeAsDouble?c},1.00
</#list>
206-0002-001                , 0
I.V.A Pend/Pago:Targ CAM (${sucursalName})
${ivaPagosConTarjetaCam?c},1.00
206-0001-001                , 0
I.V.A  En ventas(${sucursalName})

${ivaPagosConTarjetaCam?c},1.00
902-0001-000                , 0
ACUMULABLE IETU MOS
${ietuMos?c},1.00
903-0001-000                , 0
IETU ACUMULABLE MOS

${ietuMos?c},1.00
902-0002-000                , 0
ACUMULABLE IETU CAM
${ietuCam?c},1.00
903-0002-000                , 0
IETU ACUMULABLE CAM

${ietuCam?c},1.00
204-D001-000                , 0
ANT A CLIENTES

${anticipoCam?c},1.00
206-0001-005                , 0
IVA ANT A CLIENTES

${ivaAnticipoCam?c},1.00
203-D001-000                , 0
ACREDORES DIVERSOS

0.00,1.00
110-B003-000                , 0
FUNCIONARIOS EMPLEADOS

0.00,1.00
704-0001-000                , 0
OTROS GASTOS MOS
${otrosGastos?c},1.00
702-0001-000                , 0
OTROS INGRESOS MOS

${otrosIngresos?c},1.00
FIN

