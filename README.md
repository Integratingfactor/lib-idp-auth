# lib-idp-auth
IDP Authorization Server library  

This is a work in progress, to create a library that can be used to implement an OAuth2 authorization server based on Spring Security framework. We are starting from default configuration with some hard coded parameters, and then will iterate to make it more flexible and production ready.

# Version 0.0.1
This is a very basic release, with default configurations and some hard coded values, to demonstrate how this library can be used to implement an authorization server.

## What does this release has?
This release has following 2 `@Configuration` beans:
* `OAuth2AuthServerConfig` : this bean configures a very basic authorization server using default configuration from Spring Security Framework and following hard coded client details:
  * client id : `if.test.client`
  * grant type : `authorization_code`
  * authority : `ROLE_CLIENT`
  * scope : `read`
  * redirect url : `http://localhost:8080`
  * resourceId : `test-resource`
* `SecurityConfig` : this bean configures a very basic spring security profile using default configuration from Spring Security Framework and following hard coded user details:
  * username/password : `user`/`password`, roles : `USER`
  * username/password : `admin`/`password`, roles : `USER`, `ADMIN`

## How to implement authorization server with this release?
Clone or download project.  

Build and install project library: `mvn install`  

Add following dependencies into your maven project:
```XML
<!-- iF IDP Library -->
<dependency>
   <groupId>com.integratingfactor.idp</groupId>
   <artifactId>lib-idp-auth</artifactId>
   <version>0.0.2-SNAPSHOT</version>
</dependency>
```

Library uses Javaconfig to configure Spring Security Framework. However, following minimal xml configuration is needed:
* Security filter configuration in `web.xml` as described in Spring Security Framework Reference [Section 4.2.1 web.xml Configuration](http://docs.spring.io/spring-security/site/docs/4.0.3.RELEASE/reference/htmlsingle/#ns-web-xml)
* Configuration bean declaration in application's context file:  
  ```XML
  <!-- Configure OAuth beans -->  
  <bean id="idpOAuthService" class="com.integratingfactor.idp.lib.config.OAuth2AuthServerConfig"/>  

  <!-- Configure security beans -->  
  <bean id="idpSecurityService" class="com.integratingfactor.idp.lib.config.SecurityConfig"/>
  ```

**Note: Spring Security filter only works with resources that are served by DispatcherServlet. So, if you have any static resources being served by default container servlet (e.g. `welcome-file-list`), then they will not be subject to spring security checks.**

## How to get acces token from a client?
Getting access token for a client is a 2 step process:
* Step 1 : Request authorization code
  * use a browser (not postman) to do a `GET http://localhost:8080/oauth/authorize?client_id=if.test.client&response_type=code` (x-www-form-urlencoded)
  * above should redirect your browser to login page
  * use the login credentials configured with UserDetailsService (default: user/password)
  * after user login, browser will be redirected to approval page `http://localhost:8080/oauth/authorize?client_id=if.test.client&response_type=code`
  * this is where authenticated user provides the client access authorization on their behalf
  * once approved, browser will redirect to `http://localhost:8080/?code=\{xxxxxxxx}`
  * authorization code is the `xxxxx` from uri parameter `code` above
* Step 2 : Get access token
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

## How to verify access token from client's request?
Resource servers can verify an access token as following:
* use postman, or similar Rest client, to do a `POST htt[://localhost:8080/oauth/check_token` with following:
    * method type is POST
    * Use x-www-form-urlencoded
    * no authentication/authorization header required (if present, will be ignored)
    * set body key/value as `token` : `f5b05404-2b58-449b-9460-1f9b0637c5f8` (value of access token from above)
  * above should return a response like:  
    ```
      {
        "exp": 1447787342,
        "user_name": "user",
        "scope": [
          "read"
        ],
        "authorities": [
          "ROLE_USER"
        ],
        "aud": [
          "test-resource"
        ],
        "client_id": "if.test.client"
      }
    ```
