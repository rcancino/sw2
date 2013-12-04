package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * 
 * 
 * @author OCTAVIO
 *
 */
public class ChequeDevueltoContaForm extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/AuxiliarDeChequeDevueltoConta.jasper"), form.getParametros());
		}
		form.dispose();
	}
	

	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		
		private JXDatePicker fechaIni;
		private JXDatePicker fechaFin;
		
		
	

		public ReportForm() {
			super("Cheques Devueltos Conta");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){			
			fechaIni=new JXDatePicker();
			fechaIni.setFormats(new String[]{"dd/MM/yyyy"});
			fechaFin=new JXDatePicker();
			fechaFin.setFormats(new String[]{"dd/MM/yyyy"});
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout(
					"l:40dlu,3dlu,p, 3dlu, " +
					"l:40dlu,3dlu,p:g " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaIni,5);
			builder.nextLine();
			builder.append("Fecha Final",fechaFin,5);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			

			parametros.put("FECHA_INI",fechaIni.getDate() );
			parametros.put("FECHA_FIN",fechaFin.getDate() );
			System.out.println("Parametros: "+parametros);
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		@Override
		protected JComponent buildHeader() {
			return new HeaderPanel("Cheques Devueltos","Auxiliar de Cheques Devueltos");
		}

		
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		ChequeDevueltoContaForm action=new ChequeDevueltoContaForm();
		action.execute();
	}

}
