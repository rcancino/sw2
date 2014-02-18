package com.luxsoft.sw3.services;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;



import com.luxsoft.siipap.cxc.service.DepositosManager;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.cfdi.CFDI_MailServicesPOS;
import com.luxsoft.siipap.service.AutorizacionesManager;
import com.luxsoft.siipap.service.LoginManager;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.service.core.ProveedorManager;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.services.ComprobantesDigitalesManager;
import com.luxsoft.sw3.cfdi.CFDIManager;
import com.luxsoft.sw3.cfdi.CFDITimbrador;
import com.luxsoft.sw3.cfdi.CFDITraslado;
import com.luxsoft.sw3.cfdi.CFDI_MailServices;
import com.luxsoft.sw3.cfdi.ITraslado;

/**
 * Service locator para accesar los beans administrados por Spring
 * 
 * @author Ruben Cancino Ramos
 *
 */
public final class Services {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private ApplicationContext context;
	
	private Services(){
	}
	
	protected String[] getConfigLocations() {
        return new String[] {                
        		//"file:/sw3/etc/sw3-ctx-db.xml" //For DAOS
        		"classpath:audit.xml" // AuditLog
        		,"classpath:spring/sw3-pos-db.xml"
                ,"classpath*:spring/sw3-applicationContext.xml" // for modular projects
                ,"classpath*:spring/sw3-serviceContext.xml" // for service layers
                ,"classpath*:spring/sw3-pos-jms-context.xml" // for JMS support
            };
    }
	
	public ApplicationContext getContext(){		
		if(context==null){
			context=new ClassPathXmlApplicationContext(
					getConfigLocations()
					);
		}
		return context;
	}
	
	public DataSource getDataSource(){
		return (DataSource)getContext().getBean("dataSource");
	}
	
	protected synchronized void closeContext(){
		((ClassPathXmlApplicationContext)INSTANCE.getContext()).close();
		context=null;
	}
	
	public synchronized Date obtenerFechaDelSistema(){
		return (Date)getJdbcTemplate().queryForObject("select now()", Date.class);
	}
	
	public JdbcTemplate getJdbcTemplate(){
		return (JdbcTemplate)getContext().getBean("jdbcTemplate");
	}
	
	public synchronized HibernateTemplate getHibernateTemplate(){
		return (HibernateTemplate)getContext().getBean("hibernateTemplate");
	}
	
	public LoginManager getLoginManager(){
		return (LoginManager)getContext().getBean("loginManager");
	}
	
	public AutorizacionesManager getAutorizacionesManager(){
		return (AutorizacionesManager)getContext().getBean("autorizacionesManager");
	}
	
	public UniversalDao getUniversalDao(){
		return (UniversalDao)getContext().getBean("universalDao");
	}
	
	public ClienteManager getClientesManager(){
		return (ClienteManager)getContext().getBean("clienteManager");
	}
	
	public ProductosManager2 getProductosManager(){
		return (ProductosManager2)getContext().getBean("productosManager");
	}
	
	
	public PedidosManager getPedidosManager(){
		return (PedidosManager)getContext().getBean("pedidosManager");
	}
	
	public FacturasManager getFacturasManager(){
		return (FacturasManager)getContext().getBean("facturasManager");
	}
	
	public ProveedorManager getProveedorManager(){
		return (ProveedorManager)getContext().getBean("proveedoresManager");
	}
	
	public InventariosManager getInventariosManager(){
		return (InventariosManager)getContext().getBean("inventariosManager");
	}
	
	public TransformacionesManager getTransfomracionesManager(){
		return (TransformacionesManager)getContext().getBean("transformacionesManager");
	}
	
	public ExistenciaDao getExistenciasDao(){
		return (ExistenciaDao)getContext().getBean("existenciaDao");
	}
	
	private Configuracion configuracion;
	
	
	
	public  Configuracion getConfiguracion(){
		
		if(configuracion==null){
			configuracion=loadConfiguracion();
		}
		return configuracion;
		/*
		try {
			Long id=Configuracion.getSucursalLocalId();
			String hql="from Configuracion  c where c.sucursal.id=?";
			List<Configuracion> data=getInstance().getHibernateTemplate().find(hql,id);
			if(!data.isEmpty())
				return data.get(0);
			System.err.println( "*********************"+data.get(0));
			return null;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}*/
	}
	
