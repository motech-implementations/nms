package org.motechproject.nms.testing.it.helperUtils;

import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;

import javax.jdo.Query;
import java.util.List;

public class HelperUtils {


    public static List<MctsMother> retrieveAllMothers(MctsMotherDataService mctsMotherDataService){

        SqlQueryExecution<List<MctsMother>> queryExecution = new SqlQueryExecution<List<MctsMother>>() {

            @Override
            public String getSqlQuery() {
                return "Select * FROM nms_mcts_mothers";
            }

            @Override
            public List<MctsMother> execute(Query query) {
                query.setClass(MctsMother.class);
                return (List<MctsMother>) query.execute();
            }
        };
        return mctsMotherDataService.executeSQLQuery(queryExecution);
    }
}
