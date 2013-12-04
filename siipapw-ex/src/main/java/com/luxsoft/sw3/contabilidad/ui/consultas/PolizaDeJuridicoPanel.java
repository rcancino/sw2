package com.luxsoft.sw3.contabilidad.ui.consultas;

import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.PolizaDeJuridicoManager;

public class PolizaDeJuridicoPanel extends PanelGenericoDePoliza{
	
	

	public PolizaDeJuridicoPanel() {
		super();
		setClase("CXC TRAMITE JURIDICO");
		setManager(new PolizaDeJuridicoManager());
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			
			CuentaContable cuenta =det.getCuenta();
			String desc=det.getDescripcion2();
			//String desc1=det.getDescripcion();
			String asiento=det.getAsiento();
			if(cuenta.getClave().equalsIgnoreCase("401")){
				AnalisisDeVentasPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("FICHA")){
				AnalisisDeFichasDeDepositoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("CORTE")){
				AnalisisDeCortesDeTarjetaPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("TRANS") ||desc.startsWith("DEPO")){
				AnalisisDeIngresosPorDepositosAutorizadosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("ANTICI")){
				AnalisisDeAnticiposPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("Saldo a Favor")){
				AnalisisSaldosAFavorPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("OG Ajustes automaticos menores a $1")){
				System.out.println("Drill ajustes automaticos <1");
				AnalisisDeOtrosGastosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("OI Ajustes automaticos menores a $10")){
				System.out.println("Drill ajustes automaticos <10");
				AnalisisDeOtrosProductosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(asiento.startsWith("Cobranza CAM") && cuenta.getClave().equalsIgnoreCase("105")){
				AnalisisDeAplicacionesPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}
			
		}
	}
	
	

}
