package com.luxsoft.siipap.pos.ui.forms;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.pos.ui.utils.UIUtils;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.embarque.EntregaDet;

public class EntregaForm extends AbstractForm {
	
	private JXTable grid;
	private JTextField pedidoField;

	public EntregaForm(EntregaController model) {
		super(model);
		setTitle("Registro de Entrega");
	}
	
	private EntregaController getController(){
		return (EntregaController)getModel();
	}

	@Override
	protected JComponent buildFormPanel() {
		final JPanel panel=new JPanel(new VerticalLayout());
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;70dlu), 3dlu," +
				"p,2dlu,max(p;70dlu),p:g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.appendSeparator("Factura");
		
		pedidoField=new JTextField(10);
		pedidoField.setEditable(false);
		pedidoField.setFocusable(false);
		pedidoField.setText(String.valueOf(getController().getEntrega().getFactura().getPedido().getFolio()));
		builder.append("Pedido",pedidoField,true);
		
		builder.append("Venta",addReadOnly("documento"));
		builder.append("Fiscal",addReadOnly("numeroFiscal"));
		
		builder.append("Tipo",addReadOnly("origen"));
		builder.append("Fecha",addReadOnly("fechaFactura"));
		
		builder.append("Cliente",addReadOnly("nombre"),6);
		
		builder.appendSeparator("Instrucciones");		
		builder.append("Paquetes",getControl("paquetes"));
		builder.append("Parcial",getControl("parcial"),true);
		
		builder.append("Surtidor",getControl("surtidor"));
		builder.append("Surtido",getControl("surtido"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),6);
		builder.append("Direccion",getControl("instruccionDeEntrega"),6);
		
		panel.add(builder.getPanel());
		
		panel.add(buildGrid());
		return panel;
	}
	

	@Override
	protected JComponent buildButtonBarWithOKCancel() {
		
		JButton insert=new JButton(getInsertAction());
		insert.setText("Insertar");
		insert.setMnemonic('I');
		
		JButton delete=new JButton(getDeleteAction());
		delete.setText("Eliminar");
		delete.setMnemonic('E');
		
		JButton modificarDireccion=new JButton("Dirección");
		modificarDireccion.setMnemonic('D');
		modificarDireccion.addActionListener(EventHandler.create(ActionListener.class, getController(), "cambiarDireccion"));
		
		JPanel bar = ButtonBarFactory.buildRightAlignedBar(new JButton[]{
				modificarDireccion,
				insert,
				delete,
	            createOKButton(true),
	            createCancelButton()
	            }
		);
	    bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
	    return bar;
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("instruccionDeEntrega".equals(property)){			
			JTextArea ta=new JTextArea(6,20);
			ta.setEditable(false);
			//ta.setEnabled(false);
			ta.setFont(ta.getFont().deriveFont(Font.BOLD));
			Bindings.bind(ta, ConverterFactory.createStringConverter(model.getModel(property), UIUtils.buildToStringFormat()));
			return ta;
		}else if("surtido".equals(property)){
			JSpinner s=com.luxsoft.siipap.swing.binding.Bindings
					.createDateSpinnerBinding(model.getModel(property));
			s.setEnabled(!model.isReadOnly());
			return s;
		}else if("surtidor".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		return super.createCustomComponent(property);
	}
		
	

	private JComponent buildGrid(){
		String[] propertyNames={"factura","ventaDet.renglon","clave","descripcion","ventaDet.cantidad","entregaAnterior","cantidad"};
		String[] columnLabels={"Fac","Rengl","Prod","Descripcion","Vendido","Entregado","Cantidad"};
		boolean[] edits={false,false,false,false,false,false,true};
		final TableFormat tf=GlazedLists.tableFormat(EntregaDet.class,propertyNames, columnLabels,edits);
		
		final EventTableModel tm=new EventTableModel(getController().getPartidas(),tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		
		
		
		grid.setSelectionModel(getController().getSelectionModel());
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.getColumnModel().getColumn(2).setPreferredWidth(250);
						
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		gridComponent.setPreferredSize(new Dimension(750,250));
		((JXTable)grid).setColumnControlVisible(true);
		
		
		return gridComponent;		
	}

	
	public void deletePartida(){
		getController().deletePartida();
	}
	public void insertPartida(){
		getController().insertarPartida();
	}

	

	
}
