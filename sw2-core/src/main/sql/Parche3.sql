    
## Generacion de tablas para el manejo de las fichas de depositos

drop table if exists SX_FICHAS;
create table SX_FICHAS (
FICHA_ID varchar(255) not null
, COMENTARIO varchar(255)
, FECHA date not null, CREADO time
, CREADO_USERID varchar(255), MODIFICADO time
, MODIFICADO_USERID varchar(255)
, CUENTA_ID bigint not null
, SUCURSAL_ID bigint not null
, primary key (FICHA_ID)
) ENGINE=InnoDB



drop table if exists SX_FICHASDET;
create table SX_FICHASDET (
FICHADET_ID varchar(255) not null
, BANCO varchar(255)
, CHEQUE numeric(19,2), EFECTIVO numeric(19,2)
, IMPORTE numeric(19,2)
, ABONO_ID varchar(255) not null
, FICHA_ID varchar(255) not null
, RENGLON integer
, primary key (FICHADET_ID)
) ENGINE=InnoDB


alter table SX_FICHAS add index FKE49EE1B420C3FF1F (SUCURSAL_ID)
, add constraint FKE49EE1B420C3FF1F foreign key (SUCURSAL_ID) references SW_SUCURSALES (SUCURSAL_ID)

alter table SX_FICHAS add index FKE49EE1B4EEF94E03 (CUENTA_ID)
, add constraint FKE49EE1B4EEF94E03 foreign key (CUENTA_ID) references SW_CUENTAS (id)

alter table SX_FICHASDET add index FKD53C59BF7D6FEC75 (FICHA_ID), add constraint
 FKD53C59BF7D6FEC75 foreign key (FICHA_ID) references SX_FICHAS (FICHA_ID)

alter table SX_FICHASDET add index FKD53C59BF97A49E89 (ABONO_ID), add constraint
 FKD53C59BF97A49E89 foreign key (ABONO_ID) references SX_CXC_ABONOS (ABONO_ID)






## Modificaciones a columnas de SX_VENTASDET 

ALTER TABLE sx_ventasdet MODIFY COLUMN `PRECIO_L` DECIMAL(19,4) NOT NULL DEFAULT 0    
  
ALTER TABLE sx_ventasdet MODIFY COLUMN `PRECIO` DECIMAL(19,4) NOT NULL DEFAULT 0

ALTER TABLE sx_ventasdet MODIFY COLUMN `IMPORTE` DECIMAL(19,4) NOT NULL DEFAULT 0    

ALTER TABLE sx_ventasdet MODIFY COLUMN `SUBTOTAL` DECIMAL(19,4) NOT NULL DEFAULT 0           
    
ALTER TABLE sx_ventasdet MODIFY COLUMN `IMPORTE_NETO` DECIMAL(19,4) NOT NULL DEFAULT 0        
    
ALTER TABLE sx_ventasdet MODIFY COLUMN `PRECIO_CORTES` DECIMAL(19,4) NOT NULL DEFAULT 0        
    
       
