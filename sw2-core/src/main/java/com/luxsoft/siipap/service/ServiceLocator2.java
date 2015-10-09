package com.luxsoft.siipap.service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;






import com.luxsoft.siipap.compras.dao.ListaDePreciosDao;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.service.ClienteServices;
import com.luxsoft.siipap.cxc.service.DepositosManager;
import com.luxsoft.siipap.cxc.service.NotaDeCreditoManager;
import com.luxsoft.siipap.cxp.service.AnalisisDeCompraManager;
import com.luxsoft.siipap.cxp.service.AnticipoDeComprasManager;
import com.luxsoft.siipap.cxp.service.FacturaManager;


import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.gastos.GCompraDao;
import com.luxsoft.siipap.dao.hibernate.UniversalDaoHibernate;
import com.luxsoft.siipap.dao.tesoreria.CargoAbonoDao;
import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.inventarios.dao.ExistenciaMaqDao;
import com.luxsoft.siipap.inventarios.dao.InventarioAnualDao;
import com.luxsoft.siipap.inventarios.service.CostoPromedioManager;
import com.luxsoft.siipap.inventarios.service.CostosServices;
import com.luxsoft.siipap.inventarios.service.InventarioManager;
import com.luxsoft.siipap.inventarios.service.TransformacionesManager;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.replica.aop.ExportadorManager;
import com.luxsoft.siipap.service.aop.EntityModificationListener;
import com.luxsoft.siipap.service.aop.PersistenceNotificator;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.service.core.ProductoManager;
import com.luxsoft.siipap.service.core.ProveedorManager;
import com.luxsoft.siipap.service.gastos.ComprasDeGastosManager;
import com.luxsoft.siipap.service.tesoreria.RequisicionesManager;
import com.luxsoft.siipap.service.ventas.FacturasManager;
import com.luxsoft.siipap.service.ventas.PedidosManager;
import com.luxsoft.siipap.ventas.dao.ListaDePreciosVentaDao;
import com.luxsoft.siipap.ventas.dao.VentaDao;
import com.luxsoft.siipap.ventas.service.DescuentosManager;
import com.luxsoft.siipap.ventas.service.ListaDePreciosVentaManager;
import com.luxsoft.siipap.ventas.service.VentasManager;
import com.luxsoft.sw3.cfd.dao.CertificadoDeSelloDigitalDao;
import com.luxsoft.sw3.cfd.services.CFDMailServices;
import com.luxsoft.sw3.cfd.services.ComprobantesDigitalesManager;
import com.luxsoft.sw3.cfdi.CFDIFactura;
import com.luxsoft.sw3.cfdi.CFDIManager;
import com.luxsoft.sw3.cfdi.CFDI_EnvioServices;
import com.luxsoft.sw3.cfdi.CFDI_MailServices;
import com.luxsoft.sw3.cfdi.IFactura;
import com.luxsoft.sw3.cfdi.INotaDeCredito;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.services.CierreAnualManager;
import com.luxsoft.sw3.contabilidad.services.CuentasContablesManager;
import com.luxsoft.sw3.contabilidad.services.PolizasManager;
import com.luxsoft.sw3.contabilidad.services.SaldoDeCuentasManager;
import com.luxsoft.sw3.cxc.services.CXCMailServices;
import com.luxsoft.sw3.replica.ReplicaMessageCreator;
import com.luxsoft.sw3.replica.ReplicaMessageCreator2;
import com.luxsoft.sw3.services.ActivoFijoManager;
import com.luxsoft.sw3.services.AnalisisDeTransfomracionesManager;
import com.luxsoft.sw3.services.CheckplusManager;
import com.luxsoft.sw3.services.ComprasManager;
import com.luxsoft.sw3.services.IngresosManager;
import com.luxsoft.sw3.services.ListaDePreciosClienteManager;
import com.luxsoft.sw3.services.MaquilaManager;
import com.luxsoft.sw3.services.SimuladorDePreciosManager;
import com.luxsoft.sw3.services.SolicitudDeDepositosManager;
import com.luxsoft.sw3.services.SolicitudDeModificacionesManager;
import com.luxsoft.sw3.services.TesoreriaManager;

