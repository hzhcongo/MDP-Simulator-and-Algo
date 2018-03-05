#!/usr/bin
from bluetooth import *
client_sock=0
server_sock=0

def main3():
 try:
     connectToBluetooth()
     while True:
         print("In while loop...")
         data = read_Bluetooth()
         if len(data) == 0:
             break
         print("Received [%s]" % data)
         message="sending from RPI "
         send_Bluetooth(message)
 except IOError:
     pass
 close_bt_socket()
 print("")

def read_Bluetooth():
    try:
        global client_sock
        data = client_sock.recv(1024)  # client socket receives 1024 bytes
        return data
    except BluetoothError:
        print("[Bluetooth] Bluetooth Error.")
        connectToBluetooth()


def send_Bluetooth(message):
    try:
     global client_sock
     client_sock.send(message)
     print("Bluetooth receive: '%s' "% message+"\n" )
    except BluetoothError:
     print("[Bluetooth] Bluetooth Error.")
     connectToBluetooth()

def connectToBluetooth():
    global server_sock
    server_sock = BluetoothSocket(RFCOMM)
    server_sock.bind(("", 4))
    server_sock.listen(1)
    port = server_sock.getsockname()[1]
    #uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    uuid = "00001101-0000-1000-8000-00805F9B34FB"
    advertise_service(server_sock, "MDP-Server",
                      service_id=uuid,
                      service_classes=[uuid, SERIAL_PORT_CLASS],
                      profiles=[SERIAL_PORT_PROFILE],
                      # protocols = [ OBEX_UUID ]
                      )
    print("[Bluetooth] Waiting for connection on RFCOMM channel %d" % port)
    global client_sock
    client_sock, client_info = server_sock.accept()
    print("Accepted connection from ", client_info)

def close_bt_socket():
    global client_sock
    global server_sock
    client_sock.close()
    server_sock.close()
    print("[Bluetooth] Disconnected")

