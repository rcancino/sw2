package com.luxsoft.siipap.gastos.operaciones;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.gastos.ActivoFijo;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * Forma para el registro y mantenimiento de instancias de {@link ActivoFijo} generadas a partir de una compra de gastos
 * 
 * @author Ruben Cancino
 *
 */
public class RegistroDeActivoPorCompraForm extends AbstractForm{
	
	
	public RegistroDeActivoPorCompraForm(IFormModel model) {
		super(model);
		setTitle("Alta de activo fijo");
	}
	
	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Alta de activo fijo","Ingreso de gasto al control de activo fijo");
	}	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;70dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;100dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos principales");
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}
		
		builder.append("Sucursal",getControl("sucursal"));
		builder.append("Departamento",getControl("departamento"));
		
		
		builder.append("Proveedor",getControl("proveedor"),5);
		builder.append("Producto",getControl("producto"),5);
		
		builder.append("Fecha Adq",addReadOnly("fechaDeAdquisicion"));
		builder.append("Documento",addReadOnly("documento"));
		
		builder.append("MOI",addReadOnly("moi"));
		builder.append("INPC",getControl("inpcOriginal"));
		
		
		
		builder.append("Tasa (Dep)",getControl("tasaDepreciacion"),true);
		builder.append("Rubro",getControl("rubro"),5);
		
		builder.appendSeparator();		
		builder.append("Responsable",getControl("consignatario"),5);
		builder.append("Comentario",getControl("comentario"),5);
		
		return builder.getPanel();
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			return BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
		}else if("producto".equals(property)){
			return BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
		}else if("sucursal".equals(property)){
			return BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
		}else if("departamento".equals(property)){
			return BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
		}else if("inpcOriginal".equals(property) || "ultimoINPC".equals(property)){
			INPCControl control=new INPCControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("rubro".equals(property)){
			JComboBox box=Bindings.createClasificacionDeActivoFijoBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("consignatario".equals(property)){
			ConsignatarioControl control=new ConsignatarioControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}
		return null;
	}
	
		
	public static ActivoFijo registrarActivo(GCompraDet gasto){
		ActivoFijo bean=new ActivoFijo();
		bean.setCompraDeGastoDet(gasto);
		bean.setFechaActualizacion(gasto.getFacturacion().getFecha());
		DefaultFormModel model=new DefaultFormModel(bean);
		final RegistroDeActivoPorCompraForm form=new RegistroDeActivoPorCompraForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (ActivoFijo)model.getBaseBean();
		}
		return null;
	}
	
	public static ActivoFijo showForm(ActivoFijo bean){
		return showForm(bean,false);
	}
	
	public static ActivoFijo showForm(ActivoFijo bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final RegistroDeActivoPorCompraForm form=new RegistroDeActivoPorCompraForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (ActivoFijo)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) throws Exception{
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				GCompraDet gasto=(GCompraDet)ServiceLocator2.getUniversalDao().get(GCompraDet.class, 763348L);
				registrarActivo(gasto);
			}
		});
	}

}
