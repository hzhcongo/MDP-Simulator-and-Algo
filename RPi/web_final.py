import socket
HOST, PORT = '', 2323


def main2():
 webModule = WebModule('pc')
 webModule.start()
 try:
    while True:
     print("In while loop...")
     #check=webModule.write_to_pc()
     #if check ==true :
         #break
     #data = webModule.read_from_pc()
     #print("reach here")
     #if len(data) == 0:
        #break
     #print("Received [%s]" % data)
 except IOError:
   pass
 print("[WEB] Disconnected")
 webModule.stop()
 print("Terminated !!!!!!!!!!!")




listen_socket = None
client_connection = None
name= None

def start():
    try:
        global listen_socket
        global client_connection
        global name
        listen_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        listen_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        listen_socket.bind((HOST, PORT))
        listen_socket.listen(1)
        print ('[Web] Thread: %s, Serving HTTP on port %s' % (name,PORT))
        client_connection, client_address = listen_socket.accept()
        print("[Web] Client connected, hostname, ",client_address  )
    except socket.error as e:
        print("[Web] Connection failed. Restart Program!")

def read_from_pc():
    try:
        global listen_socket
        global client_connection
        request = client_connection.recv(1024)
        if not request:
            raise ValueError('[Web] Data Stream terminated')
        #print(request)
        print("[Web] Recieved, '", request, "'")
        return request
    except socket.error as e:
        print("[Web] Connection terminated. Reconnecting again...")
        listen_socket.close()
        client_connection = None
        start()

def write_to_pc(message):
    try:
     global listen_socket
     global client_connection
     #message="RPI SENDING WEB DATA !!!!!!!!!!!"
     print("[Web] Sending: '%s', to pc." % message)
     print("[Web] Client sented to , ", client_connection)
     client_connection.send(message+"\n")
     print("Message send out")
     return "true"
    except socket.error as e:
        print("[Web] Connection terminated. Reconnecting again...")
        listen_socket.close()
        client_connection = None
        start()

def stop():
    global listen_socket
    global client_connection
    client_connection.close()
    listen_socket.shutdown(1)
    listen_socket.close()

