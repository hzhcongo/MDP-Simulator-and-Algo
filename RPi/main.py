import threading
import time
# from Arduino import *
import bluetooth_final
import web_final
from arduino2 import *
import numpy as np
import Constants

# arduino
arduino_thread = arduinoMod()
arduinoLock = threading.Lock()
arduinoHasData = False
arduino_message = ""
# WiFi
pcLock = threading.Lock()
pcHasData = False
pc_Message = ""
# bluetooth
blueToothHasData = False
bluetooth_message = ""
blueToothLock = threading.Lock()

# Algo
# Int representations of a cell
# -1 = Error
# 0 = Initialized and unexplored
# 1 = UNExplored - not walkable due to wall
# 2 = Explored - walkable
# 3 = Explored - not walkable due to obstacle
# 4 = Explored - not walkable and is walked
# 5 = Explored - walkable and is walked
mapper = np.zeros([Constants.MAP_ROWS, Constants.MAP_COLS])
areaExplored = 0
botRow = 1
botCol = 1
botDir = Constants.NORTH

def main():
    web_final.start()
    print("Reach here ")
    bluetooth_final.connectToBluetooth()
    arduino_thread.connect_serial()
    startAllThreads()
    keep_self_alive()

def startAllThreads():
    networkRead = threading.Thread(target=readPC)
    networkWrite = threading.Thread(target=sendPC)
    arduinoRead = threading.Thread(target=read_arduino)
    arduinoWrite = threading.Thread(target=write_arduino)
    blueToothWrite = threading.Thread(target=write_bluetooth)
    bluetoothRead = threading.Thread(target=read_bluetooth)
    # Declare threads as daemon to terminate the threads when the main process is killed
    arduinoRead.daemon = True
    arduinoWrite.daemon = True
    networkRead.daemon = True
    networkWrite.daemon = True
    bluetoothRead.daemon = True
    blueToothWrite.daemon = True
    arduinoRead.start()
    arduinoWrite.start()
    networkWrite.start()
    networkRead.start()
    blueToothWrite.start()
    bluetoothRead.start()

def keep_self_alive():
    global arduino_thread
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        bluetooth_final.close_bt_socket()
        arduino_thread.disconnect_serial()
        web_final.stop()

def read_bluetooth():
    global pcLock
    global pcHasData
    global pc_Message
    global arduinoHasData
    global arduino_message
    global arduinoLock
    while True:
        request = bluetooth_final.read_Bluetooth()
        if (request):
            # START OF EDIT
            if request == 0:
                print("Setting start zone as explored and walked")
                mapper[0][0] = 2
                mapper[0][1] = 2
                mapper[0][2] = 2
                mapper[1][0] = 2
                mapper[1][1] = 2
                mapper[1][2] = 2
                mapper[2][0] = 2
                mapper[2][1] = 2
                mapper[2][2] = 2

                areaExplored = calculateAreaExplored()
                print("Explored Area: " + areaExplored)

                print("Executing exploration")
                doExploration()

            elif request == 1:
                print("Executing fastest path")

            elif request == '2.9.9':
                print("Setting waypoint")

            elif request == 3:
                print("Manual override")

            else:
                print("ERROR: android message received not correct")
                # ASK: Tell android message received not correct?

            # END OF EDIT
            pcLock.acquire()
            pcHasData = True
            pc_Message = request
            # audrino send
            arduinoLock.acquire()
            arduino_message = request
            arduinoHasData = True
            print("[receive from bluetooth] Recieved, '", request, "'")

            pcLock.release()

def write_bluetooth():
    global bluetooth_message
    global blueToothHasData
    global blueToothLock
    while True:

        if (blueToothHasData):
            blueToothLock.acquire()
            blueToothHasData = False
            bluetooth_final.send_Bluetooth(bluetooth_message)
            print("[send to bluetooth] Recieved, '", bluetooth_message, "'")
            bluetooth_message = ""
            blueToothLock.release()

def readPC():
    global blueToothLock
    global blueToothHasData
    global bluetooth_message
    global arduinoHasData
    global arduino_message
    global arduinoLock
    while True:
        request = web_final.read_from_pc()
        print("[Web] Recieved, '", request, "'")
        if (request):
            blueToothLock.acquire()
            blueToothHasData = True
            bluetooth_message = request
            # arduino sent
            arduinoLock.acquire()
            arduino_message = request
            arduinoHasData = True
            blueToothLock.release()
            arduinoLock.release()

