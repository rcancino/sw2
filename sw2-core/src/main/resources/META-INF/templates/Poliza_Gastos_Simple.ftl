${tipo}
1
${titulo}

<#list asientes as asiento>
${asiento.cuentaAsString}
${asiento.debeAsString?c}
${asiento.haberAsString?c}
</#list>
FIN