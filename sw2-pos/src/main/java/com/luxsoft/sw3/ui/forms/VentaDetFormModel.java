package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.services.Services;


public class VentaDetFormModel extends DefaultFormModel implements PropertyChangeListener{

	private EventList<Existencia> existencias;
	private Sucursal sucursal;
	private boolean credito=false;
	private Date fecha=new Date();
	private Header header;	
	
	private double existenciaTotal;
	
	public VentaDetFormModel(VentaDet bean) {
		super(bean);
	}
	
	protected void init(){
		existencias=new BasicEventList<Existencia>(0);	
		Matcher<Existencia> matcher=Matchers.beanPropertyMatcher(Existencia.class, "sucursal.habilitada", Boolean.TRUE);
		existencias=new FilterList<Existencia>(existencias,matcher);
		Comparator<Existencia> c2=GlazedLists.beanPropertyComparator(Existencia.class, "sucursal.clave");
		existencias=new SortedList<Existencia>(existencias,GlazedLists.chainComparators(c2));
		addBeanPropertyChangeListener(this);
		setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		setFecha(new Date());
		if(getProductio()!=null){
			actualizarExistencias();
		}
	}
	
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getVentaDet().getCantidad()==0){
			support.getResult().addError("La cantidad no puede ser 0");
		}if(getVentaDet().getProducto()!=null)
			if(getVentaDet().getProducto().getPaquete()>1){
				System.out.println("Validando paquete");
				double cantidad=getVentaDet().getCantidad();
				double paquete=(double)getVentaDet().getProducto().getPaquete();
				double modulo=cantidad%paquete;
				System.out.println("Modulo: "+modulo);
				if(modulo!=0){
					double faltante=paquete-modulo;
					support.getResult()
					.addError("Este producto se vende en multiplos de: "+getVentaDet().getProducto().getPaquete()+ " Faltante: "+faltante);
				}
			}
	}

	public void dispose(){
		removeBeanPropertyChangeListener(this);
	}
	
	protected Producto getProductio(){
		return (Producto)getValue("producto");
	}
	
	protected VentaDet getVentaDet(){
		return (VentaDet)getBaseBean();
	}
	
	private void actualizarPrecio(){
		
		double precio=getProductio().getPrecioContado();
		if(isCredito())
			precio=getProductio().getPrecioCredito();
		setValue("precio", BigDecimal.valueOf(precio));
	}
	
	private void actualizarMedidasDeCorte(){}
	
	protected void actualizarExistencias(){
		
		existencias.clear();
		
		SwingWorker<List<Existencia>, String> worker=new SwingWorker<List<Existencia>, String>(){			
			protected List<Existencia> doInBackground() throws Exception {
				logger.info("Actualizando existencias para: "+getVentaDet().getProducto().getClave());
				return Services.getInstance()
						.getExistenciasDao()
						.buscarExistencias(getVentaDet().getProducto(), getFecha())
						;
			}
			protected void done() {
				try {
					existencias.addAll(get());
					
				} catch (Exception e) {
					logger.error("Error actualizando existencias del producto: "+getVentaDet().getProducto());
				}
				existenciaTotal=0;
				for(Existencia e:existencias){
					existenciaTotal+=e.getCantidad();
				}
				updateHeader();
			}
		};
		worker.execute();
		
		
	}
	
	public EventList<Existencia> getExistencias() {
		return existencias;
	}
	
	public double getExistencia(){
		Existencia res= (Existencia)CollectionUtils.find(getExistencias(), new Predicate(){
			public boolean evaluate(Object object) {
				Existencia exis=(Existencia)object;
				return exis.getSucursal().equals(getSucursal());
			}
		});
		//System.out.println("Existencia localizada: "+res);
		double exi= res!=null?res.getDisponible():0.0;
		//System.out.println("Existencia detectada: "+exi);
		return exi;
	}
	
	
	public boolean isCredito() {
		return credito;
	}

	public void setCredito(boolean credito) {
		this.credito = credito;
	}
	
	public void updateHeader() {
		if(header!=null){
			Producto p=getProductio();
			if(p!=null){
				header.setTitulo(MessageFormat.format("{0} ({1})",p.getDescripcion(),p.getClave()));
				String pattern="Uni:{0}\t Ancho:{1}\tLargo:{2}\t Calibre:{3}" +
						"\nAcabado:{4}\t Caras:{5}\tPrecio:{6}" +
						"\nCrédito: {7,number,currency}\tContado: {8,number,currency} " +
						"\n\nExistencias     Sucursal:{9,number,#,###,###}   Total: {10,number,#,###,###}"
						;
				String desc=MessageFormat.format(pattern
						,p.getUnidad().getNombre()						
						,p.getAncho()
						,p.getLargo()
						,p.getCalibre()
						,p.getAcabado()
						,p.getCaras()
						,p.getModoDeVenta()!=null?(p.getModoDeVenta().equals("B")?"Bruto":"Neto"):""
						,p.getPrecioCredito()
						,p.getPrecioContado()
						,getExistencia()
						,this.existenciaTotal
						);
				if(p.getPaquete()>1)
					desc+=" Paquete: "+p.getPaquete();
				header.setDescripcion(desc);
			}
			else{
				header.setTitulo("Seleccione un producto");
				header.setDescripcion("");
			}
		}
	}
	

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("producto")){
			actualizarExistencias();
			actualizarPrecio();
			updateHeader();
		}else if("cantidad".equals(evt.getPropertyName())){
			getVentaDet().actualizar();
		}else if("corteLargo".equals(evt.getPropertyName())){
			actualizarMedidasDeCorte();
		}else if("corteAncho".equals(evt.getPropertyName())){
			actualizarMedidasDeCorte();
		}else if("cortes".equals(evt.getPropertyName())){
			getVentaDet().actualizar();
		}else if("precioCorte".equals(evt.getPropertyName())){
			getVentaDet().actualizar();
		}
		
	}
	
	
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Header getHeader() {
		if(header==null){
			header=new Header("Seleccione un producto","");
			header.setDescRows(5);
			updateHeader();
		}
		return header;
	}	

	

}
