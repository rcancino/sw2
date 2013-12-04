package com.luxsoft.siipap.tesoreria.procesos;

import java.util.Date;

import javax.swing.SwingWorker;

import com.jgoodies.binding.value.ValueHolder;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.tesoreria.ImportadorDeDepositos;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Importa los depositos
 * 
 * @author Ruben Cancino
 *
 */
public class ImportarDepositosTask extends SWXAction{
	
	
	public void execute(){
		final ValueHolder vm=new ValueHolder(new Date());
		SXAbstractDialog dialog=Binder.createDateSelector(vm);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			
			SwingWorker worker=new SwingWorker(){
				@Override
				protected Object doInBackground() throws Exception {
					Date fecha=(Date)vm.getValue();
					ImportadorDeDepositos imp=(ImportadorDeDepositos)ServiceLocator2.instance().getContext().getBean("importadorDeDepositos");
					imp.importar(fecha);
					return null;
				}
				
			};
			TaskUtils.executeSwingWorker(worker);
			
		}
		
	}
	
	

}
