package com.luxsoft.siipap.inventario;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.binding.ProductoLookup;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swx.binding.ProductoControl;

/**
 * Forma para el mantenimiento de instancias de {@link Transformacion}
 * 
 * @author Octavio
 *
 */
public class TransformacionForm extends GenericAbstractForm<Transformacion>{
	
	private JComboBox serie;

	public TransformacionForm(IFormModel model) {
		super(model);
		setTitle("Transformaciones");
	}
	
	private void initComponents(){
		serie=new JComboBox();
		serie.addItem("E");
		serie.addItem("A");
		serie.addItem("C");
	}
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Tranformaciones","Transformaciones de Articulos");
	}


	@Override
	protected JComponent buildFormPanel() {
		initComponents();
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildSalidaForm(),BorderLayout.NORTH);
		panel.add(buildEntradaForm(),BorderLayout.CENTER);
		return panel;
	}
	

	private JComponent buildSalidaForm(){
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu,2dlu," +
				"50dlu,2dlu,70dlu,2dlu,"  +
				"30dlu,2dlu,30dlu,2dlu","p,20dlu");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setBorder(new TitledBorder("Transformacion"));
		builder.append("Sucursal",getControl("sucursal"),1);
		builder.append("fecha",getControl("fecha"),3);
		builder.nextLine();
		builder.append("Documento",getControl("documento"),1);
		builder.append("Serie", getControl("serie"),3);
		builder.nextLine();
		builder.append("Clase",getControl("clase"),1);
		builder.append("Gastos",getControl("gastos"),3);
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),7);
		return builder.getPanel();
	}
	
	private JComponent buildEntradaForm(){
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu,2dlu," +
				"50dlu,2dlu,70dlu,2dlu,"  +
				"30dlu,2dlu,30dlu,2dlu","p,20dlu");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setBorder(new TitledBorder("Articulo a Transformar"));
		builder.append("Origen",getControl("origen"),8);
		builder.nextLine();
		builder.append("Cantidad",getControl("cantidad"));
		builder.nextLine();
		builder.append("Destino",getControl("destino"),8);
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("sucursal".equals(property)){
			JComponent c=Bindings.createSucursalesBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("origen".equals(property)){
			ProductoLookup control=new ProductoLookup();
			control.setValueModel(model.getModel(property));
			return control;
		}else if("destino".equals(property)){
			ProductoLookup control=new ProductoLookup();
			control.setValueModel(model.getModel(property));
			return control;
		}else if("clase".equals(property)){
			JComboBox box=Bindings.createClaseBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("serie".equals(property)){
			return serie;
		}
		return null;
	}
	

	
	
	

	
	
	public static Transformacion showForm(Transformacion bean){
		return showForm(bean,false);
	}
	
	public static Transformacion showForm(Transformacion bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final TransformacionForm form=new TransformacionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Transformacion)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Transformacion());
		TransformacionForm.showObject(bean);
		ServiceLocator2.getInventarioManager().salvarTransformacion((Transformacion)bean);
		System.exit(0);
	}

}
