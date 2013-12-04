package com.luxsoft.sw3.maquila.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;





/**
 * Forma para el mantenimiento de entradas unitarias de material {@link EntradaDeMaterialDet}
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeMaterialDetForm extends AbstractForm{
	
	private EventList<Producto> productos=new BasicEventList<Producto>();
	

	public RecepcionDeMaterialDetForm(IFormModel model) {
		super(model);
		setTitle("Entrada de material");
		model.addBeanPropertyChangeListener(new Handler());
		model.getModel("producto").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getNewValue()!=null){
					getControl("kilos").requestFocusInWindow();
					
				}
			}
		});
	}
	

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,90dlu,2dlu" +
				",p,2dlu,90dlu" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",getControl("producto"),5);
		builder.nextLine();		
		builder.append("Kilos",getControl("kilos"));
		builder.append("Precio ",addReadOnly("precioPorKilo"));
		builder.nextLine();
		builder.append("M2",getControl("metros2"));
		builder.append("Precio",addReadOnly("precioPorM2"));
		builder.nextLine();
		builder.append("Importe",getControl("importe"));
		builder.append("Bobinas",getControl("bobinas"));
		builder.nextLine();
		builder.append("Comentario",getControl("observaciones"),5);
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		ComponentUtils.decorateTabFocusTraversal(getControl("producto"));
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
			JComponent pc=createProductosControl();
			return pc;
		}else if("bobinas".equals(property)){
			JComponent c=BasicComponentFactory.createIntegerField(model.getModel(property), 0);
			return c;
		}
		return super.createCustomComponent(property);
	}
	
	
	private JComponent createProductosControl(){
		
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList<Producto> source=GlazedLists.eventList(getProductos());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        support.setSelectsTextOnFocusGain(true);
        //support.setStrict(true);
        box.getEditor().addActionListener(new ActionListener(){
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
	
	public EventList<Producto> getProductos() {
		return productos;
	}

	public void setProductos(EventList<Producto> productos) {
		this.productos = productos;
	}		

	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("producto".equals(evt.getPropertyName())){
				updateProducto();
			}
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				EventList<Producto> productos=GlazedLists.eventList(ServiceLocator2.getProductoManager().buscarProductosActivos());
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DefaultFormModel model=new DefaultFormModel(new EntradaDeMaterialDet());
				RecepcionDeMaterialDetForm form=new RecepcionDeMaterialDetForm(model);
				form.setProductos(productos);
				form.open();
				if(!form.hasBeenCanceled()){
					showObject(model.getBaseBean());
				}
				System.exit(0);
			}

		});
	}

}
