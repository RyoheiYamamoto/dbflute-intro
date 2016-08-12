/*uili
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
package org.dbflute.intro.app.logic.intro;

/**
 * @author p1us2er0
 * @author jflute
 */
public class IntroPhysicalLogic {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ENGINE_TEMPLATE_PATH = "mydbflute/dbflute-%1$s";

    /**
     * <pre>
     * e.g. "."
     *  dbflute-intro
     *   |-dbflute_maihamadb // client
     *   |-mydbflute         // engine
     *   |-dbflute-intro.jar
     * </pre>
     */
    public static final String BASE_DIR_PATH = ".";

    // ===================================================================================
    //                                                                              Client
    //                                                                              ======
    /**
     * <pre>
     * e.g.
     *  toDBFluteClientPath("maihamadb"): ./dbflute_maihamadb
     * </pre>
     * @param clientProject The project name of DBFlute client. (NotNull)
     * @return The path to the DBFlute client. (NotNull)
     */
    public String buildClientPath(String clientProject) {
        return buildBasicPath("dbflute_" + clientProject);
    }

    public String buildClientResourcePath(String clientProject, String resource) {
        return buildClientPath(clientProject) + "/" + resource;
    }

    public String buildDfpropDirPath(String clientProject) {
        return buildClientResourcePath(clientProject, "dfprop");
    }

    public String buildDocumentOutputDirPath(String clientProject) {
        return buildClientResourcePath(clientProject, "output/doc");
    }

    // ===================================================================================
    //                                                                              Engine
    //                                                                              ======
    public String buildEnginePath(String dbfluteVersion) {
        return buildBasicPath(String.format(ENGINE_TEMPLATE_PATH, dbfluteVersion));
    }

    public String buildEngineResourcePath(String dbfluteVersion, String resource) {
        return buildEnginePath(dbfluteVersion) + "/" + resource;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String buildBasicPath(String path) {
        return BASE_DIR_PATH + "/" + path;
    }
}