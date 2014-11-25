package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Forma para ejecutar el reporte de analisis de diferencia en conteo fisico
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ProductosSinSectorForm extends SWXAction{
	
	
	public static void run(){
		new ProductosSinSectorForm().execute();
	}
	
	@Override
	protected void execute() {
		ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			Map params=form.getParameters();
			ReportUtils2.runReport("invent/ProductosSinSector.jasper",params);
		}
	}
	
	private class ReportForm extends SXAbstractDialog{
		
	
		private JComboBox sucursalBox;
		private JComboBox lineaBox;		
		private JComboBox claseBox;

		public ReportForm() {
			super("Productos Sin Sector");
			
		}
		
		private void init(){
			sucursalBox=ReportControls.createSucursalesBox();
			lineaBox=ReportControls.createLineasBox();		
			claseBox=ReportControls.createClaseBox();
		}

		@Override
		protected JComponent buildContent() {
			init();
			FormLayout layout=new FormLayout("p,2dlu,max(150;p), 3dlu, p,2dlu,max(150;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			 
			
			builder.append("Sucursal",sucursalBox);
						
			builder.append("Línea",lineaBox);
			builder.append("Clase",claseBox,true);
				
			JPanel conten=new JPanel(new BorderLayout());
			conten.add(builder.getPanel(),BorderLayout.CENTER);
			conten.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return conten;
		}		
		
		
		
		private Sucursal getSucursal(){
			return (Sucursal)sucursalBox.getSelectedItem();
		}
		
		public Map getParameters(){
			Map map=new HashMap();
			
			
			
			map.put("SUCURSAL", getSucursal().getId());
			Linea linea=(Linea)lineaBox.getSelectedItem();
			Clase clase=(Clase)claseBox.getSelectedItem();
			map.put("LINEA", linea!=null?linea.getId().toString():"%");
			map.put("CLASE", clase!=null?clase.getId().toString():"%");
			return map;
		}
		
		private ParamLabelValue getParam(JComboBox box){
			return (ParamLabelValue)box.getSelectedItem();
		}
		
	}
	
	public static class ParamLabelValue{
		
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
				ProductosSinSectorForm form=new ProductosSinSectorForm();
				form.execute();
				//System.exit(0);
			}

		});
	}

}
