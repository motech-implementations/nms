# TODO
- Create location, circle & language import files
- Write script to generate MCTS import files
  - Script should generate seperate files for:
    - Mothers
    - Children
    - FLWs
  - Parameter for count should be included
  - What other toggles should the script take?
- How will we set config values?
  - Deployed services?
  - Whitelist
- How will we age records to test purging?
- How will we simulate user load & IVR
- Should a script be created that can take a clean server, import files & script for data munging and produce a deployed server for testing?

# To generate the SQLAlchemy models file
sqlacodegen --tables NMS_KK_SUMMARY_RECORDS_STATUSSTATS,nms_call_content,nms_circles,nms_csv_audit_records,nms_deployed_services,nms_districts,nms_flw_cdrs,nms_front_line_workers,nms_health_blocks,nms_health_facilities,nms_health_facility_types,nms_health_sub_facilities,nms_imi_cdrs,nms_imi_csrs,nms_imi_file_audit_records,nms_inbox_call_data,nms_inbox_call_details,nms_kk_retry_records,nms_kk_summary_records,nms_languages,nms_ma_completion_records,nms_ma_course,nms_mcts_children,nms_mcts_mothers,nms_national_default_language,nms_service_usage_caps,nms_states,nms_states_join_circles,nms_subscribers,nms_subscription_errors,nms_subscription_pack_messages,nms_subscription_packs,nms_subscriptions,nms_talukas,nms_villages,nms_whitelist_entries,nms_whitelisted_states mysql://root:password@localhost/motech_data_services > models.py

For imports we need the following files with the following fields:

# States:

StateID,Name

# Circles:

Circle,State

# Districts:

DCode,Name_G,Name_E,StateID

# Talukas:

TCode,ID,Name_G,Name_E,DCode,StateID

# Census Villages:

VCode,Name_G,Name_E,TCode,DCode,StateID

# Non-Census Village

SVID,Name_G,Name_E,TCode,VCode,DCode,StateID

# HealthBlocks:

BID,Name_G,Name_E,HQ,TCode,DCode,StateID

# Health Facilities:

PID,Name_G,Name_E,BID,Facility_Type,TCode,DCode,StateID

# Health Sub-Facilities:

SID,Name_G,Name_E,PID,BID,TCode,DCode,StateID

# Languages -> Location mapping:

languagelocation code,Language,Circle,State,District,Default Language for Circle (Y/N)";

# Child beneficiaries:

State Name :	
User Name :	
Password :	
From Date (dd-mm-yyyy) :	
To Date (dd-mm-yyyy) :	
		
StateID	District_ID	Taluka_ID	HealthBlock_ID	PHC_ID	SubCentre_ID	Village_ID	ID_No	Name	Mother_ID	Whom_PhoneNo	Birthdate Entry_Type

# Mother beneficiaries:

State Name :	
User Name :	
Password :	
From Date (dd-mm-yyyy) :	
To Date (dd-mm-yyyy) :	
		
StateID	District_ID	Taluka_ID	HealthBlock_ID	PHC_ID	SubCentre_ID	Village_ID	ID_No	Name	Whom_PhoneNo	Birthdate	LMP_Date	Abortion	Outcome_Nos Entry_Type

# FLWs

State Name : State 1
User Name :
Password :
From Date (dd-mm-yyyy) :	
To Date (dd-mm-yyyy) :	
		
ID	District_ID	Taluka_ID	HealthBlock_ID	PHC_ID	SubCentre_ID	Village_ID	Name	Contact_No
