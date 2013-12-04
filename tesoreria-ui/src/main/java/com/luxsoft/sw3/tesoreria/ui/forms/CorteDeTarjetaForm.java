package com.luxsoft.sw3.tesoreria.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjetaDet;



/**
 * Forma para la generación y mantenimiento de cortes de tarjeta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CorteDeTarjetaForm extends AbstractForm implements ListSelectionListener{
	

	public CorteDeTarjetaForm(final CorteDeTarjetaFormModel model) {
		super(model);
		setTitle("Corte de Tarjeta     ");		
	}
	
	public CorteDeTarjetaFormModel getController(){
		return (CorteDeTarjetaFormModel)getModel();
	}

	
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.5),3dlu," +
				"p,2dlu,p:g(.5)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null)
			builder.append("Id",addReadOnly("id"));
		builder.append("Sucursal",getControl("sucursal"));
		builder.nextLine();
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Corte",getControl("corte"));
		
		builder.nextLine();
		builder.append("Tipo",getControl("tipoDeTarjeta"));
		builder.append("Cuenta",getControl("cuenta"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		builder.append("Total",addReadOnly("total"),true);
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		
		
		ajustarActions(panel);
		return panel;
	}
	
	protected void ajustarActions(JPanel panel){
		getOKAction().putValue(Action.NAME, "Salvar [F10]");
		getOKAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/edit/save_edit.gif"));
		getCancelAction().putValue(Action.NAME, "Cancelar");
		ComponentUtils.addAction(panel, new AbstractAction(){			
			public void actionPerformed(ActionEvent e) {
				if(getOKAction().isEnabled())
					getOKAction().actionPerformed(null);
			}
		}, 
		KeyStroke.getKeyStroke("F10"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		ComponentUtils.addInsertAction(panel, getInsertAction());
	}
	
	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		if(model.getValue("id")==null)
			getControl("sucursal").requestFocusInWindow();
		
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("sucursal".equals(property)){
			return buildSucursalControl(model.getModel(property));
		}else if("tipoDeTarjeta".equals(property)){
			SelectionInList sl=new SelectionInList(CorteDeTarjeta.TIPOS_DE_TARJETAS,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		} else if ("cuenta".equals(property)) {
			String hql = "from Cuenta c where c.activoEnVentas=true";
			List data = ServiceLocator2.getHibernateTemplate().find(hql);
			SelectionInList sl = new SelectionInList(data, model
					.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		} 
		return null;
	}
	
	private JComponent buildSucursalControl(final ValueModel vm) {
		if (model.getValue("id") == null) {
			final JComboBox box = new JComboBox();			
			EventList source =null;
			source=GlazedLists.eventList(ServiceLocator2.getLookupManager().getSucursalesOperativas());
			final TextFilterator filterator = GlazedLists.textFilterator(
					new String[] { "id","nombre" });
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
			model.addListDataListener(new Bindings.WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
			return box;
		} else {
			String prov = ((Sucursal) vm.getValue()).getNombre();
			JLabel label = new JLabel(prov);
			return label;
		}
	}
	
	protected JPanel buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Insertar [INS]");
		getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");
		
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().setEnabled(!model.isReadOnly());
		getEditAction().setEnabled(!model.isReadOnly());
		
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
				//,new JButton(getEditAction())
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JXTable grid;
	
	private EventSelectionModel<CorteDeTarjetaDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] props={"pago.nombre","pago.fecha","pago.tarjeta.nombre","pago.tarjeta.debito","pago.total"};
		String[] names={"Nombre","Fecha","Tarjeta","Débito","Total"};
		final TableFormat tf=GlazedLists.tableFormat(CorteDeTarjetaDet.class,props, names);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<CorteDeTarjetaDet>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);		
		gridComponent.setPreferredSize(new Dimension(790,300));
		grid.packAll();
		return gridComponent;
		
	}
	
	public void insertPartida(){
		getController().insertar();
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
		grid.packAll();
	}
	
	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().elminarPartida(index);
			}
		}
	}
	
	
	
	public void valueChanged(ListSelectionEvent e) {
		boolean val=!selectionModel.isSelectionEmpty();
		if(model.isReadOnly()){
			val=false;
		}
		//getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selectionModel.isSelectionEmpty());
	}
	

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				CorteDeTarjetaFormModel controller=new CorteDeTarjetaFormModel();
				CorteDeTarjetaForm form=new CorteDeTarjetaForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					CorteDeTarjeta res=controller.commit();
					res=ServiceLocator2.getIngresosManager().registrarCorte(res);
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
				}
				System.exit(0);
			}

		});
	}

	
	

}
