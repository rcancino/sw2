package com.luxsoft.sw3.contabilidad.ui.consultas2;

/*import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;*/
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;


public class PolizaDeMaquilaPanel extends PolizaDinamicaPanel{

	public PolizaDeMaquilaPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	/*
	protected Comparator getDefaultDetailComparator(){
		Comparator c1=GlazedLists.beanPropertyComparator(PolizaDet.class, "referencia","descripcion2","tipo");
		return c1;		
	}
	

	
	public void imprimirPoliza(){
		if(getSelectedObject()!=null){
			imprimirPoliza((Poliza)getSelectedObject());
		}
	}
	
	
	public void imprimirPoliza(Poliza bean){
		
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("ORDEN", "ORDER BY D.REFERENCIA,D.DESCRIPCION2,TIPO,D.RENGLON");
		String path=ReportUtils.toReportesPath("contabilidad/Poliza.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}
	*/
	
}
