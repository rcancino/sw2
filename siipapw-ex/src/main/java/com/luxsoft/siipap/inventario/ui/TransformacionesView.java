package com.luxsoft.siipap.inventario.ui;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPaneContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.luxsoft.siipap.compras.ui.ProductosPanel;
import com.luxsoft.siipap.compras.ui.ProductosView;
import com.luxsoft.siipap.inventario.TransformacionForm;
import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

public class TransformacionesView extends DefaultTaskView implements InitializingBean {
	private InternalTaskTab transformacionTaskTab;
	private TransformacionPanel transformacionPanel;
	
	
	
	
	
	@Override
	protected void instalarTaskPanels(JXTaskPaneContainer container) {
		this.taskContainer.remove(procesos);
		this.detalles.setTitle("Resumen");
		this.detalles.setEnabled(true);
	}

	private void mostrarTransformaciones(){
		if(logger.isDebugEnabled()){
		logger.debug("Mostrando consutal de Tranformaciones");	
		}
		if(transformacionTaskTab==null){
			InternalTaskAdapter adpter=new InternalTaskAdapter(transformacionPanel);
			adpter.setTitle("Tranformaciones");
			transformacionTaskTab=new InternalTaskTab(adpter);
		}
		addTab(transformacionTaskTab);
	}
	
	public void open(){
		mostrarTransformaciones();
		transformacionPanel.open();
	}
	
	public void close(){
		
		super.close();
		
	}

	public TransformacionPanel getTransformacionPanel() {
		return transformacionPanel;
	}

	public void setTransformacionPanel(TransformacionPanel transformacionPanel) {
		this.transformacionPanel = transformacionPanel;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(transformacionPanel);
		
	}
	
	public static void main(String[] args) {
		
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			
			final TransformacionesView view=new TransformacionesView();

			@Override
			protected JComponent buildContent() {
				view.setTransformacionPanel(new TransformacionPanel());
				return view.getContent();
			}

			@Override
			protected void onWindowOpened() {
				view.open();
			}
			
		};
		dialog.open();
		System.exit(0);
	}
	

}
