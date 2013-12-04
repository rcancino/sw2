package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
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
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisSaldosAFavorPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.FilterBrowserDialog;

/**
 * Panel para el mantenimiento de polizas de cobranza camioneta
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeCobranzaCamionetaPanel extends PolizaDinamicaPanel{

	public PolizaDeCobranzaCamionetaPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	@Override
	public void drill(final PolizaDet det) {
		if(det!=null){
			
			CuentaContable cuenta =det.getCuenta();
			String desc=det.getDescripcion2();
			
			String asiento=det.getAsiento();
			if(asiento.startsWith("COBRANZA FICHA")){
				final AnalisisDeFichasDeDepositoPanel browser=new AnalisisDeFichasDeDepositoPanel(){
					@Override
					protected List<Ficha> findData() {
						String hql="from Ficha f where f.fecha=?  " +
								" and f.corte is not null " +
								" and f.cancelada is null " +
								" and f.origen=\'CAM\'" +
								" and f.sucursal.nombre=?";
						Object[] params={det.getPoliza().getFecha(),det.getReferencia2()};
						return getHibernateTemplate().find(hql,params);
					}
				};
				
				FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
				dialog.open();
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
			}else if(asiento.startsWith("Cobranza CAM") && cuenta.getClave().equalsIgnoreCase("105")){
				AnalisisDeAplicacionesPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(cuenta.getClave().equalsIgnoreCase("405")){
				AnalisisDeNotasDeCreditoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}
			
		}
	}
	
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
}
