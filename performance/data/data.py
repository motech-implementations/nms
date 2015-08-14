import argparse
import unirest
import sys
import os


def data_file(name):
    this_dir = os.path.split(__file__)[0]
    return os.path.join(this_dir, "files", name)


def exec_http_get(url, params=None):
    if args.verbose:
        print "GET url      = {}".format(url)
        print "GET params   = {}".format(params)

    response = unirest.post(url=url, params=params)

    if args.verbose:
        print "response code = {}".format(response.code)

    if args.debug:
        print "response body = {}".format(response.body)

    if response.code != 200:
        print "### ERROR ###"
        print "Expecting HTTP 200 but received {}".format(response.code)
        sys.exit(1)


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
    parser.add_argument("--cleardb", help="clear database", action="store_true")
    parser.add_argument("--debug", help="debug mode, even more verbose", action="store_true")
    parser.add_argument("--mctsmoms", help="number of MCTS mothers", type=int, default=0)
    args = parser.parse_args()

    if args.verbose:
        print "verbose mode\nargs={}".format(args)

    #
    # http defaults
    #
    unirest.timeout(300)
    unirest.default_header("Accept", "application/json")

    #
    # Clear the database?
    #
    if args.cleardb:
        exec_http_get("{}/module/testing/clearDatabase".format(args.server))
        exec_http_get("{}/module/testing/createSubscriptionPacks".format(args.server))
        import_region_domain_data("state")
        import_region_domain_data("circle")
        import_region_domain_data("district")
        import_domain_data("languageLocationCode/import", "language_location_code.csv")

    #
    # Create MCTS mothers file?
    #
    if args.mctsmoms > 0:
        exec_http_get("{}/module/testing/createMctsMoms".format(args.server), {'count': args.mctsmoms})
