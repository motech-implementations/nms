#
# Listens to the same http endpoints IMI will in production reads OBD files - fake calls - generates CDR and CSR files
#

import os
from flask import Flask, render_template, request, flash, send_from_directory
import argparse
import csv
from random import randint, choice
import urllib2
import json
import time
import datetime
import subprocess


#
# field names
#
REQUEST_ID = 'RequestId'
SERVICE_ID = 'ServiceId'
CALL_ID = 'CallId'
MSISDN = 'Msisdn'
CLI = 'Cli'
PRIORITY = 'Priority'
CALL_FLOW_URL = 'CallFlowURL'
CONTENT_FILE_NAME = 'ContentFileName'
CONTENT_FILE = 'ContentFile'
WEEK_ID = 'WeekId'
LANGUAGE_LOCATION_CODE = 'LanguageLocationCode'
LANGUAGE_LOCATION_ID = 'LanguageLocationId'
CIRCLE = 'Circle'
SUBSCRIPTION_ORIGIN = 'SubscriptionOrigin'
ATTEMPT_NO = 'AttemptNo'
CALL_START_TIME = 'CallStartTime'
CALL_ANSWER_TIME = 'CallAnswerTime'
CALL_END_TIME = 'CallEndTime'
CALL_DURATION_IN_PULSE = 'CallDurationInPulse'
CALL_STATUS = 'CallStatus'
STATUS_CODE = 'StatusCode'
FINAL_STATUS = 'FinalStatus'
ATTEMPTS = 'Attempts'
MSG_PLAY_START_TIME = 'MsgPlayStartTime'
MSG_PLAY_END_TIME = 'MsgPlayEndTime'
CIRCLE_ID = 'CircleId'
OPERATOR_ID = 'OperatorId'
CALL_DISCONNECT_REASON = 'CallDisconnectReason'


#
# Final Status Codes
#

FS_SUCCESS = 1
FS_FAILED = 2
FS_REJECTED = 3


app = Flask(__name__)


@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'), 'favicon.ico',
                               mimetype='image/vnd.microsoft.icon')


@app.route('/')
def home():

    stats = {
        'foo': 'bar',
        'baz': 123,
    }
    return render_template('index.html', stats=stats)


@app.route('/cdr', methods=['POST'])
def handle_cdr():

    print "/cdr"
    print "request = {}".format(request.get_json())

    return render_template('resp.html', resp="OK")


@app.route('/obd', methods=['POST'])
def handle_obd():

    required_fields = []
    errors = []

    targetFileNotification = request.get_json()

    print "/obd"
    print "request = {}".format(targetFileNotification)

    if 'fileName' not in targetFileNotification:
        required_fields.append("fileName")
    if 'checksum' not in targetFileNotification:
        required_fields.append("checksum")
    if 'recordsCount' not in targetFileNotification:
        required_fields.append("recordsCount")

    if len(required_fields) > 0:
        errors.append("required {} missing: {}".format("fields" if len(required_fields) > 1 else "field",
                                                       ', '.join(required_fields)))

    if len(errors) > 0:
        return render_template('400.html', errors=errors), 400

    resp = ''
    file_name = targetFileNotification['fileName']
    if os.path.isfile(obd_file_path(file_name)):
        resp = read_obd_file(file_name)
    else:
        print "{} is not a file".format(obd_file_path(file_name))
        errors.append("Invalid file: {}".format(file_name))

    if len(errors) > 0:
        return render_template('400.html', errors=errors), 400

    return render_template('resp.html', resp=resp)


def obd_file_path(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.obdfiles, name)


def cdr_file(name):
    return "cdrDetail_{}".format(name)


def csr_file(name):
    return "cdrSummary_{}".format(name)


def cdr_file_path(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.cdrfiles, cdr_file(name))


def csr_file_path(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.cdrfiles, csr_file(name))


def make_call_id():
    return "CID-{:10d}".format(randint(0, 9999999999))


# OBD_SUCCESS_CALL_CONNECTED(1001),
successful_call_statuses = [1001]


# OBD_FAILED_NOATTEMPT(2000),
# OBD_FAILED_BUSY(2001),
# OBD_FAILED_NOANSWER(2002),
# OBD_FAILED_SWITCHEDOFF(2003),
# OBD_FAILED_INVALIDNUMBER(2004),
# OBD_FAILED_OTHERS(2005),
failed_call_statuses = [2000, 2001, 2002, 2003, 2004, 2005]


# Normal Drop: 1
successful_disconnect_statuses = [1]


# VXML Runtime exception: 2
# Content Not found: 3
# Usage Cap exceeded: 4
# Error in the API: 5
# System Error: 6
failed_disconnect_statuses = [2, 3, 4, 5, 6]


operators = ['D', 'A', 'B', 'L', 'C', 'H', 'I', 'M', 'R', 'E', 'S', 'Y', 'P', 'W', 'T', 'U', 'V']


