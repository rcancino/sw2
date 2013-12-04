package com.luxsoft.siipap.pos.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;


/**
 * Forma para el mantenimiento de regreso de embarque
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RegresoDeEmbarqueForm extends AbstractForm implements ListSelectionListener{
	
	

	public RegresoDeEmbarqueForm(final RegresoDeEmbarqueFormModel model) {
		super(model);
		setTitle("Registro de retorno de embarque");
		model.getUserModel().addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				User user=(User)evt.getNewValue();
				usuarioNombre.setText(user!=null?user.getFullName():"Digite su password");
			}
		});
		
		
	}
	
	public RegresoDeEmbarqueFormModel getController(){
		return (RegresoDeEmbarqueFormModel)getModel();
	}
	private JLabel usuarioNombre;

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,p,3dlu," +
				"p,2dlu,p,3dlu," +
				"p,2dlu,p:g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Documento",addReadOnly("documento"));
		builder.append("Sucursal",addReadOnly("sucursal"));
		builder.nextLine();
		builder.append("Chofer",addReadOnly("chofer"));
		builder.append("Transporte",addReadOnly("transporte"));
		builder.nextLine();
		builder.append("Salida",addReadOnly("salida"));
		builder.append("Fecha",addReadOnly("fecha"));
		builder.nextLine();		
		builder.append("Valor",addReadOnly("valor"));
		builder.append("Kilos",addReadOnly("kilos"));
		builder.nextLine();
		builder.append("Regreso",getControl("regreso"));
		//builder.nextLine();
		usuarioNombre=new JLabel("Digite su password.");
		builder.append("Operador (clave)",getControl("usuario"));
		builder.append(usuarioNombre);
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		ajustarActions(panel);
		ComponentUtils.decorateSpecialFocusTraversal(panel);
		ComponentUtils.decorateTabFocusTraversal(getControl("regreso"));
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		
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
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("regreso".equals(property)){
			JSpinner sp=new JSpinner();
			Date d1=DateUtils.addDays(new Date(), -1);
			Date d2=DateUtils.addDays(new Date(), 1);
			sp.setModel(SpinnerAdapterFactory.createDateAdapter(model.getModel(property), new Date(),d1,d2,Calendar.MINUTE));
			//SpinnerDateModel smodel=(SpinnerDateModel)box.getModel();
			//smodel.setStart(DateUtils.addHours(getController().getEmbarque().getSalida(), 1));
			//smodel.setEnd(DateUtils.addDays(getController().getEmbarque().getSalida(), 1));
			//smodel.setCalendarField(Calendar.HOUR_OF_DAY);
			sp.setEnabled(false);
			return sp;
		}else if("comentario".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}else if("fecha".equals(property) || "salida".equals(property)){
			JLabel l=BasicComponentFactory.createLabel(model.getModel(property), new SimpleDateFormat("dd/MM/yyyy  HH:mm"));
			return l;
			//JFormattedTextField box=BasicComponentFactory.createDateField(model.getModel(property));
			//box.setEnabled(!model.isReadOnly());
			//return box;
		}else if("usuario".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(getController().getPasswordModel(),true);
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		return null;
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
				//new JButton(getInsertAction())
				new JButton(getDeleteAction())
				//,new JButton(getEditAction())
				//,new JButton(getImprimirAction())
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
	
	private JXTable grid;
	
	private EventSelectionModel<Entrega> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] props={
				"factura.origen"
				,"factura.documento"
				,"factura.fecha"
				,"factura.nombre"
				,"factura.total"
				,"valor"
				,"parcial"
				,"arribo"
				,"recepcion"
				};
		String[] names={
				"Origen"
				,"Factura"
				,"Fecha"
				,"Cliente"
				,"Total (Fac)"
				,"Valor (emb)"
				,"Parcial"
				,"Arribo"
				,"Recibió"
				};
		
		final TableFormat tf=GlazedLists.tableFormat(Entrega.class,props, names);
		SortedList sorted=new SortedList(getController().getPartidasSource(),null);
		final EventTableModel tm=new EventTableModel(sorted,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<Entrega>(sorted);
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
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
			Entrega det=selectionModel.getSelected().get(0);
			getController().elminarPartida(det);
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
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().view(index);
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
				final Embarque e=new Embarque();
				e.setSalida(new Date());
				RegresoDeEmbarqueFormModel controller=new RegresoDeEmbarqueFormModel(e);
				RegresoDeEmbarqueForm form=new RegresoDeEmbarqueForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}
	

}
