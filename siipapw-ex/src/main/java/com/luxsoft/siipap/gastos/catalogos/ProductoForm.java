package com.luxsoft.siipap.gastos.catalogos;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.gastos.GastosModel;
import com.luxsoft.siipap.model.Unidad;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;



/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ProductoForm extends GenericAbstractForm<GProductoServicio>{
	
	private JTabbedPane tabPanel;

	public ProductoForm(IFormModel model) {
		super(model);
		setTitle("Catálogo de Productos y Servicios");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Productos ","Bienes y Servicios");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		tabPanel=new JTabbedPane();
		tabPanel.addTab("General", buildGeneralForm());
		//tabPanel.addTab("Dirección", buildSecondaryForm());
		//tabPanel.addTab("Cuenta", buildCuentaForm());
		
		return tabPanel;
	}
	
	@Override
	protected void onWindowOpened() {		
		super.onWindowOpened();
		//setInitialComponent(getControl("clave"));
	}



	private JComponent buildGeneralForm(){
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
			//getControl("clave").setEnabled(false);
			//getControl("clave").setFocusable(false);
		}		
		//builder.append("Clave",addMandatory("clave"),true);
		
		/*getControl("clave").addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				validarClave();
			}			
		});
		*/
		builder.append("Descripción",addMandatory("descripcion"),5);
		builder.append("Rubro",getControl("rubro"),5);
		builder.append("Unidad",addMandatory("unidad"));
		builder.append("Inventariable",addMandatory("inventariable"));
		builder.append("Servicio",getControl("servicio"));
		builder.append("Inversión",getControl("inversion"));				
		builder.append("Código",getControl("codigo"));
		builder.append("Nacional",getControl("nacional"));
		
		builder.append("IETU",getControl("ietu"));
		builder.append("Retención",getControl("retencion"));
		
		final CellConstraints cc=new CellConstraints();		
		builder.append("Observaciones");
		builder.appendRow(new RowSpec("17dlu"));
		builder.add(new JScrollPane((JTextArea) getControl("nota")),
				cc.xywh(builder.getColumn(), builder.getRow(),5,2));
		builder.nextLine(2);
		
		return builder.getPanel();
	}
	
	/*
	private JComponent buildSecondaryForm(){
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		return builder.getPanel();		
	}
	
	private JComponent buildProveedoresForm(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;70dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;70dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		return builder.getPanel();
	}
	*/
		
	@Override
	protected JComponent createCustomComponent(String property) {
		/*if("clave".equals(property)|| "nombre".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}*/
		if("nota".equals(property)){
			JTextArea control=BasicComponentFactory.createTextArea(model.getComponentModel(property), false);
			return control;
		}if("unidad".equals(property)){
			SelectionInList selection=new SelectionInList(Unidad.values(),model.getComponentModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(selection);
			box.setEnabled(!model.isReadOnly());
			return box;
		}if("rubro".equals(property)){
			JComboBox box=Bindings.createConceptosDeGastoBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return null;
	}
	/*
	private void validarClave(){
		if(model.getValue("clave")==null) return;
		String clave=model.getValue("clave").toString();
		GProductoServicio found=GastosModel.instance().buscarProducto(clave);
		if(found==null)
			return;
		if(model.getValue("id")!=null){
			if(model.getValue("id").equals(found.getId()))
				return;
		}
		if(clave.equalsIgnoreCase(found.getClave())){
			MessageUtils.showMessage(getContentPane(),"La clace ya esta registrada", "Validando");
			model.setValue("clave", null);
			getControl("clave").requestFocusInWindow();
		}				
	}
	*/
	
	public static GProductoServicio showForm(GProductoServicio bean){
		return showForm(bean,false);
	}
	
	public static GProductoServicio showForm(GProductoServicio bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final ProductoForm form=new ProductoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (GProductoServicio)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new GProductoServicio());
		ProductoForm.showObject(bean);
		System.exit(0);
	}

}
