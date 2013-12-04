${poliza.tipo}
1
${poliza.concepto}

<#list asientos as asiento>
${asiento.cuenta}                , 0
${asiento.concepto}
<#if asiento.cargo=true>
${asiento.debeAsDouble?c},1.00
<#else>

${asiento.haberAsDouble?c},1.00
</#if>
</#list>
FIN
