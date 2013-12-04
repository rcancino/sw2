 package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.util.List;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de CXP Compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeCXPCompras extends ControladorDinamico{
	
	//InicializadorCompras inicializador;
	Proc_CxPCompras proc;
	
	public  ControladorDeCXPCompras() {
		setClase("PAGOS");
		proc=new Proc_CxPCompras();
	}
	
	@Override
	public List<Poliza> generar(Periodo periodo) {
		return proc.generaPoliza(periodo);
	}
	
	
	
	public static void main(String[] args) {
		ControladorDeCXPCompras c=new ControladorDeCXPCompras();
		c.generar(DateUtil.toDate("04/11/2011"));
		
	}

		
}


