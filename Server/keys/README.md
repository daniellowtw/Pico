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
