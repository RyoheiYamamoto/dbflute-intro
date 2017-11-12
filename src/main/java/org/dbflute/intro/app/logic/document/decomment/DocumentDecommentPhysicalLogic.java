package org.dbflute.intro.app.logic.document.decomment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.infra.doc.decomment.DfDecoMapFile;
import org.dbflute.infra.doc.decomment.DfDecoMapFile.DfDecoMapFileReadFailureException;
import org.dbflute.infra.doc.decomment.DfDecoMapFile.DfDecoMapFileWriteFailureException;
import org.dbflute.infra.doc.decomment.DfDecoMapPickup;
import org.dbflute.infra.doc.decomment.DfDecoMapPiece;
import org.dbflute.intro.app.logic.document.DocumentAuthorLogic;
import org.dbflute.intro.app.logic.intro.IntroPhysicalLogic;
import org.dbflute.intro.bizfw.tellfailure.PhysicalDecoMapFileException;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfStringUtil;
import org.lastaflute.core.time.TimeManager;

/**
 * @author cabos
 * @author hakiba
 * @author jflute
 */
public class DocumentDecommentPhysicalLogic {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // TODO cabos shugyo++: move it to infra's decomment classes, file name and path handling by jflute (2017/11/11)
    private static final String PICKUP_FILE_NAME = "decomment-pickup.dfmap";
    protected static final Map<String, String> REPLACE_CHAR_MAP;

