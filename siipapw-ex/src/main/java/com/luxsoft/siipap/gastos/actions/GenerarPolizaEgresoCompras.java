package com.luxsoft.siipap.gastos.actions;

import java.util.Date;

import javax.swing.SwingWorker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.service.cxp.Poliza_EgresosCxP;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class GenerarPolizaEgresoCompras extends SWXAction{

	@Override
	protected void execute() {
		ValueModel model=new ValueHolder(new Date());
		SXAbstractDialog dialog=Binder.createDateSelector(model);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			generarPoliza((Date)model.getValue());
		}
		
	}
	protected void generarPoliza(final Date fecha){
		SwingWorker<String, String> worker=new SwingWorker<String, String>(){
			@Override
			protected String doInBackground() throws Exception {
				Poliza_EgresosCxP poliza=new Poliza_EgresosCxP();
				poliza.procesar(fecha);
				return "OK";
			}
			@Override
			protected void done() {
				MessageUtils.showMessage("Proceso terminado", "Poliza Disco");
			}
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	public static void main(String[] args) {
		GenerarPolizaEgresoCompras p=new GenerarPolizaEgresoCompras();
		p.execute();
	}

}
