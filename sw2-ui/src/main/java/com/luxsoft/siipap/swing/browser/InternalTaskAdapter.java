package com.luxsoft.siipap.swing.browser;

import java.lang.reflect.Method;

import javax.swing.Action;
import javax.swing.JComponent;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.springframework.beans.BeanUtils;

import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;

public class InternalTaskAdapter extends AbstractInternalTaskView{
	
	protected final FilteredBrowserPanel browser;
	
	public InternalTaskAdapter(final FilteredBrowserPanel panel){
		this.browser=panel;
	}

	public JComponent getControl() {
		return browser.getControl();
	}
	
	@Override
	public void instalOperacionesAction(JXTaskPane operaciones) {
		for(Action a:browser.getActions()){
			if(a!=null)
				operaciones.add(a);
		}
		if(browser.periodo!=null){
			operaciones.add(browser.getPeriodoLabel());
		}
		for(JComponent c:browser.getOperacionesComponents()){
			operaciones.add(c);
		}
	}
	
	
	/**
	 * Instala las acciones catalogadas como procesos en el task de procesos 
	 * 
	 */
	@Override
	public void instalProcesosActions(JXTaskPane procesos) {
		if(browser.getProccessActions()!=null){
			for(Object o:browser.getProccessActions()){
				Action a=(Action)o;
				procesos.add(a);
			}
		}
	}

	@Override
	public void installFiltrosPanel(JXTaskPane filtros) {
		filtros.add(browser.getFilterPanel());
	}
	
	@Override
	public void installDetallesPanel(JXTaskPane detalle) {
		Method method=BeanUtils.findDeclaredMethodWithMinimalParameters(browser.getClass(), "getTotalesPanel");
		if(method!=null){
			JComponent res;
			try {
				res = (JComponent)method.invoke(browser,new Object[0]);
				detalle.add(res);
				detalle.setExpanded(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void open(){
		try {
			browser.open();
		} catch (Exception e) {
			System.out.println("Error abriendo browser :"+getTitle()+ "\n"+ExceptionUtils.getCause(e));
		}
	}
	
	public void close(){
		try {
			browser.close();
		} catch (Exception e) {
			System.out.println("Error cerrando browser :"+getTitle()+ "\n"+ExceptionUtils.getCause(e));
		}
		
	}

}
