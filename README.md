# lib-idp-auth
IDP Authorization Server library


## Client Usage
Getting access token for a client is a 2 step process:
* Step 1: Get authorization code
  * use a browser (not postman) to do a `GET http://localhost:8080/oauth/authorize?client_id=if.test.client&response_type=code` (x-www-form-urlencoded)
  * above should redirect your browser to login page
  * use the login credentials configured with UserDetailsService (default: user/password)
  * this will redirect browser to `http://localhost:8080/?code=\{xxxxxxxx}`
  * authorization code is the `xxxxx` from uri parameter `code` above
* Step 2: Get access token
  * use postman, or similar Rest client, to do a `POST http://localhost:8080/oauth/token` with following:
    * method type is POST
    * Use Http-Basic authentication using just the client id `if.test.client` (there is no password)
    * set body key/value as `grant_type` : `authorization_code`
    * set body key/value as `code` : `xxxxxx` (code from step 1)
  * above should return a response like:  
    ```
      {  
          "access_token":"f5b05404-2b58-449b-9460-1f9b0637c5f8",  
          "token_type":"bearer",  
          "expires_in":42312,"scope":"read"  
      }
    ```

## Server Usage
Resource servers can verify an access token as following:
