package com.luxsoft.sw3.embarques.ui.consultas;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.embarques.ui.forms.ComisionPorTrasladoForm;
import com.luxsoft.sw3.embarques.ui.forms.ComisionPorTrasladoFormModel;

/**
 * Panel para el mantenimiento de las comisiones de choferes sobre traslados
 * de materiales
 * 
 * @author Ruben Cancino
 *
 */
public class TrasladosPanel extends FilteredBrowserPanel<Traslado>{

	public TrasladosPanel() {
		super(Traslado.class);	
		setTitle("Traslados por chofer");
	}
	
	
	protected void init(){
		addProperty(
				"sucursal.nombre"
				,"documento"
				,"fecha"				
				,"solicitud.sucursal.nombre"
				,"solicitud.documento"
				,"chofer"
				,"kilos"
				,"comisionChofer"
				,"fechaComision"
				,"comentarioComision"
				,"comentario"
				,"importado"
				,"replicado"
				);
		addLabels(
				"Suc(TPS)"
				,"TPS"
				,"Fecha"				
				,"Suc(TPE)"
				,"TPE"
				,"Chofer"
				,"Kilos"
				,"Comisión"
				,"Com (Fecha)"
				,"Comisión (Coment)"
				,"Comentario"
				,"Importado"
				,"Replicado"
				);
		installTextComponentMatcherEditor("Sucursal","sucursal.nombre");
		installTextComponentMatcherEditor("Documento","documento");
		//installTextComponentMatcherEditor("Sol","solicitud.documento");
		installTextComponentMatcherEditor("Chofer","chofer");
		manejarPeriodo();
		periodo=Periodo.getPeriodoDelMesActual();
	}

	@Override
	protected List<Traslado> findData() {
		String hql="from Traslado t where t.fecha between ? and ? and tipo=?";
		Object[] values={periodo.getFechaInicial(),periodo.getFechaFinal(),"TPS"};
		return getHibernateTemplate().find(hql,values);
	}
	
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				,getViewAction()
				,addAction("", "aplicarComision", "Aplicar comisión")
				};
		return actions;
	}

	public void aplicarComision(){
		if(!selectionModel.isSelectionEmpty()){
			ComisionPorTrasladoFormModel model=new ComisionPorTrasladoFormModel();
			ComisionPorTrasladoForm form=new ComisionPorTrasladoForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				List<Traslado> traslados=new ArrayList<Traslado>(getSelected());
				for(Traslado t:traslados){
					int index=source.indexOf(t);
					if(index!=-1){
						t.setPrecioComisionTonelada(model.getTraslado().getPrecioPorTonelada());
						t.setComentarioComision(model.getTraslado().getComentario());
						t=save(t);
						source.set(index, t);
					}
				}				
			}
		}
	}
	
	
	
	private Traslado save(Traslado t){
		
		return (Traslado)getHibernateTemplate().merge(t);
	}
	
	private HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
	
	

}
