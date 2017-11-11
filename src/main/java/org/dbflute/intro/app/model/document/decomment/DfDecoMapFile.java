package org.dbflute.intro.app.model.document.decomment;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbflute.exception.DfPropFileReadFailureException;
import org.dbflute.exception.DfPropFileWriteFailureException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.mapstring.MapListFile;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapTablePart;
import org.dbflute.intro.mylasta.appcls.AppCDef.PieceTargetType;
import org.dbflute.optional.OptionalThing;

// done cabos DfDecoMapFile by jflute (2017/07/27)

/**
 * @author cabos
 * @author hakiba
 * @author jflute
 */
public class DfDecoMapFile {

    // ===================================================================================
    //                                                                               Read
    //                                                                              ======
    // done yuto write e.g. (2017/11/11)
    // map:{
    //     ; formatVersion = 1.0
    //     ; merged = false
    //     ; tableName = MEMBER
    //     ; columnName = null
    //     ; targetType = TABLE
    //     ; decomment = loginable user, my name is deco
    //     ; databaseComment = loginable user
    //     ; commentVersion = 0
    //     ; authorList = list:{ deco }
    //     ; pieceCode = AL3OR1P
    //     ; pieceDatetime = 2017-12-31T12:34:56.789
    //     ; pieceOwner = deco
    //     ; previousPieceList = list:{}
    // }
    // map:{
    //     ; formatVersion = 1.0
    //     ; merged = false
    //     ; tableName = MEMBER
    //     ; columnName = MEMBER_NAME
    //     ; targetType = COLUMN
    //     ; decomment = sea mystic land oneman
    //     ; databaseComment = sea mystic
    //     ; commentVersion = 1
    //     ; authorList = list:{ cabos ; hakiba ; deco ; jflute }
    //     ; pieceCode = HF7ELSE
    //     ; pieceDatetime = 2017-10-15T16:17:18.199
    //     ; pieceOwner = jflute
    //     ; previousPieceList = list:{ FE893L1 }
    // }
    public DfDecoMapPiece readPiece(InputStream ins) {
        final MapListFile mapListFile = createMapListFile();
        try {
            Map<String, Object> map = mapListFile.readMap(ins);
            return mappingToDecoMapPiece(map);
        } catch (Exception e) {
            throwDecoMapReadFailureException(ins, e);
            return null; // unreachable
        }
    }

    // done hakiba cast check by hakiba (2017/07/29)
    @SuppressWarnings("unchecked")
    private DfDecoMapPiece mappingToDecoMapPiece(Map<String, Object> map) throws Exception {

        Boolean merged = Boolean.valueOf((String) map.get("merged"));
        String tableName = (String) map.get("tableName");
        String columnName = (String) map.get("columnName");
        PieceTargetType targetType = PieceTargetType.of(map.get("targetType")).get();
        String decomment = (String) map.get("decomment");
        String databaseComment = (String) map.get("databaseComment");
        Long commentVersion = Long.valueOf("commentVersion");
        List<String> authorList = (List<String>) map.get("authorList");
        String pieceCode = (String) map.get("pieceCode");
        LocalDateTime pieceDatetime = new HandyDate((String) map.get("pieceDatetime")).getLocalDateTime();
        String pieceOwner = (String) map.get("pieceOwner");
        List<String> previousPieceList = (List<String>) map.get("previousPieceList");
        String formatVersion = (String) map.get("formatVersion");

        DfDecoMapPiece piece = new DfDecoMapPiece();
        piece.setMerged(merged);
        piece.setTableName(tableName);
        piece.setColumnName(columnName);
        piece.setTargetType(targetType);
        piece.setDecomment(decomment);
        piece.setDatabaseComment(databaseComment);
        piece.setCommentVersion(commentVersion);
        piece.setAuthorList(authorList);
        piece.setPieceCode(pieceCode);
        piece.setPieceDatetime(pieceDatetime);
        piece.setPieceOwner(pieceOwner);
        piece.setPreviousPieceList(previousPieceList);
        piece.setFormatVersion(formatVersion);
        return piece;
    }

