package org.motechproject.nms.kilkari.utils;

import org.motechproject.nms.kilkari.contract.RchAnmAshaRecord;
import org.motechproject.nms.kilkari.contract.AnmAshaRecord;

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
            String msisdn = (String) record.get(KilkariConstants.MSISDN);
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
            String msisdn = (String) record.get(KilkariConstants.MSISDN);
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
            String msisdn = (String) record.get(KilkariConstants.MOBILE_NO);
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
            String msisdn = (String) record.get(KilkariConstants.MOBILE_NO);
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

    public static List<List<AnmAshaRecord>> cleanFlwRecords(List<AnmAshaRecord> anmAshaRecords) {
        List<AnmAshaRecord> rejectedRecords = new ArrayList<>();
        List<AnmAshaRecord> acceptedRecords = new ArrayList<>();
        List<List<AnmAshaRecord>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, Long> motherPhoneIdMap = new HashMap<>();
        for (AnmAshaRecord record : anmAshaRecords) {
            if (motherPhoneMap.containsKey(record.getContactNo())) {
                boolean identicalIdPhone = motherPhoneIdMap.get(record.getContactNo()).equals(record.getId());
                if (!identicalIdPhone) {
                    motherPhoneMap.put(record.getContactNo(), motherPhoneMap.get(record.getContactNo()) + 1);
                }
            } else {
                motherPhoneMap.put(record.getContactNo(), 1);
                motherPhoneIdMap.put(record.getContactNo(), record.getId());
            }
        }
        for (AnmAshaRecord record : anmAshaRecords) {
            Integer count = motherPhoneMap.get(record.getContactNo());
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

    public static List<List<RchAnmAshaRecord>> cleanRchFlwRecords(List<RchAnmAshaRecord> rchAnmAshaRecords) {
        List<RchAnmAshaRecord> rejectedRecords = new ArrayList<>();
        List<RchAnmAshaRecord> acceptedRecords = new ArrayList<>();
        List<List<RchAnmAshaRecord>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, Long> motherPhoneIdMap = new HashMap<>();
        for (RchAnmAshaRecord record : rchAnmAshaRecords) {
            if (motherPhoneMap.containsKey(record.getMobileNo())) {
                boolean identicalIdPhone = motherPhoneIdMap.get(record.getMobileNo()).equals(record.getGfId());
                if (!identicalIdPhone) {
                    motherPhoneMap.put(record.getMobileNo(), motherPhoneMap.get(record.getMobileNo()) + 1);
                }
            } else {
                motherPhoneMap.put(record.getMobileNo(), 1);
                motherPhoneIdMap.put(record.getMobileNo(), record.getGfId());

            }
        }
        for (RchAnmAshaRecord record : rchAnmAshaRecords) {
            Integer count = motherPhoneMap.get(record.getMobileNo());
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
