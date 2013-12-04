package com.luxsoft.siipap.ventas;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.ventas.model.VentaRow;

/**
 * Modelo principal de estado para las operaciones de Ventas
 * Funciona como modelo principal para diversas operaciones y controloadores
 * del modulo de ventas
 * 
 * Probablemente hagamos que se incorpore directamente a spring
 * implementando intercaces especificas de ese framework
 * 
 * @author Ruben Cancino
 *
 */
public class VentasMainModel {
	
	private EventList<VentaRow> ventasSource;
	
	
	public VentasMainModel(){
		init();
	}
	
	protected void init(){
		ventasSource=GlazedLists.threadSafeList(new BasicEventList<VentaRow>());
	}

	
	/**
	 * Regresa la no modificable lista general de ventas cargada 
	 * 
	 * @return
	 */
	public EventList<VentaRow> getVentasSource() {
		return GlazedLists.readOnlyList(ventasSource);
	}	

}
