package com.luxsoft.siipap.inventario.ui.task;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Forma generica para la ejecucion de tareas relacionadas con el costeo de inventarios
 * Permite seleccionar un producto o todos, asi como el año y mes
 * 
 * Adicionalmente mantiene un map con valores adecuados para la mayoria de los reportes
 * 
 * @author Ruben Cancino
 *
 */
public  class CostosTaskForm extends SXAbstractDialog{
	
	
	private JComponent yearComponent;
	
	private JComponent mesComponent;
	
	private JComboBox productoControl;
	
	private JComboBox sucursalBox;
	
	private JCheckBox todasLasSucursales;
	
	
	private JCheckBox todosBox;
	

	public CostosTaskForm() {
		super("Costeo de inventarios");
	}
	
	private void initComponents(){		
		final Date fecha=new Date();
		yearHolder=new ValueHolder(Periodo.obtenerYear(fecha));
		mesHolder=new ValueHolder(Periodo.obtenerMes(fecha));
		sucursalHolder=new ValueHolder();
		yearComponent=Binder.createYearBinding(yearHolder);
		mesComponent=Binder.createMesBinding(mesHolder);
		productoControl=buildProveedorControl();
		sucursalBox=Bindings.createSucursalesBinding(sucursalHolder);
		sucursalBox.setEnabled(false);
		todasLasSucursales=new JCheckBox("Todas",true);
		todasLasSucursales.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				sucursalBox.setEnabled(!todasLasSucursales.isSelected());
				
			}
		});
	}

	@Override
	protected JComponent buildContent() {
		initComponents();
		JPanel panel=new JPanel(new BorderLayout());			
		final FormLayout layout=new FormLayout(
				"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g,2dlu,p",
				"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",productoControl,5);
		builder.append(todosBox);
		builder.append("Sucursal",sucursalBox,todasLasSucursales);
		builder.nextLine();
		builder.append("Año",yearComponent);
		builder.append("Mes",mesComponent);
		
		panel.add(builder.getPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JComboBox buildProveedorControl() {
		todosBox=new JCheckBox("Todos",true);
		todosBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				productoControl.setEnabled(!todosBox.isSelected());
				if(!todosBox.isSelected()){
					validarArticuloSeleccionado();
				}
			}
		});
		getOKAction().setEnabled(true);
		final JComboBox box = new JComboBox();
		final EventList source = GlazedLists.eventList(ServiceLocator2.getProductoManager().buscarProductosActivos());
		final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave" ,"descripcion"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
		support.setFilterMode(TextMatcherEditor.CONTAINS);
		support.setCorrectsCase(true);
		box.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {					
				validarArticuloSeleccionado();
			}
			
		});
		box.setEnabled(!todosBox.isSelected());
		return box;
	}
	
	private void validarArticuloSeleccionado(){
		Object selected=productoControl.getSelectedItem();			
		if((selected!=null) && (selected instanceof Producto)) 
			getOKAction().setEnabled(true);
		else
			getOKAction().setEnabled(false);			
	}
	
	public String getProductoClave(){
		if(todosBox.isSelected())
			return "%";
		Object selected=productoControl.getSelectedItem();
		if(selected!=null)
			return ((Producto)selected).getClave();
		return "";
	}
	
	
	private ValueHolder yearHolder;
	private ValueHolder mesHolder;
	private ValueHolder sucursalHolder;
	
	public int getYear(){
		return (Integer)yearHolder.getValue();
	}
	public int getMes(){
		return (Integer)mesHolder.getValue();
	}
	
	public Sucursal getSucursal(){
		return (Sucursal)sucursalHolder.getValue();
	}
	
	@Override
	public void doApply() {			
		super.doApply();
		aplicarParamtros();
	}
	
	/**
	 * Template method para ajustar los parametros
	 */
	protected void aplicarParamtros(){
		int mes=getMes();
		int year=getYear();
		
		int mes_ini=mes;
		int year_ini=year;
		
		if(mes==1){
			mes_ini=12;
			year_ini=year-1;
		}else{
			mes_ini=mes-1;
		}
		
		parametros.put("AÑO", (long)year);
		parametros.put("MES", (long)mes);
		parametros.put("MES_INI", (long)mes_ini);
		parametros.put("AÑO_INI", (long)year_ini);			
		parametros.put("ARTICULOS", getProductoClave());
		if(todasLasSucursales.isSelected()){
			parametros.put("SUCURSAL", "%");
		}else
			parametros.put("SUCURSAL", getSucursal().getId().toString());
	}
	
	private final Map<String, Object> parametros=new HashMap<String, Object>();

	public Map<String, Object> getParametros() {
		return parametros;
	}
	
	
	
	public JComponent getYearComponent() {
		return yearComponent;
	}

	public void setYearComponent(JComponent yearComponent) {
		this.yearComponent = yearComponent;
	}

	public JComponent getMesComponent() {
		return mesComponent;
	}

	public void setMesComponent(JComponent mesComponent) {
		this.mesComponent = mesComponent;
	}

	public JComboBox getProductoControl() {
		return productoControl;
	}

	public void setProductoControl(JComboBox productoControl) {
		this.productoControl = productoControl;
	}

	public JComboBox getSucursalBox() {
		return sucursalBox;
	}

	public void setSucursalBox(JComboBox sucursalBox) {
		this.sucursalBox = sucursalBox;
	}

	public JCheckBox getTodasLasSucursales() {
		return todasLasSucursales;
	}

	public void setTodasLasSucursales(JCheckBox todasLasSucursales) {
		this.todasLasSucursales = todasLasSucursales;
	}

	public JCheckBox getTodosBox() {
		return todosBox;
	}

	public void setTodosBox(JCheckBox todosBox) {
		this.todosBox = todosBox;
	}

	public ValueHolder getYearHolder() {
		return yearHolder;
	}

	public void setYearHolder(ValueHolder yearHolder) {
		this.yearHolder = yearHolder;
	}

	public ValueHolder getMesHolder() {
		return mesHolder;
	}

	public void setMesHolder(ValueHolder mesHolder) {
		this.mesHolder = mesHolder;
	}

	public ValueHolder getSucursalHolder() {
		return sucursalHolder;
	}

	public void setSucursalHolder(ValueHolder sucursalHolder) {
		this.sucursalHolder = sucursalHolder;
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){

			public void run() {
				CostosTaskForm form=new CostosTaskForm();
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println("Year: "+form.getYear()+ "Mes: "+form.getMes()+" Producto:"+form.getProductoClave()+ " Sucursal: "+form.getSucursal());
					System.out.println(form.getParametros());
				}
				
			}
			
		});
	}
}