package com.luxsoft.sw3.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.PlasticFieldCaret;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.services.Services;



/**
 * Forma para el mantenimiento de partidas de conteo
 * 
 * @author Ruben Cancino
 *
 */
public class SolicitudDeTrasladoDetForm extends AbstractForm{
	


	public SolicitudDeTrasladoDetForm(IFormModel model) {
		super(model);
		setTitle("Solicitude de Traslado");
	//	model.addBeanPropertyChangeListener(new Handler());
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,60dlu,2dlu" +
				",p,2dlu,max(p;200dlu):g" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
	
		builder.append("Cantidad",getControl("solicitado"),true);
		builder.appendSeparator("Instrucción de corte");
		builder.append("Instruccion",getControl("instruccionesDecorte"),5);
		builder.nextLine();
		//builder.append("Cantidad de Cortes",getControl("cortes"));
		
		return builder.getPanel();
	}

	/*private Header header;
	
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Seleccione un producto","");
		}
		return header.getHeader();
	}
	
	public void updateHeader() {
		if(header!=null){
			Producto p=(Producto)model.getValue("producto");
			if(p!=null){
				header.setTitulo(MessageFormat.format("{0} ({1})",p.getDescripcion(),p.getClave()));
				String pattern="Uni: {0} Largo: {1} Ancho: {2} Calibre: {3}" +
						"\nAcabado: {4} Caras: {5}";
				String desc=MessageFormat.format(pattern, p.getUnidad().getNombre(),p.getLargo(),p.getAncho(),p.getCalibre(),p.getAcabado(),p.getCaras());
				header.setDescripcion(desc);
			}
			else{
				header.setTitulo("Seleccione un producto");
				header.setDescripcion("");
			}
		}
	}*/
	
	/*private void updateProducto(){
		updateHeader();
	}*/
	
	@Override
	protected JComponent createCustomComponent(String property) {
		 if("solicitado".equals(property)){
			JComponent c=Binder.createNumberBinding(model.getModel(property), 0);
			return c;
		}else if("cortes".equals(property)){
			JFormattedTextField tf=BasicComponentFactory.createIntegerField(model.getModel(property), 0);
			tf.setCaret(new PlasticFieldCaret());
			return tf;
		}
		return null;
	}
	

	

	
/*	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("producto".equals(evt.getPropertyName())){
				updateProducto();
			}else if("cantidad".equals(evt.getPropertyName())){
				
			}
		}
		
	}
	*/
	

	

}
