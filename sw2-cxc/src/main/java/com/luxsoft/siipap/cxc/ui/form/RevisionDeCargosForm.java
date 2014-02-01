package com.luxsoft.siipap.cxc.ui.form;



import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * Forma para el mantenimiento de ciertas propiedades de un Cargo
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RevisionDeCargosForm extends AbstractForm{
	
	private HeaderPanel header;

	public RevisionDeCargosForm(RevisionDeCargosModel model) {
		super(model);
		
	}

	public RevisionDeCargosModel getRevisionModel(){
		return (RevisionDeCargosModel)getModel();
	}
	
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new BorderLayout());
		FormLayout layout=new FormLayout("60dlu,2dlu,140dlu,3dlu,60dlu,2dlu,140dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Del Catálogo");
		builder.append("Día Revisión",getControl("diaRevision"));
		builder.append("Día Cobro",getControl("diaCobro"));
		builder.append("Vencimiento",getControl("vencimientoFechaRevision"));
		/*Modificacion por ordenes de Direccion General (Lic. Jose Sanchez) 14/08/2013
		builder.append("Plazo",addReadOnly("plazo"));
		*/
		builder.append("Plazo",getControl("plazo"));
		
		builder.appendSeparator("");
		builder.append("Fecha Revisión",addReadOnly("fechaRevision"));
		builder.append("Cobrador",getControl("cobrador"));
		
		builder.append("Com Rec CxC",getControl("comentarioRecepcion"),5);
		builder.append("Com Rev",getControl("comentarioRevision"),5);
		
		panel.add(builder.getPanel(),BorderLayout.CENTER);
		panel.add(buildGrid(),BorderLayout.SOUTH);
		
		return panel;
	}
	
	 protected JComponent buildHeader() {
		 header=new HeaderPanel(getRevisionModel().getCliente().getNombre(),"A revision para: "+getRevisionModel().getValue("fechaRevision"));
		return header;
	 }
	
	private JComponent buildGrid(){
		JXTable grid=ComponentUtils.getStandardTable();
		EventList source=GlazedLists.eventList(getRevisionModel().getCuentas());		
		EventTableModel tm=new EventTableModel(source,getRevisionModel().createCXCTableFormat());
		grid.setModel(tm);
		grid.packAll();
		JComponent c=ComponentUtils.createTablePanel(grid);
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if(("diaCobro".equals(property)) || ("diaRevision".equals(property))){
			JComponent c=Binder.createDiaDeLaSemanaConNuloBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("cobrador".equals(property)){
			final List data=ServiceLocator2.getCXCManager().getCobradores();
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}

	public static List showForm(final List<Cargo> cxcs){
		if(!cxcs.isEmpty()){
			RevisionDeCargosModel model=new RevisionDeCargosModel(cxcs);
			final RevisionDeCargosForm form=new RevisionDeCargosForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				model.aplicarCambios();
				return model.getCuentas();
			}	
		}
		
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		final List<Cargo> cxcs=ServiceLocator2.getCXCManager().buscarCuentasPorCobrar(new Cliente("M030381",""), OrigenDeOperacion.CRE);
		
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				showForm(cxcs);
				showObject(cxcs);
			}
		});
		System.exit(0);
	}

}
