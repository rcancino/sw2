package com.luxsoft.siipap.compras.ui.form;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;





/**
 * Forma para el mantenimiento particular de unidades de compra
 * 
 * @author Ruben Cancino
 *
 */
public class CompraCentralizadaDetForm extends AbstractForm{
	
	private EventList<Producto> productos=new BasicEventList<Producto>();
	
	private boolean centralizada=false;
	private boolean especial=false;
	private List sucursales;

	public CompraCentralizadaDetForm(IFormModel model) {
		super(model);
		setTitle("Unidad de Compra");
		model.addBeanPropertyChangeListener(new Handler());
		model.getModel("producto").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getNewValue()!=null){
					getControl("solicitado").requestFocusInWindow();
					
				}
				Producto p=(Producto)evt.getNewValue();
				if(p!=null && p.isMedidaEspecial()){
					getControl("ancho").setEnabled(true);
					getControl("largo").setEnabled(true);
				}else{
					getControl("ancho").setEnabled(false);
					getControl("largo").setEnabled(false);
				}
			}
		});
		
		
	}
	
	private Compra2 getCompra(){
		if(model.getValue("compra")!=null){
			Compra2 c=(Compra2)model.getValue("compra");
			return c;
		}
		return null;
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,max(p;100dlu):g(.5),2dlu" +
				",p,2dlu,max(p;100dlu):g(.5)" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",getControl("producto"),5);
		//builder.nextLine();
		builder.append("Cantidad",getControl("solicitado"),true);
		
		builder.append("Ancho",getControl("ancho"));
		builder.append("Largo",getControl("largo"));
		
		if(isEspecial())
			builder.append("Precio",getControl("precio"));
		else
			builder.nextLine();
		if(isCentralizada()){
			builder.append("Sucursal",getControl("sucursal"));
		}else
			builder.append("Sucursal",addReadOnly("sucursalNombre"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		if(getCompra()!=null){
			if(getCompra().isImportacion()){
				builder.append("Aduana",getControl("aduana"));
				builder.nextLine();
			}
		}
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
	
	private void resolverPrecio(){
		CompraUnitaria cu=(CompraUnitaria)model.getBaseBean();
		if(cu.getCompra()!=null)
			if(cu.getCompra().getProveedor()!=null){
				ListaDePreciosDet res=ServiceLocator2.getListaDePreciosDao().buscarPrecioVigente(cu.getProducto(), cu.getMoneda(), cu.getCompra().getProveedor(),
						cu.getCompra().getFecha());
				if(res!=null){
					model.setValue("precio", res.getNeto());
				}
			}
		
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("producto".equals(property)){
			JComponent pc=createProductosControl();
			return pc;
			
		}else if("cantidad".equals(property)){
			JComponent c=Binder.createNumberBinding(model.getModel(property), 0);
			return c;
		}else if("sucursal".equals(property)){			
			final SelectionInList sl=new SelectionInList(getSucursales());
			final JComponent c=BasicComponentFactory.createComboBox(sl);			
			c.setEnabled(!model.isReadOnly());
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
	

	public List getSucursales() {
		return sucursales;
	}

	public void setSucursales(List sucursales) {
		this.sucursales = sucursales;
	}
	
	public boolean isCentralizada() {
		return centralizada;
	}

	public void setCentralizada(boolean centralizada) {
		this.centralizada = centralizada;
	}
	
	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		this.especial = especial;
	}



	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("producto".equals(evt.getPropertyName())){
				updateProducto();
				resolverPrecio();
			}else if("cantidad".equals(evt.getPropertyName())){
				
			}
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				EventList<Producto> productos=GlazedLists.eventList(ServiceLocator2.getProductoManager().buscarProductosActivos());
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DefaultFormModel model=new DefaultFormModel(new CompraUnitaria());
				CompraCentralizadaDetForm form=new CompraCentralizadaDetForm(model);
				form.setCentralizada(true);
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
