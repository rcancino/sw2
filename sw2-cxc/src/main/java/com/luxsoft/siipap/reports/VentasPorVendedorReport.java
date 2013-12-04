package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

import com.luxsoft.siipap.ventas.model.Vendedor;

/**
 * 
 * 
 * @author OCTAVIO
 *
 */
public class VentasPorVendedorReport extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("BI/VentasPorVendedor.jasper"), form.getParametros());
			System.err.println("Si debió imprimir el reporte");
		}
		form.dispose();
	}
	

	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		
		private final PresentationModel model;
		private Date fechaInicial;
		private Date fechaFinal;
		private JComponent FechaIni;
		private JComponent FechaFin;
		private JTextField vendedor;
		private JComboBox tipoVenta;
		private JComboBox vendedorBox;
		
		
		
	

		public JComboBox getTipoVenta() {
			return tipoVenta;
		}

		public ReportForm() {
			super("Cobranza Crédito");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){			
			FechaIni=Binder.createDateComponent(model.getModel("fechaInicial"));
			FechaFin=Binder.createDateComponent(model.getModel("fechaFinal"));
			tipoVenta=new JComboBox(Order2.values());
		
			Object[] vends=ServiceLocator2.getCXCManager().getVendedores().toArray(new Vendedor[0]);
			vendedorBox=new JComboBox(vends);
						
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout(
					"l:40dlu,3dlu,p, 3dlu, " +
					"l:40dlu,3dlu,p:g " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",FechaIni,5);
			builder.nextLine();
			builder.append("Fecha Final",FechaFin,5);
			builder.append("Vendedor",vendedorBox,5);
			builder.append("TipoVenta",tipoVenta,5);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI",model.getValue("fechaInicial") );
			parametros.put("FECHA_FIN",model.getValue("fechaFinal") );	
			Order2 om=(Order2)tipoVenta.getSelectedItem();
			parametros.put("ORIGEN",om.getValor().toString());
			Vendedor v=(Vendedor)vendedorBox.getSelectedItem();
			String id=v.getId().toString();
			parametros.put("VENDEDOR",id);
			System.out.println("Parametros********************************: "+parametros);
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
	

		
	}
	
	private enum Order2{
		credito("CREDITO","CRE"),
		contado("CONTADO","%M%"),
		ambos("AMBOS","%"),;
		
		private final String item;
		private final String valor;
		
		private Order2(final String item, final String valor) {
			this.item = item;
			this.valor = valor;
		}
		
		public String toString(){
			return item;
		}
		
		public String getValor(){
			return valor;
		}
		
		public static Order2 getOrder2(String id){
			for(Order2 c:values()){
				if(c.getValor()==id)
					return c;
			}
			return null;
		}
		
	}
	
	public static void run(){
		VentasPorVendedorReport action=new VentasPorVendedorReport();
		action.execute();
	}
	
	public static void main(String[] args) {
	
		VentasPorVendedorReport action=new VentasPorVendedorReport();
		action.execute();
	}

}
