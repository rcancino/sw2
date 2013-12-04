package com.luxsoft.sw3.bi;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.ventas.CheckPlusReferenciaBancaria;



public class CheckPlusReferenciasPanel extends JPanel implements ListEventListener{
	
	private final CheckplusClienteFormModel model;
	
	private EventList partidas;
	private EventSelectionModel selectionModel;
	
	public CheckPlusReferenciasPanel(CheckplusClienteFormModel model) {
		this.model=model;
		init();
	}
	
	private void init(){
		setLayout(new BorderLayout());
		JToolBar toolbar=new JToolBar();
		toolbar.add(CommandUtils.createInsertAction(this, "insertar"));
		toolbar.add(CommandUtils.createDeleteAction(this, "eliminar"));
		toolbar.add(CommandUtils.createViewAction(this, "select"));
		add(toolbar,BorderLayout.NORTH);
		
		partidas=GlazedLists.eventList(new BasicEventList());
		partidas.addAll(model.getCliente().getReferenciasBancarias());
		partidas.addListEventListener(this);
		
		final TableFormat tf=GlazedLists.tableFormat(CheckPlusReferenciaBancaria.class
			, new String[]{"banco","sucursal","numeroDeCuenta"}
			,new String[]{"Banco","Sucursal","Número"}
		);
		final EventTableModel tm=new EventTableModel(partidas,tf);
		final JTable table=new JTable(tm);
		selectionModel=new EventSelectionModel(partidas);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				select();
			}
		});
		table.setSelectionModel(selectionModel);
		final JScrollPane sp=new JScrollPane(table);
		//sp.setPreferredSize(new Dimension(200,250));
		add(sp,BorderLayout.CENTER);
	}
	
	public void select(){
		CheckPlusReferenciaBancaria ref=getSelected();
		if(ref!=null){
			
		}
	}
	
	private CheckPlusReferenciaBancaria getSelected(){
		Object obj=selectionModel.getSelected().isEmpty()?null:selectionModel.getSelected().get(0);
		return (CheckPlusReferenciaBancaria)obj;
	}
	
	public void insertar(){
		DefaultFormModel formModel=new DefaultFormModel(Bean.proxy(CheckPlusReferenciaBancaria.class));
		final ReferenciaForm form=new ReferenciaForm(formModel);
		form.open();
		if(!form.hasBeenCanceled()){
			CheckPlusReferenciaBancaria target=new CheckPlusReferenciaBancaria();
			Bean.normalizar(formModel.getBaseBean(), target, new String[0]);
			if(model.getCliente().getId()==null)
				model.getCliente().agregarReferencia(target);
			else{
				target.setCliente(model.getCliente());
				ServiceLocator2.getHibernateTemplate().saveOrUpdate(target);
			}
			partidas.add(target);
		}
	}
	
	public void eliminar(){
		CheckPlusReferenciaBancaria sel=getSelected();
		if(sel!=null){
			if(MessageUtils.showConfirmationMessage("Eliminar referencia bancaria: "+sel, "Cliente CheckPlus")){
				model.getCliente().eliminarReferencia(sel);
				partidas.remove(sel);
			}
		}
		
	}
	
	public void listChanged(ListEvent listChanges) {
		/*while(listChanges.next()){
			
		}*/
		
		if(listChanges.next()){
			switch (listChanges.getType()) {
			case ListEvent.INSERT:				
				break;
			case ListEvent.DELETE:
				//doListChange();
				break;
			case ListEvent.UPDATE:
				//doListUpdated(listChanges);
				break;
			default:
				break;
			}				
		}
		model.validate();
		
	}
	
	
	public static class ReferenciaForm extends AbstractForm{
		
		public ReferenciaForm(IFormModel model) {
			super(model);
		}
		
		@Override
		protected JComponent buildFormPanel() {
			final FormLayout layout=new FormLayout(
					"p,2dlu,p:g(.5)" 
					,"");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Banco",getControl("banco"));
			builder.append("Sucursal",getControl("sucursal"));
			builder.append("Número de cuenta",getControl("numeroDeCuenta"));
			builder.append("Ejecutivo",getControl("ejecutivo"));
			builder.append("Teléfono",getControl("telefono"));
			builder.append("Fecha apertura",getControl("fechaApertura"));
			return builder.getPanel();
		}
		
		@Override
		protected JComponent createCustomComponent(String property) {
			
			if("banco".equals(property)){
				 JComponent box=Bindings.createBancosBinding(getModel().getModel(property));
				 box.setEnabled(!getModel().isReadOnly());
				 return box;
			}
			return null;
		}
	}

}
