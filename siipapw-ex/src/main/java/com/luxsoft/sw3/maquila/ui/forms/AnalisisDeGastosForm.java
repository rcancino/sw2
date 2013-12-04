package com.luxsoft.sw3.maquila.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;



/**
 * Forma para la mantenimiento de gastos de flete y hojeo en Entradas
 * de maquila al Almacen de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeGastosForm extends AbstractForm implements ListSelectionListener{
	
	

	public AnalisisDeGastosForm(final AnalisisDeGastosFormModel model) {
		super(model);
		setTitle("Análisis de gastos");
	}
	
	public AnalisisDeGastosFormModel getController(){
		return (AnalisisDeGastosFormModel)getModel();
	}	

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,80dlu,3dlu," +
				"p,2dlu,80dlu,3dlu," +
				"p,2dlu,80dlu"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null)
			builder.append("Id",addReadOnly("id"),true);
		
		builder.appendSeparator("Flete");
		builder.append("Factura",getControl("facturaFlete"));
		builder.append("Fecha",getControl("fechaDoctoFlete"));
		builder.nextLine();
		
		builder.append("Importe",getControl("importeFlete"));
		builder.append("Impuesto",getControl("impuestoFlete"));
		builder.append("Total",getControl("totalFlete"));
		builder.nextLine();
		
		builder.appendSeparator("Hojeo");
		builder.append("Factura",getControl("facturaMaquilador"));
		builder.append("Fecha",getControl("fechaDoctoMaquila"));
		builder.nextLine();
		
		builder.append("Importe",getControl("importeMaquilador"));
		builder.append("Impuesto",getControl("impuestoMaquilador"));
		builder.append("Total",getControl("totalMaquilador"));
		builder.nextLine();
		
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		ajustarActions(panel);
		//ComponentUtils.decorateSpecialFocusTraversal(panel);
		//ComponentUtils.decorateTabFocusTraversal(getControl("almacen"));
		JFormattedTextField t1=(JFormattedTextField)getControl("importeMaquilador");
		t1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getController().actualizarTotalMaquilaConImporte();
			}
		});
		
		JFormattedTextField t2=(JFormattedTextField)getControl("totalFlete");
		t2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getController().actualizarTotalMaquilaConTotal();
			}
		});
		
		JFormattedTextField t3=(JFormattedTextField)getControl("importeFlete");
		t3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getController().actualizarTotalFleteConImporte();
			}
		});
		
		JFormattedTextField t4=(JFormattedTextField)getControl("totalMaquilador");
		t4.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getController().actualizarTotalFleteConTotal();
			}
		});
		
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
		//getControl("factura").requestFocusInWindow();
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}return null;
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
				//,new JButton(getEditAction())
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JXTable grid;
	
	private EventSelectionModel<EntradaDeMaquila> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] props={"sucursal.nombre","documento","clave","descripcion","cantidad","kilosCalculados","costoFlete","costoCorte","costoMateria","costo","comentario"};
		String[] names={"Sucursal","Docto","Producto","Descripción","Cantidad","Kilos","Flete","Hojeo","Costo M.P.","Costo","Comentario"};
		 
		final TableFormat tf=GlazedLists.tableFormat(EntradaDeMaquila.class,props,names);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<EntradaDeMaquila>(getController().getPartidasSource());
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
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selectionModel.isSelectionEmpty());
		
	}
	

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				AnalisisDeGastosFormModel controller=new AnalisisDeGastosFormModel();
				AnalisisDeGastosForm form=new AnalisisDeGastosForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
