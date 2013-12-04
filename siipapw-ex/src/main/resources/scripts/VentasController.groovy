package com.luxsoft.sw3.contabilidad.polizas.ventas

import org.springframework.ui.ModelMap;

import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;

class VentasController extends ControladorDinamico{
	

	void cargar(ModelMap model) {
		println 'Cargando datos de la base de datos.......'
		
	}	
	void procesar(Poliza arg0, ModelMap model) {
		println 'Procesando desde groovy..........'
	}

}
