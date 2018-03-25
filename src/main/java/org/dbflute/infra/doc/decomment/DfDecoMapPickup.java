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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbflute.helper.mapstring.MapListString;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapMappingPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;

/**
 * @author hakiba
 * @author cabos
 * @author jflute
 * @author deco
 */
public class DfDecoMapPickup {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_FORMAT_VERSION = "1.1";

    // -----------------------------------------------------
    //                                                   Key
    //                                                   ---
    public static final String DECO_MAP_KEY_DECOMAP = "decoMap";
    public static final String DECO_MAP_KEY_TABLE_LIST = "tableList";
    public static final String DECO_MAP_KEY_MAPPING_LIST = "mappingList";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // done cabos add pickupDatetime by jflute (2017/11/11)
    protected final String formatVersion;
    protected final LocalDateTime pickupDatetime;
    // -----------------------------------------------------
    //                                               decoMap
    //                                               -------
    protected final List<DfDecoMapTablePart> tableList;
    protected final List<DfDecoMapMappingPart> mappingList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDecoMapPickup(String formatVersion, List<DfDecoMapTablePart> tableList, List<DfDecoMapMappingPart> mappingList,
        LocalDateTime pickupDatetime) {
        this.formatVersion = formatVersion;
        this.pickupDatetime = pickupDatetime;
        this.tableList = tableList;
        this.mappingList = mappingList;
    }

    public DfDecoMapPickup(List<DfDecoMapTablePart> tableList, List<DfDecoMapMappingPart> mappingList, LocalDateTime pickupDatetime) {
        this.pickupDatetime = pickupDatetime;
        this.formatVersion = getFormatVersion();
        this.tableList = tableList;
        this.mappingList = mappingList;
    }

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    // map:{
    //     ; formatVersion = 1.1
    //     ; pickupDatetime = 2017-11-09T09:09:09.009
    //     ; decoMap = map:{
    //         ; mappingList = list:{ # if mapping conflict was occurred, mapping list size was not equals 0
    //             ; map:{
    //                 ; oldTableName = OLD_TABLE_NAME
    //                 : oldColumnName = OLD_COLUMN_NAME
    //                 ; targetType = COLUMN
    //                 ; newNameList = list:{
    //                     ; map:{
    //                         ; newTableName = NEW_TABLE_NAME
    //                         ; newColumnName = NEW_COLUMN_NAME
    //                         ; mappingCode = HAKI0000
    //                         ; mappingDatetime = 2018-03-25T11:45:14.191
    //                         ; mappingOwner = hakiba
    //                         ; previousMappingList = list:{ CABOS000 }
    //                     }
    //                     ; map:{
    //                         ; newTableName = NEW_OTHER_TABLE_NAME
    //                         ; newColumnName = NEW_OTHER_COLUMN_NAME
    //                         ; mappingCode = DECO0000
    //                         ; mappingDatetime = 2018-03-25T11:45:14.191
    //                         ; mappingOwner = deco
    //                         ; previousMappingList = list:{ CABOS000 }
    //                     }
    //                     ; map:{ ...
    //                 }
    //             }
    //         }
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
    //                         ; pieceGitBranch = develop
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
    //                                 ; pieceGitBranch = master
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
    public Map<String, Object> convertToMap() {
        final Map<String, List<Map<String, Object>>> decoMap = new LinkedHashMap<>();
        final List<Map<String, Object>> convertedTableList =
            this.tableList.stream().map(DfDecoMapTablePart::convertPickupMap).collect(Collectors.toList());
        decoMap.put(DECO_MAP_KEY_TABLE_LIST, convertedTableList);
        final List<Map<String, Object>> convertedMappingList =
            this.mappingList.stream().map(DfDecoMapMappingPart::convertToMap).collect(Collectors.toList());
        decoMap.put(DECO_MAP_KEY_MAPPING_LIST, convertedMappingList);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("formatVersion", formatVersion);
        map.put("pickupDatetime", pickupDatetime);
        map.put(DECO_MAP_KEY_DECOMAP, decoMap);
        return map;
    }

    // done hakiba move to before Accessor by jflute (2017/08/17)
    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    @Override
    public String toString() {
        return new MapListString().buildMapString(this.convertToMap());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getFormatVersion() {
        return formatVersion;
    }

    public LocalDateTime getPickupDatetime() {
        return pickupDatetime;
    }

    public List<DfDecoMapTablePart> getTableList() {
        return Collections.unmodifiableList(this.tableList);
    }
}
