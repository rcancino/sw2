package com.luxsoft.sw3.ui.forms;

import java.awt.Dimension;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
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

import com.luxsoft.siipap.inventarios.model.Conteo;
import com.luxsoft.siipap.inventarios.model.ConteoDet;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.services.Services;


/**
 * Forma para el registro de devoluciones de compra
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ConteoForm extends AbstractForm implements ListSelectionListener{
	
	

	public ConteoForm(final ConteoController model) {
		super(model);
		setTitle("Captura de conteo de inventario    ("+model.getValue("sucursal")+" )");
	}
	
	public ConteoController getController(){
		return (ConteoController)getModel();
	}
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"40dlu,2dlu,max(60dlu;p):g(.5),3dlu," +
				"40dlu,2dlu,max(60dlu;p):g(.5)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null){
			builder.append("Documento",addReadOnly("documento"),true);
			
		}
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Sector",getControl("sector"));
		builder.nextLine();
		builder.append("Capturo",getControl("capturista"));
		builder.nextLine();
		builder.append("Conto 1",getControl("contador1"));
		builder.append("Conto 2",getControl("contador2"));
		builder.nextLine();
		builder.append("Reviso 1",getControl("auditor1"));
		builder.append("Reviso 2",getControl("auditor2"));
		builder.nextLine();
		//builder.append("Comentario",getControl("comentario"),true);
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
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
	
	
	

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		return null;
	}
	
	
	protected JPanel buildToolbarPanel(){
		
		getInsertAction().putValue(Action.NAME, "Insertar [INS]");
		getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getDeleteAction().setEnabled(!model.isReadOnly());
		
		getViewAction().setEnabled(false);
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JXTable grid;
	private EventSelectionModel<ConteoDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={"renglon","clave","descripcion","unidad","kilos","cantidad"};
		String[] columnLabels={"Rngl","Producto","Descripción","U","kilos","Cantidad"};
		boolean[] edits={false,false,false,false,false,true};
		final TableFormat tf=GlazedLists.tableFormat(ConteoDet.class,propertyNames, columnLabels,edits);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<ConteoDet>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.setEnabled(!model.isReadOnly());
		grid.packAll();
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
	
	private KeyHandler keyHandler;
	
	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		keyHandler=new KeyHandler();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		getControl("sector").requestFocusInWindow();
	}
	
	@Override
	public void close() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyHandler);
		super.close();
	}
	
	private class KeyHandler implements KeyEventPostProcessor{
		/**
		 * Implementacion de {@link KeyEventPostProcessor} para los accesos de teclado rápido
		 * 
		 */
		public boolean postProcessKeyEvent(final  KeyEvent e) {
			if(KeyEvent.KEY_PRESSED==e.getID()){
				
				if(KeyStroke.getKeyStroke("INSERT").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						insertPartida();
						return true;
					}
				}else if(KeyStroke.getKeyStroke("DELETE").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						deletePartida();
						return true;
					}
				}else if(KeyStroke.getKeyStroke("F11").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						//edit();
						return true;
					}
				}
				
			}								
			return false;
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				ConteoController controller=new ConteoController();
				ConteoForm form=new ConteoForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					Conteo res=controller.persistir();
					System.out.println(ToStringBuilder.reflectionToString(res));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
