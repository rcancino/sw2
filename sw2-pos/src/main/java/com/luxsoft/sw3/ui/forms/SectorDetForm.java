package com.luxsoft.sw3.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.services.Services;



/**
 * Forma para el mantenimiento de partidas de conteo
 * 
 * @author Ruben Cancino
 *
 */
public class SectorDetForm extends AbstractForm{
	
	private EventList<Producto> productos;

	public SectorDetForm(IFormModel model) {
		super(model);
		setTitle("Captura Producto Para Sector");
		model.addBeanPropertyChangeListener(new Handler());
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,60dlu,2dlu" +
				",p,2dlu,max(p;200dlu):g" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",getControl("producto"),5);
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}

	private Header header;
	
	
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
	}
	
	private void updateProducto(){
		updateHeader();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("producto".equals(property)){
			JComponent pc=createProductosControl2();
			return pc;
		}else if("cantidad".equals(property)){
			JComponent c=Binder.createNumberBinding(model.getModel(property), 0);
			return c;
		}
		return null;
	}
	
	private JComboBox createProductosControl(){
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList<Producto> source=getProductos();
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        support.setSelectsTextOnFocusGain(true);
        box.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				if(sel instanceof Producto){
					model.setValue("producto", sel);
				}	
			}
        });
        if(model.getValue("producto")!=null)
        	box.setSelectedItem(model.getValue("producto"));
		return box;
	}
	
	private JComponent createProductosControl2(){
		
		
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList<Producto> source=GlazedLists.eventList(getProductos());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        support.setSelectsTextOnFocusGain(true);
        
        
        box.getEditor().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				if(sel instanceof Producto){
					model.setValue("producto", sel);
					getControl("cantidad").requestFocus();
				}else if(sel instanceof String){
					String clave=(String)sel;
					if(!StringUtils.isBlank(clave)){
						Producto p=Services.getInstance().getProductosManager().buscarPorClave(clave);
						if(p!=null)
							model.setValue("producto", p);
					}
					
				}
			}
        });        
        if(model.getValue("producto")!=null)
        	box.setSelectedItem(model.getValue("producto"));        
		return box;
		
	}
	
	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("producto".equals(evt.getPropertyName())){
				updateProducto();
			}else if("cantidad".equals(evt.getPropertyName())){
				
			}
		}
		
	}
	
	public EventList<Producto> getProductos() {
		return productos;
	}

	public void setProductos(EventList<Producto> productos) {
		this.productos = productos;
	}

	

}
