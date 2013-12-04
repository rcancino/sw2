package com.luxsoft.siipap.inventario.ui.consultas;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Producto.Presentacion;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swx.catalogos.ProductoFormModel.Familia;



/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ProductoParaInventarioForm extends GenericAbstractForm<Producto>{
	
	private JTabbedPane tabPanel;

	public ProductoParaInventarioForm(ProductoParaInventarioFormModel model) {
		super(model);
		setTitle("Catálogo de Productos");
	}
	
	private ProductoParaInventarioFormModel getProductoModel(){
		return (ProductoParaInventarioFormModel)getModel();
	}
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Productos ","Compra/Venta");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		tabPanel=new JTabbedPane();
		tabPanel.addTab("Generales", buildGeneralForm());
		return tabPanel;
	}
	
	@Override
	protected void onWindowOpened() {		
		super.onWindowOpened();
	}



	private JComponent buildGeneralForm(){
		JScrollPane sp=new JScrollPane();
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;90dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		
		builder.append("Clave",addReadOnly("clave"),true);
		builder.append("Descripción",addReadOnly("descripcion"),5);
		
		builder.append("Linea",addReadOnly("linea"));
		builder.append("Clase",addReadOnly("clase"));
		builder.append("Marca",addReadOnly("marca"));
		builder.nextLine();
		
		builder.append("Kilos",addReadOnly("kilos"));
		builder.append("Gramos",addReadOnly("gramos"));
		
		builder.append("Largo",addReadOnly("largo"));
		builder.append("Ancho",addReadOnly("ancho"));
		
		builder.append("Calibre",addReadOnly("calibre"));
		builder.append("M2 por Mill",addReadOnly("metros2PorMillar"));
		
		builder.append("Activo",addReadOnly("activo"));
		builder.append("De Línea",addReadOnly("deLinea"));
		
		builder.append("Nacional",addReadOnly("nacional"));
		builder.append("Inventariable",addReadOnly("inventariable"));
		
		
		builder.append("Color",addReadOnly("color"));
		builder.append("Paquete",addReadOnly("paquete"));
		
		builder.append("Modo de venta",addReadOnly("modoDeVenta"));
		builder.nextLine();
		
		builder.appendSeparator("Propiedades modificables");
		builder.append("Presentación",getControl("presentacion"));
		builder.append("Caras",getControl("caras"));
		builder.append("Acabado",getControl("acabado"));
		builder.append("Familia",getControl("lineaOrigen"));
		return builder.getPanel();
	}
	
	
		
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if("presentacion".equals(property)){
			SelectionInList selection=new SelectionInList(Presentacion.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(selection);
			box.setEnabled(!model.isReadOnly());
			return box;
		}if("acabado".equals(property)){
			SelectionInList selection=new SelectionInList(new String[]{"BRILLANTE","MATE","ALTO BRILLO","SEMI MATE","HOLOGRAMA","PLATA"},model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(selection);
			box.setEnabled(!model.isReadOnly());
			return box;
		}if("lineaOrigen".equals(property)){			
			ValueHolder holder=new ValueHolder(model.getValue("lineaOrigen"));
			holder.addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					Familia f=(Familia)evt.getNewValue();
					if(f!=null)
						model.setValue("lineaOrigen", f.getClave());
				}
			});
			final List<Familia> fams=getProductoModel().getFamilias();
			SelectionInList selection=new SelectionInList(fams,holder);
			JComboBox box=BasicComponentFactory.createComboBox(selection,new FamiliaRenderer());
			box.setEditable(true);
			box.setEnabled(!model.isReadOnly());
			box.setPrototypeDisplayValue(new Familia("__________","_______"));
			Object obj=model.getValue(property);
			if(obj!=null){
				System.out.println("Current: "+obj);
				Familia current=new Familia(obj.toString(),"");
				holder.setValue(current);
				int index=fams.indexOf(current);
				System.out.println("index: "+index);
				box.setSelectedIndex(index);
				//box.setSelectedItem(current);
			}
			return box;
		}
		return null;
	}
	
	public static  class FamiliaRenderer extends DefaultListCellRenderer{

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if(value!=null){
				Familia f=(Familia)value;
				value=f.getClave()+"  ("+f.getNombre()+")";
			}
			return super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
		}
		
	}
	
	public static Producto showForm(Producto bean){
		return showForm(bean,false);
	}
	
	public static Producto showForm(Producto bean,boolean readOnly){
		ProductoParaInventarioFormModel model=new ProductoParaInventarioFormModel(bean);
		model.setReadOnly(readOnly);
		final ProductoParaInventarioForm form=new ProductoParaInventarioForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Producto)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(ServiceLocator2.getProductoManager().buscarPorClave("POL74"));
		ProductoParaInventarioForm.showObject(bean);
		System.exit(0);
	}

}
