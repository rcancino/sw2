package com.luxsoft.siipap.ventas.dao;

import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.DescPorVol;

public class DescPorVolDaoImpl extends GenericDaoHibernate<DescPorVol, Long> implements DescPorVolDao{

	public DescPorVolDaoImpl() {
		super(DescPorVol.class);
	}
	
	private List<DescPorVol> descuentosContado;
	
	private List<DescPorVol> descuentosCredito;

	public double buscarDescuentoContado(double importe) {
		if(descuentosContado==null){
			descuentosContado=getHibernateTemplate()
				.find("from DescPorVol d where d.tipo=\'CONTADO\'");
		}		
		return buscarDescuento(descuentosContado, importe);
	}
	
	public double buscarDescuentoContadoPostFechado(double volumen) {
		double res=buscarDescuentoContado(volumen);
		return res-2;
	}



	public double buscarDescuentoCredito(double importe) {
		if(descuentosCredito==null){
			descuentosCredito=getHibernateTemplate()
				.find("from DescPorVol d where d.tipo=\'CREDITO\'");
		}		
		return buscarDescuento(descuentosCredito, importe);
	}
	
	/**
	 * Busca el descuento apropiado para el volumen indicado
	 * 
	 * @param descuentos
	 * @param importe
	 * @return
	 */
	private double buscarDescuento(final List<DescPorVol> descuentos,double importe){
		DescPorVol descuento=null;
		
		final int total=descuentos.size();
		
		for(int index=0;index<total;index++){
			DescPorVol actual=descuentos.get(index);
			if(importe>actual.getImporte().doubleValue()){
				descuento=actual;
				continue;
			}else{
				if(index<total){							
					descuento=descuentos.get(index);
				}
				else 
					descuento=actual;
				break;
			}			
		}
		return descuento!=null?descuento.getDescuento():0;
	}
	
	public static void main(String[] args) {
		DescPorVolDaoImpl dao=new DescPorVolDaoImpl();
		dao.setSessionFactory(ServiceLocator2.getSessionFactory());
		double res=dao.buscarDescuentoContado(49230.00d);
		System.out.println("Descuento: "+res);
		
		res=dao.buscarDescuentoContado(89000.00d);
		System.out.println("Descuento: "+res);
		
		
		res=dao.buscarDescuentoContado(80.00d);
		System.out.println("Descuento: "+res);
		
	}

}
