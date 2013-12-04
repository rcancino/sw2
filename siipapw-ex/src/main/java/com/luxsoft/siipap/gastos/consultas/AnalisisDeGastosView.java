package com.luxsoft.siipap.gastos.consultas;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPane;

import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

public class AnalisisDeGastosView extends DefaultTaskView{
	
	private InternalTaskTab tab;
	private InternalTaskTab analisis2Tab;
	
	
	
	@Override
	protected void instalarTaskElements() {
		Action showAnalisis2=new AbstractAction("Analisis Por Gasto"){
			public void actionPerformed(ActionEvent e) {
				showAnalisis2();
			}
		};
		consultas.add(showAnalisis2);
	}

	public void showTab(){
		if(tab==null){
			AnalisisDeGastosPanel panel=new AnalisisDeGastosPanel();
			InternalTaskAdapter adapter=new InternalTaskAdapter(panel);
			adapter.setTitle("General");
			tab=new InternalTaskTab(adapter);
		}
		addTab(tab);
	}
	
	public void showAnalisis2(){
		if(analisis2Tab==null){
			final AnalisisDeGastos2Panel panel=new AnalisisDeGastos2Panel();
			InternalTaskAdapter adapter=new InternalTaskAdapter(panel){

				@Override
				public void instalProcesosActions(JXTaskPane procesos) {
					for(Action a:panel.getReportActions()){
						procesos.add(a);
					}
					operaciones.add(panel.getPeriodoLabel());
				}

				@Override
				public void installDetallesPanel(JXTaskPane detalle) {
					detalle.add(panel.getTotalesPanel());
				}
				
				
			};
			adapter.setTitle("Por Gasto");
			analisis2Tab=new InternalTaskTab(adapter);
		}
		addTab(analisis2Tab);
	}
	
	public void open(){
		showTab();
	}
	
	public static void main(String[] args) {
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			
			private AnalisisDeGastosView view;

			@Override
			protected JComponent buildContent() {
				view=new AnalisisDeGastosView();
				return view.getContent();
			}

			@Override
			protected void onWindowOpened() {
				view.showTab();
			}
			
			
			
			
		};
		dialog.setResizable(true);
		dialog.open();
		System.exit(0);
	}
	
	


}
