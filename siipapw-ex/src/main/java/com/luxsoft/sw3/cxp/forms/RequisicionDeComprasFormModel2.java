package com.luxsoft.sw3.cxp.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeContraRecibos;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.selectores.SelectorDeAnalisisDeHojeo;
import com.luxsoft.sw3.cxp.selectores.SelectorDeAnalisisDeMaterialMaquila;
import com.luxsoft.sw3.cxp.selectores.SelectorDeAnalisisDeTransformaciones;
import com.luxsoft.sw3.cxp.selectores.SelectorDeAnalisisPorRequisitar;
import com.luxsoft.sw3.cxp.selectores.SelectorDeFacturasDeCompras;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;


public class RequisicionDeComprasFormModel2 extends DefaultFormModel{
	
	private EventList<RequisicionDe> partidas;
	
	public RequisicionDeComprasFormModel2(Requisicion bean) {
		super(bean);		
	}
	
	public RequisicionDeComprasFormModel2() {
		super(new Requisicion());
	}
	
	public Requisicion getRequisicion(){
		return (Requisicion)getBaseBean();
	}
	
	@Override
	protected void init() {
		partidas=GlazedLists.eventList(getRequisicion().getPartidas());
		//Handlers		
		final MonedaHandler monedaHandler=new MonedaHandler();		
		getModel("moneda").addValueChangeListener(monedaHandler);
		definirTipoDeCambio();
		getModel("fecha").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				definirTipoDeCambio();
			}
		});
		getModel("descuentoFinanciero").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("DF actualizado..");
				actualizarPartidas();
			}
		});
		getModel("comentario").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Comentarion :"+evt.getNewValue());
			}
		});
	}
	
	protected void addValidation(PropertyValidationSupport support) {
		if(getRequisicion().getTotal().amount().doubleValue()<=0)
			support.addError("Total", "Se requiere el importe de la requisición");
		for(RequisicionDe det:getRequisicion().getPartidas()){
			if(det.getTotal().amount().doubleValue()<=0){
				support.addError("partidas"
						,MessageFormat.format("La factura {0} tiene un importe incorrecto de: {1} ",det.getDocumento(),det.getTotal()));
			}
		}
	}
	
	
	
	public void delete(RequisicionDe det){
		logger.info("Eliminando partida: "+det);
		boolean res=getRequisicion().eleiminarPartida(det);
		logger.info("Analisis por desvincular: "+getRequisicion().getAnalisisPorActualizar().size());
		if(res){
			//getRequisicion().actualizarTotal();
			partidas.remove(det);
			actualizarPartidas();
			//System.out.println("Nuevo total: "+getRequisicion().getTotal());
		}
	}
	
	public void insertar(){
		Proveedor p=getRequisicion().getProveedor();
		if(isReadOnly() || (p==null)) 
			return; //Make sure nothing happends when the form is read-only	
		if(getRequisicion().getConcepto()!=null){
			if(getRequisicion().getConcepto().getClave().equals("PAGO")){
				insertarAnalisis();
			}else if(getRequisicion().getConcepto().getClave().equals("PAGO PARCIAL")){
				insertarAnalisis();
			}else if(getRequisicion().getConcepto().getClave().equals("ANTICIPO")){
				insertarFacturaAnticipo();
			}else if(getRequisicion().getConcepto().getClave().equals("FLETE")){
				insertarFacturaFlete();
			}else if(getRequisicion().getConcepto().getClave().equals("TRANSFORMACION")){
				insertarFacturaGastoTRS();
			}else if(getRequisicion().getConcepto().getClave().equals("HOJEO")){
				insertarFacturaHojeo();
			}else if(getRequisicion().getConcepto().getClave().equals("MP MAQUILA")){
				insertarFacturaMPMaquila();
			}
		}
	}
	
	public void insertarPorContrarecibo(){
		Proveedor p=getRequisicion().getProveedor();
		if(isReadOnly() || (p==null)) 
			return; //Make sure nothing happends when the form is read-only		
		List<String> numeros=SelectorDeContraRecibos.buscarFacturasRecibidasPendientes(p);
		
		List<AnalisisDeFactura> analisis=ServiceLocator2.getAnalisisDeCompraManager().buscarAnalisisPorFactura(numeros, p);
		System.out.println("Insertar analisis por contrarecibo");
		procesarAnalisis(analisis);
	}
	
	private void insertarAnalisis(){
		List<AnalisisDeFactura> analisis=SelectorDeAnalisisPorRequisitar.buscarFacturas(
				getRequisicion().getProveedor(), 
				getRequisicion().getMoneda());
		if(!analisis.isEmpty()){
			procesarAnalisis(analisis);
		}
	}
	
	private void procesarAnalisis(final List<AnalisisDeFactura> analisis){
		for(AnalisisDeFactura a:analisis){
				System.out.println("Procesando Analisis"+ a.getId());
			
			if(a.getRequisicionDet()!=null){
				MessageUtils.showMessage("La factura "+a.getFactura().getDocumento()+" ya existe en la requisición: ", "Requisición de compras");
				
				continue;
			}
			CXPFactura fac=a.getFactura();
			// Garantizar misma moneda
			if(getRequisicion().getMoneda().equals(fac.getMoneda())){
				RequisicionDe det=new RequisicionDe();
				det.setComentario("Pag Factura de compras");
				det.setFacturaDeCompras(fac);
				det.setAnalisis(a);
				a.setRequisicionDet(det);
				det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
				//det.setTotal(fac.getPorRequisitar(isDescuentoFinanciero()));
				boolean res=getRequisicion().agregarPartida(det);
				if(res){
					partidas.add(det);
					actualizarPartidas();
				}
			}			
		}
		
	}
	
	public void insertarFacturaAnticipo(){
		final Proveedor p=getRequisicion().getProveedor();
		final Currency moneda=getRequisicion().getMoneda();
		final double df=getRequisicion().getDescuentoFinanciero();
		
		if(p!=null){
			SelectorDeFacturasDeCompras selector = new SelectorDeFacturasDeCompras(){
				@Override
				protected List<CXPFactura> getData() {
					String hql="from CXPFactura f where f.proveedor.id=? and f.anticipof=true  " +
							"and f.moneda=? and f.requisitado=0";
					return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{p.getId(),moneda});
				}
			};
			selector.setTitle("Facturas de Anticipos");
			selector.open();
			CXPFactura fac=selector.getSelected();
			if(fac!=null){
				
				if(getRequisicion().getMoneda().equals(fac.getMoneda())){
					RequisicionDe det=new RequisicionDe();
					det.setComentario("Pag Factura de compras");
					det.setFacturaDeCompras(fac);
					det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
					//det.setTotal(new CantidadMonetaria(fac.getSaldoCalculado(),moneda));
					det.setTotal(new CantidadMonetaria(fac.getSaldoCalculado(),moneda));
					boolean res=getRequisicion().agregarPartida(det);
					if(res){
						partidas.add(det);
			//			actualizarPartidas();
						logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
						getRequisicion().actualizarTotal();
						//logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
						firePropertyChange("total", null, getRequisicion().getTotal());
						validate();
					}
					
				}	
			}
		}
	}
	
	private void insertarFacturaGastoTRS(){
		final Proveedor p=getRequisicion().getProveedor();
		final Currency moneda=getRequisicion().getMoneda();
		final double df=getRequisicion().getDescuentoFinanciero();
		if(p!=null){
			SelectorDeAnalisisDeTransformaciones selector=new SelectorDeAnalisisDeTransformaciones(){
				@Override
				protected List<AnalisisDeTransformacion> getData() {
					String hql=" from AnalisisDeTransformacion a " +
							" where a.proveedor.id=? " +
							"   and a.cxpFactura.moneda=? " +
							"   and a.cxpFactura.saldoReal>0" +
							"  and a.cxpFactura.id not in(select x.facturaDeCompras.id from RequisicionDe x where x.facturaDeCompras.proveedor.id=a.proveedor.id)";
					return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{p.getId(),moneda});
				}
			};
			
			selector.setTitle("Análisis de facturas por Transformación");
			selector.open();
			List<AnalisisDeTransformacion> selected=new ArrayList<AnalisisDeTransformacion>();
			selected.addAll(selector.getSelectedList());
			for(AnalisisDeTransformacion a:selected){
				CXPFactura fac=a.getCxpFactura();
				if(fac!=null){
					if(getRequisicion().getMoneda().equals(fac.getMoneda())){
						RequisicionDe det=new RequisicionDe();
						det.setComentario("Pag Factura de compras (TRS)");
						det.setFacturaDeCompras(fac);
						det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
						det.setTotal(new CantidadMonetaria(a.getAnalizadoConIva(),moneda));
						boolean res=getRequisicion().agregarPartida(det);
						if(res){
							partidas.add(det);
					//		actualizarPartidas();
							AnalisisDeTransformacion analisis=a;
							
							/*CantidadMonetaria ta=new CantidadMonetaria(analisis.getImporte(),getRequisicion().getMoneda());*/
							CantidadMonetaria ta= new CantidadMonetaria(a.getAnalizado(),moneda);
											
								if(df>0){						
									ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
									det.getFacturaDeCompras().setDescuentoFinanciero(df);
								}else{
									det.getFacturaDeCompras().setDescuentoFinanciero(df);
								}
								ta=ta.subtract(analisis.getCxpFactura().getBonificadoCM());
								ta=ta.subtract(analisis.getCxpFactura().getDevolucionesCM());
							
								if(df>0){
									ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
									det.getFacturaDeCompras().setDescuentoFinanciero(df);
								}
							
							det.setTotal(ta);					
						det.setImporte(MonedasUtils.calcularImporteDelTotal(det.getTotal()));
						det.setImpuesto(MonedasUtils.calcularImpuesto(det.getImporte()));
						//tot=tot.add(det.getTotal());
					
						logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
						getRequisicion().actualizarTotal();
						//logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
						firePropertyChange("total", null, getRequisicion().getTotal());
						validate();
							
						}
					}	
				}
			}
			
		}
	}
	
	private void insertarFacturaFlete(){
		final Proveedor p=getRequisicion().getProveedor();
		final Currency moneda=getRequisicion().getMoneda();
		final double df=getRequisicion().getDescuentoFinanciero();
		if(p!=null){
			SelectorDeFacturasDeCompras selector = new SelectorDeFacturasDeCompras(){
				@Override
				protected List<CXPFactura> getData() {
					String hql="select a.cxpFactura " +
							" from AnalisisDeFlete a " +
							" where a.proveedor.id=? " +
							"   and a.cxpFactura.moneda=? " +
							"   and a.cxpFactura.saldoReal>0";
					return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{p.getId(),moneda});
				}
			};
			selector.setTitle("Facturas de Fletes analizados");
			selector.open();
		    
			/*List<CXPFactura> facs=selector.getSelectedList();
			 for(CXPFactura fac:facs){
				 if(fac!=null){
					 if(!fac.getRequisitado().equals(BigDecimal.ZERO)){
						 MessageUtils.showMessage("La factura "+fac.getDocumento()+" ya existe en la requisición: "
									, "Requisición de Flete");
							continue;
					 }
						if(getRequisicion().getMoneda().equals(fac.getMoneda())){
							RequisicionDe det=new RequisicionDe();
							det.setComentario("Pag Factura de compras (Flete)");
							det.setFacturaDeCompras(fac);
							det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
							det.setTotal(new CantidadMonetaria(fac.getSaldoCalculado(),moneda));
							boolean res=getRequisicion().agregarPartida(det);
							if(res){
								partidas.add(det);
					//			actualizarPartidas();
								logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
								getRequisicion().actualizarTotal();
								//logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
								firePropertyChange("total", null, getRequisicion().getTotal());
								validate();
							}
						}	
					}
			 }*/
			CXPFactura fac=selector.getSelected();
			if(fac!=null){
				if(getRequisicion().getMoneda().equals(fac.getMoneda())){
					RequisicionDe det=new RequisicionDe();
					det.setComentario("Pag Factura de compras (Flete)");
					det.setFacturaDeCompras(fac);
					det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
					det.setTotal(new CantidadMonetaria(fac.getSaldoCalculado(),moneda));
					boolean res=getRequisicion().agregarPartida(det);
					if(res){
						partidas.add(det);
			//			actualizarPartidas();
						logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
						getRequisicion().actualizarTotal();
						//logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
						firePropertyChange("total", null, getRequisicion().getTotal());
						validate();
					}
				}	
			}
		}
	}
	
	private void insertarFacturaHojeo(){
		final Proveedor p=getRequisicion().getProveedor();
		final Currency moneda=getRequisicion().getMoneda();
		final double df=getRequisicion().getDescuentoFinanciero();
		if(p!=null){
	
			List<AnalisisDeHojeo> selected=SelectorDeAnalisisDeHojeo.find();
			for(AnalisisDeHojeo a:selected){
				
				if(a.getCxpFactura().getRequisitado().longValue()!=0){
					System.err.println("---------------------"+a.getCxpFactura().getRequisitado() +"-----------"+a.getCxpFactura().getDocumento());
					MessageUtils.showMessage("La factura "+a.getCxpFactura().getDocumento()+" ya existe en la requisición: "+a.getCxpFactura()
							, "Requisición de compras");
					continue;
				}
				
				CXPFactura fac=a.getCxpFactura();
				if(getRequisicion().getMoneda().equals(fac.getMoneda())){
					RequisicionDe det=new RequisicionDe();
					det.setComentario("Pag Factura de compras (Hojeo)");
					det.setFacturaDeCompras(fac);
					det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
					det.setTotal(new CantidadMonetaria(a.getAnalizado(),moneda));
					boolean res=getRequisicion().agregarPartida(det);
					if(res){
						partidas.add(det);
		//				actualizarPartidas();
						AnalisisDeHojeo analisis=a;
						
						/*CantidadMonetaria ta=new CantidadMonetaria(analisis.getImporte(),getRequisicion().getMoneda());*/
						CantidadMonetaria ta= new CantidadMonetaria(a.getAnalizado(),moneda);
										
							if(df>0){						
								ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
								det.getFacturaDeCompras().setDescuentoFinanciero(df);
							}else{
								det.getFacturaDeCompras().setDescuentoFinanciero(df);
							}
							ta=ta.subtract(analisis.getCxpFactura().getBonificadoCM());
							ta=ta.subtract(analisis.getCxpFactura().getDevolucionesCM());
						
							if(df>0){
								ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
								det.getFacturaDeCompras().setDescuentoFinanciero(df);
							}
						
						det.setTotal(ta);					
					det.setImporte(MonedasUtils.calcularImporteDelTotal(det.getTotal()));
					det.setImpuesto(MonedasUtils.calcularImpuesto(det.getImporte()));
					//tot=tot.add(det.getTotal());
				
					logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
					getRequisicion().actualizarTotal();
					//logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
					firePropertyChange("total", null, getRequisicion().getTotal());
					validate();
					}
				}
			}
		}
	}
	
	private void insertarFacturaMPMaquila(){
		final Proveedor p=getRequisicion().getProveedor();
		final Currency moneda=getRequisicion().getMoneda();
		final double df=getRequisicion().getDescuentoFinanciero();
		if(p!=null){
			List<AnalisisDeMaterial> selected=SelectorDeAnalisisDeMaterialMaquila.find();
			for(AnalisisDeMaterial a:selected){
				CXPFactura fac=a.getCxpFactura();
				if(getRequisicion().getMoneda().equals(fac.getMoneda())){
					RequisicionDe det=new RequisicionDe();
					det.setComentario("Pag Factura de compras (MP Maquila)");
					det.setFacturaDeCompras(fac);
					det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
					det.setTotal(new CantidadMonetaria(a.getTotal(),moneda));
					boolean res=getRequisicion().agregarPartida(det);
					if(res){
						partidas.add(det);
				//		actualizarPartidas();
						AnalisisDeMaterial analisis=a;
						
						/*CantidadMonetaria ta=new CantidadMonetaria(analisis.getImporte(),getRequisicion().getMoneda());*/
						CantidadMonetaria ta= new CantidadMonetaria(a.getTotal(),moneda);
										
							if(df>0){						
								ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
								det.getFacturaDeCompras().setDescuentoFinanciero(df);
							}else{
								det.getFacturaDeCompras().setDescuentoFinanciero(df);
							}
							ta=ta.subtract(analisis.getCxpFactura().getBonificadoCM());
							ta=ta.subtract(analisis.getCxpFactura().getDevolucionesCM());
						
							if(df>0){
								ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
								det.getFacturaDeCompras().setDescuentoFinanciero(df);
							}
						
						det.setTotal(ta);
					det.setImporte(MonedasUtils.calcularImporteDelTotal(det.getTotal()));
					det.setImpuesto(MonedasUtils.calcularImpuesto(det.getImporte()));
				
					logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
					getRequisicion().actualizarTotal();
					//logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
					firePropertyChange("total", null, getRequisicion().getTotal());
					validate();
					}					
				}
			}
		}
	}
	
	
	private void actualizarPartidas(){
		
		//CantidadMonetaria tot=new CantidadMonetaria(0d,getRequisicion().getMoneda());
		final double df=getRequisicion().getDescuentoFinanciero();
		
		for(RequisicionDe det:getRequisicion().getPartidas()){			
			AnalisisDeFactura analisis=det.getAnalisis();
			if(analisis!=null){				
				CantidadMonetaria ta=new CantidadMonetaria(analisis.getImporte(),getRequisicion().getMoneda());
				ta=ta.add(MonedasUtils.calcularImpuesto(ta));
				if(analisis.isPrimerAnalisis()){					
					CXPFactura fac=analisis.getFactura();
					 ta=new CantidadMonetaria(fac.getTotalAnalizadoConFlete(),fac.getMoneda());					
					if(df>0){						
						ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
						det.getFacturaDeCompras().setDescuentoFinanciero(df);
					}else{
						det.getFacturaDeCompras().setDescuentoFinanciero(df);
					}
					ta=ta.subtract(analisis.getFactura().getBonificadoCM());
					ta=ta.subtract(analisis.getFactura().getDevolucionesCM());
				}else{
					if(df>0){
						ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, df);
						det.getFacturaDeCompras().setDescuentoFinanciero(df);
					}
				}
				det.setTotal(ta);
				//tot=tot.add(det.getTotal());
				int index = partidas.indexOf(det);
				if(index>=0)
					partidas.set(index,det);
			}				
			det.setImporte(MonedasUtils.calcularImporteDelTotal(det.getTotal()));
			det.setImpuesto(MonedasUtils.calcularImpuesto(det.getImporte()));
			//tot=tot.add(det.getTotal());
		}
		logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
		getRequisicion().actualizarTotal();
		logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
		firePropertyChange("total", null, getRequisicion().getTotal());
		validate();
	}

	
	public void editar(final RequisicionDe source) {
		RequisicionDe target=new RequisicionDe();
		BeanUtils.copyProperties(source, target,new String[]{"id"});
		DefaultFormModel model=new DefaultFormModel(target,false);
		RequisicionDeComprasDetForm form=new RequisicionDeComprasDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){			
			int index=partidas.indexOf(source);
			if(index!=-1){
				BeanUtils.copyProperties(target, source,new String[]{"id"});
				partidas.set(index, source);
				//actualizarPartidas();
				for(RequisicionDe det:getRequisicion().getPartidas()){	
					det.setImporte(MonedasUtils.calcularImporteDelTotal(det.getTotal()));
					det.setImpuesto(MonedasUtils.calcularImpuesto(det.getImporte()));
				}
				logger.debug("Actualizando Req Total actual: "+getRequisicion().getTotal());
				getRequisicion().actualizarTotal();
				logger.debug("Partidas: "+getRequisicion().getPartidas().size()+" Nvo total: "+getRequisicion().getTotal());
				firePropertyChange("total", null, getRequisicion().getTotal());
				validate();
			}
		}
		
	}
	
	public void view(final RequisicionDe det){
		DefaultFormModel model=new DefaultFormModel(det,true);
		RequisicionDeComprasDetForm form=new RequisicionDeComprasDetForm(model);
		form.open();
	}
	
	public EventList<RequisicionDe> getPartidas() {
		return partidas;
	}
	
	public List<Concepto> getConceptosValidos(){
		final List<Concepto> data=ServiceLocator2.getUniversalDao().getAll(Concepto.class);
		CollectionUtils.filter(data, new Predicate(){
			public boolean evaluate(Object object) {
				Concepto cc=(Concepto)object;
				return  cc.getClave().equals("PAGO")
						|| cc.getClave().equals("PAGO PARCIAL")
						|| cc.getClave().equals("ANTICIPO")
						|| cc.getClave().equals("FLETE")
						|| cc.getClave().equals("TRANSFORMACION")
						|| cc.getClave().equals("HOJEO")
						|| cc.getClave().equals("MP MAQUILA")
						;
				 
			}
		});
		return data;
	}
	
	protected void definirTipoDeCambio(){
		if(getValue("id")==null){
			if(getValue("moneda").equals(MonedasUtils.PESOS)){
				setValue("tipoDeCambio", BigDecimal.ONE);
				getComponentModel("tipoDeCambio").setEnabled(false);
			}else if(getValue("moneda").equals(MonedasUtils.DOLARES)){
				double tc=ServiceLocator2.buscarTipoDeCambio(getRequisicion().getFecha());
				setValue("tipoDeCambio", BigDecimal.valueOf(tc));
				getComponentModel("tipoDeCambio").setEnabled(true);
			}else{				
				setValue("tipoDeCambio", BigDecimal.ONE);
				getComponentModel("tipoDeCambio").setEnabled(true);
			}
		}
	}
	
	/**
	 * Listener que detecta cambios en la moneda  para aplicar las reglas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class MonedaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(getValue("id")==null){
				getRequisicion().eliminarPartidas();
				partidas.clear();
				definirTipoDeCambio();
			}
			
		}
	}


}
