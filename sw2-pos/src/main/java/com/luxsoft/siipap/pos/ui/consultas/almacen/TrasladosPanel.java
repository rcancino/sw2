package com.luxsoft.siipap.pos.ui.consultas.almacen;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;

/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class TrasladosPanel extends FilteredBrowserPanel<Inventario>{

	public TrasladosPanel() {
		super(Inventario.class);
		
	}
	
	protected void init(){		
		addProperty("sucursal.nombre","fecha","clave","descripcion","producto.linea.nombre","unidad.nombre","kilos","cantidad","comentario");
		addLabels("sucursal","fecha","concepto","comentario");
		installTextComponentMatcherEditor("Sucursal", new String[]{"sucursal.nombre"});
		manejarPeriodo();
	}

}
