package com.luxsoft.siipap.cxc.ui.form;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class JuridicoForm extends AbstractForm{

	public JuridicoForm(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,90dlu, 3dlu, p,2dlu,90dlu:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Cuenta por cobrar");
		builder.append("Cliente",addReadOnly("nombre"),5);
		builder.append("Docto",addReadOnly("documento"));
		builder.append("Fiscal",addReadOnly("fiscal"));
		builder.append("Fecha",addReadOnly("fechaDocto"));
		builder.append("Vto",addReadOnly("vencimiento"));
		builder.append("Moneda",addReadOnly("moneda"));
		builder.append("TC",addReadOnly("tc"));
		builder.append("Total",addReadOnly("total"));
		builder.append("Atraso",addReadOnly("atraso"));
		builder.appendSeparator("Jurídico");
		builder.append("Fecha",getControl("traspaso"),true);
		builder.append("Abogado",getControl("abogado"),5);
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("abogado".equals(property)){
			String[] data={
					 "CENTRAL DE COBRANZA"
					,"FRANCISCO FRIAS ( 2000 PLUS)"
					,"ALEJANDRO LEZAMA BRACHO"
					};
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}

	protected JComponent buildHeader() {
		return new HeaderPanel("Traslado a Jurídico","Cuenta por cobrar adminsitrada en jurídico");
	}
	
	public static Juridico showForm(final Cargo c){
		Juridico jur=new Juridico();
		jur.setCargo(c);
		DefaultFormModel model=new DefaultFormModel(jur);
		final JuridicoForm form=new JuridicoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Juridico)model.getBaseBean();
		}
		return null;
	}
	
	public static Juridico showForm(final Juridico jur,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(jur);
		model.setReadOnly(readOnly);
		final JuridicoForm form=new JuridicoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Juridico)model.getBaseBean();
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private static Juridico showForm(){
		DefaultFormModel model=new DefaultFormModel(new Juridico());
		final JuridicoForm form=new JuridicoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Juridico)model.getBaseBean();
		}
		return null;
	}
	
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				Cargo c=ServiceLocator2.getCXCManager().getCargo("8a8a81c7-220e83c2-0122-0e8a1922-0001");
				System.out.println(ToStringBuilder.reflectionToString(showForm(c),ToStringStyle.MULTI_LINE_STYLE));
				
				
				
			}
		});
		
	}

}
