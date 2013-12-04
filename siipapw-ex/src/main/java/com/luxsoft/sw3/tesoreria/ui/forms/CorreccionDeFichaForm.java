package com.luxsoft.sw3.tesoreria.ui.forms;

import java.text.MessageFormat;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.tesoreria.model.CorreccionDeFicha;

/**
 * Forma para la corrección de fichas
 * 
 * @author Ruben Cancino
 *
 */
public class CorreccionDeFichaForm extends AbstractForm{

	public CorreccionDeFichaForm(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,3dlu,p,4dlu,p,3dlu,p:g"
				,"");
		
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Fecha",addReadOnly("fecha"),true);
//<<<<<<< .mine
		builder.append("Tipo", getControl("tipo"),5);
//=======
		builder.append("Clasificacion", getControl("tipo"),5);
//>>>>>>> .r1675
		builder.append("Importe Real: ",getControl("importeReal"));
		builder.append("Importe anterior: ",addReadOnly("importeOriginal"));
		builder.append("Caja",getControl("concepto"),5);
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}
	private Header header;
	@Override
	protected JComponent buildHeader() {
		CorreccionDeFicha co=(CorreccionDeFicha)model.getBaseBean();
		
		String pattern="Folio: {0}  Sucursal: {1}";
		String message=MessageFormat.format(pattern
				,co.getFicha().getFolio()
				,co.getSucursal().getNombre()
				);
		header=new Header("Faltantes - Sobrantes",message);
		return header.getHeader();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("concepto".equals(property)){
			String hql="from ConceptoContable c where c.clave like 'EMPC%' ";
			List<ConceptoContable> data=ServiceLocator2.getHibernateTemplate().find(hql);
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}else if("comentario".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}else if("tipo".equals(property)){
						
			SelectionInList sl=new SelectionInList(CorreccionDeFicha.Tipo.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}
		return super.createCustomComponent(property);
	}

	public static CorreccionDeFicha showForm(String fichaId){
		Ficha f=(Ficha)ServiceLocator2.getUniversalDao().get(Ficha.class, fichaId);
		CorreccionDeFicha co=new CorreccionDeFicha(f);
		DefaultFormModel model=new DefaultFormModel(co);
		final CorreccionDeFichaForm form=new CorreccionDeFichaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (CorreccionDeFicha)model.getBaseBean();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showObject(showForm("8a8a8284-32bfd9e3-0132-c0ed218f-017d"));
			}
		});
	}		

}
