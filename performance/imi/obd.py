#
# Listens to the same http endpoints IMI will in production reads OBD files - fake calls - generates CDR and CSR files
#

import os
from flask import Flask, render_template, request, flash, send_from_directory
import argparse
import csv
from random import randint


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
def obd():

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
    if os.path.isfile(obd_file(file_name)):
        resp = read_obd_file(file_name)
    else:
        print "{} is not a file".format(obd_file(file_name))
        errors.append("Invalid file: {}".format(file_name))

    if len(errors) > 0:
        return render_template('400.html', errors=errors), 400

    return render_template('resp.html', resp=resp)


def obd_file(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.obdfiles, name)


def cdr_file(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.cdrfiles, "cdrDetail_{}".format(name))


def csr_file(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.cdrfiles, "cdrSummary_{}".format(name))


def make_call_id():
    return "CID-{:10d}".format(randint(0, 9999999999))


def write_cdr_row(obd, writer, call_id, attempt, successful):
    cdr = {
        'RequestId': obd['request_id'],
        'Msisdn': obd['msisdn'],
        'CallId': call_id,
        'AttemptNo': attempt,
        'CallStartTime': "",
        'CallAnswerTime': "",
        'CallEndTime': "",
        'CallDurationInPulses': "",
        'CallStatus': "",
        'LanguageLocationId': "",
        'ContentFile': ""
    }
    writer.writerow(cdr)


def obd_header():
    return ['RequestId', 'ServiceId', 'Msisdn', 'Cli', 'Priority', 'CallFlowURL', 'ContentFileName', 'WeekId',
            'LanguageLocationCode', 'Circle', 'SubscriptionOrigin']


def cdr_header():
    return ['RequestId', 'Msisdn', 'CallId', 'AttemptNo', 'CallStartTime', 'CallAnswerTime', 'CallEndTime',
            'CallDurationInPulses', 'CallStatus', 'LanguageLocationId', 'ContentFile']


def csr_header():
    return ['RequestId', 'ServiceId', 'Msisdn', 'Cli', 'Priority', 'CallFlowURL', 'ContentFileName', 'WeekId',
            'LanguageLocationCode', 'Circle', 'FinalStatus', 'StatusCode', 'Attempts']


#
# pretend call - write to CDR and CSR
#
def mock_call(obd, cdrwriter, csrwriter):
    global args

    print "calling {}".format(obd)

    cdr_lines = 0

    call_id = make_call_id()

    attempt = 0

    # first some retries
    if randint(0, 100) <= args.retry:
        for i in range(0, randint(0, args.maxretries)):
            attempt += 1
            write_cdr_row(obd, cdrwriter, call_id, attempt, False)
            cdr_lines += 1

    # and then ultimately, succeed or fail...
    if randint(0, 100) > args.failure:

        cdrwriter.writerow(["{} - success".format(obd['subscription_id'])])
        csrwriter.writerow(["{} - success".format(obd['subscription_id'])])
    else:
        cdrwriter.writerow(["{} - failure".format(obd['subscription_id'])])
        csrwriter.writerow(["{} - failure".format(obd['subscription_id'])])

    return cdr_lines + 1


def parse_obd_row(row):
    d = {}
    d['request_id'] = row[0]
    d['service_id'] = row[1]
    d['msisdn'] = row[2]
    d['cli'] = row[3]
    d['priority'] = row[4]
    d['call_flow_url'] = row[5]
    d['content_file_name'] = row[6]
    d['week_id'] = row[7]
    d['language_location_code'] = row[8]
    d['circle'] = row[9]
    d['subsrciption_origin'] = row[10]
    return d


def read_obd_file(name):

    cdr_lines = 0
    csr_lines = 0

    try:

        print "OBD: {}".format(obd_file(name))

        # iterate over OBD file
        with open(obd_file(name), 'r') as obdfile, \
                open(cdr_file(name), 'w') as cdrfile, \
                open(csr_file(name), 'w') as csrfile:

            reader = csv.reader(obdfile)

            cdrwriter = csv.writer(cdrfile)
            cdrwriter.writerow(cdr_header())

            csrwriter = csv.writer(csrfile)
            csrwriter.writerow(csr_header())

            if reader.next() != obd_header():
                raise ImportError("Invalid header")

            for row in reader:
                cdr_lines += mock_call(parse_obd_row(row), cdrwriter, csrwriter)
                csr_lines += 1


        # send obdFileProcessedStatusNotification request to MOTECH after OBD file was 'checked'

        #todo

        # wait xxx (to mock how long it takes to call everybody) before sending the next request

        #todo

        # send cdrFileNotification request to MOTECH when entire OBD file was 'called'

        #todo

    except Exception as e:
        print "### EXCEPTION\n{}\n### EXCEPTION".format(e.message)
        return "### EXCEPTION: {}".format(e.message)

    return "{} ({})\n{} ({})".format(cdr_file(name), cdr_lines, csr_file(name), csr_lines)



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
    args = parser.parse_args()

    #debug
    print "args={}".format(args)

    app.secret_key = 'utvsm9blavlu+1o18d0n9_xe5$&^ulw6d82gr*q&t&azwe-gf$'
    app.run(host='0.0.0.0')