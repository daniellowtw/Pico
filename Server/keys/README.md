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


# Root and device certificate

The process in brief:

The process for creating your own certificate authority is pretty straight forward: 
* Create a private key
* Self-sign
* Install root CA on your various workstations

Once you do that, every device that you manage via HTTPS just needs to have its own certificate created with the following steps:
* Create CSR for device
* Sign CSR with root CA key

## Generate root certificate

Generate key with 

    openssl genrsa -out rootCA.key 2048

Add -des3 to create a key that is also password protected

Now to create a self signed certificate

    openssl req -x509 -new -nodes -key rootCA.key -days 1024 -out rootCA.pem

(-nodes means if a private key is created, it will not be encrypted)

Install this on to the computer and browser.

## Create a certificate

Create a private key 

    openssl genrsa -out device.key 2048

Next, create a device certificate signing request

    openssl req -new -key device.key -out device.csr

The most important question is "Common Name". It has to be what you see in the browser, even if that means an IP address.

    Common Name (e.g. server FQDN or YOUR name) []:localhost

Then we will sign it with our root CA key

    openssl x509 -req -in device.csr -CA root.pem -CAkey root.key -CAcreateserial -out device.crt -days 500

We can combine this into a pem file as follows

    cat device.key device.crt > device.pem

Source:http://datacenteroverlords.com/2012/03/01/creating-your-own-ssl-certificate-authority/