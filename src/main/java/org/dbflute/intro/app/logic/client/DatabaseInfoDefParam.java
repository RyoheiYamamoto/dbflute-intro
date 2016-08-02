/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.intro.app.logic.client;

import org.dbflute.intro.app.def.DatabaseInfoDef;

/**
 * @author p1us2er0
 * @author jflute
 */
public class DatabaseInfoDefParam {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final String databaseName;
    private final String driverName;
    private final String urlTemplate;
    private final String defultSchema;
    private final boolean needSchema;
    private final boolean needJdbcDriverJar;
    private final boolean upperSchema;
    private final boolean assistInputUser;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DatabaseInfoDefParam(DatabaseInfoDef databaseInfoDef) {
        this.databaseName = databaseInfoDef.getDatabaseName();
        this.driverName = databaseInfoDef.getDriverName();
        this.urlTemplate = databaseInfoDef.getUrlTemplate();
        this.defultSchema = databaseInfoDef.getDefultSchema();
        this.needSchema = databaseInfoDef.isNeedSchema();
        this.needJdbcDriverJar = databaseInfoDef.isNeedJdbcDriverJar();
        this.upperSchema = databaseInfoDef.isUpperSchema();
        this.assistInputUser = databaseInfoDef.isAssistInputUser();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getDatabaseName() {
        return databaseName;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public String getDefultSchema() {
        return defultSchema;
    }

    public boolean isNeedJdbcDriverJar() {
        return needJdbcDriverJar;
    }

    public boolean isNeedSchema() {
        return needSchema;
    }

    public boolean isUpperSchema() {
        return upperSchema;
    }

    public boolean isAssistInputUser() {
        return assistInputUser;
    }
}
