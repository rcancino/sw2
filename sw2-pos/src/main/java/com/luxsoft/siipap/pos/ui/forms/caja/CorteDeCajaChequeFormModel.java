package com.luxsoft.siipap.pos.ui.forms.caja;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDePagos;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.services.Services;

/**
 * Modelo y controller para el corte de caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CorteDeCajaChequeFormModel extends DefaultFormModel {
		
	private EventList<Pago> cheques;
	
	private EventList<Ficha> fichasMismoBanco;
	
	private EventList<Ficha> fichasOtrosBancos;
	
	private EventList<Pago> chequeMismoBanco;
	
	private EventList<Pago> chequesOtrosBanco;
	
	 
	
	
 
	public CorteDeCajaChequeFormModel() {
		super(Bean.proxy(CorteDeChequesViewModel.class));
	}
	
	@Override
	protected void init() {
		CorteDeChequesViewModel viewModel=getViewModel();
		viewModel.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		viewModel.setCuenta(Services.getInstance().getConfiguracion().getCuentaPreferencial());
		viewModel.setCuenta(getCuenta());
		Date time=new Date();
		viewModel.setFecha(time);
		
		Handler handler=new Handler();		
		getModel("fecha").addValueChangeListener(handler);
		getModel("origen").addValueChangeListener(handler);
		initGlazedLists();
	}
	
	private void initGlazedLists(){
		Matcher<Pago> m1=Matchers.beanPropertyMatcher(Pago.class, "banco", getCuenta().getBanco().getNombre());
		Matcher<Pago> m2=Matchers.invert(m1);
		chequeMismoBanco=new FilterList<Pago>(getChequesSource(),m1);
		chequesOtrosBanco=new FilterList<Pago>(getChequesSource(),m2);
		
		//this.fichasMismoBanco=new FunctionList<List<Pago>, Ficha>(this.chequeMismoBanco);
		
		ListHandler hanler=new ListHandler();
		getChequesSource().addListEventListener(hanler);
		 
	}
	
	private EventList<Pago> getChequesSource(){
		if(this.cheques==null){
			Comparator<Pago> c1=GlazedLists.beanPropertyComparator(Pago.class, "id");
			cheques=new UniqueList(new BasicEventList<Pago>(0),c1);
			Matcher<Pago> matcher=new Matcher<Pago>(){
				public boolean matches(Pago item) {
					return item.getDeposito()==null;
				}
			};
			cheques=new FilterList<Pago>(cheques,matcher);
		}
		return cheques;
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		validarCorteDeCaja(support);		
	}	
	
	private void validarCorteDeCaja(PropertyValidationSupport support){
		if(cheques!=null && cheques.size()==0){
			support.getResult().addError("Debe registrar los cheques");			
		}
	}

	public CorteDeChequesViewModel getViewModel(){
		return (CorteDeChequesViewModel)getBaseBean();
	}
	
	public void actualizarImportes(){
		BigDecimal total=BigDecimal.ZERO;
		for(Pago cheque:getChequesSource()){
			total=total.add(cheque.getTotal());
		}
		getViewModel().setImporte(total);
		getViewModel().setCheques(cheques.size());
		getViewModel().setChequesMismoBanco(this.chequeMismoBanco.size());
		getViewModel().setChequesOtrosBancos(this.chequesOtrosBanco.size());
		
		getViewModel().setImporteCheques(total);
		BigDecimal chmb=BigDecimal.ZERO;
		for(Pago cheque:this.chequeMismoBanco){
			chmb=chmb.add(cheque.getTotal());
		}
		getViewModel().setImporteChequesMismoBanco(chmb);
		
		BigDecimal chob=BigDecimal.ZERO;
		for(Pago cheque:this.chequesOtrosBanco){
			chob=chob.add(cheque.getTotal());
		}
		getViewModel().setImporteChequesOtrosBancos(chob);
		actualizarFichas();
		
	}
	
	public void actualizarPagos(){
		if(getViewModel().getOrigen()==null)
			return;
		Object[] params={
				getViewModel().getFecha()
				,getViewModel().getSucursal().getId()
				};
		String hql="select sum(p.total) from PagoConCheque p " +
				"where p.fecha=? " +
				" and p.sucursal.id=?" +				
				" and p.origenAplicacion=\'@ORIGEN\'" +
				" and p.deposito is not null" 				
				;
		hql=hql.replaceAll("@ORIGEN", getViewModel().getOrigen().name());		
		List<BigDecimal> res=Services.getInstance().getHibernateTemplate().find(hql, params);
		BigDecimal pagosConCheque=res.get(0)==null?BigDecimal.ZERO:res.get(0);
		getViewModel().setPagos(pagosConCheque);
		logger.info("Total de Pagos con cheque: "+pagosConCheque);
		
		String hql2="select sum(p.total) from PagoPorCambioDeCheque p " +
			"where p.fecha=? " +
			"  and p.sucursal.id=?" +				
			"  and p.origen=\'@ORIGEN\'" +
			"  and p.deposito is  not null" 
		;		
		hql2=hql2.replaceAll("@ORIGEN", getViewModel().getOrigen().name());
		List<BigDecimal> res2=Services.getInstance().getHibernateTemplate().find(hql2, params);
		BigDecimal cambiosDeCheque=res2.get(0)==null?BigDecimal.ZERO:res2.get(0);
		getViewModel().setCambiosDeCheque(cambiosDeCheque);
		logger.info("Total de Cambio de Che x Efe: "+cambiosDeCheque);
		
		getViewModel().setDisponible(getViewModel().getDisponibleCalculado());
		
	}
	
	public void actualizarCortesAcumulados(){
		if(getViewModel().getOrigen()==null)
			return;
		String hql="select sum(c.deposito) from Caja c " +
				" where c.fecha=? " +
				" and c.sucursal.id=?" +
				" and c.concepto=\'CORTE_CAJA\'" +
				" and c.tipo=\'CHEQUE\'" +
				" and c.origen=\'@ORIGEN\'";
		hql=hql.replaceAll("@ORIGEN", getViewModel().getOrigen().name());
		Object[] params={
				getViewModel().getFecha()
				,getViewModel().getSucursal().getId()
				};
		List<BigDecimal> res=Services.getInstance().getHibernateTemplate().find(hql, params);
		BigDecimal acumulado=res.get(0)==null?BigDecimal.ZERO:res.get(0);
		logger.info("Pagos acumulados: "+acumulado);
		getViewModel().setCortesAcumulados(acumulado);
		
	}
	
	
	
	private List<Pago> buscarPagos(){
		Object[] params={getViewModel().getFecha(),getViewModel().getSucursal().getId()};
		String hql="from PagoConCheque p  where " +
				"     p.fecha=? " +
				" and p.sucursal.id=?" +
				" and p.origenAplicacion=\'@ORIGEN\'"
				;
		hql=hql.replaceAll("@ORIGEN", getViewModel().getOrigen().name());
		List<Pago> pagos=Services.getInstance().getHibernateTemplate().find(hql, params);
		String hql2="from PagoPorCambioDeCheque p where " +
			"     p.fecha=? " +
			" and p.sucursal.id=?" +
			" and p.origen=\'@ORIGEN\'"
			;
		hql2=hql2.replaceAll("@ORIGEN", getViewModel().getOrigen().name());
		List<Pago> pagos2=Services.getInstance().getHibernateTemplate().find(hql2, params);
		pagos.addAll(pagos2);
		return pagos;
	}
	
	public void registrarPagos(){
		if(getViewModel().getOrigen()==null)
			return;
		getChequesSource().clear();
		getChequesSource().addAll(buscarPagos());
		mostrarPagos();
	}
	
	public void mostrarPagos(){
		SelectorDePagos selector=new SelectorDePagos(){
			protected List<Pago> getData() {
				return getChequesSource();
			}
			public void adjustGrid(final JXTable grid){
				grid.getColumnExt("Sucursal").setVisible(false);
				grid.getColumnExt("Cliente").setVisible(false);
				grid.getColumnExt("Primera Ap").setVisible(false);
				grid.getColumnExt("Dif").setVisible(false);
				grid.getColumnExt("Disponible").setVisible(false);
				grid.getColumnExt("Deposito").setVisible(false);
				
			}
		};
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
	}
	
	
	private Cuenta getCuenta(){
		return getViewModel().getCuenta();
	}
	
	private void actualizarFichas(){
		if(this.fichasMismoBanco==null)
			this.fichasMismoBanco=new BasicEventList<Ficha>();
		if(this.fichasOtrosBancos==null)
			this.fichasOtrosBancos=new BasicEventList<Ficha>();
		this.fichasMismoBanco.clear();
		this.fichasOtrosBancos.clear();
		int count=0;
		
		if(this.cheques.size()==0)
			return;
		if(this.chequeMismoBanco.size()>0){
			Ficha ficha=createFicha();
			ficha.setTipoDeFicha(Ficha.FICHA_MISMO_BANCO);
			for(Pago cheque:chequeMismoBanco){
				FichaDet fd=new FichaDet();
				fd.setPago(cheque);
				ficha.agregarPartida(fd);
				count++;
				if(count%5==0){
					ficha.actualizarTotal();
					fichasMismoBanco.add(ficha);
					ficha=createFicha();
					ficha.setTipoDeFicha(Ficha.FICHA_MISMO_BANCO);
				}
				
			}
			if(ficha.getPartidas().size()>0)
				fichasMismoBanco.add(ficha);
		}
		if(this.chequesOtrosBanco.size()>0){
			count=0;
			Ficha ficha=createFicha();
			ficha.setTipoDeFicha(Ficha.FICHA_OTROSBANCOS);
			for(Pago cheque:chequesOtrosBanco){
				FichaDet fd=new FichaDet();
				fd.setPago(cheque);
				ficha.agregarPartida(fd);
				count++;
				if(count%5==0){
					fichasOtrosBancos.add(ficha);
					ficha=createFicha();
					ficha.setTipoDeFicha(Ficha.FICHA_OTROSBANCOS);
				}
				
			}
			if(ficha.getPartidas().size()>0)
				fichasOtrosBancos.add(ficha);
		}
		
		BigDecimal totMB=BigDecimal.ZERO;
		BigDecimal totOB=BigDecimal.ZERO;
		for(Ficha f:this.fichasMismoBanco){
			f.actualizarTotal();			
			totMB=totMB.add(f.getTotal());
		}
		
		for(Ficha f:this.fichasOtrosBancos){
			f.actualizarTotal();
			System.out.println("Ficha Imp: "+f.getTotal());
			System.out.println("Ficha Tot: "+f.getTotal());
			System.out.println("FichasDet: "+f.getPartidas().size());
			for(FichaDet det:f.getPartidas()){
				System.out.println("--Det: "+det.getImporte());
				System.out.println("--Det Pag: "+det.getPago().getTotal());
			}
			totOB=totOB.add(f.getTotal());
		}
		getViewModel().setFichasMismoBanco(this.fichasMismoBanco.size());		
		getViewModel().setImporteFichasMismoBanco(totMB);
		getViewModel().setFichasOtrosBancos(this.fichasOtrosBancos.size());
		getViewModel().setImporteFichasOtrosBancos(totOB);		
		getViewModel().setFichas(this.fichasMismoBanco.size()+this.fichasOtrosBancos.size());
		getViewModel().setTotalFichas(totMB.add(totOB));
	}
	
	private Ficha createFicha(){
		Ficha ficha=new Ficha();
		ficha.setFecha(getViewModel().getFecha());
		ficha.setSucursal(getViewModel().getSucursal());		
		ficha.setCuenta(getCuenta());
		ficha.setOrigen(getViewModel().getOrigen());
		ficha.setComentario("CORTE DE CHEQUES "+getViewModel().getOrigen());
		return ficha;
	}
	
	public Caja persist(){
		Caja corte=new Caja();
		corte.setSucursal(getViewModel().getSucursal());
		corte.setFecha(getViewModel().getFecha());
		corte.setBanco(getViewModel().getCuenta().getBanco());
		corte.setImporte(getViewModel().getImporteCheques());
		corte.setTipo(Caja.Tipo.CHEQUE);
		corte.setConcepto(Caja.Concepto.CORTE_CAJA);
		corte.setOrigen(getViewModel().getOrigen());
		corte.setComentario("CORTE CHEQUE: "+corte.getOrigen());
		corte.setCorte(new Date());
		corte.setPagos(getViewModel().pagos);
		corte.setCambiosDeCheque(getViewModel().getCambiosDeCheque());
		corte.setCortesAcumulados(getViewModel().getCortesAcumulados());
		corte.aplicar();
		List<Ficha> fichas=new ArrayList<Ficha>(this.fichasOtrosBancos);
		fichas.addAll(this.fichasMismoBanco);
		corte=Services.getInstance().getCorteDeCajaManager().registrarCorteDeCajaCheque(corte,fichas);
		return corte;
	}
	
	
	public static class CorteDeChequesViewModel {
		private Sucursal sucursal;
		private Cuenta cuenta;
		private Date fecha=new Date();
		private Date corte=new Date();
		private OrigenDeOperacion origen;
		private BigDecimal pagos=BigDecimal.ZERO;
		private BigDecimal cortesAcumulados=BigDecimal.ZERO;
		private BigDecimal cambiosDeCheque=BigDecimal.ZERO;
		
		private BigDecimal importe=BigDecimal.ZERO;
		private BigDecimal disponible=BigDecimal.ZERO;
		
		
		private int cheques;
		private BigDecimal importeCheques=BigDecimal.ZERO;
		
		private int chequesMismoBanco;
		private BigDecimal importeChequesMismoBanco=BigDecimal.ZERO;
		
		private int chequesOtrosBancos;		
		private BigDecimal importeChequesOtrosBancos=BigDecimal.ZERO;
		
		private int fichas;
		private BigDecimal totalFichas;
		
		private int fichasMismoBanco;
		private BigDecimal importeFichasMismoBanco=BigDecimal.ZERO;
		
		private int fichasOtrosBancos;		
		private BigDecimal importeFichasOtrosBancos=BigDecimal.ZERO;
		
		public Sucursal getSucursal() {
			return sucursal;
		}
		public void setSucursal(Sucursal sucursal) {
			this.sucursal = sucursal;
		}
		
		public Cuenta getCuenta() {
			return cuenta;
		}
		public void setCuenta(Cuenta cuenta) {
			this.cuenta = cuenta;
		}
		public int getCheques() {
			return cheques;
		}
		public void setCheques(int cheques) {
			this.cheques = cheques;
		}
		public BigDecimal getImporteCheques() {
			return importeCheques;
		}
		public void setImporteCheques(BigDecimal importeCheques) {
			this.importeCheques = importeCheques;
		}
		public int getChequesMismoBanco() {
			return chequesMismoBanco;
		}
		public void setChequesMismoBanco(int chequesMismoBanco) {
			this.chequesMismoBanco = chequesMismoBanco;
		}
		public BigDecimal getImporteChequesMismoBanco() {
			return importeChequesMismoBanco;
		}
		public void setImporteChequesMismoBanco(BigDecimal importeChequesMismoBanco) {
			this.importeChequesMismoBanco = importeChequesMismoBanco;
		}
		
		public Date getCorte() {
			return corte;
		}
		public void setCorte(Date corte) {
			this.corte = corte;
		}
		public int getChequesOtrosBancos() {
			return chequesOtrosBancos;
		}
		public void setChequesOtrosBancos(int chequesOtrosBancos) {
			this.chequesOtrosBancos = chequesOtrosBancos;
		}
		public BigDecimal getImporteChequesOtrosBancos() {
			return importeChequesOtrosBancos;
		}
		public void setImporteChequesOtrosBancos(BigDecimal importeChequesOtrosBancos) {
			this.importeChequesOtrosBancos = importeChequesOtrosBancos;
		}
		public int getFichas() {
			return fichas;
		}
		public void setFichas(int fichas) {
			this.fichas = fichas;
		}
		
		public BigDecimal getTotalFichas() {
			return totalFichas;
		}
		public void setTotalFichas(BigDecimal totalFichas) {
			this.totalFichas = totalFichas;
		}
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		public OrigenDeOperacion getOrigen() {
			return origen;
		}
		public void setOrigen(OrigenDeOperacion origen) {
			this.origen = origen;
		}
		
		public BigDecimal getPagos() {
			return pagos;
		}
		public void setPagos(BigDecimal pagos) {
			this.pagos = pagos;
		}
		public BigDecimal getCortesAcumulados() {
			return cortesAcumulados;
		}
		public void setCortesAcumulados(BigDecimal cortesAcumulados) {
			this.cortesAcumulados = cortesAcumulados;
		}
		
		public BigDecimal getCambiosDeCheque() {
			return cambiosDeCheque;
		}
		public void setCambiosDeCheque(BigDecimal cambiosDeCheque) {
			this.cambiosDeCheque = cambiosDeCheque;
		}
		public BigDecimal getImporte() {
			return importe;
		}
		public void setImporte(BigDecimal importe) {
			this.importe = importe;
		}
		public BigDecimal getDisponible() {
			return disponible;
		}
		public void setDisponible(BigDecimal disponible) {
			this.disponible = disponible;
		}
		
		public BigDecimal getDisponibleCalculado(){
			return getPagos().add(getCambiosDeCheque()).subtract(getCortesAcumulados());
		}
		
		
		public int getFichasMismoBanco() {
			return fichasMismoBanco;
		}
		public void setFichasMismoBanco(int fichasMismoBanco) {
			this.fichasMismoBanco = fichasMismoBanco;
		}
		public BigDecimal getImporteFichasMismoBanco() {
			return importeFichasMismoBanco;
		}
		public void setImporteFichasMismoBanco(BigDecimal importeFichasMismoBanco) {
			this.importeFichasMismoBanco = importeFichasMismoBanco;
		}
		public int getFichasOtrosBancos() {
			return fichasOtrosBancos;
		}
		public void setFichasOtrosBancos(int fichasOtrosBancos) {
			this.fichasOtrosBancos = fichasOtrosBancos;
		}
		public BigDecimal getImporteFichasOtrosBancos() {
			return importeFichasOtrosBancos;
		}
		public void setImporteFichasOtrosBancos(BigDecimal importeFichasOtrosBancos) {
			this.importeFichasOtrosBancos = importeFichasOtrosBancos;
		}
		public String getHora(){
			if(getCorte()!=null)
				return new SimpleDateFormat("HH:mm").format(getCorte());
			return new SimpleDateFormat("HH:mm").format(new Date());
		}
		 
	}
	
	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			registrarPagos();
			
			actualizarCortesAcumulados();
			actualizarPagos();
			actualizarImportes();
		}		
	}
	
	
	private class ListHandler implements ListEventListener<Pago>{

		public void listChanged(ListEvent<Pago> listChanges) {
			while(listChanges.next()){
				
			}
			actualizarImportes();			
			validate();
			
		}
		
	}
	
	
}
