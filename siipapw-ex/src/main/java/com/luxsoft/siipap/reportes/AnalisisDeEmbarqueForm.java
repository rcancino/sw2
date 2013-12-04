package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
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
public class AnalisisDeEmbarqueForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			
			System.out.println("Parametros enviados: "+form.getParametros());
			ReportUtils.viewReport(ReportUtils.toReportesPath("embarques/AnalisisDeEmbarques.jasper"), form.getParametros());
			
			
				
		}
		form.dispose();
		
	}
	
	public static void run(){
		AnalisisDeEmbarqueForm action=new AnalisisDeEmbarqueForm();
		action.execute();
	}
	
	public static String[] TIPO_DE_ORDENAMIENTO={
		"TONELADAS"
		,"OPERACIONES"
		,"COMISIONES"
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
		private JComboBox sucursalControl;
		private JComboBox ordenBox;
		private JComboBox formaBox;
		private JCheckBox todasLasSucursales;
		
		
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
			super("Analisis De Embarques");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){		
			fechaInicial=new JXDatePicker();
			//Date ini=DateUtils.addMonths(new Date(), -2);
			//fechaInicial.setDate(ini);
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			sucursalControl=createSucursalControl();
			//formatter.setMaximum(new Integer(0));
			ordenBox=new JComboBox(TIPO_DE_ORDENAMIENTO);
			formaBox=new JComboBox(ORDENAMIENTO);
			todasLasSucursales=new JCheckBox("",false);
			todasLasSucursales.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					sucursalControl.setEnabled(!todasLasSucursales.isSelected());
				}
			});
			
			
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
			
			builder.append("Sucursal",sucursalControl,5);			
			builder.append("Todas",todasLasSucursales);
			builder.nextLine();
			builder.append("Orden",ordenBox,5);
			builder.nextLine();
			builder.append("Forma ",formaBox);
			builder.nextLine();
			
						
			
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		
		
		private JComboBox createSucursalControl() {			
			final JComboBox box = new JComboBox(ServiceLocator2.getLookupManager().getSucursalesOperativas().toArray());
			Sucursal local=ServiceLocator2.getConfiguracion().getSucursal();
			for(int index=0;index<box.getModel().getSize();index++){
				Sucursal s=(Sucursal)box.getModel().getElementAt(index);
				if(s.equals(local)){
					box.setSelectedIndex(index);
					break;
				}
			}
			return box;
		}
		
		private Long getSucursalId(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getId();
		}
		
		private String getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getNombre();
		}
		
		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			
			String suc=String.valueOf(getSucursalId());
			if(todasLasSucursales.isSelected())
				suc="%";
			parametros.put("SUCURSAL_ID", suc);
			
			String suc1=getSucursal();
			if(todasLasSucursales.isSelected())
				suc1="%";
			parametros.put("SUCURSAL", suc1);
			
			int order=getOrden();
			parametros.put("ORDEN", String.valueOf(order));
			parametros.put("FORMA",getForma() );
			
			
		}
		
		private int getOrden(){
			String orden=(String)ordenBox.getSelectedItem();
			if(orden.equals(TIPO_DE_ORDENAMIENTO[0])){
				return 17;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[1])){
				return 18;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[2])){
				return 19;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[3])){
				return 1;
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
				AnalisisDeEmbarqueForm.run();
				//System.exit(0);
			}

		});
	}
	
	

}
