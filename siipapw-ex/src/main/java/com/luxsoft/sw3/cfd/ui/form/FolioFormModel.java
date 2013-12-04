package com.luxsoft.sw3.cfd.ui.form;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.beans.Model;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.sw3.cfd.model.FolioFiscalId;

public class FolioFormModel extends DefaultFormModel{
	
	private PresentationModel idModel;

	public FolioFormModel() {
		super(new FolioFiscal());
	}

	public FolioFormModel(FolioFiscal bean, boolean readOnly) {
		super(bean, readOnly);
	}
	
	public void init(){
		super.init();
		FolioIdModel folioId;
		if(getFolio().getId()!=null){
			folioId=new FolioIdModel();
			folioId.setSerie(getFolio().getId().getSerie());
			folioId.setSucursal(getSucursal(getFolio().getId().getSucursal()));
		}else{
			folioId=new FolioIdModel();
		}
		idModel=new PresentationModel(folioId);
	}
	
	public FolioFiscal getFolio(){
		return (FolioFiscal)getBaseBean();
	}
	
	public PresentationModel getIdModel(){
		return idModel;
	}

	public FolioFiscal commit() {
		FolioFiscal res=getFolio();
		if(res.getId()==null){
			FolioFiscalId id=new FolioFiscalId();
			FolioIdModel idModel=(FolioIdModel)getIdModel().getBean();
			id.setSucursal(idModel.getSucursal().getId());
			id.setSerie(idModel.getSerie());
			res.setId(id);
			res.setFolio(0L);
		}
		return res;
	}
	
	private Sucursal getSucursal(Long id){
		return (Sucursal)ServiceLocator2.getUniversalDao().get(Sucursal.class, id);
	}
	
	public static class FolioIdModel extends Model{
		
		private Sucursal sucursal;
		
		private String serie;

		public Sucursal getSucursal() {
			return sucursal;
		}

		public void setSucursal(Sucursal sucursal) {
			Object old=this.sucursal;
			this.sucursal = sucursal;
			firePropertyChange("sucursal", old, sucursal);
		}

		public String getSerie() {
			return serie;
		}

		public void setSerie(String serie) {
			Object old=this.serie;
			this.serie = serie;
			firePropertyChange("serie", old, serie);
		}
		
		
	}

}
