package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXDatePicker;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.compras.model.Compra.TipoPedido;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.ParamLabelValue;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para ejecutar el reporte General de alcances
 * 
 * @author Ruben Cancino
 *
 */
public class ReporteDePedidosNoFacturados extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			
			POSDBUtils.whereWeAre();
			System.out.println("Parametros enviados: "+form.getParametros());
			ReportUtils2.runReport("ventas/PedidosNoFacturados.jasper", form.getParametros());
			
				
		}
		form.dispose();
		
	}
	
	public static void run(){
		ReporteDePedidosNoFacturados action=new ReporteDePedidosNoFacturados();
		action.execute();
	}
	
	public static String[] TIPO_DE_ORDENAMIENTO={
		"ANTIGÜEDAD"
		,"CLIENTE"
		
		};
	
	public static String[] ORDENAMIENTO={"ASCENDENTE","DESCENDENTE"};
	
	/**
	 * Forma especial para el reporte
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		
		
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		
		private JComboBox ordenBox;
		private JComboBox formaBox;
		
	
		
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
			super("Reporte general de alcances");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){		
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
		
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter.setValueClass(Double.class);
			//formatter.setMaximum(new Integer(0));
			
			ordenBox=new JComboBox(TIPO_DE_ORDENAMIENTO);
			formaBox=new JComboBox(ORDENAMIENTO);
			
				}
			
			

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu," +
					"p,2dlu,70dlu:g,3dlu," +
					"p,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.nextLine();
			builder.append("Orden",ordenBox);
			builder.nextLine();
			builder.append("Forma ",formaBox);
			builder.nextLine();
		    builder.nextLine();
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		
		
		
		
		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			int order=getOrden();
			parametros.put("ORDEN", String.valueOf(order));
			parametros.put("FORMA",getForma() );	
		}
		
		private int getOrden(){
			String orden=(String)ordenBox.getSelectedItem();
			if(orden.equals(TIPO_DE_ORDENAMIENTO[0])){
				return 7;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[1])){
				return 2;
			}
			return 0;
		}
		
		private String getForma(){
			String s=(String)formaBox.getSelectedItem();
			return s.startsWith("ASC")?"ASC":"DESC";
		}
		
		

		public Map<String, Object> getParametros() {
			return parametros;
		}
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				ReporteDePedidosNoFacturados.run();
				//System.exit(0);
			}

		});
	}
	
	

}
