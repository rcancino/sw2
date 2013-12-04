package com.luxsoft.sw3.contabilidad.ui.consultas;

import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Panel para la administración de la poliza de Anticipos de gastos
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeAnticiposGastosPanel extends PanelGenericoDePoliza{

	public PolizaDeAnticiposGastosPanel() {
		super();
		setClase("ANTICIPOS GASTOS");
		setManager(new PolizaDeAnticiposGastosController());
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			MessageUtils.showMessage("En construcción taladreo", "Compras - Almacen");
		} 
			
	}
	
	
	
	
	

}
