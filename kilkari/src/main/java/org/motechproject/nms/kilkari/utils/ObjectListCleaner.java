package org.motechproject.nms.kilkari.utils;

import org.motechproject.nms.kilkari.contract.RchAnmAshaRecord;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;
import org.motechproject.nms.kilkari.contract.AnmAshaRecord;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;

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

    public static List<List<MotherRecord>> cleanMotherRecords(List<MotherRecord> motherRecords) {
        List<MotherRecord> rejectedRecords = new ArrayList<>();
        List<MotherRecord> acceptedRecords = new ArrayList<>();
        List<List<MotherRecord>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
        for (MotherRecord record : motherRecords) {
            if (motherPhoneMap.containsKey(record.getWhomPhoneNo())) {
                boolean identicalIdPhone = motherPhoneIdMap.get(record.getWhomPhoneNo()).equals(record.getIdNo());
                if (!identicalIdPhone) {
                    motherPhoneMap.put(record.getWhomPhoneNo(), motherPhoneMap.get(record.getWhomPhoneNo()) + 1);
                }

            } else {
                motherPhoneMap.put(record.getWhomPhoneNo(), 1);
                motherPhoneIdMap.put(record.getWhomPhoneNo(), record.getIdNo());
            }
        }
        for (MotherRecord record : motherRecords) {
            Integer count = motherPhoneMap.get(record.getWhomPhoneNo());
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

    public static List<List<RchMotherRecord>> cleanRchMotherRecords(List<RchMotherRecord> rchMotherRecords) {
        List<RchMotherRecord> rejectedRecords = new ArrayList<>();
        List<RchMotherRecord> acceptedRecords = new ArrayList<>();
        List<List<RchMotherRecord>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
        for (RchMotherRecord record : rchMotherRecords) {
            if (motherPhoneMap.containsKey(record.getMobileNo())) {
                boolean identicalIdPhone = motherPhoneIdMap.get(record.getMobileNo()).equals(record.getRegistrationNo());
                if (!identicalIdPhone) {
                    motherPhoneMap.put(record.getMobileNo(), motherPhoneMap.get(record.getMobileNo()) + 1);
                }
            } else {
                motherPhoneMap.put(record.getMobileNo(), 1);
                motherPhoneIdMap.put(record.getMobileNo(), record.getRegistrationNo());
            }
        }
        for (RchMotherRecord record : rchMotherRecords) {
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

    public static List<List<ChildRecord>> cleanChildRecords(List<ChildRecord> childRecords) {
        List<ChildRecord> rejectedRecords = new ArrayList<>();
        List<ChildRecord> acceptedRecords = new ArrayList<>();
        List<List<ChildRecord>> full = new ArrayList<>();
        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
        for (ChildRecord record : childRecords) {
            if (motherPhoneMap.containsKey(record.getWhomPhoneNo())) {
                boolean identicalIdContact = motherPhoneIdMap.get(record.getWhomPhoneNo()).equals(record.getIdNo());
                if (!identicalIdContact) {
                    motherPhoneMap.put(record.getWhomPhoneNo(), motherPhoneMap.get(record.getWhomPhoneNo()) + 1);
                }
            } else {
                motherPhoneMap.put(record.getWhomPhoneNo(), 1);
                motherPhoneIdMap.put(record.getWhomPhoneNo(), record.getIdNo());
            }
        }
        for (ChildRecord record : childRecords) {
            Integer count = motherPhoneMap.get(record.getWhomPhoneNo());
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
            String msisdn = (String) record.get(KilkariConstants.MOBILE_NO);
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

//    public static List<List<RchChildRecord>> oldcleanRchChildRecords(List<RchChildRecord> rchChildRecords) {
//        List<RchChildRecord> rejectedRecords = new ArrayList<>();
//        List<RchChildRecord> acceptedRecords = new ArrayList<>();
//        List<List<RchChildRecord>> full = new ArrayList<>();
//        HashMap<String, Integer> motherPhoneMap = new HashMap<>();
//        HashMap<String, String> motherPhoneIdMap = new HashMap<>();
//        for (RchChildRecord record : rchChildRecords) {
//            if (motherPhoneMap.containsKey(record.getMobileNo())) {
//                boolean identicalIdContact = motherPhoneIdMap.get(record.getMobileNo()).equals(record.getRegistrationNo());
//                if (!identicalIdContact) {
//                    motherPhoneMap.put(record.getMobileNo(), motherPhoneMap.get(record.getMobileNo()) + 1);
//                }
//            } else {
//                motherPhoneMap.put(record.getMobileNo(), 1);
//                motherPhoneIdMap.put(record.getMobileNo(), record.getRegistrationNo());
//            }
//        }
//        for (RchChildRecord record : rchChildRecords) {
//            Integer count = motherPhoneMap.get(record.getMobileNo());
//            if (count > 1) {
//                rejectedRecords.add(record);
//            } else {
//                acceptedRecords.add(record);
//            }
//        }
//        full.add(rejectedRecords);
//        full.add(acceptedRecords);
//        return full;
//    }

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
