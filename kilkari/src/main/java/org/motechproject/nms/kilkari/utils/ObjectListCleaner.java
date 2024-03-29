package org.motechproject.nms.kilkari.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by beehyv on 24/7/17.
 */
public final class ObjectListCleaner {

    private ObjectListCleaner() {
    }

    public static List<List<Map<String, Object>>> cleanMotherRecords(List<Map<String, Object>> motherRecords) {
        List<Map<String, Object>> rejectedRecords = new ArrayList<>();
        List<Map<String, Object>> acceptedRecords = new ArrayList<>();
        List<List<Map<String, Object>>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
        for (Map<String, Object> record : motherRecords) {
            String msisdn = record.get(KilkariConstants.MSISDN) == null ? null : record.get(KilkariConstants.MSISDN).toString();
            String mctsId = (String) record.get(KilkariConstants.BENEFICIARY_ID);
            if (motherPhoneMap.containsKey(msisdn)) {
                boolean identicalIdPhone = motherPhoneIdMap.get(msisdn).equals(mctsId);
                if (!identicalIdPhone) {
                    motherPhoneMap.put(msisdn, motherPhoneMap.get(msisdn) + 1);
                }

            } else {
                motherPhoneMap.put(msisdn, 1);
                motherPhoneIdMap.put(msisdn, mctsId);
            }
        }
        for (Map<String, Object> record : motherRecords) {
            String msisdn = record.get(KilkariConstants.MSISDN) == null ? null : record.get(KilkariConstants.MSISDN).toString();
            Integer count = motherPhoneMap.get(msisdn);
            if (count > 1) {
                rejectedRecords.add(record);
            } else {
                acceptedRecords.add(record);
            }
        }
        full.add(rejectedRecords);
        full.add(acceptedRecords);
        return full;
    }

    public static List<List<Map<String, Object>>> cleanRchMotherRecords(List<Map<String, Object>> rchMotherRecords) {
        List<Map<String, Object>> rejectedRecords = new ArrayList<>();
        List<Map<String, Object>> acceptedRecords = new ArrayList<>();
        List<List<Map<String, Object>>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
        for (Map<String, Object> record : rchMotherRecords) {
            String msisdn = record.get(KilkariConstants.MOBILE_NO) == null ? null : record.get(KilkariConstants.MOBILE_NO).toString();
            String rchId = (String) record.get(KilkariConstants.RCH_ID);
            if (motherPhoneMap.containsKey(msisdn)) {
                boolean identicalIdPhone = motherPhoneIdMap.get(msisdn).equals(rchId);
                if (!identicalIdPhone) {
                    motherPhoneMap.put(msisdn, motherPhoneMap.get(msisdn) + 1);
                }
            } else {
                motherPhoneMap.put(msisdn, 1);
                motherPhoneIdMap.put(msisdn, rchId);
            }
        }
        for (Map<String, Object> record : rchMotherRecords) {
            String msisdn = record.get(KilkariConstants.MOBILE_NO) == null ? null : record.get(KilkariConstants.MOBILE_NO).toString();
            Integer count = motherPhoneMap.get(msisdn);
            if (count > 1) {
                rejectedRecords.add(record);
            } else {
                acceptedRecords.add(record);
            }
        }
        full.add(rejectedRecords);
        full.add(acceptedRecords);
        return full;
    }

    public static List<List<Map<String, Object>>> cleanChildRecords(List<Map<String, Object>> childRecords) {
        List<Map<String, Object>> rejectedRecords = new ArrayList<>();
        List<Map<String, Object>> acceptedRecords = new ArrayList<>();
        List<List<Map<String, Object>>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
        for (Map<String, Object> record : childRecords) {
            String msisdn = record.get(KilkariConstants.MSISDN) == null ? null : record.get(KilkariConstants.MSISDN).toString();
            String mctsId = (String) record.get(KilkariConstants.BENEFICIARY_ID);
            if (motherPhoneMap.containsKey(msisdn)) {
                boolean identicalIdContact = motherPhoneIdMap.get(msisdn).equals(mctsId);
                if (!identicalIdContact) {
                    motherPhoneMap.put(msisdn, motherPhoneMap.get(msisdn) + 1);
                }
            } else {
                motherPhoneMap.put(msisdn, 1);
                motherPhoneIdMap.put(msisdn, mctsId);
            }
        }
        for (Map<String, Object> record : childRecords) {
            String msisdn = record.get(KilkariConstants.MSISDN) == null ? null : record.get(KilkariConstants.MSISDN).toString();
            Integer count = motherPhoneMap.get(msisdn);
            if (count > 1) {
                rejectedRecords.add(record);
            } else {
                acceptedRecords.add(record);
            }
        }
        full.add(rejectedRecords);
        full.add(acceptedRecords);
        return full;
    }

    public static List<List<Map<String, Object>>> cleanRchChildRecords(List<Map<String, Object>> rchChildRecords) {
        List<Map<String, Object>> rejectedRecords = new ArrayList<>();
        List<Map<String, Object>> acceptedRecords = new ArrayList<>();
        List<List<Map<String, Object>>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
        for (Map<String, Object> record : rchChildRecords) {
            String msisdn = record.get(KilkariConstants.MOBILE_NO) == null ? null : record.get(KilkariConstants.MOBILE_NO).toString();
            String rchId = (String) record.get(KilkariConstants.RCH_ID);
            if (motherPhoneMap.containsKey(msisdn)) {
                boolean identicalIdContact = motherPhoneIdMap.get(msisdn).equals(rchId);
                if (!identicalIdContact) {
                    motherPhoneMap.put(msisdn, motherPhoneMap.get(msisdn) + 1);
                }
            } else {
                motherPhoneMap.put(msisdn, 1);
                motherPhoneIdMap.put(msisdn, rchId);
            }
        }
        for (Map<String, Object> record : rchChildRecords) {
            String msisdn = record.get(KilkariConstants.MOBILE_NO) == null ? null : record.get(KilkariConstants.MOBILE_NO).toString();
            Integer count = motherPhoneMap.get(msisdn);
            if (count > 1) {
                rejectedRecords.add(record);
            } else {
                acceptedRecords.add(record);
            }
        }
        full.add(rejectedRecords);
        full.add(acceptedRecords);
        return full;
    }

}
