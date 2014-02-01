<html>
<head>
	<style type="text/css">
		h4 {color: red}
		h3 {color: black}
	</style>

  <title>Facturas y/o cargos pendientes!</title>
  <img src="cid:papelLogo" alt="NO_LOGO" width="135" hight="110" />
</head>
<body>
  <h3>Departamento de Crédito 
  	<br>
  	<br>Fecha: 		${fecha?date}
  	<br>Atención: 	${cliente}
  </h3>
  <p>Estimado cliente de acuerdo a nuestros registros su cuenta presenta retraso en las facturas que a continuación se listan.<br>Por este medio
  le hacemos un atento recordatorio para liquidar  a la brevedad  estos adeudos  y evitar el deterioro de su historial crediticio.


  
  <h4>Facturas vencidas:</h2>
  
  <table align="center"
		valign="middle"
		  width="800" border="1" cellspacing="1" cellpadding="1">
  
	<tr bgcolor="#999999">
	<th>Factura</th>
	<th>Importe</th>
	<th>Fecha</th>
	<th>Vencimiento</th>
	<th>F.Pago</th>
	<th>Atraso</th>
	<th>Cargo</th>
	<th>Descuento</th>
	<th>Importe Dscto</th>
	<th>A Pagar      </th>	
	
	<#list facturas as fac>
		<tr>
			<td align="left">${fac.numero}</td>
			<td align="right">${fac.totalSinDevolucionesAsDouble}</td>
			<td align="center">${fac.fecha?date}</td>			
			<td align="center">${fac.vencimiento?date}</td>
			<td align="center">${fac.credito.reprogramarPago?date}</td>
			<td align="center">${fac.atraso}</td>
			<td align="center">${fac.cargo}</td>			
			<td align="center">
				<#if (fac.atraso>=90) >
					0
				</#if>
				<#if (fac.atraso<90) >
					${ (fac.descuentoPactado-fac.cargo) }
				</#if>
			</td>
			<td align="right">
				<#if (fac.atraso>=90) >
					0
				</#if>
				<#if (fac.atraso<90) >
					${(fac.totalSinDevolucionesAsDouble*((fac.descuentoPactado-fac.cargo)/100)) } 
				</#if>
			</td>
			<td align="right">${fac.saldoEstimadoEnDouble} </td>												
			
		</tr>
	</#list>  
  </table>
  <p><b>Nota:</b> Los saldos y el descuento  están calculados al ${fecha?date}; estos últimos seguirán disminuyendo hasta liquidar el adeudo
  <br>
  <p><b>Nota:</b>Después de 90 días naturales contados a partir de la fecha de factura se pierde todo el descuento
  <br>
  <p> Quedando a sus ordenes para cualquier duda o aclaración
  
  <p>Atentamente
  <br>Gerencia de Cuentas por Cobrar
  <br>Tel: 5342-71-66 ext:7141
  
  
  
</body>
</html>  