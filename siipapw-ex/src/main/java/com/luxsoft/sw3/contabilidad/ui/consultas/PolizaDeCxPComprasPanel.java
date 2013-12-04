package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;

public class PolizaDeCxPComprasPanel extends PanelGenericoDePolizasMultiples{
	
	private PolizaDeCxPComprasController controller;
	
	public PolizaDeCxPComprasPanel() {
		super();
		setClase("CXP COMPRAS");
		controller=new PolizaDeCxPComprasController();
	}
	
	@Override
	public void drill(PolizaDet det) {
		MessageUtils.showMessage("En construcción taladreo", "Compras - Almacen");
	}

	@Override
	public List<Poliza> generarPolizas(final Date fecha) {
		return controller.generaPoliza(fecha);
	}
	public void imprimirPoliza(){
		if(getSelectedObject()!=null){
			imprimirPoliza((Poliza)getSelectedObject());
		}
	}
	
	
	public void imprimirPoliza(Poliza bean){
		
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("ORDEN", "ORDER BY 12,3");
		String path=ReportUtils.toReportesPath("contabilidad/Poliza.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}

}
