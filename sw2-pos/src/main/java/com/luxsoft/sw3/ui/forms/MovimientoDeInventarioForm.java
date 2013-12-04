package com.luxsoft.sw3.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;

/**
 * Forma para el mantenimiento de inventario
 * 
 * @author Ruben Cancino
 *
 */
public class MovimientoDeInventarioForm extends AbstractForm implements ListSelectionListener{

	public MovimientoDeInventarioForm(MovimientoController controller) {
		super(controller);
		setTitle("Movimiento de Inventario   ("+controller.getValue("sucursal")+")");
		
	}
	
	public MovimientoController getController(){
		return (MovimientoController)getModel();
	}

	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Movimiento genérico de inventario","");
		}
		return header.getHeader();
	}

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,max(50dlu;p),3dlu," +
				"40dlu,2dlu,max(50dlu;p),3dlu," +
				"40dlu,2dlu,p:g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Tipo",getControl("concepto"));
		builder.nextLine();
		builder.append("Por inventario",getControl("porInventario"));
		builder.append("Comentario",getControl("comentario"),5);
		
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
	protected JComponent createCustomComponent(String property) {
		if("concepto".equals(property)){
			final SelectionInList sl=new SelectionInList(Movimiento.Concepto.values(),model.getModel(property));
			final JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}else if("fecha2".equals(property)){
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
			return c;
		}
		return null;
	}
	protected JPanel buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Agregar");
		getDeleteAction().putValue(Action.NAME, "Eliminar");
		getEditAction().putValue(Action.NAME, "Editar");		
		
		getViewAction().setEnabled(false);
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
				,new JButton(getEditAction())
				,new JButton(getImprimirAction())
				//,new JButton(getAutorizarAction())
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
	
	private JTable grid;
	
	private EventSelectionModel<MovimientoDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={"clave","descripcion","unidad.nombre","cantidad","comentario","tipoCis"};
		String[] columnLabels={"Producto","Descripcion","Unidad","Cantidad","Comentario","CIS"};
		final TableFormat tf=GlazedLists.tableFormat(MovimientoDet.class,propertyNames, columnLabels);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=new JTable(tm);
		selectionModel=new EventSelectionModel<MovimientoDet>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		
		gridComponent.setPreferredSize(new Dimension(750,300));
		return gridComponent;
		
	}
	
	public void insertPartida(){
		getController().insertarPartida();
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
	}
	
	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().eliminarPartida(index);
			}
		}
	}
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().edit(index);
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
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				Movimiento target=new Movimiento();
				MovimientoController controller=new MovimientoController(target);
				MovimientoDeInventarioForm form=new MovimientoDeInventarioForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					controller.persist();
					showObject(controller.getBaseBean());
					
				}
				System.exit(0);
			}

		});
	}

}
