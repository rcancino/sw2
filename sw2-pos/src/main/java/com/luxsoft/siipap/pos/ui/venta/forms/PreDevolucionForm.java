package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.PreDevolucion;
import com.luxsoft.siipap.ventas.model.PreDevolucionDet;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para la generacion de devoluciones
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PreDevolucionForm extends AbstractForm{
	
	private JXTable grid;
	private Header header;

	public PreDevolucionForm(PreDevolucionController controller) {
		super(controller);
		String pattern="Pre devolución de venta     [{0}]";
		setTitle(MessageFormat.format(pattern, controller.getPreDevolucion().getSucursal()));
	}
	
	private PreDevolucionController getController(){
		return (PreDevolucionController)getModel();
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
		builder.append("Chofer",getControl("chofer"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());
		panel.add(buildGridPanel());
		//panel.add(buildButtonBar());
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
		}else if("chofer".equals(property)){
			JComponent box=buildChoferesBox(model.getModel("chofer"));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}
	
	private JComponent buildChoferesBox(final ValueModel vm){
		EventList<String> list=GlazedLists.eventList(Services.getInstance().getJdbcTemplate().queryForList("select NOMBRE from SX_CHOFERES",String.class));
		final JComboBox box = new JComboBox();		
		AutoCompleteSupport support = AutoCompleteSupport.install(box,list, null);
		support.setFilterMode(TextMatcherEditor.CONTAINS);
		support.setStrict(false);
		final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
		model.addListDataListener(new Bindings.WeakListDataListener(vm));
		box.setSelectedItem(vm.getValue());
		
		return box;
	}

	private JComponent buildGridPanel(){
		grid=ComponentUtils.getStandardTable();
		boolean[] edits={false,false,false,false,false,false,true};
		TableFormat tf=GlazedLists.tableFormat(
			PreDevolucionDet.class
			,new String[]{
				"clave"
				,"descripcion"
				,"ventaDet.cantidad"
				,"ventaDet.devueltas"
				,"devueltas"
				,"disponible"
				,"cantidad"}
			,new String[]{"Producto","Desc","Vendido","Devueltas","Pre Dev","Disp","Devolucion"}
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
		getController().getPreDevolucion().setCliente(c);
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
		getController().seleccionarVenta();
	}
	
	
	public static PreDevolucion showForm(){
		PreDevolucionController controller=new PreDevolucionController();
		PreDevolucionForm form=new PreDevolucionForm(controller);
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
				PreDevolucion d=showForm();
				showObject(d);
				System.exit(0);
			}
		});
	}

}
