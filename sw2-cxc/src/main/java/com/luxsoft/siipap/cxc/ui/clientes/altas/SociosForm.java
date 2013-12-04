package com.luxsoft.siipap.cxc.ui.clientes.altas;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Socio;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.ventas.model.Vendedor;




/**
 * Forma para el mantenimiento de Clientes desde el punto de venta
 * 
 * @author Ruben Cancino
 *
 */
public class SociosForm extends AbstractForm{
	
	
	

	public SociosForm(IFormModel model) {
		super(model);
		setTitle("Mantenimiento de Socio");
	}

	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Socios de la Unión ","");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,150dlu:g(.5), 2dlu," +
				"p,2dlu,150dlu:g(.5)"
				,""
				);
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos generales");
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
			builder.nextLine();
		}
		//builder.append("Cliente",buildClienteControl(),5);
		//builder.append("Clave",getControl("clave"));
		builder.append("Nombre",getControl("nombre"),5);		
		builder.append("Dirección",getControl("direccion"),5);
		builder.appendSeparator("Vendedor");
		builder.append("Nombre",getControl("vendedor"));
		builder.append("Comisión",getControl("comisionVendedor"));
		
		return builder.getPanel();
	}
	
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("vendedor".equals(property)){
			SelectionInList sl=new SelectionInList(ServiceLocator2.getUniversalDao().getAll(Vendedor.class),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				Socio socio=new Socio();
				DefaultFormModel model=new DefaultFormModel(socio);
				SociosForm form=new SociosForm(model);
				form.open();
				System.exit(0);
			}

		});
	}
}
