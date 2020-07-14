/** Integration tests for Location Rejections  */
package org.motechproject.nms.testing.it.region;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.repository.*;
import org.motechproject.nms.region.service.*;
import org.motechproject.nms.rejectionhandler.repository.*;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.nms.tracking.repository.ChangeLogDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LocationRejectionServiceBundleIT extends BasePaxIT {
    @Inject
    private TestingService testingService;

    @Inject
    private StateService stateService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictService districtService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private DistrictRejectionDataService districtRejectionDataService;

    @Inject
    private TalukaService talukaService;

    @Inject
    private TalukaDataService talukaDataService;

    @Inject
    private TalukaRejectionDataService talukaRejectionDataService;

    @Inject
    private HealthBlockService healthBlockService;

    @Inject
    private HealthBlockDataService healthBlockDataService;

    @Inject
    private HealthBlockRejectionDataService healthBlockRejectionDataService;

    @Inject
    private HealthFacilityTypeDataService healthFacilityTypeDataService;

    @Inject
    private HealthFacilityService healthFacilityService;

    @Inject
    private HealthFacilityDataService healthFacilityDataService;

    @Inject
    private HealthSubFacilityRejectionDataService healthFacilityRejectionDataService;

    @Inject
    private VillageService villageService;

    @Inject
    private VillageDataService villageDataService;

    @Inject
    private VillageRejectionDataService villageRejectionDataService;

    @Inject
    private ChangeLogDataService changeLogDataService;

    @Inject
    private HealthSubFacilityService healthSubFacilityService;

    @Inject
    private HealthSubFacilityDataService healthSubFacilityDataService;

    @Inject
    private HealthSubFacilityRejectionDataService healthSubFacilityRejectionDataService;

    @Before
    public void setUp() {
        testingService.clearDatabase();
        changeLogDataService.deleteAll();
    }
    private State createState() {
        State state = stateDataService.create(new State("Delhi", 1234L));
        return state;
    }

    private Taluka createTaluka(District district, String talukaCode) {
        Taluka taluka = new Taluka();
        taluka.setDistrict(district);
        taluka.setCode(talukaCode);
        taluka.setName("taluka name");
        taluka.setRegionalName("taluka regional name");
        talukaDataService.create(taluka);
        return taluka;
    }

    private District createDistrict(State state, Long distCode) {
        District district = new District();
        district.setCode(distCode);
        district.setName("district name");
        district.setState(state);
        district.setRegionalName("district regional name");
        district = districtDataService.create(district);
        return district;
    }

    private HealthBlock createHealthBlock(District district, Long HBCode,Long talukaIdOid) {
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setDistrict(district);
        healthBlock.setCode(HBCode);
        healthBlock.setName("health block name");
        healthBlock.setRegionalName("health block regional name");
        healthBlock.setHq("health block hq");
        healthBlock.setTalukaIdOID(talukaIdOid);
        healthBlock = healthBlockDataService.create(healthBlock);
        return healthBlock;
    }

    private HealthFacility createHealthFacility(HealthBlock healthBlock,Long talukaIDOID,Long HFCode,Long distIdOID){
        HealthFacility healthFacility =new HealthFacility();
        healthFacility.setCode(HFCode);
        healthFacility.setHealthBlock(healthBlock);
        healthFacility.setDistrictIdOID(distIdOID);
        healthFacility.setHealthFacilityType(null);
        healthFacility.setName("health facility name");
        healthFacility.setRegionalName("health facility regional name");
        healthFacility.setTalukaIdOID(talukaIDOID);
        return healthFacility;
    }

    private HealthSubFacility createHealthSubFacility(HealthFacility healthFacility,Long HSFCode,Long talukaIDOID){
        HealthSubFacility healthSubFacility =new HealthSubFacility();
        healthSubFacility.setCode(HSFCode);
        healthSubFacility.setName("health sub facility name");
        healthSubFacility.setRegionalName("health sub facility regional name");
        healthSubFacility.setHealthFacility(healthFacility);
        healthSubFacility.setTalukaIdOID(talukaIDOID);
        return healthSubFacility;
    }

    private Village createVillage(Taluka taluka,Long VCode){
        Village village = new Village();
        village.setName("Village name");
        village.setTaluka(taluka);
        village.setVcode(VCode);
        village.setRegionalName("Village regional name");
         return village;
    }



    private Map<String, Object> mapDistrict(District district,Long stateCode){
        Map<String, Object> map = new HashMap<>();
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, district.getCode());
        map.put(KilkariConstants.DISTRICT_NAME, district.getName());
        map.put(KilkariConstants.EXEC_DATE, null);
        return map;
    }
    private Map<String, Object> mapHealthBlock(HealthBlock healthBlock, Long stateCode){
        Map<String, Object> map = new HashMap<>();
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, healthBlock.getDistrict().getCode());
        map.put(KilkariConstants.TALUKA_ID, (talukaDataService.findById(healthBlock.getTalukaIdOID())).getCode());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, healthBlock.getCode());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, healthBlock.getName());
        map.put(KilkariConstants.EXEC_DATE, null);
        return map;
    }

    private Map<String, Object> mapHealthFacility(HealthFacility healthFacility, Long stateCode){
        Map<String, Object> map = new HashMap<>();
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, (districtDataService.findById(healthFacility.getDistrictIdOID())).getCode());
        map.put(KilkariConstants.TALUKA_ID, (talukaDataService.findById(healthFacility.getTalukaIdOID())).getCode());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, healthFacility.getHealthBlock().getCode());
        map.put(KilkariConstants.HEALTH_FACILITY_ID, healthFacility.getCode());
        map.put(KilkariConstants.HEALTH_FACILITY_NAME, healthFacility.getName());
        map.put(KilkariConstants.EXEC_DATE, null);
        return map;
    }

    private Map<String, Object> mapHealthSubFacility(HealthSubFacility healthSubFacility, Long stateCode){
        Map<String, Object> map = new HashMap<>();
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, (districtDataService.findById(healthSubFacility.getDistrictIdOID())).getCode());
        map.put(KilkariConstants.TALUKA_ID, (talukaDataService.findById(healthSubFacility.getTalukaIdOID())).getCode());
        map.put(KilkariConstants.HEALTH_FACILITY_ID, healthSubFacility.getHealthFacility().getCode() );
        map.put(KilkariConstants.HEALTH_SUB_FACILITY_ID, healthSubFacility.getCode());
        map.put(KilkariConstants.HEALTH_SUB_FACILITY_NAME, healthSubFacility.getName());
        map.put(KilkariConstants.EXEC_DATE, null);
        return map;
    }

    private Map<String, Object> mapTaluka(Taluka taluka,Long stateCode){
        Map<String, Object> map = new HashMap<>();
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, taluka.getDistrict().getCode());
        map.put(KilkariConstants.TALUKA_ID, taluka.getCode());
        map.put(KilkariConstants.TALUKA_NAME, taluka.getName());
        map.put(KilkariConstants.EXEC_DATE, null);
        return map;
    }

    private Map<String, Object> mapVillage(Village village, Long stateCode){
        Map<String, Object> map = new HashMap<>();
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, (districtDataService.findById(village.getDistrictIdOID())).getCode());
        map.put(KilkariConstants.TALUKA_ID, village.getTaluka().getCode());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, village.getVcode());
        map.put(KilkariConstants.VILLAGE_NAME, village.getName());
        map.put(KilkariConstants.EXEC_DATE, null);
        return map;
    }

   /** To maintain location codes unique here 1s are used for district, 2s for HB, 3s for Taluka, 4s for HF, 5s for HSF, 6s for Villages. */


    /**
     * To verify district location rejection data is updated successfully.
     */
    @Test
    public void verifyDistrictRejections() throws InterruptedException, IOException {
        State state = createState();
        /** correct district record */
        District district1 = createDistrict(state,1L);
        District district2 = createDistrict(state,11L);
        /**creating faulty record by setting location code null*/
        district2.setCode(null);
        District district3 = createDistrict(state,111L);
        /**creating faulty record by setting location name null*/
        district3.setName(null);
        List<Map<String, Object>> distList = new ArrayList<>();
        Map<String, State> stateHashMap = new HashMap<>();
        stateHashMap.put(state.getCode().toString(), state);
        distList.add(mapDistrict(district1,state.getCode()));
        distList.add(mapDistrict(district2,state.getCode()));
        distList.add(mapDistrict(district3,state.getCode()));
        districtService.createUpdateDistricts(distList,stateHashMap);
        List districtsRejected = districtRejectionDataService.retrieveAll();
        /** checking total count of rejected record created as location code null will create 2 and 1 for location name not present*/
        assertTrue(districtsRejected.size() == 3);
    }


    /**
     * To verify health block location rejection data is updated successfully.
     */
    @Test
    public void verifyHealthBlockRejections() {
    State state = createState();
    District district = createDistrict(state, 1L);
    Taluka taluka = createTaluka(district,"0003");
    HealthBlock healthBlock1 = createHealthBlock(district,2l,taluka.getId());
    HealthBlock healthBlock2 = createHealthBlock(district,22L,taluka.getId());
    healthBlock2.setName(null);
    HealthBlock healthBlock3 = createHealthBlock(district,222L,null);
    Map<String, State> stateHashMap = new HashMap<>();
    stateHashMap.put(state.getCode().toString(), state);

    Map<String, District> districtHashMap;
    List<Map<String, Object>> HBList = new ArrayList<>();
    HBList.add(mapHealthBlock(healthBlock1,state.getCode()));
    HBList.add(mapHealthBlock(healthBlock2,state.getCode()));
    HBList.add(mapHealthBlock(healthBlock3,state.getCode()));
    districtHashMap = districtService.fillDistrictIds(HBList, stateHashMap);
    Map<String, Taluka> talukaHashMap;
    talukaHashMap = talukaService.fillTalukaIds(HBList, districtHashMap);

    healthBlockService.createUpdateHealthBlocks(HBList,stateHashMap,districtHashMap,talukaHashMap);

    List HBRejected = healthBlockRejectionDataService.retrieveAll();

    assertTrue(HBRejected.size() ==2 );
    }


    /**
     * To verify health facility location rejection data is updated successfully.
     */
    @Test
    public void verifyHealthFacilityRejections() {
        State state = createState();
        District district = createDistrict(state, 1L);
        Taluka taluka = createTaluka(district,"0003");

        HealthBlock hb1 = createHealthBlock(district,2L,taluka.getId());
        HealthBlock hb2 = createHealthBlock(district,22L,null);

        HealthFacility hf1 = createHealthFacility(hb1,taluka.getId(),4L,district.getId());
        HealthFacility hf2 = createHealthFacility(hb1,taluka.getId(),44L,district.getId());
        hf2.setName(null);

        hb1.setCode(null);
        HealthFacility hf3 = createHealthFacility(hb1,taluka.getId(),444L,district.getId());
        HealthFacility hf4 = createHealthFacility(hb2,null,4444l,district.getId());

        Map<String, State> stateHashMap = new HashMap<>();
        stateHashMap.put(state.getCode().toString(), state);

        List<Map<String, Object>> HFList = new ArrayList<>();
        HFList.add(mapHealthFacility(hf1,state.getCode()));
        HFList.add(mapHealthFacility(hf2,state.getCode()));
        HFList.add(mapHealthFacility(hf3,state.getCode()));
        HFList.add(mapHealthFacility(hf4,state.getCode()));

        Map<String, District> districtHashMap = districtService.fillDistrictIds(HFList,stateHashMap);
        Map<String, Taluka> talukaHashMap = talukaService.fillTalukaIds(HFList,districtHashMap);
        Map<String, HealthBlock> healthBlockHashMap = healthBlockService.fillHealthBlockIds(HFList,districtHashMap);

        healthFacilityService.createUpdateHealthFacilities(HFList,stateHashMap,districtHashMap,talukaHashMap,healthBlockHashMap);

        List HFRejected = healthFacilityRejectionDataService.retrieveAll();

        assertTrue(HFRejected.size() ==3 );
    }


    /**
     * To verify health sub facility location rejection data is updated successfully.
     */
    @Test
    public void verifyHealthSubFacilityRejections() {
        State state = createState();
        District district = createDistrict(state, 1L);
        Taluka taluka = createTaluka(district,"0003");
        HealthBlock healthBlock = createHealthBlock(district,2l,taluka.getId());
        HealthFacility healthFacility = createHealthFacility(healthBlock,taluka.getId(),4l,district.getId());

        HealthSubFacility healthSubFacility = createHealthSubFacility(healthFacility,5l,taluka.getId());
        HealthSubFacility healthSubFacility1 = createHealthSubFacility(healthFacility,55l,null);
        HealthSubFacility healthSubFacility2 = createHealthSubFacility(healthFacility,555l,taluka.getId());
        healthSubFacility2.setName(null);
        List<Map<String, Object>> HSFList = new ArrayList<>();
        HSFList.add(mapHealthSubFacility(healthSubFacility,state.getCode()));
        HSFList.add(mapHealthSubFacility(healthSubFacility1,state.getCode()));
        HSFList.add(mapHealthSubFacility(healthSubFacility2,state.getCode()));

        Map<String, State> stateHashMap = new HashMap<>();
        stateHashMap.put(state.getCode().toString(), state);
        Map<String, District> districtHashMap = districtService.fillDistrictIds(HSFList,stateHashMap);
        Map<String, Taluka> talukaHashMap = talukaService.fillTalukaIds(HSFList,districtHashMap);
        Map<String, HealthFacility> healthFacilityHashMap = healthFacilityService.fillHealthFacilitiesFromTalukas(HSFList,talukaHashMap);

        healthSubFacilityService.createUpdateHealthSubFacilities(HSFList,stateHashMap,districtHashMap,talukaHashMap,healthFacilityHashMap);

        List HSFRejected = healthSubFacilityRejectionDataService.retrieveAll();

        assertTrue(HSFRejected.size() ==2 );

    }


    /**
     * To verify taluka location rejection data is updated successfully.
     */
    @Test
    public void verifyTalukaRejections() {
        State state = createState();
        District district = createDistrict(state, 1L);
        Map<String, State> stateHashMap = new HashMap<>();
        stateHashMap.put(state.getCode().toString(), state);

        List<Map<String, Object>> TalukaList = new ArrayList<>();
        Taluka taluka = createTaluka(district,"0003");
        Taluka taluka1 = createTaluka(district, "0033");
        taluka1.setName(null);
        taluka.getDistrict().setCode(null);

        TalukaList.add(mapTaluka(taluka,state.getCode()));
        TalukaList.add(mapTaluka(taluka1,state.getCode()));

        Map<String, District> districtHashMap = districtService.fillDistrictIds(TalukaList,stateHashMap);

        talukaService.createUpdateTalukas(TalukaList,stateHashMap,districtHashMap);

        List talukaRejects = talukaRejectionDataService.retrieveAll();

        assertTrue(talukaRejects.size() ==2 );

    }


    /**
     * To verify village location rejection data is updated successfully.
     */
    @Test
    public void verifyVillageRejections() {
        State state = createState();
        District district = createDistrict(state, 1L);
        Taluka t1 = createTaluka(district,"0003");
        district.setCode(null);
        Taluka t2 = createTaluka(district, "0033");
        Village v1 = createVillage(t1,6L);
        v1.getTaluka().setCode(null);
        Village v2 = createVillage(t1,66L);
        v2.setName(null);
        Village v3 = createVillage(t2,666L);

        Map<String, State> stateHashMap = new HashMap<>();
        stateHashMap.put(state.getCode().toString(), state);
        List<Map<String, Object>> VillageList = new ArrayList<>();

        VillageList.add(mapVillage(v1,state.getCode()));
        VillageList.add(mapVillage(v2,state.getCode()));
        VillageList.add(mapVillage(v3,state.getCode()));

        Map<String, District> districtHashMap = districtService.fillDistrictIds(VillageList,stateHashMap);

        Map<String, Taluka> talukaHashMap = talukaService.fillTalukaIds(VillageList,districtHashMap);

        villageService.createUpdateVillages(VillageList,stateHashMap,districtHashMap,talukaHashMap);

        List villageRejects = villageRejectionDataService.retrieveAll();

        assertTrue(villageRejects.size() ==3 );

    }
}
