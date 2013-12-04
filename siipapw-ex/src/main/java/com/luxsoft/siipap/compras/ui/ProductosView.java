package com.luxsoft.siipap.compras.ui;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPaneContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

public class ProductosView extends DefaultTaskView implements InitializingBean{
	
	private InternalTaskTab productosTab;
	private ProductosPanel productosPanel;
	

	protected void instalarTaskElements(){
		
	}
	
	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		this.taskContainer.remove(procesos); //We don't need this
		this.detalles.setTitle("Resumen");
		this.detalles.setExpanded(true);
	}
	
	
	private void mostrarProductos(){
		if(logger.isDebugEnabled()){
			logger.debug("Mostrando la consulta de compras");
		}
		if(productosTab==null){
			InternalTaskAdapter adapter=new InternalTaskAdapter(productosPanel);
			adapter.setTitle("Productos");
			productosTab=new InternalTaskTab(adapter);
		}
		addTab(productosTab);		
	}
	
	public void open(){
		mostrarProductos();
		productosPanel.open();
	}
	
	public void close(){
		
		super.close();
		
	}
	
	public ProductosPanel getProductosPanel() {
		return productosPanel;
	}

	public void setProductosPanel(ProductosPanel productosPanel) {
		this.productosPanel = productosPanel;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(productosPanel);
	}
	 

	public static void main(String[] args) {
		
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			
			final ProductosView view=new ProductosView();

			@Override
			protected JComponent buildContent() {
				view.setProductosPanel(new ProductosPanel());
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
