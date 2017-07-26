package org.motechproject.nms.rejectionhandler.utils;

import org.motechproject.nms.mcts.contract.AnmAshaRecord;
import org.motechproject.nms.mcts.contract.ChildRecord;
import org.motechproject.nms.mcts.contract.MotherRecord;
import org.motechproject.nms.rch.contract.RchAnmAshaRecord;
import org.motechproject.nms.rch.contract.RchChildRecord;
import org.motechproject.nms.rch.contract.RchMotherRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by beehyv on 24/7/17.
 */
public class ObjectListCleaner {

    public static List<List<MotherRecord>> cleanMotherRecords(List<MotherRecord> motherRecords) {
        List<MotherRecord> rejectedRecords = new ArrayList<>();
        List<MotherRecord> acceptedRecords = new ArrayList<>();
        List<List<MotherRecord>> full = new ArrayList<>();
        HashMap<String,Integer> motherPhoneMap = new HashMap<>();
        for(MotherRecord record : motherRecords){
            if(motherPhoneMap.containsKey(record.getWhomPhoneNo())){
                motherPhoneMap.put(record.getWhomPhoneNo(),motherPhoneMap.get(record.getWhomPhoneNo())+1);
            }else{
                motherPhoneMap.put(record.getWhomPhoneNo(),1);
            }
        }
        for(MotherRecord record : motherRecords){
            Integer count = motherPhoneMap.get(record.getWhomPhoneNo());
            if(count>1){
                rejectedRecords.add(record);
            }else{
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
        HashMap<String,Integer> motherPhoneMap = new HashMap<>();
        for(RchMotherRecord record : rchMotherRecords){
            if(motherPhoneMap.containsKey(record.getMobileNo())){
                motherPhoneMap.put(record.getMobileNo(),motherPhoneMap.get(record.getMobileNo())+1);
            }else{
                motherPhoneMap.put(record.getMobileNo(),1);
            }
        }
        for(RchMotherRecord record : rchMotherRecords){
            Integer count = motherPhoneMap.get(record.getMobileNo());
            if(count>1){
                rejectedRecords.add(record);
            }else{
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
        HashMap<String,Integer> motherPhoneMap = new HashMap<>();
        for(ChildRecord record : childRecords){
            if(motherPhoneMap.containsKey(record.getWhomPhoneNo())){
                motherPhoneMap.put(record.getWhomPhoneNo(),motherPhoneMap.get(record.getWhomPhoneNo())+1);
            }else{
                motherPhoneMap.put(record.getWhomPhoneNo(),1);
            }
        }
        for(ChildRecord record : childRecords){
            Integer count = motherPhoneMap.get(record.getWhomPhoneNo());
            if(count>1){
                rejectedRecords.add(record);
            }else{
                acceptedRecords.add(record);
            }
        }
        full.add(rejectedRecords);
        full.add(acceptedRecords);
        return full;
    }

    public static List<List<RchChildRecord>> cleanRchChildRecords(List<RchChildRecord> rchChildRecords) {
        List<RchChildRecord> rejectedRecords = new ArrayList<>();
        List<RchChildRecord> acceptedRecords = new ArrayList<>();
        List<List<RchChildRecord>> full = new ArrayList<>();
        HashMap<String,Integer> motherPhoneMap = new HashMap<>();
        for(RchChildRecord record : rchChildRecords){
            if(motherPhoneMap.containsKey(record.getMobileNo())){
                motherPhoneMap.put(record.getMobileNo(),motherPhoneMap.get(record.getMobileNo())+1);
            }else{
                motherPhoneMap.put(record.getMobileNo(),1);
            }
        }
        for(RchChildRecord record : rchChildRecords){
            Integer count = motherPhoneMap.get(record.getMobileNo());
            if(count>1){
                rejectedRecords.add(record);
            }else{
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
        HashMap<String,Integer> motherPhoneMap = new HashMap<>();
        for(AnmAshaRecord record : anmAshaRecords){
            if(motherPhoneMap.containsKey(record.getContactNo())){
                motherPhoneMap.put(record.getContactNo(),motherPhoneMap.get(record.getContactNo())+1);
            }else{
                motherPhoneMap.put(record.getContactNo(),1);
            }
        }
        for(AnmAshaRecord record : anmAshaRecords){
            Integer count = motherPhoneMap.get(record.getContactNo());
            if(count>1){
                rejectedRecords.add(record);
            }else{
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
        HashMap<String,Integer> motherPhoneMap = new HashMap<>();
        for(RchAnmAshaRecord record : rchAnmAshaRecords){
            if(motherPhoneMap.containsKey(record.getMobileNo())){
                motherPhoneMap.put(record.getMobileNo(),motherPhoneMap.get(record.getMobileNo())+1);
            }else{
                motherPhoneMap.put(record.getMobileNo(),1);
            }
        }
        for(RchAnmAshaRecord record : rchAnmAshaRecords){
            Integer count = motherPhoneMap.get(record.getMobileNo());
            if(count>1){
                rejectedRecords.add(record);
            }else{
                acceptedRecords.add(record);
            }
        }
        full.add(rejectedRecords);
        full.add(acceptedRecords);
        return full;
    }
}
