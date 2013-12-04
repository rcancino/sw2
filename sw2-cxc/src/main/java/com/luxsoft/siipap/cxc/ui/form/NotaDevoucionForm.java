package com.luxsoft.siipap.cxc.ui.form;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeRMD;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;

public class NotaDevoucionForm extends AbstractForm{

	public NotaDevoucionForm(NotaDevolucionFormModel model) {
		super(model);
		setTitle("Nota de credito por devolución");
		//setModal(false);
	}
	
	public NotaDevolucionFormModel getNotaDevolucionModel(){
		return (NotaDevolucionFormModel)model;
	}
	
	
	HeaderPanel headerPanel;
	
	protected JComponent buildHeader() {
		String title=getNotaDevolucionModel().getDevoModel().getCliente();
		title=StringUtils.isBlank(title)?"SIN CLIENTE":title;
		headerPanel=new HeaderPanel(title,"Nota de credito por devolución de mercancía");
		return headerPanel;
	}
	
	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,7dlu,f:250dlu","t:p,2dlu,t:p");
		final PanelBuilder builder=new PanelBuilder(layout);
		final CellConstraints cc=new CellConstraints();
		builder.add(buildFacturaPanel(),cc.xy(1, 1));
		builder.add(buildDescuentosPanel(),cc.xy(3, 1));
		builder.add(buildGrid(),cc.xyw(1, 3,3));
		return builder.getPanel();
	}
	
	
	protected JComponent buildFacturaPanel() {
		FormLayout layout=new FormLayout(
				"p,2dlu,60dlu, 3dlu " +
				"p,2dlu,60dlu" 
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Factura");
		builder.append("Folio",addReadOnly("facNumero"));
		builder.append("P.N.",addReadOnly("precioNeto"));
		builder.append("Fecha",addReadOnly("facFecha"));
		builder.append("Vence",addReadOnly("facVto"));
		builder.append("Total",addReadOnly("facTotal"));
		builder.append("Saldo",addReadOnly("facSaldo"));
		
		builder.appendSeparator("Nota de Crédito");
		builder.append("Folio",getControl("folio"),true);
		builder.append("Fecha",getControl("fecha"));
		//builder.append("Cortes",getControl("cortes"));
		builder.append("Descuento1",addReadOnly("descuento1"));
		//builder.append("Descuento2",getControl("descuento2"));
		
		builder.append("Importe",addReadOnly("importe"),5);
		builder.append("Imp Cortes",getControl("impCortes"),5);
		builder.append("impuesto",addReadOnly("impuesto"),5);
		builder.append("total",addReadOnly("total"),5);
		
		return builder.getPanel();
	}
	
	protected JComponent buildDescuentosPanel() {
		FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.1), 3dlu " +
				"p,2dlu,p:g(.9)" 
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Descuentos y Cortes");
		builder.append("Descuentos",addReadOnly("facDescuentos"),5);
		builder.append("Bonificaciones",addReadOnly("facBonificaciones"),5);
		builder.append("Devoluciones",addReadOnly("facDevoluciones"),5);
		builder.append("Cortes",addReadOnly("facCortes"));
		builder.append("P.Corte",addReadOnly("facPrecioCortes"));
		builder.append("Imp Cortes",addReadOnly("facImporteCortes"),5);
		builder.appendSeparator("RMD");
		builder.append("Folio",addReadOnly("rmdNumero"),true);
		builder.append("Fecha",addReadOnly("rmdFecha"));
		builder.append("Sucursal",addReadOnly("rmdSucursal"));
		builder.append("Comentario",addReadOnly("rmdComentario"),5);
		return builder.getPanel();
	}
	
	private JComponent buildGrid(){
		JXTable grid=ComponentUtils.getStandardTable();
		grid.setColumnControlVisible(false);
		String[] props={"devolucion.numero","renglon","devolucion.venta.sucursal.nombre","fecha","clave","descripcion","producto.kilos","cantidad","precio","ventaDet.descuento","importeNeto"};
		String[] labels={"Rmd","Ren","Sucursal","Fecha","Articulo","Descripcion","Kgs","Cantidad","Precio","Descuento","Importe"};
		TableFormat<DevolucionDeVenta> tf=GlazedLists.tableFormat(DevolucionDeVenta.class,props, labels);
		final EventTableModel tm=new EventTableModel(getNotaDevolucionModel().getPartidas(),tf);
		grid.setModel(tm);
		JComponent c=ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(200,200));
		return c;
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if(property.startsWith("descuento")){
			return Bindings.createDescuentoEstandarBinding(model.getModel(property));
		}else if("importe".equals(property)){
			JTextField tf=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			tf.setHorizontalAlignment(JTextField.RIGHT);
			return tf;
		}else if("impCortes".equals(property)){
			JTextField tf=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			tf.setHorizontalAlignment(JTextField.RIGHT);
			return tf;
		}else if("impuesto".equals(property)){
			JTextField tf=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			tf.setHorizontalAlignment(JTextField.RIGHT);
			return tf;
		}else if("total".equals(property)){
			JTextField tf= Binder.createBigDecimalForMonyBinding(model.getModel(property));
			tf.setHorizontalAlignment(JTextField.RIGHT);
			return tf;
		}
		return super.createCustomComponent(property);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
 
			public void run() {
				SWExtUIManager.setup();
				Devolucion dev=SelectorDeRMD.seleccionarAplicables().get(0);
				NotaDevolucionFormModel model=new NotaDevolucionFormModel(dev);
				NotaDevoucionForm form=new NotaDevoucionForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					List<NotaDeCreditoDevolucion> notas=model.procesar();
					for(NotaDeCreditoDevolucion n:notas){
						//ServiceLocator2.getCXCManager().salvarNota(n);
						showObject(n);
					}
				}
				System.exit(0);
			}
			
		});
		
	}

	

}
