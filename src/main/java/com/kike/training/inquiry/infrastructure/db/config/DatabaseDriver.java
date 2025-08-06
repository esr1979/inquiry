package com.kike.training.inquiry.infrastructure.db.config;

public enum DatabaseDriver {
    DB2_AS400("com.ibm.as400.access.AS400JDBCDriver", "SELECT 1 FROM SYSIBM.SYSDUMMY1");

    private final String driverClassName;
    private final String validationQuery;

    DatabaseDriver(String driverClassName, String validationQuery) {
        this.driverClassName = driverClassName;
        this.validationQuery = validationQuery;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getValidationQuery() {
        return validationQuery;
    }
}
