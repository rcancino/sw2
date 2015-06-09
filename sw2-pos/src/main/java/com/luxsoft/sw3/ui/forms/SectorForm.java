package com.luxsoft.sw3.ui.forms;

import java.awt.Dimension;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
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
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.inventarios.model.Sector;
import com.luxsoft.siipap.inventarios.model.SectorDet;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;


/**
 * Forma para el registro de devoluciones de compra
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SectorForm extends AbstractForm implements ListSelectionListener{
	
	

	public SectorForm(final SectorController model) {
		super(model);
		setTitle("Registro De Sectores   ("+model.getValue("sucursal")+" )");
	}
	
	public SectorController getController(){
		return (SectorController)getModel();
	}
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"50dlu,2dlu,max(60dlu;p):g(.5),3dlu," +
				"50dlu,2dlu,max(60dlu;p):g(.5)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		/*if(model.getValue("id")!=null){
			builder.append("Documento",addReadOnly("documento"),true);
			
		}*/
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Sector",getControl("sector"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"));
		builder.nextLine();
		builder.append("Responsable 1",getControl("responsable1"));
		builder.append("Responsable 2",getControl("responsable2"));
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
	private EventSelectionModel<SectorDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={"ind","clave","descripcion","unidad","kilos","comentario"};
		String[] columnLabels={"Rngl","Producto","Descripcin","U","kilos","Comentario"};
		boolean[] edits={true,false,false,false,false,false,true};
		final TableFormat tf=GlazedLists.tableFormat(SectorDet.class,propertyNames, columnLabels,edits);
        Comparator<SectorDet> c=GlazedLists.beanPropertyComparator(SectorDet.class, "ind");
		EventList<SectorDet> partidasSector=getController().getPartidasSource();
		Collections.sort(partidasSector, c);
		
		final EventTableModel tm=new EventTableModel(partidasSector,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<SectorDet>(getController().getPartidasSource());
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
				SectorController controller=new SectorController();
				SectorForm form=new SectorForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					Sector res=controller.persistir();
					System.out.println(ToStringBuilder.reflectionToString(res));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
