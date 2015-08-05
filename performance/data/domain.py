import argparse

import unirest

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-s", "--server", help="MOTECH URL", default="http://localhost:8080/motech-platform-server")
    parser.add_argument("-v", "--verbose", help="verbose mode", action="store_true")
    args = parser.parse_args()

    if args.verbose:
        print "verbose mode\nargs={}".format(args)

    response = unirest.get(args.server)

    if args.verbose:
        print "GET {} = {}".format(args.server, response)