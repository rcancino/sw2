package com.luxsoft.sw3.solicitudes;

import javax.swing.SwingUtilities;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class TestSolicitudForm {
	
	public static void main(String[] args) throws Exception{
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				
				//final SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel();
				final SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel(ServiceLocator2.getConfiguracion().getSucursal());
				/*final SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel(
						(SolicitudDeModificacion)ServiceLocator2.getHibernateTemplate().get(SolicitudDeModificacion.class, "8a8a8161-3f5d148b-013f-5d1706f7-0002"),
						ServiceLocator2.getConfiguracion().getSucursal()
						);
						*/
				model.setManager(ServiceLocator2.getSolicitudDeModificacionesManager());
				//model.setReadOnly(true);
				final SolicitudDeModificacionForm form=new SolicitudDeModificacionForm(model);
				form.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
				form.open();
				
				if(!form.hasBeenCanceled()){
					model.commit();
				}
				
				
			}
		});
	}

}
