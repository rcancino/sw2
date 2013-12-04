package com.luxsoft.siipap.cxc.ui.form;


import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.ui.AbonoPanel;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeCXC;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

public class NotaDeCreditoForm extends AbonoPanel{
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;

	public NotaDeCreditoForm(NotaDeCreditoFormModel model) {
		super(model);
		setTitle("Nota de Credito");
	}
	
	public NotaDeCreditoFormModel getNotaModel(){
		return (NotaDeCreditoFormModel)model;
	}
	
	public JComponent buildFormPanel(){
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildTolbar(),BorderLayout.NORTH);
		panel.add(buildDocPanel(),BorderLayout.CENTER);
		panel.add(buildTotalesPanel(),BorderLayout.EAST);
		panel.add(buildAplicacionesPanel(),BorderLayout.SOUTH);
		return panel;
	}
	
	
	protected JComponent buildDocPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,110dlu:g(.5), 2dlu," +
				"p,2dlu,110dlu:g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Generales");
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
		}
		
		getControl("moneda").setEnabled(getAbonoModel().isMultiMonedaPermitido());
		getControl("tc").setEnabled(getAbonoModel().isMultiMonedaPermitido());
		
		builder.append("Cliente",getControl("cliente"),5);
		builder.nextLine();
		builder.append("Fecha",getControl("fecha"));
		builder.append("Sucursal",getControl("sucursal"));
		builder.append("Moneda",getControl("moneda"));
		builder.append("T.C.",getControl("tc"));
		builder.append("Comentario",getControl("comentario"),5);
		builder.setDefaultDialogBorder();		
		instalarAntesDeComentario(builder);
		builder.nextLine();		
		return builder.getPanel();		
	}
	
	protected JComponent buildTotalesPanel(){
		final FormLayout layout=new FormLayout(
				"p,2dlu,80dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Importes");
		builder.append("Folio",getControl("folio"));
		builder.append("Descuento",getControl("descuento"));		
		builder.append("Importe",getControl("importe"));		
		builder.append("Impuesto",getControl("impuesto"));
		builder.append("Total",getControl("total"));
		builder.setDefaultDialogBorder();
		return builder.getPanel();
	}
	
	protected JComponent buildTolbar(){
		JToolBar bar=new JToolBar();
		bar.add(CommandUtils.createInsertAction(this, "insertar"));
		bar.add(CommandUtils.createDeleteAction(this, "eliminar"));
		bar.add(CommandUtils.createEditAction(this, "modificar"));
		bar.add(CommandUtils.createPrintAction(this, "imprimir"));
		return bar;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("descuento".equals(property)){			
			return Bindings.createDescuentoEstandarBindingBase1(model.getModel(property));
		}else
			return super.createCustomComponent(property);
		
	}
	
	public void insertar(){
		List<Cargo> cuentas=SelectorDeCXC.seleccionar(getNotaModel().getNota().getCliente(), origen);
		if(cuentas!=null){
			getNotaModel().aplicar(cuentas);
			grid.packAll();
		}
	}
	
	public void eliminar(){
		if(!selectionModel.isSelectionEmpty()){
			List data=new ArrayList(selectionModel.getSelected().size());
			data.addAll(selectionModel.getSelected());
			getNotaModel().eliminarAplicaciones(data);
		}
	}
	
	public void modificar(){
		System.out.println("Modificando aplicaciones...");
	}
	public void imprimir(){
		System.out.println("Imprimir Nota");
	}
	
	protected JXTable grid;
	protected EventSelectionModel selectionModel;
	
	protected JComponent buildAplicacionesPanel(){
		grid=ComponentUtils.getStandardTable();
		//grid.setColumnControlVisible(false);
		final SortedList sorted=new SortedList(getNotaModel().getAplicaciones(),null);
		final EventTableModel tm=new EventTableModel(sorted,getNotaModel().createTableformat());
		grid.setModel(tm);
		selectionModel =new EventSelectionModel(sorted);
		grid.setSelectionModel(selectionModel);
		return ComponentUtils.createTablePanel(grid);
	}
	
	
	
	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	public static NotaDeCredito showForm(final NotaDeCreditoFormModel model,OrigenDeOperacion origen){
		final NotaDeCreditoForm form=new NotaDeCreditoForm(model);
		form.setOrigen(origen);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getNota();
		}
		return null;
	}
	
	

}
