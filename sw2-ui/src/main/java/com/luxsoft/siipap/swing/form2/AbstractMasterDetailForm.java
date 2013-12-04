package com.luxsoft.siipap.swing.form2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;




/**
 * Forma para el mantenimiento de instancias con maestro y detalle
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractMasterDetailForm extends GenericAbstractForm{
	
	protected Logger logger=Logger.getLogger(getClass());

	public AbstractMasterDetailForm(MasterDetailFormModel model) {
		super(model);
		model.addPropertyChangeListener("isReadOnly", new ReadOnlyListener());
	}
	
	public MasterDetailFormModel getMainModel(){
		return (MasterDetailFormModel)getModel();
	}
	
	protected HeaderPanel header;

	@Override
	protected JComponent buildHeader() {
		if(header==null)
			header=new HeaderPanel("Título","Descripción");
		return header;
	}
	
	
	/**
	 * Sobre escribimos para poder instalar el panel de validacion 
	 * en un lugar mas apropiado
	 * 
	 */
	protected JComponent buildMainPanel(){
		return buildFormPanel();
	}
	
	protected String getValidationPanelRowDef(){
		return "50dlu";
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p:g,2dlu,f:p"
				,"p,5dlu,p,5dlu,f:p:g,5dlu,"+getValidationPanelRowDef()+",5dlu");
		final PanelBuilder builder=new PanelBuilder(layout);
		final CellConstraints cc=new CellConstraints();
		builder.add(buildMasterForm(),cc.xy(1, 1));
		builder.add(buildDetailPanel(),cc.xyw(1, 5, 1));
		if(getMainModel().manejaTotalesEstandares())
			builder.add(buildTotalesPanel(),cc.xy(1, 7));
		else
			builder.add(buildValidationPanel(),cc.xy(1, 7));
		model.validate();
		updateComponentTreeMandatoryAndSeverity(builder.getPanel());
		return builder.getPanel();
	}
	
	/**
	 * {@link DefaultFormBuilder} que puede ser usado como el default
	 * en las sub-clases
	 * 
	 * @return
	 */
	protected DefaultFormBuilder getDefaultMasterFormBuilder(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;40dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;90dlu):g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		return builder;
	}

	/**
	 * Template Method para la construccion del panel maestro para la forma
	 * 
	 * @return
	 */
	protected abstract JComponent buildMasterForm();
	
	//protected abstract AbstractForm buildDetailForm();
	
	
	/**
	 * Consstruye el panel de Importe, impuesto y total. Asume que el modelo puede
	 * proporcionar {@link ValueModel} para estas propiedades
	 * Tambien coloca aqui el panel de validación si la fomra no es de solo lectura
	 * 
	 * @return
	 */
	protected JComponent buildTotalesPanel(){
		
		final FormLayout layout=new FormLayout(
				"p:g,5dlu,p,2dlu,max(p;50dlu)"
				,"p,2dlu,p,2dlu,p");
		
		//final FormDebugPanel debugPanel=new FormDebugPanel(layout);
		
		final PanelBuilder builder=new PanelBuilder(layout);		
		final CellConstraints cc=new CellConstraints();
		
		if(!model.isReadOnly())			
			builder.add(buildValidationPanel(),cc.xywh(1, 1,1,5));
		
		builder.addLabel("Importe",cc.xy(3, 1));
		builder.add(addReadOnly("importe"),cc.xy(5, 1));
		
		builder.addLabel("Impuesto",cc.xy(3, 3));
		builder.add(addReadOnly("impuesto"),cc.xy(5, 3));
		
		builder.addLabel("Total",cc.xy(3, 5));
		builder.add(addReadOnly("total"),cc.xy(5, 5));	
				
		return builder.getPanel();
	}
	
	
	
	
	
	protected JComponent buildDetailPanel(){
		JPanel panel=new JPanel(new BorderLayout());
		grid=buildGrid();
		grid.setEnabled(!model.isReadOnly());
		fixColumns(grid.getColumnModel());
		adjustGrid(grid);
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		//final JScrollPane sp=new JScrollPane(grid);
		configDetailScrollPanel(gridComponent);
		panel.add(gridComponent,BorderLayout.CENTER);
		installToolbar(panel);
		/*
		if(getToolbarType()==JToolBar.HORIZONTAL)
			panel.add(getDetallesToolbarBuilder().getToolBar(),BorderLayout.NORTH);
		else
			panel.add(getDetallesToolbarBuilder().getToolBar(),BorderLayout.WEST);
			*/
		return panel;
	}
	
	protected void installToolbar(final JPanel panel){
		if(getToolbarType()==JToolBar.HORIZONTAL)
			panel.add(getDetallesToolbarBuilder().getToolBar(),BorderLayout.NORTH);
		else
			panel.add(getDetallesToolbarBuilder().getToolBar(),BorderLayout.WEST);
	}
	
	protected void configDetailScrollPanel(final JComponent sp){
		sp.setPreferredSize(new Dimension(300,200));
	}
	
	protected void fixColumns(final TableColumnModel cm){
		
	}
	
	/**
	 * Template method para ajustar el grid justo despues de su creacion
	 * 
	 * @param grid
	 */
	protected void adjustGrid(JXTable grid){
		
	}
	
	protected int getToolbarType(){
		return JToolBar.HORIZONTAL;
	}
	
	protected ToolBarBuilder getDetallesToolbarBuilder(){
		final JToolBar bar=new JToolBar(getToolbarType());
		final ToolBarBuilder builder=new ToolBarBuilder(bar);
		for(Action a:getDetallesActions()){
			builder.add(a);
		}
		enableEditingActions(!model.isReadOnly());
		enableSelectionActions();
		return builder;
	}
	
	protected Action[] actions;
	
	protected Action[] getDetallesActions(){
		if(actions==null)
			actions=new Action[]{getInsertAction(),getDeleteAction(),getEditAction(),getViewAction()};
		return actions;
	}
	
	//*************************** GLAZEDLIST *******************************************/
	
	protected JXTable grid;
	protected EventList partidasSource;
	protected SortedList sortedPartidas;
	protected EventSelectionModel selection;	
	
	
	protected abstract TableFormat getTableFormat();
	
	@SuppressWarnings("unchecked")
	protected JXTable buildGrid(){
		partidasSource=getFilterList(getMainModel().getPartidas());
		EventList eventList=partidasSource;
		eventList.addListEventListener(new PartidasListener());
		sortedPartidas=new SortedList(eventList,null);
		grid=ComponentUtils.getStandardTable();
		final EventTableModel tm=new EventTableModel(sortedPartidas,getTableFormat());
		selection=new EventSelectionModel(sortedPartidas);
		selection.addListSelectionListener(new SelectionHandler());
		grid.setModel(tm);
		grid.setSelectionModel(selection);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					view();
			}			
		});
		new TableComparatorChooser(grid,sortedPartidas,true);
		grid.setColumnControlVisible(false);
		return grid;
	}
	
	/**
	 * Template method para dar oportunidad a personalizar el {@link EventList}
	 * que contiene a las partidas
	 * 
	 * @param source
	 * @return
	 */
	protected EventList getFilterList(final EventList source){
		return source;
	}
	
	public Object getSelected(){
		if(!selection.isSelectionEmpty()){
			return selection.getSelected().get(0);
		}
		return null;
	}
	

	//*************************************** END GLAZEDLIST **************************************/
	
	
	/***** Acciones y su compratimiento   *******/
	
	
	
	/**
	 * Inserta un nuevo detalle delegando el proceso al modelo
	 *
	 */
	public void insertPartida(){
		Object newObject=doInsertPartida();
		if(newObject!=null){
			newObject=getMainModel().insertDetalle(newObject);
			if(newObject!=null){
				int index=getMainModel().getPartidas().size()-1;
				selection.setSelectionInterval(index, index);
				afterPartidaInserted(newObject);
				if(logger.isDebugEnabled()){
					logger.debug("Nueva partida agregada: "+newObject);
				}
			}
		}
	}
	
	/**
	 * Template method para crear un nuevo detalle desde la perspectiva de la 
	 * GUI
	 * Con este metodo proporcionamos dos opciones para generar detalles
	 * Una a partir de una forma de captura y la otra mediante la edicion
	 * de las propiedades desde el grid con un TableFormat de escritura
	 * 
	 * TODO: El intento de este metodo podria ser modificado para mayor claridad
	 * por el momento es lo mejor que tenemos
	 * 
	 * 
	 * @return
	 */
	public Object doInsertPartida(){
		if(logger.isDebugEnabled()){
			logger.debug("Generando una nueva partida");
		}
		return null;
	}
	
	/**
	 * Template Method para personalizar el comporamiento posterior a la insercion de la partida
	 * Esta implementación delega a {@link MasterDetailFormModel}
	 *  
	 * @param partida
	 */
	protected void afterPartidaInserted(final Object partida){
		getMainModel().afeterPartidaInserted(partida);
		grid.packAll();
	}
	
	public void deletePartida(){		
		if(getSelected()!=null)
			doDeletePartida(getSelected());
	}
	
	public void doDeletePartida(Object obj){
		getMainModel().deleteDetalle(obj);		
	}
	
	public void edit(){
		if(logger.isDebugEnabled()){
			logger.debug("Editando una partida");
		}
		if(getSelected()!=null)
			doEdit(getSelected());
	}
	
	protected void doEdit(Object obj){
		
	}
	
	public void view(){
		if(logger.isDebugEnabled()){
			logger.debug("Consultado una partida");
		}
		if(getSelected()!=null)
			doView(getSelected());
	}
	protected void doView(Object obj){
		
	}
	
	
	/**
	 * Habilirta acciones relacionadas con edicion
	 * 
	 * @param val
	 */
	public void enableEditingActions(boolean val){
		getInsertAction().setEnabled(val);
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
	}
	
	/**
	 * Habilita acciones en funcion de la seleccion del grid de partidas
	 * Si el usuario no ha seleccionado nada estas acciones deben permanecer
	 * deshabilitadas
	 * 
	 *  
	 * @param val
	 */
	protected void enableSelectionActions(){
		boolean val=!selection.isSelectionEmpty();
		if(model.isReadOnly()){
			val=false;
		}
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selection.isSelectionEmpty());
	}
	
	/*
	protected void doPartidasChange(){
		if(logger.isDebugEnabled()){
			logger.debug("Cambio las partidas del detalle...");
		}
		
	}
	*/
	
	/**
	 * 
	 *  
	 **/ 
	protected void doPartidaInserted(ListEvent listChanges){
		
	}
	
	protected void doPartidaDeleted(ListEvent listChanges){
		
	}
	
	protected void doPartidaUpdated(ListEvent listChanges){
		
	}
	
	// ******************************* END  Acciones y su compratimiento   *******/
	
	/**
	 * Controla el comportamiento de la forma en funcion
	 * de que el modelo sea o no de solo lectura
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class ReadOnlyListener implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			Boolean val=(Boolean)evt.getNewValue();
			enableEditingActions(!val);
		}		
	}
	
	/**
	 * Controla las acciones en funcion de la seleccion de
	 * partidas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public class SelectionHandler implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e) {
			enableSelectionActions();
		}
		
	}
	
	
	/**
	 * Handler para detectar modificaciones en la lista de partidas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class PartidasListener implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			while(listChanges.next()){
				if(listChanges.getType()==ListEvent.INSERT){
					doPartidaInserted(listChanges);
				}else if(listChanges.getType()==ListEvent.DELETE){
					doPartidaDeleted(listChanges);
				}else if(listChanges.getType()==ListEvent.UPDATE){
					doPartidaUpdated(listChanges);
				}
			}
		}
	}

}
