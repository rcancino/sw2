package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;


/**
 * Forma para ejecutar el reporte General de alcances
 * 
 * @author Ruben Cancino
 *
 */
public class BajaEnVentas extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			
			System.out.println("Parametros enviados: "+form.getParametros());
			ReportUtils.viewReport(ReportUtils.toReportesPath("BI/BajaEnVentas.jasper"), form.getParametros());
			
		/*	if (form.sinVentas.isSelected()){
			System.out.println("Parametros enviados: "+form.getParametros());
			ReportUtils.viewReport(ReportUtils.toReportesPath("BI/ClienteSinVenta.jasper"), form.getParametros());
			}
			else {
				System.out.println("Parametros enviados: "+form.getParametros());
				ReportUtils.viewReport(ReportUtils.toReportesPath("BI/BajaEnVentas.jasper"), form.getParametros());
			}
				*/
		}
		form.dispose();
		
	}
	
	public static void run(){
		BajaEnVentas action=new BajaEnVentas();
		action.execute();
	}
	
	public static String[] TIPO_DE_ORDENAMIENTO={
		"PROMEDIO" 
		,"PORCENTAJE" 
		,"CLIENTE" 
		,"TIPO" 
		,"PERIODO" 
		,"ULT.VENTA"
		};
	
	public static String[] ORDENAMIENTO={"DESCENDENTE","ASCENDENTE"};
	
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
		//private JFormattedTextField diasAConsiderar;
		private JLabel dias;
		private JComboBox ordenBox;
		private JComboBox formaBox;
		private JTextField ventaNetaMayora;
		private JTextField porcentaje;
		//private JCheckBox sinVentas;
		private JComboBox jOrigen;
	
		
		
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
			super("Reporte De Baja En Ventas");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){		
			fechaInicial=new JXDatePicker();
			Date ini=DateUtils.addMonths(new Date(), -2);
			fechaInicial.setDate(ini);
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter.setValueClass(Double.class);
			//formatter.setMaximum(new Integer(0));
			//diasAConsiderar=new JFormattedTextField(formatter);
			//diasAConsiderar.setValue(new Double(0));
			dias =new JLabel("Dias a considerar:  30");
			ordenBox=new JComboBox(TIPO_DE_ORDENAMIENTO);
			formaBox=new JComboBox(ORDENAMIENTO);
			ventaNetaMayora=new JTextField();
			//ventaNetaMayora.setValue(new Double(0));
			//sinVentas=new JCheckBox("",false);
			Object[] values={"CREDITO","CONTADO","TODOS"};
			jOrigen= new JComboBox(values);
			porcentaje=new JTextField();
			//porcentaje.setValue(new Double(0));
			
			
			
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
			
			/*builder.append("Sucursal",sucursalControl,5);			
			builder.append("Todas",todasLasSucursales);
			
			builder.nextLine();
			builder.append("Linea",lineaControl,5);
			builder.nextLine();*/
			builder.append("Orden",ordenBox);
			builder.append("Forma ",formaBox);
			builder.nextLine();
			//builder.append("Dias a Considerar",diasAConsiderar);
			builder.append(dias,3);
			builder.append("Venta Mayor a",ventaNetaMayora);
			builder.nextLine();
			builder.append ("Origen",jOrigen);
			builder.append("Porcentaje",porcentaje);
			//builder.append("Sin Ventas",sinVentas);
			
			/*builder.append("Filtro ",filtrosBox);
			builder.append("Meses a filtrar",mesesF);			
			builder.nextLine();
			builder.append("Tipo",filtro2Box);
			builder.nextLine();*/
			
						
			
			
			
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
			parametros.put("ORDER", Integer.valueOf(order));
			parametros.put("FORMA",getForma() );
			//parametros.put("DIAS", Integer.parseInt(diasAConsiderar.getText()));
			parametros.put("VALOR_VENTA",BigDecimal.valueOf(Double.parseDouble((ventaNetaMayora.getText()))));
			parametros.put("PORCENTAJE",Double.parseDouble(porcentaje.getText()));
			String ori;
			if(jOrigen.getSelectedItem().equals("TODOS"))
				ori="%";
			else
				ori=(String) jOrigen.getSelectedItem();
			parametros.put("ORIGEN",ori);
			
						
		}
		
		private int getOrden(){
			String orden=(String)ordenBox.getSelectedItem();
			if(orden.equals(TIPO_DE_ORDENAMIENTO[0])){
				return 6;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[1])){
				return 8;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[2])){
				return 2;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[3])){
				return 4;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[4])){
				return 7;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[5])){
				return 9;
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
				BajaEnVentas.run();
				//System.exit(0);
			}

		});
	}
	
	

}
