package com.luxsoft.sw3.contabilidad.ui.form;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.contabilidad.model.Naturaleza;



public class CuentaContableForm extends AbstractForm{

	public CuentaContableForm(CuentaContableFormModel model) {
		super(model);
	}
	public CuentaContableFormModel getBaseModel(){
		return (CuentaContableFormModel)getModel();
	}

	@Override
	protected JComponent buildFormPanel() {	
		JTabbedPane tabPanel=new JTabbedPane();
		FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.3), 3dlu," +
				"p,2dlu,p:g(.3), 3dlu," +
				"p,2dlu,p:g(.3)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Descripción");		
		builder.append("Clave",getControl("clave"));
		//builder.append("Padre",getControl("padre"),5);
		builder.nextLine();
		builder.append("Descripción",  getControl("descripcion"),9);
		builder.nextLine();
		builder.append("Descripción 2",getControl("descripcion2"),9);
		builder.nextLine();
		builder.appendSeparator("Características");
		builder.append("Tipo",getControl("tipo"));
		builder.append("SubTipo",getControl("subTipo"));
		builder.append("Naturaleza",	getControl("naturaleza"));		
		builder.append("De Detalle",	getControl("detalle"));
		builder.append("De Resultados",	getControl("deResultado"));
		builder.nextLine();
		builder.appendSeparator("Presentaciones");
		builder.append("Contable",		getControl("presentacionContable"));
		builder.append("Fiscal",		getControl("presentacionFiscal"));
		builder.append("Financiera",	getControl("presentacionFinanciera"));
		builder.append("Presupuestal",	getControl("presentacionPresupuestal"));
		builder.nextLine();
		tabPanel.addTab("General", builder.getPanel());
		tabPanel.addTab("Conceptos", buildConceptosPanel());
		return tabPanel;
	}
	
	private JComponent buildConceptosPanel(){
		CuentaContableConceptosPanel cpanel=new CuentaContableConceptosPanel(getBaseModel().getCuenta());
		return cpanel;
	}
	
	public void acumulativa(boolean val){
		getControl("tipo").setEnabled(val);
		getControl("subTipo").setEnabled(val);
		getControl("naturaleza").setEnabled(val);
		getControl("detalle").setEnabled(val);
		getControl("deResultado").setEnabled(val);
		getControl("presentacionContable").setEnabled(val);
		getControl("presentacionFiscal").setEnabled(val);
		getControl("presentacionFinanciera").setEnabled(val);
		getControl("presentacionPresupuestal").setEnabled(val);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("tipo".equals(property)){
			SelectionInList sl=new SelectionInList(getBaseModel().getTiposList(),getModel().getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("subTipo".equals(property)){
			SelectionInList sl=new SelectionInList(getBaseModel().getSubTiposList(),getModel().getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setPrototypeDisplayValue("SUB TIPO DE CUENTA");
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("naturaleza".equals(property)){
			SelectionInList sl=new SelectionInList(Naturaleza.values(),getModel().getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if(property.startsWith("descri")){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		return super.createCustomComponent(property);
	}
	
	private Action lookupAction;
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"buscarCuenta");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
		}
		return lookupAction;
	}	
	
	
	

}
