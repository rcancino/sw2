package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.EsquemaPorTarjeta;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Selector especial para seleccionar una tarjeta de credito 
 * con posiblemente una promicion de meses sin intereses
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SelectorDeTarjeta extends SXAbstractDialog{
	
	boolean credito=true;
	boolean debigo=true;
	private EventList<Tarjeta> tarjetas;
	private CollectionList<Tarjeta, EsquemaPorTarjeta> esquemas;

	public SelectorDeTarjeta() {
		super("Selector de pago con tarjeta");
		init();
	}
	
	public SelectorDeTarjeta(boolean credito,boolean debito) {
		super("Selector de pago con tarjeta");
		this.credito=credito;
		this.debigo=debito;
		init();
	}	
	
	private void init(){
		tarjetas=new BasicEventList<Tarjeta>(0);		
	}

	@Override
	protected JComponent buildContent() {
		JPanel content=new JPanel(new VerticalLayout(5));
		
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);		
		builder.add(CommandUtils.createLoadAction(this, "load"));	
		builder.add(ComponentUtils.buildTextFilterPanel(inputField));
		content.add(builder.getToolBar(),BorderLayout.NORTH);
		content.add(buildGridPanel(),BorderLayout.CENTER);
		
		return content;
	}
	
	private EventSelectionModel<Tarjeta> tarjetasSelectionModel;
	private EventSelectionModel<EsquemaPorTarjeta> esquemasSelectionModel;
	
	private JXTable tarjetasGrid;
	private JXTable esquemasGrid;
	private JTextField inputField=new JTextField(30);
	
	protected JComponent buildGridPanel(){
		
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setResizeWeight(.8);
		TextComponentMatcherEditor editor=new TextComponentMatcherEditor(inputField
				,GlazedLists.textFilterator(new String[]{"banco.nombre","nombre"}));
		FilterList<Tarjeta> filterTarjetas=new FilterList<Tarjeta>(tarjetas,editor);
		final SortedList<Tarjeta> sortedSource=new SortedList<Tarjeta>(filterTarjetas,null);
		final TableFormat<Tarjeta> tf=GlazedLists.tableFormat(Tarjeta.class
				, new String[]{"id","banco.nombre","nombre","comisionVenta"}
				, new String[]{"Id","Banco","Tarjeta","Comisión"}
		);
		final EventTableModel<Tarjeta> tm=new EventTableModel<Tarjeta>(sortedSource,tf);
		tarjetasSelectionModel=new EventSelectionModel(sortedSource);
		tarjetasSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		tarjetasGrid=ComponentUtils.getStandardTable();
		tarjetasGrid.setModel(tm);
		tarjetasGrid.setSelectionModel(tarjetasSelectionModel);
		final Action select=new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				doAccept();
			}
		};
		ComponentUtils.addEnterAction(tarjetasGrid, select);
		tarjetasGrid.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) 
					doAccept();
			}			
		});
		TableComparatorChooser.install(tarjetasGrid, sortedSource, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		tarjetasGrid.packAll();
		JComponent tp=ComponentUtils.createTablePanel(tarjetasGrid);
		tp.setPreferredSize(new Dimension(500,400));
		sp.setTopComponent(tp);
		
		esquemas=new CollectionList<Tarjeta, EsquemaPorTarjeta>(tarjetasSelectionModel.getSelected(),new CollectionList.Model<Tarjeta, EsquemaPorTarjeta>(){
			public List<EsquemaPorTarjeta> getChildren(Tarjeta parent) {				
				return new ArrayList<EsquemaPorTarjeta>(parent.getEsquemas());
			}			
		});
		
		final TableFormat<EsquemaPorTarjeta> tf2=GlazedLists.tableFormat(
				new String[]{"esquema","comisionVenta"}
				,new String[]{"Esquema","Comisión"}
				);
		final EventTableModel<EsquemaPorTarjeta> tm2=new EventTableModel<EsquemaPorTarjeta>(this.esquemas,tf2);
		esquemasGrid=ComponentUtils.getStandardTable();
		esquemasGrid.setModel(tm2);
		esquemasSelectionModel=new EventSelectionModel<EsquemaPorTarjeta>(esquemas);		
		esquemasSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		esquemasGrid.setSelectionModel(esquemasSelectionModel);
		final Action select2=new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				doAccept();
			}
		};
		ComponentUtils.addEnterAction(esquemasGrid, select2);
		esquemasGrid.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) 
					doAccept();
			}			
		});
		esquemasGrid.packAll();
		JComponent tp2=ComponentUtils.createTablePanel(esquemasGrid);
		tp2.setPreferredSize(new Dimension(500,200));
		sp.setBottomComponent(tp2);
		
		return sp;
	}
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	public Tarjeta getTarjeta(){
		return tarjetasSelectionModel.isSelectionEmpty()?null:tarjetasSelectionModel.getSelected().get(0);
	}
	public EsquemaPorTarjeta getEsquema(){
		return esquemasSelectionModel.isSelectionEmpty()?null:esquemasSelectionModel.getSelected().get(0);
	}
	
	public void clear(){
		tarjetas.clear();
	}

	public void load(){		
		SwingWorker<List<Tarjeta>,String> worker=new SwingWorker<List<Tarjeta>, String>(){
			
			protected List<Tarjeta> doInBackground() throws Exception {
				final String hql;
				if(!debigo && credito)
					hql="from Tarjeta t where t.credito=true";
				else if(debigo && !credito)
					hql="from Tarjeta t where t.debito=true";
				else
					hql="from Tarjeta t ";
				return Services.getInstance().getHibernateTemplate().find(hql);
			}
			
			protected void done() {
				try {
					tarjetas.clear();
					tarjetas.addAll(get());
					tarjetasGrid.packAll();
				} catch (Exception e) {e.printStackTrace();}
				
			}
			
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				SelectorDeTarjeta selector=new SelectorDeTarjeta();
				selector.open();
				System.exit(0);
			}

		});
	}

}
