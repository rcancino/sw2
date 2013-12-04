package com.luxsoft.siipap.tesoreria.movimientos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.time.DateUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.Deposito;
import com.luxsoft.siipap.model.tesoreria.Concepto.Tipo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 * Forma para el mantenimiento de Bancos
 * 
 * @author Ruben Cancino
 *
 */
public class DepositoForm extends GenericAbstractForm<Deposito>{
	
	private JComboBox cuentasBox;
	

	public DepositoForm(IFormModel model) {
		super(model);
		
		setTitle("Deposito bancario");
		if(getDepositoModel().getDeposito().isRetiro())
			setTitle("Retiro bancario");
		model.getModel("moneda").addValueChangeListener(new MonedaHandler());
		model.getModel("fecha").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				registrarTipoDeCambio();
			}
		});
	}

	@Override
	protected JComponent buildHeader() {
		if(getDepositoModel().getDeposito().isRetiro())
			return new HeaderPanel("Retiro","Generación de retiro");
		else
			return new HeaderPanel("Deposito","Generación de deposito");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,p:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Sucursal",getControl("sucursal"));
		builder.nextLine();
		builder.append("Fecha",addMandatory("fecha"));
		builder.append("Concepto",getControl("concepto"));
				
		builder.append("Importe",getControl("importe"));
		builder.append("Moneda",getControl("moneda"));
		
		builder.append("Tipo de Cambio",getControl("tc"));
		builder.append("Tipo",getControl("formaDePago"));
		
		builder.append("Referencia",getControl("referencia"),5);
		builder.append("Cuenta",getControl("cuenta"),5);
		
		builder.append("Comentario",getControl("comentario"),5);
		//builder.append("No enviar",getControl("noenviar"));
		model.setValue("moneda", MonedasUtils.PESOS);//Set initial value
		
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("formaDePago".equals(property)){
			return Bindings.createFormasDeCargoAbonoBinding(model.getModel(property));
		}else if("concepto".equals(property)){
			Tipo tipo=getDepositoModel().getDeposito().isRetiro()?Tipo.CARGO:Tipo.ABONO;
			String hql="from Concepto c where c.tipo=\'@TIPO\' and c.clase=\'@CLASE\'";
			hql=hql.replaceAll("@TIPO", tipo.name());
			hql=hql.replaceAll("@CLASE",Concepto.Clase.TESORERIA1.name());
			List<Concepto> data=ServiceLocator2.getHibernateTemplate().find(hql);
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEditable(!model.isReadOnly());
			return box;
		}else if("cuenta".equals(property)){
			 cuentasBox= createCuentasBox(model.getModel(property));
			 return cuentasBox;
		}else if("comentario".equals(property)){
			return BasicComponentFactory.createTextField(model.getComponentModel(property), true);
		}else if("sucursal".equals(property)){
			List data=ServiceLocator2.getLookupManager().getSucursales();
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}
		return null;
	}
	
	private DepositoModel getDepositoModel(){
		return (DepositoModel)model;
	}
	
	private JComboBox createCuentasBox(ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=getDepositoModel().getCuentas();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","banco","numero"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
		return box;		
	}
	
	public void doApply(){
		getDepositoModel()
			.getDeposito()
			.setCuenta((Cuenta)cuentasBox.getSelectedItem());
	}
	
	
	private class MonedaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			cuentasBox.setSelectedItem(null);
			if(evt.getNewValue().equals(MonedasUtils.PESOS)){
				model.setValue("tc", BigDecimal.ONE);
				//getControl("tc").setEnabled(false);
			}else{
				//getControl("tc").setEnabled(false);
				registrarTipoDeCambio();
			}
			
		}		
	}
	
	private void registrarTipoDeCambio(){
		Date fecha=getDepositoModel().getDeposito().getFecha();
		if(fecha!=null){
			fecha=DateUtils.addDays(fecha, -1);
			double tc=ServiceLocator2.buscarTipoDeCambio(fecha);
			getDepositoModel().setValue("tc", BigDecimal.valueOf(tc));
		}else{
			getDepositoModel().setValue("tc", BigDecimal.valueOf(1d));
		}
	}
	
	
	
	 static class DepositoModel extends DefaultFormModel{
		
		private EventList<Cuenta> cuentas;
		private MonedaSelector monedaSelector;
		

		public DepositoModel() {
			this(new Deposito());
		}
		public DepositoModel(Deposito cta){
			super(cta);
		}
		
		@Override
		protected void init() {
			monedaSelector=new MonedaSelector();
			cuentas=GlazedLists.threadSafeList(new BasicEventList<Cuenta>());
			cuentas.addAll(ServiceLocator2.getLookupManager().getCuenta());
			cuentas=new FilterList<Cuenta>(cuentas,new ThreadedMatcherEditor<Cuenta>(monedaSelector));
			getModel("moneda").addValueChangeListener(monedaSelector);
			setValue("moneda", null);//Trigger initial value	
			
		}

		protected void addValidation(PropertyValidationSupport support){
			
			if((getDeposito().getMoneda()!=null) &&  !getDeposito().getMoneda().equals(MonedasUtils.PESOS)){
				if(getDeposito().getTc().doubleValue()<=1)
					support.addError("tc", "No existe tipo de cambio registrado en el sistema");
				return;
			}
		}
		
		public Deposito getDeposito(){
			return (Deposito)getBaseBean();
		}
		
		public EventList<Cuenta> getCuentas(){
			return cuentas;
		}
		
		
		



		private class MonedaSelector extends AbstractMatcherEditor<Cuenta> implements PropertyChangeListener{
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getNewValue()==null) fireMatchNone();
				final Currency mon=(Currency)evt.getNewValue();
				fireChanged(Matchers.beanPropertyMatcher(Cuenta.class, "moneda", mon));
			}
			
		}
		
	}
	
	public static void main(String[] args) {		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DepositoModel model=new DepositoModel();
				DepositoForm form=new DepositoForm(model);
				form.open();
				System.exit(0);
			}
		});
	}

}
