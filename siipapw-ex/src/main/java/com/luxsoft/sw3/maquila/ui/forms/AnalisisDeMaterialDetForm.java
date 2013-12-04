package com.luxsoft.sw3.maquila.ui.forms;

import java.text.MessageFormat;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;





/**
 * Forma para la asignación de costos a la entrada unitaria 
 *  de material {@link EntradaDeMaterialDet}
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisDeMaterialDetForm extends AbstractForm{
	
	
	public AnalisisDeMaterialDetForm(IFormModel model) {
		super(model);
		setTitle("Entrada de material");
	}
	

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,90dlu,2dlu" +
				",p,2dlu,90dlu" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		//builder.append("Producto",add("producto"),5);
		//builder.nextLine();		
		builder.append("Kilos",addReadOnly("kilos"));
		builder.append("Precio ",addReadOnly("precioPorKilo"));
		builder.nextLine();
		builder.append("M2",addReadOnly("metros2"));
		builder.append("Precio",addReadOnly("precioPorM2"));
		builder.nextLine();
		builder.append("Importe",getControl("importe"));
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		return builder.getPanel();
	}
	
	

	private Header header;
	
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Seleccione un producto","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	public void updateHeader() {
		if(header!=null){
			Producto p=(Producto)model.getValue("producto");
			if(p!=null){
				header.setTitulo(MessageFormat.format("{0} ({1})",p.getDescripcion(),p.getClave()));
				String pattern="Uni: {0} Largo: {1} Ancho: {2} Calibre: {3}" +
						"\nAcabado: {4} Caras: {5}";
				String desc=MessageFormat.format(pattern, p.getUnidad().getNombre(),p.getLargo(),p.getAncho(),p.getCalibre(),p.getAcabado(),p.getCaras());
				header.setDescripcion(desc);
			}
			else{
				header.setTitulo("Seleccione un producto");
				header.setDescripcion("");
			}
		}
	}
	
	

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DefaultFormModel model=new DefaultFormModel(new EntradaDeMaterialDet());
				AnalisisDeMaterialDetForm form=new AnalisisDeMaterialDetForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					showObject(model.getBaseBean());
				}
				System.exit(0);
			}

		});
	}

}
