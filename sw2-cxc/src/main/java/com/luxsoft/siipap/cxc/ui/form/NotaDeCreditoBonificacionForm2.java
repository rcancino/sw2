package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.cxc.model.AutorizacionesCxC;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.Concepto;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.ModeloDeCalculo;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeCXC;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.UpperCaseField;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Venta;

public class NotaDeCreditoBonificacionForm2 extends AbstractForm{

	public NotaDeCreditoBonificacionForm2(NotaDeCreditoBonificacionFormModel2 model) {
		super(model);
		model.getModel("modo").addValueChangeListener(new ModoHandler());
		setTitle("Nota de crédito por bonificación");
	}
	
	public NotaDeCreditoBonificacionFormModel2 getNotaBonificacionModel(){
		return (NotaDeCreditoBonificacionFormModel2)model;
	}
	
	public JComponent buildFormPanel(){
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildTolbar(),BorderLayout.NORTH);
		panel.add(buildDocPanel(),BorderLayout.CENTER);
		panel.add(buildTotalesPanel(),BorderLayout.EAST);
		panel.add(buildPartidasPanel(),BorderLayout.SOUTH);
		return panel;
	}
	
	
	protected JComponent buildDocPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,110dlu:g(.5), 2dlu," +
				"p,2dlu,110dlu:g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Generales");
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
		}		
		getControl("moneda").setEnabled(false);
		getControl("tc").setEnabled(false);
		nombreField=new JTextField(50);
		nombreField.setEnabled(false);
		builder.append("Nombre",nombreField,5);
		builder.append("Cliente",getControl("cliente"));
		builder.append("Fecha",getControl("fecha"));
		
		
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		builder.setDefaultDialogBorder();		
		builder.append("Comentario CxC",getControl("comentario2"),5);
		builder.append("Concepto",getControl("concepto"));		
		builder.append("Modelo ",getControl("modo"));
		getControl("impuesto").setEnabled(false);
		getControl("importe").setEnabled(false);
		getControl("total").setEnabled(false);
		builder.nextLine();		
		return builder.getPanel();		
	}
	
	protected JComponent buildPartidasPanel(){
		grid=ComponentUtils.getStandardTable();
		//grid.setColumnControlVisible(false);
		final SortedList sorted=new SortedList(getNotaBonificacionModel().getAplicaciones(),null);
		final EventTableModel tm=new EventTableModel(sorted,getNotaBonificacionModel().createTableformat());
		grid.setModel(tm);
		selectionModel =new EventSelectionModel(sorted);
		grid.setSelectionModel(selectionModel);
		JComponent c= ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(800,350));
		return c;
	}
	
	
	
	protected JComponent buildTotalesPanel(){
		final FormLayout layout=new FormLayout(
				"p,2dlu,80dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Importes");
		builder.append("Folio",getControl("folio"));
		builder.append("Descuento",getControl("descuento"));		
		builder.append("Importe",getControl("importe"));		
		builder.append("Impuesto",getControl("impuesto"));
		builder.append("Total",getControl("total"));
		builder.setDefaultDialogBorder();
		return builder.getPanel();
	}
	
	protected JComponent buildTolbar(){
		JToolBar bar=new JToolBar();
		bar.add(CommandUtils.createInsertAction(this, "insertar"));
		bar.add(CommandUtils.createDeleteAction(this, "eliminar"));
		bar.add(CommandUtils.createEditAction(this, "modificar"));
		bar.add(CommandUtils.createPrintAction(this, "imprimir"));
		Action buscarSelectiva=new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				getNotaBonificacionModel().asignarFacturaSelectiva();
			}			
		};
		buscarSelectiva.putValue(Action.NAME, "Buscar factura");
		buscarSelectiva.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/find.png"));
		bar.add(buscarSelectiva);
		return bar;
	}
	
	
	
	public void insertar(){
		List<Cargo> cuentas=SelectorDeCXC.seleccionar(getNotaBonificacionModel().getNota().getCliente(), origen);
		if(cuentas==null)
			return;
		List<Venta> ventas=new ArrayList<Venta>();
		for(Cargo c:cuentas){
			if(c instanceof Venta)
				ventas.add((Venta)c);
		}
		if(!ventas.isEmpty()){
			getNotaBonificacionModel().generarConceptos(ventas);
			grid.packAll();
		}
	}
	
	public void eliminar(){
		if(!selectionModel.isSelectionEmpty()){
			List data=new ArrayList(selectionModel.getSelected().size());
			data.addAll(selectionModel.getSelected());
			getNotaBonificacionModel().eliminarAplicaciones(data);
		}
	}
	
	public void modificar(){
		System.out.println("Modificando aplicaciones...");
	}
	public void imprimir(){
		System.out.println("Imprimir Nota");
	}
	
	protected JXTable grid;
	protected EventSelectionModel selectionModel;
	
	
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	
	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}
	
	
	
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("concepto".equals(property)){
			SelectionInList sl=new SelectionInList(Concepto.values(),model.getModel("concepto"));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("modo".equals(property)){
			SelectionInList sl=new SelectionInList(ModeloDeCalculo.values(),model.getModel("modo"));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("cliente".equals(property)){
			if(getNotaBonificacionModel().getNota().getCliente()==null){
				JComponent box=buildClienteControl();
				return box;
			}else{
				JTextField tf=new JTextField(20);
				tf.setText(getNotaBonificacionModel().getNota().getCliente().getNombreRazon());
				tf.setEnabled(false);
				return tf;
			}
		}else if("descuento".equals(property)){			
			return Bindings.createDescuentoEstandarBindingBase1(model.getModel(property));
		}else if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;
			
		}else if("sucursal".equals(property)){
			JComboBox box=Bindings.createSucursalesBinding(model.getModel(property));
			model.setValue("sucursal", ServiceLocator2.getConfiguracion().getSucursal());
			boolean val=!model.isReadOnly();
			box.setEditable(val);
			box.setFocusable(val);
			return box;
		}
		return null;
	}
	
	private JTextField claveField;
	private JTextField nombreField;
	
	private JComponent buildClienteControl(){
		FormLayout layout=new FormLayout("70dlu:g,2dlu,p,2dlu,p","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		JButton bt1=new JButton(getLookupAction());
		bt1.setFocusable(false);
		
		claveField=new UpperCaseField();
		claveField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getNotaBonificacionModel().asignarCliente(claveField.getText());
				nombreField.setText(getNotaBonificacionModel().getNota().getNombre());
			}
		});
		
		builder.append(claveField,bt1);
		return builder.getPanel();
	}
	
	
	
	private Action lookupAction;
	
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"seleccionarCliente");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
		}
		return lookupAction;
	}
	public void seleccionarCliente(){
		Cliente c=SelectorDeClientes.seleccionar(this);
		getNotaBonificacionModel().getNota().setCliente(c);
		nombreField.setText(c.getNombre());
	}
	
	
	@Override
	public void doAccept() {
		final AutorizacionDeAbono aut=AutorizacionesCxC.autorizacionDeNotaDeBonificacion();
		if(aut!=null){
			model.setValue("autorizacion", aut);
			super.doAccept();
		}
	}

	
	
	public static NotaDeCreditoBonificacion showForm(final NotaDeCreditoBonificacionFormModel2 model,OrigenDeOperacion origen){
		final NotaDeCreditoBonificacionForm2 form=new NotaDeCreditoBonificacionForm2(model);
		form.setOrigen(origen);
		form.open();
		if(!form.hasBeenCanceled()){
			model.commit();
			return (NotaDeCreditoBonificacion)model.getNota();
		}
		return null;
	}
	
	
	
	private class ModoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			ModeloDeCalculo m=getNotaBonificacionModel().getNotaBonificacion().getModo();
			getControl("total").setEnabled(m.equals(ModeloDeCalculo.PRORREATAR));
			getControl("descuento").setEnabled(!m.equals(ModeloDeCalculo.PRORREATAR));
		}		
	}
	
	public static NotaDeCredito showForm(final NotaDeCreditoFormModel model,OrigenDeOperacion origen){
		final NotaDeCreditoForm form=new NotaDeCreditoForm(model);
		form.setOrigen(origen);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getNota();
		}
		return null;
	}
	
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
 
			public void run() {
				SWExtUIManager.setup();
				NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
				NotaDeCreditoBonificacionFormModel2 model=new NotaDeCreditoBonificacionFormModel2(nota,false);						
				showForm(model,null);				
				showObject(model.getNota());
				System.exit(0);
				
			}
			
		});
		
	}

}
