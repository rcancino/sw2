package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.util.Date;

import javax.swing.SwingWorker;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Panel para la administración de la poliza de Compras - Almancen
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeProvisionDeGastosPanel extends PanelGenericoDePoliza{
	
	PolizaDeProvisionDeGastosController controller;

	public PolizaDeProvisionDeGastosPanel() {
		super();
		setClase("PROVISION DE GASTOS");
		controller=new PolizaDeProvisionDeGastosController();
		setManager(controller);
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			CuentaContable cuenta =det.getCuenta();
			String desc=det.getDescripcion2();
			final Date fecha=det.getPoliza().getFecha();
		} 
			
	}
	
	public void insert(){
		final Date fecha=SelectorDeFecha.seleccionar();
		if(fecha!=null){
			final SwingWorker<Poliza, String> worker=new SwingWorker<Poliza, String>(){
				protected Poliza doInBackground() throws Exception {
					return controller.generar(Periodo.getPeriodoEnUnMes(fecha));
				}
				protected void done() {
					try {
						Poliza res=get();
						source.add(res);
						afterInsert(res);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			};
			executeLoadWorker(worker);
		}
	}

}