def some_time_today():
    today = datetime.datetime.now() - datetime.timedelta(hours = choice(range(10)), minutes = choice(range(50)))
    epoch = datetime.datetime(1970,1,1)
    return (today - epoch).total_seconds()


def write_cdr_row(obd, writer, call_id, attempt, status, start_time, answer_time, end_time, duration, operator):

    msg_play_start_time = some_time_today()
    msg_play_end_time = msg_play_start_time + duration # assuming that duration is in seconds
    call_disconnect_reason = choice(successful_disconnect_statuses) if status in successful_call_statuses \
        else choice(failed_disconnect_statuses)

    # REQUEST_ID, MSISDN, CALL_ID, ATTEMPT_NO, CALL_START_TIME, CALL_ANSWER_TIME, CALL_END_TIME,
    # CALL_DURATION_IN_PULSE, CALL_STATUS, LANGUAGE_LOCATION_ID, CONTENT_FILE, MSG_PLAY_START_TIME, MSG_PLAY_END_TIME,
    # CIRCLE_ID, OPERATOR_ID, PRIORITY, CALL_DISCONNECT_REASON, WEEK_ID

    writer.writerow([
        # RequestId
        obd[REQUEST_ID],
        # Msisdn
        obd[MSISDN],
        # CallId
        call_id,
        # AttemptNo
        attempt,
        # CallStartTime
        start_time,
        # CallAnswerTime
        answer_time,
        # CallEndTime
        end_time,
        # CallDurationInPulse
        duration,
        # CallStatus
        status,
        # LanguageLocationId
        obd[LANGUAGE_LOCATION_CODE],
        # ContentFile
        obd[CONTENT_FILE],
        # MSG_PLAY_START_TIME
        msg_play_start_time,
        # MSG_PLAY_END_TIME,
        msg_play_end_time,
        # CIRCLE_ID
        obd[CIRCLE],
        # OPERATOR_ID
        operator,
        # PRIORITY
        obd[PRIORITY],
        # CALL_DISCONNECT_REASON
        call_disconnect_reason,
        # WEEK_ID
        obd[WEEK_ID]
    ])


def write_csr_row(obd, writer, attempts, status):

    # REQUEST_ID, SERVICE_ID, MSISDN, CLI, PRIORITY, CALL_FLOW_URL, CONTENT_FILE_NAME, WEEK_ID, LANGUAGE_LOCATION_CODE,
    # CIRCLE, FINAL_STATUS, STATUS_CODE, ATTEMPTS

    writer.writerow([
        # RequestId
        obd[REQUEST_ID],
        # ServiceId
        obd[SERVICE_ID],
        # Msisdn
        obd[MSISDN],
        # Cli
        obd[CLI],
        # Priority
        obd[PRIORITY],
        # CallFlowURL
        obd[CALL_FLOW_URL],
        # ContentFileName
        obd[CONTENT_FILE],
        # WeekId
        obd[WEEK_ID],
        # LanguageLocationCode
        obd[LANGUAGE_LOCATION_CODE],
        # Circle
        obd[CIRCLE],
        # FinalStatus
        FS_SUCCESS if status in successful_call_statuses else FS_FAILED,
        # StatusCode
        status,
        # Attempts
        attempts
    ])


def obd_header():
    return [REQUEST_ID, SERVICE_ID, MSISDN, CLI, PRIORITY, CALL_FLOW_URL, CONTENT_FILE_NAME, WEEK_ID,
            LANGUAGE_LOCATION_CODE, CIRCLE, SUBSCRIPTION_ORIGIN]


def cdr_header():
    return [REQUEST_ID, MSISDN, CALL_ID, ATTEMPT_NO, CALL_START_TIME, CALL_ANSWER_TIME, CALL_END_TIME,
            CALL_DURATION_IN_PULSE, CALL_STATUS, LANGUAGE_LOCATION_ID, CONTENT_FILE, MSG_PLAY_START_TIME,
            MSG_PLAY_END_TIME, CIRCLE_ID, OPERATOR_ID, PRIORITY, CALL_DISCONNECT_REASON, WEEK_ID]


def csr_header():
    return [REQUEST_ID, SERVICE_ID, MSISDN, CLI, PRIORITY, CALL_FLOW_URL, CONTENT_FILE_NAME, WEEK_ID,
            LANGUAGE_LOCATION_CODE, CIRCLE, FINAL_STATUS, STATUS_CODE, ATTEMPTS]


#
# pretend call - write to CDR and CSR
#
def mock_call(obd, cdr_writer, csr_writer):
    global args

    print "calling {}".format(obd)

    cdr_lines = 0

    call_id = make_call_id()

    attempt = 0


    # first some retries
    if randint(0, 100) <= args.retry:

        status = choice(failed_call_statuses)
        operator = choice(operators)
        for i in range(0, randint(0, args.maxretries)):
            attempt += 1
            write_cdr_row(obd, cdr_writer, call_id, attempt, status, 123, 234, 345, 10, operator)
            cdr_lines += 1


    # and then ultimately, succeed or fail...
    if randint(0, 100) > args.failure:
        status = choice(failed_call_statuses)
    write_cdr_row(obd, cdr_writer, call_id, attempt, status, 123, 234, 345, 10, operator)
    write_csr_row(obd, csr_writer, attempt + 1, status)

    return cdr_lines + 1


