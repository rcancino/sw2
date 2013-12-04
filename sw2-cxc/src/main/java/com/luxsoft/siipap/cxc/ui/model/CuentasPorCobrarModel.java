package com.luxsoft.siipap.cxc.ui.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Modelo para el estado y comportamiento de las cuentas por cobrar 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CuentasPorCobrarModel implements PropertyChangeListener{
	
	private EventList<Cargo> cuentasPorCobrar;
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	
	private Logger logger=Logger.getLogger(getClass());
	
	
	
	protected void init(){
		cuentasPorCobrar=new BasicEventList<Cargo>();
	}
	
	public void loadCuentasPorCobrar(){
		
	}
		
	public EventList<Cargo> getCuentasPorCobrar(){
		return cuentasPorCobrar;
	}
	
	public List<Cargo> buscarCuentasPorCobrar(){
		/*logger.debug("Localizando cuentas por cobrar");
		String sql="SELECT A.CARGO_ID" +				
				" FROM SX_VENTAS  A " +
				" WHERE ORIGEN=\'CRE\' " +
				"   AND FECHA >=? " +
				"   AND (A.TOTAL-IFNULL( (SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE A.CARGO_ID=X.CARGO_ID) ,0)) <>0";
		final List<String> ids=ServiceLocator2.getJdbcTemplate().queryForList(sql,new Object[]{DateUtil.toDate("01/01/2009")},String.class);
		final List<Cargo> cargos=new ArrayList<Cargo>();
		for(String id:ids){
			cargos.add(getManager().getCargo(id));
		}
		return cargos;*/
		return getManager().buscarCuentasPorCobrar();
	}
	
	/**
	 * Actualiza las fechas de revision y cobro para la cartera de credito
	 * 
	 */
	public void actualizarRevision(){
		SwingWorker<List<Cargo>, String> worker=new SwingWorker<List<Cargo>, String>(){			
			protected List<Cargo> doInBackground() throws Exception {				
				return getManager().actualizarRevisionYCobro();
			}			
			protected void done() {
				try {
					cuentasPorCobrar.clear();
					cuentasPorCobrar.addAll(get());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
				}
			}
			
		};
		TaskUtils.executeSwingWorker(worker);
		
	}
	
	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}


	public void propertyChange(PropertyChangeEvent evt) {
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
	}
	
	protected transient PropertyChangeSupport support=new PropertyChangeSupport(this);
	
	public final synchronized void addPropertyChangeListener(
             PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
		 
	}
	 
	public final synchronized void removePropertyChangeListener(
             PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);		
	}
	
	public void firePropertyChange(String propertyName, Object old, Object value){
		support.firePropertyChange(propertyName, old, value);
	}
	
	
	public static final String CUENTAS_CARGADAS="cuentasModificadas";
	
	public void notificarCambioDeCuentas(){
		logger.info("Notificando actualizacion de cuentas por cobrar");
		firePropertyChange(CUENTAS_CARGADAS, 0,cuentasPorCobrar.size() );
	}

	

}
