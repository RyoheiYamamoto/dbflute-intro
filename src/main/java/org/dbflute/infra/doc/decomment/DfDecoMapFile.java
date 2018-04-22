/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.infra.doc.decomment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dbflute.helper.HandyDate;
import org.dbflute.helper.mapstring.MapListFile;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.doc.decomment.exception.DfDecoMapFileReadFailureException;
import org.dbflute.infra.doc.decomment.exception.DfDecoMapFileWriteFailureException;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapMappingPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapPropertyPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;

// done cabos DfDecoMapFile by jflute (2017/07/27)
// done cabos add copyright in source file header like this class to classes of infra.doc.decomment by jflute (2017/11/11)

/**
 * @author cabos
 * @author hakiba
 * @author jflute
 * @author deco
 */
public class DfDecoMapFile {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // e.g. dbflute_maihamadb/scheme/decomment/
    private static final String BASE_DECOMMENT_DIR_PATH = "/schema/decomment/";
    // e.g. dbflute_maihamadb/scheme/decomment/piece/
    private static final String BASE_PICKUP_DIR_PATH = BASE_DECOMMENT_DIR_PATH + "piece/";
    // e.g. dbflute_maihamadb/scheme/decomment/pickup/decomment-pickup.dfmap
    private static final String BASE_PIECE_FILE_PATH = BASE_DECOMMENT_DIR_PATH + "pickup/decomment-pickup.dfmap";

    private static final Map<String, String> REPLACE_CHAR_MAP;

