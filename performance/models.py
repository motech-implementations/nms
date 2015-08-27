# coding: utf-8
from sqlalchemy import BigInteger, Column, DateTime, ForeignKey, Index, Integer, String, text
from sqlalchemy.orm import relationship
from sqlalchemy.dialects.mysql.base import BIT
from sqlalchemy.ext.declarative import declarative_base


Base = declarative_base()
metadata = Base.metadata


class NMSKKSUMMARYRECORDSSTATUSSTAT(Base):
    __tablename__ = 'NMS_KK_SUMMARY_RECORDS_STATUSSTATS'

    id_OID = Column(ForeignKey(u'nms_kk_summary_records.id'), primary_key=True, nullable=False, index=True)
    KEY = Column(Integer, primary_key=True, nullable=False)
    VALUE = Column(Integer)

    nms_kk_summary_record = relationship(u'NmsKkSummaryRecord')


class NmsCallContent(Base):
    __tablename__ = 'nms_call_content'

    id = Column(BigInteger, primary_key=True, index=True)
    callDetailRecord_id_OID = Column(ForeignKey(u'nms_flw_cdrs.id', ondelete=u'CASCADE'), index=True)
    completionFlag = Column(BIT(1))
    contentFile = Column(String(255, u'utf8_bin'))
    contentName = Column(String(255, u'utf8_bin'))
    correctAnswerEntered = Column(BIT(1))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    endTime = Column(DateTime)
    mobileKunjiCardCode = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    startTime = Column(DateTime)
    type = Column(String(255, u'utf8_bin'))

    nms_flw_cdr = relationship(u'NmsFlwCdr')


class NmsCircle(Base):
    __tablename__ = 'nms_circles'

    id = Column(BigInteger, primary_key=True, index=True)
    defaultLanguage_id_OID = Column(ForeignKey(u'nms_languages.id'), index=True)
    name = Column(String(255, u'utf8_bin'), unique=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))

    nms_language = relationship(u'NmsLanguage')


class NmsCsvAuditRecord(Base):
    __tablename__ = 'nms_csv_audit_records'

    id = Column(BigInteger, primary_key=True, index=True)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    endpoint = Column(String(255, u'utf8_bin'))
    file = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    outcome = Column(String(1000, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))


