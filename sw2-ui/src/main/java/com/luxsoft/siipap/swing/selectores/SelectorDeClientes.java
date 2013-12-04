package com.luxsoft.siipap.swing.selectores;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.springframework.util.StringUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uifextras.util.UIFactory;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.service.core.ClienteManagerImpl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.BrowserUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;


public class SelectorDeClientes extends SXAbstractDialog{
	
	private EventList<Cliente> clientes=new BasicEventList<Cliente>();
	private EventSelectionModel<Cliente> selectionModel;
	private JXTable grid;
	private JTextField inputField;
	
	public SelectorDeClientes() {
		super("Selector de Clientes");
	}	
	
	private String[] properties={"clave","nombre","permitirCheque"};
	private String[] columnNames={"Clave","Nombre","Permitir cheque"};
	
		
	public ClienteManager getManager() {
		return  ServiceLocator2.getClienteManager();
	}
	
	private void initComponents(){		
		SortedList<Cliente> sortedList=new SortedList<Cliente>(clientes,null);
		
		inputField=new JTextField();
		TextFilterator<Cliente> filterator=GlazedLists.textFilterator(properties);
		TextComponentMatcherEditor<Cliente> editor=new TextComponentMatcherEditor<Cliente>(inputField,filterator);
		FilterList<Cliente> textFilter=new FilterList<Cliente>(sortedList,editor);
		
		selectionModel=new EventSelectionModel<Cliente>(textFilter);
		selectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		
		TableFormat<Cliente> tf=GlazedLists.tableFormat(properties,columnNames);
		EventTableModel<Cliente> tm=new EventTableModel<Cliente>(textFilter,tf);
		
		grid=new JXTable(tm);
		grid.setSortable(false);
		grid.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		grid.setSelectionModel(selectionModel);
		grid.getActionMap().put("select",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				doAccept();
			}
		});
		grid.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"select");
		grid.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					doAccept();
				}
			}
		});
		inputField.getActionMap().put("f2",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				cargarPorClave(inputField.getText().toUpperCase());
			}
		});
		inputField.getInputMap().put(KeyStroke.getKeyStroke("F2"),"f2");
		
		Action transferAction =new AbstractAction("transfer"){
			public void actionPerformed(ActionEvent e) {
				grid.requestFocus();
			}			
		};
		ComponentUtils.addAction(inputField, transferAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		
		grid.packAll();
	}
	

	@Override
	public void doAccept() {
		//System.out.println("Selection: "+selectionModel.getSelected());
		super.doAccept();
	}
	
	private void loadData(List<Cliente> data){
		clientes.getReadWriteLock().writeLock().lock();
		try{
			clientes.clear();
			clientes.addAll(data);
			if(grid!=null)grid.packAll();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			clientes.getReadWriteLock().writeLock().unlock();
		}
	}
	
	public void cargarPorClave(final String clave){
		if(!StringUtils.hasLength(clave)) return;
		SwingWorker<List<Cliente>, String> worker=new SwingWorker<List<Cliente>, String>(){
			protected List<Cliente> doInBackground() throws Exception {
				return getManager().buscarClientePorClave(clave);
			}
			@Override
			protected void done() {
				try {
					loadData(get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		TaskUtils.executeSwingWorker(worker);
	}
	public void cargarPorNombre(final String nombre){		
		if(!StringUtils.hasLength(nombre)) return;
		SwingWorker<List<Cliente>, String> worker=new SwingWorker<List<Cliente>, String>(){
			protected List<Cliente> doInBackground() throws Exception {
				return getManager().buscarClientePorNombre(nombre);
			}
			@Override
			protected void done() {
				try {
					loadData(get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		};
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected JComponent buildContent() {
		initComponents();
		JPanel panel=new JPanel(new BorderLayout(5,5));
		
		JComponent inputPanel=BrowserUtils.buildTextFilterPanel(inputField);
		panel.add(inputPanel,BorderLayout.NORTH);
		
		JComponent c=UIFactory.createTablePanel(grid);
		c.setPreferredSize(Resizer.FOUR2THREE.fromHeight(500));
		panel.add(c,BorderLayout.CENTER);
		
		return panel;
	}
	
	public Cliente getSelection(){
		if(selectionModel.getSelected().isEmpty()) return null;
		return selectionModel.getSelected().get(0);
	}
	
	public static void main(String[] args) {
		SelectorDeClientes selector=new SelectorDeClientes();
		selector.open();
	}

}
