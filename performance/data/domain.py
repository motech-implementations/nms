import argparse
import unirest
import sys
import os


def data_file(name):
    this_dir = os.path.split(__file__)[0]
    return os.path.join(this_dir, "files", name)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-s", "--server", help="MOTECH URL", default="http://localhost:8080/motech-platform-server")
    parser.add_argument("-v", "--verbose", help="verbose mode", action="store_true")
    parser.add_argument("-k", "--cleardb", help="clear database", action="store_true")
    parser.add_argument("-d", "--debug", help="debug mode, even more verbose", action="store_true")
    args = parser.parse_args()

    if args.verbose:
        print "verbose mode\nargs={}".format(args)

    #
    # Clear the database?
    #
    if args.cleardb:
        url = "{}/module/testing/clearDatabase".format(args.server)

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

    #
    # States
    #
    url = "{}/module/region/data/import/state".format(args.server)
    headers = headers={"Accept": "application/json"}
    params = {"parameter": "value", "csvFile": open(data_file("state.csv"))}

    if args.verbose:
        print "POST url      = {}".format(url)
        print "POST headers  = {}".format(headers)
        print "POST params   = {}".format(params)

    response = unirest.post(url=url, headers=headers, params=params)

    if args.verbose:
        print "response code = {}".format(response.code)

    if args.debug:
        print "response body = {}".format(response.body)

    if response.code == 200:
        sys.exit()

    print "Expected HTTP 200 but received {}".format(response.code)
    sys.exit(1)