def sendPC():
    global pcHasData
    global pc_Message
    global pcLock
    while True:
        if (pcHasData):
            pcLock.acquire()
            web_final.write_to_pc(pc_Message)
            pcHasData = False
            pc_Message = ""
            pcLock.release()

def read_arduino():
    global pcHasData
    global pc_Message
    global pcLock
    global blueToothHasData
    global bluetooth_message
    global blueToothLock
    global arduino_thread
    while True:
        print("[Main] Waiting for arduino read")
        request = arduino_thread.read_from_arduino()
        if (request):
            print("[From Arduino] Recieved, '", request, "'")
            pcLock.acquire()
            blueToothLock.acquire()
            pcHasData = True
            pc_Message = request
            blueToothHasData = True
            bluetooth_message = request
            pcLock.release()
            blueToothLock.release()

def write_arduino():
    global arduinoLock
    global arduinoHasData
    global arduino_message
    global arduino_thread
    while True:
        if (arduinoHasData):
            arduinoLock.acquire()
            arduinoHasData = False
            arduino_thread.write_to_arduino(arduino_message)
            arduino_message = ""
            arduinoLock.release()

def calculateAreaExplored():
    result = 0
    r = 0
    while r < Constants.MAP_ROWS:
        c = 0
        while c < Constants.MAP_COLS:
            if mapper[r][c] > 1:
                result += 1
            c += 1
        r += 1
    print("Area explored: " + areaExplored)
    return result

def doExploration(time_limit, coverage):
    print('Exploration Started !')
    startTime = time.time()
    elapsedTime = startTime - time.time()
    while (elapsedTime <= time_limit and areaExplored < int(coverage)):
        # ASK: HOW DO BELOW?
        print('Tell Arduino to sense and return surroundings')
        arduinoHasData = True
        arduino_message = "Sense and return surroundings"

        print('Wait for Arduino to return surrounding conditions')
        while(not request):
            request = arduino_thread.read_from_arduino()

        print('Update map with recieved Arduino data: ' + request)
        updateMap(request)

        nextMove()

        areaExplored = calculateAreaExplored()
        if areaExplored >= 300:
            break
    goHome()

def updateMap(request):
    try:
        # request[x] + 1 as corresponding value markers is 2 and 3
        # checks for invalid indexes
        if(botDir == Constants.NORTH):
            if botCol > 1:
                mapper[botRow + 1][botCol - 2] = request[0] + 2
            if botRow < 18:
                mapper[botRow + 2][botCol - 1] = request[1] + 2
                mapper[botRow + 2][botCol] = request[2] + 2
                mapper[botRow + 2][botCol + 1] = request[3] + 2
            if botCol < 13:
                mapper[botRow + 1][botCol + 2] = request[4] + 2
        elif(botDir == Constants.SOUTH):
            if botCol < 13:
                mapper[botRow - 1][botCol + 2] = request[0] + 2
            if botRow > 1:
                mapper[botRow - 2][botCol + 1] = request[1] + 2
                mapper[botRow - 2][botCol] = request[2] + 2
                mapper[botRow - 2][botCol - 1] = request[3] + 2
            if botCol > 1:
                mapper[botRow - 1][botCol - 2] = request[4] + 2
        elif(botDir == Constants.EAST):
            if botRow < 18:
                mapper[botRow + 2][botCol + 1] = request[0] + 2
            if botCol < 13:
                mapper[botRow + 1][botCol + 2] = request[1] + 2
                mapper[botRow][botCol + 2] = request[2] + 2
                mapper[botRow - 1][botCol + 2] = request[3] + 2
            if botRow > 1:
                mapper[botRow - 2][botCol + 1] = request[4] + 2
        elif(botDir == Constants.WEST):
            if botRow > 1:
                mapper[botRow - 2][botCol - 1] = request[0] + 2
            if botCol > 1:
                mapper[botRow + 1][botCol - 2] = request[1] + 2
                mapper[botRow][botCol - 2] = request[2] + 2
                mapper[botRow - 1][botCol - 2] = request[3] + 2
            if botRow < 18:
                mapper[botRow + 2][botCol - 1] = request[4] + 2
    except ValueError:
        print('Non-numeric data found in the file.')
    except ImportError:
        print "NO module found"
    except EOFError:
        print('Why did you do an EOF on me?')
    except KeyboardInterrupt:
        print('You cancelled the operation.')
    except:
        print('An error occured.')

# Start of actual execution
main()
# End of actual execution
