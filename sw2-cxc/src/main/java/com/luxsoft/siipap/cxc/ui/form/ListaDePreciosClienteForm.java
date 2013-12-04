package com.luxsoft.siipap.cxc.ui.form;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.hibernate.validator.InvalidValue;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.ValidationUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;

public class ListaDePreciosClienteForm extends AbstractMasterDetailForm{
	
	private JComponent box;
	
	public ListaDePreciosClienteForm(ListaDePreciosClienteFormModel model) {
		super(model);
		setTitle("Lista de Precios Clientes Cargo");
		model.getModel("cliente").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent arg0) {
				System.out.println(arg0.getNewValue());
			}			
		});
	}
	

	
	public ListaDePreciosClienteFormModel  getClienteModel(){
		return(ListaDePreciosClienteFormModel)getMainModel();
	}
	
	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Lista de Precios de Cliente Credito","Generación y mantenimiento de lista de precios ");
	}
	
	protected int getToolbarType(){
		return JToolBar.HORIZONTAL;
	}
	

	


	@Override
	protected JComponent buildMasterForm() {
		final DefaultFormBuilder builder=getDefaultMasterFormBuilder();
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("cliente").setEnabled(false);
		}
		builder.append("Cliente",getControl("cliente"),9);
		builder.nextLine();		
		builder.append("Fecha Inicial",getControl("fechaInicial"));
		builder.append("Fecha Final",getControl("fechaFinal"),true);
		builder.append("Comentario",getControl("comentario"),9);
		builder.append("Vigente",getControl("activo"),true);
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
	if("cliente".equals(property)){
			box=createClienteBox(model.getModel("cliente"));
			return box;
		}
		
		
		return null;
	}
	
	protected JComboBox createClienteBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(ServiceLocator2.getClienteManager().getAll());
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","nombre","rfc"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
		return box;
	}


	@Override
	protected TableFormat getTableFormat() {
		final String[] props={"producto.clave","producto.descripcion","precioDeLista","descuento","precio","moneda","precioKilo"};
		final String[] names={"Producto","Descripción","PrecioLista","Descuento","Precio","Moneda","PrecioKilo"};		
		return GlazedLists.tableFormat(ListaDePreciosClienteDet.class, props, names);
		
	}
	
	
	public void insertPartida(){
		ListaDePreciosClienteDet det=new ListaDePreciosClienteDet();		
		det=ListaDePreciosClienteDetForm.showForm(det);
		if(det!=null){
			InvalidValue[] vals=ValidationUtils.validate(det);
			for(InvalidValue iv:vals){
				JOptionPane.showMessageDialog(this, iv.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				
			}
			if(vals.length>0)
				return;
			if(det!=null){
				getMainModel().insertDetalle(det);			
			}
		}
		
	}
	
	protected void doEdit(Object obj){
		ListaDePreciosClienteDet source=(ListaDePreciosClienteDet)obj;
		ListaDePreciosClienteDet target=new ListaDePreciosClienteDet();
		BeanUtils.copyProperties(source, target);
		target=ListaDePreciosClienteDetForm.showForm(target);
		if(target!=null){
			BeanUtils.copyProperties(target, source);
		}
	}
	
	@Override
	protected Action[] getDetallesActions() {
		Action bulkUpdate=new AbstractAction("Bulk"){
			public void actionPerformed(ActionEvent e) {
				bulkEdit();
			}
		};
		bulkUpdate.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/application_cascade.png"));
		
		Action bulkInsert=new AbstractAction("BulkInsert"){
			public void actionPerformed(ActionEvent e) {
				bulkInsert();
			}
		};
		bulkInsert.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/text_indent_remove.png"));
		
		return new Action[]{
				getInsertAction(),getDeleteAction(),getEditAction(),getViewAction()
				,bulkInsert,bulkUpdate};
	}
	
	public void bulkEdit(){
		if(selection.isSelectionEmpty()) return;
		ListaDePreciosClienteDet dummy=(ListaDePreciosClienteDet)selection.getSelected().get(0);
		ListaDePreciosClienteDet template=new ListaDePreciosClienteDet();
		template.setProducto(dummy.getProducto());
		template.setPrecio(dummy.getPrecio());
		template=ListaDePreciosDetBulkForm.showForm(template);
		if(template!=null){
			List<ListaDePreciosClienteDet> selected=new ArrayList<ListaDePreciosClienteDet>();
			selected.addAll(selection.getSelected());
			for(ListaDePreciosClienteDet det:selected){
				det.setDescuento(template.getDescuento());
			}
		}
	}
	
	public void bulkInsert(){
		List<Producto> list=ProductoFinder.findWithDialog();
		for(Producto p:list){
			ListaDePreciosClienteDet det=new ListaDePreciosClienteDet();
			det.setProducto(p);
			getMainModel().insertDetalle(det);
		}
	}
	
	protected void doView(Object obj){
		ListaDePreciosClienteDet det=(ListaDePreciosClienteDet)obj;		
		ListaDePreciosClienteDetForm.showForm(det);
	}
	
	protected void doPartidaUpdated(ListEvent listChanges){
		
	}
	
	public static ListaDePreciosCliente showForm(){
		return showForm(new ListaDePreciosCliente());
	}
	
	public static ListaDePreciosCliente showForm(ListaDePreciosCliente bean){
		return showForm(bean,false);
	}
	
	public static ListaDePreciosCliente showForm(ListaDePreciosCliente bean,boolean readOnly){
		ListaDePreciosClienteFormModel model=new ListaDePreciosClienteFormModel(bean);
		model.setReadOnly(readOnly);
		final ListaDePreciosClienteForm form=new ListaDePreciosClienteForm(model);
		form.enableEditingActions(readOnly);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getLista();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				ListaDePreciosCliente lista=new ListaDePreciosCliente();
				
				ListaDePreciosCliente bean=showForm(lista);
				if(bean!=null){
					ListaDePreciosClienteForm.showObject(bean);
				}
				System.exit(0);
				
			}
			
		});

	}	

}
