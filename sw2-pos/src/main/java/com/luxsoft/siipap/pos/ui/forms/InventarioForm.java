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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
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
public class InventarioForm extends AbstractForm{
	
	private boolean porExistencia=false;
	
	private EventList<Producto> productos=new BasicEventList<Producto>();
	
	private boolean supervisor=false;
	//private List sucursales;

	public InventarioForm(IFormModel model) {
		super(model);
		setTitle("Movimiento de Inventario");
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
		builder.append("Cantidad",getControl("cantidad"));
		if(isSupervisor()){
			builder.append("Costo",getControl("costop"),5);
		}
		builder.nextLine();
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
			JComponent pc=createProductosControl();
			return pc;
		}else if("cantidad".equals(property)){
			JComponent c=Binder.createNumberBinding(model.getModel(property), 0);
			return c;
		}else if("existencia".equals(property)){
			JComponent pc=createProductosControl();
			return pc;
		}/*else if("sucursal".equals(property)){			
			final SelectionInList sl=new SelectionInList(getSucursales());
			final JComponent c=BasicComponentFactory.createComboBox(sl);			
			c.setEnabled(!model.isReadOnly());
			return c;
		}*/
		return super.createCustomComponent(property);
	}
	
	private JComboBox createProductosControl(){
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList source=getProductos();
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
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
	
	
	
	
	public EventList<Producto> getProductos() {
		return productos;
	}

	public void setProductos(EventList<Producto> productos) {
		this.productos = productos;
	}	
	
	
	
	/*public List getSucursales() {
		return sucursales;
	}

	public void setSucursales(List sucursales) {
		this.sucursales = sucursales;
	}*/

	public boolean isPorExistencia() {
		return porExistencia;
	}

	public void setPorExistencia(boolean porExistencia) {
		this.porExistencia = porExistencia;
	}

	public boolean isSupervisor() {
		return supervisor;
	}

	public void setSupervisor(boolean supervisor) {
		this.supervisor = supervisor;
	}
	
	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("producto".equals(evt.getPropertyName())){
				updateProducto();
			}else if("cantidad".equals(evt.getPropertyName())){
				
			}
		}
		
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				EventList<Producto> productos=new ca.odell.glazedlists.BasicEventList<Producto>();
				//com.luxsoft.sw3.ui.selectores.LookupUtils.getDefault().loadProductosInventariables(productos);
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DefaultFormModel model=new DefaultFormModel(new com.luxsoft.siipap.inventarios.model.TrasladoDet());
				InventarioForm form=new InventarioForm(model);
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
