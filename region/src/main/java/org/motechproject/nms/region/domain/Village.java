package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This class has to hold both census and non-census villages.
 *
 * A Census village will have the following schema:
 * VCode int M Not null
 * Name_E varchar 50 M Not null English Name
 * Name_G nvarchar 50 M Not null Regional language name
 *
 * A Non-Census village will have the following schema:
 * SVID int M Not null Codes greater than 100000
 * VCode int O NULL If associated with any census village then code of that village
 * Name_E varchar 50 M Not null English Name
 * Name_G nvarchar 50 M Not null Regional language name
 *
 */
@Entity(tableName = "nms_villages", recordHistory = true)
@Unique(name = "uniqueVillageCode", members = {"vcode", "svid" })
//@ValidVillage
public class Village extends MdsEntity {

    @Field
    @Column(allowsNull = "false", length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String name;

    @Field
    @Column(allowsNull = "false", length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String regionalName;

    @Field
    // This is the village code for a census village.  One may be present for a non-census village if it is
    // associated with a census village
    private Long vcode;

    @Field
    // This is the village code for a non-census village.
    private Long svid;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Taluka taluka;

    public Village() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionalName() {
        return regionalName;
    }

    public void setRegionalName(String regionalName) {
        this.regionalName = regionalName;
    }

    /**
     * Returns the village code.  For a census village this will be the vcode provided by the MoH.
     * For a non-census village it will be the vcode if the village is associated with a census village.  If
     * it isn't then it will be the svid provided by the MoH
     *
     * @return vcode for a census village.  vcode or svid for a non-census village
     */
    @Ignore
    public Long getVillageCode() {
        if (vcode != null) {
            return vcode;
        }

        if (svid != null) {
            return svid;
        }

        return null;
    }

    public Long getVcode() {
        return vcode;
    }

    public void setVcode(Long vcode) {
        this.vcode = vcode;
    }

    public Long getSvid() {
        return svid;
    }

    public void setSvid(Long svid) {
        this.svid = svid;
    }

    public Taluka getTaluka() {
        return taluka;
    }

    public void setTaluka(Taluka taluka) {
        this.taluka = taluka;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Village village = (Village) o;

        if (name != null ? !name.equals(village.name) : village.name != null) {
            return false;
        }
        if (vcode != null ? !vcode.equals(village.vcode) : village.vcode != null) {
            return false;
        }
        return !(svid != null ? !svid.equals(village.svid) : village.svid != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (vcode != null ? vcode.hashCode() : 0);
        result = 31 * result + (svid != null ? svid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Village{" +
                "name='" + name + '\'' +
                ", regionalName='" + regionalName + '\'' +
                ", vcode=" + vcode +
                ", svid=" + svid +
                '}';
    }
}
