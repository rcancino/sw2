alter table SX_CLIENTES add column TX_IMPORTADO datetime;

alter table SX_CLIENTES add column TX_REPLICADO datetime;

alter table SX_CLIENTES_CREDITO add column TX_IMPORTADO datetime;

alter table SX_CLIENTES_CREDITO add column TX_REPLICADO datetime;

alter table SX_COMPRAS2 add column TX_IMPORTADO datetime;

alter table SX_COMPRAS2 add column TX_REPLICADO datetime;

alter table SX_DEVOLUCIONES add column TX_IMPORTADO datetime;

alter table SX_DEVOLUCIONES add column TX_REPLICADO datetime;

alter table SX_DEVOLUCION_COMPRAS add column TX_IMPORTADO datetime;

alter table SX_DEVOLUCION_COMPRAS add column TX_REPLICADO datetime;

alter table SX_ENTRADA_COMPRAS add column TX_IMPORTADO datetime;

alter table SX_ENTRADA_COMPRAS add column TX_REPLICADO datetime;

alter table SX_EXISTENCIAS add column TX_IMPORTADO datetime;

alter table SX_EXISTENCIAS add column TX_REPLICADO datetime;

alter table SX_KITS add column TX_IMPORTADO datetime;

alter table SX_KITS add column TX_REPLICADO datetime;

alter table SX_MOVI add column TX_IMPORTADO datetime;

alter table SX_MOVI add column TX_REPLICADO datetime;

alter table SX_PRODUCTOS add column TX_IMPORTADO datetime;

alter table SX_PRODUCTOS add column TX_REPLICADO datetime;

alter table SX_SOLICITUD_TRASLADOS add column IMPORTADO datetime;

alter table SX_TRANSFORMACIONES add column TX_IMPORTADO datetime;

alter table SX_TRANSFORMACIONES add column TX_REPLICADO datetime;

alter table SX_TRASLADOS add column IMPORTADO datetime;

alter table SX_VENTAS add column TX_IMPORTADO datetime;

alter table SX_VENTAS add column TX_REPLICADO datetime;

alter table SX_CXC_ABONOS add column TX_IMPORTADO datetime;

alter table SX_CXC_ABONOS add column TX_REPLICADO datetime;