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
 *
 * @author Ruben Cancino
 *
 */
public class PolizaDeInteresesPrestamoChoferesPanel extends PolizaDinamicaPanel{

	public PolizaDeInteresesPrestamoChoferesPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	@Override
	public void drill(final PolizaDet det) {
		
	}
	
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
}
