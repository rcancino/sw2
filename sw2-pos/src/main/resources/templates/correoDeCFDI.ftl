<html>
<head>
	<style type="text/css">
		h4 {color: red}
		h3 {color: black}
	</style>

  <title>Comprobantes Fiscales Digitales (CFDI)</title>
  <h2>${empresa}</h2>
</head>
<body>
  <h3>Departamento de Cr�dito 
  	<br>
  	<br>Fecha: 		${fecha?date}
  	<br>Atenci�n: 	${cliente}
  </h3>
  <p>Anexo a este correo le hacemos llegar los siguientes comprobantes fiscales (CFDI)
  
  <table align="center"
		valign="middle"
		  width="800" border="1" cellspacing="1" cellpadding="1">
  
	<tr bgcolor="#999999">
	<th>Tipo</th>
	<th>Serie</th>
	<th>Folio</th>
	
	<th>Fecha</th>
	<th>Total</th>
	
	<#list cfds as fac>
		<tr>
			<td align="left">${fac.tipo}</td>
			<td align="center">${fac.serie}</td>
			<td align="center">${fac.folio}</td>
			
			<td align="center">${fac.log.creado?date}</td>
			<td align="right">${fac.totalAsString}</td>
			
			
		</tr>
	</#list>  
  </table>
  <p> Quedando a sus ordenes para cualquier duda o aclaraci�n
  
  <p>Atentamente
  <br>Gerencia de Cuentas por Cobrar 
  
  
  
  
</body>
</html>  