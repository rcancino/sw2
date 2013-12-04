package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

public class PolizaDeCXPComprasPanel extends PolizaDinamicaPanel{

	public PolizaDeCXPComprasPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	public void imprimirPoliza(){
		if(getSelectedObject()!=null){
			imprimirPoliza((Poliza)getSelectedObject());
		}
	}
	
	public void generar(){
		Date fecha=SelectorDeFecha.seleccionar();
		if(fecha!=null){
			Periodo periodo=Periodo.getPeriodoDelMesActual(fecha);
			System.out.println("Periodo: "+fecha);
			List<Poliza> res=controller.generar(periodo);
			super.insertarPolizas(res);
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

	@Override
	protected List<Poliza> findData() {
		String hql="from Poliza p " +
				" where p.clase=? " +
				"  and p.fecha between ? and ?" +
				"  and p.descripcion like ?";
		Object[] params={getClase(),periodo.getFechaInicial(),periodo.getFechaFinal(),"COMPRAS:%"};
		return ServiceLocator2
			.getHibernateTemplate()
			.find(hql,params);
	}
}