def parse_obd_row(row):
    return {
        REQUEST_ID: row[0],
        SERVICE_ID: row[1],
        MSISDN: row[2],
        CLI: row[3],
        PRIORITY: row[4],
        CALL_FLOW_URL: row[5],
        CONTENT_FILE: row[6],
        WEEK_ID: row[7],
        LANGUAGE_LOCATION_CODE: row[8],
        CIRCLE: row[9],
        SUBSCRIPTION_ORIGIN: row[10]
    }


def obd_file_processed_url():
    return "{}/module/imi/obdFileProcessedStatusNotification".format(args.server)


def cdr_file_notification_url():
    return "{}/module/imi/cdrFileNotification".format(args.server)


def checksum(file):
    s = subprocess.check_output(["/usr/bin/md5sum", file])
    return s.split(" ")[0]


def read_obd_file(name):

    cdr_lines = 0
    csr_lines = 0

    try:

        print "OBD: {}".format(obd_file_path(name))

        # iterate over OBD file
        with open(obd_file_path(name), 'r') as obdfile, \
                open(cdr_file_path(name), 'w') as cdrfile, \
                open(csr_file_path(name), 'w') as csrfile:

            obd_reader = csv.reader(obdfile)

            cdr_writer = csv.writer(cdrfile)
            cdr_writer.writerow(cdr_header())

            csr_writer = csv.writer(csrfile)
            csr_writer.writerow(csr_header())

            if obd_reader.next() != obd_header():
                raise ImportError("Invalid header")

            for row in obd_reader:
                cdr_lines += mock_call(parse_obd_row(row), cdr_writer, csr_writer)
                csr_lines += 1


        # send obdFileProcessedStatusNotification request to MOTECH after OBD file was 'checked'


        headers = {'Content-Type': 'application/json'}
        data = {u'fileProcessedStatus': 8000, u'fileName': name}
        json_string = json.dumps(data)
        request = urllib2.Request(obd_file_processed_url(), headers=headers, data=json_string)

        print "POST requ: {}".format(obd_file_processed_url())
        print "POST data: {}".format(json_string)
        response = urllib2.urlopen(request, json.dumps(data))
        print "POST resp: HTTP {} - {}".format(response.code, response.msg)

        # wait (to mock how long it takes to call everybody) before sending the next request

        time.sleep(args.wait)

        # send cdrFileNotification request to MOTECH when entire OBD file was 'called'

        data = {
            u'fileName': name,
            u'cdrDetail': {
                u'cdrFile': cdr_file(name),
                u'checksum': checksum(cdr_file_path(name)),
                u'recordsCount': cdr_lines
            },
            u'cdrSummary': {
                u'cdrFile': csr_file(name),
                u'checksum': checksum(csr_file_path(name)),
                u'recordsCount': csr_lines
            }
        }
        json_string = json.dumps(data)
        request = urllib2.Request(cdr_file_notification_url(), headers=headers, data=json_string)

        print "POST requ: {}".format(cdr_file_notification_url())
        print "POST data: {}".format(json_string)
        response = urllib2.urlopen(request)
        print "POST resp: HTTP {} - {}".format(response.code, response.msg)

    except Exception as e:
        error = "### EXCEPTION: {} ###".format(e)
        print error
        return error

    return "{} ({})\n{} ({})".format(cdr_file_path(name), cdr_lines, csr_file_path(name), csr_lines)



if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-s", "--server", help="MOTECH URL", default="http://localhost:8080/motech-platform-server")
    parser.add_argument("-o", "--obdfiles", help="location of the OBD files", default="obd-files-remote")
    parser.add_argument("-c", "--cdrfiles", help="location of the CDR files", default="cdr-files-remote")
    parser.add_argument("-f", "--failure", help="call failure percentage (0-100)", type=int, choices=range(0,101),
                        default=30, metavar="[0-100]")
    parser.add_argument("-m", "--maxretries", help="maximum number of times a call is retried", type=int, default=3)
    parser.add_argument("-r", "--retry", help="retry percentage (0-100)", type=int, choices=range(0,101),
                        default=50, metavar="[0-100]")
    parser.add_argument("-w", "--wait", help="seconds to wait before cdrFileNotification", type=int, default=0)
    args = parser.parse_args()

    #debug
    print "args={}".format(args)

    app.secret_key = 'utvsm9blavlu+1o18d0n9_xe5$&^ulw6d82gr*q&t&azwe-gf$'
    app.run(host='0.0.0.0')