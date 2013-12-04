package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.model.Periodo;

import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;


public class PolizaDeCierreAnualPanel extends PolizaDinamicaPanel{

	public PolizaDeCierreAnualPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	protected Comparator getDefaultDetailComparator(){
		Comparator c1=GlazedLists.beanPropertyComparator(PolizaDet.class, "referencia","descripcion2","tipo");
		return c1;
		//return GlazedLists.chainComparators(c1,new CuentaComparator());
		
	}
	
	@Override
	protected void manejarPeriodo() {
		Date fecha=new Date();
		int year=Periodo.obtenerYear(fecha)-1;
		periodo=Periodo.getPeriodoDelYear(year);
	}
	
	public void generar(){
		Date fecha=periodo.getFechaFinal();
		DateFormat df=new SimpleDateFormat("dd/MM");
		if(!df.format(fecha).equals("31/12")){
			MessageUtils.showMessage("Solo se puede ejecutar en el periodo  31 de diciembre", "Cierre anual");
			return;
		}
		if(fecha!=null){
			try {
				List<Poliza> res=controller.generar(fecha);
				insertarPolizas(res);
			} catch (Exception e) {				
				logger.error(e);
				e.printStackTrace();
				MessageUtils.showMessage("Error: "+ExceptionUtils.getRootCauseMessage(e), "Generación de poliza");
			}
			
		}
	}
	
	public void imprimirPoliza(){
		if(getSelectedObject()!=null){
			imprimirPoliza((Poliza)getSelectedObject());
		}
	}
	
	public void salvar(){
		for(Object object:getSelected()){
			Poliza selected=(Poliza)object;
			int index=source.indexOf(selected);
			if(index!=-1){
				selected=salvar(selected);
				//source.set(index, selected);
			}
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
