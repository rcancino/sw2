package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.neo4j.cypher.internal.compiler.v2_1.perty.printToString;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.inventarios.model.Conteo;
import com.luxsoft.siipap.inventarios.model.ConteoDet;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.siipap.inventarios.model.Sector;
import com.luxsoft.siipap.inventarios.model.SectorDet;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.reports.AnalisisDeDifConteo;
import com.luxsoft.siipap.pos.ui.reports.ConteoFisicoAnalisisDiferenciasForm;
import com.luxsoft.siipap.pos.ui.reports.ConteoFisicoDiferenciaForm;
import com.luxsoft.siipap.pos.ui.reports.ConteoFisicoValidacionForm;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeExistencia;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeExistenciasParaConteo;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.ConteoController;
import com.luxsoft.sw3.ui.forms.ConteoForm;
import com.luxsoft.sw3.ui.forms.ConteoForm2;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Panel para el proceso de conteo de inventario
 * 
 * @author Ruben Cancino
 *
 */
public class ConteoDeInventarioPanel2 extends AbstractMasterDatailFilteredBrowserPanel<Conteo, ConteoDet>{
	
	
	public ConteoDeInventarioPanel2() {
		super(Conteo.class);
	}
	
	
	
	protected void init(){		
		super.init();
		addProperty("sector","sucursal.nombre","fecha","capturista","contador1","contador2","auditor1","auditor2","documento","comentario");
		addLabels("Sector","Sucursal","Fecha","Capturista","Contador1","Contador2","Auditor1","Auditor2","Folio","Comentario");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Sector", "sector");
		installTextComponentMatcherEditor("Capturista", "capturista");
		installTextComponentMatcherEditor("Contadores", "contador1","contador2");
		installTextComponentMatcherEditor("Auditores", "auditor1","auditor2");
		
		
		manejarPeriodo();
		periodo=Periodo.getPeriodoDelYear(Periodo.obtenerYear(new Date()));
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"renglon","documento","clave","unidad","descripcion","cantidad"};
		String[] labels={"Rngl","Folio","Producto","U","Descripcin","cantidad"};
		return GlazedLists.tableFormat(ConteoDet.class, props,labels);
	}

	@Override
	protected Model<Conteo, ConteoDet> createPartidasModel() {
		return new CollectionList.Model<Conteo, ConteoDet>(){
			public List<ConteoDet> getChildren(Conteo parent) {
				return parent.getPartidas();
			}
		};
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
		//		,getInsertAction()
				,getEditAction()
				,getViewAction()
		//		,CommandUtils.createDeleteAction(this, "cancelar")
				,CommandUtils.createPrintAction(this, "print")
		//		,addAction("", "consultarExistencias", "Consultar Existencias")
				};
		return actions;
	}	
	
	

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=new ArrayList<Action>();
		procesos.add(addAction("", "cargarSectores", "Cargar Sectores"));
		procesos.add(addAction("", "generarExistenciasParaConteoFisico", "Generar Existencias para conteo fsico"));
		//procesos.add(addAction("", "reporteDeConteoSelectovo", "Rep Conteo selectivo"));
		procesos.add(addAction("", "reporteNoCapturados", "Rep No Capturados"));
		procesos.add(addAction("", "reporteConteoValidacion", "Rep Validacin"));
		procesos.add(addAction("", "reporteDeDiferencias", "Rep. de Diferencias"));
		
		//procesos.add(addAction("", "generarExistenciasParaConteo", "Generar existencias"));
		procesos.add(addAction("", "aplicarConteo", "Fijar conteo"));		
		//procesos.add(addAction("", "reporteConteoFisicoAnalisisDiferencias", "Rep Anlisis Diferencias"));
		procesos.add(addAction("", "limpiarExistencias", "Limpiar existencias p/conteo"));
		procesos.add(addAction("", "reporteMedidasEspeciales", "Rep. Medidas especiales"));
		
		
		 
		return procesos;
	}

	@Override
	protected List<Conteo> findData() {
		
		String hql="from Conteo c where " +
				"c.fecha between ? and ? ";
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{
				periodo.getFechaInicial()
				,periodo.getFechaFinal()
				});
	}
	
	@Override
	protected Conteo doInsert() {
		final ConteoController controller=new ConteoController();
		final ConteoForm form=new ConteoForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			Conteo res=controller.persistir();
			controller.dispose();
			return res;
		}
		return null;
	}
	
	@Override
	public boolean doDelete(Conteo bean) {
		Services.getInstance().getUniversalDao().remove(Conteo.class, bean.getId());
		return true;
	}

	@Override
	protected Conteo doEdit(Conteo bean) {
		 
		if(validarInventarioFijado(bean.getFecha())){
			Conteo target=(Conteo)Services.getInstance().getHibernateTemplate().get(Conteo.class, bean.getId());
			final ConteoController controller=new ConteoController(target,false);
			final ConteoForm form=new ConteoForm(controller);
			form.open();
			if(!form.hasBeenCanceled()){
				return controller.persistir();
			}
			return bean;
		}
		return null;
	}
	
	@Override
	protected void doSelect(Object bean) {
		if(bean!=null){
			Conteo c=(Conteo)bean;
			final ConteoController controller=new ConteoController(c,true);
			final ConteoForm form=new ConteoForm(controller);
			form.open();
		}
	}

	@Override
	protected void afterInsert(Conteo bean) {
		super.afterInsert(bean);
		if(MessageUtils.showConfirmationMessage("Imprimir Captura?", "Conteo de inventarios"))
			print(bean);
		int index=source.indexOf(bean);
		if(index!=-1)
			selectionModel.setSelectionInterval(index, index);
	}
	

	public void cancelar() {
		
	}
	
	public void generarExistenciasParaConteo(){
		
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			final Date fecha=Services.getInstance().obtenerFechaDelSistema();
			
			SelectorDeExistencia finder=new SelectorDeExistencia();
			finder.open();
			if(!finder.hasBeenCanceled()){
				final List<Existencia> seleccion= finder.selectionModel.getSelected();
				final boolean parcial=finder.isParcial();
				//final List<Existencia> seleccion=SelectorDeExistencia.find();
				SwingWorker worker=new SwingWorker(){
					protected Object doInBackground() throws Exception {
						
						Services.getInstance().getInventariosManager().generarExistenciasParaConteo(seleccion,parcial, fecha,user.getFullName());
						return "OK";
					}
					protected void done() {
						MessageUtils.showMessage("Proceso terminado", "Generacin de existencias para conteo");
					}
				};
				TaskUtils.executeSwingWorker(worker);
			}
			
			
			
			
		}else{
			MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");
		}
		
		
		/*
		List<ExistenciaConteo> res=Services.getInstance().getHibernateTemplate()
				.find("from ExistenciaConteo e where e.sucursal.id=?",Configuracion.getSucursalLocalId());
		if(res.isEmpty()){
			if(KernellUtils.validarAcceso(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
				SwingWorker worker=new SwingWorker(){
					protected Object doInBackground() throws Exception {
						Date fecha=Services.getInstance().obtenerFechaDelSistema();
						Services.getInstance().getInventariosManager().generarExistenciasParaConteo(Configuracion.getSucursalLocalId(), fecha);
						return "OK";
					}
					protected void done() {
						MessageUtils.showMessage("Proceso terminado", "Generacin de existencias para conteo");
					}
				};
				TaskUtils.executeSwingWorker(worker);
			}
		}else{
			MessageUtils.showMessage("Ya han sido generadas las existencias para inventario fsico", "");
		}
		*/
	}
	
	
