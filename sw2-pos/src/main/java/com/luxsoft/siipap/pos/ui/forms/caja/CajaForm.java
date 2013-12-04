package com.luxsoft.siipap.pos.ui.forms.caja;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeGastos;
import com.luxsoft.siipap.pos.ui.venta.forms.SelectorDeTarjeta;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.caja.Gasto;

public class CajaForm extends AbstractForm implements PropertyChangeListener{

	public CajaForm(IFormModel model) {
		super(model);
		model.addBeanPropertyChangeListener(this);
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout("p,2dlu,max(p;100dlu),3dlu,p,2dlu,max(p;100dlu):g(.5)","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Hora",addReadOnly("hora"));
		builder.nextLine();
		if(model.getValue("concepto").equals(Caja.Concepto.CORTE_CAJA)){
			
			builder.append("Tipo",getControl("tipo"));
			builder.append("Moneda",addReadOnly("moneda"));
			
			builder.append("Pagos Registrados",addReadOnly("pagos"));
			builder.append("Cortes Acumulados",addReadOnly("cortesAcumulados"));
			
			builder.append("Cambios de cheque",addReadOnly("cambiosDeCheque"));
			builder.nextLine();
			builder.append("Importe",getControl("importe"));
			builder.append("Disponible",addReadOnly("disponible"));
			
			builder.nextLine();
		}
		
		else if(model.getValue("concepto").equals(Caja.Concepto.CAMBIO_CHEQUE)){
			builder.append("Importe",getControl("importe"));
			builder.append("Moneda",addReadOnly("moneda"));
			builder.nextLine();
			builder.appendSeparator("Datos del cheque");
			builder.append("Propietario",getControl("chequeNombre"),5);
			builder.append("Número",getControl("chequeNumero"));
			builder.append("Banco",getControl("banco"));
			builder.nextLine();
			
		}
		else if(model.getValue("concepto").equals(Caja.Concepto.CAMBIO_TARJETA)){
			builder.append("Importe",getControl("importe"));
			//builder.append("Disponible",addReadOnly("disponibleCalculado"));
			builder.nextLine();
			builder.appendSeparator("Datos de la tarjeta");
			builder.append("Tarjeta",getControl("tarjeta"),5);
			builder.append("No Autorización",getControl("numeroDeAutorizacion"));
			builder.nextLine();
			
		}
		else if(model.getValue("concepto").equals(Caja.Concepto.FONDO_FIJO)){
			builder.append("Importe",getControl("importe"));
			JButton btn=new JButton("Asignar gastos");
			btn.addActionListener(EventHandler.create(ActionListener.class, this, "seleccionarGastos"));
			builder.append("Gastos:",btn);
			builder.nextLine();
		}
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("banco".equals(property)){
			SelectionInList sl=new SelectionInList(getBancos(),model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}else if("tipo".equals(property)){
			SelectionInList sl=new SelectionInList(tipos,model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}else if("importe".equals(property)|| "pagos".equals(property)
				|| "cortesAcumulados".equals(property)|| "disponible".equals(property)){
			return Binder.createBigDecimalForMonyBinding(model.getModel(property));
		}else if("tarjeta".equals(property)){
			return buildTarjetaControl();
		}
		return super.createCustomComponent(property);
	}
	private JTextField tarjetaField;
	private JComponent buildTarjetaControl(){		
		FormLayout layout=new FormLayout("p:g,2dlu,p","");		
		tarjetaField=BasicComponentFactory.createFormattedTextField(model.getModel("tarjeta"), getFormat());
		tarjetaField.setEditable(false);
		tarjetaField.setFocusable(false);
		ComponentUtils.addF2Action(tarjetaField, getLookupTarjetaAction());				
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		final UIFButton btn=new UIFButton(getLookupTarjetaAction());
		btn.setFocusable(false);
		builder.append(tarjetaField,btn);
		return builder.getPanel();
	}
	
	private Action lookupTarjetaAction;
	
	public Action getLookupTarjetaAction(){
		if(lookupTarjetaAction==null){
			lookupTarjetaAction=new DispatchingAction(this,"seleccionarTarjeta");
			lookupTarjetaAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupTarjetaAction.putValue(Action.NAME, "F2");
		}
		return lookupTarjetaAction;
	}
	
	public void seleccionarTarjeta(){
		final SelectorDeTarjeta selector=new SelectorDeTarjeta();
		selector.open();
		if(!selector.hasBeenCanceled()){
			model.setValue("tarjeta", selector.getTarjeta());
			getControl("numeroDeAutorizacion").requestFocusInWindow();
		}
	}
	private Format format;
	public Format getFormat() {
		if(format==null){
			format=new Format(){				
				public StringBuffer format(Object obj, StringBuffer toAppendTo,FieldPosition pos) {
					if(obj!=null)
						toAppendTo.append(obj.toString());
					return toAppendTo;
				}				
				public Object parseObject(String source, ParsePosition pos) {
					return null;
				}
			};
		}
		return format;
	}
	
	public void seleccionarGastos(){
		List<Gasto> res=SelectorDeGastos.select();
		Caja caja=(Caja)model.getBaseBean();
		caja.setRembolsos(res);
	}


	private List bancos;
	
	


	public List getBancos() {
		return bancos;
	}

	public void setBancos(List bancos) {
		this.bancos = bancos;
	}
	
	
	
	


	private Object[] tipos;
	

	public void setTipos(Object...objects ) {
		this.tipos = objects;
	}

	private Map pagos=new HashMap();
	private Map cortes=new HashMap();
	private Map cambios=new HashMap();


	public Map getPagos() {
		return pagos;
	}

	public void setPagos(Map pagos) {
		this.pagos = pagos;
	}

	public Map getCortes() {
		return cortes;
	}

	public void setCortes(Map cortes) {
		this.cortes = cortes;
	}
	
	

	public Map getCambios() {
		return cambios;
	}

	public void setCambios(Map cambios) {
		this.cambios = cambios;
	}

	/**
	 * TODO Mover al FormModel o controller por que crecio en importancia
	 */
	public void propertyChange(PropertyChangeEvent evt) {		
		if(evt.getPropertyName().equals("tipo")){
			Caja.Tipo tipo=(Caja.Tipo)evt.getNewValue();
			
			if(tipo!=null){				
				Object key=evt.getNewValue();
				Object pagos=getPagos().get(key);
				if(pagos==null)
					pagos=BigDecimal.ZERO;
				model.setValue("pagos", pagos);
				
				Object cortes=getCortes().get(key);
				if(cortes==null)
					cortes=BigDecimal.ZERO;
				model.setValue("cortesAcumulados", cortes);
				model.setValue("disponible", model.getValue("disponibleCalculado"));
				Object cambios=getCambios().get(key);
				if(cambios!=null){
					model.setValue("cambiosDeCheque", cambios);
					model.setValue("disponible", model.getValue("disponibleCalculado"));
				}else{
					model.setValue("cambiosDeCheque", BigDecimal.ZERO);
					model.setValue("disponible", model.getValue("disponibleCalculado"));
				}
			}
			if(Caja.Tipo.CHEQUE.equals(tipo) 
					|| Caja.Tipo.TARJETA.equals(tipo) 
					|| Caja.Tipo.DEPOSITO.equals(tipo)
					||Caja.Tipo.TRANSFERENCIA.equals(tipo) )
			{
				model.setValue("importe", model.getValue("disponibleCalculado"));
				getControl("importe").setEnabled(false);
			}else{
				getControl("importe").setEnabled(true);
			}
		}
		
	}
	
	
	

}
