# Covert C&C and Exfiltration

#### [Demo] _https://www.youtube.com/watch?v=LvgzIZapwLY_
#### https://github.com/HyejunJeong/covert-cnc-exfiltration

This program is a client and a server for covert command and control of, and data exfiltration from, an infected client.

The client is a reverse shell. After an initial execution, the client program will copy itself to the autostart directory so that the script will be executed every time the victim's operating system boots up and run silently in the background. 

AWS cloud server is used and the data to be transfered are encrypted to hide the traffic.

The client (victim machine) sends a request and connects to the attacker-controlled server (AWS cloud server) in the same way it receives the shell commands. Notifications and any status updates are periodically sent to the server. Files are encrypted and transferred out of the client's network through TCP connection.  
Large files are broken up into multiple packets and transferred to the server through TCP connection in order to achieve a reliable in-order data transfer.


#### Requirements
* Linux
* python3

## Getting Started


### Running the Server

1. cd to the project directory, and ssh into aws cloud server. 
    ```shell
    chmod 700 ./aws_key.pem 
    ssh -i "aws_key.pem" ubuntu@ec2-18-205-103-236.compute-1.amazonaws.com
    ```
2. In the aws cloud server, cd to covert-cnc-exfiltration and run the server.
    ```shell
    cd covert-cnc-exfiltration
    sudo python3 server.py
    ```

### Running the Client
> **Note:** You will have to **start the server first** before running the client.

1. Clone this repository.
    ```shell
    git clone https://github.com/HyejunJeong/covert-cnc-exfiltration
    ```

2. Run the client on the project directory (in the client machine).
    ```shell
    python3 client.py
    ```
> **Note:** By running client.py on your computer will give the cloud server control of your computer (hence "botnet"), so it's good idea to use a virtual machine instead of your actual computer.



## Instructions

1. In the server, wait for the client connection and run ``` list ``` to see connected clients.

2. Run ``` select <client number>``` to select the target
3. Try running a command. 

> **Note:** If your command is invalid, it will print an error message, "No such file or directory: ...". This is because subprocesses uses the underlying POSIX execve and that's what it will report if command is invalid.

4. You can download and upload files using ``` download <file name from target> ``` and ``` upload <file name from C&C computer> ``` to download and send files from/to victim respectively.

> **Note:** If you do download/upload on a file that doesn't exist, it will give you the same error (No such file or directory: ...).

5. Press ``Ctrl-C`` to shut down the server.


## Authors

* **Xiangshuai Gao (110930549)** 
* **Burak Torman (111916573)**
* **Hyejun Jeong (110647348)**
