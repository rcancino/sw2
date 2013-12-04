package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.reportes.ReporteDeAlcancesForm.ReportForm.ParamLabelValue;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class VentasXLineaXDia extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
							
				ReportUtils.viewReport(ReportUtils.toReportesPath("BI/VentasXLineaXDia.jasper"), form.getParametros());
		
		}
		form.dispose();
	}
	
	public static String[] TIPO_DE_ORDENAMIENTO={
		"IMPORTE" 
		,"MILLARES" 
		,"KILOS" 
		};

	
	/**
	 * Forma para el reporte de Ventas por cliente
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		
		private Date fechaInicial;
		
		
		
		private final PresentationModel model;
		
		private JComponent jCliente;
		private JComponent jFechaIni;
		private JComboBox ordenBox;
		private JComboBox jOrigen;
		private JTextField jClientes;
		private JComboBox lineaControl;
	
		 
		private ParamLabelValue getParam(JComboBox box){
			return (ParamLabelValue)box.getSelectedItem();
		}
		
	
public  class ParamLabelValue{
			
			private final String name;
			private final Object value;
			
			public ParamLabelValue(String name, Object value) {			
				this.name = name;
				this.value = value;
			}

			public String getName() {
				return name;
			}

			public Object getValue() {
				return value;
			}
			
			public String toString(){
				return name;
			}
			
			public String stringValue(){
				return value.toString();
			}
			
			public Integer intValue(){
				return Integer.valueOf(stringValue());
			}
			
			public Number numberValue(){
				return (Number)value;
			}
			
			
		}


		public ReportForm() {
			super("Ventas Por Linea");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){
			jClientes=new JTextField();
			jFechaIni=Binder.createDateComponent(model.getModel("fechaInicial"));
			ordenBox=new JComboBox(TIPO_DE_ORDENAMIENTO);
			Object[] values={"CREDITO","CONTADO","TODOS"};
			jOrigen= new JComboBox(values);
			lineaControl=buildLineaControl();
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter.setValueClass(Double.class);
			
			
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			//CellConstraints cc=new CellConstraints();
			final FormLayout layout=new FormLayout(
					"l:40dlu,30dlu,60dlu, 3dlu, " +
					"l:40dlu,30dlu,p:g,2dlu,p,2dlu,p " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Linea",lineaControl,5);
			builder.nextLine();
			builder.append("Fecha", jFechaIni);
			builder.nextLine();
			builder.append("No. Clientes",jClientes);
			builder.nextLine();
			builder.append("Origen",jOrigen,3);
			builder.append("Orden",ordenBox);
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			
		
			parametros.put("LINEA", Integer.parseInt(getLinea()));
			parametros.put("FECHA_INI", model.getValue("fechaInicial"));
			int order=getOrden();
			parametros.put("ORDER", Integer.valueOf(order));
		
			
			String ori;
			if(jOrigen.getSelectedItem().equals("TODOS"))
				ori="%";
			else
				ori=(String) jOrigen.getSelectedItem();
			parametros.put("ORIGEN",ori);
			 Integer clientes=Integer.parseInt(jClientes.getText());
			parametros.put("NO_CLIENTES",clientes);
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		private int getOrden(){
			String orden=(String)ordenBox.getSelectedItem();
			if(orden.equals(TIPO_DE_ORDENAMIENTO[0])){
				return 8;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[1])){
				return 9;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[2])){
				return 10;
			}
			
			return 0;
		}
		

	
		

		public Date getFechaInicial() {
			return fechaInicial;
		}
		public void setFechaInicial(Date fechaInicial) {
			Object oldValue=this.fechaInicial;
			this.fechaInicial = fechaInicial;
			firePropertyChange("fechaInicial", oldValue, fechaInicial);
		}

	

		private String getLinea(){
			Linea selected=(Linea)lineaControl.getSelectedItem();
			if(selected!=null)
				return String.valueOf(selected.getId());
			return "%";
		}
		
		private JComboBox buildLineaControl() {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2.getHibernateTemplate().find("from Linea l order by l.nombre"));
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "nombre"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.STARTS_WITH);
			support.setCorrectsCase(true);
			return box;
		}
		 
		
	}
	
	public static void run(){
		VentasXLineaXDia action =new VentasXLineaXDia();
		action.execute();
		
	}
	
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				VentasXLineaXDia.run();
			}

		});
	}

}
