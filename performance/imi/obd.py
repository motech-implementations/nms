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
CALL_DURATION_IN_PULSES = 'CallDurationInPulses'
CALL_STATUS = 'CallStatus'
STATUS_CODE = 'StatusCode'
FINAL_STATUS = 'FinalStatus'
ATTEMPTS = 'Attempts'


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


@app.route('/obd', methods=['POST'])
def handle_obd():

    required_fields = []
    errors = []

    targetFileNotification = request.get_json()

    print "/obd"
    print "targetFileNotification = {}".format(targetFileNotification)

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
call_status_success = [1001]

# OBD_FAILED_NOATTEMPT(2000),
# OBD_FAILED_BUSY(2001),
# OBD_FAILED_NOANSWER(2002),
# OBD_FAILED_SWITCHEDOFF(2003),
# OBD_FAILED_INVALIDNUMBER(2004),
# OBD_FAILED_OTHERS(2005),
call_status_failure = [2000, 2001, 2002, 2003, 2004, 2005]

# OBD_DNIS_IN_DND(3001);
call_status_reject = [3001]


def make_call_status(successful):

    if successful:
        return choice(call_status_success)

    return choice(call_status_failure)


def write_cdr_row(obd, writer, call_id, attempt, successful):

    if successful:
        call_status = choice(call_status_success)
        call_start_time = 123
        call_answer_time = 234
        call_end_time = 456
        call_duration = 10
    else:
        call_status = choice(call_status_failure)
        call_start_time = 123
        call_answer_time = ""
        call_end_time = ""
        call_duration = ""

    # cdr = {
    #     'RequestId': obd['request_id'],
    #     'Msisdn': obd['msisdn'],
    #     'CallId': call_id,
    #     'AttemptNo': attempt,
    #     'CallStartTime': call_start_time,
    #     'CallAnswerTime': call_answer_time,
    #     'CallEndTime': call_end_time,
    #     'CallDurationInPulses': call_duration,
    #     'CallStatus': call_status,
    #     'LanguageLocationId': "",
    #     'ContentFile': ""
    # }
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
        call_start_time,
        # CallAnswerTime
        call_answer_time,
        # CallEndTime
        call_end_time,
        # CallDurationInPulses
        call_duration,
        # CallStatus
        call_status,
        # LanguageLocationId
        obd[LANGUAGE_LOCATION_CODE],
        # ContentFile
        obd[CONTENT_FILE]
    ])


def obd_header():
    return [REQUEST_ID, SERVICE_ID, MSISDN, CLI, PRIORITY, CALL_FLOW_URL, CONTENT_FILE_NAME, WEEK_ID,
            LANGUAGE_LOCATION_CODE, CIRCLE, SUBSCRIPTION_ORIGIN]


def cdr_header():
    return [REQUEST_ID, MSISDN, CALL_ID, ATTEMPT_NO, CALL_START_TIME, CALL_ANSWER_TIME, CALL_END_TIME,
            CALL_DURATION_IN_PULSES, CALL_STATUS, LANGUAGE_LOCATION_ID, CONTENT_FILE]


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
        for i in range(0, randint(0, args.maxretries)):
            attempt += 1
            write_cdr_row(obd, cdr_writer, call_id, attempt, False)
            cdr_lines += 1

    # and then ultimately, succeed or fail...
    success = randint(0, 100) > args.failure
    write_cdr_row(obd, cdr_writer, call_id, attempt + 1, success)
    csr_writer.writerow(["{}".format(obd[REQUEST_ID])])

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
    #return subprocess.check_output(["echo", "foobar"])
    return "checksum123"


def record_count(file):
    return 123


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

        print "sending POST request: {}".format(obd_file_processed_url())

        headers = {'Content-Type': 'application/json'}
        request = urllib2.Request(obd_file_processed_url(), headers=headers)
        data = {u'fileProcessedStatus': 8000, u'fileName': name}
        response = urllib2.urlopen(request, json.dumps(data))
        print "{} POST response: {}".format(obd_file_processed_url(), response)

        # wait (to mock how long it takes to call everybody) before sending the next request

        time.sleep(args.wait)

        # send cdrFileNotification request to MOTECH when entire OBD file was 'called'

        print "sending POST request: {}".format(cdr_file_notification_url())

        data = {
            u'fileName': name,
            u'cdrDetail': {
                u'cdrFile': cdr_file(name),
                u'checksum': checksum(cdr_file_path(name)),
                u'recordsCount': record_count(cdr_file_path(name))
            },
            u'cdrSummary': {
                u'cdrFile': csr_file(name),
                u'checksum': checksum(csr_file_path(name)),
                u'recordsCount': record_count(cdr_file_path(name))
            }
        }
        json_string = json.dumps(data)
        request = urllib2.Request(cdr_file_notification_url(), headers=headers, data=json_string)
        response = urllib2.urlopen(request)
        print "{} POST response: {}".format(cdr_file_notification_url(), response)

    except Exception as e:
        error = "### EXCEPTION: {} ###".format(e.message)
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