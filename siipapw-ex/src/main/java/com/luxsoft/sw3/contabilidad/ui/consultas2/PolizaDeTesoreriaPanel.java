package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;


public class PolizaDeTesoreriaPanel extends PolizaDinamicaPanel{
	
	
	public PolizaDeTesoreriaPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	public void generar(){
		Date fecha=SelectorDeFecha.seleccionar();
		if(fecha!=null){
			Periodo periodo=Periodo.getPeriodoDelMesActual(fecha);
			List<Poliza> res=controller.generar(periodo);
			super.insertarPolizas(res);
		}
	}
	
	

	
}
