package org.c4c.jaas.mysql;

import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;


public class App {
    
    public static void main(String[] args) {
    	 System.setProperty("java.security.auth.login.config", "mysqlJaasAuth.config");
    	 
    	 LoginContext lc = null;
         try {
             lc = new LoginContext("mySqlJaas", new MySqlLoginCallbackHandler("admin","admin".toCharArray()));
             lc.login();
            
             Set <RolePrincipal> roles = lc.getSubject().getPrincipals(RolePrincipal.class);
             for (RolePrincipal object : roles) {
				System.out.println(object.getName());
			}
             System.out.println("Authentication succeeded!");
             System.exit(0);
         } catch (LoginException le) {
             System.err.println("Cannot create LoginContext. "
                 + le.getMessage());
             System.exit(-1);
         } catch (SecurityException se) {
             System.err.println("Cannot create LoginContext. "
                 + se.getMessage());
             System.exit(-1);
         }

         

        
    }
}
