package org.dbflute.intro.app.logic.document.decomment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.dbflute.intro.app.logic.intro.IntroPhysicalLogic;
import org.dbflute.intro.app.model.document.decomment.DfDecoMapFile;
import org.dbflute.intro.app.model.document.decomment.DfDecoMapPiece;
import org.lastaflute.core.time.TimeManager;

/**
 * @author cabos
 */
public class DocumentDecommentPhysicalLogic {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private IntroPhysicalLogic introPhysicalLogic;
    @Resource
    private TimeManager timeManager;

    // ===================================================================================
    //                                                                           Piece Map
    //                                                                           =========
    public void saveDecommentPieceMap(String clientProject, DfDecoMapPiece decoMapPiece) {
        String tableName = decoMapPiece.getDecoMap().getTableName();
        String author = decoMapPiece.getAuthor();
        File pieceMapFile = new File(buildDecommentPiecePath(clientProject, buildPieceFileName(tableName, author)));
        createPieceMapFile(pieceMapFile);
        try (OutputStream outputStream = new FileOutputStream(pieceMapFile)) {
            DfDecoMapFile dfDecoMapFile = new DfDecoMapFile();
            dfDecoMapFile.writeMap(outputStream, decoMapPiece.convertMap());
        } catch (IOException e) {
            // TODO cabos throw more detail exception (2017/07/29)
            throw new UncheckedIOException(e);
        }
    }

    private String buildPieceFileName(String tableName, String author) { // e.g decomment-piece-MEMBER-20170316-123456-789-jflute.dfmap
        return "decomment-piece-" + tableName + "-" + getCurrentDateStr() + "-" + author + ".dfmap";
    }

    private String getCurrentDateStr() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
        return formatter.format(timeManager.currentDate());
    }

    private void createPieceMapFile(File pieceMapFile) {
        try {
            if (!pieceMapFile.createNewFile()) {
                // TODO cabos throw file exists exception (it is strange that same name file exists) (2017/07/29)
                throw new IllegalStateException(pieceMapFile.getName() + " is already exists.");
            }
        } catch (IOException e) {
            // TODO cabos throw more detail exception (2017/07/29)
            throw new UncheckedIOException("fail to create piece map file", e);
        }
    }

    // ===================================================================================
    //                                                                                Path
    //                                                                                ====
    private String buildDecommentPiecePath(String clientProject, String fileName) {
        return introPhysicalLogic.buildClientPath(clientProject, "schema", "decomment", "piece");
    }

    private String buildDecommentPickupPath(String clientProject) {
        return introPhysicalLogic.buildClientPath(clientProject, "schema", "decomment", "pickup");
    }
}
