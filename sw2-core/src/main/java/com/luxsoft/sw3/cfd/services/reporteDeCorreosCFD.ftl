  
  Reporte diario del envío automatico de comprobantes fiscales digitales (CFD) así como archivos PDF
  
  Resumen: 
  
  Fecha de generación: 		${fecha?date}
  Número total de comprobantes generados: ${total}
  Número de comprobantes enviados exitosamente al  los clientes: ${totalExitosos}
  Número de comprobantes sin enviar: ${totalFallidos}
  	
  Detalle: 
  Los siguientes comprobantes no  fueron enviados  por no tener registrado un correo electrónico válido
  <#list fallidos as cfd>
			${cfd.tipo} ${cfd.serie}-${cfd.folio}  Receptor: ${cfd.receptor} ${cfd.log.creado?date} Total: ${cfd.totalAsString}
  </#list>  

  
Este correo es enviado de forma automático por el sistema SiipapWin Ex, para cualquier aclaración favor de contactar 
al departamento de sistemas
  