package org.dbflute.intro.app.web.client;

import org.apache.commons.io.FileUtils;
import org.dbflute.intro.app.logic.simple.DbFluteIntroLogic;
import org.dbflute.intro.app.web.base.IntroBaseAction;
import org.dbflute.intro.mylasta.exception.DfpropFileNotFoundException;
import org.lastaflute.core.exception.LaSystemException;
import org.lastaflute.web.Execute;
import org.lastaflute.web.response.JsonResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author deco
 */
public class ClientDfpropAction extends IntroBaseAction {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String UTF8 = "UTF-8";

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                                 index
    //                                                 -----
    @Execute
    public JsonResponse<List<ClientDfpropBean>> index(String project) {
        File[] dfpropFiles = findDfpropFiles(project);
        List<ClientDfpropBean> beans = mappingToBeans(dfpropFiles);
        return asJson(beans);
    }

    private File[] findDfpropFiles(String project) {
        File dfpropDir = new File(DbFluteIntroLogic.BASE_DIR_PATH, getProjectPath(project));
        File[] dfpropFiles = dfpropDir.listFiles((dir, name) -> name.endsWith(".dfprop"));
        if (dfpropFiles == null || dfpropFiles.length == 0) {
            throw new DfpropFileNotFoundException("Not found dfprop files. file dir: " + dfpropDir.getPath());
        }
        return dfpropFiles;
    }

    private List<ClientDfpropBean> mappingToBeans(File[] dfpropFiles) {
        return Stream.of(dfpropFiles).map(dfpropFile -> {
            String fileText;
            try {
                fileText = FileUtils.readFileToString(dfpropFile, UTF8);
            } catch (IOException e) {
                throw new LaSystemException("Cannot read the file: " + dfpropFile);
            }
            return new ClientDfpropBean(dfpropFile.getName(), fileText);
        }).collect(Collectors.toList());
    }

    // -----------------------------------------------------
    //                                                update
    //                                                ------
    @Execute(urlPattern = "@word/{}")
    public JsonResponse<Void> update(String project, ClientDfpropUpdateForm form) {
        validate(form, messages -> {});

        File dfpropFile = findDfpropFile(project, form);
        writeDfpropFile(form.content, dfpropFile);

        return JsonResponse.asEmptyBody();
    }

    private File findDfpropFile(String project, ClientDfpropUpdateForm form) {
        File dfpropFile = new File(DbFluteIntroLogic.BASE_DIR_PATH, getProjectPath(project) + form.fileName);
        if (!dfpropFile.isFile()) {
            throw new DfpropFileNotFoundException("Not found dfprop file: " + dfpropFile.getPath());
        }
        return dfpropFile;
    }

    private void writeDfpropFile(String content, File dfpropFile) {
        try {
            FileUtils.write(dfpropFile, content, UTF8);
        } catch (IOException e) {
            throw new LaSystemException("Cannot write the file: " + dfpropFile);
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private String getProjectPath(String project) {
        return "dbflute_" + project + "/dfprop/";
    }
}
