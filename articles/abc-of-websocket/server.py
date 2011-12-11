import socket
import struct
import hashlib

class WebSocketSever:
	def __init__(self,  port=1234, path="/"):
		self.path = path
		self.port = port
		self.buffer = " "

	def run(self):
		print "WebSocketSever running on port %d" %self.port
		sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		sock.bind(("", self.port))
		sock.listen(5)
		conn, address = sock.accept()
		headers = {}
		handshaken = False

		while True:
			if handshaken == False:
				self.buffer += conn.recv(1024)
				if self.buffer.find('\r\n\r\n') != -1:
					header, data = self.buffer.split('\r\n\r\n', 1)
					for line in header.split("\r\n")[1:]:
						key, value = line.split(": ", 1)
						headers[key] = value

					headers["Location"] = "ws://%s%s" %(headers["Host"], self.path)
					print headers
					key1 = headers["Sec-WebSocket-Key1"]
					key2 = headers["Sec-WebSocket-Key2"]
					if len(data) < 8:
						data += conn.recv(8-len(data))
					key3 = data[:8]
					self.buffer = data[8:]
					token = self.generate_token(key1, key2, key3)
					print "token:"+token

					handshake = '\
HTTP/1.1 101 Web Socket Protocol Handshake\r\n\
Upgrade: WebSocket\r\n\
Connection: Upgrade\r\n\
Sec-WebSocket-Origin: %s\r\n\
Sec-WebSocket-Location: %s\r\n\r\n\
' %(headers['Origin'], headers['Location'])
					print handshake
					conn.send(handshake+token)
					handshaken = True
			else:
				self.buffer += conn.recv(64)
				if self.buffer.find("\xFF")!=-1:
					s = self.buffer.split("\xFF")[0][1:]
					print "Got msg:%s" %s
					ret = "you just said: %s" %s
					conn.send("\x00%s\xFF" % ret)

	def generate_token(self, key1, key2, key3):
		print "key1:%s\r\nkey2:%s\r\nkey3:%s\r\n" %(key1, key2, key3)
		num1 = int("".join([digit for digit in list(key1) if digit.isdigit()]))
		spaces1 = len([char for char in list(key1) if char == " "])
		num2 = int("".join([digit for digit in list(key2) if digit.isdigit()]))
		spaces2 = len([char for char in list(key2) if char == " "])

		combined = struct.pack(">II", num1/spaces1, num2/spaces2) + key3
		return hashlib.md5(combined).digest()

if __name__ == "__main__":
	server = WebSocketSever()
	server.run()