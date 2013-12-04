package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturas;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para la generacion de devoluciones
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DevolucionForm extends AbstractForm{
	
	private JXTable grid;
	private Header header;

	public DevolucionForm(DevolucionController controller) {
		super(controller);
		String pattern="Devolución de venta     [{0}]";
		setTitle(MessageFormat.format(pattern, controller.getSucursal()));
	}
	
	private DevolucionController getController(){
		return (DevolucionController)getModel();
	}
	
	protected JComponent buildHeader(){
		header=new Header("Seleccione un cliente ","");
		return header.getHeader();
	}

	@Override
	protected JComponent buildFormPanel() {
		final JPanel panel=new JPanel(new VerticalLayout(3));
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,p ,3dlu," +
				"p,2dlu,p ,3dlu,p,2dlu,p:g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append(buildClienteControl(),buildDocumentoControl());
		//builder.append("Documento",buildDocumentoControl());
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());
		panel.add(buildGridPanel());
		panel.add(buildButtonBar());
		return panel;
	}
	
	private JTextField clave=new JTextField(10);
	private JTextField documento=new JTextField(8);
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("clave".equals(property)){
			return clave;
		}else if("documento".equals(property)){
			return documento;
		}
		return super.createCustomComponent(property);
	}

	private JComponent buildGridPanel(){
		grid=ComponentUtils.getStandardTable();
		boolean[] edits={false,false,false,true};
		TableFormat tf=GlazedLists.tableFormat(
			DevolucionDeVenta.class
			,new String[]{"clave","descripcion","ventaDet.cantidad","cantidad"}
			,new String[]{"Producto","Desc","Vendido","Devolucion"}
			,edits
		);
		
		EventTableModel tm=new EventTableModel(getController().getPartidas(),tf);
		grid.setModel(tm);
		grid.setSelectionModel(getController().getSelectionModel());
		grid.setSelectionMode(ListSelection.SINGLE_SELECTION);
		JComponent c=ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(650,400));
		return c;
	}
	
	private JComponent buildButtonBar(){
		JButton insert=new JButton(CommandUtils.createInsertAction(this, "insert"));
		insert.setMnemonic('I');
		return ButtonBarFactory.buildRightAlignedBar(
				insert
				);
				
	}
	
	private JComponent buildClienteControl(){
		FormLayout layout=new FormLayout("p,2dlu,p","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		JButton bt1=new JButton("Cliente");
		bt1.addActionListener(EventHandler.create(ActionListener.class, this, "seleccionarCliente"));
		bt1.setMnemonic('C');
		bt1.setFocusable(false);
		builder.append(getControl("clave"),bt1);
		return bt1;
	}
	
	private JComponent buildDocumentoControl(){
		FormLayout layout=new FormLayout("p:g,2dlu,p","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		JButton bt1=new JButton("Documento",ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
		bt1.addActionListener(EventHandler.create(ActionListener.class, this, "seleccionarDocumento"));
		bt1.setFocusable(false);
		bt1.setMnemonic('D');
		builder.append(getControl("documento"),bt1);
		return bt1;
	}
	
	public void seleccionarCliente(){
		Cliente c=SelectorDeClientes.seleccionar(this);
		getController().setCliente(c);
		if(c!=null){
			clave.setText(c.getNombre());
			documento.requestFocusInWindow();
			header.setTitulo(c.getNombre());
			header.setDescripcion("Seleccione el documento");
		}
		else{
			header.setTitulo("Seleccione un cliente");
		}
			
	}
	
	public void seleccionarDocumento(){
		if(getController().getCliente()!=null){
			//Venta v=SelectorDeFacturas.seleccionar(getController().getCliente());
			
			
			
			SelectorDeFacturas selector=new SelectorDeFacturas(){
				@Override
				protected List<Venta> getData() {
					Object[] params=new Object[]{
							//getController().getSucursal().getId()
							Services.getInstance().getConfiguracion().getSucursal().getId()
							,getController().getCliente().getClave()
							,periodo.getFechaInicial()
							,periodo.getFechaFinal()
							,getController().getCliente().getClave()
							};
					String hql="from Venta v " +
							" where v.sucursal.id=? and v.cliente.clave=? " +
							" and v.fecha between ? and ? " +
							" and v.id not in(select d.venta.id from Devolucion " +
							"					d where d.venta.cliente.clave=?)";
					return Services.getInstance().getHibernateTemplate()
						.find(hql, params);
				}
			};
			
			selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
			selector.setCliente(getController().getCliente());
			selector.open();
			if(!selector.hasBeenCanceled()){
				Venta v= selector.getSelected();
				v= Services.getInstance().getFacturasManager().buscarVentaInicializada(v.getId());
				getController().setValue("venta", v);
				
			}		
			actualizarDescripcion();
			
		}
	}
	
	private void actualizarDescripcion(){
		Venta v=getController().getDevolucion().getVenta();
		if(v!=null){
			String pattern="Factura/Dcto: {0}\t Fecha: {1,date,short}\nTotal:{2}";
			String msg=MessageFormat.format(pattern, v.getDocumento(),v.getFecha(),v.getTotalCM());
			header.setDescripcion(msg);
		}else{
			header.setDescripcion("Seleccione un documento");
		}
	}
	
	public static Devolucion showForm(){
		DevolucionController controller=new DevolucionController();
		DevolucionForm form=new DevolucionForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			return controller.persist();
		}
		return null;
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
				//showObject(showForm());
				POSDBUtils.whereWeAre();
				Devolucion d=showForm();
				showObject(d);
				System.exit(0);
			}

		});
	}

}
