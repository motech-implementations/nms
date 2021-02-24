package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

/**Created by vishnu on 27/6/18.
 * update by rakesh on 20/08/20.
 */

public class RchDistrictRecord {

    @JsonProperty("DistrictCode")
    private Long districtCode;

    @JsonProperty("DistrictName")
    private String districtName;

    @JsonProperty("MDDS_Code")
    private Long MDDS_Code;

    @JsonProperty("StateCode")
    private Long StateCode;


    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Long getMDDS_Code() {
        return MDDS_Code;
    }

    public void setMDDS_Code(Long MDDS_Code) {
        this.MDDS_Code = MDDS_Code;
    }

    public Long getStateCode() {
        return StateCode;
    }

    public void setStateCode(Long stateCode) {
        StateCode = stateCode;
    }
}
