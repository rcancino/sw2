package com.luxsoft.sw3.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.MovimientoDet.TipoCIS;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;



/**
 * Forma para el mantenimiento particular de unidades de compra
 * 
 * @author Ruben Cancino
 *
 */
public class MovimientoDeInventarioDetForm extends AbstractForm{
	
	private EventList<Producto> productos;
	boolean cis=false;

	public MovimientoDeInventarioDetForm(IFormModel model) {
		super(model);
		setTitle("Unidad de movimiento  ["+model.getValue("concepto")+"]");
		model.addBeanPropertyChangeListener(new Handler());
	}
	

	public boolean isCis() {
		return cis;
	}


	public void setCis(boolean cis) {
		this.cis = cis;
	}


	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,60dlu,2dlu" +
				",p,2dlu,max(p;200dlu):g" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",getControl("producto"),5);
		builder.append("Cantidad",getControl("cantidad"),true);
		if(isCis()){
			builder.append("CIS",getControl("tipoCis"),true);
		}
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
		//Producto p=(Producto)model.getValue("producto");
		
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("producto".equals(property)){
			JComponent pc=createProductosControl();
			return pc;
		}else if("cantidad".equals(property)){
			JComponent c=Binder.createNumberBinding(model.getModel(property), 0);
			return c;
		}else if("tipoCis".equals(property)){
			SelectionInList sl=new SelectionInList(TipoCIS.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
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

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				EventList<Producto> productos=new ca.odell.glazedlists.BasicEventList<Producto>();
				com.luxsoft.sw3.ui.selectores.LookupUtils.getDefault().loadProductosInventariables(productos);
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				MovimientoDet target=new MovimientoDet();
				target.setConcepto("MERMA");
				DefaultFormModel model=new DefaultFormModel(target);
				MovimientoDeInventarioDetForm form=new MovimientoDeInventarioDetForm(model);
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
