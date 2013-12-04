package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.util.Date;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de pago de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeChequeEnTransito extends ControladorDinamico{
	
	
	Proc_DeCobroDeChequeEnTransito proc_chequeTransito;
	
	public  ControladorDeChequeEnTransito() {
		
		proc_chequeTransito=new Proc_DeCobroDeChequeEnTransito(getHibernateTemplate());
	}
	
	@Override
	public void cargar(ModelMap model) {
				
	}
	
	public List<Poliza> generar(Date fecha) {
		return proc_chequeTransito.generaPoliza(fecha);
	}
	
	
	
	public static void main(String[] args) {
		ControladorDeChequeEnTransito c=new ControladorDeChequeEnTransito();
		c.generar(DateUtil.toDate("16/11/2011"));
		
	}

		
}


