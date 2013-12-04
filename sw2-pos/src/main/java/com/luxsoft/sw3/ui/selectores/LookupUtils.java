package com.luxsoft.sw3.ui.selectores;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Lookup utility methods para UI
 * 
 * @author Ruben Cancino
 *
 */
public abstract class LookupUtils {
	
	private static LookupUtils INSTANCE;
	
	
	
	/**
	 * Carga en EDT la lista de productos activos
	 * 
	 * @param productos
	 */
	public abstract void loadProductosParaCompras(final EventList<Producto> productos);
	
	public abstract void loadProveedores(final EventList<Proveedor> proveedor);
	
	public abstract void loadProductosInventariables(final EventList<Producto> productos);
	
	public abstract List<Sucursal> getSucursales();
	
	public static LookupUtils getDefault(){
		if(INSTANCE==null){
			INSTANCE=new DefaultLookupUtils();
		}
		return INSTANCE;
	}
	
	public static List<String> getEstados() {
		final List<String> estados=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource("META-INF/estados.txt");
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String edo=null;
			do{
				edo=reader.readLine();
				if(edo!=null)
					estados.add(edo.trim().toUpperCase());				
			}while(edo!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return estados;
	}

	public static List<String> getCiudades() {
		final List<String> ciudades=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource("META-INF/ciudades.txt");
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String edo=null;
			do{
				edo=reader.readLine();
				if(edo!=null)
					ciudades.add(edo.trim());				
			}while(edo!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ciudades;
	}

	public static List<String> getMunicipios() {
		final List<String> municipios=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource("META-INF/municipios.txt");
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String edo=null;
			do{
				edo=reader.readLine();
				if(edo!=null)
					municipios.add(edo.trim().toUpperCase());				
			}while(edo!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return municipios;
	}
	
	protected static class DefaultLookupUtils extends LookupUtils{
		
			
		
		@Override
		public void loadProductosParaCompras(final EventList<Producto> productos) {
			
			SwingWorker<List<Producto>, String> worker=new SwingWorker<List<Producto>, String>(){			
				protected List<Producto> doInBackground() throws Exception {
					return Services.getInstance().getProductosManager().getProductosParaComprasNacionales();
				}
				protected void done() {
					try {
						productos.getReadWriteLock().writeLock().lock();
						productos.clear();
						productos.addAll(get());
						System.out.println("Productos cargados: "+productos.size());
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						productos.getReadWriteLock().writeLock().unlock();
					}
				}
			};
			TaskUtils.executeSwingWorker(worker, "Cargando productos", "Buscando productos activos");
			
		}
		
		@Override
		public void loadProductosInventariables(final EventList<Producto> productos) {
			
			SwingWorker<List<Producto>, String> worker=new SwingWorker<List<Producto>, String>(){			
				protected List<Producto> doInBackground() throws Exception {
					return Services.getInstance().getProductosManager().getActivos();
				}
				protected void done() {
					try {
						productos.getReadWriteLock().writeLock().lock();
						productos.clear();
						productos.addAll(get());
						
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						productos.getReadWriteLock().writeLock().unlock();
					}
				}
			};
			TaskUtils.executeSwingWorker(worker, "Cargando productos", "Buscando productos inventariables activos");
			
		}

		@Override
		public void loadProveedores(final EventList<Proveedor> proveedores) {
			SwingWorker<List<Proveedor>, String> worker=new SwingWorker<List<Proveedor>, String>(){			
				protected List<Proveedor> doInBackground() throws Exception {
					return Services.getInstance().getProveedorManager().buscarActivos();
				}
				protected void done() {
					try {
						proveedores.getReadWriteLock().writeLock().lock();
						proveedores.clear();
						proveedores.addAll(get());
						
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						proveedores.getReadWriteLock().writeLock().unlock();
					}
				}
			};
			TaskUtils.executeSwingWorker(worker, "Cargando Proveedores", "Buscando proveedores activos");
			
		}
		
		private List<Sucursal> sucursales;

		@Override
		public List<Sucursal> getSucursales() {
			if(sucursales==null){
				sucursales=Services.getInstance().getSucursalesOperativas();
			}
			return sucursales;			
		}
		
		
	}
	
	

	
}
