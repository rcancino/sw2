package com.luxsoft.sw3.contabilidad.polizas.maquila;

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
public class ControladorDeMaquila extends ControladorDinamico{
	
	InicializadorMaquila inicializador; 
	
	public  ControladorDeMaquila() {
		setClase("MAQUILA");
		inicializador=new InicializadorMaquila(getHibernateTemplate(),getJdbcTemplate());
	//	getProcesadores().add(new Proc_EntradasAlMaquilador());	
		getProcesadores().add(new Proc_EntradaDeMaterialAlMaquilador());	
		
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza res=super.generar(fecha, referencia);
		res.setDescripcion("Maquila");
		res.setTipo(Poliza.Tipo.DIARIO);
		return res;
	}
	
	
	
	public static void main(String[] args) {
		ControladorDeMaquila c=new ControladorDeMaquila();
	  c.generar(DateUtil.toDate("02/01/2012"));
		
	}

		
}


