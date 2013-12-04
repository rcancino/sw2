package com.luxsoft.sw3.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeComprasPendientes;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.services.Services;


/**
 * Forma para la recepción de compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RecepcionDeCompraForm extends AbstractForm{
	
	private Header header;
	private JXTable grid;
	private EventSelectionModel selectionModel;

	public RecepcionDeCompraForm(final RecepcionDeCompraController controller) {
		super(controller);
		if(controller.getValue("id")==null){
			controller.getModel("compra").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					updateHeader();
				}
			});
		}
		setTitle("Recepción de compra "+model.getValue("sucursal"));
	}
	
	private RecepcionDeCompraController getController(){
		return (RecepcionDeCompraController)getModel();
	}

	@Override
	protected JComponent buildHeader() {
		header=new Header("","");
		updateHeader();
		return header.getHeader();
	}
	
	private void updateHeader(){
		if(getController().getCompra()!=null){
			header.setTitulo(getController().getCompra().getNombre());
			String pattern="Compra  {0}   \tFecha : {1,date,short}    \tEntrega (Aprox): {2,date,short}";
			String msg=MessageFormat.format(pattern,
					getController().getCompra().getFolio()
					,getController().getCompra().getFecha()
					,getController().getCompra().getEntrega()
					);
			header.setDescripcion(msg);
		}else{
			header.setTitulo("Seleccione una compra (Alt+C)");
			header.setDescripcion("");
		}
		
	}

	@Override
	protected JComponent buildFormPanel() {
		
		JPanel content=new JPanel(new VerticalLayout());
		final FormLayout layout=new FormLayout("p,2dlu,p, 3dlu,p,2dlu,p , 3dlu,p,2dlu,p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Compra",createSeleccionBtn());
		builder.nextLine();
		builder.append("Remisión",getControl("remision"));
		builder.append("Comentario",getControl("comentario"),5);
		content.add(builder.getPanel());
		//content.add(ButtonBarFactory.buildRightAlignedBar(createSeleccionBtn()));
		content.add(buildGridPanel());
		return content;
	}
	
	
	
	
	protected JComponent buildGridPanel(){
		grid=ComponentUtils.getStandardTable();
		String[] props={
				"renglon"
				,"sucursal.nombre"
				,"clave"
				,"descripcion"
				,"solicitado"
				,"compraDet.recibido"
				,"cantidad"
				,"pendiente"
				};
		String[] names={
				"Rngl"
				,"Sucursal"
				,"Producto"
				,"Descripción"
				,"Solicitado"
				,"Recibido"
				,"Por Recibir"
				,"Pendiente"
				};
		boolean[] edits={
				false
				,false
				,false
				,false
				,false
				,false
				,true
				,false};
		TableFormat tf=GlazedLists.tableFormat(EntradaPorCompra.class, props,names,edits);
		SortedList<EntradaPorCompra> sortedList=new SortedList<EntradaPorCompra>(
				getController().getPartidas()
				//,GlazedLists.beanPropertyComparator(EntradaPorCompra.class, "renglon")
				,new ComparadorDeRenglon()
				);
		//EventTableModel tm=new EventTableModel(getController().getPartidas(),tf);
		//selectionModel=new EventSelectionModel(getController().getPartidas());
		EventTableModel tm=new EventTableModel(sortedList,tf);
		selectionModel=new EventSelectionModel(sortedList);
		grid.setModel(tm);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		JComponent c=ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(650,450));
		return c;
	}
	
	private JButton createSeleccionBtn(){
		JButton btn=new JButton("Seleccionar Compra ");
		btn.setMnemonic('C');
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "seleccionarCompra"));
		btn.setEnabled(!model.isReadOnly());
		return btn;
	}
	
	public void seleccionarCompra(){
		Compra2 compra=SelectorDeComprasPendientes.seleccionar();
		model.setValue("compra", compra);
		getControl("remision").requestFocusInWindow();
		grid.packAll();
	}
	
	private class ComparadorDeRenglon implements Comparator<EntradaPorCompra>{
		public int compare(EntradaPorCompra e1, EntradaPorCompra e2) {
			if(e2.getRenglon()==0){
				return -1;
			}
			return e1.getRenglon()-e2.getRenglon();
		}
		
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
				RecepcionDeCompraController controller=new RecepcionDeCompraController();
				RecepcionDeCompraForm form=new RecepcionDeCompraForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					showObject(controller.getBaseBean());
					Services.getInstance().getComprasManager().registrarRecepcion(controller.getRecepcion());
				}
				System.exit(0);
			}

		});
	} 

}
