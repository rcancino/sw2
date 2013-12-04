package com.luxsoft.siipap.pos.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturasParaEntrega;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.embarque.ClientePorTonelada;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.embarque.EntregaDet;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;

public class EntregaController extends DefaultFormModel implements PropertyChangeListener,ListEventListener<EntregaDet>{
	
	private EventList<EntregaDet> partidas;
	private EventSelectionModel<EntregaDet> selectionModel;

	public EntregaController(Entrega entrega) {
		super(entrega);
		getModel("parcial").addValueChangeListener(this);
	}
	
	public Entrega getEntrega(){
		return (Entrega)getBaseBean();
	}
	
	protected void init(){
		addPropertyChangeListener(this);
		partidas=GlazedLists.eventList(getEntrega().getPartidas());
		/*if(getEntrega().getId()!=null){
			partidas=GlazedLists.eventList(getEntrega().getPartidas());
		}else{
			partidas=GlazedLists.eventList(new BasicEventList<EntregaDet>(0));
		}*/
		partidas=new ObservableElementList<EntregaDet>(partidas,GlazedLists.beanConnector(EntregaDet.class));
		partidas.addListEventListener(this);
		selectionModel=new EventSelectionModel<EntregaDet>(partidas);		
		addBeanPropertyChangeListener(this);
		//Venta v=Services.getInstance().getFacturasManager().buscarVentaInicializada(getEntrega().getFactura().getId());
		//getEntrega().setFactura(v);
		if(getEntrega().getId()!=null){
			if(!isReadOnly()){
				for(EntregaDet det:partidas){
					double anterior=buscarEngregado(det.getVentaDet());
					det.setEntregaAnterior(anterior-det.getCantidad());
				}
			}
			
		}
	}
	
	public void dispose(){
		removeBeanPropertyChangeListener(this);
		partidas.removeListEventListener(this);
	}
	
