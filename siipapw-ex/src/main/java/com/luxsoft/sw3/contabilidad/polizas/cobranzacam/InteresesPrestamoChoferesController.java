package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.util.Date;

import org.springframework.ui.ModelMap;


import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

public class InteresesPrestamoChoferesController extends ControladorDinamico{
	
	InteresesPrestamoCamionetaInicializador inicializador;
	
	public InteresesPrestamoChoferesController() {
		inicializador=new InteresesPrestamoCamionetaInicializador(getHibernateTemplate(),getJdbcTemplate());
		getProcesadores().add(new Proc_InteresesPrestamoCamioneta());
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {		
		Poliza pol=super.generar(fecha, referencia);
		pol.setDescripcion("Intereses prestamo choferes");
		pol.setTipo(Poliza.Tipo.INGRESO);
		return pol;
	}
	
}