    static {
        // done cabos add spaces and replaceChar should be underscore? by jflute (2017/09/07)
        List<String> notAvailableCharList = Arrays.asList("/", "\\", "<", ">", "*", "?", "\"", "|", ":", ";", "\0", " ");
        String replaceChar = "_";
        REPLACE_CHAR_MAP = notAvailableCharList.stream().collect(Collectors.toMap(ch -> ch, ch -> replaceChar));
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final Supplier<LocalDateTime> currentDatetimeSupplier;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDecoMapFile(Supplier<LocalDateTime> currentDatetimeSupplier) {
        this.currentDatetimeSupplier = currentDatetimeSupplier;
    }

    // ===================================================================================
    //                                                                               Read
    //                                                                              ======
    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    // done yuto write e.g. (2017/11/11)
    // done cabos I just noticed that this should be readPieceList()... by jflute (2017/11/18)
    // done cabos write javadoc by jflute (2017/11/18)
    /**
     * Read all decomment piece map file in "clientDirPath/schema/decomment/piece/".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @return List of all decomment piece map (NotNull: If piece map file not exists, returns empty list)
     * @see DfDecoMapPiece#convertToMap()
     */
    public List<DfDecoMapPiece> readPieceList(String clientDirPath) {
        assertClientDirPath(clientDirPath);
        String pieceDirPath = buildPieceDirPath(clientDirPath);
        if (Files.notExists(Paths.get(pieceDirPath))) {
            return Collections.emptyList();
        }
        try {
            return Files.list(Paths.get(pieceDirPath))
                    .filter(path -> path.toString().endsWith(".dfmap"))
                    .filter(path -> path.toString().contains("-piece-"))
                    .map(path -> {
                        return doReadPiece(path);
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throwDecoMapReadFailureException(pieceDirPath, e);
            return Collections.emptyList(); // unreachable
        }
    }

    // done cabos DBFlute uses doRead...() style for internal process so please change it by jflute (2017/11/18)
    private DfDecoMapPiece doReadPiece(Path path) {
        final MapListFile mapListFile = createMapListFile();
        try {
            Map<String, Object> map = mapListFile.readMap(Files.newInputStream(path));
            return mappingToDecoMapPiece(map);
        } catch (RuntimeException | IOException e) {
            throwDecoMapReadFailureException(path.toString(), e);
            return null; // unreachable
        }
    }

    // done hakiba cast check by hakiba (2017/07/29)
    private DfDecoMapPiece mappingToDecoMapPiece(Map<String, Object> map) {
        String formatVersion = (String) map.get("formatVersion");
        String tableName = (String) map.get("tableName");
        String columnName = (String) map.get("columnName");
        DfDecoMapPieceTargetType targetType = DfDecoMapPieceTargetType.of(map.get("targetType")).get();
        String decomment = (String) map.get("decomment");
        String databaseComment = (String) map.get("databaseComment");
        Long commentVersion = Long.valueOf(map.get("commentVersion").toString());
        @SuppressWarnings("unchecked")
        List<String> authorList = (List<String>) map.get("authorList");
        String pieceCode = (String) map.get("pieceCode");
        LocalDateTime pieceDatetime = new HandyDate((String) map.get("pieceDatetime")).getLocalDateTime();
        String pieceOwner = (String) map.get("pieceOwner");
        String pieceGitBranch = (String) map.get("pieceGitBranch");
        @SuppressWarnings("unchecked")
        List<String> previousPieceList = (List<String>) map.get("previousPieceList");
        return new DfDecoMapPiece(formatVersion, tableName, columnName, targetType, decomment, databaseComment, commentVersion, authorList,
                pieceCode, pieceDatetime, pieceOwner, pieceGitBranch, previousPieceList);
    }

    // -----------------------------------------------------
    //                                                Pickup
    //                                                ------
    // done hakiba sub tag comment by jflute (2017/08/17)
    /**
     * Read decomment pickup map file at "clientDirPath/schema/decomment/pickup/decomment-pickup.dfmap".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @return pickup decomment map (NotNull: If pickup map file not exists, returns empty)
     * @see DfDecoMapPickup#convertToMap()
     */
    public OptionalThing<DfDecoMapPickup> readPickup(String clientDirPath) {
        assertClientDirPath(clientDirPath);
        String filePath = buildPickupFilePath(clientDirPath);
        if (Files.notExists(Paths.get(filePath))) {
            // done hakiba null pointer so use optional thing and stream empty by jflute (2017/10/05)
            return OptionalThing.empty();
        }
        return OptionalThing.ofNullable(doReadPickup(Paths.get(filePath)), () -> {});
    }

    private DfDecoMapPickup doReadPickup(Path path) {
        MapListFile mapListFile = createMapListFile();
        try {
            Map<String, Object> map = mapListFile.readMap(Files.newInputStream(path));
            return mappingToDecoMapPickup(map);
        } catch (RuntimeException | IOException e) {
            throwDecoMapReadFailureException(path.toString(), e);
            return null; // unreachable
        }
    }

    private DfDecoMapPickup mappingToDecoMapPickup(Map<String, Object> map) {
        String formatVersion = (String) map.getOrDefault("formatVersion", DfDecoMapPickup.DEFAULT_FORMAT_VERSION);
        LocalDateTime pickupDatetime = DfTypeUtil.toLocalDateTime(map.get("pickupDatetime"));

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> decoMap =
                (Map<String, List<Map<String, Object>>>) map.getOrDefault(DfDecoMapPickup.DECO_MAP_KEY_DECOMAP, Collections.emptyMap());
        if (decoMap.isEmpty()) {
            return new DfDecoMapPickup(formatVersion, Collections.emptyList(), pickupDatetime);
        }

        List<Map<String, Object>> tableMapList = decoMap.getOrDefault(DfDecoMapPickup.DECO_MAP_KEY_TABLE_LIST, Collections.emptyList());
        List<DfDecoMapTablePart> tableList = tableMapList.stream().map(tablePartMap -> {
            return new DfDecoMapTablePart(tablePartMap);
        }).collect(Collectors.toList());
        return new DfDecoMapPickup(formatVersion, tableList, pickupDatetime);
    }

    // -----------------------------------------------------
    //                                               Mapping
    //                                               -------
    /**
     * Read all decomment mapping map file in "clientDirPath/schema/decomment/piece/".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @return List of all decomment mapping map (NotNull: If mapping map file not exists, returns empty list)
     */
    public List<DfDecoMapMapping> readMappingList(String clientDirPath) {
        assertClientDirPath(clientDirPath);
        String pieceDirPath = buildPieceDirPath(clientDirPath);
        if (Files.notExists(Paths.get(pieceDirPath))) {
            return Collections.emptyList();
        }
        try {
            return Files.list(Paths.get(pieceDirPath))
                    .filter(path -> path.toString().endsWith(".dfmap"))
                    .filter(path -> path.toString().contains("-mapping-"))
                    .map(path -> {
                        return doReadMapping(path);
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throwDecoMapReadFailureException(pieceDirPath, e);
            return Collections.emptyList(); // unreachable
        }
    }

    // done cabos DBFlute uses doRead...() style for internal process so please change it by jflute (2017/11/18)
    private DfDecoMapMapping doReadMapping(Path path) {
        final MapListFile mapListFile = createMapListFile();
        try {
            Map<String, Object> map = mapListFile.readMap(Files.newInputStream(path));
            return mappingToDecoMapMapping(map);
        } catch (RuntimeException | IOException e) {
            throwDecoMapReadFailureException(path.toString(), e);
            return null; // unreachable
        }
    }

    // done hakiba cast check by hakiba (2017/07/29)
    private DfDecoMapMapping mappingToDecoMapMapping(Map<String, Object> map) {
        String formatVersion = (String) map.get("formatVersion");
        String oldTableName = (String) map.get("oldTableName");
        String oldColumnName = (String) map.get("oldColumnName");
        String newTableName = (String) map.get("newTableName");
        String newColumnName = (String) map.get("newColumnName");
        DfDecoMapPieceTargetType targetType = DfDecoMapPieceTargetType.of(map.get("targetType")).get();
        @SuppressWarnings("unchecked")
        List<String> authorList = (List<String>) map.get("authorList");
        String mappingCode = (String) map.get("mappingCode");
        LocalDateTime mappingDatetime = new HandyDate((String) map.get("mappingDatetime")).getLocalDateTime();
        String mappingOwner = (String) map.get("mappingOwner");
        @SuppressWarnings("unchecked")
        List<String> previousMappingList = (List<String>) map.get("previousMappingList");
        return new DfDecoMapMapping(formatVersion, oldTableName, oldColumnName, newTableName, newColumnName, targetType, authorList,
                mappingCode, mappingOwner, mappingDatetime, previousMappingList);
    }

    // -----------------------------------------------------
    //                                                Common
    //                                                ------
    protected void throwDecoMapReadFailureException(String path, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the deco-map file or directory.");
        br.addItem("path");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileReadFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    /**
     * Write single decomment piece map file at "clientDirPath/schema/decomment/piece".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @param decoMapPiece Decoment piece map (NotNull)
     */
    public void writePiece(String clientDirPath, DfDecoMapPiece decoMapPiece) {
        assertClientDirPath(clientDirPath);
        String pieceMapPath = buildPieceDirPath(clientDirPath) + buildPieceFileName(decoMapPiece);
        // done cabos remove 'df' from variable name by jflute (2017/08/10)
        // done cabos make and throw PhysicalCabosException (application exception) see ClientNotFoundException by jflute (2017/08/10)
        doWritePiece(pieceMapPath, decoMapPiece);
    }

    /**
     * Build piece file name for piece map file<br>
     * e.g. table decomment : decomment-piece-TABLE_NAME-20171224-143000-123-owner-ABCDEFG.dfmap <br>
     * e.g. column decomment : decomment-piece-TABLE_NAME-COLUMN_NAME-20171224-143000-123-owner-ABCDEFG.dfmap <br>
     * @param decoMapPiece Decoment piece map (NotNull)
     * @return piece file name
     */
    protected String buildPieceFileName(DfDecoMapPiece decoMapPiece) {
        String tableName = decoMapPiece.getTableName();
        String columnName = decoMapPiece.getColumnName();
        String owner = decoMapPiece.getPieceOwner();
        String pieceCode = decoMapPiece.getPieceCode();
        if (decoMapPiece.getTargetType() == DfDecoMapPieceTargetType.Table) {
            // e.g. decomment-piece-MEMBER-20171015-161718-199-jflute-HF7ELSE.dfmap
            return "decomment-piece-" + tableName + "-" + getCurrentDateStr() + "-" + filterOwner(owner) + "-" + pieceCode + ".dfmap";
        } else if (decoMapPiece.getTargetType() == DfDecoMapPieceTargetType.Column) {
            // e.g. decomment-piece-MEMBER-MEMBER_NAME-20171015-161718-199-jflute-HF7ELSE.dfmap
            return "decomment-piece-" + tableName + "-" + columnName + "-" + getCurrentDateStr() + "-" + filterOwner(owner) + "-"
                    + pieceCode + ".dfmap";
        }
        throwIllegalTargetTypeException(decoMapPiece);
        return null; // unreachable
    }

    protected String filterOwner(String owner) {
        return DfStringUtil.replaceBy(owner, REPLACE_CHAR_MAP);
    }

    protected String getCurrentDateStr() {
        return DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(getCurrentLocalDateTime());
    }

    private void doWritePiece(String pieceFilePath, DfDecoMapPiece decoMapPiece) {
        File pieceMapFile = new File(pieceFilePath);
        if (pieceMapFile.exists()) { // no way, but just in case
            pieceMapFile.delete(); // simply delete old file
        }
        createPieceMapFile(pieceMapFile);

        final Map<String, Object> decoMap = decoMapPiece.convertToMap();
        final MapListFile mapListFile = createMapListFile();
        createMapListFile(pieceFilePath, pieceMapFile, decoMap, mapListFile);
    }

    protected void createPieceMapFile(File pieceMapFile) {
        try {
            Files.createDirectories(Paths.get(pieceMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(pieceMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throwDecoMapWriteFailureException(pieceMapFile.getPath(), e);
        }
    }

    // -----------------------------------------------------
    //                                                Pickup
    //                                                ------
    public void writePickup(String clientDirPath, DfDecoMapPickup decoMapPickup) {
        assertClientDirPath(clientDirPath);
        doWritePickup(buildPickupFilePath(clientDirPath), decoMapPickup);
    }

    protected void doWritePickup(String pickupFilePath, DfDecoMapPickup decoMapPickup) {
        File pickupMapFile = new File(pickupFilePath);
        if (pickupMapFile.exists()) { // no way, but just in case
            pickupMapFile.delete(); // simply delete old file
        }
        createPickupMapFile(pickupMapFile);

        final Map<String, Object> decoMap = decoMapPickup.convertToMap();
        final MapListFile mapListFile = createMapListFile();
        createMapListFile(pickupFilePath, pickupMapFile, decoMap, mapListFile);
    }

    protected void createPickupMapFile(File pickupMapFile) {
        try {
            Files.createDirectories(Paths.get(pickupMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(pickupMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throwDecoMapWriteFailureException(pickupMapFile.getPath(), e);
        }
    }

    // -----------------------------------------------------
    //                                               Mapping
    //                                               -------
    public void writeMapping(String clientDirPath, DfDecoMapMapping decoMapMapping) {
        assertClientDirPath(clientDirPath);
        String mappingFilePath = buildPieceDirPath(clientDirPath) + buildMappingFileName(decoMapMapping);
        doWriteMapping(mappingFilePath, decoMapMapping);
    }

    protected String buildMappingFileName(DfDecoMapMapping decoMapMapping) {
        String oldTableName = decoMapMapping.getOldTableName();
        String oldColumnName = decoMapMapping.getOldColumnName();
        String newTableName = decoMapMapping.getNewTableName();
        String newColumnName = decoMapMapping.getNewColumnName();
        String owner = decoMapMapping.getMappingOwner();
        String mappingCode = decoMapMapping.getMappingCode();
        // TODO done cabos fix comment by jflute (2018/02/22)
        if (decoMapMapping.getTargetType() == DfDecoMapPieceTargetType.Table) {
            // e.g. decomment-mapping-OLD_TABLE-NEW_TABLE-20180318-142935-095-cabos-890e4e07.dfmap
            return "decomment-mapping-" + oldTableName + "-" + newTableName + "-" + getCurrentDateStr() + "-" + filterOwner(owner) + "-"
                    + mappingCode + ".dfmap";
        } else if (decoMapMapping.getTargetType() == DfDecoMapPieceTargetType.Column) {
            // e.g. decomment-mapping-OLD_TABLE-OLD_COLUMN-NEW_TABLE-NEW-20180318-143036-012-cabos-565d8dfa.dfmap
            return "decomment-mapping-" + oldTableName + "-" + oldColumnName + "-" + newTableName + "-" + newColumnName + "-"
                    + getCurrentDateStr() + "-" + filterOwner(owner) + "-" + mappingCode + ".dfmap";
        }
        throwIllegalTargetTypeException(decoMapMapping);
        return null; // unreachable
    }

    protected void doWriteMapping(String mappingFilePath, DfDecoMapMapping decoMapMapping) {
        File mappingMapFile = new File(mappingFilePath);
        if (mappingMapFile.exists()) { // no way, but just in case
            mappingMapFile.delete(); // simply delete old file
        }
        createMappingMapFile(mappingMapFile);

        final Map<String, Object> decoMap = decoMapMapping.convertToMap();
        final MapListFile mapListFile = createMapListFile();
        createMapListFile(mappingFilePath, mappingMapFile, decoMap, mapListFile);
    }

    protected void createMappingMapFile(File mappingMapFile) {
        try {
            Files.createDirectories(Paths.get(mappingMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(mappingMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throwDecoMapWriteFailureException(mappingMapFile.getPath(), e);
        }
    }

    // -----------------------------------------------------
    //                                                Common
    //                                                ------
    private void createMapListFile(String mappingFilePath, File mappingMapFile, Map<String, Object> decoMap, MapListFile mapListFile) {
        try (OutputStream ous = new FileOutputStream(mappingMapFile)) {
            try {
                mapListFile.writeMap(ous, decoMap);
            } catch (IOException e) {
                throwDecoMapWriteFailureException(mappingFilePath, decoMap, e);
            }
        } catch (IOException e) {
            throwDecoMapResourceReleaseFailureException(mappingFilePath, decoMap, e);
        }
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void throwDecoMapWriteFailureException(String path, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to create the deco-map file.");
        br.addItem("Path");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileWriteFailureException(msg, cause);
    }

    protected void throwDecoMapWriteFailureException(String path, Map<String, Object> decoMap, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to write the deco-map file.");
        br.addItem("Path");
        br.addElement(path);
        br.addItem("DecoMap");
        br.addElement(decoMap);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileWriteFailureException(msg, cause);
    }

    protected void throwDecoMapResourceReleaseFailureException(String path, Map<String, Object> decoMap, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Maybe... fail to execute \"outputStream.close()\".");
        br.addItem("Path");
        br.addElement(path);
        br.addItem("DecoMap");
        br.addElement(decoMap);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileWriteFailureException(msg, cause);
    }

    protected void throwIllegalTargetTypeException(DfDecoMapPiece decoMapPiece) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Deco map piece target type is illegal");
        br.addItem("Target type");
        br.addElement(decoMapPiece.getTargetType());
        br.addItem("DecoMapPiece");
        br.addElement(decoMapPiece);
        final String msg = br.buildExceptionMessage();
        throw new IllegalArgumentException(msg);
    }

    protected void throwIllegalTargetTypeException(DfDecoMapMapping decoMapMapping) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Deco map mapping target type is illegal");
        br.addItem("Target type");
        br.addElement(decoMapMapping.getTargetType());
        br.addItem("DecoMapPiece");
        br.addElement(decoMapMapping);
        final String msg = br.buildExceptionMessage();
        throw new IllegalArgumentException(msg);
    }

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    // done (by cabos) hakiba write unit test by jflute (2017/09/21)
    /**
     * merge piece map and pickup map with previous piece code and mapping code clue to go on.
     *
     * @param optPickup Decoment pickup map (NotNull: If pickup map file not exists, Empty allowed)
     * @param roughPieceList Rough Decoment piece map (NotNull: If piece map file not exists, Empty allowed)
     * @param roughMappingList Rough Decomment mapping map (NotNull: If mapping map file not exists, Empty allowed)
     * @return pickup decomment map (NotNull)
     */
    public DfDecoMapPickup merge(OptionalThing<DfDecoMapPickup> optPickup, List<DfDecoMapPiece> roughPieceList,
            List<DfDecoMapMapping> roughMappingList) {

        final List<DfDecoMapTablePart> roughTablePartList = convertToRoughTableList(optPickup, roughPieceList);

        final List<DfDecoMapMapping> allMappingList = extractAllMappingList(optPickup, roughMappingList);
        final List<DfDecoMapMapping> filteredMappingPartList = filterMergedMapping(allMappingList);
        final Map<String, List<DfDecoMapMappingPart>> mappingPartListMap = convertToMappingPartListMap(filteredMappingPartList);

        // ============================================================ prepare
        // prepare filtering for already merged property
        final Set<String> pieceCodeSet = extractAllMergedPieceCode(roughTablePartList);

        // ============================================================ define
        // define mapping correct table or column name
        final Stream<DfDecoMapTablePart> correctNameTableStream =
                defineMappingToCorrectName(roughTablePartList.stream(), mappingPartListMap);
        // define merging
        final Stream<DfDecoMapTablePart> mergedTableStream = defineMerging(correctNameTableStream);
        // define filter already merged property
        final Stream<DfDecoMapTablePart> filteredTableStream = defineFilteringMergedProperty(mergedTableStream, pieceCodeSet);

        // ============================================================ do all defined process for merge
        final List<DfDecoMapTablePart> tableList = filteredTableStream.collect(Collectors.toList());

        // ============================================================ create new pickup object
        return new DfDecoMapPickup(tableList, getCurrentLocalDateTime());
    }

    // -----------------------------------------------------
    //                                 convert to rough list
    //                                 ---------------------
    private List<DfDecoMapMapping> extractAllMappingList(OptionalThing<DfDecoMapPickup> optPickup,
            List<DfDecoMapMapping> roughMappingList) {
        Stream<DfDecoMapMapping> pickupMappingStream = optPickup.map(pickup -> {
            Stream<DfDecoMapMapping> tableMappingStream = pickup.getTableList().stream().flatMap(table -> {
                return table.getMappingList().stream().map(mapping -> {
                    return new DfDecoMapMapping(table.getTableName(), null, DfDecoMapPieceTargetType.Table, mapping);
                });
            });
            Stream<DfDecoMapMapping> columnMappingStream = pickup.getTableList().stream().flatMap(table -> {
                return table.getColumnList().stream().flatMap(column -> {
                    return column.getMappingList().stream().map(mapping -> {
                        return new DfDecoMapMapping(table.getTableName(), column.getColumnName(), DfDecoMapPieceTargetType.Column, mapping);
                    });
                });
            });
            return Stream.concat(tableMappingStream, columnMappingStream);
        }).orElse(Stream.empty());
        return Stream.concat(pickupMappingStream, roughMappingList.stream()).collect(Collectors.toList());
    }

    private List<DfDecoMapTablePart> convertToRoughTableList(OptionalThing<DfDecoMapPickup> optPickup,
            List<DfDecoMapPiece> roughPieceList) {
        final Stream<DfDecoMapTablePart> pickupTableStream = defineConvertPickupToTable(optPickup);
        final Stream<DfDecoMapTablePart> pieceTableStream = defineConvertPieceListToTable(roughPieceList);
        return Stream.concat(pickupTableStream, pieceTableStream).collect(Collectors.toList());
    }

    private Stream<DfDecoMapTablePart> defineConvertPickupToTable(OptionalThing<DfDecoMapPickup> pickupOpt) {
        return pickupOpt.map(pickup -> pickup.getTableList().stream()).orElse(Stream.empty());
    }

    private Stream<DfDecoMapTablePart> defineConvertPieceListToTable(List<DfDecoMapPiece> pieceList) {
        return pieceList.stream().map(piece -> {
            return convertPieceToTablePart(piece);
        });
    }

    private DfDecoMapTablePart convertPieceToTablePart(DfDecoMapPiece piece) {
        final DfDecoMapPropertyPart property = convertToProperty(piece);
        final String tableName = piece.getTableName();
        final List<DfDecoMapPropertyPart> tablePropertyList =
                piece.isTargetTypeTable() ? Collections.singletonList(property) : Collections.emptyList();
        final List<DfDecoMapColumnPart> columnList =
                piece.isTargetTypeColumn() ? Collections.singletonList(convertPieceToColumnPart(piece, property)) : Collections.emptyList();
        return new DfDecoMapTablePart(tableName, Collections.emptyList(), tablePropertyList, columnList);
    }

    private DfDecoMapColumnPart convertPieceToColumnPart(DfDecoMapPiece piece, DfDecoMapPropertyPart property) {
        final String columnName = piece.getColumnName();
        final List<DfDecoMapPropertyPart> columnPropertyList = Collections.singletonList(property);
        return new DfDecoMapColumnPart(columnName, Collections.emptyList(), columnPropertyList);
    }

    private Map<String, List<DfDecoMapMappingPart>> convertToMappingPartListMap(List<DfDecoMapMapping> mappingList) {
        Map<String, List<DfDecoMapMappingPart>> map = new LinkedHashMap<>();
        mappingList.stream()
                .collect(Collectors.groupingBy(mapping -> convertToOldStateHash(mapping)))
                .forEach((oldStateHash, sameOldStateMappingList) -> {
                    final List<DfDecoMapMappingPart> newStateList = sameOldStateMappingList.stream()
                            .collect(Collectors.toMap(mapping -> convertToNewStateHash(mapping), mapping -> mapping, (m1, m2) -> {
                                final Set<String> authorSet =
                                        Stream.concat(m1.getAuthorList().stream(), m2.getAuthorList().stream()).collect(Collectors.toSet());
                                final Set<String> previousMappingSet =
                                        Stream.concat(m1.getPreviousMappingList().stream(), m2.getPreviousMappingList().stream())
                                                .collect(Collectors.toSet());
                                previousMappingSet.add(m2.mappingCode);
                                return new DfDecoMapMapping(DfDecoMapMapping.DEFAULT_FORMAT_VERSION, m1.getOldTableName(),
                                        m1.getOldColumnName(), m1.getNewTableName(), m1.getNewColumnName(), m1.getTargetType(),
                                        new ArrayList<>(authorSet), m1.getMappingCode(), m1.getMappingOwner(), m1.getMappingDatetime(),
                                        new ArrayList<>(previousMappingSet));
                            }))
                            .values()
                            .stream()
                            .map(mapping -> new DfDecoMapMappingPart(mapping))
                            .collect(Collectors.toList());
                    map.put(oldStateHash, newStateList);
                });
        return map;
    }

    private String convertToOldStateHash(DfDecoMapMapping mapping) {
        return createToStateHash(mapping.getOldTableName(), mapping.getOldColumnName(), mapping.getTargetType());
    }

    private String convertToNewStateHash(DfDecoMapMapping mapping) {
        return createToStateHash(mapping.getNewTableName(), mapping.getNewColumnName(), mapping.getTargetType());
    }

    private String convertToTableStateHash(DfDecoMapTablePart tablePart) {
        return createToStateHash(tablePart.getTableName(), null, DfDecoMapPieceTargetType.Table);
    }

    private String convertToColumnStateHash(DfDecoMapTablePart tablePart, DfDecoMapColumnPart columnPart) {
        return createToStateHash(tablePart.getTableName(), columnPart.getColumnName(), DfDecoMapPieceTargetType.Column);
    }

    private String createToStateHash(String tableName, String columnName, DfDecoMapPieceTargetType type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(tableName);
        sb.append(":").append(DfStringUtil.isEmpty(columnName) ? "" : columnName);
        sb.append(":").append(type);
        return Integer.toHexString(sb.toString().hashCode());
    }

    // -----------------------------------------------------
    //                                               prepare
    //                                               -------
    private Set<String> extractAllMergedPieceCode(List<DfDecoMapTablePart> tableList) {
        return tableList.stream().flatMap(table -> {
            Stream<String> previousTablePieceStream =
                    table.getPropertyList().stream().flatMap(property -> property.getPreviousPieceList().stream());
            Stream<String> previousColumnPieceStream = table.getColumnList()
                    .stream()
                    .flatMap(column -> column.getPropertyList().stream())
                    .flatMap(property -> property.getPreviousPieceList().stream());
            return Stream.concat(previousTablePieceStream, previousColumnPieceStream);
        }).collect(Collectors.toSet());
    }

    private List<DfDecoMapMapping> filterMergedMapping(List<DfDecoMapMapping> mappingList) {
        Set<String> mappingCodeSet =
                mappingList.stream().flatMap(mapping -> mapping.getPreviousMappingList().stream()).collect(Collectors.toSet());
        return mappingList.stream().filter(mapping -> {
            return !mappingCodeSet.contains(mapping.getMappingCode());
        }).collect(Collectors.toList());
    }

    // -----------------------------------------------------
    //                                                Define
    //                                                ------
    protected Stream<DfDecoMapTablePart> defineMerging(Stream<DfDecoMapTablePart> tableStream) {
        return tableStream.collect(Collectors.groupingBy(table -> table.getTableName())).entrySet().stream().map(tableEntry -> {
            final String tableName = tableEntry.getKey();
            final List<DfDecoMapTablePart> sameTableNameList = tableEntry.getValue();
            final List<DfDecoMapPropertyPart> tablePropertyList =
                    tableEntry.getValue().stream().flatMap(table -> table.getPropertyList().stream()).collect(Collectors.toList());
            final List<DfDecoMapMappingPart> tableMappingPartList =
                    tableEntry.getValue().stream().flatMap(table -> table.getMappingList().stream()).collect(Collectors.toList());
            final List<DfDecoMapColumnPart> columnList = sameTableNameList.stream()
                    .flatMap(table -> table.getColumnList().stream())
                    .collect(Collectors.groupingBy(column -> column.getColumnName()))
                    .entrySet()
                    .stream()
                    .map(columnEntry -> {
                        final String columnName = columnEntry.getKey();
                        final List<DfDecoMapMappingPart> columnMappingPartList = columnEntry.getValue()
                                .stream()
                                .flatMap(column -> column.getMappingList().stream())
                                .collect(Collectors.toList());
                        final List<DfDecoMapPropertyPart> columnPropertyList = columnEntry.getValue()
                                .stream()
                                .flatMap(column -> column.getPropertyList().stream())
                                .collect(Collectors.toList());
                        return new DfDecoMapColumnPart(columnName, columnMappingPartList, columnPropertyList);
                    })
                    .collect(Collectors.toList());
            return new DfDecoMapTablePart(tableName, tableMappingPartList, tablePropertyList, columnList);
        });
    }

    private DfDecoMapPropertyPart convertToProperty(DfDecoMapPiece piece) {
        DfDecoMapPropertyPart property =
                new DfDecoMapPropertyPart(piece.getDecomment(), piece.getDatabaseComment(), piece.getCommentVersion(),
                        piece.getAuthorList(), piece.getPieceCode(), piece.getPieceDatetime(), piece.getPieceOwner(),
                        piece.getPieceGitBranch(), piece.getPreviousPieceList());
        return property;
    }

    private Stream<DfDecoMapTablePart> defineMappingToCorrectName(Stream<DfDecoMapTablePart> tableStream,
            Map<String, List<DfDecoMapMappingPart>> mappingPartListMap) {
        return tableStream.map(table -> {
            return mappingToTableIfNameChanged(table, mappingPartListMap);
        });
    }

    private DfDecoMapTablePart mappingToTableIfNameChanged(DfDecoMapTablePart tablePart,
            Map<String, List<DfDecoMapMappingPart>> mappingPartListMap) {
        final List<DfDecoMapColumnPart> columnPartList = tablePart.getColumnList().stream().map(columnPart -> {
            return mappingToColumnIfNameChanged(tablePart, columnPart, mappingPartListMap);
        }).collect(Collectors.toList());
        final List<DfDecoMapMappingPart> mappingPartList =
                mappingPartListMap.getOrDefault(convertToTableStateHash(tablePart), Collections.emptyList());
        if (mappingPartList.size() == 1) {
            final DfDecoMapMappingPart mappingPart = mappingPartList.get(0);
            return new DfDecoMapTablePart(mappingPart.getNewTableName(), Collections.emptyList(), tablePart.getPropertyList(),
                    columnPartList);
        } else {
            return new DfDecoMapTablePart(tablePart.getTableName(), mappingPartList, tablePart.getPropertyList(), columnPartList);
        }
    }

    private DfDecoMapColumnPart mappingToColumnIfNameChanged(DfDecoMapTablePart tablePart, DfDecoMapColumnPart columnPart,
            Map<String, List<DfDecoMapMappingPart>> mappingPartListMap) {
        final List<DfDecoMapMappingPart> mappingPartList =
                mappingPartListMap.getOrDefault(convertToColumnStateHash(tablePart, columnPart), Collections.emptyList());
        if (mappingPartList.size() == 1) {
            final DfDecoMapMappingPart mappingPart = mappingPartList.get(0);
            return new DfDecoMapColumnPart(mappingPart.getNewColumnName(), Collections.emptyList(), columnPart.getPropertyList());
        } else {
            return new DfDecoMapColumnPart(columnPart.getColumnName(), mappingPartList, columnPart.getPropertyList());
        }
    }

    private Stream<DfDecoMapTablePart> defineFilteringMergedProperty(Stream<DfDecoMapTablePart> tableStream, Set<String> pieceCodeSet) {
        return tableStream.map(table -> {
            final String tableName = table.getTableName();
            final List<DfDecoMapMappingPart> tableMappingPartList = table.getMappingList();
            final List<DfDecoMapPropertyPart> tablePropertyList = table.getPropertyList()
                    .stream()
                    .filter(property -> !pieceCodeSet.contains(property.getPieceCode()))
                    .collect(Collectors.toList());
            final List<DfDecoMapColumnPart> columnList = table.getColumnList().stream().map(column -> {
                final String columnName = column.getColumnName();
                final List<DfDecoMapMappingPart> columnMappingPartList = column.getMappingList();
                final List<DfDecoMapPropertyPart> columnPropertyList = column.getPropertyList()
                        .stream()
                        .filter(property -> !pieceCodeSet.contains(property.getPieceCode()))
                        .collect(Collectors.toList());
                return new DfDecoMapColumnPart(columnName, columnMappingPartList, columnPropertyList);
            }).collect(Collectors.toList());
            return new DfDecoMapTablePart(tableName, tableMappingPartList, tablePropertyList, columnList);
        });
    }

    // ===================================================================================
    //                                                                              Delete
    //                                                                              ======
    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    public void deletePiece(String clientPath) {
        String pieceDirPath = buildPieceDirPath(clientPath);
        doDeletePiece(pieceDirPath);
    }

    private void doDeletePiece(String piecePath) {
        File pieceDir = new File(piecePath);
        if (pieceDir.isDirectory()) {
            for (File pieceFile : pieceDir.listFiles()) {
                if (pieceFile.isFile()) {
                    pieceFile.delete();
                } else {
                    doDeletePiece(pieceFile.getAbsolutePath());
                }
            }
        }
    }

    // ===================================================================================
    //                                                                        MapList File
    //                                                                        ============
    protected MapListFile createMapListFile() {
        return new MapListFile();
    }

    // ===================================================================================
    //                                                                           File Path
    //                                                                           =========
    protected String buildPieceDirPath(String clientDirPath) {
        return clientDirPath + BASE_PICKUP_DIR_PATH;
    }

    protected String buildPickupFilePath(String clientDirPath) {
        return clientDirPath + BASE_PIECE_FILE_PATH;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertClientDirPath(String clientDirPath) {
        if (clientDirPath == null || clientDirPath.trim().length() == 0) {
            String msg = "The argument 'clientDirPath' should not be null or empty: " + clientDirPath;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                          Time Logic
    //                                                                          ==========
    protected LocalDateTime getCurrentLocalDateTime() {
        // TODO done cabos use callback by jflute (2018/02/22)
        return this.currentDatetimeSupplier.get();
    }
}
