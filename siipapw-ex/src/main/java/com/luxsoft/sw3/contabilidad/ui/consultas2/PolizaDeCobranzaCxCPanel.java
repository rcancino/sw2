package com.luxsoft.sw3.contabilidad.ui.consultas2;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeAnticiposPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeAplicacionesPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeCortesDeTarjetaPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeFichasDeDepositoPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeIngresosPorDepositosAutorizadosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeNotasDeCreditoPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeOtrosGastosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeOtrosProductosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeVentasPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisSaldosAFavorPanel;


public class PolizaDeCobranzaCxCPanel extends PolizaDinamicaPanel{

	public PolizaDeCobranzaCxCPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	
	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			
			//CuentaContable cuenta =det.getCuenta();
			//String desc=det.getDescripcion2();
			
			String asiento=det.getAsiento();
		//	if(asiento.startsWith("COBRANZA")){
			
			if(det.getCuenta().getClave().equalsIgnoreCase("106")){
		
			
				AnalisisDeAplicacionesPanel.show(det.getPoliza().getFecha(),det.getReferencia());
			}else if(StringUtils.contains(asiento,"FICHA")){
				AnalisisDeFichasDeDepositoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"TARJETA")){
				AnalisisDeCortesDeTarjetaPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"DEPOSITO") ||StringUtils.contains(asiento,"POR IDENT")){
				AnalisisDeIngresosPorDepositosAutorizadosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"ANTICI")){
				AnalisisDeAnticiposPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(det.getDescripcion2(),"SAF:")){
				AnalisisSaldosAFavorPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"DIFERENCIAS")){				
				AnalisisDeOtrosGastosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(det.getDescripcion2(),"OI AJUSTE")){				
				AnalisisDeOtrosProductosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(det.getCuenta().getClave().equalsIgnoreCase("405")){
				AnalisisDeNotasDeCreditoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(det.getCuenta().getClave().equalsIgnoreCase("406")){
				AnalisisDeNotasDeCreditoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}
			
			
		}
	}
	
	

	
}
