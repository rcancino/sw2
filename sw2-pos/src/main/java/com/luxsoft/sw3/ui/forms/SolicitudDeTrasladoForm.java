package com.luxsoft.sw3.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
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
import com.luxsoft.siipap.inventarios.model.SolicitudDeTrasladoDet;
import com.luxsoft.siipap.pos.ui.utils.UIUtils;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.services.Services;


/**
 * Forma para el mantenimiento de compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudDeTrasladoForm extends AbstractForm implements ListSelectionListener{
	
	

	public SolicitudDeTrasladoForm(final SolicitudDeTrasladoController model) {
		super(model);
		setTitle("Solicitud de Traslado       ("+model.getValue("sucursal")+" )");
		getController().getUserModel().addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				String user=getController().getUser();
				usuarioNombre.setText(StringUtils.defaultString(user));
			}
			
		});
	}
	
	public SolicitudDeTrasladoController getController(){
		return (SolicitudDeTrasladoController)getModel();
	}
	
	private JLabel usuarioNombre;

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.3),3dlu," +
				"p,2dlu,p:g(.3),3dlu," +
				"p,2dlu,p:g(.4)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")==null)
			builder.append("Origen",getControl("origen"));
		else{
			builder.append("Documento",addReadOnly("documento"));
		}
		builder.append("Fecha",addReadOnly("fecha"));
		builder.nextLine();
		usuarioNombre=new JLabel("");
		builder.append("Usuario",getControl("usuario"));
		builder.append(usuarioNombre,5);
		builder.nextLine();
		builder.append("Referencia",getControl("referencia"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		ajustarActions(panel);
		ComponentUtils.decorateSpecialFocusTraversal(panel);
		if(model.getValue("id")==null)
			ComponentUtils.decorateTabFocusTraversal(getControl("origen"));
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
		if(model.getValue("id")==null)
			getControl("origen").requestFocusInWindow();
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("origen".equals(property)){
			if(model.getValue("id")==null){
				List sucursales=Services.getInstance().getSucursalesOperativas();
				sucursales.remove(getModel().getValue("sucursal"));
				SelectionInList sl=new SelectionInList(sucursales,model.getModel(property));
				suursalesBox=BasicComponentFactory.createComboBox(sl);
				return suursalesBox;
			}else{
				return BasicComponentFactory.createLabel(model.getModel(property), UIUtils.buildToStringFormat());
			}
		}else if("usuario".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(getController().getUserModel(),true);
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		return null;
	}
	
	private JComboBox suursalesBox;
	
	
	protected JPanel buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Insertar [INS]");
		getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");		
		
		getViewAction().setEnabled(false);
		JButton buttons[]={
				new JButton(getInsertAction())
				//,new JButton(getDeleteAction())
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
	
	private Action insertarBulkAction;
	public Action getInsertarBulkAction(){
		if(insertarBulkAction==null){
			insertarBulkAction=new com.luxsoft.siipap.swing.actions.DispatchingAction(this, "insertarBulk");
			insertarBulkAction.putValue(Action.NAME, "Agregar Batch");
			insertarBulkAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
		}
		return insertarBulkAction;
	}
	
	
	private JTable grid;
	private EventSelectionModel<SolicitudDeTrasladoDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={"producto.clave","producto.descripcion","solicitado","comentario"};
		String[] columnLabels={"Producto","Descripción","Solicitado","Comentario"};
		boolean[] edits={false,false,true,true};
		final TableFormat tf=GlazedLists.tableFormat(SolicitudDeTrasladoDet.class,propertyNames, columnLabels,edits);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=new JTable(tm);
		selectionModel=new EventSelectionModel<SolicitudDeTrasladoDet>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					view();
			}			
		});
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		gridComponent.setPreferredSize(new Dimension(750,300));
		return gridComponent;
	}
	
	public void insertarBulk(){
		getController().insertarBulk(grid);
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
	}
	
	public void insertPartida(){
		getController().insertarBulk(grid);
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
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().editar(index);
			}
		}
	}
	
	public void view(){
		System.out.println("Método view() no implementado");
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
				SolicitudDeTrasladoController controller=new SolicitudDeTrasladoController();
				SolicitudDeTrasladoForm form=new SolicitudDeTrasladoForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
