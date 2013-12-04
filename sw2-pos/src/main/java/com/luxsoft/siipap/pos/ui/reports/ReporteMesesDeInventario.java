package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXDatePicker;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.beans.Model;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.model.core.Marca;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para ejecutar el reporte de Inventario Costeado
 * 
 * @author Ruben Cancino
 *
 */
public class ReporteMesesDeInventario extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			POSDBUtils.whereWeAre();
			System.out.println("Parametros enviados: "+form.getParametros());
			ReportUtils2.runReport("compras/Alcance.jasper", form.getParametros());
			
		}
		form.dispose();
		
	}
	
	public static void run(){
		ReporteMesesDeInventario action=new ReporteMesesDeInventario();
		action.execute();
	}
	
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
		private JComboBox productoControl;
		

		public ReportForm() {
			super("Kardex",false);
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){		
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			sucursalControl=createSucursalControl();
			productoControl=buildProductoControl();
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Producto",productoControl,5);
			builder.append("Sucursal",sucursalControl,5);
						
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal,true);
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private JComboBox buildProductoControl() {
			getOKAction().setEnabled(false);
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(Services.getInstance()
					.getProductosManager().getAll());
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave" ,"descripcion"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setCorrectsCase(true);
			box.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {					
					if ((e.getItem()!=null) && (e.getItem() instanceof Producto))
							getOKAction().setEnabled(true);
					else
						getOKAction().setEnabled(false);
				}
				
			});
			return box;
		}
		
		private JComboBox createSucursalControl() {			
			
			final JComboBox box = new JComboBox();
			box.addItem(Services.getInstance().getConfiguracion().getSucursal());
			return box;
		}
		
		private Long getSucursal(){
			Object selected=sucursalControl.getSelectedItem();
			return Long.valueOf(((Sucursal)selected).getClave());
		}
		
		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("SUCURSAL", getSucursal());
			parametros.put("CLAVE", getProductoClave());
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			
		}
		
		private String getProductoClave(){
			Object selected=productoControl.getSelectedItem();
			if(selected!=null)
				return ((Producto)selected).getClave();
			return "";
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		
	}
	
	public static class AlcanceModel extends Model{
		
		private Date fechaInicial;
		private Date fechaFinal;
		private Linea linea;
		private int meses;
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				ReporteMesesDeInventario.run();
				
			}
			
		});
		
	}

}
