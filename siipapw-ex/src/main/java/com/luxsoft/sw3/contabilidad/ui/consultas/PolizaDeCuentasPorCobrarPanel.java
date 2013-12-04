package com.luxsoft.sw3.contabilidad.ui.consultas;

import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;

public class PolizaDeCuentasPorCobrarPanel extends PanelGenericoDePoliza{
	
	

	public PolizaDeCuentasPorCobrarPanel() {
		super();
		setClase("CXC CREDITO");
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
				AnalisisDeOtrosGastosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("OI Ajustes automaticos menores a $10")){
				AnalisisDeOtrosProductosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(asiento.startsWith("CXC Credito") && cuenta.getClave().equalsIgnoreCase("106")){
				
				AnalisisDeAplicacionesPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}
			
		}
	}
	

}
