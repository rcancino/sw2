package com.luxsoft.siipap.pos.ui.alt_i;

import javax.swing.JPanel;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;

/**
 * Panel con las existencias de los productos
 * 
 * @author Ruben Cancino 
 *
 */
public class ListaDeProductos extends FilteredBrowserPanel<Existencia>{

	public ListaDeProductos() {
		super(Existencia.class);
		
	}

}
