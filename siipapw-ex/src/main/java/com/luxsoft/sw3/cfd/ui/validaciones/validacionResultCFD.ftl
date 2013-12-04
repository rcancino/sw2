<html>
<head>
	<style type="text/css">
		h4 {color: red}
		h3 {color: black}
	</style>

  <title>Validación de comprobante fiscal digital</title>
</head>
<body>
  <h3>Departamento de Cuentas por Pagar 
  	<br>
  	<br>Fecha/Hora de validación: 	${fecha?datetime}
  	<br>Archivo XML: 				${archivo}
  	<br>Versión del comprobante: 	${version}
  </h3>
  <p>Resultado de la validación del CFD (I)</p>
  
  <table align="center"
		valign="middle"
		  width="800" border="1" cellspacing="1" cellpadding="1">
  
	<tr >
	<th>Tipo</th>
	<th>Descripción</th>
	<th>Resultado</th>
	<#list resultados as res>
		<tr>
			<td align="left">${res.tipo}</td>
			<td align="left">${res.descripcion}</td>
			<td align="center">${res.resultado}</td>		
			
		</tr>
	</#list>
   </table>
  
  
 
  
  
  
</body>
</html>  