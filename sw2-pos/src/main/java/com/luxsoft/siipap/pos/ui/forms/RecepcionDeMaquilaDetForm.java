package com.luxsoft.siipap.pos.ui.forms;

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
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.services.Services;






/**
 * Forma para el mantenimiento de entradas unitarias de maquila {@link EntradaDeMaquila}
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeMaquilaDetForm extends AbstractForm{
	
	private EventList<Producto> productos=new BasicEventList<Producto>();
	

	public RecepcionDeMaquilaDetForm(IFormModel model) {
		super(model);
		setTitle("Entrada de maquila");
		model.addBeanPropertyChangeListener(new Handler());
		
	}
	

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,p:g(.5),2dlu" +
				",p,2dlu,p:g(.5)" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",getControl("producto"),5);
		builder.nextLine();		
		builder.append("cantidad",getControl("cantidad"));
		builder.nextLine();
		
		builder.append("Ancho",getControl("ancho"));
		builder.append("Largo",getControl("largo"));
		
		builder.append("Comentario",getControl("comentario"),5);
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
			JComponent pc=createProductosControl(model.getModel(property));
			return pc;
		}else if("bobinas".equals(property)){
			JComponent c=BasicComponentFactory.createIntegerField(model.getModel(property), 0);
			return c;
		}
		return super.createCustomComponent(property);
	}
	
	
	private JComponent createProductosControl(ValueModel vm){
		
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
					getControl("cantidad").requestFocusInWindow();
				}
			}
        });        
        if(model.getValue("producto")!=null)
        	box.setSelectedItem(model.getValue("producto"));
        final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
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
				Producto p=(Producto)evt.getNewValue();
				if(p!=null && p.isMedidaEspecial()){
					getControl("ancho").setEnabled(true);
					getControl("largo").setEnabled(true);
				}else{
					getControl("ancho").setEnabled(false);
					getControl("largo").setEnabled(false);
				}
			}
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				EventList<Producto> productos=GlazedLists.eventList(Services.getInstance().getProductosManager().buscarInventariablesActivos());
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DefaultFormModel model=new DefaultFormModel(new EntradaDeMaquila());
				RecepcionDeMaquilaDetForm form=new RecepcionDeMaquilaDetForm(model);
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
