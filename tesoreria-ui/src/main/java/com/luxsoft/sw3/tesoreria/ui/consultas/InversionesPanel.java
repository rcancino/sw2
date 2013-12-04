package com.luxsoft.sw3.tesoreria.ui.consultas;


import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
	
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.tesoreria.model.Clasificacion;
import com.luxsoft.sw3.tesoreria.model.Inversion;
import com.luxsoft.sw3.tesoreria.ui.forms.InversionForm;
import com.luxsoft.sw3.tesoreria.ui.forms.InversionFormModel;


public class InversionesPanel extends AbstractMasterDatailFilteredBrowserPanel<Inversion,CargoAbono>{

	public InversionesPanel() {
		super(Inversion.class);		
	}
	protected void agregarMasterProperties(){
		addProperty(
				"id"
				,"fecha"
				,"cuentaOrigen.cuentaDesc"
				,"cuentaDestino.cuentaDesc"				
				,"tasa"
				,"plazo"
				,"vencimiento"
				,"importe"
				,"comision"
				,"impuesto"
				//,"rendimientoCalculado"
				,"rendimientoReal"
				,"importeRealISR"
				,"comentario"
				);
		addLabels(
				"Folio"
				,"Fecha"
				,"Origen"
				,"Destino"
				,"Tasa"
				,"Plazo"
				,"Vto"
				,"Importe"				
				,"Comisión"
				,"Impuesto"
				//,"Rendimiento (Calc)"
				,"Rendimiento "
				,"ISR($)"
				,"Comentario"
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(Inversion.class, "id"));
		manejarPeriodo();		
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"id","traspaso.id","cuenta.cuentaDesc","fecha","clasificacion","importe","referencia","comentario"};
		String names[]={"Folio","Traspaso","Cuenta","Fecha","Clase","Importe","Ref","Comentario"};
		return GlazedLists.tableFormat(CargoAbono.class, props, names);
	}
	@Override
	protected Model<Inversion, CargoAbono> createPartidasModel() {
		return new Model<Inversion, CargoAbono>(){
			public List<CargoAbono> getChildren(Inversion parent) {
				return ServiceLocator2.getHibernateTemplate().find("from CargoAbono c where c.traspaso.id=?",parent.getId());
			}
		};
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()				
				,getViewAction()	
				//,addAction(null,"registrarIntereses","Registrar intereses")
				//,addAction(null,"cancelarIntereses","Cancelar intereses")
				,addAction(null,"registrarRegresoInversion","Registrar retorno")
				,addAction(null,"modificarIsr","Modificar ISR")
				,addAction(null,"modificarDeposito","Modificar deposito")
				,addAction(null, "imprimir", "Imprimir")
				};
		}
		return actions;
	}

	@Override
	protected List<Inversion> findData() {
		String hql="from Inversion c where c.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					periodo.getFechaInicial()
					,periodo.getFechaFinal()
					}
		);
	}
	
	public void open(){
		load();
	}
	@Override
	protected Inversion doInsert() {
		
		InversionFormModel model=new InversionFormModel();
		InversionForm form=new InversionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			Inversion t=model.commit();
			t=ServiceLocator2.getTesoreriaManager().salvar(t);
			return t;
		}
		return super.doInsert();
	}
	
	@Override
	public boolean doDelete(Inversion bean) {
		ServiceLocator2.getUniversalDao().remove(Inversion.class, bean.getId());
		return true;
	}
	
	public void modificarDeposito(){
		for(Object o:detailSelectionModel.getSelected()){
			CargoAbono ca=(CargoAbono)o;
			if(ca.getClasificacion().equals(Clasificacion.DEPOSITO.name())){
				final DefaultFormModel model=new DefaultFormModel(Bean.proxy(ModificacionDeImportes.class));
				model.setValue("importe", ca.getImporte());
				final ModificacionDeImporteForm form=new ModificacionDeImporteForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					BigDecimal importe=(BigDecimal)model.getValue("importe");
					String msg=MessageFormat.format("Cambiar importe de {0,number,currency} a {1,number,currency}", ca.getImporte(),importe);
					if(MessageUtils.showConfirmationMessage(msg, "Inversiones")){
						int index=detailSortedList.indexOf(ca);
						ca.setImporte(importe);
						CargoAbono value=(CargoAbono)ServiceLocator2.getUniversalDao().save(ca);
						//clearSelection();
						detailSortedList.set(index, value);
					}
					
				}
			}
		}
		
	}
	
	public void modificarIsr(){
		Inversion i=(Inversion)getSelectedObject();
		if(i!=null){
			final DefaultFormModel model=new DefaultFormModel(Bean.proxy(ModificacionDeImportes.class));
			BigDecimal oldImporte=i.getImporteRealISR().doubleValue()==0?i.getImporteISR():i.getImporteRealISR();
			model.setValue("importe", oldImporte);
			final ModificacionDeImporteForm form=new ModificacionDeImporteForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				BigDecimal importe=(BigDecimal)model.getValue("importe");
				
				String msg=MessageFormat.format("Cambiar importe ISR de {0,number,currency} a {1,number,currency}", oldImporte,importe);
				if(MessageUtils.showConfirmationMessage(msg, "Inversiones")){
					int index=source.indexOf(i);
					i.setImporteRealISR(importe);
					Inversion value=(Inversion)ServiceLocator2.getUniversalDao().save(i);
					//clearSelection();
					source.set(index, value);
				}
				
			}
		}
		
		
	}
	
	
	public void registrarRegresoInversion(){
		Inversion i=(Inversion)getSelectedObject();
		if(i!=null){
			i=ServiceLocator2.getTesoreriaManager().buscarInversion(i.getId());
			CargoAbono retiroRegreso=i.buscarRegresoDeInversion();
			if(retiroRegreso!=null){
				MessageUtils.showMessage("Regreso ya registrados ", "Registro de intereses");
				return ;
			}
			if(MessageUtils.showConfirmationMessage("Registrar el retorno de la inversión: "+i.getId(), "Inversiones")){
				Date fecha=SelectorDeFecha.seleccionar(i.getVencimiento());
				int index=source.indexOf(i);
				if(index!=-1){
					i=ServiceLocator2.getTesoreriaManager().buscarInversion(i.getId());
					i.setRendimientoFecha(fecha);
					i=ServiceLocator2.getTesoreriaManager().registrarRegresoDeInversion(i);
					source.set(index, i);
				}
			}
			
			
		}
		
	}
	/*
	public void cancelarIntereses(){
		Inversion i=(Inversion)getSelectedObject();
		if(i!=null){
			i=ServiceLocator2.getTesoreriaManager().buscarInversion(i.getId());
			CargoAbono intereses=i.buscarRegistroDeIntereses();
			if(intereses==null){
				MessageUtils.showMessage("No hay intereses registrados", "Registro de intereses");
				return ;
			}
			int index=source.indexOf(i);
			if(index!=-1){
				i.setRendimientoFecha(new Date());
				InversionFormModel model=new InversionFormModel(i);
				InversionForm form=new InversionForm(model);
				form.setRegistroDeGanancias(true);
				form.open();
				if(!form.hasBeenCanceled()){
					Inversion t=model.commit();
					t=ServiceLocator2.getTesoreriaManager().registrarRendimiento(t);
					source.set(index, t);
				}
			}
			
		}
		
	}*/
	
	public static class ModificacionDeImporteForm extends AbstractForm{

		public ModificacionDeImporteForm(IFormModel model) {
			super(model);
			setTitle("Modificación de deposito");
		}

		@Override
		protected JComponent buildFormPanel() {
			final FormLayout layout=new FormLayout("p,2dlu,100dlu","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
			builder.append("Importe",getControl("importe"));
			return builder.getPanel();
		}
		
		@Override
		protected JComponent createCustomComponent(String property) {
			if("importe".equals(property)){
				return Binder.createBigDecimalForMonyBinding(model.getModel(property));
			}
			return super.createCustomComponent(property);
		}
		
	}
	
	public static class ModificacionDeImportes{
		
		private BigDecimal importe;

		public BigDecimal getImporte() {
			return importe;
		}

		public void setImporte(BigDecimal importe) {
			this.importe = importe;
		}


		
		
	}

}
