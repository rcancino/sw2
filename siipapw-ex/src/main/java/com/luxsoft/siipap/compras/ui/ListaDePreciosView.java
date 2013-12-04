package com.luxsoft.siipap.compras.ui;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;


public class ListaDePreciosView extends DefaultTaskView {
	
	private InternalTaskTab listasTab;
	private ListaDePreciosPanel listaDePreciosPanel;
	
	
	protected void instalarTaskElements() {		
		
		
	}

	protected void instalarTaskPanels(final JXTaskPaneContainer container) {		
		this.taskContainer.remove(detalles); // We don't need this
		this.taskContainer.remove(consultas);
	}
	
	public void close(){
		super.close();
		getListaDePreciosPanel().close();
	}
	
	
	@Override
	public void open() {
		mostrarListasVigentes();
	}

	public void mostrarListasVigentes(){
		if(listasTab==null){
			InternalTaskAdapter adapter=new InternalTaskAdapter(getListaDePreciosPanel()){
				@Override
				public void instalProcesosActions(JXTaskPane procesos) {
					procesos.add(getListaDePreciosPanel().getCopiarAction());
					//procesos.add(getListaDePreciosPanel().getInHabilitarAction());
					//procesos.add(getListaDePreciosPanel().getHabilitarAction());
				}				
			};
			adapter.setTitle("Listas de Precios vigentes");
			listasTab=new InternalTaskTab(adapter);
		}
		addTab(listasTab);
	}

	public ListaDePreciosPanel getListaDePreciosPanel() {
		return listaDePreciosPanel;
	}

	public void setListaDePreciosPanel(ListaDePreciosPanel listaDePreciosPanel) {
		this.listaDePreciosPanel = listaDePreciosPanel;
	}

}
