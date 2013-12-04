package com.luxsoft.siipap.cxc.ui.consultas;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.reports.RelacionDeFichas;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;

public class DepositosPanel extends AbstractMasterDatailFilteredBrowserPanel<Ficha, FichaDet>{

	public DepositosPanel() {
		super(Ficha.class);
		
	}
	
	public void init(){
		manejarPeriodo();
		addProperty("fecha","sucursal.nombre","folio","cheque","efectivo","importe","cuenta","comentario","tipoDeFicha","envioForaneo");
		addLabels("Fecha","Suc","Folio","Cheque","Efectivo","Importe","Cuenta","Comentario","Tipo(Ficha)","Evalores");	
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"pago","cheque","efectivo","banco"};
		String[] labels={"Pago","Cheque","Efectovi","Banco"};
		return GlazedLists.tableFormat(FichaDet.class,props, labels);
	}

	@Override
	protected Model<Ficha, FichaDet> createPartidasModel() {		
		return new CollectionList.Model<Ficha, FichaDet>(){
			public List<FichaDet> getChildren(final Ficha parent) {
				return ServiceLocator2
				.getHibernateTemplate()
				.find("from FichaDet det left join fetch det.pago p where det.ficha.id=?", parent.getId());
			}
		};
	}

	@Override
	public boolean doDelete(Ficha bean) {
		ServiceLocator2.getDepositosManager().cancelarDeposito(bean.getId());
		return true;
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getDeleteAction()
				,addAction(CXCActions.ReportesDeDepositos.getId(), "reporteDeFichas", "Reporte de Fichas")
				,addAction(CXCActions.CancelarEnvioDeValores.getId(),"cancelarEnvio","Cancelar Tecnoval")
				};
		return actions;
	}

	@Override
	protected String getDeleteMessage(Ficha bean) {
		return "Seguro que desea cancelar el deposito: "+bean;
	}

	@Override
	protected List<Ficha> findData() {
		String hql="from Ficha f where f.fecha between ? and ?";
		Object[] values={periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2.getHibernateTemplate().find(hql, values);
	}
	
	public void reporteDeFichas(){
		RelacionDeFichas report=new RelacionDeFichas();
		report.actionPerformed(null);
	}
	
	public void cancelarEnvio(){
		if(!getSelected().isEmpty()){
			List<Ficha> selected=new ArrayList<Ficha>();
			selected.addAll(getSelected());
			for(Ficha f:selected){
				if(f!=null){			
					int index=source.indexOf(f);
					if(index!=-1){
						f.setEnvioForaneo(false);
						f=(Ficha)ServiceLocator2.getUniversalManager().save(f);
						source.set(index,f);
					}
				}
			}
		}		
	}
	
}
