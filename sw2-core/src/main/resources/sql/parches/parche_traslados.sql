
drop table if exists sx_solicitud_trasladosdet

drop table if exists sx_inventario_trd

drop table if exists sx_traslados

drop table if exists sx_solicitud_traslados


create table SX_SOLICITUD_TRASLADOS (
        SOL_ID varchar(255) not null,
        version integer not null,        
        SUCURSAL_ID bigint not null,
        ORIGEN_ID bigint not null,
        DOCUMENTO bigint not null,
        FECHA date not null,
        COMENTARIO varchar(255),
        ATENDIDO bigint,
        REPLICADO datetime,
        CREADO datetime,
        CREADO_USR varchar(255),
        MODIFICADO datetime,
        MODIFICADO_USR varchar(255),
        createdIp varchar(255),
        createdMac varchar(255),
        updatedIp varchar(255),
        updatedMac varchar(255),
        primary key (SOL_ID),
        unique (SUCURSAL_ID, DOCUMENTO)
    ) ENGINE=InnoDB;

    create table SX_SOLICITUD_TRASLADOSDET (
        SOL_ID varchar(255) not null,
		SUCURSAL_ID bigint not null,
        ORIGEN_ID bigint not null,
        COMENTARIO varchar(255),        
        PRODUCTO_ID bigint not null,
        RECIBIDO double precision not null,
        SOLICITADO double precision not null
    ) ENGINE=InnoDB;

alter table SX_SOLICITUD_TRASLADOS 
        add index FK61E6898EB7624297 (ORIGEN_ID), 
        add constraint FK61E6898EB7624297 
        foreign key (ORIGEN_ID) 
        references SW_SUCURSALES (SUCURSAL_ID);

    alter table SX_SOLICITUD_TRASLADOS 
        add index FK61E6898E20C3FF1F (SUCURSAL_ID), 
        add constraint FK61E6898E20C3FF1F 
        foreign key (SUCURSAL_ID) 
        references SW_SUCURSALES (SUCURSAL_ID);

    alter table SX_SOLICITUD_TRASLADOSDET 
        add index FKCAE26BA5F14A8924 (PRODUCTO_ID), 
        add constraint FKCAE26BA5F14A8924 
        foreign key (PRODUCTO_ID) 
        references SX_PRODUCTOS (PRODUCTO_ID);

    alter table SX_SOLICITUD_TRASLADOSDET 
        add index FKCAE26BA5F1D492E (SOL_ID), 
        add constraint FKCAE26BA5F1D492E 
        foreign key (SOL_ID) 
        references SX_SOLICITUD_TRASLADOS (SOL_ID);


	create table SX_TRASLADOS (
        TRASLADO_ID varchar(255) not null,
		version integer not null,
		SUCURSAL_ID bigint not null,
        SOL_ID varchar(255) not null,
		TIPO varchar(3) not null,
		DOCUMENTO bigint not null,
		FECHA date not null,
        CHOFER varchar(255),
        COMENTARIO varchar(255),
        MODIFICADO datetime,
        MODIFICADO_USR varchar(255),
        CREADO datetime,
        CREADO_USR varchar(255),
		createdIp varchar(255),
        createdMac varchar(255),
        updatedIp varchar(255),
        updatedMac varchar(255),
        primary key (TRASLADO_ID)
    ) ENGINE=InnoDB;noDB;

alter table SX_TRASLADOS 
        add index FK4170AD0920C3FF1F (SUCURSAL_ID), 
        add constraint FK4170AD0920C3FF1F 
        foreign key (SUCURSAL_ID) 
        references SW_SUCURSALES (SUCURSAL_ID);

    alter table SX_TRASLADOS 
        add index FK4170AD09F1D492E (SOL_ID), 
        add constraint FK4170AD09F1D492E 
        foreign key (SOL_ID) 
        references SX_SOLICITUD_TRASLADOS (SOL_ID);


create table SX_INVENTARIO_TRD (
        INVENTARIO_ID varchar(255) not null,
        ALMACEN_ID bigint,
        CANTIDAD double precision not null,
        CLAVE varchar(10) not null,
        COMENTARIO varchar(250),
        COSTO numeric(14,6) not null,
        COSTOP numeric(14,6) not null,
        COSTOU numeric(14,6) not null,
        CREADO time,
        CREADO_USERID varchar(255),
        DESCRIPCION varchar(250) not null,
        DOCUMENTO bigint,
        EXISTENCIA DOUBLE default 0 not null,
        FACTORU double precision not null,
        FECHA datetime not null,
        KILOS double precision,
        MODIFICADO time,
        NACIONAL bit not null,
        RENGLON integer,
        MODIFICADO_USERID varchar(255),
        version integer not null,
        PRODUCTO_ID bigint not null,
        UNIDAD_ID varchar(3) not null,
        SUCURSAL_ID bigint not null,
        origen integer not null,
        SOLICITADO double precision not null,
        TIPO varchar(3) not null,
        TRASLADO_ID varchar(255) not null,
        RNGL integer,
        primary key (INVENTARIO_ID)
    ) ENGINE=InnoDB;


alter table SX_INVENTARIO_TRD 
        add index FKE13E8F911023FC44bcf7a0d2 (UNIDAD_ID), 
        add constraint FKE13E8F911023FC44bcf7a0d2 
        foreign key (UNIDAD_ID) 
        references SX_UNIDADES (UNIDAD);

    alter table SX_INVENTARIO_TRD 
        add index FKBCF7A0D2B1660D6B (TRASLADO_ID), 
        add constraint FKBCF7A0D2B1660D6B 
        foreign key (TRASLADO_ID) 
        references SX_TRASLADOS (TRASLADO_ID);

    alter table SX_INVENTARIO_TRD 
        add index FKE13E8F91F14A8924bcf7a0d2 (PRODUCTO_ID), 
        add constraint FKE13E8F91F14A8924bcf7a0d2 
        foreign key (PRODUCTO_ID) 
        references SX_PRODUCTOS (PRODUCTO_ID);

    alter table SX_INVENTARIO_TRD 
        add index FKE13E8F9120C3FF1Fbcf7a0d2 (SUCURSAL_ID), 
        add constraint FKE13E8F9120C3FF1Fbcf7a0d2 
        foreign key (SUCURSAL_ID) 
        references SW_SUCURSALES (SUCURSAL_ID);

