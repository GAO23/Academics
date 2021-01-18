password for key is cse363offsec2020.
I decompiled the source code in Ghidra and then found the function that generates the key from a given random string. 
I then simply write my own c program(see main.c), copy and paste that key generation function and the random string to generate the password. Check the key.png for screenshot of my Ghidra reverse engineering and then main.c for the function that I found.

For number, I can't crack it :(
I decompiled the bindary in Ghidra just like key, see number.png for a screen of what I did in Ghidra. I renamed all the functions and varibles and copy and paste  it into my own c program(see main.c) to make it understandble  

I still can't figure out the number however since whatever input I use will not yield /003 as the first byte which won't compare to C when comparing. All I know is that the first two numbers have to be 06 and the program will compare it to CSE363ESC at the end of the function when each byte is transform. I am hoping for partial credits. 