/**
 * Central acces to the (Core) Back-End of the SiipapWin2 application
 * Singleton /Facade
 * 
 * @author Ruben Cancino
 *
 */
public  class ServiceLocator2 {
	
	private static ServiceLocator2 INSTANCE;
	private ApplicationContext coreContext;
	private static Logger logger=Logger.getLogger(ServiceLocator2.class);
	
	/*
	static{
		INSTANCE=new ServiceLocator2();
	}
	*/
	
	private ServiceLocator2(){
		
	}
	
	public static synchronized ServiceLocator2 instance(){
		if(INSTANCE==null)
			INSTANCE=new ServiceLocator2();
		return INSTANCE;
	}
	
	public ApplicationContext getContext(){
		if(coreContext==null){
			coreContext=new ClassPathXmlApplicationContext(
					getConfigLocations()
					,getDbContext());
		}
		return coreContext;
	}
	
	public static void close(){
		if(INSTANCE!=null){
			((ClassPathXmlApplicationContext)INSTANCE.getContext().getParent()).close();
			((ClassPathXmlApplicationContext)INSTANCE.getContext()).close();
		}
	}
	
	protected ApplicationContext getDbContext(){
		return new ClassPathXmlApplicationContext(
				new String[]{
				"classpath:/applicationContext-db.xml", 		// DB configuration
				}
				);
	}
	
	protected String[] getConfigLocations() {
        return new String[] {                
        		//"classpath:/applicationContext-db.xml", 		// DB configuration
                "classpath:/applicationContext-dao.xml", 	   	// Main DAO container
                "classpath*:/applicationContext.xml", 		   	// DAOs and Managers for modules
                "classpath:/applicationContext-srv.xml" 	// Services for modules
                ,"classpath:/sw3-jms-context.xml" 			// Services for modules
                			  
            };
    }
	
	/** Public Facade accessors  **/
	
	public static DataSource getDataSource(){
		return (DataSource)instance().getContext().getBean("dataSource");
	}
	
	public static DataSource getAnalisisDataSource(){
		return (DataSource)instance().getContext().getBean("analisisDS");
	}
	
	public static SessionFactory getSessionFactory(){
		return (SessionFactory)instance().getContext().getBean("sessionFactory");
	}
	
	public static JdbcTemplate getJdbcTemplate(){
		return (JdbcTemplate)instance().getContext().getBean("jdbcTemplate");
	}
	
	public static JdbcTemplate getAnalisisJdbcTemplate(){
		return (JdbcTemplate)instance().getContext().getBean("analisisJdbcTemplate");
	}
	
	public static HibernateTemplate getHibernateTemplate(){
		return (HibernateTemplate)instance().getContext().getBean("hibernateTemplate");
	}
	
	/**
	 * Comoditi para ejecutar queries de HQL
	 * 
	 * @param params
	 * @return
	 */
	public static List findHibernateData (Object...params){
		String hql=(String)params[0];
		Object[] values=ArrayUtils.subarray(params, 1, params.length-1);
		return getHibernateTemplate().find(hql, values);
	}
	
	public static UniversalDaoHibernate getUniversalDaoIvernate(){
		return (UniversalDaoHibernate)instance().getContext().getBean("universalDaoHibernate");
	}
	
	public static UniversalDao getUniversalDao(){
		return (UniversalDao)instance().getContext().getBean("universalDao");
	}
	
	public static LookupManager getLookupManager(){
		return (LookupManager)instance().getContext().getBean("lookupManager");
	}
	
	public static UniversalManager getUniversalManager(){
		return (UniversalManager)instance().getContext().getBean("universalManager");
	}
	
