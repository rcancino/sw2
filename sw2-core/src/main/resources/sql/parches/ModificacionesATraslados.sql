
alter table SX_USUARIOS add column DEPARTAMENTO VARCHAR(150);
alter table SX_TRASLADOS add column CORTADOR  VARCHAR(150);
alter table SX_TRASLADOS add column SURTIDOR  VARCHAR(150);
alter table SX_TRASLADOS add column SUPERVISO VARCHAR(150);

//Actualizar el sistema de replica parando el replicador , corriendo y re iniciando el replicador: 
//delete from entity_configuration where table_name in ('SX_USUARIOS','SX_TRASLADOS')

