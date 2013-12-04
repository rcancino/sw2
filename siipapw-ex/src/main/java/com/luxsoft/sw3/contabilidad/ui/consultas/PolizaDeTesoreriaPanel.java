package com.luxsoft.sw3.contabilidad.ui.consultas;

import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Panel para la administración de la poliza de Compras - Almancen
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeTesoreriaPanel extends PanelGenericoDePoliza{

	public PolizaDeTesoreriaPanel() {
		super();
		setClase("POLIZA DE TESORERIA");
		setManager(new PolizaDeTesoreriaController());
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			MessageUtils.showMessage("En construcción taladreo", "Compras - Almacen");
		} 
			
	}
	
	
	
	
	

}
