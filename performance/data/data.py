import argparse
import unirest
import sys
import os


def data_file(name):
    this_dir = os.path.split(__file__)[0]
    return os.path.join(this_dir, "files", name)


def exec_http_command(url):
    if args.verbose:
        print "GET url      = {}".format(url)

    response = unirest.post(url=url)

    if args.verbose:
        print "response code = {}".format(response.code)

    if args.debug:
        print "response body = {}".format(response.body)

    if response.code != 200:
        print "### ERROR ###"
        print "Expecting HTTP 200 but received {}".format(response.code)
        sys.exit(1)


def clear_database():
    exec_http_command("{}/module/testing/clearDatabase".format(args.server))


def create_subscription_packs():
    exec_http_command("{}/module/testing/createSubscriptionPacks".format(args.server))


def import_domain_data(url_part, file):
    url = "{}/module/region/{}".format(args.server, url_part)
    params = {"parameter": "value", "csvFile": open(data_file(file))}

    if args.verbose:
        print "POST url      = {}".format(url)
        print "POST params   = {}".format(params)

    response = unirest.post(url=url, params=params)

    if args.verbose:
        print "response code = {}".format(response.code)

    if args.debug:
        print "response body = {}".format(response.body)

    if response.code != 200:
        print "Expected HTTP 200 but received {}".format(response.code)
        sys.exit(1)


def import_region_domain_data(what):
    url_part = "data/import/{}".format(what)
    file = "{}.csv".format(what)
    import_domain_data(url_part, file)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--server", help="MOTECH URL", default="http://localhost:8080/motech-platform-server")
    parser.add_argument("--verbose", help="verbose mode", action="store_true")
    parser.add_argument("--domain", help="create domain data", action="store_true")
    parser.add_argument("--cleardb", help="clear database", action="store_true")
    parser.add_argument("--debug", help="debug mode, even more verbose", action="store_true")
    parser.add_argument("--mctsmoms", help="number of MCTS mothers", type=int, default=10)
    parser.add_argument("--mctskids", help="number of MCTS children", type=int, default=10)
    parser.add_argument("--ivrmoms", help="number of IVR mothers", type=int, default=10)
    parser.add_argument("--ivrkids", help="number of IVS children", type=int, default=10)
    args = parser.parse_args()

    if args.verbose:
        print "verbose mode\nargs={}".format(args)

    #
    # http defaults
    #
    unirest.timeout(30)
    unirest.default_header("Accept", "application/json")

    #
    # Clear the database?
    #
    if args.cleardb:
        clear_database()

    #
    # Create domain data?
    #
    if args.domain:
        create_subscription_packs()
        import_region_domain_data("state")
        import_region_domain_data("circle")
        import_region_domain_data("district")
        import_domain_data("languageLocationCode/import", "language_location_code.csv")

    #
    # Create a MCTS mother file
    #
    header = ['StateID', 'District_ID', 'District_Name', 'Taluka_ID', 'Taluka_Name', 'HealthBlock_ID',
              'HealthBlock_Name', 'PHC_ID', 'PHC_Name', 'SubCentre_ID', 'SubCentre_Name', 'Village_ID', 'Village_Name',
              'Yr', 'GP_Village', 'Address', 'ID_No', 'Name', 'Husband_Name', 'PhoneNo_Of_Whom', 'Whom_PhoneNo',
              'Birthdate', 'JSY_Beneficiary', 'Caste', 'SubCentre_Name1', 'ANM_Name', 'ANM_Phone', 'ASHA_Name',
              'ASHA_Phone', 'Delivery_Lnk_Facility', 'Facility_Name', 'LMP_Date', 'ANC1_Date', 'ANC2_Date', 'ANC3_Date',
              'ANC4_Date', 'TT1_Date', 'TT2_Date', 'TTBooster_Date', 'IFA100_Given_Date', 'Anemia', 'ANC_Complication',
              'RTI_STI', 'Dly_Date', 'Dly_Place_Home_Type', 'Dly_Place_Public', 'Dly_Place_Private', 'Dly_Type',
              'Dly_Complication', 'Discharge_Date', 'JSY_Paid_Date', 'Abortion', 'PNC_Home_Visit', 'PNC_Complication',
              'PPC_Method', 'PNC_Checkup', 'Outcome_Nos', 'Child1_Name', 'Child1_Sex', 'Child1_Wt',
              'Child1_Brestfeeding', 'Child2_Name', 'Child2_Sex', 'Child2_Wt', 'Child2_Brestfeeding', 'Child3_Name',
              'Child3_Sex', 'Child3_Wt', 'Child3_Brestfeeding', 'Child4_Name', 'Child4_Sex', 'Child4_Wt',
              'Child4_Brestfeeding', 'Age', 'MTHR_REG_DATE', 'LastUpdateDate', 'Remarks', 'ANM_ID', 'ASHA_ID',
              'Call_Ans', 'NoCall_Reason', 'NoPhone_Reason', 'Created_By', 'Updated_By', 'Aadhar_No', 'BPL_APL', 'EID',
              'EIDTime', 'Entry_Type']