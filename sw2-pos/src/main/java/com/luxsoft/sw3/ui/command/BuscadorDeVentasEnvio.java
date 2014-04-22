package com.luxsoft.sw3.ui.command;

import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;

import antlr.collections.List;

import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.selectores.SelectorDeCargos;

public class BuscadorDeVentasEnvio extends AbstractAction{

	public void actionPerformed(ActionEvent e) {
		String res=JOptionPane.showInputDialog(Application.isLoaded()?Application.instance().getMainFrame():null,"Factura");
		if(StringUtils.isNotBlank(res)){
			if(NumberUtils.isNumber(res)){
				Long folio=Long.valueOf(res);
				Venta found=SelectorDeCargos.buscar(folio,Services.getInstance().getConfiguracion().getSucursal().getId());
				if(found!=null){
					String id= found.getId();
					final Map map=new HashMap();
					map.put("ID", id);
					
					String hql="from Entrega e where e.factura.id=?";
				java.util.List<Entrega> ent=  Services.getInstance().getHibernateTemplate().find(hql,id);
				System.err.println("Parametros de reporte:"+map);
				
				if(ent.isEmpty()){
					//Revisa si la Factura no esta asignada
						//System.err.println("No Tiene Entrega");
						ReportUtils2.runReport("embarques/FacturaPorAsignar.jasper", map);
					}else{
					//	La factura si esta asignada
						//System.err.println("Si tiene Entrega");
						if(ent.get(0).isParcial()){
					// Tiene asignaciones parciales
						//	System.err.println("Es parcial");
							ReportUtils2.runReport("embarques/EntregaParcialFactura.jasper", map);
							
						}else{
					// La asignacion fue total
							//System.err.println("Entrega Total");
							ReportUtils2.runReport("embarques/EntregaTotalFactura.jasper", map);
						}
					}
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		new BuscadorDeVentasEnvio().actionPerformed(null);
	}

}
