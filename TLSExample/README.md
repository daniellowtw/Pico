# Setting up the certificate and private key

Generate a 4096 bit private key called `server.key`
    
    openssl genrsa -des3 -out server.key 4096

Generate the decoded key `server_decoded.key` without password so it can be auto loaded

    openssl rsa -in server.key -out server_decoded.key

Generate a self-signed signing request (site.csr) <-- not sure if this step is necessary

    openssl req -new -key server_decoded.key -out server.csr
    cat server.csr > server.crt??

Generate a self-signed certificate `site.crt` in X.509 format

    openssl req -new -x509 -key server_decoded.key -out serverx509.crt

Generate PEM

    cat server_decoded.key serverx509.crt > server.pem

# Running the server

`python server.py` will start a server listening to port 8001. An option is required at the start to decide between a TLS server or not. If TLS option is chosen, the server will use the certificate and private key in server.pem

# Running the client

## Python client (TLS)

`python client.py` will start a client that tries to create a TLS connection to the server.

## Java (TLS)

`javac sslTest/*.` and `java sslTest/SSLTest` will create a TLS connection to the server. The escape character is ~

## Telnet (Non-TLS)

use `telnet <server> <port>` to test the non-TLS connection.

# Verifying that the channel is encrypted

There are few options to verify that the packets are encrypted. On Windows, if the server is hosted on a different machine, Wireshark or [RawCap](http://www.netresec.com/?page=RawCap) can be used to capture the outgoing packets. If it is hosted locally, use RawCap because it allows listening on the loopback interface. On linux, use tcpdump `sudo tcpdump -s 0 -X tcp port 8001 > tcpdump.txt`