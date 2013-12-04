package com.luxsoft.siipap.swing.actions;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Una implementacion de Action para redirigir la accion a otro
 * objeto
 * 
 * @author Ruben Cancino
 *
 */
public class DispatchingAction extends AbstractAction{
	
	private final String methodName;
	private final Object delegate;
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	public DispatchingAction(final String label,final Object delegate,final String methodName ) {		
		this.methodName = methodName;
		this.delegate = delegate;
		setLabel(label);
	}

	public DispatchingAction(final Object delegate,final String methodName ) {		
		this.methodName = methodName;
		this.delegate = delegate;
	}



	public void actionPerformed(ActionEvent e) {
		dispatchAction(methodName);
		
	}
	
	protected void dispatchAction(String name){		
		try {
			Method m=delegate.getClass().getMethod(name);
			m.invoke(delegate);
		} catch (NoSuchMethodException me){			
			String msg="El Delegado no soporta la accion : " + name
			+"\n[Modelo|Plugin]: "+delegate.getClass().getName()
			+"\nEs posible que esta proceso este en producción";
			logger.error(msg,me);
			JOptionPane.showMessageDialog(
					null,
					msg,
					"[Model|Plugin]: "+delegate.toString(),
					JOptionPane.WARNING_MESSAGE);
		}catch(Exception ex){
			String act=getValue(Action.NAME)!=null?getValue(Action.NAME).toString():"";
			String msg="Error ejecutando el metodo: " +name+			
					"\n en el modelo "+delegate.getClass().getName()+
					" con la accion: "+act;
			logger.error(msg,ex);
			ex.printStackTrace();
			MessageUtils.showError(msg, ex);
		}
	}
	
	public void setLabel(String label){
		putValue(Action.NAME, label);
	}

}
