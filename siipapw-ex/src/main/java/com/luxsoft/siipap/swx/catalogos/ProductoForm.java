package com.luxsoft.siipap.swx.catalogos;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swx.binding.ClaseControl;
import com.luxsoft.siipap.swx.binding.LineaControl;
import com.luxsoft.siipap.swx.binding.MarcaControl;
import com.luxsoft.siipap.swx.catalogos.ProductoFormModel.Familia;



/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ProductoForm extends GenericAbstractForm<Producto>{
	
	private JTabbedPane tabPanel;

	public ProductoForm(ProductoFormModel model) {
		super(model);
		setTitle("Catálogo de Productos");
	}
	
	private ProductoFormModel getProductoModel(){
		return (ProductoFormModel)getModel();
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
		tabPanel.addTab("General", buildGeneralForm());
		tabPanel.addTab("Códigos", buildCodigosForm());
		tabPanel.addTab("Comentarios", buildComentariosForm());
		tabPanel.addTab("Descuentos", buildDescuentosForm());
		tabPanel.addTab("Precios", buildPreciosForm());
		return tabPanel;
	}
	
	@Override
	protected void onWindowOpened() {		
		super.onWindowOpened();
		setInitialComponent(getControl("clave"));
	}



	private JComponent buildGeneralForm(){
		JScrollPane sp=new JScrollPane();
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;90dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("");
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("clave").setEnabled(false);
			getControl("clave").setFocusable(false);
			getControl("inventariable").setEnabled(false);
			getControl("inventariable").setFocusable(false);
		}		
		builder.append("Clave",addMandatory("clave"),true);
		getControl("clave").addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				validarClave();
			}			
		});
		
		builder.append("Descripción",addMandatory("descripcion"),5);
		builder.append("Inventariable",getControl("inventariable"));
		getControl("inventariable").addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				validarClave();
			}			
		});
		builder.append("Activo",getControl("activo"));
		builder.append("Servicio",getControl("servicio"));
		builder.append("De Línea",getControl("deLinea"));
		builder.append("Modo de venta",getControl("modoDeVenta"));
		builder.append("Nacional",getControl("nacional"));
		
		builder.append("Presentación",getControl("presentacion"));
		builder.append("Unidad",getControl("unidad"));
		
		builder.append("Kilos",getControl("kilos"));
		builder.append("Gramos",getControl("gramos"));
		builder.append("Largo",getControl("largo"));
		builder.append("Ancho",getControl("ancho"));
		builder.append("Calibre",getControl("calibre"));
		builder.append("Caras",getControl("caras"));
		builder.append("M2 por Mill",getControl("metros2PorMillar"));
		
		
		builder.append("Acabado",getControl("acabado"));
		builder.append("Color",getControl("color"));
		builder.append("Paquete",getControl("paquete"));
		builder.append("Fac. Precio N CRE",getControl("ARTFACNECR"));
		
		builder.nextLine();
		builder.append("Linea",getControl("linea"));
		builder.append("Clase",getControl("clase"));
		
		builder.append("Marca",getControl("marca"));
		builder.append("Familia",getControl("lineaOrigen"));
		
		builder.append("Clasificacion",getControl("clasificacion"));
		builder.nextLine();
		builder.appendSeparator("Precios");
		builder.append("Contado",getControl("precioContado"));
		builder.append("Crédito",getControl("precioCredito"));
		
		builder.appendSeparator("Medida especial");
		builder.append("Activar",getControl("medidaEspecial"),true);
		builder.append("Precio Kg CON",getControl("precioPorKiloContado"));
		builder.append("Precio Kg CRE",getControl("precioPorKiloCredito"));
		
		builder.appendSeparator("Operación específica");
		
		builder.append("Ventas",getControl("activoVentas"));
		builder.append("Comentario",getControl("activoVentasObs"));
		
		builder.append("Compras",getControl("activoCompras"));
		builder.append("Comentario",getControl("activoComprasObs"));
		
		builder.append("Inventario",getControl("activoInventario"));
		builder.append("Comentario",getControl("activoInventarioObs"));
		
		builder.append("Ajuste",getControl("ajuste"));
		
		return builder.getPanel();
	}
	
	private JComponent buildCodigosForm(){
		ProductoCodigosPanel panel=new ProductoCodigosPanel(getProductoModel().getPmodel());
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	private JComponent buildComentariosForm(){
		final ProductoComentariosPanel panel=new ProductoComentariosPanel(getProductoModel().getPmodel());
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	private JComponent buildDescuentosForm(){
		ProductoDescuentosPanel panel=new ProductoDescuentosPanel(getProductoModel().getPmodel());
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	private JComponent buildPreciosForm(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;90dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("clave".equals(property)|| "descripcion".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}
		if("unidad".equals(property)){
			JComboBox box=Bindings.createUnidadesBinding(model.getComponentModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}if("linea".equals(property)){
			LineaControl control=new LineaControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}if("clase".equals(property)){
			ClaseControl control=new ClaseControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}if("marca".equals(property)){
			MarcaControl control=new MarcaControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}if("presentacion".equals(property)){
			SelectionInList selection=new SelectionInList(Presentacion.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(selection);
			box.setEnabled(!model.isReadOnly());
			return box;
		}if("acabado".equals(property)){
			SelectionInList selection=new SelectionInList(new String[]{"BRILLANTE","MATE","ALTO BRILLO","SEMI MATE","HOLOGRAMA","PLATA"},model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(selection);
			box.setEnabled(!model.isReadOnly());
			return box;
		}if("modoDeVenta".equals(property)){
			SelectionInList selection=new SelectionInList(new String[]{"B","N"},model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(selection,new ModoDeVentaRenderer());
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
		}if("kilos".equals(property)){
			JComponent box=Binder.createNumberBinding(model.getModel(property), 10);
			
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("clasificacion".equals(property)){
			List data=ServiceLocator2.getJdbcTemplate().queryForList("SELECT CLASIFICACION FROM SX_PRODUCTOS_CLASIFICACION", String.class);
			
			SelectionInList sl=new SelectionInList(data, model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return null;
	}
	
	private void validarClave(){
		if(model.getValue("clave")==null) return;
		String clave=model.getValue("clave").toString();
		Producto found=ServiceLocator2.getProductoManager().buscarPorClave(clave);
		if(found==null)
			return;
		if(model.getValue("id")!=null){
			if(model.getValue("id").equals(found.getId()))
				return;
		}
		if(clave.equalsIgnoreCase(found.getClave())){
			MessageUtils.showMessage(getContentPane(),"La clave ya esta registrada", "Validando");
			model.setValue("clave", null);
			getControl("clave").requestFocusInWindow();
		}				
	}
	
	public static  class ModoDeVentaRenderer extends DefaultListCellRenderer{

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if(value!=null){
				if(value.equals("B"))
					value="BRUTO";
				else
					value="NETO";
			}
			return super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
		}
		
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
		ProductoFormModel model=new ProductoFormModel(bean);
		model.setReadOnly(readOnly);
		final ProductoForm form=new ProductoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Producto)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Producto());
		ProductoForm.showObject(bean);
		System.exit(0);
	}

}
