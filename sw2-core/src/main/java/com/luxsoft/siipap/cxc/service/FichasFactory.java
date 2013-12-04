package com.luxsoft.siipap.cxc.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.cxc.model.Depositable;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.tesoreria.Cuenta;

/**
 * Factory para la generacion de fichas de deposito 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FichasFactory {
	
	private static FichasFactory INSTANCE;
	
	private Logger logger=Logger.getLogger(getClass());
	
	public static FichasFactory getInstance(){
		if(INSTANCE==null){
			INSTANCE=new FichasFactory();
		}
		return INSTANCE;
	}
	
	private FichasFactory(){}
	
	/**
	 * Agrupa las partidas en fichas
	 * 
	 * @param fichas
	 * @param cuenta
	 */
	public List<Ficha> agrupar(final List<FichaDet> fichas ,final Cuenta cuenta){
		
		List<FichaDet> efectivo=new ArrayList<FichaDet>();
		List<FichaDet> mismoBanco=new ArrayList<FichaDet>();
		List<FichaDet> otrosBancos=new ArrayList<FichaDet>();
		
		for(FichaDet det:fichas){
			if(det.getEfectivo().doubleValue()>0)
				efectivo.add(det);
			else{
				if(det.getBanco()==null)
					det.setBanco(cuenta.getBanco().getClave());
				if(det.getBanco().equals(cuenta.getBanco().getClave()))
					mismoBanco.add(det);
				else
					otrosBancos.add(det);
			}
		}
		int folio=1;
		
		List<Ficha> res=new ArrayList<Ficha>();
		
		//Efectivo
		if(!efectivo.isEmpty()){
			Ficha fichaEfectivo=new Ficha();
			fichaEfectivo.setFolio(folio++);
			fichaEfectivo.setTipoDeFicha(Ficha.FICHA_EFECTIVO);
			for(FichaDet det:efectivo){
				det.setBanco(cuenta.getBanco().getClave());
				fichaEfectivo.agregarPartida(det);			
			}
			res.add(fichaEfectivo);
		}
		
		//Mismo banco
		List<Ficha> fichasMismoBanco=generarFichas(mismoBanco, Ficha.FICHA_MISMO_BANCO, folio);
		logger.info("Fichas del mismo banco generadas: "+fichasMismoBanco.size());
		folio+=fichasMismoBanco.size();
		List<Ficha> fichasOtrosBancos=generarFichas(otrosBancos, Ficha.FICHA_OTROSBANCOS, folio);
		logger.info("Fichas de otros bancos generadas: "+fichasOtrosBancos.size());
		
		
		res.addAll(fichasMismoBanco);
		res.addAll(fichasOtrosBancos);
		return res;
	}
	
	/**
	 * Agrupa fichas en grupos de cinco partidas
	 * 
	 * @param partidas
	 * @param tipo
	 * @param folio
	 * @return
	 */
	private List<Ficha> generarFichas(final List<FichaDet> partidas,String tipo,Integer folio){		
		
		List<Ficha> fichas=new ArrayList<Ficha>();
		ListIterator<FichaDet> pagoIter=partidas.listIterator();
		
		Ficha ficha=null;
		while(pagoIter.hasNext()){
			if(ficha==null){
				ficha=new Ficha();
				ficha.setFolio(folio++);
				ficha.setTipoDeFicha(tipo);
			}
			FichaDet det=pagoIter.next();
			ficha.agregarPartida(det);
			if(ficha.getPartidas().size()%5==0){
				fichas.add(ficha);
				ficha=null;
			}
		}
		if(ficha!=null)
			fichas.add(ficha);		
		return fichas;
	}
		
	/**
	 * Regresa una lista de los pagos que implementan Depositable y que estan pendientes
	 * 
	 * @param pagos
	 */
	public List<Pago> buscarPendientes(final List<Pago> pagos){
		
		Collection<Pago> res= CollectionUtils.select(pagos, new org.apache.commons.collections.Predicate(){
			public boolean evaluate(Object object) {
				if(object instanceof Depositable){
					Depositable dep=(Depositable)object;
					return (dep.isPendientesDeDeposito());
				}
				return false;				
			}
		});
		return new ArrayList<Pago>(res);
	}

}
