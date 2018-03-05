import serial
import time

class arduinoMod():
	def __init__(self):
		self.usb_port = '/dev/ttyACM0'
		#self.usb_port = '/dev/ttyACM1'
		self.baud_rate = 115200
		self.ser = None
	def connect_serial(self):
		print("Arduino is trying to connect...")
		try:
			# Socket connection
			self.ser = serial.Serial(self.usb_port,self.baud_rate)
			print("Arduino is connected!")
		except Exception in e:
			print("Error! Arduino connection failed!")
			
	def disconnect_serial(self):
		if(self.ser):
			self.ser.close()
			print("Arduino Connection is Closed!")
	def write_to_arduino(self,message):
		try:
			#print("Write to Arduino message: %i" % message) 
			#message = message.encode('utf-8') 
			self.ser.write('4')
			#self.ser.write(message)
			print("Write to arduino: %s " % message)
		except TypeError:
			print("Error, cannot write to Arduino")
	def read_from_arduino(self):
		try:
			#self.ser.flush()
			received_data = self.ser.readline()
			if(received_data):
				print("Read from Arduino: %s" % received_data)
				return str(received_data)
		except Exception in e:
			print("No data received")
	
#if __name__ == "__main__":
#	print ("Starting...")
#	sr = arduinoMod()
#	sr.connect_serial()
	#rpiInput = raw_input() rpiInput = int(input("Instruction 0-8 : "))
	#rpiInput = "Writing to arduino..."
	#print("Writing to arduino : %s " % rpiInput)
	#sr.write_to_arduino(rpiInput)
#	print ("\nReading...")
#	print ("Data received %s" % sr.read_from_arduino())
#	time.sleep(1)
	#print "Disconnect" sr.disconnect_serial()
