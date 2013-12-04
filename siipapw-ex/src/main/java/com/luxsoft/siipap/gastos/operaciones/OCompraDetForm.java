package com.luxsoft.siipap.gastos.operaciones;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.springframework.beans.BeanUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class OCompraDetForm extends GenericAbstractForm<GCompraDet>{
	
	private boolean conProveedor=false;
	
	private OCompraDetForm(IFormModel model) {
		super(model);
		model.getModel("producto").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				GProductoServicio val=(GProductoServicio)evt.getNewValue();						
				if(val.getRubro()!=null){
					logger.debug("Actualizando al rubro: "+val.getRubro());
					getModel().setValue("rubro", val.getRubro());
				}
				/*
				if(evt.getOldValue()==null ){
					
				}
				*/
			}
			
		});
		setTitle("Producto");
	}
	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,4dlu,70dlu, 2dlu," +
				"p,4dlu,70dlu, 2dlu," +
				"p,4dlu,70dlu:g " 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Producto", getControl("producto"),9);
		if(isConProveedor()){
			builder.append("Factura", getControl("facturaRembolso"));
			builder.append("Proveedor", getControl("proveedorRembolso"),5);
			
		}
		builder.append("Rubro",getControl("rubro"),5);
		builder.append("IETU",getControl("conceptoContable"));
		
		builder.nextLine();
		builder.append("Cantidad",getControl("cantidad"));
		builder.append("Precio",getControl("precio"),true);
		
		
		builder.append("Serie",getControl("serie"));
		builder.append("Modelo",getControl("modelo"),5);
		
		builder.appendSeparator("Descuentos");
		builder.append("Desc1",getControl("descuento1"));			
		builder.append("Desc2",getControl("descuento2"));
		builder.append("Desc3",getControl("descuento3"));
		
		builder.appendSeparator("Impuestos");
		builder.append("Iva",getControl("impuesto"));			
		builder.append("Ret 1",getControl("retencion1"));
		builder.append("Ret 2",getControl("retencion2"));
		
		builder.appendSeparator("Otros");
		builder.append("Sucursal",getControl("sucursal"));	
		builder.append("Comentario",getControl("comentario"),5);
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("producto".equals(property)){
			ProductoControl control=new ProductoControl(model.getModel("producto"));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("rubro".equals(property)){
			JComboBox box=Bindings.createConceptosDeGastoBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("sucursal".equals(property)){
			JComboBox box=Bindings.createSucursalesBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if(property.startsWith("reten")){
			JTextField tf=Bindings.createDoubleBinding(model.getModel(property),4,4);
			tf.setEnabled(!model.isReadOnly());
			return tf;
		}else if("proveedorRembolso".equals(property)){
			ProveedorControl control=new ProveedorControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			control.setEnabled(model.getValue("id")==null);
			return control;
		}else if("conceptoContable".equals(property)){
			ValueModel vm=model.getModel(property);
			final List<String> data=ServiceLocator2.getHibernateTemplate()
					.find("select c.descripcion from ConceptoContable c where c.cuenta.clave=? " +
							"and c.descripcion!=\'COMPRAS\' ","900" );
			final SelectionInList list=new SelectionInList(data,vm);
			final JComboBox box=BasicComponentFactory.createComboBox(list);	
			if(box.getItemCount()>0 && (vm.getValue()==null))
				box.setSelectedIndex(0);
			return box;
		}else
			return null;
	}
	
	public void doApply(){
		GCompraDet bean=(GCompraDet)model.getBaseBean();
		bean.actualizar();
		super.doApply();
	}
	
	public boolean isConProveedor() {
		return conProveedor;
	}
	public void setConProveedor(boolean conProveedor) {
		this.conProveedor = conProveedor;
	}
	
	public static GCompraDet edit(GCompraDet source){
		return edit(source,false);
	}
	
	public static GCompraDet edit(GCompraDet source,boolean conProveedor){
		GCompraDet target=new GCompraDet();		
		BeanUtils.copyProperties(source, target,new String[]{"id","version","compra"});
		DefaultFormModel model=new DefaultFormModel(target,false);
		OCompraDetForm form=new OCompraDetForm(model);
		form.setConProveedor(conProveedor);
		form.open();
		if(!form.hasBeenCanceled()){
			BeanUtils.copyProperties(target,source ,new String[]{"id","version","compra"});
			return source;
		}
		return null;
	}
	
	public static void view(GCompraDet source){
		GCompraDet target=new GCompraDet();
		BeanUtils.copyProperties(source, target,new String[]{"id","version","compra"});
		DefaultFormModel model=new DefaultFormModel(target,true);
		OCompraDetForm form=new OCompraDetForm(model);
		form.open();
	}
	
	

}