    // -----------------------------------------------------
    //                                                Pickup
    //                                                ------
    // map:{
    //     ; formatVersion = 1.0
    //     ; pickupDatetime = 2017-11-09T09:09:09.009
    //     ; decoMap = map:{
    //         ; tableList = list:{
    //             ; map:{
    //                 ; tableName = MEMBER
    //                 ; propertyList = list:{
    //                     ; map:{
    //                         ; decomment = first decomment
    //                         ; databaseComment = ...
    //                         ; commentVersion = ...
    //                         ; authorList = list:{ deco }
    //                         ; pieceCode = DECO0000
    //                         ; pieceDatetime = 2017-11-05T00:38:13.645
    //                         ; pieceOwner = cabos
    //                         ; previousPieceList = list:{}
    //                     }
    //                     ; map:{ // propertyList size is more than 2 if decomment conflicts exists
    //                         ; ...
    //                     }
    //                 }
    //                 ; columnList = list:{
    //                     ; map:{
    //                         ; columnName = MEMBER_NAME
    //                         ; propertyList = list:{
    //                             ; map:{
    //                                 ; decomment = sea mystic land oneman
    //                                 ; databaseComment = sea mystic
    //                                 ; commentVersion = 1
    //                                 ; authorList = list:{ cabos, hakiba, deco, jflute }
    //                                 ; pieceCode = HAKIBA00
    //                                 ; pieceDatetime = 2017-11-05T00:38:13.645
    //                                 ; pieceOwner = cabos
    //                                 ; previousPieceList = list:{ JFLUTE00, CABOS000 }
    //                             }
    //                         }
    //                     }
    //                     ; ... // more other columns
    //                 }
    //             }
    //             ; map:{ // Of course, other table decomment info is exists that
    //                 ; tableName = MEMBER_LOGIN
    //                 ; ...
    //             }
    //         }
    //     }
    // }
    // done hakiba sub tag comment by jflute (2017/08/17)
    public DfDecoMapPickup readPickup(InputStream ins) {
        MapListFile mapListFile = createMapListFile();
        try {
            Map<String, Object> map = mapListFile.readMap(ins);
            return mappingToDecoMapPickup(map);
        } catch (Exception e) {
            throwDecoMapReadFailureException(ins, e);
            return null; // unreachable
        }
    }

    private DfDecoMapPickup mappingToDecoMapPickup(Map<String, Object> map) {
        String formatVersion = (String) map.get("formatVersion");
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> decoMap = (Map<String, List<Map<String, Object>>>) map.get("decoMap");
        List<DfDecoMapTablePart> tableList = decoMap.get("tableList").stream().map(tablePartMap -> {
            return DfDecoMapTablePart.createTablePart(tablePartMap);
        }).collect(Collectors.toList());
        DfDecoMapPickup pickup = new DfDecoMapPickup();
        pickup.setFormatVersion(formatVersion);
        pickup.setTableList(tableList);
        return pickup;
    }

