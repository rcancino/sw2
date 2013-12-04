<html>
<head>
	<style type="text/css">
		h4 {color: red}
		h3 {color: black}
	</style>

  <title>Comprobantes Fiscales Digitales</title>
  <h2>Papel S.A. de C.V.</h2>
</head>
<body>
  <h3>Departamento de Crédito 
  	<br>
  	<br>Fecha: 		${fecha?date}
  	<br>Atención: 	${cliente}
  </h3>
  <p>Anexo a este correo le hacemos llegar los siguientes comprobantes fiscales digitales
  
  <table align="center"
		valign="middle"
		  width="800" border="1" cellspacing="1" cellpadding="1">
  
	<tr bgcolor="#999999">
	<th>Tipo</th>
	<th>Serie</th>
	<th>Folio</th>
	<th>Fecha</th>
	<th>Total</th>
	<th>Archivo</th>
	<#list cfds as fac>
		<tr>
			<td align="left">${fac.tipo}</td>
			<td align="center">${fac.serie}</td>
			<td align="center">${fac.folio}</td>
			<td align="center">${fac.log.creado?date}</td>
			<td align="right">${fac.totalAsString}</td>
			<td align="right">${fac.fileName}</td>
			
		</tr>
	</#list>  
  </table>
  <p> Quedando a sus ordenes para cualquier duda o aclaración
  
  <p>Atentamente
  <br>Gerencia de Cuentas por Cobrar 
  <br>Tel: 5342-7166 Ext. 7141
  
  
  
</body>
</html>  