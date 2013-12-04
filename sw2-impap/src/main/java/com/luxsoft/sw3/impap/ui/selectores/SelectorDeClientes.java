package com.luxsoft.sw3.impap.ui.selectores;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.UIFFocusTraversalPolicy;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;


/**
 * Selector de clientes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SelectorDeClientes extends AbstractSelector<Cliente> {
	
	private String[] properties={"clave","nombre","rfc","deCredito","creditoSuspendido","permitirCheque","chequePostfechado"};
	private String[] columnNames={"Clave","Nombre","RFC","Crédito","Cre Susp","Cheque","Post Fech"};
	private KeyHandler keyHandler;
	
	public SelectorDeClientes(Dialog owner){
		super(owner,Cliente.class,"Catálogo de clientes");
		init();
	}
	
	public SelectorDeClientes() {
		super(Cliente.class,"Catálogo de clientes");
		init();
	}
	
	private void init(){
		keyHandler=new KeyHandler();
	}
	
	public JTextField nombreField=new JTextField(40);
	
	@Override
	protected void installEditors(EventList<MatcherEditor<Cliente>> editors) {
		final TextFilterator filterator=GlazedLists.textFilterator("nombre","clave","rfc");
		TextComponentMatcherEditor<Cliente> editor=new TextComponentMatcherEditor<Cliente>(nombreField,filterator);
		editors.add(editor);
	}

	@Override
	protected void afterContentBuild(JPanel content) {
		UIFFocusTraversalPolicy ftp=new UIFFocusTraversalPolicy(){
			@Override
			protected boolean accept(Component component) {				
				if(nombreField==component)
					return true;				
				else if(grid==component)
					return true;
				return false;
			}			
		};
		setFocusTraversalPolicy(ftp);
		ftp.setInitialComponent(nombreField);
		//ComponentUtils.decorateSpecialFocusTraversal(content);
		/*nombreField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS
				, new HashSet(Arrays.asList(
						KeyStroke.getKeyStroke("ENTER")
						,KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0))
						)
		);
		nombreField.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS
				, ComponentUtils.getPreviousFocusKeys());*/
		
	}
	
	/**
	 * 
	 */
	protected JComponent buildToolbar(){		
		ComponentUtils.addF2Action(nombreField, getLoadAction());
		ComponentUtils.addEnterAction(nombreField, getLoadAction());
		FormLayout layout=new FormLayout("p,2dlu,400dlu:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Nombre [F2]",nombreField);
		return builder.getPanel();
	}
	
		
	@Override
	protected TableFormat<Cliente> getTableFormat() {		
		return GlazedLists.tableFormat(Cliente.class, properties,columnNames);
	}
	
	@Override
	protected List<Cliente> getData() {
		if(nombreField.hasFocus() || !StringUtils.isBlank(nombreField.getText())){
			
			String text=nombreField.getText();
			List<Cliente>res= getManager().buscarClientePorNombre(text);
			res.addAll(getManager().buscarClientePorClave(text));
			return res;
		}else{
			
			return new ArrayList<Cliente>(0);
		}
			
	}
	
	protected void afterLoad(){
		if(grid.getRowCount()>0){
			grid.setRowSelectionInterval(0, 0);
		}
	}
	
	protected void onWindowOpened() {
		
	}
	
	private Action loadAction;
	
	private Action getLoadAction(){
		if(loadAction==null){
			loadAction=CommandUtils.createLoadAction(this, "load");
		}
		return loadAction;
	}
	
	public void open(){
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		super.open();
	}
	public void close(){
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyHandler);
		super.close();
	}
	
	public ClienteManager getManager() {
		return  ServiceLocator2.getClienteManager();
	}
	
	protected void execut(SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
		//worker.execute();
	}
	
	//private static SelectorDeClientes INSTANCE;
	
	public static Cliente seleccionar(Dialog owner){
		SelectorDeClientes selector=new SelectorDeClientes(owner);
		selector.open();
		if(!selector.hasBeenCanceled()){
			Cliente selected=selector.getSelected();
			return selector.getManager().get(selected.getId());
			//return selected;
		}
		return null;
	}
	
	public static Cliente seleccionar(){
		SelectorDeClientes selector=new SelectorDeClientes();
		selector.open();
		if(!selector.hasBeenCanceled()){
			Cliente selected=selector.getSelected();
			return selected;
		}
		return null;
	}
	
	public static Cliente seleccionar(String nombre){		
		SelectorDeClientes selector=new SelectorDeClientes();
		selector.nombreField.setText(nombre);
		selector.open();
		if(!selector.hasBeenCanceled()){
			Cliente selected=selector.getSelected();
			return selected;
		}
		return null;
	}
	
	private boolean firstRow=false;
	private boolean lastRow=false;
	
	 /** Garantiza la ejecucion de ciertas tareas mediante teclas de acceso 
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class KeyHandler implements KeyEventPostProcessor{
		/**
		 * Implementacion de {@link KeyEventPostProcessor} para los accesos de teclado rápido
		 * 
		 */
		public boolean postProcessKeyEvent(final  KeyEvent e) {
			if(KeyEvent.KEY_PRESSED==e.getID()){				
				if(KeyStroke.getKeyStroke("TAB").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						grid.requestFocusInWindow();
					}
				}
				if(KeyStroke.getKeyStroke("F3").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						nombreField.selectAll();
						nombreField.requestFocusInWindow();
					}
				}if(KeyStroke.getKeyStroke("F2").getKeyCode()==e.getKeyCode()){
					if(isFocused()){						
						e.consume();
						load();
					}
				}if(nombreField.isFocusOwner() && KeyEvent.VK_DOWN==e.getKeyCode()){
					e.consume();
					grid.requestFocusInWindow();
					if(grid.getRowCount()>0){
						selectionModel.setSelectionInterval(0,0);
					}
					return false;
				}if(grid.hasFocus() && KeyEvent.VK_UP==e.getKeyCode()){					
					int row=grid.getSelectedRow();
					if(row==0 )
						nombreField.requestFocusInWindow();
					return false;
				}if(grid.hasFocus() && KeyEvent.VK_DOWN==e.getKeyCode()){
					int row=grid.getSelectedRow();
					int max=grid.getRowCount()-1;
					if(lastRow){
						nombreField.requestFocusInWindow();
						lastRow=false;
						return false;
					}else
						lastRow=(row==max);
					return false;
				}
			}
			return false;
		}
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				Cliente c=seleccionar();
				System.exit(0);
			}

		});
	}



}
