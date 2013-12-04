package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAplicacionCxC;
import com.luxsoft.siipap.cxc.model.AutorizacionesCxC;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.model.AplicacionDePagoModel;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeCXC;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * Forma para la aplicacion general de pagos
 * 
 * @author Ruben Cancino
 *
 */
public class AplicacionDePagoForm extends AbstractForm{
	
	private OrigenDeOperacion origen=null;
	
	public AplicacionDePagoForm(final AplicacionDePagoModel model){
		super(model);
		setTitle("Aplicación de pagos");
	}
	
	public AplicacionDePagoForm(final AplicacionDePagoModel model,OrigenDeOperacion origen){
		super(model);
		setTitle("Aplicación de pagos");
		this.origen=origen;
	}
	
	public AplicacionDePagoModel getAplicacionModel(){
		return (AplicacionDePagoModel)super.getModel();
	}
	

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new BorderLayout(2,10));
		panel.add(buildForm(),BorderLayout.CENTER);
		panel.add(buildGrid(),BorderLayout.SOUTH);
		panel.add(buildTolbar(),BorderLayout.NORTH);
		return panel;
	}

	
	private JComboBox clienteBox;
	private JComboBox disponiblesBox;
	
	protected JComponent buildForm() {
		final FormLayout layout=new FormLayout("p,3dlu,150dlu,4dlu,p,3dlu,f:150dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cliente", getControl("cliente"),5);
		builder.append("Abono", getControl("abono"),5);
		builder.append("Fecha",getControl("fecha"),true);
		builder.append("Disponible",getControl("disponible"));
		builder.append("Por Aplicar",getControl("total"));
		
		builder.setDefaultDialogBorder();
		return builder.getPanel();
	}
	
	
	
	@Override
	public void doAccept() {
		Abono abono=getAplicacionModel().getCurrentAbono();
		if(AutorizacionesCxC.requiereAutorizacion(abono)){
			AutorizacionDeAplicacionCxC aut=AutorizacionesCxC.autorizacionParaAplicacionDeAbono();
			if(aut!=null){
				getAplicacionModel().setAutorizacion(aut);
				super.doAccept();
			}
			else
				doCancel();
		}
		super.doAccept();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("cliente".equals(property)){
			clienteBox=createClienteBox(model.getModel("cliente"));
			handler=new ClienteHandler();
	        clienteBox.getEditor().getEditorComponent().addFocusListener(handler);
			clienteBox.setEnabled(!model.isReadOnly());
			return clienteBox;
		}else if("abono".equals(property)){
			final EventListModel sm=new EventListModel(getAplicacionModel().getDisponibles());
			final SelectionInList sl=new SelectionInList(sm,model.getModel(property));
			disponiblesBox=BasicComponentFactory.createComboBox(sl);
			disponiblesBox.setEnabled(!model.isReadOnly());
			return disponiblesBox;
			
		}else if("disponible".equals(property)||"total".equals(property)){
			JTextField c=Binder.createBigDecimalMonetaryBinding(model.getModel(property));
			c.setEditable(false);
			c.setFocusable(false);
			c.setHorizontalAlignment(JTextField.RIGHT);
			return c;
		}
		return super.createCustomComponent(property);
	}


	protected JXTable grid;
	protected EventSelectionModel selectionModel;
	
	protected JComponent buildGrid(){
		grid=ComponentUtils.getStandardTable();
		final SortedList sortedSource=new SortedList(getAplicacionModel().getAplicaciones(),null);
		final EventTableModel tm=new EventTableModel(sortedSource,getAplicacionModel().createTableFormat());
		selectionModel=new EventSelectionModel(sortedSource);
		selectionModel.addListSelectionListener(new SelectionHandler());
		grid.setModel(tm);		
		grid.setSelectionModel(selectionModel);
		//grid.setColumnControlVisible(false);
		ComponentUtils.decorateActions(grid);
		JComponent res=ComponentUtils.createTablePanel(grid);
		res.setPreferredSize(new Dimension(780,450));
		return res;
	}
	
	protected JComponent buildTolbar(){
		JToolBar bar=new JToolBar();
		bar.add(CommandUtils.createInsertAction(this, "insertar"));
		bar.add(CommandUtils.createDeleteAction(this, "eliminar"));		
		bar.add(CommandUtils.createPrintAction(this, "imprimir"));
		return bar;
	}
	
	public void insertar(){
		if(getAplicacionModel().getCurrentAbono()!=null){
			List<Cargo> cargos=SelectorDeCXC.seleccionar(getAplicacionModel().getCurrentCliente(), origen);
			if( (cargos!=null) && (!cargos.isEmpty())){
				getAplicacionModel().procesarCargos(cargos);
				grid.packAll();
			}
		}
	}
	
	public void eliminar(){
		if(!selectionModel.isSelectionEmpty()){
			List<Aplicacion> selected=new ArrayList<Aplicacion>(selectionModel.getSelected());
			for(Aplicacion a:selected){
				getAplicacionModel().eliminar(a);
			}
		}
	}
	
	protected JComboBox createClienteBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(CXCUIServiceFacade.getClientes());
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","nombre","rfc"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        //support.setStrict(true);
        support.setSelectsTextOnFocusGain(true);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        model.setSelectedItem(vm.getValue());
		return box;
	}
	
	public void close(){
		clienteBox.getEditor().getEditorComponent().removeFocusListener(handler);
		super.close();
	}
	
	protected void doSelect(Object selected){
		
	}

	private class SelectionHandler implements ListSelectionListener{

		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()){
				if(!selectionModel.isSelectionEmpty()){
					doSelect(selectionModel.getSelected().get(0));
				}
			}			
		}		
	}
	
	private ClienteHandler handler;
	
	/**
	 * Manda actualizar la lista de pagos disponibles 
	 * Actualmente este es el mejor lugar para invocar este metodo
	 * es el que menos llamadas hace, pero definitivamente no parece
	 * ser el mejor
	 * @author ruben
	 *
	 */
	private class ClienteHandler extends FocusAdapter{
		@Override
		public void focusLost(FocusEvent e) {			
			getAplicacionModel().actualizarPagosDisponibles();
		}		
	}
	
	/**
	 * 
	 * 
	 */
	public static Abono  showForm(){
		final AplicacionDePagoModel model=new AplicacionDePagoModel();		
		final AplicacionDePagoForm form=new AplicacionDePagoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.procesar();
			
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				Abono res=showForm();
				System.out.println("Abono: "+res);
				//ServiceLocator2.getCXCManager().salvarAbono(res);
				System.exit(0);
			}
			
		});
	}

}
