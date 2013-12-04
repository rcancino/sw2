package com.luxsoft.siipap.swing.binding;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.beans.PropertyAccessException;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;

import com.luxsoft.siipap.inventarios.model.Transformacion.Clase;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;

import com.luxsoft.siipap.model.gastos.ClasificacionDeActivo;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.model.gastos.TipoDeGasto;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Concepto.Tipo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.PlasticFieldCaret;

/**
 * Binding de uso comun
 * 
 * @author Ruben Cancino
 *
 */
public class Bindings {
	

	/**
	 * Regresa un Binding para el tipo de dato {@link CantidadMonetaria} 
	 * 
	 * @param vm
	 * @return
	 */
	public static JComponent createCantidadMonetariaBinding(final ValueModel vm){		
		CantidadMonetariaControl control=new CantidadMonetariaControl(vm);
		return control;
	}
	
	public static JComponent createCantidadMonetariaPesosBinding(final ValueModel vm){		
		CantidadMonetariaPesosControl control=new CantidadMonetariaPesosControl(vm);
		return control;
	}
	
	/**
	 * Binding para beans de tipo {@link Empresa}
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createEmpresaBinding(final ValueModel vm){		
		final List<Empresa> data=ServiceLocator2.getLookupManager().getEmpresas();
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		if(box.getItemCount()>0 && (vm.getValue()==null))
			box.setSelectedIndex(0);
		return box;
	}
	
	/**
	 * Binding para beans de tipo Sucursal 
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createSucursalesBinding(final ValueModel vm){		
		final List<Sucursal> data=ServiceLocator2.getLookupManager().getSucursales();
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);	
		if(box.getItemCount()>0 && (vm.getValue()==null))
			box.setSelectedIndex(0);
		return box;
	}
	
	/**
	 * Binding para beans de tipo Sucursal 
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createSucursalesOperativasBinding(final ValueModel vm){		
		final List<Sucursal> data=ServiceLocator2.getLookupManager().getSucursalesOperativas();
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);	
		if(box.getItemCount()>0 && (vm.getValue()==null))
			box.setSelectedIndex(0);
		return box;
	}
	
	/**
	 * Binding para beans de tipo Departamento 
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createDepartamentosBinding(final ValueModel vm){		
		final List<Departamento> data=ServiceLocator2.getLookupManager().getDepartamentos();
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);	
		if(box.getItemCount()>0 && (vm.getValue()==null))
			box.setSelectedIndex(0);
		return box;
	}
	
	
	
	/**
	 * Binding para beans de tipo {@link Concepto}
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createConceptosDeIngresosEgresosBinding(final ValueModel vm){
		final SelectionInList list=new SelectionInList(Concepto.Tipo.values(),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	/**
	 * Binding para los tipo de moneda soportados por el sistema
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createCurrencyBinding(final ValueModel vm){
		final Currency[] monedas={CantidadMonetaria.PESOS,CantidadMonetaria.DOLARES,CantidadMonetaria.EUROS};
		final SelectionInList list=new SelectionInList(monedas,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	/**
	 * Binding para bancos
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createBancosBinding(final ValueModel vm){		
		final List<Banco> data=ServiceLocator2.getLookupManager().getBancos();
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	public static  JComboBox createCuentasBinding(final ValueModel vm){		
		final List<Cuenta> data=ServiceLocator2.getLookupManager().getCuenta();
		Collections.sort(data, GlazedLists.beanPropertyComparator(Cuenta.class, "banco.nombre"));
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);
		return box;
	}
	
	/**
	 * Binding para Formas de pago
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createFormasDePagoBinding(final ValueModel vm){		
		final SelectionInList list=new SelectionInList(FormaDePago.values(),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	/**
	 * Binding para las formas de CargoAbono
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createFormasDeCargoAbonoBinding(final ValueModel vm){
		final SelectionInList list=new SelectionInList(FormaDePago.values(),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	/**
	 * Binding para beans {@link Concepto} de tipo ABONO
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createConceptosDeAbonoBinding(final ValueModel vm,final Tipo tipo,final Concepto.Clase clase){		
		List<Concepto> data=ServiceLocator2.getLookupManager().getConceptosDeIngreoEgreso();
		
		CollectionUtils.filter(data, new Predicate(){
			public boolean evaluate(Object object) {
				Concepto c=(Concepto)object;
				return tipo.equals(c.getTipo());
			}
			
		});
		Collection<Concepto> res=CollectionUtils.select(data, new Predicate() {
			public boolean evaluate(Object object) {
				Concepto c=(Concepto)object;
				return clase.equals(c.getClase());
			}
		});
		final SelectionInList list=new SelectionInList(res.toArray(new Concepto[0]),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	/**
	 * Crea un Binding en tre el {@link ValueModel} y el {@link JFormattedTextField}
	 * para un tipo de dato BigDecimal,usando como default 2 posiciones decimales
	 * 
	 * @param vm
	 * @return
	 */
	public static JFormattedTextField createBigDecimalBinding(final ValueModel vm){
		return createBigDecimalBinding(vm, 4,2);
	}
	