class NmsDeployedService(Base):
    __tablename__ = 'nms_deployed_services'
    __table_args__ = (
        Index('UNIQUE_STATE_SERVICE_COMPOSITE_IDX', 'state_id_OID', 'service', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    service = Column(String(255, u'utf8_bin'), nullable=False)
    state_id_OID = Column(ForeignKey(u'nms_states.id'), nullable=False, index=True)

    nms_state = relationship(u'NmsState')


class NmsDistrict(Base):
    __tablename__ = 'nms_districts'
    __table_args__ = (
        Index('UNIQUE_STATE_CODE', 'state_id_OID', 'code', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(BigInteger, nullable=False)
    language_id_OID = Column(ForeignKey(u'nms_languages.id'), index=True)
    name = Column(String(100, u'utf8_bin'))
    regionalName = Column(String(100, u'utf8_bin'))
    state_id_OID = Column(ForeignKey(u'nms_states.id', ondelete=u'CASCADE'), nullable=False, index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    districts_INTEGER_IDX = Column(Integer)

    nms_language = relationship(u'NmsLanguage')
    nms_state = relationship(u'NmsState')


class NmsFlwCdr(Base):
    __tablename__ = 'nms_flw_cdrs'

    id = Column(BigInteger, primary_key=True, index=True)
    callDisconnectReason = Column(String(255, u'utf8_bin'))
    callDurationInPulses = Column(Integer, nullable=False)
    callEndTime = Column(DateTime)
    callId = Column(BigInteger, nullable=False)
    callStartTime = Column(DateTime)
    callStatus = Column(Integer, nullable=False)
    callingNumber = Column(BigInteger, nullable=False, index=True)
    circle = Column(String(255, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    endOfUsagePromptCounter = Column(Integer, nullable=False)
    finalCallStatus = Column(String(255, u'utf8_bin'))
    frontLineWorker_id_OID = Column(ForeignKey(u'nms_front_line_workers.id'), index=True)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    operator = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    service = Column(String(255, u'utf8_bin'), nullable=False)
    welcomePrompt = Column(BIT(1))

    nms_front_line_worker = relationship(u'NmsFrontLineWorker')


class NmsFrontLineWorker(Base):
    __tablename__ = 'nms_front_line_workers'
    __table_args__ = (
        Index('status_invalidationDate_composit_idx', 'status', 'invalidationDate'),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    contactNumber = Column(BigInteger, nullable=False, unique=True)
    district_id_OID = Column(ForeignKey(u'nms_districts.id'), index=True)
    flwId = Column(String(255, u'utf8_bin'), index=True)
    healthBlock_id_OID = Column(ForeignKey(u'nms_health_blocks.id'), index=True)
    healthFacility_id_OID = Column(ForeignKey(u'nms_health_facilities.id'), index=True)
    healthSubFacility_id_OID = Column(ForeignKey(u'nms_health_sub_facilities.id'), index=True)
    invalidationDate = Column(DateTime)
    language_id_OID = Column(ForeignKey(u'nms_languages.id'), index=True)
    mctsFlwId = Column(String(255, u'utf8_bin'))
    name = Column(String(255, u'utf8_bin'))
    state_id_OID = Column(ForeignKey(u'nms_states.id'), index=True)
    status = Column(String(255, u'utf8_bin'))
    taluka_id_OID = Column(ForeignKey(u'nms_talukas.id'), index=True)
    village_id_OID = Column(ForeignKey(u'nms_villages.id'), index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))

    nms_district = relationship(u'NmsDistrict')
    nms_health_block = relationship(u'NmsHealthBlock')
    nms_health_facility = relationship(u'NmsHealthFacility')
    nms_health_sub_facility = relationship(u'NmsHealthSubFacility')
    nms_language = relationship(u'NmsLanguage')
    nms_state = relationship(u'NmsState')
    nms_taluka = relationship(u'NmsTaluka')
    nms_village = relationship(u'NmsVillage')


class NmsHealthBlock(Base):
    __tablename__ = 'nms_health_blocks'
    __table_args__ = (
        Index('UNIQUE_TALUKA_CODE', 'taluka_id_OID', 'code', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(BigInteger, nullable=False)
    hq = Column(String(50, u'utf8_bin'))
    name = Column(String(35, u'utf8_bin'))
    regionalName = Column(String(50, u'utf8_bin'))
    taluka_id_OID = Column(ForeignKey(u'nms_talukas.id', ondelete=u'CASCADE'), nullable=False, index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    healthBlocks_INTEGER_IDX = Column(Integer)

    nms_taluka = relationship(u'NmsTaluka')


class NmsHealthFacility(Base):
    __tablename__ = 'nms_health_facilities'
    __table_args__ = (
        Index('UNIQUE_HEALTH_BLOCK_CODE', 'healthBlock_id_OID', 'code', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(BigInteger, nullable=False)
    healthBlock_id_OID = Column(ForeignKey(u'nms_health_blocks.id', ondelete=u'CASCADE'), nullable=False, index=True)
    healthFacilityType_id_OID = Column(ForeignKey(u'nms_health_facility_types.id'), nullable=False, index=True)
    name = Column(String(50, u'utf8_bin'))
    regionalName = Column(String(50, u'utf8_bin'))
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    healthFacilities_INTEGER_IDX = Column(Integer)

    nms_health_block = relationship(u'NmsHealthBlock')
    nms_health_facility_type = relationship(u'NmsHealthFacilityType')


class NmsHealthFacilityType(Base):
    __tablename__ = 'nms_health_facility_types'

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(BigInteger, nullable=False, unique=True)
    name = Column(String(100, u'utf8_bin'))
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))


class NmsHealthSubFacility(Base):
    __tablename__ = 'nms_health_sub_facilities'
    __table_args__ = (
        Index('UNIQUE_HEALTH_FACILITY_CODE', 'healthFacility_id_OID', 'code', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(BigInteger, nullable=False)
    healthFacility_id_OID = Column(ForeignKey(u'nms_health_facilities.id', ondelete=u'CASCADE'), nullable=False, index=True)
    name = Column(String(100, u'utf8_bin'))
    regionalName = Column(String(100, u'utf8_bin'))
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    healthSubFacilities_INTEGER_IDX = Column(Integer)

    nms_health_facility = relationship(u'NmsHealthFacility')


class NmsImiCdr(Base):
    __tablename__ = 'nms_imi_cdrs'

    id = Column(BigInteger, primary_key=True, index=True)
    attemptNo = Column(String(255, u'utf8_bin'))
    callAnswerTime = Column(String(255, u'utf8_bin'))
    callDisconnectReason = Column(String(255, u'utf8_bin'))
    callDurationInPulse = Column(String(255, u'utf8_bin'))
    callEndTime = Column(String(255, u'utf8_bin'))
    callId = Column(String(255, u'utf8_bin'))
    callStartTime = Column(String(255, u'utf8_bin'))
    callStatus = Column(String(255, u'utf8_bin'))
    circleId = Column(String(255, u'utf8_bin'))
    contentFile = Column(String(255, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    languageLocationId = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    msgPlayEndTime = Column(String(255, u'utf8_bin'))
    msgPlayStartTime = Column(String(255, u'utf8_bin'))
    msisdn = Column(BigInteger)
    operatorId = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    priority = Column(String(255, u'utf8_bin'))
    requestId = Column(String(255, u'utf8_bin'))
    weekId = Column(String(255, u'utf8_bin'))


class NmsImiCsr(Base):
    __tablename__ = 'nms_imi_csrs'

    id = Column(BigInteger, primary_key=True, index=True)
    attempts = Column(Integer)
    callFlowUrl = Column(String(255, u'utf8_bin'))
    circle = Column(String(255, u'utf8_bin'))
    cli = Column(String(255, u'utf8_bin'))
    contentFileName = Column(String(255, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    finalStatus = Column(Integer)
    languageLocationCode = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    msisdn = Column(BigInteger)
    owner = Column(String(255, u'utf8_bin'))
    priority = Column(Integer)
    requestId = Column(String(255, u'utf8_bin'))
    serviceId = Column(String(255, u'utf8_bin'))
    statusCode = Column(Integer)
    weekId = Column(String(255, u'utf8_bin'))


class NmsImiFileAuditRecord(Base):
    __tablename__ = 'nms_imi_file_audit_records'

    id = Column(BigInteger, primary_key=True, index=True)
    checksum = Column(String(40, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    error = Column(String(1024, u'utf8_bin'))
    fileName = Column(String(255, u'utf8_bin'), index=True)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    recordCount = Column(Integer)
    success = Column(BIT(1), nullable=False)
    type = Column(String(255, u'utf8_bin'), nullable=False)


class NmsInboxCallDatum(Base):
    __tablename__ = 'nms_inbox_call_data'

    id = Column(BigInteger, primary_key=True, index=True)
    contentFileName = Column(String(255, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    endTime = Column(DateTime)
    inboxWeekId = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    startTime = Column(DateTime)
    subscriptionId = Column(String(255, u'utf8_bin'))
    subscriptionPack = Column(String(255, u'utf8_bin'))
    content_id_OWN = Column(ForeignKey(u'nms_inbox_call_details.id'), index=True)

    nms_inbox_call_detail = relationship(u'NmsInboxCallDetail')


class NmsInboxCallDetail(Base):
    __tablename__ = 'nms_inbox_call_details'

    id = Column(BigInteger, primary_key=True, index=True)
    callDisconnectReason = Column(Integer)
    callDurationInPulses = Column(Integer)
    callEndTime = Column(DateTime)
    callId = Column(BigInteger)
    callStartTime = Column(DateTime)
    callStatus = Column(Integer)
    callingNumber = Column(BigInteger)
    circle = Column(String(255, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    operator = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))


class NmsKkRetryRecord(Base):
    __tablename__ = 'nms_kk_retry_records'

    id = Column(BigInteger, primary_key=True, index=True)
    callStage = Column(String(255, u'utf8_bin'))
    circle = Column(String(255, u'utf8_bin'))
    contentFileName = Column(String(255, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    dayOfTheWeek = Column(String(255, u'utf8_bin'), index=True)
    languageLocationCode = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    msisdn = Column(BigInteger)
    owner = Column(String(255, u'utf8_bin'))
    subscriptionId = Column(String(255, u'utf8_bin'), index=True)
    subscriptionOrigin = Column(String(255, u'utf8_bin'))
    weekId = Column(String(255, u'utf8_bin'))


class NmsKkSummaryRecord(Base):
    __tablename__ = 'nms_kk_summary_records'

    id = Column(BigInteger, primary_key=True, index=True)
    attemptedDayCount = Column(Integer)
    callAttempts = Column(Integer)
    circle = Column(String(255, u'utf8_bin'))
    contentFileName = Column(String(255, u'utf8_bin'))
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    finalStatus = Column(String(255, u'utf8_bin'))
    languageLocationCode = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    msisdn = Column(BigInteger)
    owner = Column(String(255, u'utf8_bin'))
    percentPlayed = Column(Integer)
    requestId = Column(String(255, u'utf8_bin'), unique=True)
    weekId = Column(String(255, u'utf8_bin'))


class NmsLanguage(Base):
    __tablename__ = 'nms_languages'

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(String(255, u'utf8_bin'), unique=True)
    name = Column(String(255, u'utf8_bin'), unique=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))


class NmsMaCompletionRecord(Base):
    __tablename__ = 'nms_ma_completion_records'

    id = Column(BigInteger, primary_key=True, index=True)
    callingNumber = Column(BigInteger, nullable=False, unique=True)
    completionCount = Column(Integer, nullable=False)
    lastDeliveryStatus = Column(String(255, u'utf8_bin'))
    notificationRetryCount = Column(Integer, nullable=False)
    score = Column(Integer, nullable=False)
    sentNotification = Column(BIT(1), nullable=False)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))


class NmsMaCourse(Base):
    __tablename__ = 'nms_ma_course'

    id = Column(BigInteger, primary_key=True, index=True)
    content = Column(String)
    name = Column(String(255, u'utf8_bin'), unique=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))


class NmsMctsChildren(Base):
    __tablename__ = 'nms_mcts_children'

    id = Column(BigInteger, primary_key=True, index=True)
    mother_id_OID = Column(ForeignKey(u'nms_mcts_mothers.id'), index=True)
    beneficiaryId = Column(String(255, u'utf8_bin'), unique=True)
    district_id_OID = Column(ForeignKey(u'nms_districts.id'), index=True)
    healthBlock_id_OID = Column(ForeignKey(u'nms_health_blocks.id'), index=True)
    healthFacility_id_OID = Column(ForeignKey(u'nms_health_facilities.id'), index=True)
    healthSubFacility_id_OID = Column(ForeignKey(u'nms_health_sub_facilities.id'), index=True)
    name = Column(String(255, u'utf8_bin'))
    primaryHealthCenter_id_OID = Column(ForeignKey(u'nms_health_facilities.id'), index=True)
    state_id_OID = Column(ForeignKey(u'nms_states.id'), index=True)
    taluka_id_OID = Column(ForeignKey(u'nms_talukas.id'), index=True)
    village_id_OID = Column(ForeignKey(u'nms_villages.id'), index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))

    nms_district = relationship(u'NmsDistrict')
    nms_health_block = relationship(u'NmsHealthBlock')
    nms_health_facility = relationship(u'NmsHealthFacility', primaryjoin='NmsMctsChildren.healthFacility_id_OID == NmsHealthFacility.id')
    nms_health_sub_facility = relationship(u'NmsHealthSubFacility')
    nms_mcts_mother = relationship(u'NmsMctsMother')
    nms_health_facility1 = relationship(u'NmsHealthFacility', primaryjoin='NmsMctsChildren.primaryHealthCenter_id_OID == NmsHealthFacility.id')
    nms_state = relationship(u'NmsState')
    nms_taluka = relationship(u'NmsTaluka')
    nms_village = relationship(u'NmsVillage')


class NmsMctsMother(Base):
    __tablename__ = 'nms_mcts_mothers'

    id = Column(BigInteger, primary_key=True, index=True)
    dateOfBirth = Column(DateTime)
    beneficiaryId = Column(String(255, u'utf8_bin'), unique=True)
    district_id_OID = Column(ForeignKey(u'nms_districts.id'), index=True)
    healthBlock_id_OID = Column(ForeignKey(u'nms_health_blocks.id'), index=True)
    healthFacility_id_OID = Column(ForeignKey(u'nms_health_facilities.id'), index=True)
    healthSubFacility_id_OID = Column(ForeignKey(u'nms_health_sub_facilities.id'), index=True)
    name = Column(String(255, u'utf8_bin'))
    primaryHealthCenter_id_OID = Column(ForeignKey(u'nms_health_facilities.id'), index=True)
    state_id_OID = Column(ForeignKey(u'nms_states.id'), index=True)
    taluka_id_OID = Column(ForeignKey(u'nms_talukas.id'), index=True)
    village_id_OID = Column(ForeignKey(u'nms_villages.id'), index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))

    nms_district = relationship(u'NmsDistrict')
    nms_health_block = relationship(u'NmsHealthBlock')
    nms_health_facility = relationship(u'NmsHealthFacility', primaryjoin='NmsMctsMother.healthFacility_id_OID == NmsHealthFacility.id')
    nms_health_sub_facility = relationship(u'NmsHealthSubFacility')
    nms_health_facility1 = relationship(u'NmsHealthFacility', primaryjoin='NmsMctsMother.primaryHealthCenter_id_OID == NmsHealthFacility.id')
    nms_state = relationship(u'NmsState')
    nms_taluka = relationship(u'NmsTaluka')
    nms_village = relationship(u'NmsVillage')


class NmsNationalDefaultLanguage(Base):
    __tablename__ = 'nms_national_default_language'

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(Integer, nullable=False, unique=True, server_default=text("'0'"))
    language_id_OID = Column(ForeignKey(u'nms_languages.id'), nullable=False, index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))

    nms_language = relationship(u'NmsLanguage')


class NmsServiceUsageCap(Base):
    __tablename__ = 'nms_service_usage_caps'
    __table_args__ = (
        Index('UNIQUE_STATE_SERVICE_COMPOSITE_IDX', 'state_id_OID', 'service', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    maxUsageInPulses = Column(Integer, nullable=False)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    service = Column(String(255, u'utf8_bin'))
    state_id_OID = Column(ForeignKey(u'nms_states.id'), index=True)

    nms_state = relationship(u'NmsState')


class NmsState(Base):
    __tablename__ = 'nms_states'

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(BigInteger, nullable=False, unique=True)
    name = Column(String(255, u'utf8_bin'), index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))


class NmsStatesJoinCircle(Base):
    __tablename__ = 'nms_states_join_circles'

    circle_id = Column(ForeignKey(u'nms_circles.id'), primary_key=True, nullable=False, index=True)
    state_id = Column(ForeignKey(u'nms_states.id'), nullable=False, index=True)
    IDX = Column(Integer, primary_key=True, nullable=False)

    circle = relationship(u'NmsCircle')
    state = relationship(u'NmsState')


class NmsSubscriber(Base):
    __tablename__ = 'nms_subscribers'

    id = Column(BigInteger, primary_key=True, index=True)
    callingNumber = Column(BigInteger, nullable=False, unique=True)
    child_id_OID = Column(ForeignKey(u'nms_mcts_children.id'), index=True)
    circle_id_OID = Column(ForeignKey(u'nms_circles.id'), index=True)
    dateOfBirth = Column(DateTime)
    language_id_OID = Column(ForeignKey(u'nms_languages.id'), index=True)
    lastMenstrualPeriod = Column(DateTime)
    mother_id_OID = Column(ForeignKey(u'nms_mcts_mothers.id'), index=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))

    nms_mcts_children = relationship(u'NmsMctsChildren')
    nms_circle = relationship(u'NmsCircle')
    nms_language = relationship(u'NmsLanguage')
    nms_mcts_mother = relationship(u'NmsMctsMother')


class NmsSubscriptionError(Base):
    __tablename__ = 'nms_subscription_errors'

    id = Column(BigInteger, primary_key=True, index=True)
    beneficiaryId = Column(String(255, u'utf8_bin'), index=True)
    contactNumber = Column(BigInteger, nullable=False, index=True)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    packType = Column(String(255, u'utf8_bin'))
    rejectionMessage = Column(String(255, u'utf8_bin'))
    rejectionReason = Column(String(255, u'utf8_bin'))


class NmsSubscriptionPackMessage(Base):
    __tablename__ = 'nms_subscription_pack_messages'

    id = Column(BigInteger, primary_key=True, index=True)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    duration = Column(Integer, nullable=False)
    messageFileName = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    weekId = Column(String(255, u'utf8_bin'))
    messages_id_OWN = Column(ForeignKey(u'nms_subscription_packs.id'), index=True)
    messages_INTEGER_IDX = Column(Integer)

    nms_subscription_pack = relationship(u'NmsSubscriptionPack')


class NmsSubscriptionPack(Base):
    __tablename__ = 'nms_subscription_packs'

    id = Column(BigInteger, primary_key=True, index=True)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    messagesPerWeek = Column(Integer, nullable=False)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    name = Column(String(100, u'utf8_bin'), unique=True)
    owner = Column(String(255, u'utf8_bin'))
    type = Column(String(255, u'utf8_bin'), nullable=False, index=True)
    weeks = Column(Integer, nullable=False)


class NmsSubscription(Base):
    __tablename__ = 'nms_subscriptions'
    __table_args__ = (
        Index('status_endDate_composit_idx', 'status', 'endDate'),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    activationDate = Column(DateTime)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    deactivationReason = Column(String(255, u'utf8_bin'))
    endDate = Column(DateTime)
    firstMessageDayOfWeek = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    needsWelcomeMessageViaObd = Column(BIT(1), nullable=False)
    origin = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    secondMessageDayOfWeek = Column(String(255, u'utf8_bin'))
    startDate = Column(DateTime, index=True)
    status = Column(String(255, u'utf8_bin'), nullable=False, index=True)
    subscriber_id_OID = Column(ForeignKey(u'nms_subscribers.id'), nullable=False, index=True)
    subscriptionId = Column(String(36, u'utf8_bin'), unique=True)
    subscriptionPack_id_OID = Column(ForeignKey(u'nms_subscription_packs.id'), nullable=False, index=True)

    nms_subscriber = relationship(u'NmsSubscriber')
    nms_subscription_pack = relationship(u'NmsSubscriptionPack')


class NmsTaluka(Base):
    __tablename__ = 'nms_talukas'
    __table_args__ = (
        Index('UNIQUE_DISTRICT_CODE', 'district_id_OID', 'code', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    code = Column(String(7, u'utf8_bin'))
    district_id_OID = Column(ForeignKey(u'nms_districts.id', ondelete=u'CASCADE'), nullable=False, index=True)
    identity = Column(Integer, nullable=False)
    name = Column(String(100, u'utf8_bin'))
    regionalName = Column(String(100, u'utf8_bin'))
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    talukas_INTEGER_IDX = Column(Integer)

    nms_district = relationship(u'NmsDistrict')


class NmsVillage(Base):
    __tablename__ = 'nms_villages'
    __table_args__ = (
        Index('UNIQUE_TALUKA_VCODE_SVID', 'taluka_id_OID', 'vcode', 'svid', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    name = Column(String(50, u'utf8_bin'))
    regionalName = Column(String(50, u'utf8_bin'))
    svid = Column(BigInteger, nullable=False)
    taluka_id_OID = Column(ForeignKey(u'nms_talukas.id', ondelete=u'CASCADE'), nullable=False, index=True)
    vcode = Column(BigInteger, nullable=False)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))
    villages_INTEGER_IDX = Column(Integer)

    nms_taluka = relationship(u'NmsTaluka')


class NmsWhitelistEntry(Base):
    __tablename__ = 'nms_whitelist_entries'
    __table_args__ = (
        Index('UNIQUE_STATE_CONTACT_NUMBER_COMPOSITE_IDX', 'state_id_OID', 'contactNumber', unique=True),
    )

    id = Column(BigInteger, primary_key=True, index=True)
    contactNumber = Column(BigInteger, nullable=False)
    creationDate = Column(DateTime, nullable=False)
    creator = Column(String(255, u'utf8_bin'), nullable=False)
    modificationDate = Column(DateTime, nullable=False)
    modifiedBy = Column(String(255, u'utf8_bin'), nullable=False)
    owner = Column(String(255, u'utf8_bin'))
    state_id_OID = Column(ForeignKey(u'nms_states.id'), index=True)

    nms_state = relationship(u'NmsState')


class NmsWhitelistedState(Base):
    __tablename__ = 'nms_whitelisted_states'

    id = Column(BigInteger, primary_key=True, index=True)
    state_id_OID = Column(ForeignKey(u'nms_states.id'), nullable=False, unique=True)
    creationDate = Column(DateTime)
    creator = Column(String(255, u'utf8_bin'))
    modificationDate = Column(DateTime)
    modifiedBy = Column(String(255, u'utf8_bin'))
    owner = Column(String(255, u'utf8_bin'))

    nms_state = relationship(u'NmsState')