    static {
        // done cabos add spaces and replaceChar should be underscore? by jflute (2017/09/07)
        List<String> notAvailableCharList = Arrays.asList("/", "\\", "<", ">", "*", "?", "\"", "|", ":", ";", "\0", " ");
        String replaceChar = "_";
        REPLACE_CHAR_MAP = notAvailableCharList.stream().collect(Collectors.toMap(ch -> ch, ch -> replaceChar));
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // TODO done cabos move this above logic DI variables, framework component should be top (jflute policy...) by jflute (2017/11/11)
    @Resource
    private TimeManager timeManager;
    @Resource
    private IntroPhysicalLogic introPhysicalLogic;
    @Resource
    private DocumentAuthorLogic documentAuthorLogic;

    // ===================================================================================
    //                                                                           Piece Map
    //                                                                           =========
    // TODO done cabos also rename pieceMap to piece (can be simple here) by jflute (2017/11/11)
    public void saveDecommentPiece(String clientProject, DfDecoMapPiece decoMapPiece) {
        String tableName = decoMapPiece.getTableName();
        String columnName = decoMapPiece.getColumnName();
        String owner = decoMapPiece.getPieceOwner();
        String pieceCode = decoMapPiece.getPieceCode();
        String pieceMapPath = buildDecommentPiecePath(clientProject, buildPieceFileName(tableName, columnName, owner, pieceCode));
        try {
            // done cabos remove 'df' from variable name by jflute (2017/08/10)
            DfDecoMapFile decoMapFile = new DfDecoMapFile();
            decoMapFile.writePiece(pieceMapPath, decoMapPiece);
            // done cabos make and throw PhysicalCabosException (application exception) see ClientNotFoundException by jflute (2017/08/10)
        } catch (DfDecoMapFileWriteFailureException | FileNotFoundException | SecurityException e) {
            throw new PhysicalDecoMapFileException("fail to open decomment piece map file, file path : " + pieceMapPath, pieceMapPath, e);
        } catch (IOException e) {
            throw new PhysicalDecoMapFileException("maybe... fail to execute \"outputStream.close()\".", pieceMapPath, e);
        }
    }

    protected String buildPieceFileName(String tableName, String columnName, String owner,
        String pieceCode) { // e.g decomment-piece-TABLE_NAME-COLUMN_NAME-20170316-123456-789-jflute-FE893L1.dfmap
        return "decomment-piece-" + tableName + "-" + columnName + "-" + getCurrentDateStr() + "-" + owner + "-" + pieceCode + ".dfmap";
    }

    protected String getCurrentDateStr() {
        return DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(timeManager.currentDateTime());
    }

    protected void createPieceMapFile(File pieceMapFile) {
        try {
            Files.createDirectories(Paths.get(pieceMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(pieceMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throw new PhysicalDecoMapFileException("fail to create decomment piece map file, file path : " + pieceMapFile.getAbsolutePath(),
                pieceMapFile.getAbsolutePath(), e);
        }
    }

    // ===================================================================================
    //                                                                          Pickup Map
    //                                                                          ==========
    // done hakiba tag comment: Pickup Map by jflute (2017/08/17)
    public DfDecoMapPickup readMergedPickup(String clientProject) {
        List<DfDecoMapPiece> pieces = readAllDecommentPiece(clientProject);
        OptionalThing<DfDecoMapPickup> pickupOpt = readDecommentPickup(clientProject);
        DfDecoMapFile decoMapFile = new DfDecoMapFile();
        return decoMapFile.merge(pickupOpt, pieces);
    }

    // done hakoba public on demand, so private now by jflute (2017/08/17)
    private List<DfDecoMapPiece> readAllDecommentPiece(String clientProject) {
        String dirPath = buildDecommentPieceDirPath(clientProject);
        // done hakiba support no-existing directory by jflute (2017/09/28)
        if (Files.notExists(Paths.get(dirPath))) {
            return Collections.emptyList();
        }
        try {
            return Files.list(Paths.get(dirPath)).filter(path -> path.toString().endsWith(".dfmap")).map(path -> {
                try {
                    DfDecoMapFile decoMapFile = new DfDecoMapFile();
                    return decoMapFile.readPiece(Files.newInputStream(path));
                } catch (DfDecoMapFileReadFailureException | IOException e) {
                    String debugMsg = "Failed to read decomment piece map: filePath=" + path;
                    throw new PhysicalDecoMapFileException(debugMsg, path.toString(), e);
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("fail to read decomment piece map directory. path : " + dirPath, e);
        }
    }

    private OptionalThing<DfDecoMapPickup> readDecommentPickup(String clientProject) {
        String filePath = buildDecommentPickupPath(clientProject);
        // done hakiba support no-existing directory or file by jflute (2017/09/28)
        if (Files.notExists(Paths.get(filePath))) {
            // done hakiba null pointer so use optional thing and stream empty by jflute (2017/10/05)
            return OptionalThing.empty();
        }
        try {
            DfDecoMapFile decoMapFile = new DfDecoMapFile();
            return OptionalThing.of(decoMapFile.readPickup(Files.newInputStream(Paths.get(filePath))));
        } catch (DfDecoMapFileReadFailureException | IOException e) {
            String debugMsg = "Failed to read decomment pickup map: filePath=" + filePath;
            throw new PhysicalDecoMapFileException(debugMsg, filePath, e);
        }
    }

    // ===================================================================================
    //                                                                              Author
    //                                                                              ======
    public String getAuthor() {
        return filterAuthor(documentAuthorLogic.getAuthor());
    }

    private String filterAuthor(String author) {
        return DfStringUtil.replaceBy(author, REPLACE_CHAR_MAP);
    }

    // ===================================================================================
    //                                                                               Path
    //                                                                              ======
    protected String buildDecommentPieceDirPath(String clientProject) {
        return introPhysicalLogic.buildClientPath(clientProject, "schema", "decomment", "piece");
    }

    protected String buildDecommentPiecePath(String clientProject, String fileName) {
        return introPhysicalLogic.buildClientPath(clientProject, "schema", "decomment", "piece", fileName);
    }

    private String buildDecommentPickupPath(String clientProject) {
        return introPhysicalLogic.buildClientPath(clientProject, "schema", "decomment", "pickup", PICKUP_FILE_NAME);
    }
}
