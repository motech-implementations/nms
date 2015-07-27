#
# Listens to the same http endpoints IMI will in production reads OBD files - fake calls - generates CDR and CSR files
#

import os
from flask import Flask, render_template, request, flash, send_from_directory
import argparse
import csv


app = Flask(__name__)
app.secret_key = 'utvsm9blavlu+1o18d0n9_xe5$&^ulw6d82gr*q&t&azwe-gf$'


@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'), 'favicon.ico', mimetype='image/vnd.microsoft.icon')


@app.route('/')
def home():

    stats = {
        'foo': 'bar',
        'baz': 123,
    }
    return render_template('index.html', stats=stats)


@app.route('/obd', methods=['POST'])
def obd():

    errors = []
    required_fields = []

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

    file_name = targetFileNotification['fileName']
    if os.path.isfile(obd_file(file_name)):
        read_obd_file(file_name)
    else:
        print "{} is not a file".format(obd_file(file_name))
        errors.append("Invalid file: {}".format(file_name))

    if len(errors) > 0:
        return render_template('400.html', errors=errors), 400

    return render_template('resp.html', stats={})


def obd_file(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.obdfiles, name)


def cdr_file(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.cdrfiles, "cdrDetail_{}".format(name))


def csr_file(name):
    homeDir = os.path.expanduser("~")
    return os.path.join(homeDir, args.cdrfiles, "cdrSummary_{}".format(name))


#
# pretend call - write to CDR and CSR
#
def mock_call(row, cdrwriter, csrwriter):
    print "calling {}".format(row)

    #todo: really dumb right now but planning on doing something better...
    cdrwriter.writerow(row)
    csrwriter.writerow(row)


def read_obd_file(name):

    # create CDR file
    # create CSR file

    try:

        # iterate over OBD file
        with open(obd_file(name), 'r') as obdfile, \
                open(cdr_file(name), 'w') as cdrfile, \
                open(csr_file(name), 'w') as csrfile:
            reader = csv.reader(obdfile)
            cdrwriter = csv.writer(cdrfile)
            csrwriter = csv.writer(csrfile)
            for row in reader:
                mock_call(row, cdrwriter, csrwriter)

        # send obdFileProcessedStatusNotification request to MOTECH after OBD file was 'checked'

        #todo

        # wait xxx (to mock how long it takes to call everybody) before sending the next request

        #todo

        # send cdrFileNotification request to MOTECH when entire OBD file was 'called'

        #todo

    except Exception as e:
        print "Exception! {}".format(e.message)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-s", "--server", help="MOTECH URL", default="http://localhost:8080/motech-platform-server")
    parser.add_argument("-o", "--obdfiles", help="location of the OBD files", default="obd-files-remote")
    parser.add_argument("-c", "--cdrfiles", help="location of the CDR files", default="cdr-files-remote")
    args = parser.parse_args()

    #debug
    print "args={}".format(args)

    app.run(host='0.0.0.0')