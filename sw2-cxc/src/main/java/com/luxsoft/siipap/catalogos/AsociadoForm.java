package com.luxsoft.siipap.catalogos;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Asociado;

public class AsociadoForm extends GenericAbstractForm<Asociado>{

	private JTabbedPane tab;
	private JComponent cliente;

	public AsociadoForm(IFormModel model) {
		super(model);
		setTitle("Catalogo de Socios");
	}
	
	@Override
	protected JComponent buildFormPanel() {
		tab=new JTabbedPane();
		tab.add("General", buildForm());
		return tab;
	}

	
	private JComponent buildForm(){
		initComponents();
		FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;90dlu):g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setBorder(new TitledBorder("Datos"));
		builder.append("Cliente",cliente,5);
		builder.nextLine();
		builder.append("Vendedor",getControl("vendedor"),5);
		builder.nextLine();
		builder.append("Direccion",getControl("direccion"),5);
		builder.nextLine();
		builder.append("Comi Ven",getControl("comisionVendedor"));
		builder.nextLine();
		builder.append("Comi Cob",getControl("comisionCobrador"));
		return builder.getPanel();
	}

	private void initComponents(){
		cliente=Binder.createClientesBinding(model.getModel("cliente"));
	}


	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Socios","Catalogo de Socios");
	}
	
	
	public static Asociado showForm(Asociado bean){
		return showForm(bean,false);
	}
	
	public static Asociado showForm(Asociado bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final AsociadoForm form=new AsociadoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Asociado)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Asociado());
		AsociadoForm.showObject(bean);
		System.exit(0);
	}


	
	

}