	/**
	 * Crea un Binding en tre el {@link ValueModel} y el {@link JFormattedTextField}
	 * 
	 * @param vm
	 * @param maxDecimal El numero maximo de decimales
	 * @param minDecimals El numero minimo de decimales
	 * @return
	 */
	public static JFormattedTextField createBigDecimalBinding(final ValueModel vm,final int maxDecimal,int minDecimals){
		final NumberFormat format=NumberFormat.getNumberInstance(Locale.US);
		format.setMaximumFractionDigits(maxDecimal);
		format.setGroupingUsed(false);
		format.setMinimumFractionDigits(minDecimals);
		final NumberFormatter formatter=new NumberFormatter(format);
		formatter.setValueClass(BigDecimal.class);
		formatter.setOverwriteMode(true);
		final JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(vm, formatter);
		tf.setHorizontalAlignment(JTextField.RIGHT);
		return tf;
	}
	
	/**
	 * Crea un Binding en tre el {@link ValueModel} y el {@link JFormattedTextField}
	 * para un tipo de dato BigDecimal,usando como default 2 posiciones decimales
	 * 
	 * @param vm
	 * @return
	 */
	public static JFormattedTextField createDoubleBinding(final ValueModel vm){
		return createDoubleBinding(vm, 2,2);
	}
	
	/**
	 * Crea un Binding en tre el {@link ValueModel} y el {@link JFormattedTextField}
	 * 
	 * @param vm
	 * @param maxDecimal El numero maximo de decimales
	 * @param minDecimals El numero minimo de decimales
	 * @return
	 */
	public static JFormattedTextField createDoubleBinding(final ValueModel vm,final int maxDecimal,int minDecimals){
		final NumberFormat format=NumberFormat.getNumberInstance(Locale.US);
		format.setMaximumFractionDigits(maxDecimal);
		format.setGroupingUsed(false);
		format.setMinimumFractionDigits(minDecimals);
		final NumberFormatter formatter=new NumberFormatter(format);
		formatter.setOverwriteMode(true);
		formatter.setValueClass(Double.class);
		final JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(vm, formatter);
		tf.setHorizontalAlignment(JTextField.RIGHT);
		tf.setCaret(new PlasticFieldCaret());
		return tf;
	}
	
	
	
	
	/** Bindings De Bienes/Servicios ************/
	
