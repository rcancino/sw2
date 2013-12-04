package com.luxsoft.siipap.cxp.ui.form;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class ContraRecibosForm  extends AbstractMasterDetailForm{

	public ContraRecibosForm(ContraRecibosFormModel model) {
		super(model);
	}
	
	public ContraRecibosFormModel getCrecibosModel(){
		return (ContraRecibosFormModel)getMainModel();
	}

	@Override
	protected JComponent buildMasterForm() {
		FormLayout layout=new FormLayout(
				"50dlu,2dlu,60dlu, 3dlu,50dlu,2dlu,f:p:g"			
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null){
			builder.append("Id",addReadOnly("id"),true);
		}
		builder.append("Fecha",getControl("fecha"),true);
		builder.append("Proveedor",getControl("proveedor"),5);
		builder.append("Comentario",getControl("comentario"),5);
		builder.append("Total",addReadOnly("total"),true);				
		return builder.getPanel();
	}
	
	protected int getToolbarType(){
		return JToolBar.HORIZONTAL;
	}
	
	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Contra Recibos","Registro de Contra Recibos");
	}
	
	protected void configDetailScrollPanel(final JScrollPane sp){
		sp.setPreferredSize(new Dimension(550,250));
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			JComponent control=buildProveedorControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return super.createCustomComponent(property);
	}
	
	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("proveedor") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2
					.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists
					.textFilterator(new String[] { "clave", "nombre", "rfc" });
			AutoCompleteSupport support = AutoCompleteSupport.install(box,
					source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			final EventComboBoxModel model = (EventComboBoxModel) box
					.getModel();
			model.addListDataListener(new Bindings.WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
			return box;
		} else {
			String prov = ((Proveedor) vm.getValue()).getNombreRazon();
			JLabel label = new JLabel(prov);
			return label;
		}
	} 

	@Override
	protected TableFormat getTableFormat() {
		String[] props={"documento","tipo","fecha","vencimiento","moneda","total"};
		String[] labels={"Documento","Tipo","Fecha","Vto","Mon","Total"};
		return GlazedLists.tableFormat(ContraReciboDet.class, props,labels);
	}

	protected void fixColumns(final TableColumnModel cm){
		if(model.getValue("id")!=null){
			grid.packAll();
		}else{
			cm.getColumn(0).setPreferredWidth(50);
			cm.getColumn(1).setPreferredWidth(350);
			cm.getColumn(2).setPreferredWidth(50);
		}
		
	}
	
	
	public Object doInsertPartida() {
		return ContraRecibosFormDet.showForm(new ContraReciboDet());
	}

	protected void doEdit(Object obj) {
		if(!model.isReadOnly()){
			ContraReciboDet det=(ContraReciboDet)obj;
			det=ContraRecibosFormDet.showForm(det);
			((EventTableModel)grid.getModel()).fireTableDataChanged();			
		}
	} 
	
	protected void doView(Object obj) {
		ContraReciboDet det=(ContraReciboDet)obj; 
		ContraRecibosFormDet.showForm(det,true);
	}

	public static ContraRecibo showForm(){
		return showForm(new ContraRecibo());
	}
	
	public static ContraRecibo showForm(ContraRecibo m){
		return showForm(m,false);
	}
	
	public static ContraRecibo showForm(ContraRecibo m,boolean readOnly){
		final ContraRecibosFormModel model=new ContraRecibosFormModel(m,readOnly);
		final ContraRecibosForm form=new ContraRecibosForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getRecibo();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				ContraRecibo m=showForm();
				if(m!=null){
					m=CXPServiceLocator.getInstance().getRecibosManager().save(m);
					ContraRecibosForm.showObject(m);
					
					
				}
				
			}
			
		});
		
		
	}
	
}
