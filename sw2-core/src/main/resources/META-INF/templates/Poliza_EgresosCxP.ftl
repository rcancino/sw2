${tipo}
1
${titulo}

<#list pagos as cxp>
${cxp.cuenta}                , 0
Pago F: ${cxp.referencia} ${cxp.fechaF}
${cxp.totalAsDouble?c},1.00
</#list>
${cuentaBancaria}                , 0
${tituloBanco}

${total?c},1.00
117-0001-001                , 0
IVA EN COMPRAS DE MATERIAS PRIMAS
${iva?c}                , 0
<#list pagos as cxp>
117-0003-001                , 0
F: ${cxp.referencia} ${cxp.nombre} 

${cxp.iva?c},1.00
</#list>
900-0001-000                , 0
IETU COMPRAS
${ietu?c},1.00
901-0001-000                , 0
IETU COMPRAS

${ietu?c},1.00
FIN

