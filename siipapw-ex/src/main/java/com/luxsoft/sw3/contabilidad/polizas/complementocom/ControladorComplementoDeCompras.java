package com.luxsoft.sw3.contabilidad.polizas.complementocom;

import java.util.Date;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorComplementoDeCompras extends ControladorDinamico{
	
	InicializadorComplementoCom inicializador; 
	
	public  ControladorComplementoDeCompras() {
		inicializador=new InicializadorComplementoCom(getHibernateTemplate(),getJdbcTemplate());
		
		getProcesadores().add(new Proc_ComplementoHojeo());
		getProcesadores().add(new Proc_ComplementoFlete());
		getProcesadores().add(new Proc_ComplementoTransformacion());
		
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza res=super.generar(fecha, referencia);
		res.setDescripcion("Compras Complemento");
		res.setTipo(Poliza.Tipo.DIARIO);
		return res;
	}
	
	public static void main(String[] args) {
		ControladorComplementoDeCompras c=new ControladorComplementoDeCompras();
		c.generar(DateUtil.toDate("04/06/2012"));
		
	}

		
}


