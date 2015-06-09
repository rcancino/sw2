package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXDatePicker;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.Familia;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.ParamLabelValue;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para ejecutar el reporte de validación de conteo de inventario
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RecorridosPorLineaForm extends SWXAction{
	
	
	public static void run(){
		new RecorridosPorLineaForm().execute();
	}
	
	@Override
	protected void execute() {
		ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			Map params=form.getParameters();
			System.out.println(params);
			ReportUtils2.runReport("invent/RecorridosPorLinea.jasper",params);
		}
	}
	
	
	
	
	
	private class ReportForm extends SXAbstractDialog{
		
		
		
	

		private JXDatePicker datePicker;
		private JComboBox sucursalBox;
	
		private JComboBox estadoBox;
		private JComboBox tipoBox;
		private JComboBox existenciaBox;
		
		
		
		private JComboBox lineaBox;		
	
		
		
		private JComboBox claseBox1;
		private Linea lineaSel;
		 String linea;
		

		public ReportForm() {
			super("Validación de captura");
			
		}
		
		
		
		
		private void init(){
			
			sucursalBox=ReportControls.createSucursalesBox();
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getIntegerInstance());
			formatter.setValueClass(Integer.class);
			datePicker=new JXDatePicker();
			datePicker.setFormats("dd/MM/yyyy");
			
			
			estadoBox=new JComboBox(new Object[]{
					new ParamLabelValue("TODOS"," "),
					new ParamLabelValue("ACTIVOS",	"AND ACTIVO IS TRUE"),
					new ParamLabelValue("SUSPENDIDOS","AND ACTIVO IS FALSE")
					});
			tipoBox=new JComboBox(new Object[]{
					new ParamLabelValue("TODOS"," "),
					new ParamLabelValue("DE LINEA","AND DELINEA IS TRUE"),
					new ParamLabelValue("ESPECIALES","AND DELINEA IS FALSE")
					});
			
			existenciaBox=new JComboBox(new Object[]{
					new ParamLabelValue("TODOS"," "),
					new ParamLabelValue("POSITIVOS"," AND E.CANTIDAD >0"),
					new ParamLabelValue("NEGATIVOS"," AND E.CANTIDAD<0"),
					new ParamLabelValue("SIN EXISTENCIA"," AND E.CANTIDAD=0")
					});
		
						
			
			sucursalBox=ReportControls.createSucursalesBox(5L);
			lineaBox=ReportControls.createLineasBox();
			
			claseBox1=new JComboBox();
				llenaClaseBox();
				
			NumberFormat format=NumberFormat.getNumberInstance();
			format.setGroupingUsed(false);
			NumberFormatter formatter2=new NumberFormatter(format);
			formatter2.setValueClass(Double.class);
			
			
			
		}

		@Override
		protected JComponent buildContent() {
			init();
			FormLayout layout=new FormLayout("p,2dlu,p, 3dlu, p,2dlu,p","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			
			builder.appendSeparator();
		
			builder.append("Estado",estadoBox);
			builder.append("Tipo",tipoBox);
			builder.append("Existencia",existenciaBox);
			builder.nextLine();		
			builder.appendSeparator("Linea");
			
			builder.append("Línea",lineaBox);
            			
			lineaBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
				llenaClaseBox();
				}
			});
			builder.nextLine();
			builder.append("Clase",claseBox1,true);
			
			JPanel conten=new JPanel(new BorderLayout());
			conten.add(builder.getPanel(),BorderLayout.CENTER);
			conten.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return conten;
		}		
		
		
		
		private Sucursal getSucursal(){
			return (Sucursal)sucursalBox.getSelectedItem();
		}
		
		
		private  void  llenaClaseBox(){
			
			lineaSel=(Linea)lineaBox.getSelectedItem();
			linea=lineaSel!=null? lineaSel.getNombre() : " ";
			String hql="select distinct(p.clase) from Producto p   where p.linea.nombre=?";
			Object[] params={linea};
			List<Clase> data=Services.getInstance().getHibernateTemplate().find(hql, params);
			claseBox1.removeAllItems();
			
			if (lineaSel==null)
			claseBox1.addItem(" ");
			else
				claseBox1.addItem("TODOS");
				
			for(Clase clase: data){
				claseBox1.addItem(clase.getNombre());
			}
					
		}
		
	
		
		
		
		
		public Map getParameters(){
			Map map=new HashMap();
			
			
			
			map.put("SUCURSAL", Services.getInstance().getConfiguracion().getSucursal().getId());
			map.put("ACTIVO", getParam(estadoBox).stringValue());
			map.put("DELINEA", getParam(tipoBox).stringValue());
			map.put("EXISTENCIA", getParam(existenciaBox).stringValue());
			
			Linea linea=(Linea)lineaBox.getSelectedItem();
			String clase=(String) claseBox1.getSelectedItem();
			
			map.put("LINEA", linea!=null?linea.getNombre():"%");
			map.put("CLASE", clase!="TODOS" ? clase:"%");
		
			return map;
		}
		
		private ParamLabelValue getParam(JComboBox box){
			return (ParamLabelValue)box.getSelectedItem();
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
				RecorridosPorLineaForm form=new RecorridosPorLineaForm();
				form.execute();
				//System.exit(0);
			}

		});
	}
	
	
	

}


