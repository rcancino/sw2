package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.ParamLabelValue;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

import com.luxsoft.sw3.services.Services;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class Discrepancias extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils2.runReport("invent/DiscrepanciasDeInv.jasper", form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de cobranza
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
     
		private JComponent yearComponent;
		private JComponent mesComponent;
		
		

		public ReportForm() {
			super("Inventarios");
		}
		
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
		
		
		private void initComponents(){
			final Date fecha=new Date();
			yearHolder=new ValueHolder(Periodo.obtenerYear(fecha));
			mesHolder=new ValueHolder(Periodo.obtenerMes(fecha));
			
			yearComponent=Binder.createYearBinding(yearHolder);
			mesComponent=Binder.createMesBinding(mesHolder);
			
			
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Año",yearComponent);
			builder.append("Mes",mesComponent);
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Discrepancia de informacion de inventarios","").getHeader();
		}

		@Override
		protected JComponent buildContent() {			
			JPanel panel=new JPanel(new BorderLayout());			
			panel.add(buildForm(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			Integer mes=(Integer)mesHolder.getValue();
			Integer year=(Integer)yearHolder.getValue();
			parametros.put("AÑO", year);
			parametros.put("MES", mes);
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			parametros.put("SUCURSAL", suc.getId());
			
			
			
			logger.info("Parametros de reporte:"+parametros);
			
			
		}

		public Map<String, Object> getParametros() {
			
			return parametros;
		}

		private ValueHolder yearHolder;
		private ValueHolder mesHolder;
	
		
		 
		
	}
	
	public static void run(){
		Discrepancias action=new Discrepancias();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
