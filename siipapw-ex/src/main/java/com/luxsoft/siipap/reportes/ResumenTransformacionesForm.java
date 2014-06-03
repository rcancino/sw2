package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
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

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
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
public class ResumenTransformacionesForm extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
							
				ReportUtils.viewReport(ReportUtils.toReportesPath("INVENT/TransformacionDetalleGlobal.jasper"), form.getParametros());
		
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de Ventas por cliente
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		private JComboBox sucursalControl;
		private Date fechaInicial;
		private Date fechaFinal;
		private JComboBox trTipo;
	
		
		private final PresentationModel model;
		
	
		private JComponent jFechaIni;
		private JComponent jFechaFin;
	
		
	

		public ReportForm() {
			super("Transformaciones Resumen");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){
	
			jFechaIni=Binder.createDateComponent(model.getModel("fechaInicial"));
			jFechaFin=Binder.createDateComponent(model.getModel("fechaFinal"));
			sucursalControl=createSucursalControl();
			String[] items={"TRS","REC","RAU"};
			trTipo=new JComboBox(items);
			
			
			
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
		
		private String getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getId().toString();
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
	
			builder.append("Sucursal",sucursalControl);
			builder.nextLine();
			builder.append("Tipo", trTipo);
			builder.nextLine();
			builder.append("Fecha Inicial", jFechaIni,3);
			builder.nextLine();
			builder.append("Fecha Final ",jFechaFin,3);
			builder.nextLine();
			
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			
		
			//Cliente c=(Cliente)model.getValue("cliente");
			//parametros.put("CLAVE", c.getId());
			parametros.put("FECHA_INI", model.getValue("fechaInicial"));
			parametros.put("FECHA_FIN", model.getValue("fechaFinal"));	
			parametros.put("SUCURSAL", getSucursal());
			parametros.put("TIPO", trTipo.getSelectedItem().toString());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}


		public Date getFechaFinal() {
			return fechaFinal;
		}
		public void setFechaFinal(Date fechaFinal) {
			Object oldValue=this.fechaFinal;
			this.fechaFinal = fechaFinal;
			firePropertyChange("fechaFinal", oldValue, fechaFinal);
		}

		public Date getFechaInicial() {
			return fechaInicial;
		}
		public void setFechaInicial(Date fechaInicial) {
			Object oldValue=this.fechaInicial;
			this.fechaInicial = fechaInicial;
			firePropertyChange("fechaInicial", oldValue, fechaInicial);
		}
		
	}
	
	
	 public static void run(){
		 ResumenTransformacionesForm action =new ResumenTransformacionesForm();
		 action.execute();
		 
	 }
	public static void main(String[] args) {
		ResumenTransformacionesForm action=new ResumenTransformacionesForm();
		action.execute();
	}

}