    protected void throwDecoMapReadFailureException(InputStream ins, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the deco-map file.");
        br.addItem("Advice");
        br.addElement("Make sure the map-string is correct in the file.");
        br.addElement("For example, the number of start and end braces are the same.");
        br.addItem("Decomment Map");
        br.addElement(ins);
        final String msg = br.buildExceptionMessage();
        throw new DfPropFileReadFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    // TODO hakiba be more rich method, e.g. saveDecommentPieceMap()'s logic by jflute (2017/09/21)
    public void writeMap(OutputStream ous, Map<String, Object> map) {
        final MapListFile mapListFile = createMapListFile();
        try {
            mapListFile.writeMap(ous, map);
        } catch (Exception e) {
            throwDecoMapWriteFailureException(ous, e);
        }
    }

    protected void throwDecoMapWriteFailureException(OutputStream ous, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to write the deco-map file.");
        br.addItem("Decomment Map");
        br.addElement(ous);
        final String msg = br.buildExceptionMessage();
        // done cabos use WriteFailure by jflute (2017/08/10)
        throw new DfPropFileWriteFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    // TODO hakiba write unit test by jflute (2017/09/21)
    public DfDecoMapPickup merge(OptionalThing<DfDecoMapPickup> pickupOpt, List<DfDecoMapPiece> pieces) {
        return new DfDecoMapPickup();
    }

    // hakiba's memorable code by jflute (2017/11/11)
    //public DfDecoMapPickup merge(OptionalThing<DfDecoMapPickup> pickupOpt, List<DfDecoMapPiece> pieces) {
    //    // Create all table part list
    //    final List<DfDecoMapTablePart> allTablePartListFiltered =
    //        generateLatestCommentVersionStream(pickupOpt, pieces).collect(Collectors.toList());
    //
    //    // Extract all table name
    //    final Set<String> allTableNameSet =
    //        allTablePartListFiltered.stream().map(tablePart -> tablePart.getTableName()).collect(Collectors.toSet());
    //    // Extract all column name
    //    final Set<String> allColumnNameSet = allTablePartListFiltered.stream()
    //        .flatMap(tablePart -> tablePart.getColumnList().stream())
    //        .map(columnPart -> columnPart.getColumnName())
    //        .collect(Collectors.toSet());
    //
    //    // Merge tables
    //    final List<DfDecoMapTablePart> mergedTableList = allTableNameSet.stream().map(tableName -> {
    //        // Merge columns
    //        final List<DfDecoMapColumnPart> mergedColumnPartList = allColumnNameSet.stream().map(columnName -> {
    //            // Merge propertyList of column (already filtering by latest comment version)
    //            final List<DfDecoMapPropertyPart> mergedProperties = allTablePartListFiltered.stream()
    //                .filter(tablePart -> tableName.equals(tablePart.getTableName()))
    //                .flatMap(tablePart -> tablePart.getColumnList().stream())
    //                .filter(columnPart -> columnName.equals(columnPart.getColumnName()))
    //                .flatMap(columnPart -> columnPart.getPropertyList().stream())
    //                .collect(Collectors.toList());
    //
    //            DfDecoMapColumnPart mergedColumn = new DfDecoMapColumnPart();
    //            mergedColumn.setColumnName(columnName);
    //            mergedColumn.setPropertyList(mergedProperties);
    //            return mergedColumn;
    //        }).filter(columnPart -> !columnPart.getPropertyList().isEmpty()).collect(Collectors.toList());
    //
    //        DfDecoMapTablePart mergedTablePart = new DfDecoMapTablePart();
    //        mergedTablePart.setTableName(tableName);
    //        mergedTablePart.setColumnList(mergedColumnPartList);
    //        return mergedTablePart;
    //    }).filter(tablePart -> !tablePart.getColumnList().isEmpty()).collect(Collectors.toList());
    //
    //    final String formatVersion = pickupOpt.map(pickup -> pickup.getFormatVersion()).orElse(null);
    //    DfDecoMapPickup mergedPickup = new DfDecoMapPickup();
    //    mergedPickup.setFormatVersion(formatVersion);
    //    mergedPickup.setTableList(mergedTableList);
    //    return mergedPickup;
    //}
    //
    //private Stream<DfDecoMapTablePart> generateLatestCommentVersionStream(OptionalThing<DfDecoMapPickup> pickupOpt,
    //    List<DfDecoMapPiece> pieces) {
    //    final Map<String, Long> latestCommentVersionMap = generateAllColumnLatestCommentVersionMap(pickupOpt, pieces);
    //
    //    // Pickup: Extract latest comment version column
    //    Stream<DfDecoMapTablePart> pickupStream =
    //        pickupOpt.map(pickup -> pickup.getTableList()).map(tableParts -> tableParts.stream()).orElse(Stream.empty()).map(tablePart -> {
    //            final List<DfDecoMapColumnPart> maxCommentVersionColumnPartList = tablePart.getColumnList()
    //                .stream()
    //                .filter(columnPart -> columnPart.getLatestCommentVersion() == latestCommentVersionMap.get(columnPart.getColumnName()))
    //                .collect(Collectors.toList());
    //            tablePart.setColumnList(maxCommentVersionColumnPartList);
    //            return tablePart;
    //        });
    //
    //    // filtering latest comment version column Function
    //    Function<DfDecoMapPiece, DfDecoMapPiece> filteringLatestCommentVersion = piece -> {
    //        piece.getTableList().forEach(tablePart -> {
    //            List<DfDecoMapColumnPart> columnList = tablePart.getColumnList()
    //                .stream()
    //                .filter(columnPart -> columnPart.getLatestCommentVersion() == latestCommentVersionMap.get(columnPart.getColumnName()))
    //                .collect(Collectors.toList());
    //            tablePart.setColumnList(columnList);
    //        });
    //        return piece;
    //    };
    //
    //    // filtering latest decomment datetime Function
    //    BinaryOperator<DfDecoMapPiece> filteringLatestDecommentDatetime = (v1, v2) -> {
    //        final Optional<LocalDateTime> optV1LocalDateTime = v1.getTableList()
    //            .stream()
    //            .flatMap(table -> table.getColumnList().stream())
    //            .flatMap(column -> column.getPropertyList().stream())
    //            .map(property -> property.getPieceDatetime())
    //            .filter(time -> time != null)  // none cabos why does here need null check?
    //            .findFirst();
    //        final Optional<LocalDateTime> optV2LocalDateTime = v2.getTableList()
    //            .stream()
    //            .flatMap(table -> table.getColumnList().stream())
    //            .flatMap(column -> column.getPropertyList().stream())
    //            .map(property -> property.getPieceDatetime())
    //            .filter(time -> time != null)
    //            .findFirst();
    //
    //        return optV1LocalDateTime.flatMap(v1LocalDateTime -> optV2LocalDateTime.map(v2LocalDateTime -> {
    //            return v1LocalDateTime.isAfter(v2LocalDateTime) ? v1 : v2;
    //        })).orElse(v1);
    //    };
    //
    //    // mapping piece to table part Function
    //    Function<Stream<DfDecoMapPiece>, Stream<DfDecoMapTablePart>> pieceToTablePartWithFiltering = pieceStream -> pieceStream
    //        // filtering latest comment version column
    //        .map(filteringLatestCommentVersion)
    //        // filtering latest decomment datetime for each column by author
    //        .collect(Collectors.toMap(piece -> piece.getAuthor(), Function.identity(), filteringLatestDecommentDatetime))
    //        .entrySet()
    //        .stream()
    //        .map(pieceEntry -> pieceEntry.getValue())
    //        .flatMap(piece -> piece.getTableList().stream());
    //
    //    //@formatter:off
    //    // extract map {key: table name value: {key: column name, value: piece}}
    //    Map<String, Map<String,List<DfDecoMapPiece>>> tableMap = pieces.stream()
    //        .collect(Collectors.groupingBy(
    //            piece -> piece.getTableList().get(0).getTableName(),
    //            Collectors.mapping(
    //                piece -> piece,
    //                Collectors.groupingBy(piece -> piece.getTableList().get(0).getColumnList().get(0).getColumnName())
    //            )
    //        )
    //    );
    //
    //     // Piece: Extract with latest comment version and latest decomment datetime for each column by author
    //     Stream<DfDecoMapTablePart> pieceStream = tableMap.entrySet()
    //         .stream()
    //         .map(tableEntry -> tableEntry.getValue())
    //         .map(columnMap -> columnMap.entrySet())
    //         .flatMap(columnEntry -> columnEntry.stream())
    //         .map(columnEntry -> columnEntry.getValue())
    //         .map(columnList -> columnList.stream())
    //         .map(pieceToTablePartWithFiltering).flatMap(tablePartStream -> tablePartStream);
    //     //@formatter:on
    //
    //    return Stream.concat(pickupStream, pieceStream);
    //}
    //
    //private Map<String, Long> generateAllColumnLatestCommentVersionMap(OptionalThing<DfDecoMapPickup> pickupOpt,
    //    List<DfDecoMapPiece> pieces) {
    //    Stream<DfDecoMapTablePart> pickupStream =
    //        pickupOpt.map(pickup -> pickup.getTableList()).map(tableList -> tableList.stream()).orElse(Stream.empty());
    //    Stream<DfDecoMapTablePart> pieceStream = pieces.stream().flatMap(dfDecoMapPiece -> dfDecoMapPiece.getTableList().stream());
    //    return Stream.concat(pickupStream, pieceStream)
    //        .flatMap(tablePart -> tablePart.getColumnList().stream())
    //        .collect(Collectors.toMap(// Convert Map
    //            column -> column.getColumnName(), // key: column name
    //            column -> column.getLatestCommentVersion(), // value: max comment version
    //            (v1, v2) -> Math.max(v1, v2)));
    //}

    // ===================================================================================
    //                                                                        MapList File
    //                                                                        ============
    protected MapListFile createMapListFile() {
        return new MapListFile();
    }
}
