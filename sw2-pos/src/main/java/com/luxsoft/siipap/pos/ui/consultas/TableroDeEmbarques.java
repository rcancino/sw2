package com.luxsoft.siipap.pos.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.embarque.ControlDeEntrega;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.services.Services;

public class TableroDeEmbarques extends FilteredBrowserPanel<ControlDeEntrega>implements PropertyChangeListener{

	public TableroDeEmbarques() {
		super(ControlDeEntrega.class);
	}
	
	protected void init(){
		addProperty(
				"sucursal"
				,"cliente"
				,"fechaPedido"
				,"fechaFactura"
				,"surtidoHora"
				,"enviadoHora"
				,"tiempoDeEntregaEnvio"
				,"tiempoDeEntregaPedido"
				,"recepcion"
				,"incidente"
				,"comentario"
				);
		addLabels("Suc"
				,"Cliente"
				,"Fecha P"
				,"Fecha Fac"
				,"Surtido"
				,"Enviado"
				,"T.E Envio"
				,"T.E.Ped"
				,"Rec"
				,"Incidentes"
				,"Comentario"
				);
	}
	
	private HeaderPanel header;
	
	
	@Override
	protected JComponent buildHeader(){
		if(header==null){
			header=new HeaderPanel("Tableros de control","");
			updateHeader();
		}
		return header;
	}
	
	private void updateHeader(){
		if((header!=null) && (periodo!=null))
			header.setDescription("Periodo: "+periodo.toString());
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<ControlDeEntrega> findData() {
		List<Entrega> entregas;
		if(periodo==null){
			String hql="from Entrega  ex left join fetch ex.embarque e where e.regreso is null";
			entregas= Services.getInstance().getHibernateTemplate().find(hql);
			
		}else{
			String hql="from Entrega ex left join fetch ex.embarque e where e.fecha between ? and ?";
			entregas=Services.getInstance().getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
			periodo=null;
		}
		final List<ControlDeEntrega> res=new ArrayList<ControlDeEntrega>();
		for(Entrega e:entregas){
			res.add(new ControlDeEntrega(e));
		}
		return res;
	}
	/**
	 * Necesario para evitar NPE por la etiqueta de periodo
	 */
	protected void updatePeriodoLabel(){
		//periodoLabel.setText("Periodo:" +periodo.toString());
	}
	
	private Action buscarAction;
	
	protected void initActions(){
		buscarAction=addAction("buscar.id","buscar", "Buscar");
		buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			initActions();
			actions=new Action[]{
				getLoadAction()
				,buscarAction
				};
		}
		return actions;
	}
	
	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	
	

}
