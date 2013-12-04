package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.cxc.service.DepositosManager;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.reports.RelacionDeFichas;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.sw3.services.Services;

public class DepositosPanel extends AbstractMasterDatailFilteredBrowserPanel<Ficha, FichaDet>{

	public DepositosPanel() {
		super(Ficha.class);
		
	}
	
	public void init(){
		//manejarPeriodo();
		addProperty("origen","fecha","sucursal.nombre","folio","total","cuenta","comentario","tipoDeFicha","envioForaneo");
		addLabels("Origen","Fecha","Suc","Folio","Total","Cuenta","Comentario","Tipo(Ficha)","Evalores");	
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Folio", "folio");
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Tipo", "tipoDeFicha");
		
	}
	
	
	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"pago.nombre","pago.fecha","pago.info","cheque","efectivo","banco"};
		String[] labels={"Cliente","Fecha","Pago","Cheque","Efectovi","Banco"};
		return GlazedLists.tableFormat(FichaDet.class,props, labels);
	}
	
	/*** Filtros para el detalle ***/
	
	private JTextField clienteField=new JTextField(5);
	private JTextField chequeField=new JTextField(5);
	private JTextField importeField=new JTextField(5);
	private JTextField bancoField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Cliente",clienteField);
		builder.append("Cheque",chequeField);
		builder.append("Importe",importeField);
		builder.append("Banco",bancoField);
	}	
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
	
		
		TextFilterator clienteFilterator=GlazedLists.textFilterator("pago.nombre");
		TextComponentMatcherEditor cteEditor=new TextComponentMatcherEditor(clienteField,clienteFilterator);
		editors.add(cteEditor);
		
		TextFilterator cheFilterator=GlazedLists.textFilterator("pago.info");
		TextComponentMatcherEditor cheEditor=new TextComponentMatcherEditor(chequeField,cheFilterator);
		editors.add(cheEditor);
		
		TextFilterator importeFilterator=GlazedLists.textFilterator("cheque");
		TextComponentMatcherEditor impEditor=new TextComponentMatcherEditor(bancoField,importeFilterator);
		editors.add(impEditor);
		
		TextFilterator bancoFilterator=GlazedLists.textFilterator("banco");
		TextComponentMatcherEditor banEditor=new TextComponentMatcherEditor(bancoField,bancoFilterator);
		editors.add(banEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}

	@Override
	protected Model<Ficha, FichaDet> createPartidasModel() {		
		return new CollectionList.Model<Ficha, FichaDet>(){
			public List<FichaDet> getChildren(final Ficha parent) {
				return Services.getInstance()
				.getHibernateTemplate()
				.find("from FichaDet det left join fetch det.pago p where det.ficha.id=?", parent.getId());
			}
		};
	}
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "cancelar", "Cancelar")
				,addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "reporteDeFichas", "Reporte de Fichas")
				};
		return actions;
	}
	
	
	public void cancelar(){
		Ficha selected=(Ficha)getSelectedObject();
		if(selected!=null){
			int index=source.indexOf(selected);
			if(index!=-1){
				Ficha res=getManager().cancelarDeposito(selected.getId());
				source.set(index, res);
				selectionModel.setSelectionInterval(index, index);
			}
			
		}
		
	}

	@Override
	protected String getDeleteMessage(Ficha bean) {
		return "Seguro que desea cancelar el deposito: "+bean;
	}

	@Override
	protected List<Ficha> findData() {
		String hql="from Ficha f where f.fecha = ? and f.sucursal.id=?";
		Object[] values={new Date()
				,Services.getInstance().getConfiguracion().getSucursal().getId()};
		return Services.getInstance().getHibernateTemplate().find(hql, values);
	}
	
	public void reporteDeFichas(){
		RelacionDeFichas report=new RelacionDeFichas();
		report.actionPerformed(null);
	}
	
	
	
	private DepositosManager getManager(){
		return Services.getInstance().getDepositosManager();
	}
	
}
