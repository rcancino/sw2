alter table SX_INVENTARIO_COM add column COSTO_FLETE numeric(16,6)

alter table SX_INVENTARIO_COM add column ANALISIS_FLETE_ID bigint

alter table SX_MAQ_ANALISIS_FLETE add column CXP_ID bigint

alter table SX_INVENTARIO_COM add index FKBCF760ADCA79ECB2 (ANALISIS_FLETE_ID),
    add constraint FKBCF760ADCA79ECB2 
    foreign key (ANALISIS_FLETE_ID) references SX_MAQ_ANALISIS_FLETE (ANALISIS_ID)

alter table SX_MAQ_ANALISIS_FLETE  add index FKAE5C79393480C4B4 (CXP_ID), 
    add constraint FKAE5C79393480C4B4 
    foreign key (CXP_ID) references SX_CXP (CXP_ID)

alter table SX_MAQ_ANALISIS_HOJEO add column CXP_ID bigint

alter table SX_MAQ_ANALISIS_HOJEO add index FKAE7A16563480C4B4 (CXP_ID), 
	add constraint FKAE7A16563480C4B4 
	foreign key (CXP_ID) references SX_CXP (CXP_ID)