package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.collections.ComparatorUtils;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;


public class PolizaDeComplementoComprasPanel extends PolizaDinamicaPanel{

	public PolizaDeComplementoComprasPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	protected Comparator getDefaultDetailComparator(){
		Comparator c1=GlazedLists.beanPropertyComparator(PolizaDet.class, "referencia","descripcion2","tipo");
		return c1;
		//return GlazedLists.chainComparators(c1,new CuentaComparator());
		
	}
	
	private class CuentaComparator implements Comparator<PolizaDet>{
		
		private final Map<String,Integer> order;
		
		public CuentaComparator(){
			order=new HashMap<String, Integer>();
			order.put("119", 10);
			order.put("117", 1);
			order.put("200", 8);
			order.put("205", 9);
		}
		
		public int compare(PolizaDet o1, PolizaDet o2) {
			String c1=o1.getCuenta().getClave();
			String c2=o2.getCuenta().getClave();
			System.out.println("ordenando :"+c1+"  "+c2);
			if(order.containsKey(c1.trim())){
				Integer orden1=order.get(c1);
				if(order.containsKey(c2)){
					System.out.println("ordenando :"+c1+"  "+c2);
					Integer orden2=order.get(c2);
					return orden1.compareTo(orden2);
				}else
					return 1;
			}else
				return 0;
			
		}
		
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
	
	
}
