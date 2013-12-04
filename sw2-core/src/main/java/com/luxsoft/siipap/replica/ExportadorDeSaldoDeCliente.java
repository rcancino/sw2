package com.luxsoft.siipap.replica;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;


public class ExportadorDeSaldoDeCliente implements ReplicaExporter{
	
	private Logger logger=Logger.getLogger(getClass());
	
	String old="ACUCLI${mes}.ACU" +
			"[C" +
			"[${clienteClave} 3" +
			"[ACUCLI${mes}.NTX" +
			"[1" +
			"[ACL_PAGO?-1.00?N|ACLSDOACT ?${saldo}?N" +
			"[" +
			"[ACLCR" +
			"[F" +
			"[${year?c}";
	
	public Class getBeanClass() {
		return Cliente.class;
	}

	public String export(Object bean, Tipo tipo,Object...objects) {
		switch (tipo) {		
		case C:
			BigDecimal saldo=(BigDecimal)objects[0];
			return generarCambio((Cliente)bean,saldo);
		case A:
		case B:			
		default:
			return null;
		}
	}
	
	

	private String generarCambio(Cliente cliente,BigDecimal saldo){
		
		StringBuffer buf = new StringBuffer();
		char SEP = '[';
		
		// Seccion 1 Archivo
		Date fecha=new Date();
		String archivo=ExportUtils.resolverArchivo("ACUCLI", fecha);
		buf.append(archivo+".ACU");
		buf.append(SEP);
		
		// Seccion 2 TIPO de operacion
		buf.append(Tipo.C);
		buf.append(SEP);

		// Seccion 3 Llave del indice 1
		String clie=StringUtils.rightPad(cliente.getClave(), 7, ' ');
		buf.append(clie);
		buf.append(" 3");
		buf.append(SEP);

		// Seccion 4 Indices
		String indice=ExportUtils.resolverArchivo("ACUCLI", fecha);
		buf.append(indice+".NTX");
		buf.append(SEP);

		// Seccion 5 Induce activo o de uso
		buf.append("1");
		buf.append(SEP);

		// Seccion 6 Campos a afectar
		String[] campos={				
				"ACL_PAGO"
				,"ACLSDOACT"
				};		
		String[] valores={
				"-1.00"
				,saldo.toString()
			};
		String[] tipos={
				 "N" // ACL_PAGO
				,"N" // ACLSDOACT
				
		};
		for(int index=0;index<campos.length;){
			buf.append(campos[index]);
			buf.append("?");
			buf.append(valores[index]);
			buf.append("?");
			buf.append(tipos[index]);
			index++;
			if(index!=campos.length)
				buf.append("|");
		}
		buf.append(SEP);

		// Seccion 7 ?
		//buf.append("*");
		buf.append(SEP);

		// Seccion 8 ?
		buf.append("ACLCR");
		buf.append(SEP);

		// Seccion 9 ?
		buf.append("F");
		buf.append(SEP);

		// Seccion 10 Año ?
		
		int year = Periodo.obtenerYear(fecha);
		buf.append(year);
		String res = buf.toString();
		logger.info("String para exportar al cliente: \n"+res);
		return res;
	}
	
	public String export(Collection beans, Tipo tipo) {
		throw new UnsupportedOperationException("La exportacion de clientes no se puede hacer en bulk");
	}

	public String export(Object bean, Tipo tipo) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
