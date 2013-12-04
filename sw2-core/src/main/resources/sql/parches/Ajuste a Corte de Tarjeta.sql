create table SX_CORTE_TARJETAS_APLICACIONES (
    CORTE_ID bigint not null
, CARGOABONO_ID bigint not null
, COMENTARIO varchar(255)
, IMPORTE numeric(16,2) not null
, ORDEN integer not null
, SUCURSAL_ID bigint
, TIPO varchar(20) not null
, primary key (CORTE_ID, CARGOABONO_ID)) 
ENGINE=InnoDB

alter table SX_CORTE_TARJETAS_APLICACIONES add index FKA0F67CD4568F1D83 (CARGOABONO_ID), add constraint FKA0F67CD4568F1D83 foreign key (CARGOABONO_ID) references SW_BCARGOABONO (CARGOABONO_ID)

alter table SX_CORTE_TARJETAS_APLICACIONES add index FKA0F67CD4D1B80842 (CORTE_ID), add constraint FKA0F67CD4D1B80842 foreign key (CORTE_ID) references SX_CORTE_TARJETAS (CORTE_ID)

alter table SX_CORTE_TARJETAS_APLICACIONES add index FKA0F67CD420C3FF1F (SUCURSAL_ID), add constraint FKA0F67CD420C3FF1F foreign key (SUCURSAL_ID) references SW_SUCURSALES (SUCURSAL_ID)
