package com.luxsoft.siipap.pos.ui.forms.caja;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.pos.ui.venta.forms.SelectorDeTarjeta;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Encapsula el layout para la generacion de un pago
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PagoPanel extends JPanel implements ActionListener{
	
	private final PresentationModel model;
	private boolean seleccionDecliente=false;
	private Format format;
	private FormaDePagoHandler formaDePagoHandler;
	private Object[] formasDePago=new Object[0];
	private boolean formaDePagoModificable=true;
	
	
	public PagoPanel(final PresentationModel model){
		this(model,false);
		formaDePagoHandler=new FormaDePagoHandler();
	}
	 
	
	public PagoPanel(final PresentationModel model,boolean seleccionDecliente){
		this.model=model;
		formaDePagoHandler=new FormaDePagoHandler();
		this.seleccionDecliente=seleccionDecliente;
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		model.addBeanPropertyChangeListener(formaDePagoHandler);
		setLayout(new BorderLayout());
		add(buildForm(),BorderLayout.CENTER);
		actualizarCampos();
	}

	@Override
	public void removeNotify() {		
		super.removeNotify();
		model.removeBeanPropertyChangeListener(formaDePagoHandler);
	}


	private void initComponents(){		
			
	}
	
	protected JComponent buildForm(){
		initComponents();
		FormLayout layout=new FormLayout(
				"p,2dlu,max(p;75dlu),3dlu," +
				"p,2dlu,max(p;75dlu),3dlu," +
				"p,2dlu,max(p;75dlu):g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Fecha",getControl("fecha"),true);
		
		if(seleccionDecliente){
			builder.append("Cliente",getControl("cliente"),5);
			builder.append("Origen",getControl("origen"));
			builder.nextLine();
		}
		
		
		//
		//builder.append("Sucursal",getReadOnly("sucursal"),true);
		
		
		
		
		builder.append("F.P.",getControl("formaDePago"));
		builder.append("Efectivo",getControl("efectivo"));
		builder.append("Cheque",getControl("cheque"));
		//builder.append("Importe",getControl("importe"));
		
		builder.nextLine();
		
		builder.append("Banco",getControl("banco"));
		builder.append("Referencia",getControl("referencia"));
		builder.nextLine();
		builder.append("Fecha Dep",getControl("fechaDeposito"));
		builder.append("Cuenta(Dest)",getControl("cuentaDestino"));
		builder.nextLine();
		
		builder.append("Moneda",getControl("moneda"));
		builder.append("T.C.",getControl("tc"));
		builder.append("Anticipo",getControl("anticipo"));
		
		builder.append("Comentario",getControl("comentario"),9);
		
		
		builder.appendSeparator("Cheque");
		
		builder.append("Cuenta",getControl("cuenta"));
		builder.append("Número",getControl("numero"));
		builder.append("Vto",getControl("vencimiento"),true);		
		builder.append("Nombre",getControl("nombre"),9);
		
		
		builder.appendSeparator("Tarjeta");
		builder.append("Tarjeta",getControl("tarjeta"),9);
		builder.nextLine();
		builder.append("Numero",getReadOnly("numeroTarjeta"));
		builder.append("Autorización",getReadOnly("autorizacion"));
		
		efectivo(true);
		ComponentUtils.decorateSpecialFocusTraversal(builder.getContainer());
		nombreField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,ComponentUtils.getNextFoculsKeys(KeyStroke.getKeyStroke("ENTER")));
		return builder.getPanel();
	}

	
	
	
	private JTextField nombreField=new JTextField(20);
	
	private JComponent buildClienteControl(){
		
		FormLayout layout=new FormLayout("p,2dlu,p","");
		
		//nombreField=new JTextField(20);
		nombreField.addActionListener(this);
		nombreField.setEditable(seleccionDecliente);
		if(seleccionDecliente){			
			ComponentUtils.addF2Action(nombreField, getLookupAction());
		}		
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		final UIFButton btn=new UIFButton(getLookupAction());
		btn.setFocusable(false);
		builder.append(nombreField,btn);
		return builder.getPanel();
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
	
	private Action lookupAction;
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"selecctionarCliente");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
			lookupAction.setEnabled(seleccionDecliente);
			
		}
		return lookupAction;
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
	
	
	private Map<String, JComponent> components=new HashMap<String, JComponent>();
	
	protected JComponent getControl(final String property){	
		JComponent c=components.get(property);		
		if(c==null){
			if("moneda".equals(property)){
				c= BasicComponentFactory.createLabel(model.getModel(property),getFormat());
			}else if("cliente".equals(property)){				
				c= buildClienteControl();
			}else if("fecha".equals(property)){
				JLabel ln=BasicComponentFactory.createLabel(model.getModel(property),new SimpleDateFormat("dd / MMMM  / yyyy"));
				ln.setFont(ln.getFont().deriveFont(Font.BOLD));
				return ln;
			}else if("importe".equals(property)){
				c= Binder.createBigDecimalForMonyBinding(model.getModel(property));
			}else if("anticipo".equals(property)){
				c= BasicComponentFactory.createCheckBox(model.getModel(property),"");
			}else if("origen".equals(property)){
				SelectionInList sl=new SelectionInList(
						new OrigenDeOperacion[]{OrigenDeOperacion.CRE,OrigenDeOperacion.CAM,OrigenDeOperacion.MOS}
					,model.getModel(property));
				c= BasicComponentFactory.createComboBox(sl);
			}else if("tc".equals(property)){
				c= Bindings.createDoubleBinding(model.getModel(property));
			}else if("formaDePago".equals(property)){
				SelectionInList sl=new SelectionInList(getFormasDePago(),model.getModel(property));
				c= BasicComponentFactory.createComboBox(sl);
				//c.setEnabled(isFormaDePagoModificable());
			}else if("referencia".equals(property)){
				c= BasicComponentFactory.createTextField(model.getModel(property), false);
			}else if("comentario".equals(property)){
				c=BasicComponentFactory.createTextField(model.getModel(property), false);
			}else if("cuenta".equals(property)){
				c= BasicComponentFactory.createTextField(model.getModel(property), false);
			}else if("numero".equals(property)){
				c= BasicComponentFactory.createIntegerField(model.getModel(property));
			}else if("postFechado".equals(property)){
				c= BasicComponentFactory.createCheckBox(model.getModel(property),"");
			}else if("nombre".equals(property)){
				c= BasicComponentFactory.createTextField(model.getModel(property), false);
			}else if("vencimiento".equals(property)){
				c= Binder.createDateComponent(model.getModel(property));
			}else if("efectivo".equals(property) ||"cheque".equals(property)||"transferencia".equals(property)){
				c= Binder.createBigDecimalForMonyBinding(model.getModel(property));
			}else if("tarjeta".equals(property)){
				c= buildTarjetaControl();
			}else if("banco".equals(property)){
				List data=Services.getInstance().getUniversalDao().getAll(Banco.class);
				SelectionInList sl=new SelectionInList(data,model.getModel(property));
				c= BasicComponentFactory.createComboBox(sl);
			}else if("cuentaDestino".equals(property)){
				String hql="from Cuenta c where c.activoEnVentas=true";
				List data=Services.getInstance().getHibernateTemplate().find(hql);
				SelectionInList sl=new SelectionInList(data,model.getModel(property));
				c= BasicComponentFactory.createComboBox(sl);
			}else if("fechaDeposito".equals(property)){
				c=Binder.createDateComponent(model.getModel(property));
			}else{
				c=new JTextField(15);
				c.setName("property");
				c.setToolTipText(property+ " Sin binding");
				
				//return c;
			}
			components.put(property, c);
			return c;
		}else
			return c;
	}
	
	protected JComponent getReadOnly(final String property){
		JLabel c=(JLabel)components.get(property);
		if(c==null){
			try {
				c=BasicComponentFactory.createLabel(model.getModel(property), getFormat());
				//c.setHorizontalAlignment(JLabel.RIGHT);
				components.put(property, c);
			} catch (Exception e) {
				c=new JLabel("ND");
				//c.setHorizontalAlignment(JLabel.RIGHT);
				c.setToolTipText(ExceptionUtils.getRootCauseMessage(e));
				return c;
			}
		}
		return c;
	}
	
	/**
	 * Implementacion local de {@link ActionListener} para  buscar el cliente por clave
	 * TODO Mover al controlador o LookupUtils
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==nombreField){
			if(!StringUtils.isBlank(nombreField.getText())){
				Cliente c=Services.getInstance().getClientesManager().buscarPorClave(nombreField.getText());
				model.setValue("cliente", c);
			}else
				model.setValue("cliente", null);
		}
	}
	
	public void efectivo(boolean enabled){
		String[] properties={"cliente","formaDePago","efectivo","anticipo","origen","comentario","cuentaDestino","fechaDeposito"};
		habilitar(properties, enabled);
		
	}
	
	public void cheque(boolean enabled){		
		String[] properties={"cliente","formaDePago","cheque","anticipo","origen","comentario","banco","nombre","cuenta","numero","cuentaDestino","fechaDeposito"};
		habilitar(properties, enabled);
		
	}
	
	public void chequePostFechado(boolean enabled){		
		String[] properties={"cliente","formaDePago","importe","cheque","origen","comentario","banco","nombre","cuenta","numero","vencimiento","cuentaDestino","fechaDeposito"};
		habilitar(properties, enabled);
		
	}
	
	public void deposito(boolean enabled){
		String[] properties={"cliente","formaDePago","efectivo","cheque","anticipo","origen","comentario","banco","efectivo","cheque","referencia","cuentaDestino","fechaDeposito"};
		habilitar(properties, enabled);
	}
	
	public void transferencia(boolean enabled){
		String[] properties={"cliente","efectivo","anticipo","origen","comentario","banco","referencia","transferencia","cuentaDestino","fechaDeposito"};
		habilitar(properties, enabled);
	}
	
	public void tarjeta(boolean enabled){
		String[] properties={"cliente","formaDePago","efectivo","anticipo","origen","comentario","numeroTarjeta","autorizacion","cuentaDestino","fechaDeposito"};
		habilitar(properties, enabled);
	}
	
	private void habilitar(String properties[],boolean enabled){
		for(Map.Entry<String,JComponent> entry:components.entrySet()){
			if(ArrayUtils.contains(properties, entry.getKey())){
				entry.getValue().setEnabled(enabled);
			}else
				entry.getValue().setEnabled(!enabled);
		}
		afterFormaDePagoChanged();
	}
	
	protected void afterFormaDePagoChanged(){
		
		getControl("formaDePago").setEnabled(isFormaDePagoModificable());
	}
	
	public void selecctionarCliente(){
		Cliente c=SelectorDeClientes.seleccionar(nombreField.getText());
		if(c!=null){			
			model.setValue("cliente", c);
			nombreField.setText(c.getClave());
		}else
			nombreField.setText("");
	}

	public void seleccionarTarjeta(){
		FormaDePago fp=(FormaDePago)model.getValue("formaDePago");
		if(fp.name().startsWith("TARJETA")){
			final SelectorDeTarjeta selector=new SelectorDeTarjeta();
			selector.open();
			if(!selector.hasBeenCanceled()){
				model.setValue("tarjeta", selector.getTarjeta());
				getControl("efectivo").requestFocusInWindow();
			}
		}
	}

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

	public void setFormat(Format format) {
		this.format = format;
	}

	public Object[] getFormasDePago() {
		return formasDePago;
	}


	public void setFormasDePago(Object[] formasDePago) {
		this.formasDePago = formasDePago;
	}
	
	

	public boolean isFormaDePagoModificable() {
		return formaDePagoModificable;
	}


	public void setFormaDePagoModificable(boolean formaDePagoModificable) {
		this.formaDePagoModificable = formaDePagoModificable;
	}


	public void actualizarCampos(){
		FormaDePago fp=(FormaDePago)model.getValue("formaDePago");
		if(fp!=null){
			switch (fp) {
			case EFECTIVO:
				deposito(false);
				transferencia(false);
				tarjeta(false);
				cheque(false);
				chequePostFechado(false);
				efectivo(true);
				break;
			case CHEQUE:
				deposito(false);
				transferencia(false);
				efectivo(false);
				tarjeta(false);
				chequePostFechado(false);
				cheque(true);
				break;
			case CHEQUE_POSTFECHADO:
				deposito(false);
				transferencia(false);
				efectivo(false);
				tarjeta(false);					
				cheque(false);
				chequePostFechado(true);
				break;
			case TARJETA_CREDITO:
			case TARJETA_DEBITO:
				seleccionarTarjeta();
				deposito(false);
				transferencia(false);
				efectivo(false);	
				cheque(false);
				chequePostFechado(false);
				tarjeta(true);
				break;
			case DEPOSITO:					
				transferencia(false);
				efectivo(false);	
				cheque(false);
				chequePostFechado(false);
				tarjeta(false);
				deposito(true);
				break;
			case TRANSFERENCIA:
				deposito(false);					
				efectivo(false);	
				cheque(false);
				chequePostFechado(false);
				tarjeta(false);
				transferencia(true);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Forma de pago Handler para activar y desactivar campos en funcion de la forma de pago
	 * seleccionada
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class FormaDePagoHandler implements PropertyChangeListener{		
		public void propertyChange(PropertyChangeEvent evt) {			
			if("formaDePago".equals(evt.getPropertyName())){
				
				//FormaDePago fp=(FormaDePago)evt.getNewValue();
				actualizarCampos();
				
			}
			
		}		
	}
	

}
