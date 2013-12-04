package com.luxsoft.sw3.bi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.BeanWrapperImpl;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


/**
 * Forma para autorizar clientes a recibir o no cheque
 * 
 * @author Ruben Cancino
 *
 */
public class AutorizacionChequeForm extends AbstractForm{
	
	 String titulo;
	 
	
	public AutorizacionChequeForm(AutorizacionClienteFormModel model,String titulo) {
		super(model);
		
		
		model.getModel("cliente").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Cliente c=(Cliente)evt.getNewValue();
				getControl("comentario").setEnabled(c!=null);
				BeanWrapperImpl w1=new BeanWrapperImpl(c);
				String property=getBaseModel().getProperty();
				getModel().setValue(property, w1.getPropertyValue(property));
				//System.out.println("Cliente seleccionado: "+c);
				
				//getModel().setValue(getBaseModel().getProperty(), );
			}
		});
		//PropertyConnector.connect(getBaseModel().getCliente(), getBaseModel().getProperty(), model.getBaseBean(), getBaseModel().getProperty());
		setTitle(titulo);
	}
	
	public AutorizacionClienteFormModel getBaseModel(){
		return (AutorizacionClienteFormModel)getModel();
	}
	
		@Override
	protected JComponent buildHeader() {
		
		String titulo="Mantenimiento clientes "+getTitle();
		String descripcion="Seleccione el cliente a modificar";
		Header header=new Header(titulo, descripcion);
		return header.getHeader();
	}
	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,350dlu:g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cliente",getControl("cliente"));
		String property=getBaseModel().getProperty();
		builder.append(WordUtils.capitalize(property),getControl(property));
		builder.append("Comentario",getControl("comentario"));
		getControl("comentario").setEnabled(false);
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if("cliente".equals(property)){
			JComponent c=Binder.createClientesBinding(getModel().getModel(property));
			 c.setEnabled(!getModel().isReadOnly());
			 return c;
		}else if("comentario".equals(property)){
			JComponent c=Binder.createMayusculasTextField(getModel().getModel(property));
			c.setEnabled(false);
			return c;
		
		}
		return null;
	}
	
	
	public static Cliente modificarPermitirCheque(){
		final AutorizacionClienteFormModel model=new AutorizacionClienteFormModel("permitirCheque");	
		final AutorizacionChequeForm form=new AutorizacionChequeForm(model,"Autorización/Cancelacion");
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public static Cliente modificarSuspendido(){
		final AutorizacionClienteFormModel model=new AutorizacionClienteFormModel("suspendido");	
		final AutorizacionChequeForm form=new AutorizacionChequeForm(model,"Suspender totalmente ");
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				modificarPermitirCheque();
			}
		});
	}

}
