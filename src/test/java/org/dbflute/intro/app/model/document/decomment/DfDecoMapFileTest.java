package org.dbflute.intro.app.model.document.decomment;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapColumnPart.ColumnPropertyPart;
import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapTablePart;
import org.dbflute.intro.unit.UnitIntroTestCase;
import org.dbflute.optional.OptionalThing;

/**
 * @author hakiba
 */
public class DfDecoMapFileTest extends UnitIntroTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Long LATEST_COMMENT_VERSION = 3L;

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    public void test_merge_all_column_comment() throws Exception {
        // ## Arrange ##
        DfDecoMapFile decoMapFile = new DfDecoMapFile();
        OptionalThing<DfDecoMapPickup> optPickup = OptionalThing.empty(); // not exists pickup
        LocalDateTime now = currentLocalDateTime();
        DfDecoMapPiece piece1 = preparePiece("MEMBER", "MEMBER_NAME", "hakiba", LATEST_COMMENT_VERSION, now);
        DfDecoMapPiece piece2 = preparePiece("MEMBER", "MEMBER_STATUS", "cabos", LATEST_COMMENT_VERSION, now);
        DfDecoMapPiece piece3 = preparePiece("PURCHASE", "PURCHASE_PRODUCT", "cabos", LATEST_COMMENT_VERSION, now);
        List<DfDecoMapPiece> pieceList = Arrays.asList(piece1, piece2, piece3);

        // ## Act ##
        DfDecoMapPickup result = decoMapFile.merge(optPickup, pieceList);

        // ## Assert ##
        assertNotNull(result);
        log(result);
        // assert all table and column
        long columnCount = result.getDecoMap()
            .stream()
            .map(tableParts -> tableParts)
            .map(tablePart -> tablePart.getColumns())
            .flatMap(columnParts -> columnParts.stream())
            .count();
        assertEquals(3, columnCount);
    }

    public void test_merge_latest_comment_version() throws Exception {
        // ## Arrange ##
        DfDecoMapFile decoMapFile = new DfDecoMapFile();
        OptionalThing<DfDecoMapPickup> optPickup = preparePickup();
        LocalDateTime now = currentLocalDateTime();
        DfDecoMapPiece oldPiece1 = preparePiece("MEMBER", "MEMBER_NAME", "hakiba", LATEST_COMMENT_VERSION - 1, now);
        DfDecoMapPiece latestPiece1 = preparePiece("MEMBER", "MEMBER_NAME", "hakiba", LATEST_COMMENT_VERSION, now);
        DfDecoMapPiece latestPiece2 = preparePiece("MEMBER", "MEMBER_STATUS", "cabos", LATEST_COMMENT_VERSION, now);
        List<DfDecoMapPiece> pieceList = Arrays.asList(oldPiece1, latestPiece1, latestPiece2);

        // ## Act ##
        DfDecoMapPickup result = decoMapFile.merge(optPickup, pieceList);

        // ## Assert ##
        assertNotNull(result);
        // assert all comment version are latest
        result.getDecoMap()
            .stream()
            .map(tablePart -> tablePart.getColumns())
            .flatMap(columnParts -> columnParts.stream())
            .map(columnPart -> columnPart.getProperties())
            .flatMap(propertyParts -> propertyParts.stream())
            .map(columnPropertyPart -> columnPropertyPart.getCommentVersion())
            .forEach(commentVersion -> assertEquals(commentVersion, LATEST_COMMENT_VERSION));
    }

    private OptionalThing<DfDecoMapPickup> preparePickup() {
        ColumnPropertyPart propertyPart = new ColumnPropertyPart();
        propertyPart.setDecomment("decomment");
        propertyPart.setDatabaseComment("databasecomment");
        propertyPart.setPreviousWholeComment("previous comment");
        propertyPart.setAuthorList(Collections.singletonList("hakiba"));
        propertyPart.setCommentVersion(LATEST_COMMENT_VERSION);

        DfDecoMapColumnPart columnPart = new DfDecoMapColumnPart();
        columnPart.setColumnName("MEMBER");
        columnPart.setProperties(Collections.singletonList(propertyPart));

        DfDecoMapTablePart tablePart = new DfDecoMapTablePart();
        tablePart.setTableName("MEMBER_NAME");
        tablePart.setColumns(Collections.singletonList(columnPart));

        DfDecoMapPickup pickup = new DfDecoMapPickup();
        pickup.setFileName("pickup");
        pickup.setFormatVersion("1.0");
        pickup.setDecoMap(null);

        return OptionalThing.of(pickup);
    }

    private DfDecoMapPiece preparePiece(String tableName, String columnName, String author, long commentVersion,
        LocalDateTime decommentDateTime) {
        ColumnPropertyPart propertyPart = new ColumnPropertyPart();
        propertyPart.setDecomment("decomment");
        propertyPart.setDatabaseComment("databasecomment");
        propertyPart.setPreviousWholeComment("previous comment");
        propertyPart.setAuthorList(Collections.singletonList(author));
        propertyPart.setCommentVersion(commentVersion);

        DfDecoMapColumnPart columnPart = new DfDecoMapColumnPart();
        columnPart.setColumnName(columnName);
        columnPart.setProperties(Collections.singletonList(propertyPart));

        DfDecoMapTablePart tablePart = new DfDecoMapTablePart();
        tablePart.setTableName(tableName);
        tablePart.setColumns(Collections.singletonList(columnPart));

        DfDecoMapPiece piece = new DfDecoMapPiece();
        piece.setFileName("piece");
        piece.setDecommentDatetime(decommentDateTime);
        piece.setMerged(false);
        piece.setFormatVersion("1.0");
        piece.setAuthor(author);
        piece.setDecoMap(tablePart);
        return piece;
    }
}
