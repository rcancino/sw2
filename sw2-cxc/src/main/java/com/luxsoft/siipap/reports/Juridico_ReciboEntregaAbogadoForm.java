package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class Juridico_ReciboEntregaAbogadoForm extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/ReciboEntregaAbogado.jasper"), form.getParametros());
		}
		form.dispose();
	}
	

	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		
		private JXDatePicker fecha;
		private JComboBox box=new JComboBox(new String[]{
				"CENTRAL DE COBRANZA"
				,"FRANCISCO FRIAS ( 2000 PLUS)"
				,"ALEJANDRO LEZAMA BRACHO"
				});
		private JComponent clienteControl;
		ValueModel clienteModel;
		
		
	

		public ReportForm() {
			super("Cheques Devueltos Conta");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){			
			fecha=new JXDatePicker();
			fecha.setFormats(new String[]{"dd/MM/yyyy"});
			clienteModel=new ValueHolder();
			clienteControl=Binder.createClientesBinding(clienteModel);
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			FormLayout layout=new FormLayout(
					"50dlu,2dlu,p, 3dlu,50dlu,2dlu,p:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);			
			builder.append("Cliente",clienteControl,5);
			builder.append("Fecha",fecha);
			builder.append("Abogado",box,true);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return panel;
		}

		@Override
		public void doApply() {			

			parametros.put("ENTREGADO",fecha.getDate() );
			Cliente c=(Cliente)clienteModel.getValue();
			if(c!=null)
				parametros.put("CLIENTE",c.getClave());
			parametros.put("ABOGADO",box.getSelectedItem().toString());
			System.out.println("Parametros: "+parametros);
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		@Override
		protected JComponent buildHeader() {
			return new HeaderPanel("Entrega a jurídico","Documentos entregados al abogado");
		}

		
	}
	
	public static void run() {
		Juridico_ReciboEntregaAbogadoForm action=new Juridico_ReciboEntregaAbogadoForm();
		action.execute();
	}
	
	
}
