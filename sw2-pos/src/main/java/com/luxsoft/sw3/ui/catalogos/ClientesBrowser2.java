package com.luxsoft.sw3.ui.catalogos;

import java.awt.Dimension;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.table.JTableHeader;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.controls.SXTable;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Nuevo browser para el mantenimiento de clientes 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ClientesBrowser2 extends SXAbstractDialog{
	
	private EventList<Cliente> eventList;
	private EventSelectionModel<Cliente> selectionModel;
	private JXTable grid;
	private JTextField claveField;
	private JTextField nombreField;

	public ClientesBrowser2() {
		super("Catálogo de clientes (Mantenimiento)");
	}
	
	protected JComponent buildContent() {
		JPanel panel=new JPanel(new VerticalLayout());
		
		panel.add(buildToolbar());
		panel.add(buildGrid());
		
		return panel;
	}
	
	private JComponent buildToolbar(){
		claveField=new JTextField(10);
		nombreField=new JTextField(40);
		
		ToolBarBuilder builder=new ToolBarBuilder();
		builder.add(getViewAction());
		builder.add(CommandUtils.createLoadAction(this, "doLoad"));
		//builder.add(getInsertAction());
		//builder.add(CommandUtils.createEditAction(this, "doEdit"));
		
		builder.add(DefaultComponentFactory.getInstance().createTitle(" Nombre "));		
		builder.add(nombreField);
		builder.add(DefaultComponentFactory.getInstance().createTitle(" Clave "));
		builder.add(claveField);		
				
		return builder.getToolBar();
	}
	
	private JComponent buildGrid(){		
		grid=new SXTable();
    	grid.setColumnControlVisible(false);
		grid.setHorizontalScrollEnabled(true);		
		grid.setRolloverEnabled(true);
		Highlighter alternate=HighlighterFactory.createAlternateStriping();		
		grid.setHighlighters(new Highlighter[]{alternate});
		grid.setRolloverEnabled(true);		
		grid.setSortable(false);
		grid.getSelectionMapper().setEnabled(false);
		grid.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
		
		//EventList
		eventList=new BasicEventList<Cliente>(0);
		final EventList<MatcherEditor<Cliente>> editors=new BasicEventList<MatcherEditor<Cliente>>();
		
		editors.add(new TextComponentMatcherEditor<Cliente>(nombreField, GlazedLists.textFilterator(Cliente.class, "nombre") ));
		editors.add(new TextComponentMatcherEditor<Cliente>(claveField, GlazedLists.textFilterator(Cliente.class, "clave") ));
		
		final MatcherEditor<Cliente> matcherEditor=new CompositeMatcherEditor<Cliente>(editors);
		FilterList<Cliente> filterList=new FilterList<Cliente>(eventList,matcherEditor);
		SortedList<Cliente> sortedList=new SortedList<Cliente>(filterList,null);
		final TableFormat<Cliente> tf=GlazedLists.tableFormat(Cliente.class
				,new String[]{"clave","nombre","rfc","direccionAsString"}
				,new String[]{"Clave","Nombre","RFC","Dirección"}
		);
		
		
		final EventTableModel<Cliente> tm=new EventTableModel<Cliente>(sortedList,tf);
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<Cliente>(sortedList);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateForDobleClick(grid,getViewAction() );
		ComponentUtils.addEnterAction(grid, getViewAction());
		ComponentUtils.addInsertAction(grid, getInsertAction());
		JComponent c=ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(650,450));
		return c;
	}
	
	
	public void doLoad(){
		if( !nombreDigitado() && !claveDigitada() ){			
			MessageUtils.showMessage("Digite el nombre o clave (parcial o total) del cliente(s) " +
					"que busca","Buscando clientes");
		}
		else{
			SwingWorker<List<Cliente>,String> worker=new SwingWorker<List<Cliente>, String>(){				
				protected List<Cliente> doInBackground() throws Exception {
					return findData();
				}				
				protected void done() {
					try {
						List<Cliente> data=get();
						eventList.clear();
						eventList.addAll(data);
						grid.packAll();
					} catch (InterruptedException e) {						
						e.printStackTrace();
					} catch (ExecutionException e) {						
						e.printStackTrace();
					}finally{
						grid.packAll();
					}
				}
			};
			TaskUtils.executeSwingWorker(worker);
		}
	}
	
	public List<Cliente> findData(){
		if(nombreDigitado()){
			return Services.getInstance().getClientesManager().buscarClientePorNombre(nombreField.getText());			
		}else if(claveDigitada()){
			return Services.getInstance().getClientesManager().buscarClientePorClave(claveField.getText());
		}else
			return ListUtils.EMPTY_LIST;
	}
	
	private boolean nombreDigitado(){
		return StringUtils.isNotBlank(nombreField.getText());
	}
	private boolean claveDigitada(){
		return StringUtils.isNotBlank(claveField.getText());
	}
	
	public void doSelect(){
		int index=selectionModel.getMinSelectionIndex();
		if(index!=-1){
			Cliente c=eventList.get(index);
			Cliente target=Services.getInstance().getClientesManager().get(c.getId());
			final ClienteModel model=new ClienteModel(target);
			model.setReadOnly(true);
			final ClienteForm form=new ClienteForm(model);
			form.open();
		}
	}

	
	public void doInsert(){
		Cliente target=ClienteController.getInstance().registrar();
		if(target!=null){
			eventList.add(target);
		}
	}
	
	public void doEdit(){
		if(!selectionModel.isSelectionEmpty()){
			Cliente selected=selectionModel.getSelected().get(0);
			int index=eventList.indexOf(selected);
			if(index!=-1){
				Cliente target=Services.getInstance().getClientesManager().get(selected.getId());
				final ClienteModel model=new ClienteModel(target);
				final ClienteForm form=new ClienteForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					Cliente res= (Cliente)model.getBaseBean();
					res=Services.getInstance().getClientesManager().save(res);
					MessageUtils.showMessage("Cliente registrado con clave: "+res.getClave(), "Registro de clientes");
					eventList.set(index, res);
				}
			}
		}
	}
	
	private Action viewAction;
	private Action insertAction;
	
	
	public Action getViewAction(){
		if(viewAction==null){
			viewAction=CommandUtils.createViewAction(this, "doSelect");
		}
		return viewAction;
	}
	
	public Action getInsertAction(){
		if(insertAction==null){
			insertAction=CommandUtils.createInsertAction(this, "doInsert");
		}
		return insertAction;
	}
	
	private KeyHandler keyHandler;
	
	@Override
	protected void onWindowOpened() {
		keyHandler=new KeyHandler();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		if(nombreField!=null)
			nombreField.requestFocusInWindow();
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
				if(KeyStroke.getKeyStroke("F2").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						doLoad();
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public static Action getRegistrarAction(){
		Action a=new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ClientesBrowser2 browser=new ClientesBrowser2();
				browser.open();
			}
		};
		return a;
	}


	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				ClientesBrowser2 browser=new ClientesBrowser2();
				browser.open();
				System.exit(0);
			}

		});
	}

	
	

}
