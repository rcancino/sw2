package com.luxsoft.sw3.contabilidad.ui.consultas;

import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Panel para la administración de polizas de descuentos,devoluciones y bonificaciones
 * en el modulo de compras
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeDescuentosEnComprasPanel extends PanelGenericoDePoliza{

	public PolizaDeDescuentosEnComprasPanel() {
		super();
		setClase("COMPRAS DESCUENTOS");
		setManager(new PolizaDeDescuentosEnComprasController());
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			CuentaContable cuenta =det.getCuenta();
			String desc=det.getDescripcion2();
			
		} 
			
	}
	
	
	
	
	

}
