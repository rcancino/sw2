DROP TABLE IF EXISTS AUDIT_LOG;

create table AUDIT_LOG (
        ID bigint not null auto_increment,
        entityId varchar(255) not null,
        entityName varchar(40) not null,
        action varchar(20) not null,
        tableName varchar(50) ,
        ip varchar(50) ,
        SUCURSAL_ORIGEN varchar(20) not null,
        SUCURSAL_DESTINO varchar(20),
        dateCreated datetime not null,
        lastUpdated datetime,
        replicado datetime,
        message text,
        version integer not null,
        primary key (ID)
    ) ENGINE=InnoDB;


