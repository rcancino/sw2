package com.luxsoft.sw3.impap.ui.form;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeCOMS;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;

/**
 * Analisis de factura para la generación de la cuenta por pagar
 * 
 * @author Ruben Cancino
 * 
 */
public class AnalisisDeEntradaForm extends AbstractMasterDetailForm {

	private HeaderPanel header;

	public AnalisisDeEntradaForm(AnalisisDeEntradaModel model) {
		super(model);
		
		setTitle("Analisis de factura");
		model.getModel("proveedor").addValueChangeListener(
				new ProveedorHandler());
	}
	
	private AnalisisDeEntradaModel getAnalisisModel(){
		return (AnalisisDeEntradaModel)model;
	}

	protected JComponent buildHeader() {
		header = new HeaderPanel(HEADER_TITLE, "");
		return header;
	}
	
	//private JTextField bonificacionField;
	//private JButton bonificacionLookup;

	@Override
	protected JComponent buildMasterForm() {
		
		FormLayout layout = new FormLayout(
				"  60dlu,2dlu,70dlu, 3dlu,"
				+ "60dlu,2dlu,70dlu, 3dlu," 
				+ "60dlu,2dlu,70dlu"
				, "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Documento");
		if (getModel().getValue("id") != null) {
			builder.append("Id", addReadOnly("id"));
			builder.append("Saldo", addReadOnly("saldo"));
			builder.append("Autorización", addReadOnly("autorizacion"));
			builder.append("Proveedor", addReadOnly("proveedor"), 9);
		} else {
			builder.append("Proveedor", getControl("proveedor"), 9);
		}
		builder.append("Factura", getControl("documento"));
		builder.append("Fecha", getControl("fecha"));
		builder.append("Vencimiento", addReadOnly("vencimiento"));

		builder.append("Desc Financiero", addReadOnly("descuentoFinanciero"));
		builder.append("Vto D.F", addReadOnly("vencimientoDF"));
		builder.append("Moneda", getControl("moneda"));

		builder.append("T.C.", getControl("tc"));
		builder.append("Comentario", getControl("comentario"), 5);
		
		/*bonificacionField=BasicComponentFactory.createLongField(getAnalisisModel().reciboHolder);
		bonificacionField.setEnabled(false);
		bonificacionLookup=new JButton(ResourcesUtils.getIconFromResource("images2/SEARCH.PNG"));
		bonificacionLookup.setEnabled(!model.isReadOnly());
		bonificacionLookup.addActionListener(EventHandler.create(ActionListener.class, this, "asignarBonificacion"));
		builder.append("Bonificación", bonificacionField, bonificacionLookup);*/
		
		builder.nextLine();
		
		builder.appendSeparator("Analisis");
		builder.append("Importe",addReadOnly("importeAnalizado"));
		builder.append("Impuesto",addReadOnly("impuestoAnalizado"));
		builder.append("Total",addReadOnly("totalAnalizado"));
		
		FormLayout layout2=new FormLayout(
				"p,2dlu,60dlu, 2dlu ,p,2dlu,20dlu"
				,"");
		DefaultFormBuilder builder2=new DefaultFormBuilder(layout2);
		builder.setDefaultDialogBorder();
		builder2.appendSeparator("Totales");
		builder2.append("Importe",getControl("importe"),true);
		builder2.append("Cargos",getControl("cargos"),true);
		builder2.append("Impuesto",addReadOnly("impuesto"),true);		
		builder2.append("Flete",getControl("flete"),true);
		builder2.append("Imp Flete",addReadOnly("impuestoflete"),true);
		builder2.append("Ret.Imp Flete",addReadOnly("retencionflete"));
		builder2.append("%",addReadOnly("retencionfletePor"),true);
		builder2.append("Total",addReadOnly("total"),true);
		
		FormLayout layout3=new FormLayout("p,2dlu,p","p");
		PanelBuilder builder3=new PanelBuilder(layout3);
		CellConstraints cc=new CellConstraints();
		builder3.add(builder.getPanel(),cc.xy(1, 1));
		builder3.add(builder2.getPanel(),cc.xy(3, 1));
		
		return builder3.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}else if(property.startsWith("descuento")){
			JComponent c=Bindings.createDescuentoEstandarBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if(property.endsWith("Analizado")){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(model.getModel(property), NumberFormat.getCurrencyInstance());
			tf.setHorizontalAlignment(JFormattedTextField.RIGHT);
			return tf;
		}else if("retencionfletePor".equals(property)){
			JComponent c=Binder.createDescuentoBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}/*else if("total".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(getAnalisisModel().getTotalHolder());
			c.setEnabled(!model.isReadOnly());	
			c.setEnabled(getAnalisisModel().getTotalHolder().isEnabled());
			return c;
		}*/else if("tc".equals(property)){
			JComponent c=Bindings.createDoubleBinding(getAnalisisModel().getModel("tc"),6, 2);
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		else return super.createCustomComponent(property);
	}
	
	

	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("proveedor") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2
					.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists
					//.textFilterator(new String[] { "clave" });
				.textFilterator(new String[] { "clave", "nombre", "rfc" });
			AutoCompleteSupport support = AutoCompleteSupport.install(box,
					source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			final EventComboBoxModel model = (EventComboBoxModel) box
					.getModel();
			model.addListDataListener(new Bindings.WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
			return box;
		} else {
			String prov = ((Proveedor) vm.getValue()).getNombreRazon();
			JLabel label = new JLabel(prov);
			return label;
		}
	}

	@Override
	protected TableFormat getTableFormat() {
		String[] props = {
				"entrada.compra", "entrada.fechaCompra", "entrada.remision","entrada.fechaRemision"
				,"entrada.clave","entrada.descripcion","entrada.unidad.unidad","cantidad"
				,"precio", "costo", "importe" 
				};
		String[] labels = {
				"Compra", "F.Compra", "Remisión","F.Remisión"
				,"Producto","Descripción","U","Cant(Analizada)"
				,"Precio", "Costo", "Importe"
				};
		return GlazedLists.tableFormat(CXPAnalisisDet.class, props, labels);
	}
	
	protected void configDetailScrollPanel(final JComponent sp){
		sp.setPreferredSize(new Dimension(850,200));
	}
	
	protected Action[] getDetallesActions(){
		return new Action[]{
				getInsertAction()
				,getDeleteAction()
				,getEditAction()
				};
	}
	
	public void insertPartida(){
		Proveedor p=(Proveedor)model.getValue("proveedor");
		if(p!=null){
			List<EntradaPorCompra> entradas=SelectorDeCOMS.buscarEntradas(p);
			if(!entradas.isEmpty()){
				getAnalisisModel().procesarEntradas(entradas);
				grid.packAll();
			}
			
		}
	}

	@Override
	protected void doEdit(Object obj) {
		final CXPAnalisisDet source=(CXPAnalisisDet)obj;
		final AnalisisDeEntradaDetForm form=new AnalisisDeEntradaDetForm(source);
		form.open();
		if(!form.hasBeenCanceled()){
			form.commit();
			getAnalisisModel().afterEdit(source);
			
		}
	}
	
	/*public void asignarBonificacion(){		
		MessageUtils.showMessage("EN CONSTRUCCION, ESTA OPCION ES PARA ASIGNAR UNA NOTA DE BONIFICACION" +
				"\n EXISTENTE AL MOMENTO DE LA GENERACION DEL  ANALISIS", "Analisis de facturas");
		
	}*/


	private static final String HEADER_TITLE = "Seleccione un proveedor";

	private class ProveedorHandler implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			Proveedor p = (Proveedor) evt.getNewValue();
			if (p != null) {
				header.setTitle(p.getNombreRazon());
				header.setDescription("Analisis de factura para la generación de la cuenta por pagar");
			} else {
				header.setTitle(HEADER_TITLE);
				header.setDescription("");
			}
			/*getControl("total").setEnabled(!p.getCobraFlete());
			//((JTextField)getControl("total")).setEditable(!p.getCobraFlete());
			
			getControl("flete").setEnabled(p.getCobraFlete());
			((JTextField)getControl("flete")).setEditable(p.getCobraFlete());*/
		}
	}
}
