package com.luxsoft.sw2.replica.valida;

import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;

public interface ImportadorDeFaltantes {

	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId);
}
