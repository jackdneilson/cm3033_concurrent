import pexpect
import time

to_input = "BUY BP 1\n"
tel = pexpect.spawn("telnet localhost 9999")
time.sleep(1)
i = 0
while i < 2007:
    tel.send(to_input)
    i += 1
    time.sleep(0.01)
print("finished")
