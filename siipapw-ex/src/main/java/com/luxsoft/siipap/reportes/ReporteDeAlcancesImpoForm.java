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
import com.luxsoft.siipap.model.core.Clase;
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
public class ReporteDeAlcancesImpoForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			
				System.out.println("Parametros enviados: "+form.getParametros());
				ReportUtils.viewReport(ReportUtils.toReportesPath("compras/AlcanceImportaciones.jasper"), form.getParametros());
			
				
		}
		form.dispose();
		
	}
	
	public static void run(){
		ReporteDeAlcancesImpoForm action=new ReporteDeAlcancesImpoForm();
		action.execute();
	}
	
	public static String[] TIPO_DE_ORDENAMIENTO={
		"ALCANCE TOTAL"
		,"GRAMAJE"
		,"PROMEDIO DE VENTA"
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
		private JComboBox lineaControl;
		private JFormattedTextField meses;
		private JComboBox ordenBox;
		private JComboBox formaBox;
		private JComboBox filtrosBox;
		private JComboBox filtro2Box;
		private JFormattedTextField mesesF;
		//private JCheckBox alcance;
		private JCheckBox todasLasSucursales;
		private JComboBox clase1;
		private JComboBox clase2;
		private JComboBox clase3;
		private JComboBox clase4;
		
		
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
			Date ini=DateUtils.addMonths(new Date(), -2);
			fechaInicial.setDate(ini);
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			sucursalControl=createSucursalControl();
			lineaControl=buildLineaControl();
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter.setValueClass(Double.class);
			//formatter.setMaximum(new Integer(0));
			meses=new JFormattedTextField(formatter);
			meses.setValue(new Double(0));
			ordenBox=new JComboBox(TIPO_DE_ORDENAMIENTO);
			formaBox=new JComboBox(ORDENAMIENTO);
			filtrosBox=new JComboBox(new Object[]{
						new ParamLabelValue("TODOS"," LIKE '%'"),
						new ParamLabelValue("ALCANCE MAYOR",">$P{MESESF}"),
						new ParamLabelValue("ALCANCE MENOR","<=$P{MESESF}")
						});
			mesesF=new JFormattedTextField(formatter);
			mesesF.setValue(new Double(0));
			//alcance=new JCheckBox("",false);
			todasLasSucursales=new JCheckBox("",false);
			todasLasSucursales.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					sucursalControl.setEnabled(!todasLasSucursales.isSelected());
				}
				
			});
			
			clase1=buildClaseControl();
			NumberFormatter formatter1=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter1.setValueClass(Double.class);
			clase2=buildClaseControl();
			NumberFormatter formatter2=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter2.setValueClass(Double.class);
			clase3=buildClaseControl();
			NumberFormatter formatter3=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter3.setValueClass(Double.class);
			clase4=buildClaseControl();
			NumberFormatter formatter4=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter4.setValueClass(Double.class);
			filtro2Box=new JComboBox(new Object[]{
					new ParamLabelValue("TODOS"," "),
					new ParamLabelValue("DE LINEA","AND DELINEA IS TRUE"),
					new ParamLabelValue("ESPECIALES","AND DELINEA IS FALSE")					
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
			builder.append("Linea",lineaControl,5);
			builder.nextLine();
			builder.append("clase 1",clase1);
			builder.append("clase 2",clase2);
			builder.nextLine();
			builder.append("clase 3",clase3);
			builder.append("clase 4",clase4);
			builder.nextLine();
			builder.append("Orden",ordenBox,5);
			builder.nextLine();
			builder.append("Forma ",formaBox);
			builder.append("Meses de alcance",meses);
			builder.nextLine();
			builder.append("Filtro ",filtrosBox);
			builder.append("Meses a filtrar",mesesF);			
			builder.nextLine();
			builder.append("Tipo",filtro2Box);
			//builder.append("Meses de invent.",alcance);
						
			
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
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
		
		private JComboBox buildClaseControl(){
			final JComboBox box=new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2.getHibernateTemplate().find("from Clase   c order by c.nombre"));
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "nombre"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.STARTS_WITH);
			support.setCorrectsCase(true);
			return box;
			
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
		
		
		private Long getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getId();
		}
		
		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			String suc=String.valueOf(getSucursal());
			if(todasLasSucursales.isSelected())
				suc="%";
			parametros.put("SUCURSAL", suc);
			parametros.put("LINEA", getLinea());
			int order=getOrden();
			parametros.put("ORDEN", String.valueOf(order));
			parametros.put("FORMA",getForma() );
			parametros.put("MESES", getMeses());
			parametros.put("FILTRO", getParam(filtrosBox).value);
			parametros.put("FILTRADO", getParam(filtrosBox).name);
			parametros.put("MESESF", (Double)mesesF.getValue());
			parametros.put("DELINEA", getParam(filtro2Box).value);
			String clase = null;
				if(getClase4()!=null)
	 				 clase= new String(" AND C.NOMBRE IN(\'").concat(getClase1()).concat("\',\'").concat(getClase2()).concat("\',\'").concat(getClase3()).concat("\',\'").concat(getClase4()).concat("\')");
				else
					if(getClase3()!=null)
	 				 clase= new String(" AND C.NOMBRE IN(\'").concat(getClase1()).concat("\',\'").concat(getClase2()).concat("\',\'").concat(getClase3()).concat("\')");
					else
						if(getClase2()!=null)
		 				 clase= new String(" AND C.NOMBRE IN(\'").concat(getClase1()).concat("\',\'").concat(getClase2()).concat("\')");
						else
							if(getClase1()!=null)
			 				 clase= new String(" AND C.NOMBRE IN(\'").concat(getClase1()).concat("\')");
					           else new String("");					
			parametros.put("CLASE",clase);
			
			/*parametros.put("CLASE1",getClase1());
			parametros.put("CLASE2",getClase2());
			parametros.put("CLASE3",getClase3());
			parametros.put("CLASE4",getClase4());*/
			
			
		}
		
		private int getOrden(){
			String orden=(String)ordenBox.getSelectedItem();
			if(orden.equals(TIPO_DE_ORDENAMIENTO[0])){
				return 11;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[1])){
				return 16;
			}
			if(orden.equals(TIPO_DE_ORDENAMIENTO[2])){
				return 10;
			}
			return 0;
		}
		
		private String getForma(){
			String s=(String)formaBox.getSelectedItem();
			return s.startsWith("ASC")?"ASC":"DESC";
		}
		
		private double getMeses(){
			return (Double)meses.getValue();
		}
		
		private String getLinea(){
			Linea selected=(Linea)lineaControl.getSelectedItem();
			if(selected!=null)
				return String.valueOf(selected.getNombre());
			return "%";
		}

		private String getClase1(){
			Clase selected=(Clase)clase1.getSelectedItem();
			if(selected!=null)
				return String.valueOf(selected.getNombre());
			return " ";
		}
		private String getClase2(){
			Clase selected=(Clase)clase2.getSelectedItem();
			if(selected!=null)
				return String.valueOf(selected.getNombre());
			return " ";
		}
		private String getClase3(){
			Clase selected=(Clase)clase3.getSelectedItem();
			if(selected!=null)
				return String.valueOf(selected.getNombre());
			return " ";	
		}
		private String getClase4(){
			Clase selected=(Clase)clase4.getSelectedItem();
			if(selected!=null)
				return String.valueOf(selected.getNombre());
			return " ";
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
				ReporteDeAlcancesImpoForm.run();
				//System.exit(0);
			}

		});
	}
	
	

}
