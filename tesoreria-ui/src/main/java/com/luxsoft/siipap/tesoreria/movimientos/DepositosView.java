package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;
import com.luxsoft.siipap.tesoreria.TesoreriaActions;

/**
 * Consulta de depostios generados en la tabla SW_DEPOSITO
 * 
 * @author Ruben Cancino
 *
 */
public class DepositosView extends DefaultTaskView{
	
	private InternalTaskTab depositoTab;
	
	protected void instalarTaskElements(){
		final Action mostrarMovimientos=new AbstractAction("Movimientos"){
			public void actionPerformed(ActionEvent e) {
				mostrarDepositos();				
			}
		};
		configAction(mostrarMovimientos,TesoreriaActions.ShowMovimientosView.getId());
		consultas.add(mostrarMovimientos);
	}
	
	protected void mostrarDepositos(){
		if(depositoTab==null){
			final DepositosPanel panel=new DepositosPanel();
			final InternalTaskAdapter adapter=new InternalTaskAdapter(panel);
			adapter.setTitle("Depositos");
			depositoTab=new InternalTaskTab(adapter);			
		}
		addTab(depositoTab);
	}

	@Override
	public void open() {
		mostrarDepositos();
	}
	
	
}
