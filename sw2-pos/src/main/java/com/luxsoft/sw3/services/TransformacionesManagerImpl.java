package com.luxsoft.sw3.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.core.Folio;

@Service("transformacionesManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class TransformacionesManagerImpl  implements TransformacionesManager{
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private InventariosManager inventariosManager;
	
	@Autowired
	private FolioDao folioDao;
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public Transformacion get(String id){
		return (Transformacion)hibernateTemplate.load(Transformacion.class, id);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(String id) {
		Transformacion t=get(id);
		hibernateTemplate.delete(t);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Transformacion save(Transformacion t) {
		String tipo="TRS_"+t.getClase();
		Folio folio=folioDao.buscarNextFolio(t.getSucursal(), tipo);
		long documento=folio.getFolio();
		t.setDocumento(documento);
		int renglon=1;
		for(TransformacionDet det:t.getPartidas()){			
			if(det.getDestino()!=null){
				det.setRenglon(renglon++);
				det.getDestino().setRenglon(renglon++);
			}
			String concepto=null;
			Transformacion.Clase clazz=t.getClase();
			switch (clazz) {
			case Transformacion:
				concepto="TRS";
				break;
			case Metalizado:
				concepto="MET";
				break;
			case Reclasificacion:
				concepto="REC";
				break;
			default:
				throw new RuntimeException("NO EXISTE LA CLASE");
			}
			det.setConceptoOrigen(concepto);
			det.setDocumento(t.getDocumento());
			inventariosManager.actualizarExistencia(det);
		}
		folioDao.save(folio);
		return (Transformacion)hibernateTemplate.merge(t);
	}
	

}