	//public static Configuracion getCurrentConfiguration
	private static Configuracion configuracion;
	
	
	public static Configuracion getConfiguracion(){
		if(configuracion==null){
			try {
				System.out.println("Cargando configuracion1");
				List<Configuracion> data=getUniversalDao().getAll(Configuracion.class);
				System.out.println("Cargando configuracion2");
				if(!data.isEmpty())
					return data.get(0);
				return null;
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return configuracion;
	}
	
	
	
	public static synchronized Date obtenerFechaDelSistema(){
		return (Date)getJdbcTemplate().queryForObject("select now()", Date.class);
	}
	
	/** Facade para Gastos ***/
	
	public static GCompraDao getGCompraDao(){
		return (GCompraDao)instance().getContext().getBean("compraDeGastosDao");
	}
	
	public static ComprasDeGastosManager getComprasDeGastosManager(){
		return (ComprasDeGastosManager)instance().getContext().getBean("comprasDeGastosManager");
	}
	
	public static RequisicionesManager getRequisiciionesManager(){
		return (RequisicionesManager)instance().getContext().getBean("requisicionesManager");
	}
	
	public static UserManager getUserManager(){
		return (UserManager)instance().getContext().getBean("userManager");
	}
	 
	public static LoginManager getLoginManager(){
		return (LoginManager)instance().getContext().getBean("loginManager");
	}
	public static AutorizacionesManager getAutorizacionesManager(){
		return (AutorizacionesManager)instance().getContext().getBean("autorizacionesManager");
	}
	public static CargoAbonoDao getCargoAbonoDao(){
		return (CargoAbonoDao)instance().getContext().getBean("cargoAbonoDao");
	}
	
	public static ProductoManager getProductoManager(){
		return (ProductoManager)instance().getContext().getBean("productoManager");
	}
	
	

	public static ProveedorManager getProveedorManager(){
		return (ProveedorManager)instance().getContext().getBean("proveedorManager");
	}
	
	public static ClienteManager getClienteManager(){
		return (ClienteManager)instance().getContext().getBean("clienteManager");
	}
	
	public static VentasManager getVentasManager(){
		return (VentasManager)instance().getContext().getBean("ventasManager");
	}
	
	public static CXCManager getCXCManager(){
		return (CXCManager)instance().getContext().getBean("cxcManager");
	}
	public static ClienteServices getClienteServices(){
		return (ClienteServices)instance().getContext().getBean("clienteServices");
	}
	
	
	public static InventarioManager getInventarioManager(){
		return (InventarioManager)instance().getContext().getBean("inventarioManager");
	}
	
	public static MaquilaManager getMaquilaManager(){
		return (MaquilaManager)instance().getContext().getBean("maquilaManager");
	}
	
	public static ExportadorManager getExportadorManager(){
		return (ExportadorManager)instance().getContext().getBean("exportManager");
	}

	public static InventarioAnualDao getInventarioAnualDao(){
		return (InventarioAnualDao)instance().getContext().getBean("inventarioAnualDao");
	}
	
	public static CostoPromedioManager getCostoPromedioManager(){
		return (CostoPromedioManager)instance().getContext().getBean("costoPromedioManager");
	}
	
	public static CostosServices getCostosServices(){
		return (CostosServices)instance().getContext().getBean("costosServices");
	}
	
	
	
	public static ListaDePreciosDao getListaDePreciosDao(){
		return (ListaDePreciosDao)instance().getContext().getBean("listaDePreciosDao");
	}
	
	public static VentaDao getVentaDao(){
		return (VentaDao)instance().getContext().getBean("ventaDao");
	}
	
	public static DescuentosManager getDescuentosManager(){
		return (DescuentosManager)instance().getContext().getBean("descuentosManager");
	}
	
	public static void registrarPersistenceListener(EntityModificationListener listener){
		PersistenceNotificator not=(PersistenceNotificator)instance().getContext().getBean("persistenceNotificator");
		not.addListener(listener);
	}
	
	public static void removePersistenceListener(EntityModificationListener listener){
		PersistenceNotificator not=(PersistenceNotificator)instance().getContext().getBean("persistenceNotificator");
		not.removeListener(listener);
	}
	
	public static DepositosManager getDepositosManager(){
		return (DepositosManager)instance().getContext().getBean("depositosManager");
	}
	
	public static synchronized ExistenciaDao getExistenciaDao(){
		return (ExistenciaDao)instance().getContext().getBean("existenciaDao");
	}
	
	public static synchronized ExistenciaMaqDao getExistenciaMaqDao(){
		return (ExistenciaMaqDao)instance().getContext().getBean("existenciaMaqDao");
	}
	
	
	public static synchronized TransformacionesManager getTransformacionesManager(){
		return (TransformacionesManager)instance().getContext().getBean("transformacionesManager");
	}
	
	public static synchronized NotaDeCreditoManager getNotasManager(){
		return (NotaDeCreditoManager)instance().getContext().getBean("notaDeCreditoManager");
	}
	
	public static synchronized ComprasManager getComprasManager(){
		return (ComprasManager)instance().getContext().getBean("comprasManager");
	}
	
	public static synchronized SolicitudDeDepositosManager getSolicitudDeDepositosManager(){
		return (SolicitudDeDepositosManager)instance().getContext().getBean("solicitudDeDepositosManager");
	}
	public static synchronized IngresosManager getIngresosManager(){
		return (IngresosManager)instance().getContext().getBean("ingresosManager");
	}
	
	public static synchronized ListaDePreciosClienteManager getListaDePreciosClienteManager(){
		return (ListaDePreciosClienteManager)instance().getContext().getBean("listaDePreciosClienteManager");
	}
	
	public static synchronized ComprobantesDigitalesManager getCFDManager(){
		return (ComprobantesDigitalesManager)instance().getContext().getBean("cfdManager");
	}
	
	public static synchronized CertificadoDeSelloDigitalDao getCertificadoDeSelloDigitalDao(){
		return (CertificadoDeSelloDigitalDao)instance().getContext().getBean("certificadoDeSelloDigitalDao");
	}
	
	public static synchronized PedidosManager getPedidosManager(){
		return (PedidosManager)instance().getContext().getBean("pedidosManager");
	}
	public static synchronized FacturasManager getFacturasManager(){
		return (FacturasManager)instance().getContext().getBean("facturasManager");
	}
	
	public static synchronized CXCMailServices getCXCMailServices(){
		return (CXCMailServices)instance().getContext().getBean("cxcMailServices");
	}
	
	public static synchronized ListaDePreciosVentaManager getListaDePreciosVentaManager(){
		return (ListaDePreciosVentaManager)instance().getContext().getBean("listaDePreciosVentaManager");
	}
	public static synchronized CuentasContablesManager getCuentasContablesManager(){
		return (CuentasContablesManager)instance().getContext().getBean("cuentasContablesManager");
	}
	public static synchronized PolizasManager getPolizasManager(){
		return (PolizasManager)instance().getContext().getBean("polizasManager");
	}
	
	public static SaldoDeCuentasManager getSaldoDeCuentaManager() {
		return (SaldoDeCuentasManager)instance().getContext().getBean("saldosDeCuentasManager");
	}
	public static CierreAnualManager getCierreAnualManager(){
		return (CierreAnualManager)instance().getContext().getBean("cierreAnualManager");
	}
	
	public static AnalisisDeCompraManager getAnalisisDeCompraManager(){
		return (AnalisisDeCompraManager)instance().getContext().getBean("analisisDeCompraManager");
	}
	public static FacturaManager getCXPFacturaManager(){
		return (FacturaManager)instance().getContext().getBean("cxpFacturaManager");
	}
	public static AnticipoDeComprasManager getAnticipoDeComprasManager(){
		return (AnticipoDeComprasManager)instance().getContext().getBean("anticipoDeComprasManager");
	}
	public static AnalisisDeTransfomracionesManager getAnalisisDeTransformacionManager(){
		return (AnalisisDeTransfomracionesManager)instance().getContext().getBean("analisisTransformacionesManager");
	}
	public static ActivoFijoManager getActivoFijoManager(){
		return (ActivoFijoManager)instance().getContext().getBean("activoFijoManager");
	}
	public static TesoreriaManager getTesoreriaManager(){
		return (TesoreriaManager)instance().getContext().getBean("tesoreriaManager");
	}
	
	public static double buscarTipoDeCambio(final Date fecha){		
		String hql="select t.factor from TipoDeCambio t where t.fecha=?";
		List<Double> res=getHibernateTemplate().find(hql,fecha);
		if(res==null) return 1d;
		if(res.isEmpty()) return 1d;
		return res.get(0);
	}
	public static JmsTemplate getJmsTemplate(){
		return (JmsTemplate)instance().getContext().getBean("jmsTemplate");
	}
	public static ReplicaMessageCreator getReplicaMessageCreator(){
		return (ReplicaMessageCreator)instance().getContext().getBean("replicaMessageCreator");
	}
	

	
	public static CFDMailServices getCFDMailServices(){
		return (CFDMailServices)instance().getContext().getBean("cfdMailServices");
	}
	
	public static CFDI_MailServices getCFDIMailServices(){
		return (CFDI_MailServices)instance().getContext().getBean("cfdi_MailServices");
	}
	
	
	public static CFDI_EnvioServices getCFDIEnvioServices(){
		return (CFDI_EnvioServices)instance().getContext().getBean("cfdi_EnvioServices");
	}
	
	public static CheckplusManager getCheckplusManager(){
		return (CheckplusManager)instance().getContext().getBean("checkplusManager");
	}
	
	public static SolicitudDeModificacionesManager getSolicitudDeModificacionesManager(){
		return (SolicitudDeModificacionesManager)instance().getContext().getBean("solicitudDeModificacionesManager");
	}
	public static SimuladorDePreciosManager getSimuladorDePreciosManager(){
		return (SimuladorDePreciosManager)instance().getContext().getBean("simuladorDePreciosManager");
	}
	public static IFactura getCFDIFactura(){
		return (IFactura)instance().getContext().getBean("cfdiFactura");
	}
	public static INotaDeCredito getCFDINotaDeCredito(){
		return (INotaDeCredito)instance().getContext().getBean("cfdiNotaDeCredito");
	}
	public static synchronized CFDIManager getCFDIManager(){
		return(CFDIManager)instance().getContext().getBean("cfdiManager");
	}
	
	public static void main(String[] args) {
		//getJmsTemplate();
		/*
		Map mm=getSessionFactory().getAllClassMetadata();
		Map map=new TreeMap();
		
		for(Object  o:mm.entrySet()){
			Map.Entry e=(Map.Entry)(o);
			AbstractEntityPersister a=(AbstractEntityPersister)e.getValue();
			map.put(ClassUtils.getShortClassName(a.getName()), a.getTableName());
		}
		for(Object  o:map.entrySet()){
			Map.Entry e=(Map.Entry)(o);
			String msg="map.put({0},{1});";
			System.out.println(MessageFormat.format(msg
					,"\""+e.getKey()+"\""
					, "\""+e.getValue()+"\"") 
					);
		}
		*/
		/*
		Map map=ServiceLocator2.getSessionFactory().getAllCollectionMetadata();
		System.out.println("[");
		for(Object  o:map.entrySet()){
			Map.Entry e=(Map.Entry)(o);
			AbstractCollectionPersister a=(AbstractCollectionPersister)e.getValue();
			String msg="{0}:{1},";
			System.out.println(MessageFormat.format(msg,"\'"+ClassUtils.getShortClassName(a.getName())+"\'", "\'"+a.getTableName()+"\'") );
		}
		System.out.println("]");*/
		//getCheckplusManager().generarOpcionesIniciales();
		getCFDIFactura();
	}

	

}
