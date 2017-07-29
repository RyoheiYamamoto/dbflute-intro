package org.dbflute.intro.app.model.document.decomment;

import java.util.List;

import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapTablePart;

/**
 * @author hakiba
 * @author cabos
 */
public class DfDecoMapPickup {

    protected String fileName;
    protected String formatVersion;
    protected List<DfDecoMapTablePart> decoMap;

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFormatVersion() {
        return formatVersion;
    }
    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }
    public List<DfDecoMapTablePart> getDecoMap() {
        return decoMap;
    }
    public void setDecoMap(List<DfDecoMapTablePart> decoMap) {
        this.decoMap = decoMap;
    }
}
