package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Unidad;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;

import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para ejecutar el reporte de Inventario Costeado
 * 
 * @author Ruben Cancino
 *
 */
public class KardexCertReportForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		//form.setModal(false);
		form.open();
		/*if(!form.hasBeenCanceled()){
			POSDBUtils.whereWeAre();
			System.out.println("Parametros enviados: "+form.getParametros());
			ReportUtils2.runReport("invent/KardexSuc.jasper", form.getParametros());
			
		}*/
		//form.dispose();
		
	}
	
	public static void run(){
		KardexCertReportForm action=new KardexCertReportForm();
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
		
		//private JComboBox productoControl;
		
		private JComboBox clasificacion;
		

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
			//productoControl=buildProductoControl();
			
			//List data=ServiceLocator2.getJdbcTemplate().queryForList("SELECT CLASIFICACION FROM SX_PRODUCTOS_CLASIFICACION", String.class);
			List data=Services.getInstance().getJdbcTemplate().queryForList("SELECT CLASIFICACION FROM SX_PRODUCTOS_CLASIFICACION", String.class);
			clasificacion=new JComboBox(data.toArray());
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		//	builder.append("Producto",productoControl,5);
			builder.append("Sucursal",sucursalControl,5);
						
			builder.append("Fecha Inicial",fechaInicial,5);
			builder.append("Fecha Final",fechaFinal,5);
			
			builder.append("Clasificacion",clasificacion);
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		/*private JComboBox buildProductoControl() {
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
		}*/
		
		private JComboBox createSucursalControl() {			
			
			
			List<Sucursal> sucursales=Services.getInstance().getSucursalesOperativas();
			Sucursal local=Services.getInstance().getConfiguracion().getSucursal();
			sucursales.remove(local);
			sucursales.add(0,local);
			final JComboBox box = new JComboBox(sucursales.toArray(new Sucursal[0]));
			return box;
		}
		
		private Long getSucursal(){
			Object selected=sucursalControl.getSelectedItem();
			return Long.valueOf(((Sucursal)selected).getId());
		}
		
		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("SUCURSAL_ID", getSucursal());
			//parametros.put("CLAVE", getProductoClave());
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			String clasif=(String)clasificacion.getSelectedItem();
			if(clasif!=null)
				parametros.put("CLASIFICACION", clasif);
			
		}
		
		/*private String getProductoClave(){
			Object selected=productoControl.getSelectedItem();
			if(selected!=null)
				return ((Producto)selected).getClave();
			return "";
		}
		
		private String getProductoUnidad(){
			Object selected=productoControl.getSelectedItem();
			if(selected!=null){
				Producto selectedP=(Producto)selected;
				return selectedP.getUnidad().getUnidad();
			}
			return"";
		}*/

		public Map<String, Object> getParametros() {
			return parametros;
		}

		@Override
		public void doAccept() {
			parametros.put("SUCURSAL_ID", getSucursal());
			//parametros.put("CLAVE", getProductoClave());
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			String clasif=(String)clasificacion.getSelectedItem();
			if(clasif!=null)
				parametros.put("CLASIFICACION", clasif);
			POSDBUtils.whereWeAre();
			System.out.println("Parametros enviados: "+getParametros());
				
			/*if(getProductoUnidad().equals("TM.")){
						ReportUtils2.runReport("invent/KardexSucTM.jasper", getParametros());
			
			      }
			else{*/
			    ReportUtils2.runReport("invent/KardexCertificacion.jasper", getParametros());
			  //   }
			
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
				KardexCertReportForm.run();
				
			}

		});
	}

}
