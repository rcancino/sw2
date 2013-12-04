package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.ui.model.FichasFormModel;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * Forma para la generacion de fichas de deposito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class GeneracionDeFichas extends AbstractForm{
	
	
	public GeneracionDeFichas(FichasFormModel model) {
		super(model);
	}
	
	private FichasFormModel getFichasModel(){
		return (FichasFormModel)getModel();
	}

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new BorderLayout(2,5));
		final FormLayout layout=new FormLayout(
				"p,2dlu,110dlu, 3dlu, p,2dlu,110dlu"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Sucursal",addReadOnly("sucursal"));
		
		builder.append("Fichas",addReadOnly("depositos"));
		builder.append("Destino",getControl("cuenta"));
		
		builder.append("Cheque",addReadOnly("cheque"));
		builder.append("Efectivo",addReadOnly("efectivo"));
		builder.append("Total",addReadOnly("total"),true);
		builder.append("Comentario",getControl("comentario"),5);
		
		panel.add(builder.getPanel(),BorderLayout.NORTH);
		panel.add(buildGrid(),BorderLayout.CENTER);
		return panel;
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}else if("cuenta".equals(property)){
			return Bindings.createCuentasBinding(model.getModel(property));
		}else if("sucursal".equals(property)){
			return Bindings.createSucursalesBinding(model.getModel(property));
		}else if("cheque".equals(property) || "efectivo".equals(property) ||"total".equals(property)){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(model.getModel(property), NumberFormat.getCurrencyInstance());
			tf.setHorizontalAlignment(JFormattedTextField.RIGHT);
			return tf;
		}
		return super.createCustomComponent(property);
	}



	private JXTable grid;
	
	private JComponent buildGrid(){
		 grid=ComponentUtils.getStandardTable();
		 
		 final SortedList sortedList=new SortedList(getFichasModel().getPartidas(),null);
		 final String[] props={"ficha.folio","banco","cheque","efectivo","pago.nombre","pago.fecha","pago.info"};
		 final String[] names={"Ficha","Banco","Cheque","Efectivo","Cliente","Fecha(Pag)","Info"};
		 final TableFormat tf=GlazedLists.tableFormat(FichaDet.class,props, names);
		 final EventTableModel tm=new EventTableModel(sortedList,tf);
		 grid.setModel(tm);
		 grid.packAll();
		 JComponent c=ComponentUtils.createTablePanel(grid);
		 TableComparatorChooser.install(grid,sortedList,TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		 c.setPreferredSize(new Dimension(600,500));
		 return c;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				FichasFormModel model=new FichasFormModel();
				model.generarFichas(testData());
				model.setValue("cuenta", ServiceLocator2.getConfiguracion().getCuentaPreferencial());
				GeneracionDeFichas form=new GeneracionDeFichas(model);
				System.out.println(model.getValue("cheque").getClass().getName());
				form.open();
				
				//form.getFichasModel().getFichasModel().setCuenta(ServiceLocator2.getConfiguracion().getCuentaPreferencial());
				if(!form.hasBeenCanceled()){
					model.comit();
					for(Ficha f:model.getFichas()){
						ServiceLocator2.getHibernateTemplate().merge(f);
					}
					
				}
				System.exit(0);
			}
			
		});
	}
	
	
	public static List<Pago> testData(){
		return ServiceLocator2.getHibernateTemplate().find("from PagoConCheque p where p.cliente.clave=?","I020376");
	}

}
