package com.kike.training.inquiry.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa un registro de la tabla WEXHVLOC.
 * Esta clase está diseñada para ser usada con Spring Data JDBC.
 *
 * Dado que Spring Data JDBC requiere una única columna @Id y la tabla
 * tiene una clave compuesta, hemos designado 'cdisoloc' como el @Id formal.
 * Las operaciones de búsqueda reales deben usar consultas personalizadas
 * en el repositorio que utilicen la clave compuesta completa.
 */
@Table("WEXHVLOC")
public class Wexhvloc {

    /**
     *  La clave real de la tabla son estos 5 primeros campos.
     */
    @Id
    private String cdisoloc;
    private BigDecimal cdcompany;
    private BigDecimal cddealer;
    private String chassis;
    private BigDecimal nmexhvl;

    private String cdexhvl;
    private BigDecimal dtiniloc;
    private BigDecimal dtfinloc;
    private String adexhvl;
    private BigDecimal lgexhvl;
    private String tmexhvl;
    private BigDecimal dtapprv;
    private String sncreate;
    private LocalDateTime tscreate;
    private String snlstupd;
    private LocalDateTime tslstupd;
    private String cdchgsts;

    // --- Getters y Setters ---

    public String getCdisoloc() {
        return cdisoloc;
    }

    public void setCdisoloc(String cdisoloc) {
        this.cdisoloc = cdisoloc;
    }

    public BigDecimal getCdcompany() {
        return cdcompany;
    }

    public void setCdcompany(BigDecimal cdcompany) {
        this.cdcompany = cdcompany;
    }

    public BigDecimal getCddealer() {
        return cddealer;
    }

    public void setCddealer(BigDecimal cddealer) {
        this.cddealer = cddealer;
    }

    public String getChassis() {
        return chassis;
    }

    public void setChassis(String chassis) {
        this.chassis = chassis;
    }

    public BigDecimal getNmexhvl() {
        return nmexhvl;
    }

    public void setNmexhvl(BigDecimal nmexhvl) {
        this.nmexhvl = nmexhvl;
    }

    public String getCdexhvl() {
        return cdexhvl;
    }

    public void setCdexhvl(String cdexhvl) {
        this.cdexhvl = cdexhvl;
    }

    public BigDecimal getDtiniloc() {
        return dtiniloc;
    }

    public void setDtiniloc(BigDecimal dtiniloc) {
        this.dtiniloc = dtiniloc;
    }

    public BigDecimal getDtfinloc() {
        return dtfinloc;
    }

    public void setDtfinloc(BigDecimal dtfinloc) {
        this.dtfinloc = dtfinloc;
    }

    public String getAdexhvl() {
        return adexhvl;
    }

    public void setAdexhvl(String adexhvl) {
        this.adexhvl = adexhvl;
    }

    public BigDecimal getLgexhvl() {
        return lgexhvl;
    }

    public void setLgexhvl(BigDecimal lgexhvl) {
        this.lgexhvl = lgexhvl;
    }

    public String getTmexhvl() {
        return tmexhvl;
    }

    public void setTmexhvl(String tmexhvl) {
        this.tmexhvl = tmexhvl;
    }

    public BigDecimal getDtapprv() {
        return dtapprv;
    }

    public void setDtapprv(BigDecimal dtapprv) {
        this.dtapprv = dtapprv;
    }

    public String getSncreate() {
        return sncreate;
    }

    public void setSncreate(String sncreate) {
        this.sncreate = sncreate;
    }

    public LocalDateTime getTscreate() {
        return tscreate;
    }

    public void setTscreate(LocalDateTime tscreate) {
        this.tscreate = tscreate;
    }

    public String getSnlstupd() {
        return snlstupd;
    }

    public void setSnlstupd(String snlstupd) {
        this.snlstupd = snlstupd;
    }

    public LocalDateTime getTslstupd() {
        return tslstupd;
    }

    public void setTslstupd(LocalDateTime tslstupd) {
        this.tslstupd = tslstupd;
    }

    public String getCdchgsts() {
        return cdchgsts;
    }

    public void setCdchgsts(String cdchgsts) {
        this.cdchgsts = cdchgsts;
    }
}
