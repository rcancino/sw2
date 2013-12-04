package com.luxsoft.siipap.analisis.ui;

import javax.swing.Action;

import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

public class BIConsultasView extends DefaultTaskView {

	InternalTaskTab avnContableTab;
	VNContablePanel avnContablePanel;
	
	public BIConsultasView() {
	
	}
	
	protected void instalarTaskElements(){
		Action mostrarAnalisisContable=new DispatchingAction(this,"mostrarAnalisisVNC");
		mostrarAnalisisContable.putValue(Action.NAME, "Venta AC");
		consultas.add(mostrarAnalisisContable);
	}
	
	public void mostrarAnalisisVNC(){
		if(avnContablePanel==null){
			avnContablePanel =new VNContablePanel();
			InternalTaskAdapter adapter=new InternalTaskAdapter(avnContablePanel);
			avnContableTab=new InternalTaskTab(adapter);
		}
		addTab(avnContableTab);
	}
	
	public void close(){
		if(avnContablePanel!=null)
			avnContablePanel.close();
	}
	
}