	public void inicializarPartidas(){
		if(getEntrega().isParcial() ){
			
			if(getEntrega().getPartidas().isEmpty()){
				//Venta v=Services.getInstance().getFacturasManager().buscarVentaInicializada(getEntrega().getFactura().getId());
				Venta v=getEntrega().getFactura();
				for(VentaDet det:v.getPartidas()){
					
					EntregaDet ed=new EntregaDet(det,buscarEngregado(det));
					if(getEntrega().agregarEntregaUnitaria(ed))
						partidas.add(ed);
					
				}
			}else{
				
			}
			
		}else if(getEntrega().getId()==null){
				partidas.clear();
		}
	}
	
	
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(!partidas.isEmpty()){
			//logger.info("Validando las cantidades a entregar");
			for(EntregaDet det:partidas){
				if(det.getCantidad()>det.getPorEntregar())
					support.getResult().addError(det.getClave()+ "Max a entregar: "+det.getPorEntregar());
			}
		}
	}

	private double buscarEngregado(final VentaDet det){
		String hql="select sum(e.cantidad) from EntregaDet e where e.ventaDet.id=?";
		List<Double> res=Services.getInstance().getHibernateTemplate().find(hql,det.getId());
		return res.get(0)!=null?res.get(0):0.0;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		//System.out.println("Propiedad:   "+evt.getPropertyName()+ "Val: "+evt.getNewValue());
		if("parcial".equals(evt.getPropertyName())){
			if(evt.getNewValue().equals(Boolean.FALSE)){
				getEntrega().getPartidas().clear();
				partidas.clear();
			}else
				inicializarPartidas();
		}
		
	}
	
	public void insertarPartida(){
		Venta v=Services.getInstance().getFacturasManager().buscarVentaInicializada(getEntrega().getFactura().getId());
		List<VentaDet> dts=new ArrayList<VentaDet>();
		dts.addAll(v.getPartidas());
		SelectorDePartidasFac selector=new SelectorDePartidasFac(dts);
		selector.open();
		if(selector.hasBeenCanceled()){
			VentaDet selected=selector.getSelected();
			System.out.println("Selected: "+selected);
		}
	}

	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			EntregaDet selected=selectionModel.getSelected().get(0);
			if(getEntrega().eliminarEntregaUnitaria(selected))
				partidas.remove(selected);
		}
		
	}
	
	
	public void listChanged(ListEvent<EntregaDet> listChanges) {
		/*while(listChanges.next()){
			
		}*/
		
		if(listChanges.next()){
			switch (listChanges.getType()) {
			case ListEvent.INSERT:
			case ListEvent.DELETE:
				//doListChange();
				break;
			case ListEvent.UPDATE:
				doListUpdated(listChanges);
				break;
			default:
				break;
			}				
		}
		validate();
	}
	
	private void doListUpdated(ListEvent<EntregaDet> listChanges) {
		//System.out.println("Cambiando detalles: "+listChanges.getIndex());
		//EntregaDet det=partidas.get(listChanges.getIndex());
		
		
	}

	public Entrega commit(){
		Entrega entrega=getEntrega();
		if(entrega.getPartidas().isEmpty()){
			validarQueNoExistanParcialidades();
			entrega.setParcial(false);
		}
		entrega.actualizarKilosCantidad();
		entrega.actualziarValor();
		ClientePorTonelada ct=comisionEspecial(entrega);
		if(ct!=null){
			entrega.setComisionPorTonelada(ct.getPrecio());
		}
		entrega.actualizarComision();
		
		return entrega;
	}
	
	
	private ClientePorTonelada comisionEspecial(final Entrega e){
		String hql="from ClientePorTonelada c where c.cliente.id=?";
		List<ClientePorTonelada> data=Services.getInstance().getHibernateTemplate().find(hql,e.getCliente().getId());
		return data.isEmpty()?null:data.get(0);
	}
	
	/**
	 * Cuando una entrega es total y/o pasa de ser parcial a total
	 * verifica que no exista alguna parcialidad
	 * 
	 */
	public void validarQueNoExistanParcialidades(){
		Entrega entrega=getEntrega();
		for(VentaDet det:getEntrega().getFactura().getPartidas()){
			String hql="from EntregaDet e where e.ventaDet.id=?";
			List<EntregaDet> entregas=Services.getInstance().getHibernateTemplate().find(hql,det.getId());
			if(!entregas.isEmpty()){
				String pattern="Existen entregas parciales de la factura {0} , producto: {1} renglón: {2}";
				throw new RuntimeException(MessageFormat.format(pattern, entrega.getFactura().getDocumento(),det.getClave(),det.getRenglon()));
				//MessageUtils.showMessage(MessageFormat.format(pattern, entrega.getFactura().getDocumento(),det.getClave(),det.getRenglon()), "Errora");
			}
		}
	}
	
	public void cambiarDireccion(){
		InstruccionDeEntrega ie=InstruccionDeEntregaForm
		.modificar(getEntrega().getCliente(),getEntrega().getInstruccionDeEntrega()
				);
		if(ie!=null)
			getEntrega().setInstruccionDeEntrega(ie);
	}
	
	public EventList<EntregaDet> getPartidas() {
		return partidas;
	}
	
	
	public EventSelectionModel<EntregaDet> getSelectionModel() {
		return selectionModel;
	}
	
	
	private class SelectorDePartidasFac extends AbstractSelector<VentaDet>{	
		
		private final List<VentaDet> partidasVentas;
		

		public SelectorDePartidasFac(List<VentaDet> partidasVentas) {
			super(VentaDet.class,"Partidas disponibles");
			this.partidasVentas=partidasVentas;
		}

		@Override
		protected List<VentaDet> getData() {
			
			return partidasVentas;
		}

		

		@Override
		protected TableFormat<VentaDet> getTableFormat() {
			String[] propertyNames={"renglon","clave","descripcion","cantidad"};
			String[] columnLabels={"Rengl","Prod","Descripcion","Cant "};
			final TableFormat tf=GlazedLists.tableFormat(VentaDet.class,propertyNames, columnLabels);
			return tf;
		}
		
		
		
	}
	
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				Venta v=SelectorDeFacturasParaEntrega.seleccionarVenta();
				if(v!=null){
					Entrega entrega=new Entrega();
					entrega.setFactura(v);
					entrega.setInstruccionDeEntrega(v.getPedido().getInstruccionDeEntrega());
					final EntregaController controller=new EntregaController(entrega);
					final EntregaForm form=new EntregaForm(controller);
					form.open();
					if(!form.hasBeenCanceled()){
						Entrega e=controller.commit();
						EntregaForm.showObject(e);
					}
				}
				
				System.exit(0);
			}

		});
	}

	
}