	/** 
	 * Regresa un combo box con las opciones existenes de {@link TipoDeGasto}
	 */
	public static  JComboBox createTipoDeGasto(final ValueModel vm){
		final SelectionInList list=new SelectionInList(TipoDeGasto.values(),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	/**
	 * Crea un binding para seleccionar isntancias de {@link ConceptoDeGasto}
	 * @param vm
	 * @return
	 */
	public static JComboBox createClasificacionesBinding(final ValueModel vm){
		final EventList<ConceptoDeGasto> source=GlazedLists.threadSafeList(new BasicEventList<ConceptoDeGasto>());
		source.addAll(ServiceLocator2.getLookupManager().getClasificaciones());
		Format format=new Format(){			
			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				if(obj!=null)
					toAppendTo.append(obj.toString());
				return toAppendTo;
			}
			public Object parseObject(String source, ParsePosition pos) {				
				return null;
			}
		};
		return Binder.createBindingBox(vm, source,GlazedLists.textFilterator(new String[]{"clave","descripcion","tipo"}),format);
	}
	
	/**
	 * Crea un binding para seleccionar isntancias de {@link GTipoProveedor} 
	 * @param vm
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JComboBox createTipoProveedorBinding(final ValueModel vm){
		final EventList<GTipoProveedor> source=GlazedLists.threadSafeList(new BasicEventList<GTipoProveedor>());
		source.addAll(ServiceLocator2.getUniversalDao().getAll(GTipoProveedor.class));
		return Binder.createBindingBox(vm, source,GlazedLists.textFilterator(new String[]{"clave","descripcion","tipo"}));
	}
	
	public static  JComboBox createUnidadesBinding(final ValueModel vm){
		final SelectionInList list=new SelectionInList(ServiceLocator2.getUniversalDao().getAll(com.luxsoft.siipap.model.core.Unidad.class),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	public static  JComboBox createTiposDeCompraBinding(final ValueModel vm){
		final SelectionInList list=new SelectionInList(TipoDeCompra.values(),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	@SuppressWarnings("unchecked")
	public static JComboBox createTipoTipoDeProveedorBinding(final ValueModel vm){
		/*
		SelectionInList list=new SelectionInList(ServiceLocator2.getUniversalDao().getAll(GTipoProveedor.class),vm);
		return BasicComponentFactory.createComboBox(list);
		*/
		
		
		final JComboBox box=new JComboBox();
		final BasicEventList list=new BasicEventList();
		list.addAll(ServiceLocator2.getUniversalDao().getAll(GTipoProveedor.class));
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		if(SwingUtilities.isEventDispatchThread()){
			AutoCompleteSupport support = AutoCompleteSupport.install(box, list, filterator);
	        support.setFilterMode(TextMatcherEditor.CONTAINS);
	        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
			model.addListDataListener(new WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
	        return box;
		}else{
			
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				    
					public void run() {
				        AutoCompleteSupport support = AutoCompleteSupport.install(box, list, filterator);
				        support.setFilterMode(TextMatcherEditor.CONTAINS);
				        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
						model.addListDataListener(new WeakListDataListener(vm));
						box.setSelectedItem(vm.getValue());
				    }
				});
			} catch (Exception ex){
				ex.printStackTrace();
				return box;
			}
		}
		
		
		
		return box;
		
		
	}
	
	/**
	 * Regresa un JComboBox con los beans de tipo {@link ConceptoDeGasto} exitentes
	 * 
	 * @param vm
	 * @return
	 
	@SuppressWarnings("unchecked")
	public static JComboBox createConceptosDeGastoBinding(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final BasicEventList list=new BasicEventList();
		list.addAll(ServiceLocator2.getUniversalDao().getAll(ConceptoDeGasto.class));
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		if(SwingUtilities.isEventDispatchThread()){
			AutoCompleteSupport support = AutoCompleteSupport.install(box, list, filterator);
	        support.setFilterMode(TextMatcherEditor.CONTAINS);
	        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
			model.addListDataListener(new WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
	        return box;
		}else{
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				    
					public void run() {
				        AutoCompleteSupport support = AutoCompleteSupport.install(box, list, filterator);
				        support.setFilterMode(TextMatcherEditor.CONTAINS);
				        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
						model.addListDataListener(new WeakListDataListener(vm));
						box.setSelectedItem(vm.getValue());
				    }
				});
			} catch (Exception ex){
				ex.printStackTrace();
				return box;
			}
		}
		
		
		
		return box;
		
		
	}
	*/
	/**
	 * Regresa un JComboBox con los beans de tipo {@link ConceptoDeGasto} exitentes
	 * 
	 * @param vm
	 * @return
	*/ 
	@SuppressWarnings("unchecked")
	public static JComboBox createConceptosDeGastoBinding(final ValueModel vm){
		final SelectionInList list=new SelectionInList(ServiceLocator2.getUniversalDao().getAll(ConceptoDeGasto.class),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	public static class WeakListDataListener implements ListDataListener{
		
		private final WeakReference<ValueModel> ref;
		
		 public WeakListDataListener(final ValueModel model){
			 ref=new WeakReference<ValueModel>(model);
		 }

		public void contentsChanged(ListDataEvent e) {
			if(ref.get()!=null){
				ComboBoxModel source=(ComboBoxModel)e.getSource();
				Object next=source.getSelectedItem();				
				try {
					ref.get().setValue(next);
				} catch (PropertyAccessException ex) {
					//OK
				}
			}
		}

		public void intervalAdded(ListDataEvent e) {}
		public void intervalRemoved(ListDataEvent e) {}		
	}
	
	
	/**
	 * REgresa un combo con los estados registrados
	 * 
	 * @param vm
	 * @return
	 */
	public static JComboBox createEstadosBinding(final ValueModel vm){		
		SelectionInList list=new SelectionInList(ServiceLocator2.getLookupManager().getEstados(),vm);
		return BasicComponentFactory.createComboBox(list);
	}
	
	/**
	 * Regresa un combo con las ciudades registradas
	 * 
	 * @param vm
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JComboBox createCiudadesBinding(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final BasicEventList list=new BasicEventList();
		list.addAll(ServiceLocator2.getLookupManager().getCiudades());		
		if(SwingUtilities.isEventDispatchThread()){
			AutoCompleteSupport support = AutoCompleteSupport.install(box, list);
	        support.setFilterMode(TextMatcherEditor.CONTAINS);
	        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
			model.addListDataListener(new WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
	        return box;
		}else{
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				    
					public void run() {
				        AutoCompleteSupport support = AutoCompleteSupport.install(box, list);
				        support.setFilterMode(TextMatcherEditor.CONTAINS);
				        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
						model.addListDataListener(new WeakListDataListener(vm));
						box.setSelectedItem(vm.getValue());
				    }
				});
			} catch (Exception ex){
				ex.printStackTrace();
				return box;
			}
		}
		return box;
		
	}
	
	/**
	 * Regresa un combo con las ciudades registradas
	 * 
	 * @param vm
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JComboBox createMunicipiosBinding(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final BasicEventList list=new BasicEventList();
		list.addAll(ServiceLocator2.getLookupManager().getCiudades());		
		if(SwingUtilities.isEventDispatchThread()){
			AutoCompleteSupport support = AutoCompleteSupport.install(box, list);
	        support.setFilterMode(TextMatcherEditor.CONTAINS);
	        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
			model.addListDataListener(new WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
	        return box;
		}else{
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				    
					public void run() {
				        AutoCompleteSupport support = AutoCompleteSupport.install(box, list);
				        support.setFilterMode(TextMatcherEditor.CONTAINS);
				        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
						model.addListDataListener(new WeakListDataListener(vm));
						box.setSelectedItem(vm.getValue());
				    }
				});
			} catch (Exception ex){
				ex.printStackTrace();
				return box;
			}
		}
		
		
		
		return box;
		
		
	}
	/*
	public static  JComboBox createClasificacionDeActivoFijoBinding(final ValueModel vm){		
		final List<ClasificacionDeActivo> data=ServiceLocator2.getUniversalDao().getAll(ClasificacionDeActivo.class);
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		//if(box.getItemCount()>0)
			//box.setSelectedIndex(0);
		return box;
	}
	*/
	public static  JComboBox createClasificacionDeActivoFijoBinding(final ValueModel vm){		
		final List<ConceptoDeGasto> data=ServiceLocator2.getHibernateTemplate().find("from ConceptoDeGasto g where g.parent.id=?",151479L );
		final SelectionInList list=new SelectionInList(data,vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		//if(box.getItemCount()>0)
			//box.setSelectedIndex(0);
		return box;
	}
	
	public static JFormattedTextField createRFCBinding(final ValueModel vm){
		MaskFormatter formatter=null;
		try {
			formatter=new MaskFormatter("UUU*-######-AAA");
			formatter.setValueContainsLiteralCharacters(false);
			//formatter.setPlaceholderCharacter('_');
			formatter.setAllowsInvalid(false);
			formatter.setCommitsOnValidEdit(false);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(vm, formatter);
		return tf;
	}
	
	@SuppressWarnings("unchecked")
	public static JComboBox createConceptoDeRequisicionDeTesoreria(final ValueModel vm){
		final List<Concepto> data=ServiceLocator2.getHibernateTemplate().find("from Concepto c where c.clase='TESORERIA2'");
		final SelectionInList list=new SelectionInList(data,vm);
		return BasicComponentFactory.createComboBox(list);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static JComboBox createAutoCompliteBinding(final ValueModel vm,final TextFilterator filterator,final List data){
		
		final JComboBox box=new JComboBox();
		final BasicEventList list=new BasicEventList();
		list.addAll(data);
		
		if(SwingUtilities.isEventDispatchThread()){
			AutoCompleteSupport support = AutoCompleteSupport.install(box, list, filterator);
	        support.setFilterMode(TextMatcherEditor.CONTAINS);
	        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
			model.addListDataListener(new WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
	        return box;
		}else{
			
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				    
					public void run() {
				        AutoCompleteSupport support = AutoCompleteSupport.install(box, list, filterator);
				        support.setFilterMode(TextMatcherEditor.CONTAINS);
				        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
						model.addListDataListener(new WeakListDataListener(vm));
						box.setSelectedItem(vm.getValue());
				    }
				});
			} catch (Exception ex){
				ex.printStackTrace();
				return box;
			}
		}
		return box;
		
	}
	

	/**
	 * Regresa un {@link JFormattedTextField} adecuado para capturar descuentos en base 0 a 100
	 * @param vm
	 * @return
	 */
	public static JFormattedTextField createDescuentoEstandarBinding(final ValueModel vm){
		
		final NumberFormat defFormat=NumberFormat.getNumberInstance();
		defFormat.setGroupingUsed(true);
		defFormat.setMaximumIntegerDigits(2);
		defFormat.setMaximumFractionDigits(4);
		final NumberFormatter defaultFormat=new NumberFormatter(defFormat);
		
		final DecimalFormat edFormat=(DecimalFormat)NumberFormat.getNumberInstance();
		edFormat.setGroupingUsed(false);
		edFormat.setMaximumIntegerDigits(2);
		edFormat.setMaximumFractionDigits(4);
		//edFormat.setMultiplier(10);
		//edFormat.setRoundingMode(RoundingMode.HALF_EVEN);
		
		final NumberFormatter editFormat=new NumberFormatter(edFormat);
		editFormat.setValueClass(Double.class);		
		editFormat.setMaximum(new Double(99.99));
		//editFormat.setAllowsInvalid(false);
		
		//editFormat.setMaximum(new Double(.0001));
		
		final DecimalFormat disFormat=(DecimalFormat)NumberFormat.getPercentInstance();
		disFormat.setMultiplier(1);
		//disFormat.setRoundingMode(RoundingMode.HALF_EVEN);
		disFormat.setMaximumIntegerDigits(2);
		disFormat.setMaximumFractionDigits(4);
		disFormat.setMinimumFractionDigits(2);
		final NumberFormatter displayFormat=new NumberFormatter(disFormat);
		
		
		final AbstractFormatterFactory factory=new DefaultFormatterFactory(defaultFormat,displayFormat,editFormat);
		JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(vm, factory);
		return tf;
	}
	
	/**
	 * Regresa un {@link JFormattedTextField} adecuado para capturar descuentos en base 0 a 1
	 * @param vm
	 * @return
	 */
	public static JFormattedTextField createDescuentoEstandarBindingBase1(final ValueModel vm){
		
		final NumberFormat defFormat=NumberFormat.getNumberInstance();
		defFormat.setGroupingUsed(true);
		defFormat.setMaximumIntegerDigits(2);
		defFormat.setMaximumFractionDigits(4);
		final NumberFormatter defaultFormat=new NumberFormatter(defFormat);
		
		final DecimalFormat edFormat=(DecimalFormat)NumberFormat.getNumberInstance();
		edFormat.setGroupingUsed(false);
		edFormat.setMaximumIntegerDigits(2);
		edFormat.setMaximumFractionDigits(4);
		edFormat.setMultiplier(100);
		
		
		final NumberFormatter editFormat=new NumberFormatter(edFormat);
		editFormat.setValueClass(Double.class);		
		
		
		final DecimalFormat disFormat=(DecimalFormat)NumberFormat.getPercentInstance();
		//disFormat.setMultiplier(10);
		//disFormat.setRoundingMode(RoundingMode.HALF_EVEN);
		disFormat.setMaximumIntegerDigits(2);
		disFormat.setMaximumFractionDigits(4);
		disFormat.setMinimumFractionDigits(2);
		final NumberFormatter displayFormat=new NumberFormatter(disFormat);
		
		
		final AbstractFormatterFactory factory=new DefaultFormatterFactory(defaultFormat,displayFormat,editFormat);
		JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(vm, factory);
		return tf;
	}

	/**
	 * Inventarios Regresa un combo box con las opciones existenes de la
	 * enumeracion cocepto
	 */
	public static JComboBox createMovimientosInv(final ValueModel vm) {
		final SelectionInList list = new SelectionInList(
				com.luxsoft.siipap.inventarios.model.Movimiento.Concepto
						.values(), vm);
		final JComboBox box = BasicComponentFactory.createComboBox(list);
		return box;
	}
	
	/**
	 * Binding para Clase
	 * 
	 * @param vm
	 * @return
	 */
	public static  JComboBox createClaseBinding(final ValueModel vm){
		final SelectionInList list=new SelectionInList(Clase.values(),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}

	public static JSpinner createDateSpinnerBinding(final ValueModel vm){
		JSpinner sp=new JSpinner();
		sp.setModel(SpinnerAdapterFactory.createDateAdapter(vm, new Date()));
		return sp;
	}

}