	private Configuracion loadConfiguracion(){
		try {
			Long id=Configuracion.getSucursalLocalId();
			String hql="from Configuracion  c where c.sucursal.id=?";
			List<Configuracion> data=getInstance().getHibernateTemplate().find(hql,id);
			Assert.notEmpty(data,"No existe registro de configucacion para la sucursal: "+id+ " Tabla SX_CONFIGURACION");
			return data.get(0);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public List<Sucursal> getSucursalesOperativas(){
		return getHibernateTemplate().find("from Sucursal s where s.habilitada=? and s.clave not in(1,50)", Boolean.TRUE);
	}
	
	public ComprasManager getComprasManager(){
		return (ComprasManager)getContext().getBean("comprasManager");
	}
	
	public DepositosManager getDepositosManager(){
		return (DepositosManager)getContext().getBean("depositosManager");
	}
	
	public PagosManager getPagosManager(){
		return (PagosManager)getContext().getBean("pagosManager");
	}
	
	public SolicitudDeTrasladosManager getSolicitudDeTrasladosManager(){
		return (SolicitudDeTrasladosManager)getContext().getBean("solicitudDeTrasladosManager");
	}
	public TrasladosManager getTrasladosManager(){
		return (TrasladosManager)getContext().getBean("trasladosManager");
	}
	
	public EmbarquesManager getEmbarquesManager(){
		return(EmbarquesManager)getContext().getBean("embarquesManager");
		
	}
	
	public MaquilaManager getMaquilaManager(){
		return (MaquilaManager)getContext().getBean("maquilaManager");
	}
	
	public SolicitudDeDepositosManager getSolicitudDeDepositosManager(){
		return (SolicitudDeDepositosManager)getContext().getBean("solicitudDeDepositosManager");
	}
	
	public CorteDeCajaManager getCorteDeCajaManager(){
		return (CorteDeCajaManager)getContext().getBean("corteDeCajaManager");
	}
	
	public ComprobantesDigitalesManager getComprobantesDigitalManager(){
		return (ComprobantesDigitalesManager)getContext().getBean("cfdManager");
	}
	
	public synchronized static TaskExecutor getTaskExecutor(){
		return (TaskExecutor)getInstance().getContext().getBean("taskExecutor");
	}
	
	public synchronized static SolicitudDeModificacionesManager getSolicitudDeModificacionesManager(){
		return (SolicitudDeModificacionesManager)getInstance().getContext().getBean("solicitudDeModificacionesManager");
	}
	
	/** Singleton implementation ***/
	
	private static Services INSTANCE;
	
	public static Services getInstance(){
		if(INSTANCE==null)
			INSTANCE=new Services();
		return INSTANCE;
	}
	
	public static synchronized void close(){
		if(INSTANCE!=null){
			getInstance().closeContext();
		}
	}
	
	
	public  double buscarTipoDeCambio(final Date fecha){		
		String hql="select t.factor from TipoDeCambio t where t.fecha=?";
		List<Double> res=getHibernateTemplate().find(hql,fecha);
		if(res==null) return 1d;
		if(res.isEmpty()) return 1d;
		return res.get(0);
	}
	
	public synchronized static  CFDIManager getCFDIManager(){
		return (CFDIManager)getInstance().getContext().getBean("cfdiManager");
	}
	
	public synchronized static  CFDITimbrador getCFDITimbrador(){
		return (CFDITimbrador)getInstance().getContext().getBean("cfdiTimbrador");
	}
	public synchronized static ITraslado getCFDITraslado(){
		return (ITraslado)getInstance().getContext().getBean("cfdiTraslado");
	}
	
	
	public Empresa getEmpresa() {
		return getConfiguracion().getSucursal().getEmpresa();
	}
	
	/*public synchronized static JavaMailSender getMailSender(){
		System.err.println("Estoy en el mailsender");
		return (JavaMailSender)getInstance().getContext().getBean("mailSender");
	}*/
	
	public static CFDI_MailServicesPOS getCFDIMailServicesPOS(){
		return (CFDI_MailServicesPOS)getInstance().getContext().getBean("cfdi_MailServicesPOS");
	}

	public static void main(String[] args) {
		//getInstance().getPedidosManager().buscarPendientes(getInstance().getConfiguracion().getSucursal());
		/*User user=getInstance().getLoginManager().autentificar("admin","sysadmin");
		System.out.println("Usuario localizado: "+user);
		System.out.println("Password: "+user.getPassword());
		*/
		//Services.getInstance().getSolicitudDeDepositosManager();
		//Venta venta=Services.getInstance().getFacturasManager().buscarVentaInicializada("8a8a8783-36f97b1a-0136-f9861d93-0001");
		//Services.getInstance().getComprobantesDigitalManager().generarComprobante(venta);
		/*String hql="from Pedido p where p.sucursal.clave=? " +
				"and  date(p.fecha) between ? and ? and p.totalFacturado=0 and p.facturable=true";
				*/
		/*Date f1=new Date();
		Date f2=DateUtils.addDays(f1, -30);
		String hql="from Pedido p where p.sucursal.clave=? and p.fecha between ? and ?";
		//Object[] params=new Object[]{Services.getInstance().getConfiguracion().getSucursal().getClave(),f1,f2};
		Object[] params=new Object[]{Services.getInstance().getConfiguracion().getSucursal().getClave(),f2,f1};
		List res= Services.getInstance().getHibernateTemplate().find(hql, params);
		System.out.println("Pedidos encontrados: "+res.size());
		*/
		CFDI_MailServicesPOS s=getCFDIMailServicesPOS();
		Assert.notNull(s,"No existe....");
	}

}