public void generarExistenciasParaConteoFisico(){
	
	final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
	if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
		List<ExistenciaConteo> res=Services.getInstance().getHibernateTemplate()
				.find("from ExistenciaConteo e where e.sucursal.id=? and date(e.log.creado)=?",new Object[]{Configuracion.getSucursalLocalId(),new Date()});
		if(res.isEmpty()){
			SwingWorker worker=new SwingWorker(){
				protected Object doInBackground() throws Exception {
					Date fecha=Services.getInstance().obtenerFechaDelSistema();
					Services.getInstance().getInventariosManager().generarExistenciasParaConteo(Configuracion.getSucursalLocalId(), fecha,"");
					return "OK";
				}
				protected void done() {
					MessageUtils.showMessage("Proceso terminado", "Generacin de existencias para conteo");
				}
			};
			TaskUtils.executeSwingWorker(worker);
		} else{
			MessageUtils.showMessage("Ya han sido generadas las existencias para inventario fsico", "");
		}
		
	}else{
		MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");
	}
	

		
	}
	 
	
	public void aplicarConteo(){
		List<Long> res=Services.getInstance().getHibernateTemplate()
			.find("select count(e.id) from ExistenciaConteo e where e.sucursal.id=? and date(e.log.creado)=? and e.fijado is not  null"
					,new Object[]{Configuracion.getSucursalLocalId(),new Date()});
		Long contados=res.get(0);
		if(contados.intValue()==0){			
			final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
			if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
				
				try {
					doAplicarConteo();
					Date fecha=new Date();
					Services.getInstance().getInventariosManager().generarAjusteDeInventario(Services.getInstance().getConfiguracion().getSucursal(), fecha);
					
					boolean recalcular=MessageUtils.showConfirmationMessage("Los Inventario ha sido fijado y el ajuste se ha generado  Recalcular Productos ?", "Conteo de Inventario");
					if(recalcular){
						calcularExistencias();
					}else{
					MessageUtils.showMessage("No se recalcularon las Existencias Recalcular manualmente", "Conteo de Inventario" );	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else{
				MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");
			}
		}else{
			MessageUtils.showMessage("Los conteos ya han sido fijados", "");
		}
	}
	
	private void calcularExistencias(){
		final Date fecha=new Date();
		final int mes=Periodo.obtenerMes(fecha)+1;
		final int year=Periodo.obtenerYear(fecha);
		System.out.println("Recalculando existencia para Ao:"+year+ " Mes: "+mes+" Fecha:"+fecha);
		SwingWorker worker=new SwingWorker(){			
			protected Object doInBackground() throws Exception {
				Services.getInstance().getExistenciasDao()
				.actualizarExistencias(Services.getInstance().getConfiguracion().getSucursal().getId(),year , mes);
				return "OK";
			}
			protected void done() {
				try {
					get();
					MessageUtils.showMessage("Existencias actualizadas", "Recalculo de existencias");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}			
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void  cargarSectores(){
		
		
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			
			try {
				
				ConteoController controller = new ConteoController();
				String hql="from Sector c ";
				List<Sector> sectores= Services.getInstance().getHibernateTemplate().find(hql);
				for(Sector sector:sectores){
					System.out.println("------Sector :"+sector);
					Conteo conteo=new Conteo();
						conteo.setSucursal(sector.getSucursal());
						conteo.setFecha(new Date());
						conteo.setSector(sector.getSector());
						conteo.setReplicado(null);
						conteo.setImportado(null);
						conteo.setComentario(sector.getComentario());
						conteo.setContador1(sector.getResponsable1());
						conteo.setContador2(sector.getResponsable2());
						
						
					List<SectorDet> sectoresDet=sector.getPartidas();
					for(SectorDet sectordet:sectoresDet){
						System.out.println("      ----Partida :"+sectordet);
						ConteoDet conteoDet=new ConteoDet();
										conteoDet.setConteo(conteo);
										
						conteoDet.setProducto(sectordet.getProducto());
						conteo.agregarPartida(conteoDet);
						

					}
					Conteo d=Services.getInstance().getInventariosManager().registrarConteo(conteo);
					System.out.println("Conteo"+conteo);
				}
				afterCargar();
			} catch (Exception e) {
				DateFormat df=new SimpleDateFormat("dd-MM-yyyy");
				MessageUtils.showMessage("Ya existen sectores cargados en esta fecha: "+df.format(new Date()), "Inventarios");
			}
			
		}else{
			MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");
		}
	}
	 public void afterCargar(){
		 MessageUtils.showMessage("Sectores Cargados", "Carga de Sectores");
		 load();
	 }
	
 

	 
	 
	public void doAplicarConteo(){
		final Date time=new Date();
		String hql="from ExistenciaConteo e where e.sucursal.id=? and fijado is null and date(e.log.creado)=? order by e.clave";
		Object[] params={Configuracion.getSucursalLocalId(),time};
		List<ExistenciaConteo> existencias=Services.getInstance()
			.getHibernateTemplate()
			.find(hql,params);
		for(ExistenciaConteo exis:existencias){
			double conteoEnLinea=exis.getConteoEnLinea();
			//BigDecimal conteoEnLinea=BigDecimal.valueOf(exis.getConteoEnLinea()).setScale(3);
			exis.setConteo(conteoEnLinea);
			exis.actualizar();
			try {
				exis.setFijado(time);
				String hql2="select d.conteo.sector from ConteoDet d  where d.conteo.sucursal.id=? and d.clave=?  and date(d.conteo.log.creado)=?";
				Set<Integer> sectores=new TreeSet<Integer>(Services
						.getInstance().getHibernateTemplate().find(hql2,new Object[]{Configuracion.getSucursalLocalId(),exis.getClave(),new Date()}));
				Iterator<Integer> secIter=sectores.iterator();
				StringBuffer buff=new StringBuffer();
				while(secIter.hasNext()){
					buff.append(String.valueOf(secIter.next()));
					if(secIter.hasNext())
						buff.append(",");
				}
				exis.setSectores(buff.toString());
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Services.getInstance().getUniversalDao().save(exis);
		}
	}
	
	
	public void doAplicarConteo(List<ExistenciaConteo> existencias){
		Date time=Services.getInstance().obtenerFechaDelSistema();
		
		for(ExistenciaConteo row:existencias){
			int index=source.indexOf(row);
			ExistenciaConteo exis=(ExistenciaConteo)Services.getInstance().getHibernateTemplate().get(ExistenciaConteo.class, row.getId());
			double conteoEnLinea=exis.getConteoEnLinea();
			exis.setConteo(conteoEnLinea);
			exis.actualizar();
			try {
				exis.setFijado(time);
				String hql2="select d.conteo.sector from ConteoDet d  where d.conteo.sucursal.id=? and d.clave=?";
				Set<Integer> sectores=new TreeSet<Integer>(Services
						.getInstance().getHibernateTemplate().find(hql2,new Object[]{Configuracion.getSucursalLocalId(),exis.getClave()}));
				Iterator<Integer> secIter=sectores.iterator();
				StringBuffer buff=new StringBuffer();
				while(secIter.hasNext()){
					buff.append(String.valueOf(secIter.next()));
					if(secIter.hasNext())
						buff.append(",");
				}
				exis.setSectores(buff.toString());
				if(index!=-1){
					source.set(index, exis);
				}
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Services.getInstance().getUniversalDao().save(exis);
		}
	}
	
	public void consultarExistencias(){
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			SelectorDeExistenciasParaConteo.seleccionar(Configuracion.getSucursalLocalId());
		}else{MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");}
	
	}

	public void print(){
	
			for(Object o:getSelected()){
				Conteo dec=(Conteo)o;
				print(dec);	
			}

	}
	
	public void print(Conteo dec){
		
			Map params=new HashMap();
			params.put("CONTEO_ID", dec.getId());
		//	ReportUtils2.runReport("invent/ConteoFisico.jasper", params);
			ReportUtils.printReport("file:/mnt/siipapwin/Reportes_MySQL/invent/ConteoFisico.jasper", params, false);
		//	ReportUtils.printReport("file:z:/Reportes_MySQL/invent/ConteoFisico.jasper", params, false);
			
		
		
	}
	
	public void reporteDeConteoSelectovo(){
		
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			
			ConteoSelectivoDeInventarioForm.run();
			
		}else{MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");}
		
	}
	
	public void reporteConteoFisicoAnalisisDiferencias(){
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			
			ConteoFisicoAnalisisDiferenciasForm.run();
			
		}else{MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");}
		
	}
	
	public void reporteConteoValidacion(){
		
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			
			ConteoFisicoDiferenciaForm.run();
			
		}else{MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");}
		
		
	}
	
	public void reporteNoCapturados(){
		
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			
			Map map =new HashMap();
			map.put("SUCURSAL_ID", Configuracion.getSucursalLocalId());
			ReportUtils2.runReport("invent/NoCapturados.jasper",map);
			
		}else{MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");}
			
	}
	
	 public void  reporteDeDiferencias(){
		 final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		 if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			 AnalisisDeDifConteo.run(); 
		 }else{MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");}
		 
	 }
	
	public void reporteMedidasEspeciales(){
		
		final User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			
			Map map =new HashMap();
			String exis="NO";
			if(MessageUtils.showConfirmationMessage("Con Existencias", "Medidas especiales"))
				exis="SI";
			map.put("IMP_EXIS", exis);
			map.put("SUCURSAL", Configuracion.getSucursalLocalId());
			ReportUtils2.runReport("invent/MedidasEspeciales.jasper",map);
			
		}else{MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");}
		
		
		
	}
	
	public void limpiarExistencias(){
		if(KernellUtils.validarAcceso(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
			doLimpiarExistencias();
		}
	}
	
	private void doLimpiarExistencias(){
		SwingWorker worker=new SwingWorker(){
			protected Object doInBackground() throws Exception {
				String sql="delete from sx_existencia_conteo where sucursal_id=?";
				int dels=Services.getInstance().getJdbcTemplate().update(sql,new Object[]{Configuracion.getSucursalLocalId()});
				System.out.println("Registros eliminados: "+dels);
				return "OK";
			}			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void doLimpiarConteos(){
		
	}
	
	private Boolean validarInventarioFijado(){
		List<ExistenciaConteo> res=Services.getInstance().getHibernateTemplate()
				.find("from ExistenciaConteo e where e.sucursal.id=? and date(e.fecha)=?"
						,new Object[]{Configuracion.getSucursalLocalId(),new Date()});
		if(!res.isEmpty() && res.get(0).getFijado()!=null){
			JOptionPane.showMessageDialog(null,"Atencin:  Inventario ya fijado" ); 
			
			return false;
		}

		return true;
	}
	
	
	private Boolean validarInventarioFijado(Date fechaValid){
		List<ExistenciaConteo> res=Services.getInstance().getHibernateTemplate()
				.find("from ExistenciaConteo e where e.sucursal.id=? and date(e.fecha)=?"
						,new Object[]{Configuracion.getSucursalLocalId(),fechaValid});
		if(!res.isEmpty() && res.get(0).getFijado()!=null){
			JOptionPane.showMessageDialog(null,"Atencin:  Inventario ya fijado" ); 
			
			return false;
		}

		return true;
	}
	
}
