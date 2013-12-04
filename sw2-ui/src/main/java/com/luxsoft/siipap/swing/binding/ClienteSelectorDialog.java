package com.luxsoft.siipap.swing.binding;

import javax.swing.JComponent;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Un dialogo para seleccion un solo cliente
 * 
 * @author Ruben Cancino
 *
 */
public class ClienteSelectorDialog extends SXAbstractDialog{
	
	private ValueModel clienteModel=new ValueHolder();
	
	public ClienteSelectorDialog(){
		this("Clientes");
	}
	
	public ClienteSelectorDialog(ValueModel model){
		this();
		setClienteModel(model);
	}
	
	public ClienteSelectorDialog(String title) {
		super(title);
	}


	private String headerTitle="Selección de Cliente";
	private String headerDescription="Esta opción requiere seleccionar un cliente";
	

	@Override
	protected JComponent buildContent() {
		FormLayout layout=new FormLayout(
				"p,3dlu,p"
				,"p,3dlu,p");
		PanelBuilder builder=new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc=new CellConstraints();
		builder.addLabel("Cliente: ", cc.xy(1, 1));
		builder.add(Binder.createClientesBinding(getClienteModel()),cc.xy(3, 1));
		builder.add(buildButtonBarWithClose(),cc.xyw(1, 3,3));
		return builder.getPanel();
	}
	
	
	
	@Override
	public void doApply() {		
		super.doApply();
		onAccept((Cliente)getClienteModel().getValue());
	}

	/**
	 * Template method para que las sub clases puedan controloar el flujo de las accciones una vez seleccionado un cliente
	 * al estilo Spring RC
	 * 
	 * @param c
	 */
	protected void onAccept(Cliente c){
		
	}
	
	


	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel(getHeaderTitle(),getHeaderDescription());
	}




	public String getHeaderDescription() {
		return headerDescription;
	}


	public void setHeaderDescription(String headerDescription) {
		this.headerDescription = headerDescription;
	}


	public String getHeaderTitle() {
		return headerTitle;
	}
	public void setHeaderTitle(String headerTitle) {
		this.headerTitle = headerTitle;
	}

	public ValueModel getClienteModel() {
		return clienteModel;
	}

	public void setClienteModel(ValueModel clienteModel) {
		this.clienteModel = clienteModel;
	}
	
	


	
}
