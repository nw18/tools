import HttpServer
import signal


def debug_quit(signum, frame):
    server.stopServer()


signal.signal(signal.SIGINT, debug_quit)
signal.signal(signal.SIGTERM, debug_quit)
server = HttpServer.HttpServer()
server.setupServer()
exit(0)
