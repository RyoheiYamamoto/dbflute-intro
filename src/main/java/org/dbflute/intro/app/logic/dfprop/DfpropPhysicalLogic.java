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
package org.dbflute.intro.app.logic.dfprop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.dbflute.intro.app.logic.intro.IntroPhysicalLogic;
import org.dbflute.intro.mylasta.exception.DfpropFileNotFoundException;
import org.lastaflute.core.exception.LaSystemException;

/**
 * @author deco
 * @author jflute
 */
public class DfpropPhysicalLogic {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String DFPROP_DIR_PATH = "dfprop";
    private static final String UTF8 = "UTF-8";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private IntroPhysicalLogic introPhysicalLogic;

    // ===================================================================================
    //                                                                               Path
    //                                                                              ======
    public String buildDfpropDirPath(String project) {
        return introPhysicalLogic.toDBFluteClientResourcePath(project, DFPROP_DIR_PATH);
    }

    public String buildDfpropFilePath(String project, String fileName) {
        String dfpropDirPath = buildDfpropDirPath(project);
        return dfpropDirPath + "/" + fileName;
    }

    // ===================================================================================
    //                                                                           Find/Read
    //                                                                           =========
    public File findDfpropFile(String project, String fileName) {
        final File dfpropFile = new File(buildDfpropFilePath(project, fileName));
        if (!dfpropFile.isFile()) {
            throw new DfpropFileNotFoundException("Not found dfprop file: " + dfpropFile.getPath());
        }
        return dfpropFile;
    }

    public List<File> findDfpropFileAllList(String project) {
        final File dfpropDir = new File(buildDfpropDirPath(project));
        final File[] dfpropFiles = dfpropDir.listFiles((dir, name) -> name.endsWith(".dfprop"));
        if (dfpropFiles == null || dfpropFiles.length == 0) {
            throw new DfpropFileNotFoundException("Not found dfprop files. file dir: " + dfpropDir.getPath());
        }
        return Collections.unmodifiableList(Arrays.asList(dfpropFiles));
    }

    public String readDfpropText(File dfpropFile) {
        String fileText;
        try {
            fileText = FileUtils.readFileToString(dfpropFile, UTF8);
        } catch (IOException e) {
            throw new LaSystemException("Cannot read the file: " + dfpropFile);
        }
        return fileText;
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void writeDfpropFile(String content, File dfpropFile) {
        try {
            FileUtils.write(dfpropFile, content, UTF8);
        } catch (IOException e) {
            throw new LaSystemException("Cannot write the file: " + dfpropFile);
        }
    }
}
