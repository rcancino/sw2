package com.luxsoft.sw3.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.TransformacionModel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeExistencias;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.VentasRoles;

/**
 * Forma para el mantenimiento de  partidas de transformacion
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class TransformacionDetForm extends AbstractForm implements ActionListener{
	
	private Action lookupAction;

	public TransformacionDetForm(IFormModel model) {
		super(model);
		setTitle("Transformacion de productos");
		model.getModel("password").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String val=(String)evt.getNewValue();
				if(StringUtils.isNotBlank(val)){
					User user=KernellSecurity.instance().findUser(val, Services.getInstance().getHibernateTemplate());
					getModel().getModel("autorizo").setValue(user);
					getModel().validate();
				}
			}
		});
		model.getModel("salida").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				getModel().getModel("entrada").setValue(evt.getNewValue());
			}
		});
		
	}
	
	private void initComponents(){
		JTextField tf1=(JTextField)getControl("origenProd");
		//tf1.setEditable(false);
		tf1.addActionListener(this);
		ComponentUtils.addF2Action(tf1, getLookupAction());
		
		JTextField tf2=(JTextField)getControl("destinoProd");
		tf2.addActionListener(this);
		//tf2.setEditable(false);
		ComponentUtils.addF2Action(tf2, getLookupAction());
	}
	
	

	@Override
	protected JComponent buildFormPanel() {
		initComponents();
		final FormLayout layout=new FormLayout(
				 "p,2dlu,150dlu, 3dlu " +
				",p,2dlu,70dlu, 3dlu " +
				",p,2dlu,70dlu"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Origen");
		builder.append("Producto [F2]",getControl("origenProd"));
		builder.append("Existencia",addReadOnly("existenciaOrigen"));	
		builder.append("Salen",getControl("salida"));
		builder.appendSeparator("Destino");
		builder.append("Producto [F2]",getControl("destinoProd"));
		builder.append("Existencia",addReadOnly("existenciaDestino"));	
		builder.append("Entran",getControl("entrada"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		builder.append("Autoriza",getControl("password"),addReadOnly("autorizo"));
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("entrada".equals(property) || "salida".equals(property)){
			
			ValueModel vm=ConverterFactory.createDoubleToIntegerConverter(model.getModel(property));
			return BasicComponentFactory.createIntegerField(vm, 0);
		}else if("password".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(model.getModel(property));
			return c;
		}else if("autorizo".equals(property)){
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property), FormatUtils.getToStringFormat());
			return c;
		}
		return super.createCustomComponent(property);
	}

	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new AbstractAction(""){
				public void actionPerformed(ActionEvent e) {
					Existencia exis=SelectorDeExistencias.seleccionar();
					if(e.getSource().equals(getControl("origenProd"))){
						//System.out.println("Origen");
						model.setValue("origen", exis);
						getControl("salida").requestFocusInWindow();
					}else{ 
						model.setValue("destino", exis);
						//System.out.println("Destino");
						getControl("entrada").requestFocusInWindow();
					}
				}
				
			};
		}
		return this.lookupAction;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(getControl("origenProd"))){
			JTextField textField=(JTextField)e.getSource();
			String clave=textField.getText();
			if(StringUtils.isNotBlank(clave)){
				model.setValue("origen", buscarPorClave(clave));
				getControl("salida").requestFocusInWindow();
			}
		}else if(e.getSource().equals(getControl("destinoProd"))){
			JTextField textField=(JTextField)e.getSource();
			String clave=textField.getText();
			if(StringUtils.isNotBlank(clave)){
				model.setValue("destino", buscarPorClave(clave));
				getControl("entrada").requestFocusInWindow();
			}
		}
		
	}
	
	private Existencia buscarPorClave(String clave){
		Sucursal s=Services.getInstance().getConfiguracion().getSucursal();
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		String hql="from Existencia e where e.sucursal.id=? and e.producto.clave=? and e.year=? and e.mes=?";
		List<Existencia> res=Services.getInstance().getHibernateTemplate().find(hql, new Object[]{s.getId(),clave,Periodo.obtenerYear(fecha),Periodo.obtenerMes(fecha)+1});
		return res.isEmpty()?null:res.get(0);
	}
	
	public static class TransformacionDetFormModel extends DefaultFormModel{

		public TransformacionDetFormModel() {
			super(Bean.proxy(TransformacionModel.class));
		}
		
		@Override
		protected void addValidation(PropertyValidationSupport support) {
			if(StringUtils.isBlank(getTransformacion().getOrigenProd())){
				support.getResult().addError("Seleccione un producto origen");
				return ;
			}
			if(getTransformacion().getSalida()<=0){
				support.getResult().addError("Digite la cantidad de salida ");
				return ;
			}
			/*if(getTransformacion().getSalida()>getTransformacion().getExistenciaOrigen()){
				support.getResult().addError("La cantidad máxima es de: "+getTransformacion().getExistenciaOrigen());
				return ;
			}*/
			/** Validando el destino **/
			if(StringUtils.isBlank(getTransformacion().getDestinoProd())){
				support.getResult().addError("Seleccione un producto destino");
				return;
			}
			if(getTransformacion().getEntrada()<=0){
				support.getResult().addError("Digite la cantidad de entrada ");
				return ;
			}
			if(getTransformacion().getOrigenProd().equalsIgnoreCase(getTransformacion().getDestinoProd())){
				support.getResult().addError("El origen y destino no puede ser el mismo producto ");
				return;
			}
			if(getTransformacion().getOrigen()!=null && getTransformacion().getDestino()!=null){
				if(getTransformacion().getOrigen().getProducto().getPrecioContado()>getTransformacion().getDestino().getProducto().getPrecioContado()){
					if(getMainModel().getValue("autorizo")==null)
						support.getResult().addError("El origen es de mayor precio que el  destino requiere autorización");
					else {
						User user=(User)getMainModel().getValue("autorizo");
						if(!user.hasRole(VentasRoles.GERENTE_SUCURSAL.name())){
							support.getResult().addError("Usuadio sin autorización para este movimiento");
						}
					}
					
				}
			}
			if(getTransformacion().getOrigen()!=null && getTransformacion().getDestino()!=null){
				Producto p1=getTransformacion().getOrigen().getProducto();
				Producto p2=getTransformacion().getDestino().getProducto();
				if(!p1.getClasificacion().equals(p2.getClasificacion())){
					support.getResult().addError("Los productos no son de la misma clasificacion");
				}
			}

		}



		private TransformacionModel getTransformacion(){
			return (TransformacionModel)getBaseBean();
		}
		
		public TransformacionModel persist(){
			return getTransformacion();
		}
		
	}
	
	public static TransformacionModel showForm(){
		final TransformacionDetFormModel model=new TransformacionDetFormModel();
		final TransformacionDetForm form=new TransformacionDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.persist();
		}
		return null;
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
				showObject(showForm());
				System.exit(0);
			}

		});
	}

	

	

}
