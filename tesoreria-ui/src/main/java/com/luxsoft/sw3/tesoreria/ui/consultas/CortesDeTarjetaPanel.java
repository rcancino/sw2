package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.util.List;

import javax.swing.Action;

import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.sw3.tesoreria.ui.forms.CorteDeTarjetaForm;
import com.luxsoft.sw3.tesoreria.ui.forms.CorteDeTarjetaFormModel;


/**
 * Panel para el mantenimiento de cortes de tarjeta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CortesDeTarjetaPanel extends AbstractMasterDatailFilteredBrowserPanel<CorteDeTarjeta,PagoConTarjeta> {
	
	

	public CortesDeTarjetaPanel() {
		super(CorteDeTarjeta.class);
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre"
				,"fecha"
				,"corte"
				,"cuenta.clave"
				,"total"
				,"tipoDeTarjeta"
				,"ingreso.id"
				,"comentario"
				
				);
		addLabels(
				"Sucursal"
				,"Fecha"
				,"Corte"
				,"Cuenta"
				,"Total"
				,"Tarjeta"
				,"Ingreso"
				,"Comentario"
				
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeDeposito.class, "id"));
		manejarPeriodo();		
	}
	
	

	@Override
	protected void adjustMainGrid(JXTable grid) {		
		
	}
	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"nombre","fecha","total"};
		String[] names={"Nombre","Fecha","Total"};
		return GlazedLists.tableFormat(props,names);
	}

	@Override
	protected Model<CorteDeTarjeta, PagoConTarjeta> createPartidasModel() {
		return new Model<CorteDeTarjeta, PagoConTarjeta>(){
			public List<PagoConTarjeta> getChildren(CorteDeTarjeta parent) {
				String hql="from PagoConTarjeta p where p.corte.id=?";
				return getHibernateTemplate().find(hql,parent.getId());
			}			
		};
	}	
	
	public void open(){
		load();
	}
	
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,getViewAction()				
				,addAction(null, "imprimir", "Imprimir")
				};
		}
		return actions;
	}

	@Override
	protected List<CorteDeTarjeta> findData() {
		String hql="from CorteDeTarjeta c where c.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					periodo.getFechaInicial()
					,periodo.getFechaFinal()
					}
		);
	}
	
	@Override
	protected CorteDeTarjeta doInsert() {		
		final CorteDeTarjetaFormModel model=new CorteDeTarjetaFormModel();
		final CorteDeTarjetaForm form=new CorteDeTarjetaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CorteDeTarjeta target=model.commit();
			target=ServiceLocator2.getIngresosManager().registrarCorte(target);
			return target;
		}
		return null;
	}

	@Override
	protected void doSelect(Object bean) {
		final CorteDeTarjetaFormModel model=new CorteDeTarjetaFormModel((CorteDeTarjeta)bean);
		model.setReadOnly(true);
		final CorteDeTarjetaForm form=new CorteDeTarjetaForm(model);
		form.open();
	}
	
	
	
	
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}

}
