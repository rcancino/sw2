package com.luxsoft.sw3.contabilidad.ui.consultas;

import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Panel para la administración de la poliza de Inventarios
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeInventarioPanel extends PanelGenericoDePoliza{

	public PolizaDeInventarioPanel() {
		super();
		setClase("POLIZA DE INVENTARIO");
		setManager(new PolizaDeInventarioController());
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			MessageUtils.showMessage("En construcción taladreo", "Compras - Almacen");
		} 
			
	}
	
	
	
	
	

}
