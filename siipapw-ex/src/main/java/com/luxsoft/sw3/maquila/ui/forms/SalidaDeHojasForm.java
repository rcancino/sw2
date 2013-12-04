package com.luxsoft.sw3.maquila.ui.forms;

import java.text.MessageFormat;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeHojeadoDisponible;






/**
 * Forma la generacion de salida de hojas
 * 
 * @author Ruben Cancino
 *
 */
public class SalidaDeHojasForm extends AbstractForm{
	
	private double requerido;
	
	public SalidaDeHojasForm(RecepcionDeCorteDet rec) {
		this(new SalidaDeHojasFormModel(rec));
	}
 	
	public SalidaDeHojasForm(IFormModel model) {
		super(model);
		setTitle("Salida de Hojas");
	}	

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,90dlu:g(.5),2dlu" +
				",p,2dlu,90dlu:g(.5)" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.appendSeparator("Salida de Hojeado");
		builder.append("Cantidad",getControl("cantidadDeSalida"));
		//builder.nextLine();
		//builder.append("Comentario",getControl("comentario"),5);
		
		return builder.getPanel();
	}	

	private Header header;	
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	public void updateHeader() {
		if(header!=null){
			Producto p=getRecepcionDet().getProducto();
			header.setTitulo(MessageFormat.format("{0} ({1})"						
					,p.getDescripcion()
					,p.getClave())
					);
			String pattern=
					  "Entrada de Maquilador: {0}     " +
					  "\n" +
					  "Requerido: {1}       Disponible: {2}"; 
			String desc=MessageFormat.format(pattern
					,getRecepcionDet().getOrigen().getEntradaDeMaquilador()
					,getRequerido()
					,getRecepcionDet().getDisponible()
					);
			header.setDescripcion(desc);
		}
	}
	
	

	@Override
	protected JComponent createCustomComponent(String property) {
		if("cantidadDeSalida".equals(property)){
			return Binder.createNumberBinding(model.getModel(property), 0);
		}
		return super.createCustomComponent(property);
	}

	private RecepcionDeCorteDet getRecepcionDet(){
		RecepcionDeCorteDet det=(RecepcionDeCorteDet)model.getBaseBean();
		return det;
	}
	
	
	public double getRequerido() {
		return requerido;
	}

	public void setRequerido(double requerido) {
		this.requerido = requerido;
	}



	public static class SalidaDeHojasFormModel extends DefaultFormModel{

		public SalidaDeHojasFormModel(Object bean) {
			super(bean);
		}

		@Override
		protected void addValidation(PropertyValidationSupport support) {
			RecepcionDeCorteDet r=(RecepcionDeCorteDet)getBaseBean();
			if(r!=null){
				double sal=r.getCantidadDeSalida();
				double dis=r.getDisponible();
				System.out.println("Disponible: "+dis);
				System.out.println("Salida: "+sal);
				if(r.getCantidadDeSalida()>r.getDisponible()){
					
					support.getResult().addError("Lo máximo de la salida puede ser: "+r.getDisponible());
				}
			}
		}
		
		
	}

	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				RecepcionDeCorteDet rec=SelectorDeHojeadoDisponible.find();
				SalidaDeHojasForm form=new SalidaDeHojasForm(rec);
				form.open();
				System.exit(0);
			}

		});
	}

}
