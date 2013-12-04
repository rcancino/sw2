create table SX_CLIENTES_AUDITLOG 
(ID bigint not null auto_increment
, action varchar(40) not null
, entityName varchar(40) not null
, tableName varchar(50) not null
, USUARIO varchar(100) not null
, ORIGEN varchar(100) not null
, dateCreated datetime not null
, entityId varchar(255) not null
, ip varchar(50)
, lastUpdated datetime
, message text
, replicado datetime
, SUCURSAL_DESTINO varchar(50)
, SUCURSAL_ORIGEN varchar(50) not null
, version integer not null
, primary key (ID)) ENGINE=InnoDB