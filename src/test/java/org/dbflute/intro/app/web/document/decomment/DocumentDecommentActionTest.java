package org.dbflute.intro.app.web.document.decomment;

import java.util.Arrays;
import java.util.Collections;

import org.dbflute.intro.app.web.document.decomment.DecommentSaveBody.DecommentTablePart;
import org.dbflute.intro.app.web.document.decomment.DecommentSaveBody.DecommentTablePart.DecommentColumnPart;
import org.dbflute.intro.unit.DocumentDecommentUnitIntroTestCase;
import org.junit.Test;
import org.lastaflute.web.response.JsonResponse;

/**
 * @author cabos
 * @author hakiba
 */
public class DocumentDecommentActionTest extends DocumentDecommentUnitIntroTestCase {

    // ===================================================================================
    //                                                                                Test
    //                                                                                ====
    @Test
    public void test_save_createHakiMap() throws Exception {
        // ## Arrange ##
        DocumentDecommentAction action = new DocumentDecommentAction();
        inject(action);
        DecommentSaveBody body = createSampleBody();

        // ## Act ##
        action.save("introdb", body);

        // ## Assert ##
        // Assert by visual confirmation
    }

    private DecommentSaveBody createSampleBody() {
        DecommentSaveBody body = new DecommentSaveBody();
        body.merged = false;
        body.table = createSampleTablePart();
        return body;
    }

    private DecommentTablePart createSampleTablePart() {
        DecommentTablePart tablePart = new DecommentTablePart();
        tablePart.tableName = "TABLE_NAME";
        tablePart.columns = Collections.singletonList(createSampleColumnPart());
        return tablePart;
    }

    private DecommentColumnPart createSampleColumnPart() {
        DecommentColumnPart columnPart = new DecommentColumnPart();
        columnPart.authorList = Arrays.asList("cabos", "sudachi");
        columnPart.columnName = "COLUMN_NAME";
        columnPart.commentVersion = 1L;
        columnPart.decomment = "orange";
        columnPart.databaseComment = "rime";
        columnPart.previousWholeComment = "lemon";
        return columnPart;
    }

    @Test
    public void test_read_PickupResult() throws Exception {
        // ## Arrange ##
        // done hakiba put test data in test/resources by hakiba (2017/08/18)
        DocumentDecommentAction action = new DocumentDecommentAction();
        inject(action);

        // ## Act ##
        JsonResponse<DecommentPickupResult> response = action.pickup(TEST_CLIENT_PROJECT);

        // ## Assert ##
        // done hakiba like this by jflute (2017/08/17)
        //showJson(response);
        //TestingJsonData<DecommentPickupResult> jsonData = validateJsonData(response);
        //DecommentPickupResult result = jsonData.getJsonResult();

        showJson(response);
        validateJsonData(response);
    }
}
