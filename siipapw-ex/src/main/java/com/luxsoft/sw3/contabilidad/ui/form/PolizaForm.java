package com.luxsoft.sw3.contabilidad.ui.form;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Forma para el mantenimiento de polizas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaForm extends AbstractForm implements ListSelectionListener{
	
	

	public PolizaForm(final PolizaFormModel model) {
		super(model);
		setTitle("Mantenimiento de Póliza      ");
	}
	
	public PolizaFormModel getController(){
		return (PolizaFormModel)getModel();
	}
	

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.3),3dlu," +
				"p,2dlu,p:g(.3),3dlu," +
				"p,2dlu,p:g(.4)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null){
			builder.append("Id",addReadOnly("id"));
			builder.append("Folio",addReadOnly("folio"));
			builder.nextLine();
		}
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Clase",addReadOnly("clase"));
		builder.nextLine();
		builder.append("Tipo",getControl("tipo"));
		builder.append("Referencia",getControl("referencia"));
		builder.nextLine();
		builder.append("Descripción",getControl("descripcion"),9);
		builder.append("Debe",addReadOnly("debe"));
		builder.append("Haber",addReadOnly("haber"));
		builder.append("Cuadre",addReadOnly("cuadre"));
		
		/*
		ajustarActions(panel);
		ComponentUtils.decorateSpecialFocusTraversal(panel);
		ComponentUtils.decorateTabFocusTraversal(getControl("proveedor"));
		*/
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		
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
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("tipo".equals(property)){
			SelectionInList sl=new SelectionInList(Poliza.Tipo.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			box.setEnabled(model.getValue("id")==null);
			return box;
		}else if("descripcion".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}else if("referencia".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}
		return null;
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
		
		getViewAction().setEnabled(false);
		
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
				,new JButton(getEditAction())
				//,new JButton(getImprimirAction())
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	private Action imprimirAction;
	
	public Action getImprimirAction(){
		if(imprimirAction==null){
			imprimirAction=CommandUtils.createPrintAction(this, "imprimir");
			imprimirAction.putValue(Action.NAME, "Imprimir [F12]");
		}
		return imprimirAction;
	}
	
	private JXTable grid;
	
	private EventSelectionModel<PolizaDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] props={"cuenta.clave","concepto","descripcion2","referencia","referencia2","debe","haber","asiento","tipo"};
		String[] names={"Cuenta","Concepto","Desc 2","Ref","Ref 2","Debe","Haber","Asiento","Tipo"};
		
		final TableFormat tf=GlazedLists.tableFormat(PolizaDet.class,props, names);
		//SortedList sorted=new SortedList(getController().getPartidasSource(),null);
		EventList sorted=getController().getPartidasSource();
		final EventTableModel tm=new EventTableModel(sorted,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<PolizaDet>(sorted);
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		//TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					view();
			}			
		});
		//grid.setEnabled(!model.isReadOnly());
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
			PolizaDet det=selectionModel.getSelected().get(0);
			getController().elminarPartida(det);
		}
	}
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().editar(index);
			}
		}
	}
	
	public void view(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().view(index);
			}
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		boolean val=!selectionModel.isSelectionEmpty();
		if(model.isReadOnly()){
			val=false;
		}
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selectionModel.isSelectionEmpty());
		
	}
	
	


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				PolizaFormModel controller=new PolizaFormModel();
				controller.getPoliza().setClase("GENERICA");
				PolizaForm form=new PolizaForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}
	

}
