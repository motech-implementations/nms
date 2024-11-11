package org.motechproject.nms.kilkari.contract;

public class EntryTypeStrategy {
    public Integer determineEntryType(Object entryType) {
        if (entryType instanceof Integer) {
            return handleIntegerType((Integer) entryType);
        } else if (entryType instanceof String) {
            return handleStringType((String) entryType);
        } else {
            return handleDefaultType();
        }
    }

    private Integer handleIntegerType(Integer entryType) {
        return entryType;
    }

    private Integer handleStringType(String entryType) {
        String entryTypeStr = entryType.toUpperCase();
        return "ACTIVE".equals(entryTypeStr) || entryTypeStr.isEmpty() ? 1 : 9;
    }

    private Integer handleDefaultType() {
        return 1;
    }
}
