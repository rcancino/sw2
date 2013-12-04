package com.luxsoft.sw3.maquila.ui.forms;

import java.text.MessageFormat;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.SalidaDeBobinas;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeBobinasDisponibles;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeMaqs;






/**
 * Forma la generacion de salida de hojas
 * 
 * @author Ruben Cancino
 *
 */
public class SalidaDeBobinasForm extends AbstractForm{
	
	
	
	public SalidaDeBobinasForm(SalidaDeBobinas salida) {
		this(new SalidaDeBobinaFormModel(salida));
	}
 	
	public SalidaDeBobinasForm(IFormModel model) {
		super(model);
		setTitle("Salida de Bobina");
	}	

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,90dlu:g(.5),2dlu" +
				",p,2dlu,90dlu:g(.5)" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.appendSeparator("Salida de Bobinas");
		builder.append("Fecha",getControl("fecha"));
		builder.append("Cantidad",getControl("cantidad"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		
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
			Producto p=getSalida().getProducto();
			header.setTitulo(MessageFormat.format("{0} ({1})"						
					,p.getDescripcion()
					,p.getClave())
					);
			String pattern=
					  "Entrada de Maquilador: {0}     " +
					  "\nMaq (Dcto): {1}       Remisión:   {2}    Maq Fecha{3,date,short}" +
					  "\nRequerido : {4}       Disponible: {5}"; 
			String desc=MessageFormat.format(pattern
					,getSalida().getOrigen().getEntradaDeMaquilador()
					,getSalida().getDestino().getDocumento()
					,getSalida().getDestino().getRemision()
					,getSalida().getDestino().getFecha()
					,getSalida().getDestino().getPendiente()
					,getSalida().getOrigen().getDisponibleKilos()
					);
			header.setDescripcion(desc);
		}
	}
	
	

	@Override
	protected JComponent createCustomComponent(String property) {
		if("cantidad".equals(property)){
			return Binder.createNumberBinding(model.getModel(property), 3);
		}else if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return super.createCustomComponent(property);
	}

	private SalidaDeBobinas getSalida(){
		SalidaDeBobinas det=(SalidaDeBobinas)model.getBaseBean();
		return det;
	}
	
	



	public static class SalidaDeBobinaFormModel extends DefaultFormModel{

		public SalidaDeBobinaFormModel(Object bean) {
			super(bean);
		}

		@Override
		protected void addValidation(PropertyValidationSupport support) {
			SalidaDeBobinas s=(SalidaDeBobinas)getBaseBean();
			if(s!=null){
				double sal=s.getCantidad();
				double dis=s.getOrigen().getDisponibleKilos().doubleValue();
				System.out.println("Disponible: "+dis);
				System.out.println("Salida: "+sal);
				if(sal>dis){					
					support.getResult().addError("Lo máximo de la salida puede ser: "+dis);
				}
				if(sal<=0){
					support.getResult().addError("Cantidad incorrecta");
				}
			}
		}
		
		
	}

	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				EntradaDeMaterialDet origen=SelectorDeBobinasDisponibles.seleccionar(null);
				if(origen!=null){
					EntradaDeMaquila destino=SelectorDeMaqs.find(origen.getProducto());
					SalidaDeBobinas salida=new SalidaDeBobinas();
					salida.setOrigen(origen);
					salida.setDestino(destino);
					SalidaDeBobinasForm form=new SalidaDeBobinasForm(salida);
					form.open();
				}
				
				System.exit(0);
			}

		});
	}

}
