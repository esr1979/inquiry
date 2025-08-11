-- Este es un script de creación de tabla TRADUCIDO para ser compatible con H2.

CREATE TABLE WEXHVLOC
(
    -- Mapeos directos que funcionan bien
    CDISOLOC  CHAR(2)      default ' ' not null,
    CDCOMPANY NUMERIC(1)   default 0   not null,
    CDDEALER  NUMERIC(5)   default 0   not null,
    CHASSIS   CHAR(17)     default ' ' not null,
    NMEXHVL   DECIMAL(9)   default 0   not null,
    CDEXHVL   CHAR(3)      default ' ' not null,
    DTINILOC  DECIMAL(8)   default 0   not null,
    DTFINLOC  DECIMAL(8)   default 0   not null,

    -- Columna problemática NCHAR, traducida a VARCHAR con un default simple.
    -- No podemos replicar el valor por defecto exótico de DB2, así que usamos un espacio.
    ADEXHVL   VARCHAR(100) default ' ' not null,

    -- Mapeos directos
    LGEXHVL   DECIMAL(6)   default 0   not null,
    TMEXHVL   CHAR(4)      default ' ' not null,
    DTAPPRV   DECIMAL(8)   default 0   not null,
    SNCREATE  CHAR(10)     default ' ' not null,

    -- Usamos TIMESTAMP que es más estándar en H2.
    TSCREATE  TIMESTAMP    default CURRENT_TIMESTAMP not null,

    SNLSTUPD  CHAR(10)     default ' ' not null,

    -- Usamos TIMESTAMP aquí también.
    TSLSTUPD  TIMESTAMP    default CURRENT_TIMESTAMP not null,

    CDCHGSTS  CHAR(2)      default ' ' not null
);
