package com.luxsoft.siipap.swing.actions;

import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Accion mostrar instancias de tipo com.luxsoft.siipap.swing.View
 * que estan controladas e inicializadas por el contenedor de Spring
 *  
 * 
 * @author Ruben Cancino 
 *
 */
public class ShowViewAction extends SWXAction{
	
	private String viewId;
	private Logger logger=Logger.getLogger(getClass());
	private String role;
	private boolean showWhenNotGranted=true;
	
	public ShowViewAction(){
		
	}
	
	public ShowViewAction(final String viewId){
		this.viewId=viewId;
	}
	
	public ShowViewAction(final String viewId,String role){
		this(viewId);
		this.role=role;
	}

	
	@Override
	protected void execute() {
		if(StringUtils.isNotBlank(role)){
			if(KernellSecurity.instance().hasRole(role)){
				AbstractView view=getView();
				showView(view);
			}else{
				MessageUtils.showMessage("No tiene los permisos adecuados para ejecutar esta acción\n Role requerido: "+role, "Validación de acceso");
			}
		}else{
			AbstractView view=getView();
			showView(view);
		}
		
		
	}
	
	protected void showView(final AbstractView view){
		if(view!=null){
			getApplication().getMainPage().addView(view);
		}
	}
	
	public ApplicationContext getContext() {
		if(super.getContext()==null)
			this.context=Application.instance().getApplicationContext();
		return context;
	}
	
	protected AbstractView getView(){		
		if(getContext().containsBean(getViewId())){
			Object bean=getContextBean(getViewId());
			if(bean instanceof AbstractView)
				return (AbstractView)bean;
			else{
				String msg=MessageFormat.format("El bean {0} ne es de tipo AbstractView\n es de tipo: {1}", getViewId(),bean.getClass().getName());
				JOptionPane.showMessageDialog(getApplication().getMainFrame(),msg);
				logger.error(msg);
				return null;
			}
		}else{
			if(logger.isDebugEnabled()){
				String msg=MessageFormat.format("No existe la vista {0} dada de alta en  el conexto de Spring",getViewId());
				JOptionPane.showMessageDialog(getApplication().getMainFrame(),msg);
				logger.debug(msg);
			}
			
			return null;
		}
	}

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isShowWhenNotGranted() {
		return showWhenNotGranted;
	}

	public void setShowWhenNotGranted(boolean showWhenNotGranted) {
		this.showWhenNotGranted = showWhenNotGranted;
	}

	

	
	
	

}
