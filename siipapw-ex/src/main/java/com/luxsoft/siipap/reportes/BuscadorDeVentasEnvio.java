package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;




import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;


import com.luxsoft.siipap.cxc.ui.consultas.CargoView;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeCargos;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.embarque.Entrega;


public class BuscadorDeVentasEnvio extends AbstractAction{

	public void actionPerformed(ActionEvent e) {
		
		
		final BuscarVentaEnvioForm form=new BuscarVentaEnvioForm();
		form.open();
		if(!form.hasBeenCanceled()){
			final Cargo res=form.commit();			
			List<String> props=new ArrayList<String>();
			
			if((res.getDocumento()!=null)&& (res.getDocumento().intValue()>0))
				props.add("documento");
			if((res.getNumeroFiscal()!=null)&& (res.getNumeroFiscal().intValue()>0))
				props.add("numeroFiscal");
			if(res.getSucursal()!=null)
				props.add("sucursal");
			Cargo found=null;
			if(res.getDocumento()!=null && res.getSucursal()!=null){
				 found=SelectorDeCargos.buscar(res.getDocumento(), res.getSucursal().getId(), res.getNumeroFiscal());
			}else{
				 found=SelectorDeCargos.buscar(res, props.toArray(new String[1]));
				//Cargo found=SelectorDeCargos.buscar(res.getSucursal().getId(),res.getDocumento(),res.getNumeroFiscal());
			}
			
			if(found!=null){
				String id= found.getId();
				final Map map=new HashMap();
				map.put("ID", id);
				
				String hql="from Entrega e where e.factura.id=?";
				java.util.List<Entrega> ent=  ServiceLocator2.getHibernateTemplate().find(hql,id);
				System.err.println("Parametros de reporte:"+map +"---------"+ent);
				
				if(ent.isEmpty()){
					//Revisa si la Factura no esta asignada
						//System.err.println("No Tiene Entrega");
						ReportUtils.viewReport(ReportUtils.toReportesPath("embarques/FacturaPorAsignar.jasper"), map);
						
					}else{
					//	La factura si esta asignada
						//System.err.println("Si tiene Entrega");
						if(ent.get(0).isParcial()){
					// Tiene asignaciones parciales
						//	System.err.println("Es parcial");
							ReportUtils.viewReport(ReportUtils.toReportesPath("embarques/EntregaParcialFactura.jasper"), map);
						}else{
					// La asignacion fue total
							//System.err.println("Entrega Total");
							ReportUtils.viewReport(ReportUtils.toReportesPath("embarques/EntregaTotalFactura.jasper"), map);
						}
					}
				
			}
			

				
		}
		
	}
	
	
	
	public static class BuscarVentaEnvioForm extends SXAbstractDialog{
		
		private ValueHolder sucursalHolder=new ValueHolder(null);
		private ValueHolder numeroHolder=new ValueHolder(null);
		private ValueHolder fiscalHolder=new ValueHolder(null);
		

	   public BuscarVentaEnvioForm()  {
			super("Ventas de Envío");
		}

		@Override
		protected JComponent buildContent() {
			JPanel content=new JPanel(new BorderLayout());
			content.add(buildForm(),BorderLayout.CENTER);
			content.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return content;
		}
		
		private JComponent buildForm(){
			
			FormLayout layout=new  FormLayout("p,3dlu,70dlu");			
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Número",getControl("numero"));
			builder.append("Fiscal",getControl("numeroFiscal"));
			builder.append("Sucursal",getControl("sucursal"));
			
			return builder.getPanel();
		}
		
		private JComponent getControl(String property){
			if("numero".equals(property)){
				return BasicComponentFactory.createLongField(buffer(numeroHolder),0);
			}else if("numeroFiscal".equals(property)){
				return BasicComponentFactory.createIntegerField(buffer(fiscalHolder),0);
			}else if("sucursal".equals(property)){
				 List<Sucursal> sucursales=ServiceLocator2.getLookupManager().getSucursales();
				 sucursales.add(null);
				 SelectionInList sl=new SelectionInList(sucursales,buffer(sucursalHolder));
				 return BasicComponentFactory.createComboBox(sl,new SucursalRenderer());
			}else
				return null;
		}
		
		public Cargo commit(){
			Venta v=new Venta();
			v.setDocumento((Long)numeroHolder.getValue());
			v.setSucursal((Sucursal)sucursalHolder.getValue());
			v.setNumeroFiscal((Integer)fiscalHolder.getValue());
			return v;
		}
		
		
		private class SucursalRenderer extends DefaultListCellRenderer{
			
			public void setText(String t){
				if(t.isEmpty())
					super.setText("TODAS");
				else
					super.setText(t);
			}
		}
		
		
		
	}
	
	
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		new BuscadorDeVentasEnvio().actionPerformed(null);
	}

}
