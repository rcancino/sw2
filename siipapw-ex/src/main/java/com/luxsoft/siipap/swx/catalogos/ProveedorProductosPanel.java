package com.luxsoft.siipap.swx.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoPorProveedor;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swx.binding.ProductoControl;



/**
 * 
 * @author Ruben Cancino
 *
 */
public class ProveedorProductosPanel extends JPanel{
	
	private final PresentationModel model;
	private final PresentationModel productoModel;
	private final ValueHolder beanChannell;
	
	private final EventList<ProductoPorProveedor> partidas;
	private EventSelectionModel selectionModel;
	private ProductoControl prodControl;
	private JTextField claveField;
	private JTextField codigoField;
	private JTextField descField;
	
	
	public ProveedorProductosPanel(final PresentationModel model){
		this.model=model;
		partidas=GlazedLists.eventList(new BasicEventList<ProductoPorProveedor>());
		beanChannell=new ValueHolder(null,true);
		model.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN,new BeanHandler());
		productoModel =new PresentationModel(beanChannell);
		updateGrid();
		productoModel.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BUFFERING,new CommitHandler());		
		init();
		eneableForm(false);
	}
	
	private Proveedor getProveedor(){
		return (Proveedor)model.getBean();
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(getProveedor().getProductos());
	}
	
	private void init(){		
		prodControl=new ProductoControl(productoModel.getModel("producto"));
		prodControl.setEnabled(false);
		claveField=BasicComponentFactory.createTextField(productoModel.getComponentModel("claveProv"));
		codigoField=BasicComponentFactory.createTextField(productoModel.getComponentModel("codigoProv"));
		descField=BasicComponentFactory.createTextField(productoModel.getComponentModel("descripcionProv"));
		
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);
	}
	
	private JComponent buildFormPanel(){
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,f:p:g" 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);	
		builder.setDefaultDialogBorder();
		builder.append("Producto",createProductosControl());
		builder.appendSeparator("Proveedor");
		builder.append("Clave",claveField);
		builder.append("Codigo",codigoField);
		builder.append("Descripción",descField);
		
		return builder.getPanel();
		
	}
	
	@SuppressWarnings("unchecked")
	private JComponent buildGridPanel(){
		final TableFormat<ProductoPorProveedor> tf=GlazedLists.tableFormat(ProductoPorProveedor.class
			,new String[]{"producto.clave","claveProv","codigoProv","descripcionProv"}
			,new String[]{"Producto","Clave Prov","Cod Prov","DescripcionProv"}
		);
		
		final EventTableModel<ProductoPorProveedor> tm=new EventTableModel<ProductoPorProveedor>(partidas,tf);
		final JTable table=new JTable(tm);
		selectionModel=new EventSelectionModel(partidas);
		table.setSelectionModel(selectionModel);
		final JScrollPane sp=new JScrollPane(table);
		sp.setPreferredSize(new Dimension(200,250));
		return sp;
	}
	
	private JComponent createProductosControl(){
		
		DefaultFormBuilder builder=new DefaultFormBuilder(new FormLayout("p:g,2dlu,p",""));	
		
		JButton bt1=new JButton(getLookupAction());
		bt1.setFocusable(false);
		bt1.setMnemonic('F');
		
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList<Producto> source=GlazedLists.eventList(ServiceLocator2.getProductoManager().buscarProductosActivos());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        support.setSelectsTextOnFocusGain(true);
        //support.setStrict(true);
        box.getEditor().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				if(sel instanceof Producto){
					productoModel.setValue("producto", sel);
				}else if(sel instanceof String){
					String clave=(String)sel;
					if(!StringUtils.isBlank(clave)){
						Producto p=ServiceLocator2.getProductoManager().buscarPorClave(clave);
						if(p!=null)
							productoModel.setValue("producto", p);
					}					
				}
			}
        });        
        if(productoModel.getValue("producto")!=null)
        	box.setSelectedItem(productoModel.getValue("producto"));
		builder.append(box);
		builder.append(bt1);
		return builder.getPanel();
	}
	
	private Action lookupAction;
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"buscarProducto");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
		}
		return lookupAction;
	}	
	
	private JButton[] buttons;
	
	private JButton[] getButtons(){
		if(buttons==null){
			buttons=new JButton[5];
			buttons[0]=buildAddButton();
			buttons[1]=buildRemoveButton();
			buttons[2]=buildEdditButton();
			buttons[3]=buildResetButton();
			buttons[4]=buildOkButton();
		}
		
		return buttons;
	}
	
	public void setEnabled(boolean val){
		super.setEnabled(val);
		for(JButton b:buttons){
			b.setEnabled(val);
		}
	}
	
	private void eneableForm(boolean enabled){		
		prodControl.setEnabled(enabled);
		claveField.setEnabled(enabled);
		codigoField.setEnabled(enabled);
		descField.setEnabled(enabled);
		if(!enabled){
			claveField.setText("");
			codigoField.setText("");
			descField.setText("");
		}
		
	}
	
	public void nuevo(){
		ProductoPorProveedor c=new ProductoPorProveedor();
		//productoModel.setBean(c);
		beanChannell.setValue(c);
		eneableForm(true);
		//claveField.requestFocusInWindow();
	}
	public void commit(){
		productoModel.triggerCommit();
		ProductoPorProveedor c=(ProductoPorProveedor)productoModel.getBean();
		if(c.getProveedor()==null){
			getProveedor().agregarProducto(c);
		}		
		updateGrid();
		eneableForm(false);
		
	}
	
	public void reset(){
		productoModel.resetChanged();
		eneableForm(false);
	}
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			ProductoPorProveedor selected=(ProductoPorProveedor)selectionModel
				.getSelected().get(0);
			productoModel.setBean(selected);
			eneableForm(true);
			claveField.requestFocusInWindow();
		}
	}
	
	public void eliminar(){
		final EventList selection=selectionModel.getSelected();
		boolean update=false;
		for(int i=0;i<selection.size();i++){
			ProductoPorProveedor c=partidas.get(i);
			boolean res=getProveedor().removerProducto(c);
			if(!update)
				update=res;
		}
		if(update)
			updateGrid();
	}
	
	private JButton okButton=null;

	private JButton buildOkButton() {
		if(okButton==null){
			okButton=new JButton("Ok");
			okButton.addActionListener(EventHandler.create(ActionListener.class, this, "commit"));
			okButton.setEnabled(false);
		}
		
		return okButton;
	}

	private JButton buildResetButton() {
		JButton reset=new JButton("Des-Hacer");
		reset.addActionListener(EventHandler.create(ActionListener.class, this, "reset"));
		return reset;
	}

	private JButton buildEdditButton() {
		JButton edit=new JButton("Editar");
		edit.addActionListener(EventHandler.create(ActionListener.class, this, "edit"));
		return edit;
	}

	private JButton buildRemoveButton() {
		JButton delete=new JButton("Eliminar");
		delete.addActionListener(EventHandler.create(ActionListener.class, this, "eliminar"));
		return delete;
	}

	private JButton buildAddButton() {
		JButton add=new JButton("Nuevo");
		add.addActionListener(EventHandler.create(ActionListener.class, this, "nuevo"));
		return add;
	}
	
	private class CommitHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			Boolean res=(Boolean)evt.getNewValue();
			okButton.setEnabled(res);			
			if(!res){
				okButton.requestFocusInWindow();
				eneableForm(res);
			}
		}
		
	}
	
	/**
	 * Actualiza los comentarios si cambia el producto
	 * 
	 * @author RUBEN
	 *
	 */
	private class BeanHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			updateGrid();
			
		}
		
	}

}
